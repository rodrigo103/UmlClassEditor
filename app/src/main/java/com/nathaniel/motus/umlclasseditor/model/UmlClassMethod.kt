package com.nathaniel.motus.umlclasseditor.model

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
import android.widget.TextView
import android.widget.ImageButton
import com.nathaniel.motus.umlclasseditor.controller.FragmentObserver
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.nathaniel.motus.umlclasseditor.view.EditorFragment
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.RadioGroup
import android.widget.ExpandableListView.OnChildClickListener
import android.widget.EditText
import android.widget.RadioButton
import android.widget.ExpandableListView
import android.widget.LinearLayout
import com.nathaniel.motus.umlclasseditor.view.ClassEditorFragment
import com.nathaniel.motus.umlclasseditor.model.AdapterItem
import com.nathaniel.motus.umlclasseditor.model.AdapterItemComparator
import com.nathaniel.motus.umlclasseditor.model.AddItemString
import com.nathaniel.motus.umlclasseditor.controller.CustomExpandableListViewAdapter
import android.widget.AdapterView
import android.widget.Toast
import com.nathaniel.motus.umlclasseditor.model.UmlType
import com.nathaniel.motus.umlclasseditor.model.UmlType.TypeLevel
import android.widget.CheckBox
import android.widget.Spinner
import com.nathaniel.motus.umlclasseditor.view.MethodEditorFragment
import com.nathaniel.motus.umlclasseditor.model.TypeMultiplicity
import com.nathaniel.motus.umlclasseditor.model.TypeNameComparator
import android.widget.ArrayAdapter
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
import android.widget.FrameLayout
import androidx.core.view.MenuCompat
import androidx.annotation.RequiresApi
import android.os.Build
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.nathaniel.motus.umlclasseditor.controller.MainActivity
import androidx.core.view.GravityCompat
import android.content.Intent
import android.widget.AbsListView
import android.util.SparseBooleanArray
import android.text.Html
import android.app.Activity
import android.widget.BaseExpandableListAdapter
import java.util.ArrayList

class UmlClassMethod : AdapterItem {
    //    **********************************************************************************************
    //    Getters and setters
    //    **********************************************************************************************
    override var name: String? = null
    var methodOrder: Int
    var visibility = Visibility.PRIVATE
    var isStatic = false
    var umlType: UmlType? = null
    var typeMultiplicity = TypeMultiplicity.SINGLE
    var arrayDimension = 1
    var parameters: ArrayList<MethodParameter?>
        private set
    var parameterCount: Int

    //    **********************************************************************************************
    //    Constructors
    //    **********************************************************************************************
    constructor(
        name: String?,
        methodOrder: Int,
        visibility: Visibility,
        aStatic: Boolean,
        umlType: UmlType?,
        typeMultiplicity: TypeMultiplicity,
        arrayDimension: Int
    ) {
        this.name = name
        this.methodOrder = methodOrder
        this.visibility = visibility
        isStatic = aStatic
        this.umlType = umlType
        this.typeMultiplicity = typeMultiplicity
        this.arrayDimension = arrayDimension
        parameters = ArrayList()
        parameterCount = 0
    }

    constructor(
        mName: String?,
        methodOrder: Int,
        mVisibility: Visibility,
        mStatic: Boolean,
        mUmlType: UmlType?,
        mTypeMultiplicity: TypeMultiplicity,
        mArrayDimension: Int,
        mParameters: ArrayList<MethodParameter?>,
        parameterCount: Int
    ) {
        name = mName
        this.methodOrder = methodOrder
        visibility = mVisibility
        isStatic = mStatic
        umlType = mUmlType
        typeMultiplicity = mTypeMultiplicity
        arrayDimension = mArrayDimension
        parameters = mParameters
        this.parameterCount = parameterCount
    }

    constructor(methodOrder: Int) {
        this.methodOrder = methodOrder
        parameterCount = 0
        parameters = ArrayList()
    }

    //return method name with conventional modifiers
    val methodCompleteString: String
        get() {
            //return method name with conventional modifiers
            var completeString = String()
            completeString = when (visibility) {
                Visibility.PUBLIC -> "+"
                Visibility.PROTECTED -> "~"
                else -> "-"
            }
            completeString = completeString + name + "("
            for (p in parameters) {
                completeString = completeString + p?.name
                if (parameters.indexOf(p) != parameters.size - 1) completeString =
                    "$completeString, "
            }
            completeString = "$completeString) : "
            completeString = when (typeMultiplicity) {
                TypeMultiplicity.COLLECTION -> completeString + "<" + umlType?.name + ">"
                TypeMultiplicity.ARRAY -> completeString + "[" + umlType?.name + "]^" + arrayDimension
                else -> completeString + umlType?.name
            }
            return completeString
        }

    fun findParameterByOrder(parameterOrder: Int): MethodParameter? {
        for (p in parameters) if (p?.parameterOrder == parameterOrder) return p
        return null
    }

    fun getParameter(parameterName: String): MethodParameter? {
        for (p in parameters) if (p?.name == parameterName) return p
        return null
    }

    //    **********************************************************************************************
    //    Modifiers
    //    **********************************************************************************************
    fun addParameter(parameter: MethodParameter?) {
        parameters.add(parameter)
        parameterCount++
    }

    fun removeParameter(parameter: MethodParameter?) {
        parameters.remove(parameter)
    }

    fun incrementParameterCount() {
        parameterCount++
    }

    //    **********************************************************************************************
    //    Test methods
    //    **********************************************************************************************
    fun containsParameterNamed(parameterName: String): Boolean {
        for (p in parameters) if (p?.name != null && p.name == parameterName) return true
        return false
    }

    fun isEquivalentTo(method: UmlClassMethod?): Boolean {
        if (methodOrder == method!!.methodOrder) return false
        if (name != method.name) return false
        if (umlType !== method.umlType) return false
        if (parameters.size != method.parameters.size) return false
        for (i in parameters.indices) {
            if (!parameters[i]!!.isEquivalentTo(method.parameters[i])) return false
        }
        return true
    }

    //    **********************************************************************************************
    //    JSON methods
    //    **********************************************************************************************
    fun toJSONObject(): JSONObject? {
        val jsonObject = JSONObject()
        return try {
            jsonObject.put(JSON_CLASS_METHOD_NAME, name)
            jsonObject.put(JSON_CLASS_METHOD_INDEX, methodOrder)
            jsonObject.put(JSON_CLASS_METHOD_VISIBILITY, visibility)
            jsonObject.put(JSON_CLASS_METHOD_STATIC, isStatic)
            jsonObject.put(JSON_CLASS_METHOD_TYPE, umlType?.name)
            jsonObject.put(JSON_CLASS_METHOD_TYPE_MULTIPLICITY, typeMultiplicity)
            jsonObject.put(JSON_CLASS_METHOD_ARRAY_DIMENSION, arrayDimension)
            jsonObject.put(JSON_CLASS_METHOD_PARAMETERS, parametersToJSONArray)
            jsonObject.put(JSON_CLASS_METHOD_PARAMETER_COUNT, parameterCount)
            jsonObject
        } catch (jsonException: JSONException) {
            null
        }
    }

    private val parametersToJSONArray: JSONArray
        private get() {
            val jsonArray = JSONArray()
            for (p in parameters) jsonArray.put(p!!.toJSONObject())
            return jsonArray
        }

    companion object {
        const val JSON_CLASS_METHOD_NAME = "ClassMethodName"
        const val JSON_CLASS_METHOD_VISIBILITY = "ClassMethodVisibility"
        const val JSON_CLASS_METHOD_STATIC = "ClassMethodStatic"
        const val JSON_CLASS_METHOD_TYPE = "ClassMethodType"
        const val JSON_CLASS_METHOD_TYPE_MULTIPLICITY = "ClassMethodTypeMultiplicity"
        const val JSON_CLASS_METHOD_ARRAY_DIMENSION = "ClassMethodArrayDimension"
        const val JSON_CLASS_METHOD_PARAMETERS = "ClassMethodParameters"
        const val JSON_CLASS_METHOD_PARAMETER_COUNT = "ClassMethodParameterCount"
        const val JSON_CLASS_METHOD_INDEX = "ClassMethodIndex"
        fun indexOf(methodName: String, methods: ArrayList<UmlClassMethod>): Int {
            for (m in methods) if (methodName == m.name) return methods.indexOf(m)
            return -1
        }

        fun fromJSONObject(jsonObject: JSONObject): UmlClassMethod? {
            return try {
                if (UmlType.Companion.valueOf(
                        jsonObject.getString(JSON_CLASS_METHOD_TYPE),
                        UmlType.Companion.umlTypes
                    ) == null
                ) UmlType.Companion.createUmlType(
                    jsonObject.getString(
                        JSON_CLASS_METHOD_TYPE
                    ), TypeLevel.CUSTOM
                )
                UmlClassMethod(
                    jsonObject.getString(JSON_CLASS_METHOD_NAME),
                    jsonObject.getInt(JSON_CLASS_METHOD_INDEX),
                    Visibility.valueOf(jsonObject.getString(JSON_CLASS_METHOD_VISIBILITY)),
                    jsonObject.getBoolean(JSON_CLASS_METHOD_STATIC),
                    UmlType.Companion.valueOf(
                        jsonObject.getString(JSON_CLASS_METHOD_TYPE),
                        UmlType.Companion.umlTypes
                    ),
                    TypeMultiplicity.valueOf(
                        jsonObject.getString(
                            JSON_CLASS_METHOD_TYPE_MULTIPLICITY
                        )
                    ),
                    jsonObject.getInt(JSON_CLASS_METHOD_ARRAY_DIMENSION),
                    getParametersFromJSONArray(jsonObject.getJSONArray(JSON_CLASS_METHOD_PARAMETERS)),
                    jsonObject.getInt(JSON_CLASS_METHOD_PARAMETER_COUNT)
                )
            } catch (jsonException: JSONException) {
                null
            }
        }

        private fun getParametersFromJSONArray(jsonArray: JSONArray): ArrayList<MethodParameter?> {
            val methodParameters = ArrayList<MethodParameter?>()
            var jsonParameter = jsonArray.remove(0) as JSONObject
            while (jsonParameter != null) {
                methodParameters.add(MethodParameter.Companion.fromJSONObject(jsonParameter))
                jsonParameter = jsonArray.remove(0) as JSONObject
            }
            return methodParameters
        }
    }
}