package uci.localproxy.headerscreen

import uci.localproxy.BasePresenter
import uci.localproxy.BaseView
import uci.localproxy.proxydata.header.Header

/**
 * Created by daniel on 30/08/18.
 */
interface HeaderListContract {
    interface Presenter : BasePresenter {
        fun loadHeaders()
        fun addNewHeader()
        fun editHeader(requestedHeader: Header)
        fun saveHeader(header: Header)
        fun removeHeader(headerId: String)
        fun onDestroy()
    }

    interface View : BaseView<Presenter?> {
        fun showHeaders(headers: List<Header>)
        fun showAddEditHeaderDialog(header: Header?)
        fun showNoHeaders()
        fun showSuccessfullySavedMessage()
        fun showHeaderNameAlreadyExistError()
        fun showHeaderRemovedMessage()
        val isActive: Boolean
    }
}