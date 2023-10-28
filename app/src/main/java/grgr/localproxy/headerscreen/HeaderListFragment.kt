package grgr.localproxy.headerscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import grgr.localproxy.R
import grgr.localproxy.proxydata.header.Header

/**
 * Created by daniel on 30/08/18.
 */
class HeaderListFragment : Fragment(), HeaderListContract.View {
    private var mPresenter: HeaderListContract.Presenter? = null
    private var mListAdapter: HeaderListAdapter? = null
    private var mNoHeadersView: View? = null
    private var mHeadersView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mListAdapter = HeaderListAdapter(ArrayList(0))
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
        val root = inflater.inflate(R.layout.header_list_frag, container, false)

        //set up headers view
        val listView = root.findViewById<ListView>(R.id.headers_list_view)
        listView.adapter = mListAdapter
        mHeadersView = root.findViewById(R.id.headersLL)

        //set up no headers view
        mNoHeadersView = root.findViewById(R.id.noHeaders)
        val noHeadersTv = root.findViewById<TextView>(R.id.noHeadersAdd)
        noHeadersTv.setOnClickListener { mPresenter!!.addNewHeader() }

        //Set up floating action button
        val fab = activity!!
            .findViewById<FloatingActionButton>(R.id.fab_add_header)
        fab.setOnClickListener { mPresenter!!.addNewHeader() }
        setHasOptionsMenu(true)
        return root
    }

    override fun setPresenter(presenter: HeaderListContract.Presenter?) {
        mPresenter = presenter
    }

    override fun showHeaders(headers: List<Header>) {
        mListAdapter!!.replaceData(headers)
        mHeadersView!!.visibility = View.VISIBLE
        mNoHeadersView!!.visibility = View.GONE
    }

    override fun showAddEditHeaderDialog(header: Header?) {
        val alertDialog = AlertDialog.Builder(
            context!!
        )
        alertDialog.setTitle(if (header == null) getString(R.string.new_h) else getString(R.string.edit_h))
        val root = LayoutInflater.from(context).inflate(R.layout.addeditheader_dialog, null, false)
        val hName = root.findViewById<EditText>(R.id.header_name)
        val hValue = root.findViewById<EditText>(R.id.header_value)
        if (header != null) {
            hName.setText(header.name)
            hValue.setText(header.value)
        }
        alertDialog.setView(root)
        alertDialog.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            val h: Header
            h = if (header == null) {
                Header.newHeader(hName.text.toString(), hValue.text.toString())
            } else {
                Header.newHeader(header.id, hName.text.toString(), hValue.text.toString())
            }
            mPresenter!!.saveHeader(h)
            dialog.dismiss()
        }
        alertDialog.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
        alertDialog.show()
    }

    override fun showNoHeaders() {
        mHeadersView!!.visibility = View.GONE
        mNoHeadersView!!.visibility = View.VISIBLE
    }

    override fun showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_header_message))
    }

    override fun showHeaderNameAlreadyExistError() {
        val alertDialog = AlertDialog.Builder(
            context!!
        ).create()
        alertDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON)
        alertDialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_error)
        alertDialog.setTitle(getString(R.string.error_title))
        alertDialog.setMessage(getString(R.string.header_name_already_exist_message))
        alertDialog.show()
    }

    override fun showHeaderRemovedMessage() {
        showMessage(getString(R.string.header_removed_message))
    }

    override val isActive: Boolean
        get() = isAdded

    private fun showMessage(message: String) {
        Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()
    }

    private inner class HeaderListAdapter(private var mHeaderList: List<Header>) : BaseAdapter() {
        private fun setList(headerList: List<Header>) {
            mHeaderList = headerList
        }

        fun replaceData(headerList: List<Header>) {
            setList(headerList)
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return mHeaderList.size
        }

        override fun getItem(position: Int): Header {
            return mHeaderList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        inner class ViewHolder {
            var headerTextLL: LinearLayout? = null
            var headerText: TextView? = null
            var removeButton: Button? = null
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            var rowView = convertView
            if (rowView == null) {
                val inflater = LayoutInflater.from(parent.context)
                rowView = inflater.inflate(R.layout.header_list_item, parent, false)
                val viewHolder: ViewHolder = ViewHolder()
                viewHolder.headerText = rowView.findViewById(R.id.headerText)
                viewHolder.removeButton = rowView.findViewById(R.id.removeHeaderBtn)
                viewHolder.headerTextLL = rowView.findViewById(R.id.headerTextLL)
                rowView.tag = viewHolder
            }
            val viewHolder = rowView.tag as ViewHolder
            val header = getItem(position)
            viewHolder.headerText!!.text = header.toString()
            viewHolder.removeButton!!.setOnClickListener { mPresenter!!.removeHeader(header.id) }
            viewHolder.headerTextLL!!.setOnClickListener { mPresenter!!.editHeader(header) }
            return rowView
        }
    }

    companion object {
        fun newInstance(): HeaderListFragment {
            return HeaderListFragment()
        }
    }
}