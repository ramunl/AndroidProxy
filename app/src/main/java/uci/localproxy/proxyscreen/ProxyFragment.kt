package uci.localproxy.proxyscreen

import android.R
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Filter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import uci.localproxy.LocalProxyApplication
import uci.localproxy.profilescreens.addeditprofile.AddEditProfileActivity
import uci.localproxy.profilescreens.addeditprofile.AddEditProfilePresenter
import uci.localproxy.proxycore.ProxyService
import uci.localproxy.proxydata.profile.Profile
import uci.localproxy.proxydata.user.User
import java.util.Locale

/**
 * Created by daniel on 15/09/17.
 */
class ProxyFragment : Fragment(), ProxyContract.View {
    private var mPresenter: ProxyContract.Presenter? = null
    private var mUsername: AutoCompleteTextView? = null
    private var mPassword: TextView? = null
    private var mRememberPasswordCheck: CheckBox? = null
    private var mProfileSpinner: Spinner? = null
    private var mAddProfileButton: Button? = null
    private var mGlobalProxyCheck: CheckBox? = null
    private var mLocalPortEditText: EditText? = null
    private var mFabStartProxy: FloatingActionButton? = null
    private var mFabStopProxy: FloatingActionButton? = null
    private var mProfileArrayAdapter: ArrayAdapter<Profile>? = null
    private var mUserArrayAdapter: UsersArrayAdapter? = null
    private var mProgressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mProfileArrayAdapter = ArrayAdapter(
            context!!, R.layout.simple_spinner_item
        )
        mProfileArrayAdapter!!.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mUserArrayAdapter = UsersArrayAdapter(context!!, ArrayList(0))
    }

    override fun onResume() {
        super.onResume()
        mPresenter!!.start()
        //        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
//                serviceReceiver, new IntentFilter(ProxyService.SERVICE_RECIVER_NAME));
//        mUsername.requestFocus();
    }

    override fun onPause() {
//        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(
//                serviceReceiver);
        super.onPause()
    }

    override fun onDestroy() {
        mPresenter!!.onDestroy()
        super.onDestroy()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mFabStartProxy = activity!!.findViewById(uci.localproxy.R.id.fab_start_proxy)
        mFabStartProxy!!.setOnClickListener(View.OnClickListener {
            val profileId =
                if (mProfileSpinner!!.selectedItem == null) "" else (mProfileSpinner!!.selectedItem as Profile).id
            mPresenter!!.startProxyFromFabButton(
                mUsername!!.text.toString(),
                mPassword!!.text.toString(),
                profileId,
                mLocalPortEditText!!.text.toString(),
                mRememberPasswordCheck!!.isChecked,
                mGlobalProxyCheck!!.isChecked
            )
        })
        mFabStopProxy = activity!!.findViewById(uci.localproxy.R.id.fab_stop_proxy)
        mFabStopProxy!!.setOnClickListener(View.OnClickListener { mPresenter!!.stopProxy() })
        mProgressDialog = ProgressDialog(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(uci.localproxy.R.layout.proxy_frag, container, false)
        mUsername = root.findViewById(uci.localproxy.R.id.euser)
        mUsername!!.setAdapter(mUserArrayAdapter)
        mPassword = root.findViewById(uci.localproxy.R.id.epass)
        mRememberPasswordCheck = root.findViewById(uci.localproxy.R.id.check_rem_pass)
        mLocalPortEditText = root.findViewById(uci.localproxy.R.id.local_port)
        mGlobalProxyCheck = root.findViewById(uci.localproxy.R.id.globCheckBox)
        if (Build.VERSION.SDK_INT > LocalProxyApplication.MAX_SDK_SUPPORTED_FOR_WIFI_CONF) {
            mGlobalProxyCheck!!.setVisibility(View.GONE)
        }
        mProfileSpinner = root.findViewById(uci.localproxy.R.id.spinner_profiles)
        mProfileSpinner!!.setAdapter(mProfileArrayAdapter)
        mAddProfileButton = root.findViewById(uci.localproxy.R.id.add_profile_button)
        mAddProfileButton!!.setOnClickListener(View.OnClickListener { mPresenter!!.addNewProfile() })

//        ButtonAwesome buttonViewPass = (ButtonAwesome) root.findViewById(R.id.buttonViewPass);
//        buttonViewPass.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//                }
//                return false;
//            }
//        });
        setHasOptionsMenu(true)
        return root
    }

    override fun setPresenter(presenter: ProxyContract.Presenter?) {
        mPresenter = Preconditions.checkNotNull(presenter)
    }

    override fun enableAllViews() {
        mUsername!!.isEnabled = true
        mPassword!!.isEnabled = true
        mRememberPasswordCheck!!.isEnabled = true
        mProfileSpinner!!.isEnabled = true
        mLocalPortEditText!!.isEnabled = true
        if (mGlobalProxyCheck != null) {
            mGlobalProxyCheck!!.isEnabled = true
        }
    }

    override fun disableAllViews() {
        mUsername!!.isEnabled = false
        mPassword!!.isEnabled = false
        mRememberPasswordCheck!!.isEnabled = false
        mProfileSpinner!!.isEnabled = false
        mLocalPortEditText!!.isEnabled = false
        if (mGlobalProxyCheck != null) {
            mGlobalProxyCheck!!.isEnabled = false
        }
    }

    override fun setPlayView() {
        mFabStartProxy!!.visibility = View.VISIBLE
        mFabStopProxy!!.visibility = View.GONE
    }

    override fun setStopView() {
        mFabStopProxy!!.visibility = View.VISIBLE
        mFabStartProxy!!.visibility = View.GONE
    }

    override fun setUsername(username: String?) {
        mUsername!!.setText(username)
    }

    override fun setPassword(password: String?) {
        mPassword!!.text = password
    }

    override fun setRememberPassword(remember: Boolean) {
        mRememberPasswordCheck!!.isChecked = remember
    }

    override fun setLocalPort(localPort: String?) {
        mLocalPortEditText!!.setText(localPort)
    }

    override fun setGlobalProxyChecked(checked: Boolean) {
        if (mGlobalProxyCheck != null) {
            mGlobalProxyCheck!!.isChecked = checked
        }
    }

    override fun setSpinnerProfiles(profiles: List<Profile>?) {
        mProfileSpinner!!.visibility = View.VISIBLE
        mAddProfileButton!!.visibility = View.GONE
        mProfileArrayAdapter!!.clear()
        mProfileArrayAdapter!!.addAll(profiles!!)
        mProfileArrayAdapter!!.notifyDataSetChanged()
    }

    override fun setSpinnerProfileSelected(profileId: String) {
        if (mProfileSpinner!!.visibility == View.VISIBLE) {
            var pos = -1
            for (i in 0 until mProfileArrayAdapter!!.count) {
                if (mProfileArrayAdapter!!.getItem(i)!!.id == profileId) {
                    pos = i
                    break
                }
            }
            if (pos >= 0) {
                mProfileSpinner!!.setSelection(pos, false)
            }
        }
    }

    override fun showNoProfilesView() {
        mProfileArrayAdapter!!.clear()
        mProfileArrayAdapter!!.notifyDataSetChanged()
        mProfileSpinner!!.visibility = View.GONE
        mAddProfileButton!!.visibility = View.VISIBLE
    }

    override fun setUsernameEmptyError() {
        mUsername!!.error = getString(uci.localproxy.R.string.username_empty_error)
    }

    override fun setPasswordEmptyError() {
        mPassword!!.error = getString(uci.localproxy.R.string.password_empty_error)
    }

    override fun setProfileNoSelectedError() {
        Snackbar.make(
            mUsername!!,
            uci.localproxy.R.string.no_profile_selected_error,
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun setLocalPortEmptyError() {
        mLocalPortEditText!!.error = getString(uci.localproxy.R.string.proxy_port_empty_error)
    }

    override fun setLocalPortOutOfRangeError() {
        mLocalPortEditText!!.error = String.format(
            Locale.ENGLISH, getString(uci.localproxy.R.string.profile_port_outofrange_error),
            AddEditProfilePresenter.Companion.MAX_SYSTEM_PORTS_LIMIT,
            AddEditProfilePresenter.Companion.MAX_PORTS_LIMIT
        )
    }

    override fun setUsers(users: List<User>) {
        mUserArrayAdapter!!.replaceData(users)
    }

    override fun showAddProfile() {
        val intent = Intent(context, AddEditProfileActivity::class.java)
        startActivityForResult(intent, AddEditProfileActivity.Companion.REQUEST_ADD_PROFILE)
    }

    override val isConnectedToAWifi: Boolean
        get() {
            val connManager =
                requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return mWifi!!.isConnected
        }

    //    @Override
    //    public boolean isProxyServiceRunning() {
    //        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
    //        for (ActivityManager.RunningServiceInfo service : manager
    //                .getRunningServices(Integer.MAX_VALUE)) {
    //            if (ProxyService.class.getName().equals(
    //                    service.service.getClassName())) {
    //                Log.i(ProxyActivity.class.getName(), "Service running");
    //                return true;
    //            }
    //        }
    //        Log.i(ProxyActivity.class.getName(), "Service not running");
    //        return false;
    //    }
    override fun startProxyService(
        username: String?,
        password: String?,
        server: String?,
        inputport: Int,
        outputport: Int,
        bypass: String?,
        domain: String?,
        setGlobProxy: Boolean
    ) {
        val proxyIntent = Intent(activity, ProxyService::class.java)
        proxyIntent.putExtra("user", username)
        proxyIntent.putExtra("pass", password)
        proxyIntent.putExtra("server", server)
        proxyIntent.putExtra("inputport", inputport.toString() + "")
        proxyIntent.putExtra("outputport", outputport.toString() + "")
        proxyIntent.putExtra("bypass", bypass)
        proxyIntent.putExtra("domain", domain)
        proxyIntent.putExtra("set_global_proxy", setGlobProxy)
        activity!!.startService(proxyIntent)
    }

    override fun showWifiConfDialog() {
        createWifiAlertDialog().show()
    }

    override fun startWifiConfActivity() {
        val i = Intent(WifiManager.ACTION_PICK_WIFI_NETWORK)
        i.flags =
            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
        requireContext().startActivity(i)
    }

    override fun showProgressDialog(show: Boolean) {
        if (show) {
            mProgressDialog!!.setMessage(getString(uci.localproxy.R.string.proxy_credentials_check_message))
            mProgressDialog!!.setOnDismissListener { mPresenter!!.stopCredentialCheckTask() }
            mProgressDialog!!.show()
        } else {
            mProgressDialog!!.setOnDismissListener(null)
            mProgressDialog!!.dismiss()
        }
    }

    override fun showWrongCredentialsDialog() {
        createAlertDialog(
            getString(uci.localproxy.R.string.proxy_credentials_error),
            getString(uci.localproxy.R.string.proxy_credentials_error_message)
        ).show()
    }

    override fun showNetworkError() {
        createAlertDialog(
            getString(uci.localproxy.R.string.proxy_network_error),
            getString(uci.localproxy.R.string.proxy_network_error_message)
        ).show()
    }

    override fun showConnectionTimeOutError() {
        createAlertDialog(
            getString(uci.localproxy.R.string.proxy_connectiontimeout_error),
            getString(uci.localproxy.R.string.proxy_connectiontimeout_error_message)
        ).show()
    }

    override fun showUnknownHostError() {
        createAlertDialog(
            getString(uci.localproxy.R.string.proxy_unknownhost_error),
            getString(uci.localproxy.R.string.proxy_unknownhost_error_message)
        ).show()
    }

    override fun showConnectionError() {
        createAlertDialog(
            getString(uci.localproxy.R.string.proxy_connection_error),
            getString(uci.localproxy.R.string.proxy_connection_error_message)
        ).show()
    }

    override fun showErrorStartingService() {
        createAlertDialog(
            getString(uci.localproxy.R.string.error_starting_proxy_service_title),
            getString(uci.localproxy.R.string.local_port_not_available)
        ).show()
    }


    override fun setLocalPortNotAvailable() {
        createAlertDialog(
            getString(uci.localproxy.R.string.port_not_available_title),
            getString(uci.localproxy.R.string.local_port_not_available)
        ).show()
        //        mLocalPortEditText.setError(getString(R.string.port_not_available_title));
    }

    private fun createAlertDialog(title: String, message: String): AlertDialog {
        val builder = AlertDialog.Builder(
            context!!
        )
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(getString(uci.localproxy.R.string.accept_text)) { dialog, which -> dialog.cancel() }
        return builder.create()
    }

    override fun stopProxyService() {
        val proxyIntent = Intent(activity, ProxyService::class.java)
        activity!!.stopService(proxyIntent)
    }

    private fun createWifiAlertDialog(): AlertDialog {
        val builder = AlertDialog.Builder(
            context!!
        )
        val inflater = (context as AppCompatActivity?)!!.layoutInflater
        val view = inflater.inflate(uci.localproxy.R.layout.wifi_alert_dialog, null)
        val wifiConfigImage = view.findViewById<ImageView>(uci.localproxy.R.id.wifiConfigImageView)
        val mAtacher = PhotoViewAttacher(wifiConfigImage)
        val dontShowCheckBox = view.findViewById<CheckBox>(uci.localproxy.R.id.dontShowCheckBox)
        builder.setTitle(requireContext().resources.getString(uci.localproxy.R.string.wifiSettings))
        builder.setView(view)
        builder.setPositiveButton(uci.localproxy.R.string.wifiSettingsGoToWifi) { dialog, which ->
            mPresenter!!.goToWifiSettings(
                dontShowCheckBox.isChecked
            )
        }
        builder.setNegativeButton(uci.localproxy.R.string.wifiSettingsContinue) { dialog, which ->
            val profileId =
                if (mProfileSpinner!!.selectedItem == null) "" else (mProfileSpinner!!.selectedItem as Profile).id
            mPresenter!!.startProxyFromHelpDialog(
                mUsername!!.text.toString(),
                mPassword!!.text.toString(),
                profileId,
                mLocalPortEditText!!.text.toString(),
                mRememberPasswordCheck!!.isChecked,
                mGlobalProxyCheck!!.isChecked,
                dontShowCheckBox.isChecked
            )
            dialog.cancel()
        }
        return builder.create()
    }

    private inner class UsersArrayAdapter(context: Context, users: ArrayList<User?>) :
        ArrayAdapter<User?>(context, uci.localproxy.R.layout.user_list_item, users) {
        private val viewResourceId = uci.localproxy.R.layout.user_list_item
        private fun setList(users: List<User>) {
            clear()
            addAll(users)
        }

        fun replaceData(users: List<User>) {
            setList(users)
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var rowView = convertView
            if (rowView == null) {
                val inflater = LayoutInflater.from(parent.context)
                rowView = inflater.inflate(viewResourceId, parent, false)
            }
            val user = getItem(position)
            val username = rowView!!.findViewById<TextView>(uci.localproxy.R.id.user_username)
            if (user != null) {
                username.text = user.username
            }
            rowView.setOnClickListener {
                mUsername!!.setText(user!!.username)
                val password = user.password
                mPassword!!.text = password
                if (Strings.isNullOrEmpty(password)) mRememberPasswordCheck!!.isChecked =
                    false else mRememberPasswordCheck!!.isChecked = true
                mUsername!!.dismissDropDown()
            }
            return rowView
        }

        override fun getFilter(): Filter {
            return usernameFilter
        }

        var usernameFilter: Filter = object : Filter() {
            override fun convertResultToString(resultValue: Any): CharSequence {
                return (resultValue as User).username
            }

            override fun performFiltering(constraint: CharSequence): FilterResults {
                // This is performed in a worker thread, I have nothing to do here because my realm
                //instance is in the UI thread, this is a kind of issue but it's a few data that not affect
                // the view. I'm going to handle the filtering in the publishResults method.
                // I have to do this because Realm doesn't have any good implementation for this, for now
                // THIS WORK!!!!!!.
                return FilterResults()
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                //This is executed in the UI thread, PERFECT!!!!
                if (constraint != null && constraint != "") {
                    mPresenter!!.filterUsers(constraint.toString())
                } else {
                    mPresenter!!.loadUsers()
                }
            }
        }
    }

    companion object {
        //    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        //        @Override
        //        public void onReceive(Context context, Intent intent) {
        //            int message = intent.getIntExtra(ProxyService.MESSAGE_TAG, ProxyService.SERVICE_STARTED_SUCCESSFUL);
        //            Log.e("serviceMessage", message+"");
        //            mPresenter.startServiceResult(message);
        //        }
        //    };
        fun newInstance(): ProxyFragment {
            return ProxyFragment()
        }
    }
}