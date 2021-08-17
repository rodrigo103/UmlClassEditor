package com.nathaniel.motus.umlclasseditor.view

import android.app.AlertDialog
import android.app.Dialog
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
import com.nathaniel.motus.umlclasseditor.databinding.FragmentClassEditorBinding
import com.nathaniel.motus.umlclasseditor.model.*
import com.nathaniel.motus.umlclasseditor.model.UmlClass.UmlClassType
import com.nathaniel.motus.umlclasseditor.model.UmlType.TypeLevel
import java.util.*

class ClassEditorFragment  //    **********************************************************************************************
//    Constructors
//    **********************************************************************************************
    : EditorFragment(), View.OnClickListener, OnItemLongClickListener,
    RadioGroup.OnCheckedChangeListener, OnChildClickListener {

    private var _binding: FragmentClassEditorBinding? = null
    private val binding get() = _binding!!

    private var mXPos = 0f
    private var mYPos = 0f
    private var mClassOrder = 0
    private var mUmlClass: UmlClass? = null

    //    **********************************************************************************************
    //    Fragment events
    //    **********************************************************************************************
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentClassEditorBinding.inflate(inflater, container, false)
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
        mXPos = arguments!!.getFloat(XPOS_KEY)
        mYPos = arguments!!.getFloat(YPOS_KEY)
        mClassOrder = arguments!!.getInt(CLASS_ORDER_KEY, -1)
    }

    override fun setOnCreateOrEditDisplay() {
        if (mClassOrder == -1) setOnCreateDisplay() else setOnEditDisplay()
    }

    override fun configureViews() {
        binding.deleteClassButton.tag = DELETE_CLASS_BUTTON_TAG
        binding.deleteClassButton.setOnClickListener(this)
        binding.classTypeRadioGroup.setOnCheckedChangeListener(this)
        binding.classMembersList.tag = MEMBER_LIST_TAG
        binding.classMembersList.setOnChildClickListener(this)
        binding.classMembersList.onItemLongClickListener = this
        binding.classOkButton.tag = OK_BUTTON_TAG
        binding.classOkButton.setOnClickListener(this)
        binding.classCancelButton.tag = CANCEL_BUTTON_TAG
        binding.classCancelButton.setOnClickListener(this)
    }

    override fun initializeMembers() {
        if (mClassOrder != -1) {
            mUmlClass = mCallback?.project?.findClassByOrder(mClassOrder)
        } else {
            //class without type
            mUmlClass = UmlClass(mCallback?.project?.umlClassCount!!)
            mCallback?.project?.addUmlClass(mUmlClass)
        }
        if (mClassOrder != -1 && mUmlClass!!.umlClassType == UmlClassType.ENUM) sIsJavaClass =
            false else sIsJavaClass = true
    }

    override fun initializeFields() {
        if (mClassOrder != -1) {
            binding.classNameInput.setText(mUmlClass?.name)
            when (mUmlClass?.umlClassType) {
                UmlClassType.JAVA_CLASS -> binding.classJavaRadio.isChecked = true
                UmlClassType.ABSTRACT_CLASS -> binding.classAbstractRadio.isChecked = true
                UmlClassType.INTERFACE -> binding.classInterfaceRadio.isChecked = true
                else -> binding.classEnumRadio.isChecked = true
            }
        } else {
            binding.classNameInput.setText("")
            binding.classJavaRadio.isChecked = true
        }
        updateLists()
    }

    private fun populateMemberListViewForJavaClass() {
        var attributeGroupIsExpanded = false
        var methodGroupIsExpanded = false
        if (binding.classMembersList.expandableListAdapter != null) {
            if (binding.classMembersList.isGroupExpanded(0)) attributeGroupIsExpanded = true
            if (binding.classMembersList.isGroupExpanded(1)) methodGroupIsExpanded = true
        }
        val attributeList = mutableListOf<AdapterItem>()
        for (a in mUmlClass?.attributes!!) attributeList.add(a!!)
        Collections.sort(attributeList, AdapterItemComparator())
        attributeList.add(0, AddItemString(getString(R.string.new_attribute_string)))

        val methodList = mutableListOf<AdapterItem>()
        for (m in mUmlClass?.methods!!) methodList.add(m!!)
        Collections.sort(methodList, AdapterItemComparator())
        methodList.add(0, AddItemString(getString(R.string.new_method_string)))

        val title = mutableListOf<String>()
        title.add(0, getString(R.string.attributes_string))
        title.add(1, getString(R.string.methods_string))

        val hashMap = HashMap<String, List<AdapterItem>>()
        hashMap[getString(R.string.attributes_string)] = attributeList
        hashMap[getString(R.string.methods_string)] = methodList

        val adapter = CustomExpandableListViewAdapter(context, title, hashMap)
        binding.classMembersList.setAdapter(adapter)

        if (attributeGroupIsExpanded) binding.classMembersList.expandGroup(0)
        if (methodGroupIsExpanded) binding.classMembersList.expandGroup(1)
    }

    private fun populateMemberListViewForEnum() {
        val valueList = mutableListOf<AdapterItem>()
        valueList.addAll(mUmlClass?.values!!)
        Collections.sort(valueList, AdapterItemComparator())
        valueList.add(0, AddItemString(getString(R.string.new_value_string)))
        val title: MutableList<String> = ArrayList()
        title.add(getString(R.string.values_string))
        val hashMap = HashMap<String, List<AdapterItem>>()
        hashMap[getString(R.string.values_string)] = valueList
        val adapter = CustomExpandableListViewAdapter(context, title, hashMap)
        binding.classMembersList.setAdapter(adapter)
    }

    fun updateLists() {
        if (sIsJavaClass) populateMemberListViewForJavaClass() else populateMemberListViewForEnum()
    }

    private fun setOnEditDisplay() {
        binding.editClassText.text = "Edit class"
        binding.deleteClassButton.visibility = View.VISIBLE
    }

    private fun setOnCreateDisplay() {
        binding.editClassText.text = "Create class"
        binding.deleteClassButton.visibility = View.INVISIBLE
    }

    fun updateClassEditorFragment(xPos: Float, yPos: Float, classOrder: Int) {
        mXPos = xPos
        mYPos = yPos
        mClassOrder = classOrder
        initializeMembers()
        initializeFields()
        if (mClassOrder == -1) setOnCreateDisplay() else setOnEditDisplay()
        if (mClassOrder != -1 && mUmlClass?.umlClassType == UmlClassType.ENUM) sIsJavaClass =
            false else sIsJavaClass = true
        setOnBackPressedCallback()
    }

    override fun closeFragment() {
        mCallback!!.closeClassEditorFragment(this)
    }

    //    **********************************************************************************************
    //    UI events
    //    **********************************************************************************************
    override fun onClick(v: View) {
        val tag = v.tag as Int
        when (tag) {
            OK_BUTTON_TAG -> onOKButtonClicked()
            CANCEL_BUTTON_TAG -> onCancelButtonCLicked()
            DELETE_CLASS_BUTTON_TAG -> startDeleteClassDialog()
            else -> {
            }
        }
    }

    override fun onItemLongClick(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
    ): Boolean {
        val expandableListView = view.parent as ExpandableListView
        val pos = expandableListView.getExpandableListPosition(position)
        val itemType = ExpandableListView.getPackedPositionType(pos)
        val groupPos = ExpandableListView.getPackedPositionGroup(pos)
        val childPos = ExpandableListView.getPackedPositionChild(pos)
        if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            val item =
                expandableListView.expandableListAdapter.getChild(groupPos, childPos) as AdapterItem
            if (expandableListView.expandableListAdapter.getGroup(groupPos) == getString(R.string.values_string) && childPos != 0) startDeleteValueDialog(
                (item as UmlEnumValue).valueOrder
            ) else if (expandableListView.expandableListAdapter.getGroup(groupPos) == getString(R.string.attributes_string) && childPos != 0) startDeleteAttributeDialog(
                (item as UmlClassAttribute).attributeOrder
            ) else if (expandableListView.expandableListAdapter.getGroup(groupPos) == getString(R.string.methods_string) && childPos != 0) startDeleteMethodDialog(
                (item as UmlClassMethod).methodOrder
            )
        }
        return true
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        if (checkedId == R.id.class_enum_radio) sIsJavaClass = false else sIsJavaClass = true
        updateLists()
    }

    override fun onChildClick(
        expandableListView: ExpandableListView,
        view: View,
        i: Int,
        i1: Int,
        l: Long
    ): Boolean {
        val title = expandableListView.expandableListAdapter.getGroup(i) as String
        val item = expandableListView.expandableListAdapter.getChild(i, i1) as AdapterItem
        if (item.name == getString(R.string.new_attribute_string) && i1 == 0) mCallback!!.openAttributeEditorFragment(
            -1,
            mUmlClass?.classOrder!!
        ) else if (item.name == getString(R.string.new_method_string) && i1 == 0) mCallback!!.openMethodEditorFragment(
            -1,
            mUmlClass?.classOrder!!
        ) else if (item.name == getString(R.string.new_value_string) && i1 == 0) startNewValueDialog() else {
            if (title == getString(R.string.attributes_string)) mCallback!!.openAttributeEditorFragment(
                (item as UmlClassAttribute).attributeOrder,
                mUmlClass?.classOrder!!
            ) else if (title == getString(R.string.methods_string)) mCallback!!.openMethodEditorFragment(
                (item as UmlClassMethod).methodOrder,
                mUmlClass?.classOrder!!
            ) else if (title == getString(R.string.values_string)) startRenameValueDialog((item as UmlEnumValue).valueOrder)
        }
        return true
    }

    //    **********************************************************************************************
    //    Edition methods
    //    **********************************************************************************************
    override fun clearDraftObject() {
        if (mClassOrder == -1) mCallback?.project?.removeUmlClass(mUmlClass)
    }

    override fun createOrUpdateObject(): Boolean {
        return createOrUpdateClass()
    }

    private fun createOrUpdateClass(): Boolean {
        return if (className == "") {
            Toast.makeText(context, "Name cannot be blank", Toast.LENGTH_SHORT).show()
            false
        } else if (mCallback?.project?.containsClassNamed(className)!!
            && mCallback?.project?.getUmlClass(className)?.classOrder != mClassOrder
        ) {
            Toast.makeText(context, "This name already exists in project", Toast.LENGTH_SHORT)
                .show()
            false
        } else if (UmlType.Companion.containsUmlTypeNamed(className) && UmlType.Companion.valueOf(
                className, UmlType.Companion.umlTypes
            )?.typeLevel != TypeLevel.PROJECT
        ) {
            Toast.makeText(
                context,
                "This name already exists as standard or custom type",
                Toast.LENGTH_SHORT
            ).show()
            false
        } else {
            mUmlClass?.name = (className)
            mUmlClass?.umlClassType = (classType)
            if (mClassOrder == -1) {
                mUmlClass?.umlClassNormalXPos = (mXPos)
                mUmlClass?.umlClassNormalYPos = (mYPos)
                //"finish" to declare type
                mUmlClass!!.upgradeToProjectUmlType()
            }
            true
        }
    }

    private val className: String
        private get() = binding.classNameInput.text.toString()
    private val classType: UmlClassType
        private get() {
            if (binding.classJavaRadio.isChecked) return UmlClassType.JAVA_CLASS
            if (binding.classAbstractRadio.isChecked) return UmlClassType.ABSTRACT_CLASS
            return if (binding.classInterfaceRadio.isChecked) UmlClassType.INTERFACE else UmlClassType.ENUM
        }

    //    **********************************************************************************************
    //    Alert dialogs
    //    **********************************************************************************************
    private fun startNewValueDialog() {
        val adb = AlertDialog.Builder(context)
        adb.setTitle("Add a value")
            .setMessage("Enter value :")
        val input = EditText(context)
        adb.setView(input)
            .setNegativeButton("CANCEL") { dialog, which -> }
            .setPositiveButton("OK") { dialog, which ->
                mUmlClass!!.addValue(UmlEnumValue(input.text.toString(), mUmlClass?.valueCount!!))
                updateLists()
            }
        val inputDialog: Dialog = adb.create()
        inputDialog.show()
    }

    private fun startDeleteClassDialog() {
        val fragment: Fragment = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete class ?")
            .setMessage("Are you sure you want to delete this class ?")
        builder.setNegativeButton("NO") { dialog, which -> }
        builder.setPositiveButton("YES") { dialog, which ->
            mCallback!!.project!!.removeUmlClass(mUmlClass)
            mCallback!!.closeClassEditorFragment(fragment)
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun startRenameValueDialog(valueOrder: Int) {
        val builder = AlertDialog.Builder(context)
        val editText = EditText(context)
        builder.setView(editText)
            .setTitle("Rename Enum value")
            .setMessage("Enter a new name :")
            .setNegativeButton("CANCEL") { dialogInterface, i -> }
            .setPositiveButton("OK") { dialogInterface, i ->
                mUmlClass!!.findValueByOrder(valueOrder)!!.name = editText.text.toString()
                updateLists()
            }
            .create()
            .show()
    }

    private fun startDeleteValueDialog(valueOrder: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete value ?")
            .setMessage("Are you sure you want to delete this value ?")
            .setNegativeButton("NO") { dialog, which -> }
            .setPositiveButton("YES") { dialog, which ->
                mUmlClass!!.removeValue(mUmlClass!!.findValueByOrder(valueOrder))
                updateLists()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun startDeleteAttributeDialog(attributeOrder: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete attribute ?")
            .setMessage("Are you sure you want to delete this attribute ?")
            .setNegativeButton("NO") { dialog, which -> }
            .setPositiveButton("YES") { dialog, which ->
                mUmlClass!!.removeAttribute(mUmlClass!!.findAttributeByOrder(attributeOrder))
                updateLists()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun startDeleteMethodDialog(methodOrder: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete method ?")
            .setMessage("Are you sure you want to delete this method ?")
            .setNegativeButton("NO") { dialog, which -> }
            .setPositiveButton("YES") { dialog, which ->
                mUmlClass!!.removeMethod(mUmlClass!!.findMethodByOrder(methodOrder))
                updateLists()
            }
        val dialog = builder.create()
        dialog.show()
    }

    companion object {
        private var sIsJavaClass = true
        private const val NEW_ATTRIBUTE_BUTTON_TAG = 210
        private const val MEMBER_LIST_TAG = 220
        private const val NEW_METHOD_BUTTON_TAG = 230
        private const val METHOD_LIST_TAG = 240
        private const val OK_BUTTON_TAG = 250
        private const val CANCEL_BUTTON_TAG = 260
        private const val DELETE_CLASS_BUTTON_TAG = 270
        private const val NEW_VALUE_BUTTON_TAG = 280
        private const val VALUE_LIST_TAG = 290

        //class index in current project, -1 if new class
        private const val XPOS_KEY = "xPos"
        private const val YPOS_KEY = "yPos"
        private const val CLASS_ORDER_KEY = "classOrder"
        fun newInstance(xPos: Float, yPos: Float, classOrder: Int): ClassEditorFragment {
            val fragment = ClassEditorFragment()
            val args = Bundle()
            args.putFloat(XPOS_KEY, xPos)
            args.putFloat(YPOS_KEY, yPos)
            args.putInt(CLASS_ORDER_KEY, classOrder)
            fragment.arguments = args
            return fragment
        }
    }
}