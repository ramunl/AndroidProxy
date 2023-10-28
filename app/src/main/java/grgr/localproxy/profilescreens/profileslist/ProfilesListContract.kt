package grgr.localproxy.profilescreens.profileslist

import grgr.localproxy.BasePresenter
import grgr.localproxy.BaseView
import grgr.localproxy.proxydata.profile.Profile

/**
 * Created by daniel on 16/09/17.
 */
open interface ProfilesListContract {
    open interface Presenter : BasePresenter {
        fun result(requestCode: Int, resultCode: Int)
        fun loadProfiles()
        fun addNewProfile()
        fun openProfileDetails(requestedProfile: Profile)
        fun onDestroy()
    }

    open interface View : BaseView<Presenter?> {
        fun setLoadingIndicator(active: Boolean)
        fun showProfiles(profiles: List<Profile>?)
        fun showAddProfile()
        fun showNoProfiles()
        fun showProfileDetailsUI(profileId: String?)
        fun showSuccessfullySavedMessage()
        val isActive: Boolean
    }
}