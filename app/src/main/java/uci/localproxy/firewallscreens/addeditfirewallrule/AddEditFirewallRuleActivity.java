package uci.localproxy.firewallscreens.addeditfirewallrule;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import uci.localproxy.R;
import uci.localproxy.util.ActivityUtils;

/**
 * Created by daniel on 1/10/17.
 */

public class AddEditFirewallRuleActivity extends AppCompatActivity {

    public static final int REQUEST_ADD_FIREWALL_RULE = 1;

    public static final String SHOULD_LOAD_DATA_FROM_REPO_KEY = "SHOULD_LOAD_DATA_FROM_REPO_KEY";

    private AddEditFirewallRuleContract.Presenter mPresenter;

    private ActionBar mActionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addfirewallrule_act);

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

        AddEditFirewallRuleFragment addEditFirewallRuleFragment = (AddEditFirewallRuleFragment)
                getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        String firewallRuleId = getIntent().getStringExtra(AddEditFirewallRuleFragment.ARGUMENT_EDIT_FIREWALL_RULE_ID);

        setToolbarTitle(firewallRuleId);

        if (addEditFirewallRuleFragment == null){
            addEditFirewallRuleFragment = AddEditFirewallRuleFragment.newInstance();

            if (getIntent().hasExtra(AddEditFirewallRuleFragment.ARGUMENT_EDIT_FIREWALL_RULE_ID)){
                Bundle bundle = new Bundle();
                bundle.putString(AddEditFirewallRuleFragment.ARGUMENT_EDIT_FIREWALL_RULE_ID, firewallRuleId);
                addEditFirewallRuleFragment.setArguments(bundle);
            }

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    addEditFirewallRuleFragment, R.id.contentFrame);
        }

        boolean shouldLoadDataFromRepo = true;

        // Prevent the presenter from loading data from the repository if this is a config change.
        if (savedInstanceState != null) {
            // Data might not have loaded when the config change happen, so we saved the state.
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY);
        }

        //Create the presenter
        mPresenter = new AddEditFirewallRulePresenter(
                addEditFirewallRuleFragment,
                firewallRuleId,
                getApplicationContext(),
                shouldLoadDataFromRepo
        );

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, mPresenter.isDataMissing());
        super.onSaveInstanceState(outState);
    }

    private void setToolbarTitle(@Nullable String firewallRuleId) {
        if(firewallRuleId == null) {
            mActionBar.setTitle(R.string.new_firewallRule);
        } else {
            mActionBar.setTitle(R.string.edit_firewallRule);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

