package uci.localproxy.profilescreens.addeditprofile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import uci.localproxy.R
import java.util.Locale

/**
 * Created by daniel on 18/09/17.
 */
class AddEditProfileFragment  //    private TextView mDomain;
constructor() : Fragment(), AddEditProfileContract.View {
    private var mPresenter: AddEditProfileContract.Presenter? = null
    private var mName: TextView? = null
    private var mServer: TextView? = null
    private var mInPort: TextView? = null
    private var mBypass: TextView? = null
    public override fun onResume() {
        super.onResume()
        mPresenter!!.start()
    }

    public override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab_edit_profile_done)
        fab.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                mPresenter!!.saveProfile(
                    mName!!.getText().toString(),
                    mServer!!.getText().toString(),
                    mInPort!!.getText().toString(),
                    mBypass!!.getText().toString()
                )
            }
        })
    }

    public override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root: View = inflater.inflate(R.layout.addprofile_frag, container, false)
        mName = root.findViewById(R.id.ename)
        mServer = root.findViewById(R.id.eserver)
        mInPort = root.findViewById(R.id.einputport)
        mBypass = root.findViewById(R.id.ebypass)
        mBypass!!.setText(getString(R.string.bypassInitText))
        //        mDomain = (TextView) root.findViewById(R.id.domain);
        setHasOptionsMenu(true)
        return root
    }

    public override fun onDestroy() {
        mPresenter!!.onDestroy()
        super.onDestroy()
    }

    public override fun setPresenter(presenter: AddEditProfileContract.Presenter?) {
        mPresenter = presenter
    }

    public override fun showEmptyProfileError() {
        Snackbar.make((mName)!!, getString(R.string.empty_profile_message), Snackbar.LENGTH_LONG)
            .show()
    }

    public override fun finishAddEditProfileActivity() {
        requireActivity().setResult(Activity.RESULT_OK)
        requireActivity().finish()
    }

    public override fun setName(name: String?) {
        mName!!.setText(name)
    }

    public override fun setServer(server: String?) {
        mServer!!.setText(server)
    }

    public override fun setInPort(inPort: String?) {
        mInPort!!.setText(inPort)
    }

    public override fun setBypass(bypass: String?) {
        mBypass!!.setText(bypass)
    }

    override val isActive: Boolean
        //    @Override
        get() {
            return isAdded()
        }

    public override fun setProfileEqualNameError() {
        mName!!.setError(getString(R.string.profile_equal_name_error))
    }

    public override fun setNameEmptyError() {
        mName!!.setError(getString(R.string.profile_name_empty_error))
    }

    public override fun setServerEmptyError() {
        mServer!!.setError(getString(R.string.profile_server_empty_error))
    }

    public override fun setServerInvalidError() {
        mServer!!.setError(getString(R.string.profile_server_invalid_error))
    }

    public override fun setInPortEmptyError() {
        mInPort!!.setError(getString(R.string.profile_port_empty_error))
    }

    public override fun setInputPortOutOfRangeError() {
        mInPort!!.setError(
            String.format(
                Locale.ENGLISH, getString(R.string.profile_port_outofrange_error),
                0, AddEditProfilePresenter.Companion.MAX_PORTS_LIMIT
            )
        )
    }

    public override fun setBypassSyntaxError() {
        mBypass!!.setError(getString(R.string.profile_bypass_syntax_error))
    } //    @Override

    //    public void setDomainInvalidError() {
    //        mDomain.setError(getString(R.string.profile_domain_invalid_error));
    //    }
    companion object {
        val ARGUMENT_EDIT_PROFILE_ID: String = "EDIT_PROFILE_ID"
        fun newInstance(): AddEditProfileFragment {
            return AddEditProfileFragment()
        }
    }
}