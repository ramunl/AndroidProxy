package grgr.localproxy.proxyscreen

import android.os.AsyncTask
import android.os.Build
import com.google.common.base.Strings
import cz.msebera.android.httpclient.HttpHost
import cz.msebera.android.httpclient.HttpResponse
import cz.msebera.android.httpclient.auth.AuthScope
import cz.msebera.android.httpclient.auth.NTCredentials
import cz.msebera.android.httpclient.client.ClientProtocolException
import cz.msebera.android.httpclient.client.CredentialsProvider
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.conn.ConnectTimeoutException
import cz.msebera.android.httpclient.impl.client.BasicCredentialsProvider
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder
import grgr.localproxy.LocalProxyApplication
import grgr.localproxy.profilescreens.addeditprofile.AddEditProfilePresenter
import grgr.localproxy.proxycore.ProxyService
import grgr.localproxy.proxydata.firewallRule.FirewallRuleLocalDataSource
import grgr.localproxy.proxydata.pref.AppPreferencesHelper
import grgr.localproxy.proxydata.profile.Profile
import grgr.localproxy.proxydata.profile.source.ProfilesDataSource.GetProfileCallback
import grgr.localproxy.proxydata.profile.source.ProfilesDataSource.LoadProfilesCallback
import grgr.localproxy.proxydata.profile.source.ProfilesLocalDataSource
import grgr.localproxy.proxydata.user.User
import grgr.localproxy.proxydata.user.UsersDataSource.FilterUsersCallback
import grgr.localproxy.proxydata.user.UsersDataSource.GetUserCallback
import grgr.localproxy.proxydata.user.UsersDataSource.LoadUsersCallback
import grgr.localproxy.proxydata.user.UsersDataSource.SaveUpdateUserCallback
import grgr.localproxy.proxydata.user.UsersLocalDataSource
import grgr.localproxy.proxyutil.network.PortsUtils
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Created by daniel on 20/09/17.
 */
class ProxyPresenter(
    private val mProxyView: ProxyContract.View,
    private val mPrefHelper: AppPreferencesHelper
) : ProxyContract.Presenter {
    private val mProfileLocalDataSource: ProfilesLocalDataSource
    private val mUsersLocalDataSource: UsersLocalDataSource
    private val mFirewallRulesLocalDataSource: FirewallRuleLocalDataSource
    private var credentialsCheckTask: CredentialsCheckTask? = null

    init {
        mProfileLocalDataSource = ProfilesLocalDataSource.newInstance()
        mUsersLocalDataSource = UsersLocalDataSource.newInstance()
        mFirewallRulesLocalDataSource = FirewallRuleLocalDataSource.newInstance()
        mProxyView.setPresenter(this)
    }

    override fun startProxyFromFabButton(
        username: String, password: String, profileId: String,
        localPort: String, rememberPass: Boolean,
        setGlobalProxy: Boolean
    ) {
        var setGlobalProxy = setGlobalProxy
        val isValidData = validateData(username, password, profileId, localPort)
        if (!isValidData) return
        if (Build.VERSION.SDK_INT > LocalProxyApplication.MAX_SDK_SUPPORTED_FOR_WIFI_CONF &&
            !mPrefHelper.dontShowDialogAgain
        ) {
            mProxyView.showWifiConfDialog()
            return
        }
        setGlobalProxy =
            Build.VERSION.SDK_INT <= LocalProxyApplication.MAX_SDK_SUPPORTED_FOR_WIFI_CONF && setGlobalProxy
        startProxy(username, password, profileId, localPort, rememberPass, setGlobalProxy)
    }

    override fun startProxyFromHelpDialog(
        user: String, pass: String,
        profileID: String, localPort: String,
        rememberPass: Boolean, setGlobalProxy: Boolean, dontShowAgain: Boolean
    ) {
        var setGlobalProxy = setGlobalProxy
        if (dontShowAgain) {
            mPrefHelper.dontShowDialogAgain = true
        }
        setGlobalProxy =
            Build.VERSION.SDK_INT <= LocalProxyApplication.MAX_SDK_SUPPORTED_FOR_WIFI_CONF && setGlobalProxy
        val isValidData = validateData(user, pass, profileID, localPort)
        if (!isValidData) return
        startProxy(user, pass, profileID, localPort, rememberPass, setGlobalProxy)
    }

    private fun startProxy(
        username: String, password: String, profileId: String,
        localPort: String, rememberPass: Boolean,
        setGlobalProxy: Boolean
    ) {
        saveUpdateUser(username, password, rememberPass)
        saveConfiguration(username, profileId, localPort, setGlobalProxy)

//        if (!mProxyView.isConnectedToAWifi()) {
//            mProxyView.showNetworkError();
//            return;
//        }
        mProfileLocalDataSource.getProfile(profileId, object : GetProfileCallback {
            override fun onProfileLoaded(profile: Profile) {
                var user = username
                var domain = ""
                if (username.contains("\\")) {
                    val backSlashPos = username.indexOf("\\")
                    domain = username.substring(0, backSlashPos)
                    user = username.substring(backSlashPos + 1, username.length)
                }
                credentialsCheckTask = CredentialsCheckTask(
                    user,
                    password,
                    profile.host,
                    profile.inPort, localPort.toInt(),
                    profile.bypass,
                    domain,
                    setGlobalProxy
                )
                credentialsCheckTask!!.execute()
            }

            override fun onDataNoAvailable() {
                //never happens in this scenario
            }
        })
    }

    override fun stopCredentialCheckTask() {
        if (credentialsCheckTask != null) {
            credentialsCheckTask!!.cancel(true)
        }
    }

    override fun stopProxy() {
        mProxyView.stopProxyService()
        mProxyView.enableAllViews()
        mProxyView.setPlayView()
    }

    private fun saveUpdateUser(username: String, password: String, rememberPass: Boolean) {
        mUsersLocalDataSource.getUserByUsername(username, object : GetUserCallback {
            override fun onUserLoaded(user: User) {
                val userToUpdate = if (rememberPass) User.newUser(
                    user.id,
                    user.username,
                    password
                ) else User.newUser(user.id, user.username, "")
                mUsersLocalDataSource.updateUser(userToUpdate, object : SaveUpdateUserCallback {
                    override fun onUserSaved() {
                        //Nothing Ok
                    }

                    override fun onUsernameAlreadyExist() {
                        //Nothing, never happen in this scenario
                    }
                })
            }

            override fun onDataNoAvailable() {
                val user = if (rememberPass) User.newUser(username, password) else User.newUser(
                    username,
                    ""
                )
                mUsersLocalDataSource.saveUser(user, object : SaveUpdateUserCallback {
                    override fun onUserSaved() {
                        //Nothing Ok
                    }

                    override fun onUsernameAlreadyExist() {
                        //Nothing, never happen in this scenario
                    }
                })
            }
        })
    }

    private fun validateData(
        user: String,
        pass: String,
        profileId: String,
        localPort: String
    ): Boolean {
        var isValid = true
        if (Strings.isNullOrEmpty(user)) {
            mProxyView.setUsernameEmptyError()
            isValid = false
        }
        if (Strings.isNullOrEmpty(pass)) {
            mProxyView.setPasswordEmptyError()
            isValid = false
        }
        if (Strings.isNullOrEmpty(profileId)) {
            mProxyView.setProfileNoSelectedError()
            isValid = false
        }
        if (Strings.isNullOrEmpty(localPort)) {
            mProxyView.setLocalPortEmptyError()
            isValid = false
        }
        if (!Strings.isNullOrEmpty(localPort) &&
            (localPort.toInt() <= AddEditProfilePresenter.Companion.MAX_SYSTEM_PORTS_LIMIT ||
                    localPort.toInt() > AddEditProfilePresenter.Companion.MAX_PORTS_LIMIT)
        ) {
            mProxyView.setLocalPortOutOfRangeError()
            isValid = false
        }
        if (!Strings.isNullOrEmpty(localPort) && localPort.toInt() > AddEditProfilePresenter.Companion.MAX_SYSTEM_PORTS_LIMIT && localPort.toInt() <= AddEditProfilePresenter.Companion.MAX_PORTS_LIMIT &&
            !isLocalPortAvailable(localPort.toInt())
        ) {
            mProxyView.setLocalPortNotAvailable()
            isValid = false
        }
        return isValid
    }

    override fun onDestroy() {
        mProfileLocalDataSource.releaseResources()
        mUsersLocalDataSource.releaseResources()
        mFirewallRulesLocalDataSource.releaseResources()
    }

    override fun addNewProfile() {
        mProxyView.showAddProfile()
    }

    override fun goToWifiConfDialog() {
        if (mPrefHelper.dontShowDialogAgain) {
            goToWifiSettings(false)
        } else {
            mProxyView.showWifiConfDialog()
        }
    }

    override fun goToWifiSettings(dontShowAgain: Boolean) {
        if (dontShowAgain) {
            mPrefHelper.dontShowDialogAgain = true
        }
        mProxyView.startWifiConfActivity()
    }

    override fun filterUsers(constraint: String?) {
        mUsersLocalDataSource.filterByUsernameUsers(constraint!!, object : FilterUsersCallback {
            override fun onUsersFiltered(users: List<User>) {
                mProxyView.setUsers(users)
            }

            override fun onDataNoAvailable() {
                mProxyView.setUsers(ArrayList(0))
            }
        })
    }

    override fun start() {
        loadUsers()
        loadProfiles()
        loadLastConfiguration()
        if (ProxyService.IS_SERVICE_RUNNING) {
            mProxyView.setStopView()
            mProxyView.disableAllViews()
        } else {
            mProxyView.setPlayView()
            mProxyView.enableAllViews()
        }
    }

    override fun loadUsers() {
        mUsersLocalDataSource.getUsers(object : LoadUsersCallback {
            override fun onUsersLoaded(users: List<User>) {
                mProxyView.setUsers(users)
            }

            override fun onDataNoAvailable() {
                mProxyView.setUsers(ArrayList(0))
            }
        })
    }

    override fun startServiceResult(message: Int) {
        if (message == ProxyService.ERROR_STARTING_SERVICE) {
            mProxyView.showErrorStartingService()
            stopProxy()
        }
    }

    private fun loadProfiles() {
        mProfileLocalDataSource.getProfiles(object : LoadProfilesCallback {
            override fun onProfilesLoaded(profiles: List<Profile>) {
                mProxyView.setSpinnerProfiles(profiles)
            }

            override fun onDataNoAvailable() {
                mProxyView.showNoProfilesView()
            }
        })
    }

    private fun saveConfiguration(
        username: String,
        profileId: String,
        localPort: String,
        setGlobProxy: Boolean
    ) {
        mUsersLocalDataSource.getUserByUsername(username, object : GetUserCallback {
            override fun onUserLoaded(user: User) {
                mPrefHelper.currentUserId = user.id
                mPrefHelper.currentProfileId = profileId
                mPrefHelper.currentLocalPort = localPort.toInt()
                mPrefHelper.currentIsSetGlobProxy = setGlobProxy
            }

            override fun onDataNoAvailable() {
                //Never happens in this scenario, because the user was already saved
                //at the beginning in the start proxy method, its to say the user always
                //will be loaded
                mPrefHelper.currentUserId = ""
                mPrefHelper.currentProfileId = profileId
                mPrefHelper.currentLocalPort = localPort.toInt()
                mPrefHelper.currentIsSetGlobProxy = setGlobProxy
            }
        })
    }

    private fun loadLastConfiguration() {
        val userId = mPrefHelper.currentUserId
        if (!Strings.isNullOrEmpty(userId)) {
            mUsersLocalDataSource.getUser(userId, object : GetUserCallback {
                override fun onUserLoaded(user: User) {
                    mProxyView.setUsername(user.username)
                    if (!Strings.isNullOrEmpty(user.password)) {
                        mProxyView.setPassword(user.password)
                        mProxyView.setRememberPassword(true)
                    } else {
                        mProxyView.setPassword("")
                        mProxyView.setRememberPassword(false)
                    }
                }

                override fun onDataNoAvailable() {
                    mProxyView.setUsername("")
                    mProxyView.setPassword("")
                    mProxyView.setRememberPassword(false)
                }
            })
        }
        val profileId = mPrefHelper.currentProfileId
        if (!Strings.isNullOrEmpty(profileId)) {
            mProxyView.setSpinnerProfileSelected(profileId)
        }
        val localPort = mPrefHelper.currentLocalPort
        if (localPort > -1) {
            mProxyView.setLocalPort(localPort.toString())
        }
        val globProxy = mPrefHelper.currentIsSetGlobProxy
        mProxyView.setGlobalProxyChecked(globProxy)
    }

    private fun isLocalPortAvailable(port: Int): Boolean {
        return PortsUtils.isPortAvailable(port)
    }

    private inner class CredentialsCheckTask(
        private val username: String?,
        private val password: String,
        private val proxyHost: String,
        private val proxyPort: Int,
        private val localPort: Int,
        private val bypass: String,
        private val domain: String?,
        private val setGlobProxy: Boolean
    ) : AsyncTask<Any?, Any?, Int>() {


        override fun onPreExecute() {
            super.onPreExecute()
            mProxyView.showProgressDialog(true)
        }

        override fun doInBackground(vararg objects: Any?): Int {
            return try {
                val credentials: CredentialsProvider = BasicCredentialsProvider()
                credentials.setCredentials(
                    AuthScope(AuthScope.ANY),
                    NTCredentials(
                        username, password, InetAddress.getLocalHost().hostName,
                        if (Strings.isNullOrEmpty(domain)) null else domain
                    )
                )
                val client = HttpClientBuilder.create()
                    .setProxy(HttpHost(proxyHost, proxyPort))
                    .setDefaultCredentialsProvider(credentials)
                    .disableRedirectHandling()
                    .build()
                val response: HttpResponse = client.execute(HttpGet("http://google.com"))
                //                Log.e("auth_status_code", response.getStatusLine().getStatusCode() + "");
                if (response.statusLine.statusCode == 407) {
                    AUTHENTICATION_FAILED
                } else AUTHENTICATION_SUCCEED
            } catch (e: ClientProtocolException) {
                e.printStackTrace()
                CONNECTION_ERROR
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                UNKNOWN_HOST
            } catch (e: ConnectTimeoutException) {
                e.printStackTrace()
                CONNECTION_TIMEOUT
            } catch (e: IOException) {
                e.printStackTrace()
                CONNECTION_ERROR
            }
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            mProxyView.showProgressDialog(false)
            when (result) {
                AUTHENTICATION_SUCCEED -> {
                    //start proxy
                    mProxyView.startProxyService(
                        username, password,
                        proxyHost,
                        proxyPort,
                        localPort,
                        bypass,
                        if (Strings.isNullOrEmpty(domain)) null else domain,
                        setGlobProxy
                    )
                    mProxyView.disableAllViews()
                    mProxyView.setStopView()
                }

                AUTHENTICATION_FAILED -> mProxyView.showWrongCredentialsDialog()
                UNKNOWN_HOST -> mProxyView.showUnknownHostError()
                CONNECTION_TIMEOUT -> mProxyView.showConnectionTimeOutError()
                CONNECTION_ERROR -> mProxyView.showConnectionError()
            }
        }
    }

    companion object {
        private const val UNKNOWN_HOST = 1
        private const val CONNECTION_TIMEOUT = 2
        private const val AUTHENTICATION_FAILED = 3
        private const val AUTHENTICATION_SUCCEED = 4
        private const val CONNECTION_ERROR = 5
    }
}