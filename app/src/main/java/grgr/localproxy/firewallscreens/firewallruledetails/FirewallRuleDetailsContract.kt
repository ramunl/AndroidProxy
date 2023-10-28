package grgr.localproxy.firewallscreens.firewallruledetails

import grgr.localproxy.BasePresenter
import grgr.localproxy.BaseView

/**
 * Created by daniel on 2/10/17.
 */
open interface FirewallRuleDetailsContract {
    open interface Presenter : BasePresenter {
        fun editFirewallRule()
        fun deleteFirewallRule()
        fun result(requestCode: Int, resultCode: Int)
        fun onDestroy()
    }

    open interface View : BaseView<Presenter?> {
        fun showMissingFirewallRule()
        fun showRule(rule: String?)
        fun showDescription(description: String?)
        fun showFirewallRuleDeleted()
        fun showApplicationName(packageName: String?)
        fun showAllApplicationPackageName()
        fun showPackageLogo(packageName: String)
        fun showNoPackageLogo()
        fun showSuccessfullyUpdatedMessage()
        fun showEditFirewallRule(firewallRuleId: String?)
        val isActive: Boolean
    }
}