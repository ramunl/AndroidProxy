package uci.localproxy.headerscreen;

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
 * Created by daniel on 30/08/18.
 */

public class HeaderListActivity extends BaseDrawerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.headers_list_act);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.headers_title);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        //Set up navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
            navigationView.getMenu().findItem(R.id.headers_navigation_menu_item).setChecked(true);
        }

        HeaderListFragment headerListFragment =
                (HeaderListFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (headerListFragment == null){
            headerListFragment = HeaderListFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    headerListFragment, R.id.contentFrame);
        }

        //create the presenter
        new HeaderListPresenter(headerListFragment);

        //Load previously saved state, if available
        if (savedInstanceState != null) {
            //TODO
        }
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

}
