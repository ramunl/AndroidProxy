package uci.localproxy.profilescreens.profileslist

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import uci.localproxy.BaseDrawerActivity
import uci.localproxy.R
import uci.localproxy.util.ActivityUtils

/**
 * Created by daniel on 16/09/17.
 */
class ProfilesListActivity constructor() : BaseDrawerActivity() {
    //    private DrawerLayout mDrawerLayout;
    private var mProfilesPresenter: ProfilesListContract.Presenter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profiles_list_act)

        // Set up the toolbar.
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab: ActionBar? = getSupportActionBar()
        ab!!.setTitle(R.string.profiles_title)
        ab.setHomeAsUpIndicator(R.drawable.ic_menu)
        ab.setDisplayHomeAsUpEnabled(true)

        // Set up the navigation drawer.
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerLayout!!.setStatusBarBackground(R.color.colorPrimaryDark)
        val navigationView: NavigationView? = findViewById(R.id.nav_view)
        if (navigationView != null) {
            setupDrawerContent(navigationView)
            navigationView.getMenu().findItem(R.id.profile_navigation_menu_item).setChecked(true)
        }
        var profilesListFragment: ProfilesListFragment? =
            getSupportFragmentManager().findFragmentById(
                R.id.contentFrame
            ) as ProfilesListFragment?
        if (profilesListFragment == null) {
            //Create the fragment
            profilesListFragment = ProfilesListFragment.Companion.newInstance()
            ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), profilesListFragment, R.id.contentFrame
            )
        }

        //Create the presenter
        mProfilesPresenter = ProfilesListPresenter(profilesListFragment)

        //Load previously saved state, if available
        if (savedInstanceState != null) {
            //TODO
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    } //    private void setupDrawerContent(NavigationView navigationView) {
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
    //                        mDrawerLayout!!.closeDrawers();
    //                        return true;
    //                    }
    //                });
    //    }
}