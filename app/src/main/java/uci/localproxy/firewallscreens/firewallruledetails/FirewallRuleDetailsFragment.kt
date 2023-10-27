package uci.localproxy.firewallscreens.firewallruledetails

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import uci.localproxy.R
import uci.localproxy.firewallscreens.addeditfirewallrule.AddEditFirewallRuleActivity
import uci.localproxy.firewallscreens.addeditfirewallrule.AddEditFirewallRuleFragment

/**
 * Created by daniel on 2/10/17.
 */
class FirewallRuleDetailsFragment constructor() : Fragment(), FirewallRuleDetailsContract.View {
    private var mPresenter: FirewallRuleDetailsContract.Presenter? = null
    private var mPackageLogoTv: ImageView? = null
    private var mApplicationNameTv: TextView? = null
    private var mRuleTv: TextView? = null
    private var mDescriptionTv: TextView? = null
    public override fun onResume() {
        super.onResume()
        mPresenter!!.start()
    }

    public override fun setPresenter(presenter: FirewallRuleDetailsContract.Presenter?) {
        mPresenter = presenter
    }

    public override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View = inflater.inflate(R.layout.firewallrule_details_frag, container, false)
        mRuleTv = root.findViewById(R.id.firewallrule_detail_rule)
        mDescriptionTv = root.findViewById(R.id.firewallrule_detail_description)
        mApplicationNameTv = root.findViewById(R.id.applicationName)
        mPackageLogoTv = root.findViewById(R.id.packageLogo)
        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab_edit_firewallrule)
        fab.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                mPresenter!!.editFirewallRule()
            }
        })
        setHasOptionsMenu(true)
        return root
    }

    public override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.firewallruledetail_fragment_menu, menu)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.menu_delete -> mPresenter!!.deleteFirewallRule()
        }
        return false
    }

    public override fun onDestroy() {
        mPresenter!!.onDestroy()
        super.onDestroy()
    }

    public override fun showMissingFirewallRule() {
        showMessage(getString(R.string.missing_data_message))
    }

    public override fun showRule(rule: String?) {
        mRuleTv!!.setText(rule)
    }

    public override fun showDescription(description: String?) {
        mDescriptionTv!!.setText(description)
    }

    public override fun showFirewallRuleDeleted() {
        requireActivity().finish()
    }

    public override fun showApplicationName(packageName: String?) {
        mApplicationNameTv!!.setText(packageName)
    }

    public override fun showAllApplicationPackageName() {
        mApplicationNameTv!!.setTextSize(20f)
        mApplicationNameTv!!.setText(getString(R.string.all_applications))
    }

    public override fun showPackageLogo(packageName: String) {
        try {
            mPackageLogoTv!!.setImageDrawable(getPackageLogoDrawable(packageName, getContext()))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    public override fun showNoPackageLogo() {
        mPackageLogoTv!!.setVisibility(View.GONE)
    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun getPackageLogoDrawable(packageName: String, context: Context?): Drawable {
        val pm: PackageManager = requireContext().getPackageManager()
        val drawable: Drawable = pm.getApplicationIcon(packageName)
        return drawable
    }

    public override fun showSuccessfullyUpdatedMessage() {
        showMessage(getString(R.string.successfully_firewallrule_updated_message))
    }

    public override fun showEditFirewallRule(firewallRuleId: String?) {
        val intent: Intent = Intent(getContext(), AddEditFirewallRuleActivity::class.java)
        intent.putExtra(
            AddEditFirewallRuleFragment.Companion.ARGUMENT_EDIT_FIREWALL_RULE_ID,
            firewallRuleId
        )
        startActivityForResult(intent, REQUEST_EDIT_FIREWALL_RULE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mPresenter!!.result(requestCode, resultCode)
    }

    override val isActive: Boolean
        get() {
            return isAdded()
        }

    private fun showMessage(message: String) {
        Snackbar.make((mRuleTv)!!, message, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        val ARGUMENT_FIREWALL_RULE_ID: String = "FIREWALL_RULE_ID"
        val REQUEST_EDIT_FIREWALL_RULE: Int = 1
        fun newInstance(): FirewallRuleDetailsFragment {
            return FirewallRuleDetailsFragment()
        }
    }
}