package com.nathaniel.motus.umlclasseditor.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.nathaniel.motus.umlclasseditor.R
import com.nathaniel.motus.umlclasseditor.model.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [AttributeEditorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AttributeEditorFragment  //    **********************************************************************************************
//    Constructors
//    **********************************************************************************************
    : EditorFragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private var mAttributeOrder = 0
    private var mClassOrder = 0
    private var mUmlClassAttribute: UmlClassAttribute? = null
    private var mClassEditorFragmentTag: String? = null
    private var mUmlClass: UmlClass? = null
    private var mEditAttributeText: TextView? = null
    private var mDeleteAttributeButton: Button? = null
    private var mAttributeNameEdit: EditText? = null
    private var mPublicRadio: RadioButton? = null
    private var mProtectedRadio: RadioButton? = null
    private var mPrivateRadio: RadioButton? = null
    private var mStaticCheck: CheckBox? = null
    private var mFinalCheck: CheckBox? = null
    private var mTypeSpinner: Spinner? = null
    private var mMultiplicityRadioGroup: RadioGroup? = null
    private var mSimpleRadio: RadioButton? = null
    private var mCollectionRadio: RadioButton? = null
    private var mArrayRadio: RadioButton? = null
    private var mDimText: TextView? = null
    private var mDimEdit: EditText? = null
    private var mOKButton: Button? = null
    private var mCancelButton: Button? = null

    //    **********************************************************************************************
    //    Fragment events
    //    **********************************************************************************************
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attribute_editor, container, false)
    }

    //    **********************************************************************************************
    //    Configuration methods
    //    **********************************************************************************************
    override fun readBundle() {
        mAttributeOrder = arguments!!.getInt(ATTRIBUTE_ORDER_KEY, -1)
        mClassOrder = arguments!!.getInt(CLASS_ORDER_KEY, -1)
        mClassEditorFragmentTag = arguments!!.getString(CLASS_EDITOR_FRAGMENT_TAG_KEY)
    }

    override fun setOnCreateOrEditDisplay() {
        if (mAttributeOrder == -1) setOnCreateDisplay() else setOnEditDisplay()
    }

    override fun configureViews() {
        mEditAttributeText = activity!!.findViewById(R.id.edit_attribute_text)
        mDeleteAttributeButton = activity!!.findViewById(R.id.delete_attribute_button)
        mDeleteAttributeButton?.setTag(DELETE_ATTRIBUTE_BUTTON_TAG)
        mDeleteAttributeButton?.setOnClickListener(this)
        mAttributeNameEdit = activity!!.findViewById(R.id.attribute_name_input)
        mMultiplicityRadioGroup = activity!!.findViewById(R.id.attribute_multiplicity_radio_group)
        mMultiplicityRadioGroup?.setOnCheckedChangeListener(this)
        mPublicRadio = activity!!.findViewById(R.id.attribute_public_radio)
        mProtectedRadio = activity!!.findViewById(R.id.attribute_protected_radio)
        mPrivateRadio = activity!!.findViewById(R.id.attribute_private_radio)
        mStaticCheck = activity!!.findViewById(R.id.attribute_static_check)
        mFinalCheck = activity!!.findViewById(R.id.attribute_final_check)
        mTypeSpinner = activity!!.findViewById(R.id.attribute_type_spinner)
        mTypeSpinner?.setTag(TYPE_SPINNER_TAG)
        mSimpleRadio = activity!!.findViewById(R.id.attribute_simple_radio)
        mCollectionRadio = activity!!.findViewById(R.id.attribute_collection_radio)
        mArrayRadio = activity!!.findViewById(R.id.attribute_array_radio)
        mDimText = activity!!.findViewById(R.id.attribute_dimension_text)
        mDimEdit = activity!!.findViewById(R.id.attribute_dimension_input)
        mOKButton = activity!!.findViewById(R.id.attribute_ok_button)
        mOKButton?.setTag(OK_BUTTON_TAG)
        mOKButton?.setOnClickListener(this)
        mCancelButton = activity!!.findViewById(R.id.attribute_cancel_button)
        mCancelButton?.setTag(CANCEL_BUTTON_TAG)
        mCancelButton?.setOnClickListener(this)
    }

    override fun initializeMembers() {
        mUmlClass = mCallback?.project?.findClassByOrder(mClassOrder)
        if (mAttributeOrder != -1) {
            mUmlClassAttribute = mUmlClass!!.findAttributeByOrder(mAttributeOrder)
        } else {
            mUmlClassAttribute = UmlClassAttribute(mUmlClass?.umlClassAttributeCount!!)
            mUmlClass!!.addAttribute(mUmlClassAttribute)
        }
    }

    override fun initializeFields() {
        if (mAttributeOrder != -1) {
            mAttributeNameEdit?.setText(mUmlClassAttribute?.name)
            when (mUmlClassAttribute?.visibility) {
                Visibility.PUBLIC -> mPublicRadio!!.isChecked = true
                Visibility.PROTECTED -> mProtectedRadio!!.isChecked = true
                else -> mPrivateRadio!!.isChecked = true
            }
            mStaticCheck!!.isChecked = mUmlClassAttribute!!.isStatic
            mFinalCheck!!.isChecked = mUmlClassAttribute!!.isFinal
            when (mUmlClassAttribute?.typeMultiplicity) {
                TypeMultiplicity.SINGLE -> mSimpleRadio!!.isChecked = true
                TypeMultiplicity.COLLECTION -> mCollectionRadio!!.isChecked = true
                else -> mArrayRadio!!.isChecked = true
            }
            mDimEdit!!.setText(Integer.toString(mUmlClassAttribute?.arrayDimension!!))
            if (mUmlClassAttribute?.typeMultiplicity == TypeMultiplicity.ARRAY) setOnArrayDisplay() else setOnSingleDisplay()
        } else {
            mAttributeNameEdit!!.setText("")
            mPublicRadio!!.isChecked = true
            mStaticCheck!!.isChecked = false
            mFinalCheck!!.isChecked = false
            mSimpleRadio!!.isChecked = true
            mDimEdit!!.setText("")
            setOnSingleDisplay()
        }
        populateTypeSpinner()
    }

    private fun populateTypeSpinner() {
        val spinnerArray = mutableListOf<String>()
        for (t in UmlType.Companion?.umlTypes) if (t?.name != "void") spinnerArray.add(t?.name!!)
        Collections.sort(spinnerArray, TypeNameComparator())
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, spinnerArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mTypeSpinner!!.adapter = adapter
        if (mAttributeOrder != -1) mTypeSpinner!!.setSelection(
            spinnerArray.indexOf(
                mUmlClassAttribute?.umlType?.name
            )
        )
    }

    private fun setOnEditDisplay() {
        mDeleteAttributeButton!!.visibility = View.VISIBLE
        mEditAttributeText!!.text = "Edit attribute"
    }

    private fun setOnCreateDisplay() {
        mDeleteAttributeButton!!.visibility = View.INVISIBLE
        mEditAttributeText!!.text = "Create attribute"
    }

    private fun setOnArrayDisplay() {
        mDimText!!.visibility = View.VISIBLE
        mDimEdit!!.visibility = View.VISIBLE
    }

    private fun setOnSingleDisplay() {
        mDimText!!.visibility = View.INVISIBLE
        mDimEdit!!.visibility = View.INVISIBLE
    }

    fun updateAttributeEditorFragment(attributeOrder: Int, classOrder: Int) {
        mAttributeOrder = attributeOrder
        mClassOrder = classOrder
        initializeMembers()
        initializeFields()
        if (mAttributeOrder == -1) setOnCreateDisplay() else setOnEditDisplay()
        if (mAttributeOrder != -1 && mUmlClassAttribute?.typeMultiplicity == TypeMultiplicity.ARRAY) setOnArrayDisplay() else setOnSingleDisplay()
        setOnBackPressedCallback()
    }

    override fun closeFragment() {
        mCallback!!.closeAttributeEditorFragment(this)
    }

    //    **********************************************************************************************
    //    UI events
    //    **********************************************************************************************
    override fun onClick(v: View) {
        val tag = v.tag as Int
        when (tag) {
            OK_BUTTON_TAG -> onOKButtonClicked()
            CANCEL_BUTTON_TAG -> onCancelButtonCLicked()
            DELETE_ATTRIBUTE_BUTTON_TAG -> startDeleteAttributeDialog()
            else -> {
            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        if (checkedId == R.id.attribute_array_radio) setOnArrayDisplay() else setOnSingleDisplay()
    }

    //    **********************************************************************************************
    //    Edition methods
    //    **********************************************************************************************
    override fun createOrUpdateObject(): Boolean {
        return createOrUpdateAttribute()
    }

    override fun clearDraftObject() {
        if (mAttributeOrder == -1) mUmlClass!!.removeAttribute(mUmlClassAttribute)
    }

    private fun createOrUpdateAttribute(): Boolean {
        return if (attributeName == "") {
            Toast.makeText(context, "Attribute name cannot be blank", Toast.LENGTH_SHORT).show()
            false
        } else if (mUmlClass!!.containsAttributeNamed(attributeName) &&
            mUmlClass!!.getAttribute(attributeName)?.attributeOrder != mAttributeOrder
        ) {
            Toast.makeText(context, "This named is already used", Toast.LENGTH_SHORT).show()
            false
        } else {
            mUmlClassAttribute?.name = (attributeName)
            mUmlClassAttribute?.visibility = (visibility)
            mUmlClassAttribute?.isStatic = (isStatic)
            mUmlClassAttribute?.isFinal = (isFinal)
            mUmlClassAttribute?.umlType = (type)
            mUmlClassAttribute?.typeMultiplicity = (multiplicity)
            mUmlClassAttribute?.arrayDimension = (arrayDimension)
            true
        }
    }

    private val attributeName: String
        private get() = mAttributeNameEdit!!.text.toString()
    private val visibility: Visibility
        private get() {
            if (mPublicRadio!!.isChecked) return Visibility.PUBLIC
            return if (mProtectedRadio!!.isChecked) Visibility.PROTECTED else Visibility.PRIVATE
        }
    private val isStatic: Boolean
        private get() = mStaticCheck!!.isChecked
    private val isFinal: Boolean
        private get() = mFinalCheck!!.isChecked
    private val type: UmlType?
        private get() = UmlType.Companion.valueOf(
            mTypeSpinner!!.selectedItem.toString(),
            UmlType.Companion.umlTypes
        )
    private val multiplicity: TypeMultiplicity
        private get() {
            if (mSimpleRadio!!.isChecked) return TypeMultiplicity.SINGLE
            return if (mCollectionRadio!!.isChecked) TypeMultiplicity.COLLECTION else TypeMultiplicity.ARRAY
        }
    private val arrayDimension: Int
        private get() = if (mDimEdit!!.text.toString() == "") 0 else mDimEdit!!.text.toString()
            .toInt()

    //    **********************************************************************************************
    //    Alert dialogs
    //    **********************************************************************************************
    private fun startDeleteAttributeDialog() {
        val fragment: Fragment = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete attribute")
            .setMessage("Are you sure you want to delete this attribute ?")
            .setNegativeButton("NO") { dialog, which -> }
            .setPositiveButton("YES") { dialog, which ->
                mUmlClass?.attributes?.remove(mUmlClassAttribute)
                mCallback!!.closeAttributeEditorFragment(fragment)
            }
        val dialog = builder.create()
        dialog.show()
    }

    companion object {
        private const val ATTRIBUTE_ORDER_KEY = "attributeOrder"
        private const val CLASS_ORDER_KEY = "classOrder"
        private const val CLASS_EDITOR_FRAGMENT_TAG_KEY = "classEditorFragmentTag"
        private const val TYPE_SPINNER_TAG = 310
        private const val OK_BUTTON_TAG = 320
        private const val CANCEL_BUTTON_TAG = 330
        private const val DELETE_ATTRIBUTE_BUTTON_TAG = 340
        fun newInstance(
            classEditorFragmentTag: String?,
            attributeOrder: Int,
            classOrder: Int
        ): AttributeEditorFragment {
            val fragment = AttributeEditorFragment()
            val args = Bundle()
            args.putInt(ATTRIBUTE_ORDER_KEY, attributeOrder)
            args.putInt(CLASS_ORDER_KEY, classOrder)
            args.putString(CLASS_EDITOR_FRAGMENT_TAG_KEY, classEditorFragmentTag)
            fragment.arguments = args
            return fragment
        }
    }
}