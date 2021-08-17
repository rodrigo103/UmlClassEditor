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
import com.nathaniel.motus.umlclasseditor.databinding.FragmentParameterEditorBinding
import com.nathaniel.motus.umlclasseditor.model.*
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

    private var _binding: FragmentParameterEditorBinding? = null
    private val binding get() = _binding!!
    
    private var mParameterOrder = 0
    private var mMethodOrder = 0
    private var mClassOrder = 0
    private var mMethodEditorFragmentTag: String? = null
    private var mMethodParameter: MethodParameter? = null
    private var mUmlClassMethod: UmlClassMethod? = null
    private var mUmlClass: UmlClass? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentParameterEditorBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        binding.deleteParameterButton.tag = DELETE_PARAMETER_BUTTON_TAG
        binding.deleteParameterButton.setOnClickListener(this)
        binding.parameterMultiplicityRadioGroup.setOnCheckedChangeListener(this)
        binding.parameterCancelButton.tag = CANCEL_BUTTON_TAG
        binding.parameterCancelButton.setOnClickListener(this)
        binding.parameterOkButton.tag = OK_BUTTON_TAG
        binding.parameterOkButton.setOnClickListener(this)
    }

    override fun initializeFields() {
        if (mParameterOrder != -1) {
            binding.parameterNameInput.setText(mMethodParameter?.name)
            if (mMethodParameter?.typeMultiplicity == TypeMultiplicity.SINGLE) binding.parameterSimpleRadio.isChecked =
                true
            if (mMethodParameter?.typeMultiplicity == TypeMultiplicity.COLLECTION) binding.parameterCollectionRadio.isChecked =
                true
            if (mMethodParameter?.typeMultiplicity == TypeMultiplicity.ARRAY) binding.parameterArrayRadio.isChecked =
                true
            binding.parameterDimensionInput.setText(Integer.toString(mMethodParameter?.arrayDimension!!))
            if (mMethodParameter?.typeMultiplicity == TypeMultiplicity.ARRAY) setOnArrayDisplay() else setOnSingleDisplay()
        } else {
            binding.parameterNameInput.setText("")
            binding.parameterSimpleRadio.isChecked = true
            binding.parameterDimensionInput.setText("")
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
        binding.parameterTypeSpinner.adapter = adapter
        if (mParameterOrder != -1) binding.parameterTypeSpinner.setSelection(
            arrayList.indexOf(
                mMethodParameter?.umlType?.name
            )
        )
    }

    private fun setOnEditDisplay() {
        binding.editParameterText.text = "Edit parameter"
        binding.deleteParameterButton.visibility = View.VISIBLE
    }

    private fun setOnCreateDisplay() {
        binding.editParameterText.text = "Create parameter"
        binding.deleteParameterButton.visibility = View.INVISIBLE
    }

    private fun setOnSingleDisplay() {
        binding.parameterDimensionText.visibility = View.INVISIBLE
        binding.parameterDimensionInput.visibility = View.INVISIBLE
    }

    private fun setOnArrayDisplay() {
        binding.parameterDimensionText.visibility = View.VISIBLE
        binding.parameterDimensionInput.visibility = View.VISIBLE
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
        private get() = binding.parameterNameInput.text.toString()
    private val parameterType: UmlType?
        private get() = UmlType.Companion.valueOf(
            binding.parameterTypeSpinner.selectedItem.toString(),
            UmlType.Companion.umlTypes
        )
    private val parameterMultiplicity: TypeMultiplicity
        private get() {
            if (binding.parameterSimpleRadio.isChecked) return TypeMultiplicity.SINGLE
            return if (binding.parameterCollectionRadio.isChecked) TypeMultiplicity.COLLECTION else TypeMultiplicity.ARRAY
        }
    private val arrayDimension: Int
        private get() = if (binding.parameterDimensionInput.text.toString() == "") 0 else binding.parameterDimensionInput.text.toString()
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