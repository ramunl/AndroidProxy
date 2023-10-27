package uci.localproxy.firewallscreens.addeditfirewallrule

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import uci.localproxy.R
import uci.localproxy.proxydata.applicationPackage.ApplicationPackage
import uci.localproxy.proxydata.applicationPackage.ApplicationPackageLocalDataSource

/**
 * Created by daniel on 1/10/17.
 */
class AddEditFirewallRuleFragment constructor() : Fragment(), AddEditFirewallRuleContract.View {
    private var mPresenter: AddEditFirewallRuleContract.Presenter? = null
    private var mRule: TextView? = null
    private var mPackageNameSpinner: Spinner? = null
    private var mApplicationPackageAdapter: ApplicationPackagesAdapter? = null
    private var mDescription: TextView? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mApplicationPackageAdapter = ApplicationPackagesAdapter(
            (getContext())!!,
            ArrayList(0)
        )
    }

    public override fun onResume() {
        super.onResume()
        mPresenter!!.start()
    }

    public override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val fab: FloatingActionButton =
            requireActivity().findViewById(R.id.fab_edit_firewallRule_done)
        fab.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                mPresenter!!.saveFirewallRule(
                    mRule!!.getText().toString(),
                    (mPackageNameSpinner!!.getSelectedItem() as ApplicationPackage).getPackageName(),
                    mDescription!!.getText().toString()
                )
            }
        })
    }

    public override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View = inflater.inflate(R.layout.addfirewallrule_frag, container, false)
        mRule = root.findViewById(R.id.add_firewallRule_rule)
        mDescription = root.findViewById(R.id.add_firewallRule_description)
        mPackageNameSpinner = root.findViewById(R.id.applicationSpinner)
        mPackageNameSpinner!!.setAdapter(mApplicationPackageAdapter)
        setHasOptionsMenu(true)
        return root
    }

    public override fun onDestroy() {
        mPresenter!!.onDestroy()
        super.onDestroy()
    }

    public override fun setPresenter(presenter: AddEditFirewallRuleContract.Presenter?) {
        mPresenter = presenter
    }

    public override fun showEmptyFirewallRuleError() {
        Snackbar.make(
            (mRule)!!,
            getString(R.string.empty_firewallRule_message),
            Snackbar.LENGTH_LONG
        ).show()
    }

    public override fun finishAddEditFirewallRuleActivity() {
        requireActivity().setResult(Activity.RESULT_OK)
        requireActivity().finish()
    }

    public override fun setRule(rule: String?) {
        mRule!!.setText(rule)
    }

    public override fun setDescription(description: String?) {
        mDescription!!.setText(description)
    }

    public override fun setEmptyRuleError() {
        mRule!!.setError(getString(R.string.empty_rule_message))
    }

    public override fun setApplicationPackages(applicationPackages: List<ApplicationPackage>) {
        mApplicationPackageAdapter!!.replaceData(applicationPackages)
    }

    public override fun setSpinnerApplicationPackageSelected(packageName: String) {
        var pos: Int = -1
        for (i in 0 until mApplicationPackageAdapter!!.getCount()) {
            if ((packageName == (mApplicationPackageAdapter!!.getItem(i) as ApplicationPackage).getPackageName())) {
                pos = i
                break
            }
        }
        if (pos >= 0) {
            mPackageNameSpinner!!.setSelection(pos, false)
        }
    }

    override val isActive: Boolean
        get() {
            return isAdded()
        }

    private inner class ApplicationPackagesAdapter constructor(
        context: Context,
        private var items: List<ApplicationPackage>
    ) : BaseAdapter() {
        private val itemViewResourceId: Int = R.layout.application_package_item
        private fun setList(applicationPackages: List<ApplicationPackage>) {
            items = applicationPackages
        }

        fun replaceData(applicationPackages: List<ApplicationPackage>) {
            setList(applicationPackages)
            notifyDataSetChanged()
        }

        public override fun getCount(): Int {
            return items.size
        }

        public override fun getItem(i: Int): Any {
            return items.get(i)
        }

        public override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        public override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return (createView(position, convertView, parent))!!
        }

        public override fun getDropDownView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            return (createView(position, convertView, parent))!!
        }

        inner class ViewHolder constructor() {
            var logo: ImageView? = null
            var packageName: TextView? = null
        }

        private fun createView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var rowView: View? = convertView
            if (rowView == null) {
                val inflater: LayoutInflater = LayoutInflater.from(parent.getContext())
                rowView = inflater.inflate(itemViewResourceId, parent, false)
                val viewHolder: ViewHolder = ViewHolder()
                viewHolder.logo = rowView.findViewById(R.id.application_package_item_logo)
                viewHolder.packageName = rowView.findViewById(R.id.application_package_item_name)
                rowView.setTag(viewHolder)
            }
            val viewHolder: ViewHolder = rowView!!.getTag() as ViewHolder
            val applicationPackage: ApplicationPackage? = getItem(position) as ApplicationPackage?
            //            Log.e("AppPackageName", applicationPackage.getName());
//            Log.e("AppPackage", applicationPackage.getPackageName());

//            Log.e("packageName", applicationPackage.getName() + ": " + applicationPackage.getPackageName());
            if (applicationPackage != null) {
                if ((applicationPackage.getPackageName() == ApplicationPackageLocalDataSource.ALL_APPLICATION_PACKAGES_STRING)) {
                    viewHolder.packageName!!.setText(getResources().getString(R.string.all_applications))
                    viewHolder.packageName!!.setTextSize(25f)
                    viewHolder.logo!!.setVisibility(View.GONE)
                } else {
                    viewHolder.packageName!!.setText(applicationPackage.getName())
                    viewHolder.packageName!!.setTextSize(15f)
                    viewHolder.logo!!.setVisibility(View.VISIBLE)
                    val packageManager: PackageManager = requireContext().getPackageManager()
                    try {
                        viewHolder.logo!!.setImageDrawable(
                            packageManager.getApplicationIcon(
                                applicationPackage.getPackageName()
                            )
                        )
                    } catch (e: PackageManager.NameNotFoundException) {
                        viewHolder.logo!!.setImageResource(android.R.drawable.sym_def_app_icon)
                    }
                }
            }
            return rowView
        }
    }

    companion object {
        val ARGUMENT_EDIT_FIREWALL_RULE_ID: String = "EDIT_FIREWALL_RULE_ID"
        fun newInstance(): AddEditFirewallRuleFragment {
            return AddEditFirewallRuleFragment()
        }
    }
}