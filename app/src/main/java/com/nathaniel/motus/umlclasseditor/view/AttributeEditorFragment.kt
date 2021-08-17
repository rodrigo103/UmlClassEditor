package com.nathaniel.motus.umlclasseditor.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nathaniel.motus.umlclasseditor.R
import com.nathaniel.motus.umlclasseditor.databinding.FragmentAttributeEditorBinding
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

    private var _binding: FragmentAttributeEditorBinding? = null
    private val binding get() = _binding!!

    private var mAttributeOrder = 0
    private var mClassOrder = 0
    private var mUmlClassAttribute: UmlClassAttribute? = null
    private var mClassEditorFragmentTag: String? = null
    private var mUmlClass: UmlClass? = null

    //    **********************************************************************************************
    //    Fragment events
    //    **********************************************************************************************
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAttributeEditorBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
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
        binding.deleteAttributeButton.tag = DELETE_ATTRIBUTE_BUTTON_TAG
        binding.deleteAttributeButton.setOnClickListener(this)
        binding.attributeMultiplicityRadioGroup.setOnCheckedChangeListener(this)
        binding.attributeTypeSpinner.tag = TYPE_SPINNER_TAG
        binding.attributeOkButton.tag = OK_BUTTON_TAG
        binding.attributeOkButton.setOnClickListener(this)
        binding.attributeCancelButton.tag = CANCEL_BUTTON_TAG
        binding.attributeCancelButton.setOnClickListener(this)
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
            binding.attributeNameInput.setText(mUmlClassAttribute?.name)
            when (mUmlClassAttribute?.visibility) {
                Visibility.PUBLIC -> binding.attributePublicRadio.isChecked = true
                Visibility.PROTECTED -> binding.attributeProtectedRadio.isChecked = true
                else -> binding.attributePrivateRadio.isChecked = true
            }
            binding.attributeStaticCheck.isChecked = mUmlClassAttribute!!.isStatic
            binding.attributeFinalCheck.isChecked = mUmlClassAttribute!!.isFinal
            when (mUmlClassAttribute?.typeMultiplicity) {
                TypeMultiplicity.SINGLE -> binding.attributeSimpleRadio.isChecked = true
                TypeMultiplicity.COLLECTION -> binding.attributeCollectionRadio.isChecked = true
                else -> binding.attributeArrayRadio.isChecked = true
            }
            binding.attributeDimensionInput.setText(Integer.toString(mUmlClassAttribute?.arrayDimension!!))
            if (mUmlClassAttribute?.typeMultiplicity == TypeMultiplicity.ARRAY) setOnArrayDisplay() else setOnSingleDisplay()
        } else {
            binding.attributeNameInput.setText("")
            binding.attributePublicRadio.isChecked = true
            binding.attributeStaticCheck.isChecked = false
            binding.attributeFinalCheck.isChecked = false
            binding.attributeSimpleRadio.isChecked = true
            binding.attributeDimensionInput.setText("")
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
        binding.attributeTypeSpinner.adapter = adapter
        if (mAttributeOrder != -1) binding.attributeTypeSpinner.setSelection(
            spinnerArray.indexOf(
                mUmlClassAttribute?.umlType?.name
            )
        )
    }

    private fun setOnEditDisplay() {
        binding.deleteAttributeButton.visibility = View.VISIBLE
        binding.editAttributeText.text = "Edit attribute"
    }

    private fun setOnCreateDisplay() {
        binding.deleteAttributeButton.visibility = View.INVISIBLE
        binding.editAttributeText.text = "Create attribute"
    }

    private fun setOnArrayDisplay() {
        binding.attributeDimensionText.visibility = View.VISIBLE
        binding.attributeDimensionInput.visibility = View.VISIBLE
    }

    private fun setOnSingleDisplay() {
        binding.attributeDimensionText.visibility = View.INVISIBLE
        binding.attributeDimensionInput.visibility = View.INVISIBLE
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
        private get() = binding.attributeNameInput.text.toString()
    private val visibility: Visibility
        private get() {
            if (binding.attributePublicRadio.isChecked) return Visibility.PUBLIC
            return if (binding.attributeProtectedRadio.isChecked) Visibility.PROTECTED else Visibility.PRIVATE
        }
    private val isStatic: Boolean
        private get() = binding.attributeStaticCheck.isChecked
    private val isFinal: Boolean
        private get() = binding.attributeFinalCheck.isChecked
    private val type: UmlType?
        private get() = UmlType.Companion.valueOf(
            binding.attributeTypeSpinner.selectedItem.toString(),
            UmlType.Companion.umlTypes
        )
    private val multiplicity: TypeMultiplicity
        private get() {
            if (binding.attributeSimpleRadio.isChecked) return TypeMultiplicity.SINGLE
            return if (binding.attributeCollectionRadio.isChecked) TypeMultiplicity.COLLECTION else TypeMultiplicity.ARRAY
        }
    private val arrayDimension: Int
        private get() = if (binding.attributeDimensionInput.text.toString() == "") 0 else binding.attributeDimensionInput.text.toString()
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