package uci.localproxy.profilescreens.addeditprofile

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import uci.localproxy.R
import uci.localproxy.util.ActivityUtils

/**
 * Created by daniel on 18/09/17.
 */
class AddEditProfileActivity constructor() : AppCompatActivity() {
    private var mPresenter: AddEditProfileContract.Presenter? = null
    private var mActionBar: ActionBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addprofile_act)

        // Set up the toolbar.
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        mActionBar = getSupportActionBar()
        mActionBar!!.setDisplayHomeAsUpEnabled(true)
        mActionBar!!.setDisplayShowHomeEnabled(true)
        var addEditProfileFragment: AddEditProfileFragment? = getSupportFragmentManager()
            .findFragmentById(R.id.contentFrame) as AddEditProfileFragment?
        val profileId: String? =
            getIntent().getStringExtra(AddEditProfileFragment.Companion.ARGUMENT_EDIT_PROFILE_ID)
        setToolbarTitle(profileId)
        if (addEditProfileFragment == null) {
            addEditProfileFragment = AddEditProfileFragment.Companion.newInstance()
            if (getIntent().hasExtra(AddEditProfileFragment.Companion.ARGUMENT_EDIT_PROFILE_ID)) {
                val bundle: Bundle = Bundle()
                bundle.putString(
                    AddEditProfileFragment.Companion.ARGUMENT_EDIT_PROFILE_ID,
                    profileId
                )
                addEditProfileFragment.setArguments(bundle)
            }
            ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(),
                addEditProfileFragment, R.id.contentFrame
            )
        }
        var shouldLoadDataFromRepo: Boolean = true

        // Prevent the presenter from loading data from the repository if this is a config change.
        if (savedInstanceState != null) {
            // Data might not have loaded when the config change happen, so we saved the state.
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY)
        }

        // Create the presenter
        mPresenter = AddEditProfilePresenter(
            addEditProfileFragment,
            profileId,
            shouldLoadDataFromRepo
        )
    }

    private fun setToolbarTitle(profileId: String?) {
        if (profileId == null) {
            mActionBar!!.setTitle(R.string.add_profile)
        } else {
            mActionBar!!.setTitle(R.string.edit_profile)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, mPresenter!!.isDataMissing)
        super.onSaveInstanceState(outState)
    }

    public override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        val REQUEST_ADD_PROFILE: Int = 1
        val SHOULD_LOAD_DATA_FROM_REPO_KEY: String = "SHOULD_LOAD_DATA_FROM_REPO_KEY"
    }
}