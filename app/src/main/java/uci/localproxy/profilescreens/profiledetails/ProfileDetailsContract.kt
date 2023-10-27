package uci.localproxy.profilescreens.profiledetails

import uci.localproxy.BasePresenter
import uci.localproxy.BaseView

/**
 * Created by daniel on 18/09/17.
 */
open interface ProfileDetailsContract {
    open interface Presenter : BasePresenter {
        fun editProfile()
        fun deleteProfile()
        fun result(requestCode: Int, resultCode: Int)
        fun onDestroy()
    }

    open interface View : BaseView<Presenter?> {
        fun showMissingProfile()
        fun showName(name: String?)
        fun showServer(server: String?)
        fun showInPort(inPort: Int)
        fun showBypass(bypass: String?)

        //        void showDomain(String domain);
        fun showEditProfile(profileId: String?)
        fun showProfileDeleted()
        fun showSuccessfullyUpdatedMessage()
        val isActive: Boolean
    }
}