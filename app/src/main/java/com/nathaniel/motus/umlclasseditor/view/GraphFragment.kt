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
import com.nathaniel.motus.umlclasseditor.controller.FragmentObserver
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.nathaniel.motus.umlclasseditor.view.EditorFragment
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ExpandableListView.OnChildClickListener
import com.nathaniel.motus.umlclasseditor.view.ClassEditorFragment
import com.nathaniel.motus.umlclasseditor.model.AdapterItem
import com.nathaniel.motus.umlclasseditor.model.AdapterItemComparator
import com.nathaniel.motus.umlclasseditor.model.AddItemString
import com.nathaniel.motus.umlclasseditor.controller.CustomExpandableListViewAdapter
import com.nathaniel.motus.umlclasseditor.model.UmlType
import com.nathaniel.motus.umlclasseditor.model.UmlType.TypeLevel
import com.nathaniel.motus.umlclasseditor.view.MethodEditorFragment
import com.nathaniel.motus.umlclasseditor.model.TypeMultiplicity
import com.nathaniel.motus.umlclasseditor.model.TypeNameComparator
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
import androidx.core.view.MenuCompat
import androidx.annotation.RequiresApi
import android.os.Build
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.nathaniel.motus.umlclasseditor.controller.MainActivity
import androidx.core.view.GravityCompat
import android.content.Intent
import android.util.SparseBooleanArray
import android.text.Html
import android.app.Activity
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment

class GraphFragment  //    **********************************************************************************************
//    Constructors
//    **********************************************************************************************
    : Fragment(), View.OnClickListener {
    private var mGraphView: GraphView? = null
    private var mGraphText: TextView? = null
    private var mInheritanceButton: ImageButton? = null
    private var mRealizationButton: ImageButton? = null
    private var mAggregationButton: ImageButton? = null
    private var mEscapeButton: Button? = null
    private var mAssociationButton: ImageButton? = null
    private var mDependancyButton: ImageButton? = null
    private var mCompositionButton: ImageButton? = null
    private var mNewClassButton: Button? = null
    var isExpectingTouchLocation = false
    var isExpectingStartClass = false
    var isExpectingEndClass = false
    var startClass: UmlClass? = null
    var endClass: UmlClass? = null
    var umlRelationType: UmlRelationType? = null

    //    **********************************************************************************************
    //    Getters and setters
    //    **********************************************************************************************
    var callBack: FragmentObserver? = null
        private set

    //    **********************************************************************************************
    //    Fragment events
    //    **********************************************************************************************
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_graph, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createCallbackToParentActivity()
        configureViews()
    }

    //    **********************************************************************************************
    //    Setup methods
    //    **********************************************************************************************
    private fun configureViews() {
        mGraphView = activity!!.findViewById(R.id.graphview)
        mGraphView.setTag(GRAPHVIEW_TAG)
        mGraphView.setGraphFragment(this)
        mGraphText = activity!!.findViewById(R.id.graph_text)
        mInheritanceButton = activity!!.findViewById(R.id.inheritance_button)
        mInheritanceButton.setTag(INHERITANCE_BUTTON_TAG)
        mInheritanceButton.setOnClickListener(this)
        mRealizationButton = activity!!.findViewById(R.id.realization_button)
        mRealizationButton.setTag(REALIZATION_BUTTON_TAG)
        mRealizationButton.setOnClickListener(this)
        mAggregationButton = activity!!.findViewById(R.id.aggregation_button)
        mAggregationButton.setTag(AGGREGATION_BUTTON_TAG)
        mAggregationButton.setOnClickListener(this)
        mEscapeButton = activity!!.findViewById(R.id.escape_button)
        mEscapeButton.setTag(ESCAPE_BUTTON_TAG)
        mEscapeButton.setOnClickListener(this)
        mAssociationButton = activity!!.findViewById(R.id.association_button)
        mAssociationButton.setTag(ASSOCIATION_BUTTON_TAG)
        mAssociationButton.setOnClickListener(this)
        mDependancyButton = activity!!.findViewById(R.id.dependency_button)
        mDependancyButton.setTag(DEPENDENCY_BUTTON_TAG)
        mDependancyButton.setOnClickListener(this)
        mCompositionButton = activity!!.findViewById(R.id.composition_button)
        mCompositionButton.setTag(COMPOSITION_BUTTON_TAG)
        mCompositionButton.setOnClickListener(this)
        mNewClassButton = activity!!.findViewById(R.id.new_class_button)
        mNewClassButton.setTag(NEW_CLASS_BUTTON_TAG)
        mNewClassButton.setOnClickListener(this)
    }

    private fun createCallbackToParentActivity() {
        callBack = activity as FragmentObserver?
    }

    //    **********************************************************************************************
    //    Modifiers
    //    **********************************************************************************************
    fun setPrompt(prompt: String?) {
        mGraphText!!.text = prompt
    }

    fun clearPrompt() {
        mGraphText!!.text = ""
    }

    //    **********************************************************************************************
    //    Listener methods
    //    **********************************************************************************************
    override fun onClick(v: View) {
        val tag = v.tag as Int
        when (tag) {
            NEW_CLASS_BUTTON_TAG -> {
                isExpectingTouchLocation = true
                setPrompt("Locate the new class")
            }
            INHERITANCE_BUTTON_TAG -> startRelation(UmlRelationType.INHERITANCE)
            REALIZATION_BUTTON_TAG -> startRelation(UmlRelationType.REALIZATION)
            AGGREGATION_BUTTON_TAG -> startRelation(UmlRelationType.AGGREGATION)
            ASSOCIATION_BUTTON_TAG -> startRelation(UmlRelationType.ASSOCIATION)
            COMPOSITION_BUTTON_TAG -> startRelation(UmlRelationType.COMPOSITION)
            DEPENDENCY_BUTTON_TAG -> startRelation(UmlRelationType.DEPENDENCY)
            ESCAPE_BUTTON_TAG -> clearInput()
            else -> {
            }
        }
    }

    private fun startRelation(relationType: UmlRelationType) {
        isExpectingStartClass = true
        isExpectingEndClass = false
        umlRelationType = relationType
        setPrompt("Choose start class")
    }

    private fun clearInput() {
        isExpectingEndClass = false
        isExpectingStartClass = false
        isExpectingTouchLocation = false
        clearPrompt()
    }

    companion object {
        const val GRAPHVIEW_TAG = 110
        const val NEW_CLASS_BUTTON_TAG = 120
        const val INHERITANCE_BUTTON_TAG = 130
        const val REALIZATION_BUTTON_TAG = 140
        const val AGGREGATION_BUTTON_TAG = 150
        const val ESCAPE_BUTTON_TAG = 160
        const val ASSOCIATION_BUTTON_TAG = 170
        const val DEPENDENCY_BUTTON_TAG = 180
        const val COMPOSITION_BUTTON_TAG = 190
        fun newInstance(): GraphFragment {
            val fragment = GraphFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}