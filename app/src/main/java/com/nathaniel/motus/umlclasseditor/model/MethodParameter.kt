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

class MethodParameter : AdapterItem {
    //    **********************************************************************************************
    //    Getters and setters
    //    **********************************************************************************************
    override var name: String? = null
    var parameterOrder: Int
    var umlType: UmlType? = null
    var typeMultiplicity = TypeMultiplicity.SINGLE
    var arrayDimension = 1

    //    **********************************************************************************************
    //    Constructors
    //    **********************************************************************************************
    constructor(
        name: String?,
        parameterOrder: Int,
        umlType: UmlType?,
        typeMultiplicity: TypeMultiplicity,
        arrayDimension: Int
    ) {
        this.name = name
        this.parameterOrder = parameterOrder
        this.umlType = umlType
        this.typeMultiplicity = typeMultiplicity
        this.arrayDimension = arrayDimension
    }

    constructor(parameterOrder: Int) {
        this.parameterOrder = parameterOrder
    }

    //    **********************************************************************************************
    //    Test methods
    //    **********************************************************************************************
    fun isEquivalentTo(methodParameter: MethodParameter?): Boolean {
        return if (typeMultiplicity !== TypeMultiplicity.ARRAY) umlType === methodParameter!!.umlType && typeMultiplicity === methodParameter!!.typeMultiplicity else umlType === methodParameter!!.umlType && typeMultiplicity === methodParameter!!.typeMultiplicity && arrayDimension == methodParameter!!.arrayDimension
    }

    //    **********************************************************************************************
    //    JSON methods
    //    **********************************************************************************************
    fun toJSONObject(): JSONObject? {
        val jsonObject = JSONObject()
        return try {
            jsonObject.put(JSON_METHOD_PARAMETER_NAME, name)
            jsonObject.put(JSON_METHOD_PARAMETER_INDEX, parameterOrder)
            jsonObject.put(JSON_METHOD_PARAMETER_TYPE, umlType.getName())
            jsonObject.put(JSON_METHOD_PARAMETER_TYPE_MULTIPLICITY, typeMultiplicity)
            jsonObject.put(JSON_METHOD_PARAMETER_ARRAY_DIMENSION, arrayDimension)
            jsonObject
        } catch (jsonException: JSONException) {
            null
        }
    }

    companion object {
        const val JSON_METHOD_PARAMETER_NAME = "MethodParameterName"
        const val JSON_METHOD_PARAMETER_TYPE = "MethodParameterType"
        const val JSON_METHOD_PARAMETER_TYPE_MULTIPLICITY = "MethodParameterTypeMultiplicity"
        const val JSON_METHOD_PARAMETER_ARRAY_DIMENSION = "MethodParameterArrayDimension"
        const val JSON_METHOD_PARAMETER_INDEX = "MethodParameterIndex"
        fun fromJSONObject(jsonObject: JSONObject): MethodParameter? {
            return try {
                if (UmlType.Companion.valueOf(
                        jsonObject.getString(JSON_METHOD_PARAMETER_TYPE),
                        UmlType.Companion.getUmlTypes()
                    ) == null
                ) UmlType.Companion.createUmlType(
                    jsonObject.getString(
                        JSON_METHOD_PARAMETER_TYPE
                    ), TypeLevel.CUSTOM
                )
                MethodParameter(
                    jsonObject.getString(JSON_METHOD_PARAMETER_NAME),
                    jsonObject.getInt(JSON_METHOD_PARAMETER_INDEX),
                    UmlType.Companion.valueOf(
                        jsonObject.getString(JSON_METHOD_PARAMETER_TYPE),
                        UmlType.Companion.getUmlTypes()
                    ),
                    TypeMultiplicity.valueOf(
                        jsonObject.getString(
                            JSON_METHOD_PARAMETER_TYPE_MULTIPLICITY
                        )
                    ),
                    jsonObject.getInt(JSON_METHOD_PARAMETER_ARRAY_DIMENSION)
                )
            } catch (jsonException: JSONException) {
                null
            }
        }
    }
}