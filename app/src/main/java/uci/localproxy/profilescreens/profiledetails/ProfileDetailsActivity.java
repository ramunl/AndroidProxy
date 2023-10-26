package uci.localproxy.profilescreens.profiledetails;

import android.os.Bundle;
import androidx.annotation.Nullable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import uci.localproxy.R;
import uci.localproxy.util.ActivityUtils;

/**
 * Created by daniel on 18/09/17.
 */

public class ProfileDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PROFILE_ID = "PROFILE_ID";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.profile_details_act);

        //Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(R.string.profile_details_title);

        String profileId = getIntent().getStringExtra(EXTRA_PROFILE_ID);

        ProfileDetailsFragment profileDetailsFragment = (ProfileDetailsFragment) getSupportFragmentManager().
                findFragmentById(R.id.contentFrame);

        if (profileDetailsFragment == null){
            profileDetailsFragment = ProfileDetailsFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(ProfileDetailsFragment.ARGUMENT_PROFILE_ID, profileId);
            profileDetailsFragment.setArguments(bundle);

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    profileDetailsFragment, R.id.contentFrame);
        }

        new ProfileDetailsPresenter(profileDetailsFragment, profileId);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
