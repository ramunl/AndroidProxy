package uci.localproxy.firewallscreens.firewallruleslist

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import uci.localproxy.R
import uci.localproxy.firewallscreens.addeditfirewallrule.AddEditFirewallRuleActivity
import uci.localproxy.firewallscreens.firewallruledetails.FirewallRuleDetailsActivity
import uci.localproxy.firewallscreens.firewallruledetails.FirewallRuleDetailsFragment
import uci.localproxy.proxydata.applicationPackage.ApplicationPackage
import uci.localproxy.proxydata.applicationPackage.ApplicationPackageLocalDataSource
import uci.localproxy.proxydata.firewallRule.FirewallRule

/**
 * Created by daniel on 29/09/17.
 */
class FirewallRulesListFragment constructor() : Fragment(), FirewallRulesListContract.View {
    private var mPresenter: FirewallRulesListContract.Presenter? = null
    private var mListAdapter: FirewallRulesAdapter? = null
    private var mNoFirewallRulesView: View? = null
    private var mFirewallRulesView: LinearLayout? = null
    var mItemListener: FirewallRuleItemListener = object : FirewallRuleItemListener {
        public override fun onFirewallRuleClick(clickedFirewallRule: FirewallRule) {
            mPresenter!!.openFirewallRuleDetails(clickedFirewallRule)
        }

        public override fun onFirewallRuleCheckClick(
            checkedFirewallRule: FirewallRule,
            isChecked: Boolean
        ) {
            mPresenter!!.activateFirewallRule(checkedFirewallRule, isChecked)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mListAdapter = FirewallRulesAdapter(ArrayList(0), mItemListener)
    }

    public override fun onResume() {
        super.onResume()
        mPresenter!!.start()
    }

    public override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View = inflater.inflate(R.layout.firewallrules_list_frag, container, false)

        //Set up FirewallRules view
        val listView: ListView = root.findViewById(R.id.firewallRules_list)
        listView.setAdapter(mListAdapter)
        mFirewallRulesView = root.findViewById(R.id.firewallRulesLL)

        //Set up no FirewallRules view
        mNoFirewallRulesView = root.findViewById(R.id.noFirewallRules)
        val noFirewallRulesAddView: TextView = root.findViewById(R.id.noFirewallRulesAdd)
        noFirewallRulesAddView.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                showAddFirewallRule()
            }
        })

        //Set up floating action button
        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab_add_firewall_rule)
        fab.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                mPresenter!!.addNewFirewallRule()
            }
        })
        setHasOptionsMenu(true)
        return root
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mPresenter!!.result(requestCode, resultCode)
    }

    public override fun setPresenter(presenter: FirewallRulesListContract.Presenter?) {
        mPresenter = presenter
    }

    public override fun showFirewallRules(firewallRules: List<FirewallRule>) {
        mListAdapter!!.replaceData(firewallRules)
        mFirewallRulesView!!.setVisibility(View.VISIBLE)
        mNoFirewallRulesView!!.setVisibility(View.GONE)
    }

    public override fun showAddFirewallRule() {
        val intent: Intent = Intent(getContext(), AddEditFirewallRuleActivity::class.java)
        startActivityForResult(
            intent,
            AddEditFirewallRuleActivity.Companion.REQUEST_ADD_FIREWALL_RULE
        )
    }

    public override fun showNoFirewallRules() {
        mFirewallRulesView!!.setVisibility(View.GONE)
        mNoFirewallRulesView!!.setVisibility(View.VISIBLE)
    }

    public override fun showFirewallRuleDetailsUI(firewallRuleId: String?) {
        val intent: Intent = Intent(getContext(), FirewallRuleDetailsActivity::class.java)
        intent.putExtra(
            FirewallRuleDetailsFragment.Companion.ARGUMENT_FIREWALL_RULE_ID,
            firewallRuleId
        )
        startActivity(intent)
    }

    public override fun showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_firewallRule_message))
    }

    public override fun showFirewallRuleActivated() {
        showMessage(getString(R.string.firewall_rule_activated))
    }

    public override fun showFirewallRuleDeactivate() {
        showMessage(getString(R.string.firewall_rule_deactivated))
    }

    private fun showMessage(message: String) {
        Snackbar.make((getView())!!, message, Snackbar.LENGTH_LONG).show()
    }

    override val isActive: Boolean
        get() {
            return isAdded()
        }

    private inner class FirewallRulesAdapter constructor(
        private var mFirewallRules: List<FirewallRule>,
        private val mItemListener: FirewallRuleItemListener
    ) : BaseAdapter() {
        private fun setList(firewallRules: List<FirewallRule>) {
            mFirewallRules = firewallRules
        }

        fun replaceData(firewallRules: List<FirewallRule>) {
            setList(firewallRules)
            notifyDataSetChanged()
        }

        public override fun getCount(): Int {
            return mFirewallRules.size
        }

        public override fun getItem(position: Int): FirewallRule {
            return mFirewallRules.get(position)
        }

        public override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        inner class ViewHolder constructor() {
            var applicationName: TextView? = null
            var packageLogo: ImageView? = null
            var ruleTv: TextView? = null
            var checked: CheckBox? = null
        }

        public override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            var rowView: View = convertView
            if (rowView == null) {
                val inflater: LayoutInflater = LayoutInflater.from(parent.getContext())
                rowView = inflater.inflate(R.layout.firewallrule_list_item, parent, false)
                val viewHolder: ViewHolder = ViewHolder()
                viewHolder.applicationName = rowView.findViewById(R.id.applicationName)
                viewHolder.packageLogo = rowView.findViewById(R.id.packageLogo)
                viewHolder.ruleTv = rowView.findViewById(R.id.rule)
                viewHolder.checked = rowView.findViewById(R.id.active)
                rowView.setTag(viewHolder)
            }
            val viewHolder: ViewHolder = rowView.getTag() as ViewHolder
            val firewallRule: FirewallRule = getItem(position)
            val applicationPackage: ApplicationPackage? =
                ApplicationPackageLocalDataSource.getInstance(
                    (getContext())!!
                )
                    .getApplicationPackageByPackageName(firewallRule.getApplicationPackageName())
            if (applicationPackage != null) {
                if ((applicationPackage.getName()
                            == ApplicationPackageLocalDataSource.ALL_APPLICATION_PACKAGES_STRING)
                ) {
                    viewHolder.packageLogo!!.setVisibility(View.GONE)
                    viewHolder.applicationName!!.setText(getString(R.string.all_applications))
                } else {
                    viewHolder.packageLogo!!.setVisibility(View.VISIBLE)
                    viewHolder.applicationName!!.setText(applicationPackage.getName())
                    val packageManager: PackageManager = requireContext().getPackageManager()
                    try {
                        viewHolder.packageLogo!!.setImageDrawable(
                            packageManager.getApplicationIcon(
                                applicationPackage.getPackageName()
                            )
                        )
                    } catch (e: PackageManager.NameNotFoundException) {
                        viewHolder.packageLogo!!.setImageResource(android.R.drawable.sym_def_app_icon)
                    }
                }
                viewHolder.ruleTv!!.setText(firewallRule.getRule())
                viewHolder.checked!!.setChecked(firewallRule.isActive)
                if (!firewallRule.isActive) {
                    rowView.setBackgroundDrawable(
                        parent.getContext()
                            .getResources().getDrawable(R.drawable.list_deactivate_touch_feedback)
                    )
                } else {
                    rowView.setBackgroundDrawable(
                        parent.getContext()
                            .getResources().getDrawable(R.drawable.touch_feedback)
                    )
                }
                viewHolder.checked!!.setOnClickListener(object : View.OnClickListener {
                    public override fun onClick(v: View) {
                        mItemListener.onFirewallRuleCheckClick(
                            firewallRule,
                            !firewallRule.isActive
                        )
                    }
                })
                val firewallRuleListItemLL: View = rowView.findViewById(R.id.firewallRuleListItemLL)
                firewallRuleListItemLL.setOnClickListener(object : View.OnClickListener {
                    public override fun onClick(v: View) {
                        mItemListener.onFirewallRuleClick(firewallRule)
                    }
                })
            }
            return rowView
        }

        @Throws(PackageManager.NameNotFoundException::class)
        private fun getPackageLogoDrawable(packageName: String, context: Context): Drawable {
            val pm: PackageManager = context.getPackageManager()
            val drawable: Drawable = pm.getApplicationIcon(packageName)
            return drawable
        }
    }

    open interface FirewallRuleItemListener {
        fun onFirewallRuleClick(clickedFirewallRule: FirewallRule)
        fun onFirewallRuleCheckClick(checkedFirewallRule: FirewallRule, isChecked: Boolean)
    }

    companion object {
        fun newInstance(): FirewallRulesListFragment {
            return FirewallRulesListFragment()
        }
    }
}