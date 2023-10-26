package uci.localproxy.profilescreens.profileslist;

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
 * Created by daniel on 16/09/17.
 */

public class ProfilesListActivity extends BaseDrawerActivity {

//    private DrawerLayout mDrawerLayout;

    private ProfilesListContract.Presenter mProfilesPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profiles_list_act);

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.profiles_title);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        // Set up the navigation drawer.
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
            navigationView.getMenu().findItem(R.id.profile_navigation_menu_item).setChecked(true);
        }

        ProfilesListFragment profilesListFragment =
                (ProfilesListFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (profilesListFragment == null){
            //Create the fragment
            profilesListFragment = ProfilesListFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), profilesListFragment, R.id.contentFrame);
        }

        //Create the presenter
        mProfilesPresenter = new ProfilesListPresenter(profilesListFragment);

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
//                                intent = new Intent(ProfilesListActivity.this, ProxyActivity.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                                startActivity(intent);
//                                break;
//                            case R.id.profile_navigation_menu_item:
//                                menuItem.setChecked(true);
//                                break;
//                            case R.id.firewall_navigation_menu_item:
//                                intent = new Intent(ProfilesListActivity.this, FirewallRulesListActivity.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                                startActivity(intent);
//                                break;
//                            default:
//                                break;
//                        }
//                        // Close the navigation drawer when an item is selected.
//                        mDrawerLayout.closeDrawers();
//                        return true;
//                    }
//                });
//    }

}
