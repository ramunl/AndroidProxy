package grgr.localproxy.profilescreens.profiledetails

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import grgr.localproxy.R
import grgr.localproxy.util.ActivityUtils

/**
 * Created by daniel on 18/09/17.
 */
class ProfileDetailsActivity constructor() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_details_act)

        //Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar: ActionBar? = getSupportActionBar()
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setTitle(R.string.profile_details_title)
        val profileId: String? = getIntent().getStringExtra(EXTRA_PROFILE_ID)
        var profileDetailsFragment: ProfileDetailsFragment? =
            getSupportFragmentManager().findFragmentById(
                R.id.contentFrame
            ) as ProfileDetailsFragment?
        if (profileDetailsFragment == null) {
            profileDetailsFragment = ProfileDetailsFragment.Companion.newInstance()
            val bundle: Bundle = Bundle()
            bundle.putString(ProfileDetailsFragment.Companion.ARGUMENT_PROFILE_ID, profileId)
            profileDetailsFragment.setArguments(bundle)
            ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(),
                profileDetailsFragment, R.id.contentFrame
            )
        }
        ProfileDetailsPresenter(profileDetailsFragment, profileId)
    }

    public override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        val EXTRA_PROFILE_ID: String = "PROFILE_ID"
    }
}