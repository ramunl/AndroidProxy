package grgr.localproxy.proxyscreen

import grgr.localproxy.BasePresenter
import grgr.localproxy.BaseView
import grgr.localproxy.proxydata.profile.Profile
import grgr.localproxy.proxydata.user.User

/**
 * Created by daniel on 15/09/17.
 */
interface ProxyContract {
    interface Presenter : BasePresenter {
        fun startProxyFromFabButton(
            user: String, pass: String, profileID: String,
            localPort: String, rememberPass: Boolean,
            setGlobalProxy: Boolean
        )

        fun startProxyFromHelpDialog(
            user: String, pass: String, profileID: String,
            localPort: String, rememberPass: Boolean,
            setGlobalProxy: Boolean, dontShowAgain: Boolean
        )

        fun stopCredentialCheckTask()
        fun stopProxy()
        fun onDestroy()
        fun addNewProfile()
        fun goToWifiConfDialog()
        fun goToWifiSettings(dontShowAgain: Boolean)
        fun filterUsers(constraint: String?)
        fun loadUsers()
        fun startServiceResult(message: Int)
    }

    interface View : BaseView<Presenter?> {
        fun enableAllViews()
        fun disableAllViews()
        fun setPlayView()
        fun setStopView()
        fun setUsername(username: String?)
        fun setPassword(password: String?)
        fun setRememberPassword(remember: Boolean)
        fun setLocalPort(localPort: String?)
        fun setGlobalProxyChecked(checked: Boolean)
        fun setSpinnerProfiles(profiles: List<Profile>?)
        fun setSpinnerProfileSelected(profileId: String)
        fun showNoProfilesView()
        fun setUsernameEmptyError()
        fun setPasswordEmptyError()
        fun setProfileNoSelectedError()
        fun setLocalPortEmptyError()
        fun setLocalPortOutOfRangeError()
        fun setLocalPortNotAvailable()
        fun setUsers(users: List<User>)
        fun showAddProfile()

        //        boolean isProxyServiceRunning();
        val isConnectedToAWifi: Boolean

        fun stopProxyService()
        fun startProxyService(
            username: String?, password: String?, server: String?,
            inputport: Int, outputport: Int, bypass: String?,
            domain: String?, setGlobProxy: Boolean
        )

        fun showWifiConfDialog()
        fun startWifiConfActivity()
        fun showProgressDialog(show: Boolean)
        fun showWrongCredentialsDialog()
        fun showNetworkError()
        fun showConnectionTimeOutError()
        fun showUnknownHostError()
        fun showConnectionError()
        fun showErrorStartingService()
    }
}