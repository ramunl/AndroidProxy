package uci.localproxy.firewallscreens.addeditfirewallrule

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import uci.localproxy.R
import uci.localproxy.util.ActivityUtils

/**
 * Created by daniel on 1/10/17.
 */
class AddEditFirewallRuleActivity constructor() : AppCompatActivity() {
    private var mPresenter: AddEditFirewallRuleContract.Presenter? = null
    private var mActionBar: ActionBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addfirewallrule_act)

        // Set up the toolbar.
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        mActionBar = getSupportActionBar()
        mActionBar!!.setDisplayHomeAsUpEnabled(true)
        mActionBar!!.setDisplayShowHomeEnabled(true)
        var addEditFirewallRuleFragment: AddEditFirewallRuleFragment? =
            getSupportFragmentManager().findFragmentById(
                R.id.contentFrame
            ) as AddEditFirewallRuleFragment?
        val firewallRuleId: String? =
            getIntent().getStringExtra(AddEditFirewallRuleFragment.Companion.ARGUMENT_EDIT_FIREWALL_RULE_ID)
        setToolbarTitle(firewallRuleId)
        if (addEditFirewallRuleFragment == null) {
            addEditFirewallRuleFragment = AddEditFirewallRuleFragment.Companion.newInstance()
            if (getIntent().hasExtra(AddEditFirewallRuleFragment.Companion.ARGUMENT_EDIT_FIREWALL_RULE_ID)) {
                val bundle: Bundle = Bundle()
                bundle.putString(
                    AddEditFirewallRuleFragment.Companion.ARGUMENT_EDIT_FIREWALL_RULE_ID,
                    firewallRuleId
                )
                addEditFirewallRuleFragment.setArguments(bundle)
            }
            ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(),
                addEditFirewallRuleFragment, R.id.contentFrame
            )
        }
        var shouldLoadDataFromRepo: Boolean = true

        // Prevent the presenter from loading data from the repository if this is a config change.
        if (savedInstanceState != null) {
            // Data might not have loaded when the config change happen, so we saved the state.
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY)
        }

        //Create the presenter
        mPresenter = AddEditFirewallRulePresenter(
            addEditFirewallRuleFragment,
            firewallRuleId,
            getApplicationContext(),
            shouldLoadDataFromRepo
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, mPresenter!!.isDataMissing)
        super.onSaveInstanceState(outState)
    }

    private fun setToolbarTitle(firewallRuleId: String?) {
        if (firewallRuleId == null) {
            mActionBar!!.setTitle(R.string.new_firewallRule)
        } else {
            mActionBar!!.setTitle(R.string.edit_firewallRule)
        }
    }

    public override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        val REQUEST_ADD_FIREWALL_RULE: Int = 1
        val SHOULD_LOAD_DATA_FROM_REPO_KEY: String = "SHOULD_LOAD_DATA_FROM_REPO_KEY"
    }
}