package uci.localproxy.firewallscreens.firewallruleslist

import uci.localproxy.BasePresenter
import uci.localproxy.BaseView
import uci.localproxy.proxydata.firewallRule.FirewallRule

/**
 * Created by daniel on 29/09/17.
 */
open interface FirewallRulesListContract {
    open interface Presenter : BasePresenter {
        fun result(requestCode: Int, resultCode: Int)
        fun loadFirewallRules()
        fun addNewFirewallRule()
        fun openFirewallRuleDetails(requestedFirewallRule: FirewallRule)
        fun onDestroy()
        fun activateFirewallRule(requestedFirewallRule: FirewallRule, activate: Boolean)
    }

    open interface View : BaseView<Presenter?> {
        fun showFirewallRules(firewallRules: List<FirewallRule>)
        fun showAddFirewallRule()
        fun showNoFirewallRules()
        fun showFirewallRuleDetailsUI(firewallRuleId: String?)
        fun showSuccessfullySavedMessage()
        fun showFirewallRuleActivated()
        fun showFirewallRuleDeactivate()
        val isActive: Boolean
    }
}