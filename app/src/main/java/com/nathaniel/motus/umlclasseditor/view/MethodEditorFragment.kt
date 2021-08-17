package com.nathaniel.motus.umlclasseditor.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ExpandableListView.OnChildClickListener
import androidx.fragment.app.Fragment
import com.nathaniel.motus.umlclasseditor.R
import com.nathaniel.motus.umlclasseditor.controller.CustomExpandableListViewAdapter
import com.nathaniel.motus.umlclasseditor.databinding.FragmentMethodEditorBinding
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

    private var _binding: FragmentMethodEditorBinding? = null
    private val binding get() = _binding!!

    private var mMethodOrder = 0
    private var mClassOrder = 0
    private var mUmlClassMethod: UmlClassMethod? = null
    private var mUmlClass: UmlClass? = null
    private var mClassEditorFragmentTag: String? = null

    //    **********************************************************************************************
    //    Fragment events
    //    **********************************************************************************************
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMethodEditorBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        binding.deleteMethodButton.setOnClickListener(this)
        binding.deleteMethodButton.tag = DELETE_METHOD_BUTTON_TAG
        binding.methodMultiplicityRadioGroup.setOnCheckedChangeListener(this)
        binding.methodParametersList.setOnChildClickListener(this)
        binding.methodParametersList.onItemLongClickListener = this
        binding.methodCancelButton.setOnClickListener(this)
        binding.methodCancelButton.tag = CANCEL_BUTTON_TAG
        binding.methodOkButton.setOnClickListener(this)
        binding.methodOkButton.tag = OK_BUTTON_TAG
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
            binding.methodNameInput.setText(mUmlClassMethod?.name)
            when (mUmlClassMethod?.visibility) {
                Visibility.PUBLIC -> binding.methodPublicRadio.isChecked = true
                Visibility.PROTECTED -> binding.methodProtectedRadio.isChecked = true
                else -> binding.methodPrivateRadio.isChecked = true
            }
            binding.methodStaticCheck.isChecked = mUmlClassMethod!!.isStatic
            when (mUmlClassMethod?.typeMultiplicity) {
                TypeMultiplicity.SINGLE -> binding.methodSimpleRadio.isChecked = true
                TypeMultiplicity.COLLECTION -> binding.methodCollectionRadio.isChecked = true
                else -> binding.methodArrayRadio.isChecked = true
            }
            binding.methodDimensionInput.setText(Integer.toString(mUmlClassMethod?.arrayDimension!!))
            if (mUmlClassMethod?.typeMultiplicity == TypeMultiplicity.ARRAY) setOnArrayDisplay() else setOnSingleDisplay()
        } else {
            binding.methodNameInput.setText("")
            binding.methodPublicRadio.isChecked = true
            binding.methodStaticCheck.isChecked = false
            binding.methodSimpleRadio.isChecked = true
            binding.methodDimensionInput.setText("")
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
        binding.methodTypeSpinner.adapter = adapter
        if (mMethodOrder != -1) binding.methodTypeSpinner.setSelection(spinnerArray.indexOf(mUmlClassMethod?.umlType?.name)) else binding.methodTypeSpinner!!.setSelection(
            spinnerArray.indexOf("void")
        )
    }

    private fun populateParameterListView() {
        var parameterGroupIsExpanded = false
        if (binding.methodParametersList.expandableListAdapter != null && binding.methodParametersList.isGroupExpanded(0)) parameterGroupIsExpanded =
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
        binding.methodParametersList.setAdapter(adapter)

        if (parameterGroupIsExpanded) binding.methodParametersList.expandGroup(0)
    }

    private fun setOnEditDisplay() {
        binding.editMethodText.text = "Edit method"
        binding.deleteMethodButton.visibility = View.VISIBLE
    }

    private fun setOnCreateDisplay() {
        binding.editMethodText.text = "Create method"
        binding.deleteMethodButton.visibility = View.INVISIBLE
    }

    private fun setOnArrayDisplay() {
        binding.methodDimensionText.visibility = View.VISIBLE
        binding.methodDimensionInput.visibility = View.VISIBLE
    }

    private fun setOnSingleDisplay() {
        binding.methodDimensionText.visibility = View.INVISIBLE
        binding.methodDimensionInput.visibility = View.INVISIBLE
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
        private get() = binding.methodNameInput.text.toString()
    private val methodVisibility: Visibility
        private get() {
            if (binding.methodPublicRadio.isChecked) return Visibility.PUBLIC
            return if (binding.methodPrivateRadio.isChecked) Visibility.PROTECTED else Visibility.PRIVATE
        }
    private val isStatic: Boolean
        private get() = binding.methodStaticCheck.isChecked
    private val methodType: UmlType?
        private get() = UmlType.Companion.valueOf(
            binding.methodTypeSpinner.selectedItem.toString(),
            UmlType.Companion.umlTypes
        )
    private val methodMultiplicity: TypeMultiplicity
        private get() {
            if (binding.methodSimpleRadio.isChecked) return TypeMultiplicity.SINGLE
            return if (binding.methodCollectionRadio.isChecked) TypeMultiplicity.COLLECTION else TypeMultiplicity.ARRAY
        }
    private val arrayDimension: Int
        private get() = if (binding.methodDimensionInput.text.toString() == "") 0 else binding.methodDimensionInput.text.toString()
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