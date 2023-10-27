package uci.localproxy.proxyscreen

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView

import uci.localproxy.BaseDrawerActivity
import uci.localproxy.R
import uci.localproxy.proxycore.ProxyService
import uci.localproxy.proxydata.pref.AppPreferencesHelper
import uci.localproxy.util.ActivityUtils

class ProxyActivity : BaseDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.Secure.putString(getContentResolver(), Settings.Secure.HTTP_PROXY, "127.0.0.1:8007");
        setContentView(R.layout.proxy_act)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.logo = applicationContext.resources.getDrawable(R.mipmap.ic_launcher5)
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        ab!!.setHomeAsUpIndicator(R.drawable.ic_menu)
        ab.setDisplayHomeAsUpEnabled(true)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        // mDrawerLayout!!.setStatusBarBackground(R.color.colorPrimaryDark);
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        if (navigationView != null) {
            setupDrawerContent(navigationView)
            navigationView.menu.findItem(R.id.proxy_navigation_menu_item).isChecked = true
        }
        var proxyFragment = supportFragmentManager
            .findFragmentById(R.id.contentFrame) as ProxyFragment?
        if (proxyFragment == null) {
            //create the fragment
            proxyFragment = ProxyFragment.Companion.newInstance()
            ActivityUtils.addFragmentToActivity(
                supportFragmentManager, proxyFragment, R.id.contentFrame
            )
        }

        //create the presenter
        ProxyPresenter(proxyFragment, AppPreferencesHelper.getInstance(applicationContext))

        //Load previously saved state, if available
        if (savedInstanceState != null) {
            //TODO
        }
    }

    override fun onResume() {
        super.onResume()
        //used to configure the form when it is restarted
        //if closed by the system
//        if (isProxyServiceRunning(this)) {
//            disableAll();
//        } else {
//            enableAll();
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //    @Override
    //    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    //        switch (item.getItemId()) {
    //            case R.id.proxy_navigation_menu_item:
    //                item.setChecked(true);
    //                break;
    //            case R.id.profile_navigation_menu_item:
    //                Intent intent = new Intent(ProxyActivity.this, ProfilesListActivity.class);
    //                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
    //                startActivity(intent);
    //                break;
    //            case R.id.firewall_navigation_menu_item:
    //                intent = new Intent(ProxyActivity.this, FirewallRulesListActivity.class);
    //                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    //                startActivity(intent);
    //                break;
    //            default:
    //                break;
    //        }
    //
    //        // Close the navigation drawer when an item is selected.
    //        mDrawerLayout!!.closeDrawers();
    //        return true;
    //    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //no menu needed at this time
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        when (id) {
            R.id.action_about_us -> //                AlertDialog alertDialog = new AlertDialog.Builder(ProxyActivity.this).create();
//                if (themeId == ProxyActivity.DARK_THEME) {
//                    alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(ProxyActivity.this, R.style.AlertDialogCustom)).create();
//                }
//                alertDialog.setTitle(getResources().getString(R.string.createdBy));
//                alertDialog.setMessage("Daniel A. Rodriguez Caballero: \n" +
//                        "darodriguez@estudiantes.uci.cu,\n" + "danielrodcaball@gmail.com");
//                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//
//                alertDialog.show();
                return true

            android.R.id.home -> {
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //    private void chargeTheme() {
    //        SharedPreferences settings = getSharedPreferences("UCIntlm.conf",
    //                Context.MODE_PRIVATE);
    //        themeId = settings.getInt("theme", LIGHT_THEME);
    //        if (themeId == LIGHT_THEME) {
    //            setTheme(R.style.AppTheme_NoActionBar);
    //        } else if (themeId == DARK_THEME) {
    //            setTheme(R.style.DarkTheme_NoActionBar);
    //        }
    //    }
    //
    private fun fetchPrimaryColor(): Int {
        val typedValue = TypedValue()
        val a = obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimary))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    //
    //    private int fetchAccentColor() {
    //        TypedValue typedValue = new TypedValue();
    //        TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
    //        int color = a.getColor(0, 0);
    //        a.recycle();
    //        return color;
    //    }
    private fun initUi() {}

    //    private void loadConf() {
    //        SharedPreferences settings = getSharedPreferences("WifiProxy.conf",
    //                Context.MODE_PRIVATE);
    //        userInfoTab.username.setText(settings.getString("username", ""));
    //        userInfoTab.pass.setText(Encripter.decrypt(settings.getString("password", "")));
    //
    //        preferencesTab.domain.setText(settings.getString("domain", ""));
    //        preferencesTab.server.setText(settings.getString("server", ""));
    //        preferencesTab.inputport.setText(settings.getString("inputport", ""));
    //        preferencesTab.outputport.setText(settings.getString("outputport", ""));
    //        preferencesTab.bypass.setText(settings.getString("bypass", ""));
    //
    //        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
    //            (preferencesTab.globalCheckBox).setActive(settings.getBoolean("global_proxy", true));
    //        }
    //
    //        preferencesTab.spinnerTheme.setSelection(themeId);
    //
    //        if (userInfoTab.username.getText().toString().equals("")) {
    //            userInfoTab.username.requestFocus();
    //        } else {
    //            userInfoTab.pass.requestFocus();
    //        }
    //
    //        preferencesTab.authSchemeSpinner.setSelection(settings.getInt("authSchemeSelectedPos", 0));
    //    }
    //
    //    private void saveConf() {
    //        SharedPreferences settings = getSharedPreferences("WifiProxy.conf",
    //                Context.MODE_PRIVATE);
    //        Editor editor = settings.edit();
    //        editor.putString("username", userInfoTab.username.getText().toString());
    //        editor.putString("password",
    //                Encripter.encrypt(userInfoTab.pass.getText().toString()));
    //
    //        editor.putString("domain", preferencesTab.domain.getText().toString());
    //        editor.putString("server", preferencesTab.server.getText().toString());
    //        editor.putString("inputport", preferencesTab.inputport.getText().toString());
    //        editor.putString("outputport", preferencesTab.outputport.getText().toString());
    //        editor.putString("bypass", preferencesTab.bypass.getText().toString());
    //
    //        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
    //            editor.putBoolean("global_proxy", (preferencesTab.globalCheckBox).isActive);
    //        }
    //
    //        editor.putInt("authSchemeSelectedPos", preferencesTab.authSchemeSpinner.getSelectedItemPosition());
    //        editor.apply();
    //    }
    fun clickRun(arg0: View?) {
//        viewPager.setCurrentItem(0, true);
//
//        if ((preferencesTab.authSchemeSpinner.getSelectedItemPosition() == 1 && preferencesTab.domain.getText().toString().equals(""))
//                || preferencesTab.server.getText().toString().equals("")
//                || preferencesTab.inputport.getText().toString().equals("")
//                || preferencesTab.outputport.getText().toString().equals("")
//                || userInfoTab.username.getText().toString().equals("")
//                || userInfoTab.pass.getText().toString().equals("")) {
//
//            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.nodata),
//                    Toast.LENGTH_SHORT).show();
//            fab.setImageDrawable(new DrawableAwesome(R.string.fa_play, 35, Color.WHITE, false, false, 0, 0, 0, 0, this));
//            return;
//        }
//
//        if (!isProxyServiceRunning(this)) {
//            startProxy();
//        } else {
//            Intent proxyIntent = new Intent(this, ProxyService.class);
//            stopService(proxyIntent);
//
//            enableAll();
//
////            UCIntlmWidget.actualizarWidget(this.getApplicationContext(),
////                    AppWidgetManager.getInstance(this.getApplicationContext()),
////                    "off");
//        }
    }

    fun startProxy() {
//        Intent proxyIntent = new Intent(this, ProxyService.class);
//        saveConf();
//        proxyIntent.putExtra("user", userInfoTab.username.getText().toString());
//        proxyIntent.putExtra("pass", userInfoTab.pass.getText().toString());
//        proxyIntent.putExtra("domain", preferencesTab.domain.getText().toString());
//        proxyIntent.putExtra("server", preferencesTab.server.getText().toString());
//        proxyIntent.putExtra("inputport", preferencesTab.inputport.getText().toString());
//        proxyIntent.putExtra("outputport", preferencesTab.outputport.getText().toString());
//        proxyIntent.putExtra("bypass", preferencesTab.bypass.getText().toString());
//        switch (preferencesTab.authSchemeSpinner.getSelectedItemPosition()){
//            case 0:
//                proxyIntent.putExtra("authScheme", HttpForwarder.BASIC_SCHEME);
//                break;
//            case 1:
//                proxyIntent.putExtra("authScheme", HttpForwarder.NTLM_SCHEME);
//                break;
//            case 2:
//                proxyIntent.putExtra("authScheme", HttpForwarder.DIGEST_SCHEME);
//                break;
//        }
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            proxyIntent.putExtra("set_global_proxy", preferencesTab.globalCheckBox.isActive);
//        } else {
//            proxyIntent.putExtra("set_global_proxy", false);
//        }
//
//        startService(proxyIntent);
//        disableAll();
    }

    companion object {
        var LIGHT_THEME = 0
        var DARK_THEME = 1
        private fun isProxyServiceRunning(context: Context): Boolean {
            val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager
                .getRunningServices(Int.MAX_VALUE)) {
                if (ProxyService::class.java.name ==
                    service.service.className
                ) {
                    Log.i(ProxyActivity::class.java.name, "Service running")
                    return true
                }
            }
            Log.i(ProxyActivity::class.java.name, "Service not running")
            return false
        }
    }
}