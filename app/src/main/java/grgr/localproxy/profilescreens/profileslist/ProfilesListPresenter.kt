package grgr.localproxy.profilescreens.profileslist

import android.app.Activity
import com.google.common.base.Preconditions
import grgr.localproxy.profilescreens.addeditprofile.AddEditProfileActivity
import grgr.localproxy.proxydata.profile.Profile
import grgr.localproxy.proxydata.profile.source.ProfilesDataSource.LoadProfilesCallback
import grgr.localproxy.proxydata.profile.source.ProfilesLocalDataSource

/**
 * Created by daniel on 16/09/17.
 */
class ProfilesListPresenter constructor(profilesView: ProfilesListContract.View) :
    ProfilesListContract.Presenter {
    private val mProfileDataSource: ProfilesLocalDataSource
    private val mProfilesView: ProfilesListContract.View

    init {
        mProfileDataSource = ProfilesLocalDataSource.newInstance()
        mProfilesView = Preconditions.checkNotNull(profilesView, "profilesView cannot be null")
        mProfilesView.setPresenter(this)
    }

    public override fun start() {
        loadProfiles()
    }

    public override fun result(requestCode: Int, resultCode: Int) {
        // If a profile was successfully added, show snackbar
        if (AddEditProfileActivity.Companion.REQUEST_ADD_PROFILE == requestCode && Activity.RESULT_OK == resultCode) {
            mProfilesView.showSuccessfullySavedMessage()
        }
    }

    public override fun loadProfiles() {
        loadProfiles(false)
    }

    private fun loadProfiles(showLoadingUi: Boolean) {
        if (showLoadingUi) {
            mProfilesView.setLoadingIndicator(true)
        }
        mProfileDataSource.getProfiles(object : LoadProfilesCallback {
            public override fun onProfilesLoaded(profiles: List<Profile>) {
                if (!mProfilesView.isActive) return
                mProfilesView.showProfiles(profiles)
                if (showLoadingUi) {
                    mProfilesView.setLoadingIndicator(false)
                }
            }

            public override fun onDataNoAvailable() {
                if (!mProfilesView.isActive) return
                mProfilesView.showNoProfiles()
            }
        })
    }

    public override fun addNewProfile() {
        mProfilesView.showAddProfile()
    }

    public override fun openProfileDetails(requestedProfile: Profile) {
        mProfilesView.showProfileDetailsUI(requestedProfile.getId())
    }

    public override fun onDestroy() {
        mProfileDataSource.releaseResources()
    }
}