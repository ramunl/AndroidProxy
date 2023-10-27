package uci.localproxy.profilescreens.profiledetails

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import uci.localproxy.R
import uci.localproxy.profilescreens.addeditprofile.AddEditProfileActivity
import uci.localproxy.profilescreens.addeditprofile.AddEditProfileFragment

/**
 * Created by daniel on 18/09/17.
 */
class ProfileDetailsFragment  //    private TextView mDomain;
constructor() : Fragment(), ProfileDetailsContract.View {
    private var mPresenter: ProfileDetailsContract.Presenter? = null
    private var mDetailName: TextView? = null
    private var mDetailServer: TextView? = null
    private var mDetailInPort: TextView? = null
    private var mDetailBypass: TextView? = null
    public override fun onResume() {
        super.onResume()
        mPresenter!!.start()
    }

    public override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View = inflater.inflate(R.layout.profile_details_frag, container, false)
        mDetailName = root.findViewById(R.id.nameTv)
        mDetailServer = root.findViewById(R.id.serverTv)
        mDetailInPort = root.findViewById(R.id.inPortTv)
        mDetailBypass = root.findViewById(R.id.bypassTv)
        //        mDomain = (TextView) root.findViewById(R.id.domainTv);
        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab_edit_profile)
        fab.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                mPresenter!!.editProfile()
            }
        })
        setHasOptionsMenu(true)
        return root
    }

    public override fun onDestroy() {
        mPresenter!!.onDestroy()
        super.onDestroy()
    }

    public override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profiledetail_fragment_menu, menu)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.menu_delete -> mPresenter!!.deleteProfile()
        }
        return false
    }

    public override fun showMissingProfile() {
        Snackbar.make((mDetailName)!!, R.string.missing_data_message, Snackbar.LENGTH_LONG).show()
    }

    public override fun showName(name: String?) {
        mDetailName!!.setText(name)
    }

    public override fun showServer(server: String?) {
        mDetailServer!!.setText(server)
    }

    public override fun showInPort(inPort: Int) {
        mDetailInPort!!.setText(inPort.toString())
    }

    public override fun showBypass(bypass: String?) {
        mDetailBypass!!.setText(bypass)
    }

    //    @Override
    //    public void showDomain(String domain) {
    //        mDomain.setText(domain);
    //    }
    public override fun showEditProfile(profileId: String?) {
        val i: Intent = Intent(getActivity(), AddEditProfileActivity::class.java)
        i.putExtra(AddEditProfileFragment.Companion.ARGUMENT_EDIT_PROFILE_ID, profileId)
        startActivityForResult(i, REQUEST_EDIT_PROFILE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mPresenter!!.result(requestCode, resultCode)
    }

    public override fun showProfileDeleted() {
        requireActivity().finish()
    }

    public override fun showSuccessfullyUpdatedMessage() {
        Snackbar.make(
            (mDetailName)!!,
            getResources().getString(R.string.successfully_profile_updated_message),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override val isActive: Boolean
        get() {
            return isAdded()
        }

    public override fun setPresenter(presenter: ProfileDetailsContract.Presenter?) {
        mPresenter = presenter
    }

    companion object {
        val ARGUMENT_PROFILE_ID: String = "PROFILE_ID"
        val REQUEST_EDIT_PROFILE: Int = 1
        fun newInstance(): ProfileDetailsFragment {
            return ProfileDetailsFragment()
        }
    }
}