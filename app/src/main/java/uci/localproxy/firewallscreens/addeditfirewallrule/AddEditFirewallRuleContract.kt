package uci.localproxy.firewallscreens.addeditfirewallrule

import uci.localproxy.BasePresenter
import uci.localproxy.BaseView
import uci.localproxy.proxydata.applicationPackage.ApplicationPackage

/**
 * Created by daniel on 1/10/17.
 */
open interface AddEditFirewallRuleContract {
    open interface View : BaseView<Presenter?> {
        fun showEmptyFirewallRuleError()
        fun finishAddEditFirewallRuleActivity()
        fun setRule(rule: String?)
        fun setDescription(description: String?)
        fun setEmptyRuleError()
        fun setApplicationPackages(applicationPackages: List<ApplicationPackage>)
        fun setSpinnerApplicationPackageSelected(packageName: String)

        //check if the view is active
        val isActive: Boolean

    }

    open interface Presenter : BasePresenter {
        fun saveFirewallRule(rule: String, packageName: String, description: String)
        fun populateFirewallRule()
        fun onDestroy()
        val isDataMissing: Boolean
    }
}