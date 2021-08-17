package com.nathaniel.motus.umlclasseditor.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nathaniel.motus.umlclasseditor.controller.FragmentObserver
import com.nathaniel.motus.umlclasseditor.databinding.FragmentGraphBinding
import com.nathaniel.motus.umlclasseditor.model.UmlClass
import com.nathaniel.motus.umlclasseditor.model.UmlRelation.UmlRelationType

class GraphFragment  //    **********************************************************************************************
//    Constructors
//    **********************************************************************************************
    : Fragment(), View.OnClickListener {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        binding.graphview.tag = GRAPHVIEW_TAG
        binding.graphview.setGraphFragment(this)
        binding.inheritanceButton.tag = INHERITANCE_BUTTON_TAG
        binding.inheritanceButton.setOnClickListener(this)
        binding.realizationButton.tag = REALIZATION_BUTTON_TAG
        binding.realizationButton.setOnClickListener(this)
        binding.aggregationButton.tag = AGGREGATION_BUTTON_TAG
        binding.aggregationButton.setOnClickListener(this)
        binding.escapeButton.tag = ESCAPE_BUTTON_TAG
        binding.escapeButton.setOnClickListener(this)
        binding.associationButton.tag = ASSOCIATION_BUTTON_TAG
        binding.associationButton.setOnClickListener(this)
        binding.dependencyButton.tag = DEPENDENCY_BUTTON_TAG
        binding.dependencyButton.setOnClickListener(this)
        binding.compositionButton.tag = COMPOSITION_BUTTON_TAG
        binding.compositionButton.setOnClickListener(this)
        binding.newClassButton.tag = NEW_CLASS_BUTTON_TAG
        binding.newClassButton.setOnClickListener(this)
    }

    private fun createCallbackToParentActivity() {
        callBack = activity as FragmentObserver?
    }

    //    **********************************************************************************************
    //    Modifiers
    //    **********************************************************************************************
    fun setPrompt(prompt: String?) {
        binding.graphText.text = prompt
    }

    fun clearPrompt() {
        binding.graphText.text = ""
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