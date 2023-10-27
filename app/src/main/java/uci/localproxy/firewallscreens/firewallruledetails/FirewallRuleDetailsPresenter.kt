package uci.localproxy.firewallscreens.firewallruledetails

import android.app.Activity
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import uci.localproxy.proxydata.applicationPackage.ApplicationPackage
import uci.localproxy.proxydata.applicationPackage.ApplicationPackageLocalDataSource
import uci.localproxy.proxydata.firewallRule.FirewallRule
import uci.localproxy.proxydata.firewallRule.FirewallRuleDataSource.GetFirewallRuleCallback
import uci.localproxy.proxydata.firewallRule.FirewallRuleLocalDataSource

/**
 * Created by daniel on 2/10/17.
 */
class FirewallRuleDetailsPresenter constructor(
    view: FirewallRuleDetailsContract.View,
    private val mFirewallRuleId: String?,
    var mApplicationPackageLocalDataSource: ApplicationPackageLocalDataSource
) : FirewallRuleDetailsContract.Presenter {
    private val mFirewallRuleDataSource: FirewallRuleLocalDataSource
    private val mView: FirewallRuleDetailsContract.View

    init {
        mView = Preconditions.checkNotNull(view)
        mFirewallRuleDataSource = FirewallRuleLocalDataSource.newInstance()
        mView.setPresenter(this)
    }

    public override fun start() {
        openFirewallRule()
    }

    private fun openFirewallRule() {
        if (Strings.isNullOrEmpty(mFirewallRuleId)) {
            mView.showMissingFirewallRule()
            return
        }
        mFirewallRuleDataSource.getFirewallRule(
            (mFirewallRuleId)!!,
            object : GetFirewallRuleCallback {
                public override fun onFirewallRuleLoaded(firewallRule: FirewallRule) {
                    if (!mView.isActive) return
                    val applicationPackage: ApplicationPackage = mApplicationPackageLocalDataSource
                        .getApplicationPackageByPackageName(firewallRule.getApplicationPackageName())
                    mView.showRule(firewallRule.getRule())
                    mView.showDescription(firewallRule.getDescription())
                    if ((applicationPackage.getName()
                                == ApplicationPackageLocalDataSource.ALL_APPLICATION_PACKAGES_STRING)
                    ) {
                        mView.showAllApplicationPackageName()
                        mView.showNoPackageLogo()
                    } else {
                        mView.showApplicationName(applicationPackage.getName())
                        mView.showPackageLogo(firewallRule.getApplicationPackageName())
                    }
                }

                public override fun onDataNoAvailable() {
                    if (!mView.isActive) return
                    mView.showMissingFirewallRule()
                }
            })
    }

    public override fun editFirewallRule() {
        if (Strings.isNullOrEmpty(mFirewallRuleId)) {
            mView.showMissingFirewallRule()
            return
        }
        mView.showEditFirewallRule(mFirewallRuleId)
    }

    public override fun deleteFirewallRule() {
        if (Strings.isNullOrEmpty(mFirewallRuleId)) {
            mView.showMissingFirewallRule()
            return
        }
        mFirewallRuleDataSource.deleteFirewallRule((mFirewallRuleId)!!)
        mView.showFirewallRuleDeleted()
    }

    public override fun result(requestCode: Int, resultCode: Int) {
        if ((requestCode == FirewallRuleDetailsFragment.Companion.REQUEST_EDIT_FIREWALL_RULE
                    && resultCode == Activity.RESULT_OK)
        ) mView.showSuccessfullyUpdatedMessage()
    }

    public override fun onDestroy() {
        mFirewallRuleDataSource.releaseResources()
    }
}