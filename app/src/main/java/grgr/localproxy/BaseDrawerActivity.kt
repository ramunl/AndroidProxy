package grgr.localproxy

import android.content.Intent
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import grgr.localproxy.aboutscreen.AboutActivity
import grgr.localproxy.firewallscreens.firewallruleslist.FirewallRulesListActivity
import grgr.localproxy.headerscreen.HeaderListActivity
import grgr.localproxy.profilescreens.profileslist.ProfilesListActivity
import grgr.localproxy.proxyscreen.ProxyActivity
import grgr.localproxy.tracescreens.traceslist.TracesListActivity

/**
 * Created by daniel on 17/02/18.
 */
abstract class BaseDrawerActivity constructor() : AppCompatActivity() {
    protected var mDrawerLayout: DrawerLayout? = null
    protected fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener(
            object : NavigationView.OnNavigationItemSelectedListener {
                public override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
                    var intent: Intent? = null
                    when (menuItem.getItemId()) {
                        R.id.proxy_navigation_menu_item -> if (this@BaseDrawerActivity is ProxyActivity) {
                            menuItem.setChecked(true)
                        } else {
                            intent = Intent(this@BaseDrawerActivity, ProxyActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                        }

                        R.id.profile_navigation_menu_item -> if (this@BaseDrawerActivity is ProfilesListActivity) {
                            menuItem.setChecked(true)
                        } else {
                            intent =
                                Intent(this@BaseDrawerActivity, ProfilesListActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                        }

                        R.id.firewall_navigation_menu_item -> if (this@BaseDrawerActivity is FirewallRulesListActivity) {
                            menuItem.setChecked(true)
                        } else {
                            intent = Intent(
                                this@BaseDrawerActivity,
                                FirewallRulesListActivity::class.java
                            )
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                        }

                        R.id.headers_navigation_menu_item -> if (this@BaseDrawerActivity is HeaderListActivity) {
                            menuItem.setChecked(true)
                        } else {
                            intent = Intent(this@BaseDrawerActivity, HeaderListActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                        }

                        R.id.traces_navigation_menu_item -> if (this@BaseDrawerActivity is TracesListActivity) {
                            menuItem.setChecked(true)
                        } else {
                            intent = Intent(this@BaseDrawerActivity, TracesListActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                        }

                        R.id.about_menu_item -> {
                            intent = Intent(this@BaseDrawerActivity, AboutActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                        }

                        else -> {}
                    }
                    // Close the navigation drawer when an item is selected.
                    mDrawerLayout!!.closeDrawers()
                    return true
                }
            })
    }
}