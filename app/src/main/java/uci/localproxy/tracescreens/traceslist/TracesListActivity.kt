package uci.localproxy.tracescreens.traceslist

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import uci.localproxy.BaseDrawerActivity
import uci.localproxy.R
import uci.localproxy.util.ActivityUtils

/**
 * Created by daniel on 16/02/18.
 */
class TracesListActivity : BaseDrawerActivity() {
    //    private DrawerLayout mDrawer;
    private var mPresenter: TracesListContract.Presenter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.traces_list_act)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setTitle(getString(R.string.traces_title))
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu)
        actionBar.setDisplayHomeAsUpEnabled(true)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerLayout!!.setStatusBarBackground(R.color.colorPrimaryDark)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        if (navigationView != null) {
            setupDrawerContent(navigationView)
            navigationView.menu.findItem(R.id.traces_navigation_menu_item).isChecked = true
        }
        var tracesListFragment =
            supportFragmentManager.findFragmentById(R.id.contentFrame) as TracesListFragment?
        if (tracesListFragment == null) {
            tracesListFragment = TracesListFragment.Companion.newInstance()
            ActivityUtils.addFragmentToActivity(
                supportFragmentManager,
                tracesListFragment,
                R.id.contentFrame
            )
        }

        //Create the presenter
        mPresenter = TracesListPresenter(tracesListFragment)

        //Load previously saved state, if available
        if (savedInstanceState != null) {
            //TODO
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
    //                                intent = new Intent(TracesListActivity.this, ProxyActivity.class);
    //                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    //                                startActivity(intent);
    //                                break;
    //                            case R.id.profile_navigation_menu_item:
    //                                intent = new Intent(TracesListActivity.this, ProfilesListActivity.class);
    //                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    //                                startActivity(intent);
    //                                break;
    //                            case R.id.firewall_navigation_menu_item:
    //                                intent = new Intent(TracesListActivity.this, FirewallRulesListActivity.class);
    //                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    //                                startActivity(intent);
    //                                break;
    //                            case R.id.traces_navigation_menu_item:
    //                                menuItem.setChecked(true);
    //                                break;
    //                            default:
    //                                break;
    //                        }
    //                        // Close the navigation drawer when an item is selected.
    //                        mDrawer.closeDrawers();
    //                        return true;
    //                    }
    //                });
    //    }
}