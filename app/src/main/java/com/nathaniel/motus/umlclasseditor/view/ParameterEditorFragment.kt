package com.nathaniel.motus.umlclasseditor.view

//import android.view.View.OnTouchListener
//import com.nathaniel.motus.umlclasseditor.view.GraphFragment
//import com.nathaniel.motus.umlclasseditor.model.UmlProject
//import com.nathaniel.motus.umlclasseditor.view.GraphView.TouchMode
import com.nathaniel.motus.umlclasseditor.model.UmlClass
//import com.nathaniel.motus.umlclasseditor.view.GraphView.GraphViewObserver
//import android.graphics.Typeface
//import android.graphics.DashPathEffect
//import android.content.res.TypedArray
import com.nathaniel.motus.umlclasseditor.R
//import com.nathaniel.motus.umlclasseditor.model.UmlRelation.UmlRelationType
//import com.nathaniel.motus.umlclasseditor.model.UmlRelation
//import com.nathaniel.motus.umlclasseditor.view.GraphView
//import com.nathaniel.motus.umlclasseditor.model.UmlClass.UmlClassType
//import com.nathaniel.motus.umlclasseditor.model.UmlClassAttribute
import com.nathaniel.motus.umlclasseditor.model.UmlClassMethod
//import com.nathaniel.motus.umlclasseditor.model.UmlEnumValue
//import android.view.MotionEvent
//import android.content.DialogInterface
//import com.nathaniel.motus.umlclasseditor.controller.FragmentObserver
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
//import androidx.activity.OnBackPressedCallback
//import com.nathaniel.motus.umlclasseditor.view.EditorFragment
//import android.widget.AdapterView.OnItemLongClickListener
//import android.widget.ExpandableListView.OnChildClickListener
//import com.nathaniel.motus.umlclasseditor.view.ClassEditorFragment
//import com.nathaniel.motus.umlclasseditor.model.AdapterItem
//import com.nathaniel.motus.umlclasseditor.model.AdapterItemComparator
//import com.nathaniel.motus.umlclasseditor.model.AddItemString
//import com.nathaniel.motus.umlclasseditor.controller.CustomExpandableListViewAdapter
import com.nathaniel.motus.umlclasseditor.model.UmlType
//import com.nathaniel.motus.umlclasseditor.model.UmlType.TypeLevel
//import com.nathaniel.motus.umlclasseditor.view.MethodEditorFragment
import com.nathaniel.motus.umlclasseditor.model.TypeMultiplicity
import com.nathaniel.motus.umlclasseditor.model.TypeNameComparator
import com.nathaniel.motus.umlclasseditor.model.MethodParameter
//import com.nathaniel.motus.umlclasseditor.view.AttributeEditorFragment
//import com.nathaniel.motus.umlclasseditor.view.ParameterEditorFragment
//import org.json.JSONArray
//import org.json.JSONObject
//import org.json.JSONException
//import kotlin.jvm.JvmOverloads
//import android.content.pm.PackageManager
//import android.content.pm.PackageInfo
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.material.navigation.NavigationView
//import androidx.drawerlayout.widget.DrawerLayout
//import androidx.core.view.MenuCompat
//import androidx.annotation.RequiresApi
//import android.os.Build
//import android.content.SharedPreferences
//import android.content.SharedPreferences.Editor
//import com.nathaniel.motus.umlclasseditor.controller.MainActivity
//import androidx.core.view.GravityCompat
//import android.content.Intent
//import android.util.SparseBooleanArray
//import android.text.Html
//import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [ParameterEditorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ParameterEditorFragment  //    **********************************************************************************************
//    Constructors
//    **********************************************************************************************
    : EditorFragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private var mParameterOrder = 0
    private var mMethodOrder = 0
    private var mClassOrder = 0
    private var mMethodEditorFragmentTag: String? = null
    private var mMethodParameter: MethodParameter? = null
    private var mUmlClassMethod: UmlClassMethod? = null
    private var mUmlClass: UmlClass? = null
    private var mEditParameterText: TextView? = null
    private var mDeleteParameterButton: Button? = null
    private var mParameterNameEdit: EditText? = null
    private var mParameterTypeSpinner: Spinner? = null
    private var mParameterMultiplicityRadioGroup: RadioGroup? = null
    private var mSingleRadio: RadioButton? = null
    private var mCollectionRadio: RadioButton? = null
    private var mArrayRadio: RadioButton? = null
    private var mDimText: TextView? = null
    private var mDimEdit: EditText? = null
    private var mCancelButton: Button? = null
    private var mOKButton: Button? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_parameter_editor, container, false)
    }

    //    **********************************************************************************************
    //    UI events
    //    **********************************************************************************************
    override fun onClick(v: View) {
        val tag = v.tag as Int
        when (tag) {
            CANCEL_BUTTON_TAG -> onCancelButtonClicked()
            OK_BUTTON_TAG -> onOKButtonClicked()
            DELETE_PARAMETER_BUTTON_TAG -> startDeleteParameterDialog()
        }
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        if (checkedId == R.id.parameter_array_radio) setOnArrayDisplay() else setOnSingleDisplay()
    }

    private fun onCancelButtonClicked() {
        if (mParameterOrder == -1) mUmlClassMethod!!.removeParameter(mMethodParameter)
        mOnBackPressedCallback!!.remove()
        mCallback!!.closeParameterEditorFragment(this)
    }

    //    **********************************************************************************************
    //    Configuration methods
    //    **********************************************************************************************
    override fun setOnCreateOrEditDisplay() {
        if (mParameterOrder == -1) setOnCreateDisplay() else setOnEditDisplay()
    }

    override fun readBundle() {
        mMethodEditorFragmentTag = arguments!!.getString(METHOD_EDITOR_FRAGMENT_TAG_KEY)
        mParameterOrder = arguments!!.getInt(PARAMETER_ORDER_KEY)
        mClassOrder = arguments!!.getInt(CLASS_ORDER_KEY)
        mMethodOrder = arguments!!.getInt(METHOD_ORDER_KEY)
    }

    override fun initializeMembers() {
        mUmlClass = mCallback?.project?.findClassByOrder(mClassOrder)
        mUmlClassMethod = mUmlClass!!.findMethodByOrder(mMethodOrder)
        if (mParameterOrder != -1) {
            mMethodParameter = mUmlClassMethod!!.findParameterByOrder(mParameterOrder)
        } else {
            mMethodParameter = MethodParameter(mUmlClassMethod?.parameterCount!!)
            mUmlClassMethod!!.addParameter(mMethodParameter)
        }
    }

    override fun configureViews() {
        mEditParameterText = activity!!.findViewById(R.id.edit_parameter_text)
        mDeleteParameterButton = activity!!.findViewById(R.id.delete_parameter_button)
        mDeleteParameterButton?.setTag(DELETE_PARAMETER_BUTTON_TAG)
        mDeleteParameterButton?.setOnClickListener(this)
        mParameterNameEdit = activity!!.findViewById(R.id.parameter_name_input)
        mParameterTypeSpinner = activity!!.findViewById(R.id.parameter_type_spinner)
        mParameterMultiplicityRadioGroup =
            activity!!.findViewById(R.id.parameter_multiplicity_radio_group)
        mParameterMultiplicityRadioGroup?.setOnCheckedChangeListener(this)
        mSingleRadio = activity!!.findViewById(R.id.parameter_simple_radio)
        mCollectionRadio = activity!!.findViewById(R.id.parameter_collection_radio)
        mArrayRadio = activity!!.findViewById(R.id.parameter_array_radio)
        mDimText = activity!!.findViewById(R.id.parameter_dimension_text)
        mDimEdit = activity!!.findViewById(R.id.parameter_dimension_input)
        mCancelButton = activity!!.findViewById(R.id.parameter_cancel_button)
        mCancelButton?.setTag(CANCEL_BUTTON_TAG)
        mCancelButton?.setOnClickListener(this)
        mOKButton = activity!!.findViewById(R.id.parameter_ok_button)
        mOKButton?.setTag(OK_BUTTON_TAG)
        mOKButton?.setOnClickListener(this)
    }

    override fun initializeFields() {
        if (mParameterOrder != -1) {
            mParameterNameEdit?.setText(mMethodParameter?.name)
            if (mMethodParameter?.typeMultiplicity == TypeMultiplicity.SINGLE) mSingleRadio!!.isChecked =
                true
            if (mMethodParameter?.typeMultiplicity == TypeMultiplicity.COLLECTION) mCollectionRadio!!.isChecked =
                true
            if (mMethodParameter?.typeMultiplicity == TypeMultiplicity.ARRAY) mArrayRadio!!.isChecked =
                true
            mDimEdit!!.setText(Integer.toString(mMethodParameter?.arrayDimension!!))
            if (mMethodParameter?.typeMultiplicity == TypeMultiplicity.ARRAY) setOnArrayDisplay() else setOnSingleDisplay()
        } else {
            mParameterNameEdit!!.setText("")
            mSingleRadio!!.isChecked = true
            mDimEdit!!.setText("")
            setOnSingleDisplay()
        }
        populateTypeSpinner()
    }

    private fun populateTypeSpinner() {
        val arrayList = mutableListOf<String>()
        for (t in UmlType.Companion.umlTypes) if (t?.name != "void") arrayList.add(t?.name!!)
        Collections.sort(arrayList, TypeNameComparator())
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, arrayList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mParameterTypeSpinner!!.adapter = adapter
        if (mParameterOrder != -1) mParameterTypeSpinner!!.setSelection(
            arrayList.indexOf(
                mMethodParameter?.umlType?.name
            )
        )
    }

    private fun setOnEditDisplay() {
        mEditParameterText!!.text = "Edit parameter"
        mDeleteParameterButton!!.visibility = View.VISIBLE
    }

    private fun setOnCreateDisplay() {
        mEditParameterText!!.text = "Create parameter"
        mDeleteParameterButton!!.visibility = View.INVISIBLE
    }

    private fun setOnSingleDisplay() {
        mDimText!!.visibility = View.INVISIBLE
        mDimEdit!!.visibility = View.INVISIBLE
    }

    private fun setOnArrayDisplay() {
        mDimText!!.visibility = View.VISIBLE
        mDimEdit!!.visibility = View.VISIBLE
    }

    fun updateParameterEditorFragment(parameterOrder: Int, methodOrder: Int, classOrder: Int) {
        mParameterOrder = parameterOrder
        mMethodOrder = methodOrder
        mClassOrder = classOrder
        initializeMembers()
        initializeFields()
        if (mParameterOrder == -1) setOnCreateDisplay() else setOnEditDisplay()
        if (mParameterOrder != -1 && mMethodParameter?.typeMultiplicity == TypeMultiplicity.ARRAY) setOnArrayDisplay() else setOnSingleDisplay()
        setOnBackPressedCallback()
    }

    override fun closeFragment() {
        mCallback!!.closeParameterEditorFragment(this)
    }

    //    **********************************************************************************************
    //    Edition methods
    //    **********************************************************************************************
    override fun clearDraftObject() {
        if (mParameterOrder == -1) mUmlClassMethod!!.removeParameter(mMethodParameter)
    }

    override fun createOrUpdateObject(): Boolean {
        return createOrUpdateParameter()
    }

    private fun createOrUpdateParameter(): Boolean {
        return if (parameterName == "") {
            Toast.makeText(context, "Parameter cannot be blank", Toast.LENGTH_SHORT).show()
            false
        } else if (mUmlClassMethod!!.containsParameterNamed(parameterName) &&
            mUmlClassMethod!!.getParameter(parameterName)?.parameterOrder != mParameterOrder
        ) {
            Toast.makeText(context, "This named is already used", Toast.LENGTH_SHORT).show()
            false
        } else {
            mMethodParameter?.name = (parameterName)
            mMethodParameter?.umlType = (parameterType)
            mMethodParameter?.typeMultiplicity = (parameterMultiplicity)
            mMethodParameter?.arrayDimension = (arrayDimension)
            true
        }
    }

    private val parameterName: String
        private get() = mParameterNameEdit!!.text.toString()
    private val parameterType: UmlType?
        private get() = UmlType.Companion.valueOf(
            mParameterTypeSpinner!!.selectedItem.toString(),
            UmlType.Companion.umlTypes
        )
    private val parameterMultiplicity: TypeMultiplicity
        private get() {
            if (mSingleRadio!!.isChecked) return TypeMultiplicity.SINGLE
            return if (mCollectionRadio!!.isChecked) TypeMultiplicity.COLLECTION else TypeMultiplicity.ARRAY
        }
    private val arrayDimension: Int
        private get() = if (mDimEdit!!.text.toString() == "") 0 else mDimEdit!!.text.toString()
            .toInt()

    //    **********************************************************************************************
    //    Alert dialogs
    //    **********************************************************************************************
    private fun startDeleteParameterDialog() {
        val fragment: Fragment = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Parameter")
            .setMessage("Are you sure you to delete this parameter ?")
            .setNegativeButton("NO") { dialog, which -> }
            .setPositiveButton("YES") { dialog, which ->
                mUmlClassMethod!!.removeParameter(mMethodParameter)
                mCallback!!.closeParameterEditorFragment(fragment)
            }
        val dialog = builder.create()
        dialog.show()
    }

    companion object {
        private const val PARAMETER_ORDER_KEY = "parameterOrder"
        private const val METHOD_ORDER_KEY = "methodOrder"
        private const val CLASS_ORDER_KEY = "classOrder"
        private const val METHOD_EDITOR_FRAGMENT_TAG_KEY = "methodEditorFragmentTag"
        private const val DELETE_PARAMETER_BUTTON_TAG = 510
        private const val CANCEL_BUTTON_TAG = 520
        private const val OK_BUTTON_TAG = 530
        fun newInstance(
            methodEditorFragmentTag: String?,
            parameterOrder: Int,
            methodOrder: Int,
            classOrder: Int
        ): ParameterEditorFragment {
            val fragment = ParameterEditorFragment()
            val args = Bundle()
            args.putString(METHOD_EDITOR_FRAGMENT_TAG_KEY, methodEditorFragmentTag)
            args.putInt(PARAMETER_ORDER_KEY, parameterOrder)
            args.putInt(METHOD_ORDER_KEY, methodOrder)
            args.putInt(CLASS_ORDER_KEY, classOrder)
            fragment.arguments = args
            return fragment
        }
    }
}