package grgr.localproxy.firewallscreens.addeditfirewallrule

import android.content.Context
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import grgr.localproxy.proxydata.applicationPackage.ApplicationPackage
import grgr.localproxy.proxydata.applicationPackage.ApplicationPackageLocalDataSource
import grgr.localproxy.proxydata.firewallRule.FirewallRule
import grgr.localproxy.proxydata.firewallRule.FirewallRuleDataSource.GetFirewallRuleCallback
import grgr.localproxy.proxydata.firewallRule.FirewallRuleLocalDataSource

/**
 * Created by daniel on 1/10/17.
 */
class AddEditFirewallRulePresenter constructor(
    addEditFirewallRuleView: AddEditFirewallRuleContract.View,
    private val mFirewallRuleId: String?,
    context: Context,
    override var isDataMissing: Boolean
) : AddEditFirewallRuleContract.Presenter {
    private val mFirewallRuleDataSource: FirewallRuleLocalDataSource
    private val mApplicationPackageLocalDataSource: ApplicationPackageLocalDataSource
    private val mAddEditFirewallRuleView: AddEditFirewallRuleContract.View

    init {
        mFirewallRuleDataSource = FirewallRuleLocalDataSource.newInstance()
        mApplicationPackageLocalDataSource = ApplicationPackageLocalDataSource.getInstance(context)
        mAddEditFirewallRuleView = Preconditions.checkNotNull(addEditFirewallRuleView)
        mAddEditFirewallRuleView.setPresenter(this)
    }

    public override fun start() {
        loadApplicationPackages()
        if (!isNewFirewallRule && isDataMissing) populateFirewallRule()
    }

    public override fun saveFirewallRule(rule: String, packageName: String, description: String) {
        if (isNewFirewallRule) createFirewallRule(
            rule,
            packageName,
            description
        ) else updateFirewallRule(rule, packageName, description)
    }

    public override fun populateFirewallRule() {
        if (isNewFirewallRule) {
            throw RuntimeException("populateFirewallRule() was called but firewall rule is new.")
        }
        mFirewallRuleDataSource.getFirewallRule(
            (mFirewallRuleId)!!,
            object : GetFirewallRuleCallback {
                public override fun onFirewallRuleLoaded(firewallRule: FirewallRule) {
                    if (!mAddEditFirewallRuleView.isActive) return
                    mAddEditFirewallRuleView.setSpinnerApplicationPackageSelected(
                        firewallRule.getApplicationPackageName()
                    )
                    mAddEditFirewallRuleView.setRule(firewallRule.getRule())
                    mAddEditFirewallRuleView.setDescription(firewallRule.getDescription())
                    isDataMissing = false
                }

                public override fun onDataNoAvailable() {
                    if (!mAddEditFirewallRuleView.isActive) return
                    mAddEditFirewallRuleView.setEmptyRuleError()
                }
            })
    }

    public override fun onDestroy() {
        mFirewallRuleDataSource.releaseResources()
    }

    private val isNewFirewallRule: Boolean
        private get() {
            return mFirewallRuleId == null
        }

    private fun loadApplicationPackages() {
        val applicationPackageList: List<ApplicationPackage> =
            mApplicationPackageLocalDataSource.getApplicationPackages()
        if (applicationPackageList.size != 0) mAddEditFirewallRuleView.setApplicationPackages(
            applicationPackageList
        )
    }

    private fun createFirewallRule(rule: String, packageName: String, description: String) {
        val isValidData: Boolean = validateData(rule, description)
        if (!isValidData) return
        val firewallRule: FirewallRule = FirewallRule.newInstance(rule, packageName, description)
        mFirewallRuleDataSource.saveFirewallRule(firewallRule)
        if (mAddEditFirewallRuleView.isActive) mAddEditFirewallRuleView.finishAddEditFirewallRuleActivity()
    }

    private fun updateFirewallRule(rule: String, packageName: String, description: String) {
        val isValidData: Boolean = validateData(rule, description)
        if (!isValidData) return
        val firewallRule: FirewallRule =
            FirewallRule.newInstance((mFirewallRuleId)!!, rule, packageName, description)
        mFirewallRuleDataSource.updateFirewallRule(firewallRule)
        if (mAddEditFirewallRuleView.isActive) mAddEditFirewallRuleView.finishAddEditFirewallRuleActivity()
    }

    private fun validateData(rule: String, description: String): Boolean {
        var isValid: Boolean = true
        if (Strings.isNullOrEmpty(rule)) {
            mAddEditFirewallRuleView.setEmptyRuleError()
            isValid = false
        }
        return isValid
    }
}