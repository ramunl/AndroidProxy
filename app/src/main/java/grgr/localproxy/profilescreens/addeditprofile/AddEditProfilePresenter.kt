package grgr.localproxy.profilescreens.addeditprofile

import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.google.common.net.InetAddresses
import com.google.common.net.InternetDomainName
import grgr.localproxy.proxydata.profile.Profile
import grgr.localproxy.proxydata.profile.source.ProfilesDataSource.GetProfileCallback
import grgr.localproxy.proxydata.profile.source.ProfilesDataSource.SaveProfileCallback
import grgr.localproxy.proxydata.profile.source.ProfilesDataSource.UpdateProfileCallback
import grgr.localproxy.proxydata.profile.source.ProfilesLocalDataSource

/**
 * Created by daniel on 17/09/17.
 */
class AddEditProfilePresenter constructor(
    mAddProfileView: AddEditProfileContract.View,
    private val mProfileId: String?, override var isDataMissing: Boolean
) : AddEditProfileContract.Presenter {
    private val mProfilesDataSource: ProfilesLocalDataSource
    private val mAddProfileView: AddEditProfileContract.View

    init {
        mProfilesDataSource = ProfilesLocalDataSource.newInstance()
        this.mAddProfileView = Preconditions.checkNotNull(mAddProfileView)
        mAddProfileView.setPresenter(this)
    }

    public override fun start() {
        if (!isNewProfile && isDataMissing) {
            populateProfile()
        }
    }

    public override fun saveProfile(
        name: String, server: String,
        inPort: String, bypass: String
    ) {
        if (isNewProfile) {
            createProfile(name, server, inPort, bypass)
        } else {
            updateProfile(name, server, inPort, bypass)
        }
    }

    public override fun populateProfile() {
        if (isNewProfile) {
            throw RuntimeException("populateProfile() was called but profile is new.")
        }
        mProfilesDataSource.getProfile((mProfileId)!!, object : GetProfileCallback {
            public override fun onProfileLoaded(profile: Profile) {
                if (!mAddProfileView.isActive) return
                mAddProfileView.setName(profile.getName())
                mAddProfileView.setServer(profile.getHost())
                mAddProfileView.setBypass(profile.getBypass())
                mAddProfileView.setInPort(profile.getInPort().toString())
                isDataMissing = false
            }

            public override fun onDataNoAvailable() {
                if (!mAddProfileView.isActive) return
                mAddProfileView.showEmptyProfileError()
            }
        })
    }

    public override fun onDestroy() {
        mProfilesDataSource.releaseResources()
    }

    private val isNewProfile: Boolean
        private get() {
            return mProfileId == null
        }

    private fun createProfile(
        name: String, server: String,
        inPort: String, bypass: String
    ) {
        val isValidData: Boolean = validateData(
            name, server,
            inPort, bypass
        )
        if (!isValidData) return
        val profile: Profile = Profile.newProfile(name, server, inPort.toInt(), bypass)
        mProfilesDataSource.saveProfile(profile, object : SaveProfileCallback {
            public override fun onProfileSaved() {
                mAddProfileView.finishAddEditProfileActivity()
            }

            public override fun onProfileNameAlreadyExist() {
                if (mAddProfileView.isActive) {
                    mAddProfileView.setProfileEqualNameError()
                }
            }
        })
    }

    private fun updateProfile(
        name: String, server: String,
        inPort: String, bypass: String
    ) {
        val isValidData: Boolean = validateData(
            name, server,
            inPort, bypass
        )
        if (!isValidData) return
        val profile: Profile = Profile.newProfile(
            mProfileId, name, server, inPort.toInt(), bypass
        )
        mProfilesDataSource.updateProfile(profile, object : UpdateProfileCallback {
            public override fun onProfileUpdated() {
                mAddProfileView.finishAddEditProfileActivity()
            }

            public override fun onProfileNameAlreadyExist() {
                if (mAddProfileView.isActive) {
                    mAddProfileView.setProfileEqualNameError()
                }
            }
        })
    }

    private fun validateData(
        name: String, server: String,
        inPort: String, bypass: String
    ): Boolean {
        var isValid: Boolean = true
        if (Strings.isNullOrEmpty(name)) {
            mAddProfileView.setNameEmptyError()
            isValid = false
        }
        if (Strings.isNullOrEmpty(server)) {
            mAddProfileView.setServerEmptyError()
            isValid = false
        }
        if (!InternetDomainName.isValid(server) && !InetAddresses.isInetAddress(server)) {
            mAddProfileView.setServerInvalidError()
            isValid = false
        }
        if (Strings.isNullOrEmpty(inPort)) {
            mAddProfileView.setInPortEmptyError()
            isValid = false
        }
        if (!Strings.isNullOrEmpty(inPort) &&
            (inPort.toInt() < 0 ||
                    inPort.toInt() > MAX_PORTS_LIMIT)
        ) {
            mAddProfileView.setInputPortOutOfRangeError()
            isValid = false
        }

//        if (!Strings.isNullOrEmpty(domain) && !InternetDomainName.isValid(domain)){
//            mAddProfileView.setDomainInvalidError();
//            isValid = false;
//        }
        if (!isValidBypassSyntax(bypass)) {
            mAddProfileView.setBypassSyntaxError()
            isValid = false
        }
        return isValid
    }

    //TODO
    private fun isValidBypassSyntax(bypass: String): Boolean {
//        String validIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
//        String validHostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
//
//        StringBuilder regexSb = new StringBuilder();
//        Formatter formatter = new Formatter(regexSb, Locale.US);
//
//        String regex = "%1 | %2 | +([%1],%2) ";
//        regex = regex.replace("%1", validIpAddressRegex);
//        regex = regex.replace("%2", validHostnameRegex);
//
//        Pattern pattern = Pattern.compile(regex);
//        return pattern.matcher(bypass).find();
        return true
    }

    companion object {
        val MAX_PORTS_LIMIT: Int = 65535
        val MAX_SYSTEM_PORTS_LIMIT: Int = 1023
    }
}