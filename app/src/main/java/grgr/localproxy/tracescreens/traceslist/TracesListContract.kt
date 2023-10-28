package grgr.localproxy.tracescreens.traceslist

import grgr.localproxy.BasePresenter
import grgr.localproxy.BaseView
import grgr.localproxy.proxydata.trace.Trace

/**
 * Created by daniel on 16/02/18.
 */
interface TracesListContract {
    interface View : BaseView<Presenter?> {
        fun setLoadingIndicator(active: Boolean)
        fun showTraces(traces: List<Trace>)
        fun showSuccessfullyAddedAsFirewallRuleMessage()
        fun showNoTraces()
        val isActive: Boolean
    }

    interface Presenter : BasePresenter {
        fun addAsFirewallRule(rule: String?, appPackageName: String?)
        fun loadTraces(filter: String?, sortByConsumption: Boolean)
        fun deleteAllTraces()
        fun openTracesDetails(requestedTrace: Trace)
        fun onDestroy()
    }
}