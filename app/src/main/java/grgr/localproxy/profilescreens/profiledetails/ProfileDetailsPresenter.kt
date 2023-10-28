package grgr.localproxy.profilescreens.profiledetails

import android.app.Activity
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import grgr.localproxy.proxydata.profile.Profile
import grgr.localproxy.proxydata.profile.source.ProfilesDataSource.GetProfileCallback
import grgr.localproxy.proxydata.profile.source.ProfilesLocalDataSource

/**
 * Created by daniel on 18/09/17.
 */
class ProfileDetailsPresenter constructor(
    profilesDetailsView: ProfileDetailsContract.View,
    private val mProfileId: String?
) : ProfileDetailsContract.Presenter {
    private val mProfilesLocalDataSource: ProfilesLocalDataSource
    private val mProfilesDetailsView: ProfileDetailsContract.View

    init {
        mProfilesLocalDataSource = ProfilesLocalDataSource.newInstance()
        mProfilesDetailsView = Preconditions.checkNotNull(profilesDetailsView)
        mProfilesDetailsView.setPresenter(this)
    }

    public override fun start() {
        openProfile()
    }

    private fun openProfile() {
        if (Strings.isNullOrEmpty(mProfileId)) {
            mProfilesDetailsView.showMissingProfile()
            return
        }
        mProfilesLocalDataSource.getProfile((mProfileId)!!, object : GetProfileCallback {
            public override fun onProfileLoaded(profile: Profile) {
                if (!mProfilesDetailsView.isActive) {
                    return
                }
                showProfile(profile)
            }

            public override fun onDataNoAvailable() {
                if (!mProfilesDetailsView.isActive) {
                    return
                }
                mProfilesDetailsView.showMissingProfile()
            }
        })
    }

    private fun showProfile(profile: Profile) {
        if (!mProfilesDetailsView.isActive) {
            return
        }
        mProfilesDetailsView.showName(profile.getName())
        mProfilesDetailsView.showServer(profile.getHost())
        mProfilesDetailsView.showInPort(profile.getInPort())
        mProfilesDetailsView.showBypass(profile.getBypass())
        //        mProfilesDetailsView.showDomain(profile.getDomain());
    }

    public override fun editProfile() {
        if (Strings.isNullOrEmpty(mProfileId)) {
            mProfilesDetailsView.showMissingProfile()
            return
        }
        mProfilesDetailsView.showEditProfile(mProfileId)
    }

    public override fun deleteProfile() {
        if (Strings.isNullOrEmpty(mProfileId)) {
            mProfilesDetailsView.showMissingProfile()
            return
        }
        mProfilesLocalDataSource.deleteProfile((mProfileId)!!)
        mProfilesDetailsView.showProfileDeleted()
    }

    public override fun result(requestCode: Int, resultCode: Int) {
        if (requestCode == ProfileDetailsFragment.Companion.REQUEST_EDIT_PROFILE &&
            resultCode == Activity.RESULT_OK
        ) {
            mProfilesDetailsView.showSuccessfullyUpdatedMessage()
        }
    }

    public override fun onDestroy() {
        mProfilesLocalDataSource.releaseResources()
    }
}