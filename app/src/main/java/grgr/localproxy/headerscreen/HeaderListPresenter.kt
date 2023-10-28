package grgr.localproxy.headerscreen

import grgr.localproxy.proxydata.header.Header
import grgr.localproxy.proxydata.header.HeaderDataSource
import grgr.localproxy.proxydata.header.HeaderDataSource.LoadHeadersCallback
import grgr.localproxy.proxydata.header.HeaderDataSource.SaveUpdateHeaderCallback

/**
 * Created by daniel on 30/08/18.
 */
class HeaderListPresenter(private val mView: HeaderListContract.View) :
    HeaderListContract.Presenter {
    private val mHeadersDataSource: HeaderDataSource

    init {
        mHeadersDataSource = HeaderDataSource.newInstance()
        mView.setPresenter(this)
    }

    override fun loadHeaders() {
        mHeadersDataSource.getAllHeaders(object : LoadHeadersCallback {
            override fun onHeadersLoaded(headers: List<Header>) {
                if (!mView.isActive) return
                mView.showHeaders(headers)
            }

            override fun onDataNoAvailable() {
                if (!mView.isActive) return
                mView.showNoHeaders()
            }
        })
    }

    override fun addNewHeader() {
        mView.showAddEditHeaderDialog(null)
    }

    override fun editHeader(requestedHeader: Header) {
        mView.showAddEditHeaderDialog(requestedHeader)
    }

    override fun saveHeader(header: Header) {
        mHeadersDataSource.saveUpdateHeader(header, object : SaveUpdateHeaderCallback {
            override fun onHeaderSaved() {
                loadHeaders()
                mView.showSuccessfullySavedMessage()
            }

            override fun onHeaderNameAlreadyExist() {
                mView.showHeaderNameAlreadyExistError()
            }
        })
    }

    override fun removeHeader(headerId: String) {
        mHeadersDataSource.removeHeader(headerId)
        loadHeaders()
        mView.showHeaderRemovedMessage()
    }

    override fun onDestroy() {
        mHeadersDataSource.releaseResources()
    }

    override fun start() {
        loadHeaders()
    }
}