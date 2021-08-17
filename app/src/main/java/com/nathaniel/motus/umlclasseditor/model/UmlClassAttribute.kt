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

class UmlClassAttribute : AdapterItem {
    //    **********************************************************************************************
    //    Getters and setters
    //    **********************************************************************************************
    override var name: String? = null
    var attributeOrder: Int
    var visibility = Visibility.PRIVATE
    var isStatic = false
    var isFinal = false
    var umlType: UmlType? = null
    var typeMultiplicity = TypeMultiplicity.SINGLE
    var arrayDimension = 1 //only used if it's a table

    //    **********************************************************************************************
    //    Constructors
    //    **********************************************************************************************
    constructor(
        name: String?,
        attributeOrder: Int,
        visibility: Visibility,
        aStatic: Boolean,
        aFinal: Boolean,
        umlType: UmlType?,
        typeMultiplicity: TypeMultiplicity,
        arrayDimension: Int
    ) {
        this.name = name
        this.attributeOrder = attributeOrder
        this.visibility = visibility
        isStatic = aStatic
        isFinal = aFinal
        this.umlType = umlType
        this.typeMultiplicity = typeMultiplicity
        this.arrayDimension = arrayDimension
    }

    constructor(attributeOrder: Int) {
        this.attributeOrder = attributeOrder
    }

    //return attribute name with conventional modifiers
    val attributeCompleteString: String
        get() {
            //return attribute name with conventional modifiers
            var completeString = String()
            completeString = when (visibility) {
                Visibility.PUBLIC -> "+"
                Visibility.PROTECTED -> "~"
                else -> "-"
            }
            completeString = when (typeMultiplicity) {
                TypeMultiplicity.COLLECTION -> completeString + name + " : <" + umlType?.name + ">"
                TypeMultiplicity.ARRAY -> completeString + name + " : [" + umlType?.name + "]^" + arrayDimension
                else -> completeString + name + " : " + umlType?.name
            }
            return completeString
        }

    //    **********************************************************************************************
    //    JSON methods
    //    **********************************************************************************************
    fun toJSONObject(): JSONObject? {
        val jsonObject = JSONObject()
        return try {
            jsonObject.put(JSON_CLASS_ATTRIBUTE_NAME, name)
            jsonObject.put(JSON_CLASS_ATTRIBUTE_INDEX, attributeOrder)
            jsonObject.put(JSON_CLASS_ATTRIBUTE_VISIBILITY, visibility.toString())
            jsonObject.put(JSON_CLASS_ATTRIBUTE_STATIC, isStatic)
            jsonObject.put(JSON_CLASS_ATTRIBUTE_FINAL, isFinal)
            jsonObject.put(JSON_CLASS_ATTRIBUTE_TYPE, umlType?.name)
            jsonObject.put(JSON_CLASS_ATTRIBUTE_TYPE_MULTIPLICITY, typeMultiplicity.toString())
            jsonObject.put(JSON_CLASS_ATTRIBUTE_ARRAY_DIMENSION, arrayDimension)
            jsonObject
        } catch (jsonException: JSONException) {
            null
        }
    }

    companion object {
        const val JSON_CLASS_ATTRIBUTE_NAME = "ClassAttributeName"
        const val JSON_CLASS_ATTRIBUTE_INDEX = "ClassAttributeIndex"
        const val JSON_CLASS_ATTRIBUTE_VISIBILITY = "ClassAttributeVisibility"
        const val JSON_CLASS_ATTRIBUTE_STATIC = "ClassAttributeStatic"
        const val JSON_CLASS_ATTRIBUTE_FINAL = "ClassAttributeFinal"
        const val JSON_CLASS_ATTRIBUTE_TYPE = "ClassAttributeType"
        const val JSON_CLASS_ATTRIBUTE_TYPE_MULTIPLICITY = "ClassAttributeTypeMultiplicity"
        const val JSON_CLASS_ATTRIBUTE_ARRAY_DIMENSION = "ClassAttributeArrayDimension"
        fun indexOf(attributeName: String, attributes: ArrayList<UmlClassAttribute>): Int {
            for (a in attributes) if (attributeName == a.name) return attributes.indexOf(a)
            return -1
        }

        fun fromJSONObject(jsonObject: JSONObject): UmlClassAttribute? {
            return try {
                if (UmlType.Companion.valueOf(
                        jsonObject.getString(JSON_CLASS_ATTRIBUTE_TYPE),
                        UmlType.Companion.umlTypes
                    ) == null
                ) UmlType.Companion.createUmlType(
                    jsonObject.getString(
                        JSON_CLASS_ATTRIBUTE_TYPE
                    ), TypeLevel.CUSTOM
                )
                UmlClassAttribute(
                    jsonObject.getString(JSON_CLASS_ATTRIBUTE_NAME),
                    jsonObject.getInt(JSON_CLASS_ATTRIBUTE_INDEX),
                    Visibility.valueOf(jsonObject.getString(JSON_CLASS_ATTRIBUTE_VISIBILITY)),
                    jsonObject.getBoolean(JSON_CLASS_ATTRIBUTE_STATIC),
                    jsonObject.getBoolean(JSON_CLASS_ATTRIBUTE_FINAL),
                    UmlType.Companion.valueOf(
                        jsonObject.getString(JSON_CLASS_ATTRIBUTE_TYPE),
                        UmlType.Companion.umlTypes
                    ),
                    TypeMultiplicity.valueOf(
                        jsonObject.getString(
                            JSON_CLASS_ATTRIBUTE_TYPE_MULTIPLICITY
                        )
                    ),
                    jsonObject.getInt(JSON_CLASS_ATTRIBUTE_ARRAY_DIMENSION)
                )
            } catch (jsonException: JSONException) {
                null
            }
        }
    }
}