package uci.localproxy.firewallscreens.firewallruleslist;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;


import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import uci.localproxy.BaseDrawerActivity;
import uci.localproxy.R;
import uci.localproxy.util.ActivityUtils;

/**
 * Created by daniel on 29/09/17.
 */

public class FirewallRulesListActivity extends BaseDrawerActivity {

//    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firewallrules_list_act);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.firewall_title);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        //Set up navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
            navigationView.getMenu().findItem(R.id.firewall_navigation_menu_item).setChecked(true);
        }

        FirewallRulesListFragment firewallRulesListFragment =
                (FirewallRulesListFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (firewallRulesListFragment == null){
            firewallRulesListFragment = FirewallRulesListFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    firewallRulesListFragment, R.id.contentFrame);
        }

        //create the presenter
        new FirewallRulesListPresenter(firewallRulesListFragment);

        //Load previously saved state, if available
        if (savedInstanceState != null) {
            //TODO
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private void setupDrawerContent(NavigationView navigationView) {
//        navigationView.setNavigationItemSelectedListener(
//                new NavigationView.OnNavigationItemSelectedListener() {
//                    @Override
//                    public boolean onNavigationItemSelected(MenuItem menuItem) {
//                        Intent intent = null;
//                        switch (menuItem.getItemId()) {
//                            case R.id.proxy_navigation_menu_item:
//                                intent = new Intent(FirewallRulesListActivity.this, ProxyActivity.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                                startActivity(intent);
//                                break;
//                            case R.id.profile_navigation_menu_item:
//                                intent = new Intent(FirewallRulesListActivity.this, ProfilesListActivity.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                                startActivity(intent);
//                                break;
//                            case R.id.firewall_navigation_menu_item:
//                                menuItem.setChecked(true);
//                                break;
//                            default:
//                                break;
//                        }
//
//                        // Close the navigation drawer when an item is selected.
//                        mDrawerLayout.closeDrawers();
//                        return true;
//                    }
//                });
//    }
}
