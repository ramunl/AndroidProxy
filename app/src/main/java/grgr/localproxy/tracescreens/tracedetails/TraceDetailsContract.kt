package grgr.localproxy.tracescreens.tracedetails

import grgr.localproxy.BasePresenter
import grgr.localproxy.BaseView

/**
 * Created by daniel on 17/02/18.
 */
open interface TraceDetailsContract {
    open interface Presenter : BasePresenter {
        fun addAsFirewallRule(rule: String?, appPackageName: String?)
        fun result(requestCode: Int, resultCode: Int)
        fun onDestroy()
    }

    open interface View : BaseView<Presenter?> {
        fun showPackageLogo(packageName: String?)
        fun showApplicationName(applicationName: String?)
        fun showRequestedUrl(requestedUrl: String?)
        fun showConsumption(consumption: String?)
        fun showSuccessfullyAddedAsFirewallRuleMessage()
        val isActive: Boolean
    }
}