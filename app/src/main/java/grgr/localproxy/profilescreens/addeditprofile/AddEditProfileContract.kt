package grgr.localproxy.profilescreens.addeditprofile

import grgr.localproxy.BasePresenter
import grgr.localproxy.BaseView

/**
 * Created by daniel on 17/09/17.
 */
open interface AddEditProfileContract {
    open interface View : BaseView<Presenter?> {
        fun showEmptyProfileError()
        fun finishAddEditProfileActivity()
        fun setName(name: String?)
        fun setServer(server: String?)
        fun setInPort(inPort: String?)
        fun setBypass(bypass: String?)

        //        void setDomain(String domain);
        val isActive: Boolean

        fun setProfileEqualNameError()
        fun setNameEmptyError()
        fun setServerEmptyError()
        fun setServerInvalidError()
        fun setInPortEmptyError()
        fun setInputPortOutOfRangeError()
        fun setBypassSyntaxError() //        void setDomainInvalidError();
    }

    open interface Presenter : BasePresenter {
        fun saveProfile(
            name: String, server: String,
            inPort: String, bypass: String
        )

        fun populateProfile()
        val isDataMissing: Boolean
        fun onDestroy()
    }
}