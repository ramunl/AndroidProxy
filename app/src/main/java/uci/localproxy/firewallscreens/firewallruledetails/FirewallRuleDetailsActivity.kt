package uci.localproxy.firewallscreens.firewallruledetails

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import uci.localproxy.R
import uci.localproxy.proxydata.applicationPackage.ApplicationPackageLocalDataSource
import uci.localproxy.util.ActivityUtils

/**
 * Created by daniel on 2/10/17.
 */
class FirewallRuleDetailsActivity constructor() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.firewallrule_details_act)

        //Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar: ActionBar? = getSupportActionBar()
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setTitle(R.string.firewallrule_details_title)
        val firewallRuleId: String? = getIntent().getStringExtra(EXTRA_FIREWALL_RULE_ID)
        var firewallRuleDetailsFragment: FirewallRuleDetailsFragment? =
            getSupportFragmentManager().findFragmentById(
                R.id.contentFrame
            ) as FirewallRuleDetailsFragment?
        if (firewallRuleDetailsFragment == null) {
            firewallRuleDetailsFragment = FirewallRuleDetailsFragment.Companion.newInstance()
            val bundle: Bundle = Bundle()
            bundle.putString(
                FirewallRuleDetailsFragment.Companion.ARGUMENT_FIREWALL_RULE_ID,
                firewallRuleId
            )
            firewallRuleDetailsFragment.setArguments(bundle)
            ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(),
                firewallRuleDetailsFragment, R.id.contentFrame
            )
        }
        FirewallRuleDetailsPresenter(
            firewallRuleDetailsFragment,
            firewallRuleId,
            ApplicationPackageLocalDataSource.getInstance(getApplicationContext())
        )
    }

    public override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        val EXTRA_FIREWALL_RULE_ID: String = "FIREWALL_RULE_ID"
    }
}