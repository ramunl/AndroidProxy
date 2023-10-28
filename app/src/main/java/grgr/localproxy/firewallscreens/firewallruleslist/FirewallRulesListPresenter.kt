package grgr.localproxy.firewallscreens.firewallruleslist

import android.app.Activity
import grgr.localproxy.firewallscreens.addeditfirewallrule.AddEditFirewallRuleActivity
import grgr.localproxy.proxydata.firewallRule.FirewallRule
import grgr.localproxy.proxydata.firewallRule.FirewallRuleDataSource.LoadFirewallRulesCallback
import grgr.localproxy.proxydata.firewallRule.FirewallRuleLocalDataSource

/**
 * Created by daniel on 29/09/17.
 */
class FirewallRulesListPresenter constructor(private val mFirewallRulesView: FirewallRulesListContract.View) :
    FirewallRulesListContract.Presenter {
    private val mFirewallRulesDataSource: FirewallRuleLocalDataSource

    init {
        mFirewallRulesDataSource = FirewallRuleLocalDataSource.newInstance()
        mFirewallRulesView.setPresenter(this)
    }

    public override fun start() {
        loadFirewallRules()
    }

    public override fun result(requestCode: Int, resultCode: Int) {
        // If a profile was successfully added, show snackbar
        if (AddEditFirewallRuleActivity.Companion.REQUEST_ADD_FIREWALL_RULE == requestCode && Activity.RESULT_OK == resultCode) {
            mFirewallRulesView.showSuccessfullySavedMessage()
        }
    }

    public override fun loadFirewallRules() {
        mFirewallRulesDataSource.getFirewallRules(object : LoadFirewallRulesCallback {
            public override fun onFirewallRulesLoaded(firewallRules: List<FirewallRule>) {
                if (!mFirewallRulesView.isActive) return
                mFirewallRulesView.showFirewallRules(firewallRules)
            }

            public override fun onDataNoAvailable() {
                if (!mFirewallRulesView.isActive) return
                mFirewallRulesView.showNoFirewallRules()
            }
        })
    }

    public override fun addNewFirewallRule() {
        mFirewallRulesView.showAddFirewallRule()
    }

    public override fun openFirewallRuleDetails(requestedFirewallRule: FirewallRule) {
        mFirewallRulesView.showFirewallRuleDetailsUI(requestedFirewallRule.getId())
    }

    public override fun onDestroy() {
        mFirewallRulesDataSource.releaseResources()
    }

    public override fun activateFirewallRule(
        requestedFirewallRule: FirewallRule,
        activate: Boolean
    ) {
        if (activate) {
            mFirewallRulesDataSource.activateFirewallRule(requestedFirewallRule.getId())
            mFirewallRulesView.showFirewallRuleActivated()
        } else {
            mFirewallRulesDataSource.deactivateFirewallRule(requestedFirewallRule.getId())
            mFirewallRulesView.showFirewallRuleDeactivate()
        }
        loadFirewallRules()
    }
}