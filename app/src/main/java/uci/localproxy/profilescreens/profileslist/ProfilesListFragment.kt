package uci.localproxy.profilescreens.profileslist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import uci.localproxy.R
import uci.localproxy.profilescreens.addeditprofile.AddEditProfileActivity
import uci.localproxy.profilescreens.profiledetails.ProfileDetailsActivity
import uci.localproxy.proxydata.profile.Profile

/**
 * Created by daniel on 16/09/17.
 */
class ProfilesListFragment constructor() : Fragment(), ProfilesListContract.View {
    private var mPresenter: ProfilesListContract.Presenter? = null
    private var mListAdapter: ProfilesAdapter? = null
    private var mNoProfilesView: View? = null
    private var mProfilesView: LinearLayout? = null
    var mItemListener: ProfileItemListener = object : ProfileItemListener {
        public override fun onProfileClick(clickedProfile: Profile) {
            mPresenter!!.openProfileDetails(clickedProfile)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mListAdapter = ProfilesAdapter(ArrayList(0), mItemListener)
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
        val root: View = inflater.inflate(R.layout.profiles_list_frag, container, false)

        //Set up profiles view
        val listView: ListView = root.findViewById(R.id.profiles_list)
        listView.setAdapter(mListAdapter)
        mProfilesView = root.findViewById(R.id.profilesLL)

        //Set up no profiles view
        mNoProfilesView = root.findViewById(R.id.noProfiles)
        val mNoProfilesAddView: TextView = root.findViewById(R.id.noProfilesAdd)
        mNoProfilesAddView.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                showAddProfile()
            }
        })

        // Set up floating action button
        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab_add_task)
        fab.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                mPresenter!!.addNewProfile()
            }
        })
        setHasOptionsMenu(true)
        return root
    }

    public override fun onDestroy() {
        super.onDestroy()
        mPresenter!!.onDestroy()
    }

    public override fun setPresenter(presenter: ProfilesListContract.Presenter?) {
        mPresenter = presenter
    }

    public override fun setLoadingIndicator(active: Boolean) {
        //TODO
    }

    public override fun showProfiles(profiles: List<Profile>?) {
        mListAdapter!!.replaceData(profiles)
        mProfilesView!!.setVisibility(View.VISIBLE)
        mNoProfilesView!!.setVisibility(View.GONE)
    }

    public override fun showNoProfiles() {
        mProfilesView!!.setVisibility(View.GONE)
        mNoProfilesView!!.setVisibility(View.VISIBLE)
    }

    public override fun showAddProfile() {
        val intent: Intent = Intent(getContext(), AddEditProfileActivity::class.java)
        startActivityForResult(intent, AddEditProfileActivity.Companion.REQUEST_ADD_PROFILE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mPresenter!!.result(requestCode, resultCode)
    }

    public override fun showProfileDetailsUI(profileId: String?) {
        val i: Intent = Intent(getContext(), ProfileDetailsActivity::class.java)
        i.putExtra(ProfileDetailsActivity.Companion.EXTRA_PROFILE_ID, profileId)
        startActivity(i)
    }

    public override fun showSuccessfullySavedMessage() {
        showMessage(getResources().getString(R.string.successfully_saved_profile_message))
    }

    override val isActive: Boolean
        get() {
            return isAdded()
        }

    private fun showMessage(message: String) {
        Snackbar.make((getView())!!, message, Snackbar.LENGTH_LONG).show()
    }

    private class ProfilesAdapter constructor(
        profiles: List<Profile>?,
        itemListener: ProfileItemListener
    ) : BaseAdapter() {
        private var mProfiles: List<Profile>? = null
        private val mItemListener: ProfileItemListener

        init {
            setList(profiles)
            mItemListener = itemListener
        }

        private fun setList(profiles: List<Profile>?) {
            mProfiles = profiles
        }

        fun replaceData(profiles: List<Profile>?) {
            setList(profiles)
            notifyDataSetChanged()
        }

        public override fun getCount(): Int {
            return mProfiles!!.size
        }

        public override fun getItem(position: Int): Profile {
            return mProfiles!!.get(position)
        }

        public override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        inner class ViewHolder constructor() {
            var titleTV: TextView? = null
        }

        public override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            var rowView: View = convertView
            if (rowView == null) {
                val inflater: LayoutInflater = LayoutInflater.from(parent.getContext())
                rowView = inflater.inflate(R.layout.profiles_list_item, parent, false)
                val viewHolder: ViewHolder = ViewHolder()
                viewHolder.titleTV = rowView.findViewById(R.id.title)
                rowView.setTag(viewHolder)
            }
            val viewHolder: ViewHolder = rowView.getTag() as ViewHolder
            val profile: Profile = getItem(position)
            viewHolder.titleTV!!.setText(profile.getName())
            rowView.setOnClickListener(object : View.OnClickListener {
                public override fun onClick(v: View) {
                    mItemListener.onProfileClick(profile)
                }
            })
            return rowView
        }
    }

    open interface ProfileItemListener {
        fun onProfileClick(clickedProfile: Profile)
    }

    companion object {
        fun newInstance(): ProfilesListFragment {
            return ProfilesListFragment()
        }
    }
}