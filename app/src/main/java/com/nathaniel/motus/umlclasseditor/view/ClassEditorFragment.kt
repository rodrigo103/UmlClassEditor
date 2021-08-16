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
import android.app.AlertDialog
import android.app.Dialog
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import java.util.*

class ClassEditorFragment  //    **********************************************************************************************
//    Constructors
//    **********************************************************************************************
    : EditorFragment(), View.OnClickListener, OnItemLongClickListener,
    RadioGroup.OnCheckedChangeListener, OnChildClickListener {
    private var mEditClassText: TextView? = null
    private var mClassNameEdit: EditText? = null
    private var mDeleteClassButton: Button? = null
    private var mClassTypeRadioGroup: RadioGroup? = null
    private var mJavaRadio: RadioButton? = null
    private var mAbstractRadio: RadioButton? = null
    private var mInterfaceRadio: RadioButton? = null
    private var mEnumRadio: RadioButton? = null
    private var mMemberListView: ExpandableListView? = null
    private var mOKButton: Button? = null
    private var mCancelButton: Button? = null
    private var mOKCancelLinearLayout: LinearLayout? = null
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
        return inflater.inflate(R.layout.fragment_class_editor, container, false)
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
        mEditClassText = activity!!.findViewById(R.id.edit_class_text)
        mClassNameEdit = activity!!.findViewById(R.id.class_name_input)
        mDeleteClassButton = activity!!.findViewById(R.id.delete_class_button)
        mDeleteClassButton.setTag(DELETE_CLASS_BUTTON_TAG)
        mDeleteClassButton.setOnClickListener(this)
        mClassTypeRadioGroup = activity!!.findViewById(R.id.class_type_radio_group)
        mClassTypeRadioGroup.setOnCheckedChangeListener(this)
        mJavaRadio = activity!!.findViewById(R.id.class_java_radio)
        mAbstractRadio = activity!!.findViewById(R.id.class_abstract_radio)
        mInterfaceRadio = activity!!.findViewById(R.id.class_interface_radio)
        mEnumRadio = activity!!.findViewById(R.id.class_enum_radio)
        mMemberListView = activity!!.findViewById(R.id.class_members_list)
        mMemberListView.setTag(MEMBER_LIST_TAG)
        mMemberListView.setOnChildClickListener(this)
        mMemberListView.setOnItemLongClickListener(this)
        mOKButton = activity!!.findViewById(R.id.class_ok_button)
        mOKButton.setTag(OK_BUTTON_TAG)
        mOKButton.setOnClickListener(this)
        mCancelButton = activity!!.findViewById(R.id.class_cancel_button)
        mCancelButton.setTag(CANCEL_BUTTON_TAG)
        mCancelButton.setOnClickListener(this)
        mOKCancelLinearLayout = activity!!.findViewById(R.id.class_ok_cancel_linear)
    }

    override fun initializeMembers() {
        if (mClassOrder != -1) {
            mUmlClass = mCallback.project.findClassByOrder(mClassOrder)
        } else {
            //class without type
            mUmlClass = UmlClass(mCallback.project.umlClassCount)
            mCallback.project.addUmlClass(mUmlClass)
        }
        if (mClassOrder != -1 && mUmlClass.getUmlClassType() == UmlClassType.ENUM) sIsJavaClass =
            false else sIsJavaClass = true
    }

    override fun initializeFields() {
        if (mClassOrder != -1) {
            mClassNameEdit.setText(mUmlClass.getName())
            when (mUmlClass.getUmlClassType()) {
                UmlClassType.JAVA_CLASS -> mJavaRadio!!.isChecked = true
                UmlClassType.ABSTRACT_CLASS -> mAbstractRadio!!.isChecked = true
                UmlClassType.INTERFACE -> mInterfaceRadio!!.isChecked = true
                else -> mEnumRadio!!.isChecked = true
            }
        } else {
            mClassNameEdit!!.setText("")
            mJavaRadio!!.isChecked = true
        }
        updateLists()
    }

    private fun populateMemberListViewForJavaClass() {
        var attributeGroupIsExpanded = false
        var methodGroupIsExpanded = false
        if (mMemberListView!!.expandableListAdapter != null) {
            if (mMemberListView!!.isGroupExpanded(0)) attributeGroupIsExpanded = true
            if (mMemberListView!!.isGroupExpanded(1)) methodGroupIsExpanded = true
        }
        val attributeList: MutableList<AdapterItem?> = ArrayList()
        for (a in mUmlClass.getAttributes()) attributeList.add(a)
        Collections.sort(attributeList, AdapterItemComparator())
        attributeList.add(0, AddItemString(getString(R.string.new_attribute_string)))
        val methodList: MutableList<AdapterItem?> = ArrayList()
        for (m in mUmlClass.getMethods()) methodList.add(m)
        Collections.sort(methodList, AdapterItemComparator())
        methodList.add(0, AddItemString(getString(R.string.new_method_string)))
        val title: List<String> = ArrayList()
        title.add(0, getString(R.string.attributes_string))
        title.add(1, getString(R.string.methods_string))
        val hashMap = HashMap<String, List<AdapterItem?>>()
        hashMap[getString(R.string.attributes_string)] = attributeList
        hashMap[getString(R.string.methods_string)] = methodList
        val adapter = CustomExpandableListViewAdapter(context, title, hashMap)
        mMemberListView!!.setAdapter(adapter)
        if (attributeGroupIsExpanded) mMemberListView!!.expandGroup(0)
        if (methodGroupIsExpanded) mMemberListView!!.expandGroup(1)
    }

    private fun populateMemberListViewForEnum() {
        val valueList: MutableList<AdapterItem?> = ArrayList()
        valueList.addAll(mUmlClass.getValues())
        Collections.sort(valueList, AdapterItemComparator())
        valueList.add(0, AddItemString(getString(R.string.new_value_string)))
        val title: MutableList<String> = ArrayList()
        title.add(getString(R.string.values_string))
        val hashMap = HashMap<String, List<AdapterItem?>>()
        hashMap[getString(R.string.values_string)] = valueList
        val adapter = CustomExpandableListViewAdapter(context, title, hashMap)
        mMemberListView!!.setAdapter(adapter)
    }

    fun updateLists() {
        if (sIsJavaClass) populateMemberListViewForJavaClass() else populateMemberListViewForEnum()
    }

    private fun setOnEditDisplay() {
        mEditClassText!!.text = "Edit class"
        mDeleteClassButton!!.visibility = View.VISIBLE
    }

    private fun setOnCreateDisplay() {
        mEditClassText!!.text = "Create class"
        mDeleteClassButton!!.visibility = View.INVISIBLE
    }

    fun updateClassEditorFragment(xPos: Float, yPos: Float, classOrder: Int) {
        mXPos = xPos
        mYPos = yPos
        mClassOrder = classOrder
        initializeMembers()
        initializeFields()
        if (mClassOrder == -1) setOnCreateDisplay() else setOnEditDisplay()
        if (mClassOrder != -1 && mUmlClass.getUmlClassType() == UmlClassType.ENUM) sIsJavaClass =
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
            mUmlClass.getClassOrder()
        ) else if (item.name == getString(R.string.new_method_string) && i1 == 0) mCallback!!.openMethodEditorFragment(
            -1,
            mUmlClass.getClassOrder()
        ) else if (item.name == getString(R.string.new_value_string) && i1 == 0) startNewValueDialog() else {
            if (title == getString(R.string.attributes_string)) mCallback!!.openAttributeEditorFragment(
                (item as UmlClassAttribute).attributeOrder,
                mUmlClass.getClassOrder()
            ) else if (title == getString(R.string.methods_string)) mCallback!!.openMethodEditorFragment(
                (item as UmlClassMethod).methodOrder,
                mUmlClass.getClassOrder()
            ) else if (title == getString(R.string.values_string)) startRenameValueDialog((item as UmlEnumValue).valueOrder)
        }
        return true
    }

    //    **********************************************************************************************
    //    Edition methods
    //    **********************************************************************************************
    override fun clearDraftObject() {
        if (mClassOrder == -1) mCallback.project.removeUmlClass(mUmlClass)
    }

    override fun createOrUpdateObject(): Boolean {
        return createOrUpdateClass()
    }

    private fun createOrUpdateClass(): Boolean {
        return if (className == "") {
            Toast.makeText(context, "Name cannot be blank", Toast.LENGTH_SHORT).show()
            false
        } else if (mCallback.project.containsClassNamed(className)
            && mCallback.project.getUmlClass(className).classOrder != mClassOrder
        ) {
            Toast.makeText(context, "This name already exists in project", Toast.LENGTH_SHORT)
                .show()
            false
        } else if (UmlType.Companion.containsUmlTypeNamed(className) && UmlType.Companion.valueOf(
                className, UmlType.Companion.getUmlTypes()
            ).getTypeLevel() != TypeLevel.PROJECT
        ) {
            Toast.makeText(
                context,
                "This name already exists as standard or custom type",
                Toast.LENGTH_SHORT
            ).show()
            false
        } else {
            mUmlClass.setName(className)
            mUmlClass.setUmlClassType(classType)
            if (mClassOrder == -1) {
                mUmlClass.setUmlClassNormalXPos(mXPos)
                mUmlClass.setUmlClassNormalYPos(mYPos)
                //"finish" to declare type
                mUmlClass!!.upgradeToProjectUmlType()
            }
            true
        }
    }

    private val className: String
        private get() = mClassNameEdit!!.text.toString()
    private val classType: UmlClassType
        private get() {
            if (mJavaRadio!!.isChecked) return UmlClassType.JAVA_CLASS
            if (mAbstractRadio!!.isChecked) return UmlClassType.ABSTRACT_CLASS
            return if (mInterfaceRadio!!.isChecked) UmlClassType.INTERFACE else UmlClassType.ENUM
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
                mUmlClass!!.addValue(UmlEnumValue(input.text.toString(), mUmlClass.getValueCount()))
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
            mCallback.project.removeUmlClass(mUmlClass)
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