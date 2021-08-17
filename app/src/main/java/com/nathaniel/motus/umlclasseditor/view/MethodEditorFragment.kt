package com.nathaniel.motus.umlclasseditor.view

import com.nathaniel.motus.umlclasseditor.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ExpandableListView.OnChildClickListener
import com.nathaniel.motus.umlclasseditor.controller.CustomExpandableListViewAdapter
import android.app.AlertDialog
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.nathaniel.motus.umlclasseditor.model.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [MethodEditorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MethodEditorFragment  //    **********************************************************************************************
//    Constructors
//    **********************************************************************************************
    : EditorFragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener,
    OnItemLongClickListener, OnChildClickListener {
    private var mMethodOrder = 0
    private var mClassOrder = 0
    private var mUmlClassMethod: UmlClassMethod? = null
    private var mUmlClass: UmlClass? = null
    private var mClassEditorFragmentTag: String? = null
    private var mEditMethodText: TextView? = null
    private var mDeleteMethodButton: Button? = null
    private var mMethodNameEdit: EditText? = null
    private var mPublicRadio: RadioButton? = null
    private var mProtectedRadio: RadioButton? = null
    private var mPrivateRadio: RadioButton? = null
    private var mStaticCheck: CheckBox? = null
    private var mTypeSpinner: Spinner? = null
    private var mMethodMultiplicityRadioGroup: RadioGroup? = null
    private var mSingleRadio: RadioButton? = null
    private var mCollectionRadio: RadioButton? = null
    private var mArrayRadio: RadioButton? = null
    private var mDimText: TextView? = null
    private var mDimEdit: EditText? = null
    private var mParameterList: ExpandableListView? = null
    private var mCancelButton: Button? = null
    private var mOKButton: Button? = null

    //    **********************************************************************************************
    //    Fragment events
    //    **********************************************************************************************
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_method_editor, container, false)
    }

    //    **********************************************************************************************
    //    Configuration methods
    //    **********************************************************************************************
    override fun readBundle() {
        mClassEditorFragmentTag = arguments!!.getString(CLASS_EDITOR_FRAGMENT_TAG_KEY)
        mMethodOrder = arguments!!.getInt(METHOD_ORDER_KEY)
        mClassOrder = arguments!!.getInt(CLASS_ORDER_KEY)
    }

    override fun setOnCreateOrEditDisplay() {
        if (mMethodOrder != -1) setOnEditDisplay() else setOnCreateDisplay()
    }

    override fun configureViews() {
        mEditMethodText = activity!!.findViewById(R.id.edit_method_text)
        mDeleteMethodButton = activity!!.findViewById(R.id.delete_method_button)
        mDeleteMethodButton?.setOnClickListener(this)
        mDeleteMethodButton?.setTag(DELETE_METHOD_BUTTON_TAG)
        mMethodNameEdit = activity!!.findViewById(R.id.method_name_input)
        mPublicRadio = activity!!.findViewById(R.id.method_public_radio)
        mProtectedRadio = activity!!.findViewById(R.id.method_protected_radio)
        mPrivateRadio = activity!!.findViewById(R.id.method_private_radio)
        mStaticCheck = activity!!.findViewById(R.id.method_static_check)
        mTypeSpinner = activity!!.findViewById(R.id.method_type_spinner)
        mMethodMultiplicityRadioGroup =
            activity!!.findViewById(R.id.method_multiplicity_radio_group)
        mMethodMultiplicityRadioGroup?.setOnCheckedChangeListener(this)
        mSingleRadio = activity!!.findViewById(R.id.method_simple_radio)
        mCollectionRadio = activity!!.findViewById(R.id.method_collection_radio)
        mArrayRadio = activity!!.findViewById(R.id.method_array_radio)
        mDimText = activity!!.findViewById(R.id.method_dimension_text)
        mDimEdit = activity!!.findViewById(R.id.method_dimension_input)
        mParameterList = activity!!.findViewById(R.id.method_parameters_list)
        mParameterList?.setOnChildClickListener(this)
        mParameterList?.setOnItemLongClickListener(this)
        mCancelButton = activity!!.findViewById(R.id.method_cancel_button)
        mCancelButton?.setOnClickListener(this)
        mCancelButton?.setTag(CANCEL_BUTTON_TAG)
        mOKButton = activity!!.findViewById(R.id.method_ok_button)
        mOKButton?.setOnClickListener(this)
        mOKButton?.setTag(OK_BUTTON_TAG)
    }

    override fun initializeMembers() {
        mUmlClass = mCallback?.project?.findClassByOrder(mClassOrder)
        if (mMethodOrder != -1) {
            mUmlClassMethod = mUmlClass!!.findMethodByOrder(mMethodOrder)
        } else {
            mUmlClassMethod = UmlClassMethod(mUmlClass?.umlClassMethodCount!!)
            mUmlClass!!.addMethod(mUmlClassMethod)
        }
    }

    override fun initializeFields() {
        if (mMethodOrder != -1) {
            mMethodNameEdit?.setText(mUmlClassMethod?.name)
            when (mUmlClassMethod?.visibility) {
                Visibility.PUBLIC -> mPublicRadio!!.isChecked = true
                Visibility.PROTECTED -> mProtectedRadio!!.isChecked = true
                else -> mPrivateRadio!!.isChecked = true
            }
            mStaticCheck!!.isChecked = mUmlClassMethod!!.isStatic
            when (mUmlClassMethod?.typeMultiplicity) {
                TypeMultiplicity.SINGLE -> mSingleRadio!!.isChecked = true
                TypeMultiplicity.COLLECTION -> mCollectionRadio!!.isChecked = true
                else -> mArrayRadio!!.isChecked = true
            }
            mDimEdit!!.setText(Integer.toString(mUmlClassMethod?.arrayDimension!!))
            if (mUmlClassMethod?.typeMultiplicity == TypeMultiplicity.ARRAY) setOnArrayDisplay() else setOnSingleDisplay()
        } else {
            mMethodNameEdit!!.setText("")
            mPublicRadio!!.isChecked = true
            mStaticCheck!!.isChecked = false
            mSingleRadio!!.isChecked = true
            mDimEdit!!.setText("")
            setOnSingleDisplay()
        }
        populateTypeSpinner()
        populateParameterListView()
    }

    private fun populateTypeSpinner() {
        val spinnerArray = mutableListOf<String>()
        for (t in UmlType.Companion.umlTypes) spinnerArray.add(t?.name!!)
        Collections.sort(spinnerArray, TypeNameComparator())
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, spinnerArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mTypeSpinner!!.adapter = adapter
        if (mMethodOrder != -1) mTypeSpinner!!.setSelection(spinnerArray.indexOf(mUmlClassMethod?.umlType?.name)) else mTypeSpinner!!.setSelection(
            spinnerArray.indexOf("void")
        )
    }

    private fun populateParameterListView() {
        var parameterGroupIsExpanded = false
        if (mParameterList!!.expandableListAdapter != null && mParameterList!!.isGroupExpanded(0)) parameterGroupIsExpanded =
            true

        val parameterList = mutableListOf<AdapterItem>()
        for (p in mUmlClassMethod?.parameters!!) parameterList.add(p!!)
        Collections.sort(parameterList, AdapterItemComparator())
        parameterList.add(0, AddItemString(getString(R.string.new_parameter_string)))

        val hashMap = HashMap<String, List<AdapterItem>>()
        hashMap[getString(R.string.parameters_string)] = parameterList

        val title: MutableList<String> = ArrayList()
        title.add(getString(R.string.parameters_string))

        val adapter = CustomExpandableListViewAdapter(context, title, hashMap)
        mParameterList!!.setAdapter(adapter)

        if (parameterGroupIsExpanded) mParameterList!!.expandGroup(0)
    }

    private fun setOnEditDisplay() {
        mEditMethodText!!.text = "Edit method"
        mDeleteMethodButton!!.visibility = View.VISIBLE
    }

    private fun setOnCreateDisplay() {
        mEditMethodText!!.text = "Create method"
        mDeleteMethodButton!!.visibility = View.INVISIBLE
    }

    private fun setOnArrayDisplay() {
        mDimText!!.visibility = View.VISIBLE
        mDimEdit!!.visibility = View.VISIBLE
    }

    private fun setOnSingleDisplay() {
        mDimText!!.visibility = View.INVISIBLE
        mDimEdit!!.visibility = View.INVISIBLE
    }

    fun updateMethodEditorFragment(methodOrder: Int, classOrder: Int) {
        mMethodOrder = methodOrder
        mClassOrder = classOrder
        initializeMembers()
        initializeFields()
        if (mMethodOrder != -1) setOnEditDisplay() else setOnCreateDisplay()
        if (mMethodOrder != -1 && mUmlClassMethod?.typeMultiplicity == TypeMultiplicity.ARRAY) setOnArrayDisplay() else setOnSingleDisplay()
        setOnBackPressedCallback()
    }

    override fun closeFragment() {
        mCallback!!.closeMethodEditorFragment(this)
    }

    //    **********************************************************************************************
    //    UI events
    //    **********************************************************************************************
    override fun onClick(v: View) {
        val tag = v.tag as Int
        when (tag) {
            CANCEL_BUTTON_TAG -> onCancelButtonCLicked()
            OK_BUTTON_TAG -> onOKButtonClicked()
            DELETE_METHOD_BUTTON_TAG -> startDeleteMethodDialog()
            else -> {
            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        if (checkedId == R.id.method_array_radio) setOnArrayDisplay() else setOnSingleDisplay()
    }

    override fun onItemLongClick(
        adapterView: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
    ): Boolean {
        val expandableListView = view.parent as ExpandableListView
        val pos = expandableListView.getExpandableListPosition(position)
        val itemType = ExpandableListView.getPackedPositionType(pos)
        val groupPos = ExpandableListView.getPackedPositionGroup(pos)
        val childPos = ExpandableListView.getPackedPositionChild(pos)
        val item =
            expandableListView.expandableListAdapter.getChild(groupPos, childPos) as AdapterItem
        if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD && childPos != 0) startDeleteParameterDialog(
            (item as MethodParameter).parameterOrder
        )
        return true
    }

    override fun onChildClick(
        expandableListView: ExpandableListView,
        view: View,
        i: Int,
        i1: Int,
        l: Long
    ): Boolean {
        val item = expandableListView.expandableListAdapter.getChild(i, i1) as AdapterItem
        if (item.name == getString(R.string.new_parameter_string) && i1 == 0) mCallback!!.openParameterEditorFragment(
            -1,
            mUmlClassMethod?.methodOrder!!,
            mUmlClass?.classOrder!!
        ) else mCallback!!.openParameterEditorFragment(
            (item as MethodParameter).parameterOrder,
            mUmlClassMethod?.methodOrder!!, mUmlClass?.classOrder!!
        )
        return true
    }

    //    **********************************************************************************************
    //    Edition methods
    //    **********************************************************************************************
    override fun clearDraftObject() {
        if (mMethodOrder == -1) mUmlClass!!.removeMethod(mUmlClassMethod)
    }

    override fun createOrUpdateObject(): Boolean {
        return createOrUpdateMethod()
    }

    private fun createOrUpdateMethod(): Boolean {
        if (methodName == "") {
            Toast.makeText(context, "Method name cannot be blank", Toast.LENGTH_SHORT).show()
            return false
        } else {
            mUmlClassMethod?.name = (methodName)
            mUmlClassMethod?.visibility = (methodVisibility)
            mUmlClassMethod?.isStatic = (isStatic)
            mUmlClassMethod?.umlType = (methodType)
            mUmlClassMethod?.typeMultiplicity = (methodMultiplicity)
            mUmlClassMethod?.arrayDimension = (arrayDimension)
        }
        return if (mUmlClass!!.containsEquivalentMethodTo(mUmlClassMethod)) {
            Toast.makeText(context, "This method is already defined", Toast.LENGTH_SHORT).show()
            false
        } else true
    }

    private val methodName: String
        private get() = mMethodNameEdit!!.text.toString()
    private val methodVisibility: Visibility
        private get() {
            if (mPublicRadio!!.isChecked) return Visibility.PUBLIC
            return if (mPrivateRadio!!.isChecked) Visibility.PROTECTED else Visibility.PRIVATE
        }
    private val isStatic: Boolean
        private get() = mStaticCheck!!.isChecked
    private val methodType: UmlType?
        private get() = UmlType.Companion.valueOf(
            mTypeSpinner!!.selectedItem.toString(),
            UmlType.Companion.umlTypes
        )
    private val methodMultiplicity: TypeMultiplicity
        private get() {
            if (mSingleRadio!!.isChecked) return TypeMultiplicity.SINGLE
            return if (mCollectionRadio!!.isChecked) TypeMultiplicity.COLLECTION else TypeMultiplicity.ARRAY
        }
    private val arrayDimension: Int
        private get() = if (mDimEdit!!.text.toString() == "") 0 else mDimEdit!!.text.toString()
            .toInt()

    fun updateLists() {
        populateParameterListView()
    }

    //    **********************************************************************************************
    //    Alert dialogs
    //    **********************************************************************************************
    private fun startDeleteMethodDialog() {
        val fragment: Fragment = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete method ?")
            .setMessage("Are you sure you want to delete this method ?")
            .setNegativeButton("NO") { dialog, which -> }
            .setPositiveButton("YES") { dialog, which ->
                mUmlClass!!.removeMethod(mUmlClassMethod)
                mCallback!!.closeMethodEditorFragment(fragment)
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun startDeleteParameterDialog(parameterIndex: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete parameter ?")
            .setMessage("Are you sure you want delete this parameter ?")
            .setNegativeButton("NO") { dialogInterface, i -> }
            .setPositiveButton("YES") { dialogInterface, i ->
                mUmlClassMethod!!.removeParameter(
                    mUmlClassMethod!!.findParameterByOrder(
                        parameterIndex
                    )
                )
                updateLists()
            }
            .create()
            .show()
    }

    companion object {
        private const val METHOD_ORDER_KEY = "methodOrder"
        private const val CLASS_ORDER_KEY = "classOrder"
        private const val CLASS_EDITOR_FRAGMENT_TAG_KEY = "classEditorFragmentTag"
        private const val DELETE_METHOD_BUTTON_TAG = 410
        private const val CANCEL_BUTTON_TAG = 420
        private const val OK_BUTTON_TAG = 430
        private const val ADD_PARAMETER_BUTTON_TAG = 440
        fun newInstance(
            classEditorFragmentTag: String?,
            methodOrder: Int,
            classOrder: Int
        ): MethodEditorFragment {
            val fragment = MethodEditorFragment()
            val args = Bundle()
            args.putString(CLASS_EDITOR_FRAGMENT_TAG_KEY, classEditorFragmentTag)
            args.putInt(METHOD_ORDER_KEY, methodOrder)
            args.putInt(CLASS_ORDER_KEY, classOrder)
            fragment.arguments = args
            return fragment
        }
    }
}