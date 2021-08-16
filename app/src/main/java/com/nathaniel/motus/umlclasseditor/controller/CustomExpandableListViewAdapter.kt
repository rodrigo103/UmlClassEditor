package com.nathaniel.motus.umlclasseditor.controller

import com.nathaniel.motus.umlclasseditor.R
import android.widget.TextView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.nathaniel.motus.umlclasseditor.model.AdapterItem
import android.widget.AbsListView
import android.util.SparseBooleanArray
import android.text.Html
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.BaseExpandableListAdapter
import androidx.fragment.app.Fragment
import java.util.HashMap

class CustomExpandableListViewAdapter(
    private val mContext: Context?,
    private val mGroups: List<String>,
    private val mChildren: HashMap<String, List<AdapterItem>>
) : BaseExpandableListAdapter() {
    private val mFragment: Fragment? = null
    override fun getGroupCount(): Int {
        return mGroups.size
    }

    override fun getChildrenCount(i: Int): Int {
        return mChildren[mGroups[i]]!!.size
    }

    override fun getGroup(i: Int): Any {
        return mGroups[i]
    }

    override fun getChild(i: Int, i1: Int): Any {
        return mChildren[mGroups[i]]!![i1]
    }

    override fun getGroupId(i: Int): Long {
        return i.toLong()
    }

    override fun getChildId(i: Int, i1: Int): Long {
        return i1.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(i: Int, b: Boolean, view: View?, viewGroup: ViewGroup): View? {
        var view = view
        val title = mGroups[i]
        if (view == null) {
            val layoutInflater =
                mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.list_group, null)
        }
        val textViewGroup = view?.findViewById<TextView>(R.id.group_text)
        textViewGroup?.text = title
        return view
    }

    override fun getChildView(i: Int, i1: Int, b: Boolean, view: View?, viewGroup: ViewGroup): View? {
        var view = view
        val item = (getChild(i, i1) as AdapterItem).name
        if (view == null) {
            val layoutInflater =
                mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.list_item, null)
        }
        val childText = view?.findViewById<TextView>(R.id.child_text)
        childText?.text = item
        return view
    }

    override fun isChildSelectable(i: Int, i1: Int): Boolean {
        return true
    }
}