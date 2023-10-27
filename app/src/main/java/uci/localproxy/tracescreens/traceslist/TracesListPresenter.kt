package uci.localproxy.tracescreens.traceslist

import com.google.common.base.Preconditions
import uci.localproxy.proxydata.firewallRule.FirewallRule
import uci.localproxy.proxydata.firewallRule.FirewallRuleLocalDataSource
import uci.localproxy.proxydata.trace.Trace
import uci.localproxy.proxydata.trace.TraceDataSource
import uci.localproxy.proxydata.trace.TraceDataSource.LoadTracesCallback

/**
 * Created by daniel on 16/02/18.
 */
class TracesListPresenter constructor(view: TracesListContract.View) :
    TracesListContract.Presenter {
    private val mTraceDataSource: TraceDataSource
    private val mFirewallRuleDataSource: FirewallRuleLocalDataSource
    private val mView: TracesListContract.View

    init {
        mTraceDataSource = TraceDataSource.newInstance()
        mFirewallRuleDataSource = FirewallRuleLocalDataSource.newInstance()
        mView = Preconditions.checkNotNull(view, "view cannot be null")
        mView.setPresenter(this)
    }

    public override fun addAsFirewallRule(rule: String?, appPackageName: String?) {
        val firewallRule: FirewallRule =
            FirewallRule.newInstance((rule)!!, (appPackageName)!!, "Imported from traces")
        mFirewallRuleDataSource.saveFirewallRule(firewallRule)
        mView.showSuccessfullyAddedAsFirewallRuleMessage()
    }

    public override fun loadTraces(filter: String?, sortByConsumption: Boolean) {
        loadTraces(filter, sortByConsumption, true)
    }

    public override fun deleteAllTraces() {
        mTraceDataSource.deleteAllTraces()
        mView.showNoTraces()
    }

    private fun loadTraces(filter: String?, sortByConsumption: Boolean, showLoadingUi: Boolean) {
        if (showLoadingUi) {
            mView.setLoadingIndicator(true)
        }
        mTraceDataSource.filterTraces(filter, sortByConsumption, object : LoadTracesCallback {
            public override fun onTracesLoaded(traces: List<Trace>) {
                if (!mView.isActive) return
                mView.showTraces(traces)
                if (showLoadingUi) {
                    mView.setLoadingIndicator(false)
                }
            }

            public override fun onDataNoAvailable() {
                if (!mView.isActive) return
                mView.showNoTraces()
                mView.setLoadingIndicator(false)
            }
        })
    }

    public override fun openTracesDetails(requestedTrace: Trace) {}
    public override fun onDestroy() {
        mTraceDataSource.releaseResources()
        mFirewallRuleDataSource.releaseResources()
    }

    public override fun start() {
        loadTraces(null, false)
    }
}