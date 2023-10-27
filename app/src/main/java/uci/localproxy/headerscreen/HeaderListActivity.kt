package uci.localproxy.headerscreen

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import uci.localproxy.BaseDrawerActivity
import uci.localproxy.R
import uci.localproxy.util.ActivityUtils

/**
 * Created by daniel on 30/08/18.
 */
class HeaderListActivity : BaseDrawerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.headers_list_act)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        ab!!.setTitle(R.string.headers_title)
        ab.setHomeAsUpIndicator(R.drawable.ic_menu)
        ab.setDisplayHomeAsUpEnabled(true)

        //Set up navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerLayout!!.setStatusBarBackground(R.color.colorPrimaryDark)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        if (navigationView != null) {
            setupDrawerContent(navigationView)
            navigationView.menu.findItem(R.id.headers_navigation_menu_item).isChecked = true
        }
        var headerListFragment =
            supportFragmentManager.findFragmentById(R.id.contentFrame) as HeaderListFragment?
        if (headerListFragment == null) {
            headerListFragment = HeaderListFragment.Companion.newInstance()
            ActivityUtils.addFragmentToActivity(
                supportFragmentManager,
                headerListFragment, R.id.contentFrame
            )
        }

        //create the presenter
        HeaderListPresenter(headerListFragment)

        //Load previously saved state, if available
        if (savedInstanceState != null) {
            //TODO
        }
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
    }
}