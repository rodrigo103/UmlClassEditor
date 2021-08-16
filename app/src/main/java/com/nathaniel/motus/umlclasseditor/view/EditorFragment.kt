package com.nathaniel.motus.umlclasseditor.view

import android.view.View.OnTouchListener
import com.nathaniel.motus.umlclasseditor.view.GraphFragment
import com.nathaniel.motus.umlclasseditor.model.UmlProject
import com.nathaniel.motus.umlclasseditor.view.GraphView.TouchMode
import com.nathaniel.motus.umlclasseditor.model.UmlClass
import com.nathaniel.motus.umlclasseditor.view.GraphView.GraphViewObserver
import android.graphics.Typeface
import android.graphics.DashPathEffect
import android.content.res.TypedArray
import com.nathaniel.motus.umlclasseditor.R
import com.nathaniel.motus.umlclasseditor.model.UmlRelation.UmlRelationType
import com.nathaniel.motus.umlclasseditor.model.UmlRelation
import com.nathaniel.motus.umlclasseditor.view.GraphView
import com.nathaniel.motus.umlclasseditor.model.UmlClass.UmlClassType
import com.nathaniel.motus.umlclasseditor.model.UmlClassAttribute
import com.nathaniel.motus.umlclasseditor.model.UmlClassMethod
import com.nathaniel.motus.umlclasseditor.model.UmlEnumValue
import android.view.MotionEvent
import android.content.DialogInterface
import android.widget.TextView
import android.widget.ImageButton
import com.nathaniel.motus.umlclasseditor.controller.FragmentObserver
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.nathaniel.motus.umlclasseditor.view.EditorFragment
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.RadioGroup
import android.widget.ExpandableListView.OnChildClickListener
import android.widget.EditText
import android.widget.RadioButton
import android.widget.ExpandableListView
import android.widget.LinearLayout
import com.nathaniel.motus.umlclasseditor.view.ClassEditorFragment
import com.nathaniel.motus.umlclasseditor.model.AdapterItem
import com.nathaniel.motus.umlclasseditor.model.AdapterItemComparator
import com.nathaniel.motus.umlclasseditor.model.AddItemString
import com.nathaniel.motus.umlclasseditor.controller.CustomExpandableListViewAdapter
import android.widget.AdapterView
import android.widget.Toast
import com.nathaniel.motus.umlclasseditor.model.UmlType
import com.nathaniel.motus.umlclasseditor.model.UmlType.TypeLevel
import android.widget.CheckBox
import android.widget.Spinner
import com.nathaniel.motus.umlclasseditor.view.MethodEditorFragment
import com.nathaniel.motus.umlclasseditor.model.TypeMultiplicity
import com.nathaniel.motus.umlclasseditor.model.TypeNameComparator
import android.widget.ArrayAdapter
import com.nathaniel.motus.umlclasseditor.model.MethodParameter
import com.nathaniel.motus.umlclasseditor.view.AttributeEditorFragment
import com.nathaniel.motus.umlclasseditor.view.ParameterEditorFragment
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException
import kotlin.jvm.JvmOverloads
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.FrameLayout
import androidx.core.view.MenuCompat
import androidx.annotation.RequiresApi
import android.os.Build
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.nathaniel.motus.umlclasseditor.controller.MainActivity
import androidx.core.view.GravityCompat
import android.content.Intent
import android.widget.AbsListView
import android.util.SparseBooleanArray
import android.text.Html
import android.app.Activity
import android.view.View
import android.widget.BaseExpandableListAdapter
import androidx.fragment.app.Fragment

abstract class EditorFragment : Fragment() {
    protected var mCallback: FragmentObserver? = null
    protected var mOnBackPressedCallback: OnBackPressedCallback? = null

    //    **********************************************************************************************
    //    Abstract methods
    //    **********************************************************************************************
    protected abstract fun readBundle()
    protected abstract fun clearDraftObject()
    protected abstract fun createOrUpdateObject(): Boolean
    protected abstract fun closeFragment()
    protected abstract fun configureViews()
    protected abstract fun initializeMembers()
    protected abstract fun initializeFields()
    protected abstract fun setOnCreateOrEditDisplay()

    //    **********************************************************************************************
    //    Common methods
    //    **********************************************************************************************
    protected fun createOnBackPressedCallback() {
        mOnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onCancelButtonCLicked()
            }
        }
    }

    protected fun onCancelButtonCLicked() {
        clearDraftObject()
        mOnBackPressedCallback!!.remove()
        closeFragment()
    }

    protected fun onOKButtonClicked() {
        if (createOrUpdateObject()) {
            mOnBackPressedCallback!!.remove()
            closeFragment()
        }
    }

    protected fun setOnBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(this, mOnBackPressedCallback!!)
    }

    protected fun createCallbackToParentActivity() {
        mCallback = activity as FragmentObserver?
    }

    //    **********************************************************************************************
    //    Fragment events
    //    **********************************************************************************************
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) readBundle()
        createOnBackPressedCallback()
        setOnBackPressedCallback()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createCallbackToParentActivity()
        configureViews()
        initializeMembers()
        initializeFields()
        setOnCreateOrEditDisplay()
    }
}