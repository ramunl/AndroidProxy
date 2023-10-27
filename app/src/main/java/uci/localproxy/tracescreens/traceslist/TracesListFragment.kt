package uci.localproxy.tracescreens.traceslist

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.snackbar.Snackbar
import uci.localproxy.R
import uci.localproxy.proxydata.trace.Trace
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Created by daniel on 16/02/18.
 */
class TracesListFragment() : Fragment(), TracesListContract.View {
    private var mPresenter: TracesListContract.Presenter? = null
    private var mAdapter: TracesAdapter? = null
    private var mNoTracesView: View? = null
    private var mTracesView: LinearLayout? = null
    private var FILTER_KEY = ""
    private var SORT_BY_CONSUMPTION = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = TracesAdapter(ArrayList(0))
    }

    override fun onResume() {
        super.onResume()
        mPresenter!!.start()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.traces_list_frag, container, false)
        mNoTracesView = root.findViewById(R.id.noTraces)
        mTracesView = root.findViewById(R.id.tracesLL)
        val tracesRecycler = root.findViewById<RecyclerView>(R.id.traces_recycler_view)
        //        tracesRecycler.setHasFixedSize(true);
        val layoutManager = LinearLayoutManager(context)
        tracesRecycler.layoutManager = layoutManager
        tracesRecycler.adapter = mAdapter
        tracesRecycler.addItemDecoration(
            DividerItemDecoration(
                tracesRecycler.context, layoutManager.orientation
            )
        )

        // Set up progress indicator
        val swipeRefreshLayout =
            root.findViewById<ScrollChildSwipeRefreshLayout>(R.id.refresh_layout)
        swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor((activity)!!, R.color.colorPrimary),
            ContextCompat.getColor((activity)!!, R.color.colorAccent),
            ContextCompat.getColor((activity)!!, R.color.colorPrimaryDark)
        )
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(tracesRecycler)
        swipeRefreshLayout.setOnRefreshListener(OnRefreshListener {
            mPresenter!!.loadTraces(
                FILTER_KEY,
                SORT_BY_CONSUMPTION
            )
        })
        setHasOptionsMenu(true)
        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter!!.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.trace_list_menu, menu)
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView?
        searchView!!.queryHint = getString(R.string.query_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                FILTER_KEY = query
                mPresenter!!.loadTraces(FILTER_KEY, SORT_BY_CONSUMPTION)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                FILTER_KEY = ""
                mPresenter!!.loadTraces(FILTER_KEY, SORT_BY_CONSUMPTION)
                return true
            }
        })

        // Get the search close button image view
        val closeButton = searchView.findViewById<ImageView>(R.id.search_close_btn)

        // Set on click listener
        closeButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val et = searchView.findViewById<EditText>(R.id.search_src_text)
                //Clear the text from EditText view
                et.setText("")
                //Clear query
                searchView.setQuery("", false)
                FILTER_KEY = ""
                mPresenter!!.loadTraces(FILTER_KEY, SORT_BY_CONSUMPTION)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.sortByConsumption -> {
                if (item.isChecked) {
                    item.isChecked = false
                    SORT_BY_CONSUMPTION = false
                } else {
                    item.isChecked = true
                    SORT_BY_CONSUMPTION = true
                }
                mPresenter!!.loadTraces(FILTER_KEY, SORT_BY_CONSUMPTION)
            }

            R.id.clear_all -> mPresenter!!.deleteAllTraces()
        }
        return true
    }

    override fun setLoadingIndicator(active: Boolean) {
        if (view == null) return
        val srl = requireView().findViewById<SwipeRefreshLayout>(R.id.refresh_layout)

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(object : Runnable {
            override fun run() {
                srl.isRefreshing = active
            }
        })
    }

    override fun showTraces(traces: List<Trace>) {
        mAdapter!!.replaceData(traces)
        mNoTracesView!!.visibility = View.GONE
        mTracesView!!.visibility = View.VISIBLE
    }

    override fun showSuccessfullyAddedAsFirewallRuleMessage() {
        Snackbar.make(
            requireActivity().findViewById(R.id.contentFrame),
            resources.getString(R.string.successfully_added_as_firewallrule),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun showNoTraces() {
        mNoTracesView!!.visibility = View.VISIBLE
        mTracesView!!.visibility = View.GONE
    }

    override val isActive: Boolean
        get() = isAdded

    override fun setPresenter(presenter: TracesListContract.Presenter?) {
        mPresenter = presenter
    }

    private inner class TracesAdapter(private var mDataSet: List<Trace>) :
        RecyclerView.Adapter<TracesAdapter.ViewHolder>() {
        inner class ViewHolder  constructor(view: View) : RecyclerView.ViewHolder(view) {
            lateinit var appIcon: ImageView
            lateinit var url: TextView
            var consumption: TextView
            lateinit var appName: TextView
            lateinit var expandTrace: ImageButton
            lateinit var date: TextView

            init {
                view.setOnLongClickListener(object : View.OnLongClickListener {
                    override fun onLongClick(view: View): Boolean {
                        val menu = PopupMenu(context, view)
                        menu.menu.add(getString(R.string.add_as_firewall_rule))
                            .setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
                                override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                                    val alertDialog = AlertDialog.Builder(
                                        (context)!!
                                    ).create()
                                    alertDialog.setMessage(getString(R.string.firewallRule_rule_tv))
                                    val editRule = EditText(context)
                                    editRule.setText(url.text)
                                    alertDialog.setView(editRule)
                                    alertDialog.setButton(
                                        AlertDialog.BUTTON_POSITIVE, getString(R.string.add),
                                        object : DialogInterface.OnClickListener {
                                            override fun onClick(
                                                dialog: DialogInterface,
                                                which: Int
                                            ) {
                                                alertDialog.dismiss()
                                                mPresenter!!.addAsFirewallRule(
                                                    editRule.text.toString(),
                                                    appName.tag as String
                                                )
                                            }
                                        })
                                    alertDialog.setButton(
                                        AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                                        object : DialogInterface.OnClickListener {
                                            override fun onClick(
                                                dialogInterface: DialogInterface,
                                                i: Int
                                            ) {
                                                alertDialog.dismiss()
                                            }
                                        })
                                    alertDialog.show()
                                    return false
                                }
                            })
                        menu.show()
                        return false
                    }
                })
                appIcon = view.findViewById(R.id.packageLogo)
                url = view.findViewById(R.id.url)
                consumption = view.findViewById(R.id.consumption)
                appName = view.findViewById(R.id.applicationName)
                date = view.findViewById(R.id.date)
                expandTrace = view.findViewById(R.id.expand_trace)
            }
        }

        private fun setList(dataSet: List<Trace>) {
            mDataSet = dataSet
        }

        fun replaceData(dataSet: List<Trace>) {
            setList(dataSet)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.trace_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val trace = mDataSet[position]
            val packageManager = requireContext().packageManager
            try {
                holder.appIcon.setImageDrawable(packageManager.getApplicationIcon(trace.sourceApplication))
            } catch (e: PackageManager.NameNotFoundException) {
                holder.appIcon.setImageResource(android.R.drawable.sym_def_app_icon)
            }
            holder.consumption.text =
                String.format("%.3f", trace.bytesSpent / (1024.0 * 1024.0)) + " MB"
            holder.url.text = trace.requestedUrl
            holder.appName.text =
                if (((trace.appName == Trace.UNKNOWN_APP_NAME))) trace.sourceApplication else trace.appName
            holder.appName.tag = trace.sourceApplication
            val date = Date(trace.datetime)
            holder.date.text = SimpleDateFormat("dd/MM/yyyy").format(date)
            holder.expandTrace.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    val tag = view.tag as String
                    if ((tag == "close")) {
                        holder.url.maxLines = Int.MAX_VALUE
                        (view as ImageButton).setImageDrawable(
                            ContextCompat.getDrawable(
                                (context)!!, R.drawable.ic_close_trace
                            )
                        )
                        view.setTag("open")
                    } else {
                        holder.url.maxLines = 1
                        (view as ImageButton).setImageDrawable(
                            ContextCompat.getDrawable(
                                (context)!!, R.drawable.ic_expand_trace
                            )
                        )
                        view.setTag("close")
                    }
                }
            })
        }

        override fun getItemCount(): Int {
            return mDataSet.size
        }
    }

    companion object {
        fun newInstance(): TracesListFragment {
            return TracesListFragment()
        }
    }
}