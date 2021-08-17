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

class UmlClass : UmlType {
    enum class UmlClassType {
        JAVA_CLASS, ABSTRACT_CLASS, INTERFACE, ENUM
    }

    //    **********************************************************************************************
    //    Getters and setters
    //    **********************************************************************************************
    var umlClassType = UmlClassType.JAVA_CLASS
    var attributes: ArrayList<UmlClassAttribute?>
    var umlClassAttributeCount: Int
    var methods: ArrayList<UmlClassMethod?>
    var umlClassMethodCount: Int
    var values: ArrayList<UmlEnumValue>
    var valueCount: Int
    var classOrder = 0
    var umlClassNormalXPos = 0f
    var umlClassNormalYPos = 0f
    var umlClassNormalWidth = 0f
    var umlClassNormalHeight = 0f

    //    **********************************************************************************************
    //    Constructors
    //    **********************************************************************************************
    constructor(classOrder: Int) {
        attributes = ArrayList()
        methods = ArrayList()
        values = ArrayList()
        umlClassAttributeCount = 0
        umlClassMethodCount = 0
        valueCount = 0
        this.classOrder = classOrder
    }

    @JvmOverloads
    constructor(name: String?, umlClassType: UmlClassType = UmlClassType.JAVA_CLASS) : super(
        name,
        TypeLevel.PROJECT
    ) {
        this.umlClassType = umlClassType
        attributes = ArrayList()
        umlClassAttributeCount = 0
        methods = ArrayList()
        umlClassMethodCount = 0
        values = ArrayList()
        valueCount = 0
    }

    constructor(
        name: String?, classOrder: Int, umlClassType: UmlClassType,
        attributes: ArrayList<UmlClassAttribute?>, attributeCount: Int,
        methods: ArrayList<UmlClassMethod?>, methodCount: Int,
        values: ArrayList<UmlEnumValue>, valueCount: Int,
        umlClassNormalXPos: Float, umlClassNormalYPos: Float
    ) : super(name, TypeLevel.PROJECT) {
        this.classOrder = classOrder
        this.umlClassType = umlClassType
        this.attributes = attributes
        umlClassAttributeCount = attributeCount
        this.methods = methods
        umlClassMethodCount = methodCount
        this.values = values
        this.valueCount = valueCount
        this.umlClassNormalXPos = umlClassNormalXPos
        this.umlClassNormalYPos = umlClassNormalYPos
    }

    constructor(
        name: String?, umlClassType: UmlClassType,
        attributes: ArrayList<UmlClassAttribute?>,
        methods: ArrayList<UmlClassMethod?>,
        values: ArrayList<UmlEnumValue>,
        umlClassNormalXPos: Float, umlClassNormalYPos: Float
    ) : super(name, TypeLevel.PROJECT) {
        this.umlClassType = umlClassType
        this.attributes = attributes
        umlClassAttributeCount = 0
        this.methods = methods
        umlClassMethodCount = 0
        this.values = values
        valueCount = 0
        this.umlClassNormalXPos = umlClassNormalXPos
        this.umlClassNormalYPos = umlClassNormalYPos
    }

    val normalRightEnd: Float
        get() = umlClassNormalXPos + umlClassNormalWidth
    val normalBottomEnd: Float
        get() = umlClassNormalYPos + umlClassNormalHeight

    override var name: String?
        get() = super.name
        set(name) {
            super.name = name
        }

    fun findAttributeByOrder(attributeOrder: Int): UmlClassAttribute? {
        for (a in attributes) if (a?.attributeOrder == attributeOrder) return a
        return null
    }

    fun findMethodByOrder(methodOrder: Int): UmlClassMethod? {
        for (m in methods) if (m?.methodOrder == methodOrder) return m
        return null
    }

    fun findValueByOrder(valueOrder: Int): UmlEnumValue? {
        for (v in values) if (v?.valueOrder == valueOrder) return v
        return null
    }

    fun getAttribute(attributeName: String): UmlClassAttribute? {
        for (a in attributes) if (a?.name == attributeName) return a
        return null
    }

    //    **********************************************************************************************
    //    Modifiers
    //    **********************************************************************************************
    fun addMethod(method: UmlClassMethod?) {
        methods.add(method)
        umlClassMethodCount++
    }

    fun removeMethod(method: UmlClassMethod?) {
        methods.remove(method)
    }

    fun addAttribute(attribute: UmlClassAttribute?) {
        attributes.add(attribute)
        umlClassAttributeCount++
    }

    fun removeAttribute(attribute: UmlClassAttribute?) {
        attributes.remove(attribute)
    }

    fun addValue(value: UmlEnumValue) {
        values.add(value)
        valueCount++
    }

    fun removeValue(value: UmlEnumValue?) {
        values.remove(value)
    }

    fun incrementUmlClassAttributeCount() {
        umlClassAttributeCount++
    }

    fun incrementUmlClassMethodCount() {
        umlClassMethodCount++
    }

    fun incrementValueCount() {
        valueCount++
    }

    //    **********************************************************************************************
    //    Test methods
    //    **********************************************************************************************
    fun containsPoint(absoluteX: Float, absoluteY: Float): Boolean {
        return absoluteX <= umlClassNormalXPos + umlClassNormalWidth && absoluteX >= umlClassNormalXPos && absoluteY <= umlClassNormalYPos + umlClassNormalHeight && absoluteY >= umlClassNormalYPos
    }

    fun isSouthOf(umlClass: UmlClass?): Boolean {
        //is this in South quarter of umlClass ?
        return umlClassNormalYPos >= umlClass!!.normalBottomEnd && normalRightEnd >= umlClass.umlClassNormalXPos - umlClassNormalYPos + umlClass.normalBottomEnd && umlClassNormalXPos <= umlClass.normalRightEnd + umlClassNormalYPos - umlClass.normalBottomEnd
    }

    fun isNorthOf(umlClass: UmlClass?): Boolean {
        //is this in North quarter of umlClass ?
        return normalBottomEnd <= umlClass!!.umlClassNormalYPos && normalRightEnd >= umlClass.umlClassNormalXPos - umlClass.umlClassNormalYPos + normalBottomEnd && umlClassNormalXPos <= umlClass.normalRightEnd + umlClass.umlClassNormalYPos - normalBottomEnd
    }

    fun isWestOf(umlClass: UmlClass?): Boolean {
        //is this in West quarter of umlClass ?
        return normalRightEnd <= umlClass!!.umlClassNormalXPos && normalBottomEnd >= umlClass.umlClassNormalYPos - umlClass.umlClassNormalXPos + normalRightEnd && umlClassNormalYPos <= umlClass.normalBottomEnd + umlClass.umlClassNormalXPos - normalRightEnd
    }

    fun isEastOf(umlClass: UmlClass?): Boolean {
        //is this in East Quarter of umlClass ?
        return umlClassNormalXPos >= umlClass!!.normalRightEnd && normalBottomEnd >= umlClass.umlClassNormalYPos - umlClassNormalXPos + umlClass.normalRightEnd && umlClassNormalYPos <= umlClass.normalBottomEnd + umlClassNormalXPos - umlClass.normalRightEnd
    }

    fun isInvolvedInRelation(umlRelation: UmlRelation?): Boolean {
        return this === umlRelation?.relationOriginClass || this === umlRelation?.relationEndClass
    }

    fun alreadyExists(inProject: UmlProject): Boolean {
        //check whether class name already exists
        for (c in inProject.umlClasses) if (name == c!!.name) return true
        return false
    }

    fun containsAttributeNamed(attributeName: String): Boolean {
        for (a in attributes) if (a?.name != null && a?.name == attributeName) return true
        return false
    }

    fun containsEquivalentMethodTo(method: UmlClassMethod?): Boolean {
        for (m in methods) if (m!!.isEquivalentTo(method)) return true
        return false
    }

    //    **********************************************************************************************
    //    JSON methods
    //    **********************************************************************************************
    fun toJSONObject(): JSONObject? {
        val jsonObject = JSONObject()
        return try {
            jsonObject.put(JSON_CLASS_NAME, name.toString())
            jsonObject.put(JSON_CLASS_INDEX, classOrder)
            jsonObject.put(JSON_CLASS_CLASS_TYPE, umlClassType)
            jsonObject.put(JSON_CLASS_ATTRIBUTES, attributesToJSONArray)
            jsonObject.put(JSON_CLASS_ATTRIBUTE_COUNT, umlClassAttributeCount)
            jsonObject.put(JSON_CLASS_METHODS, methodsToJSONArray)
            jsonObject.put(JSON_CLASS_METHOD_COUNT, umlClassMethodCount)
            jsonObject.put(JSON_CLASS_VALUES, valuesToJSONArray)
            jsonObject.put(JSON_CLASS_VALUE_COUNT, valueCount)
            jsonObject.put(JSON_CLASS_NORMAL_XPOS, umlClassNormalXPos.toDouble())
            jsonObject.put(JSON_CLASS_NORMAL_YPOS, umlClassNormalYPos.toDouble())
            jsonObject
        } catch (e: JSONException) {
            null
        }
    }

    private val attributesToJSONArray: JSONArray
        private get() {
            val jsonArray = JSONArray()
            for (a in attributes) jsonArray.put(a!!.toJSONObject())
            return jsonArray
        }
    private val methodsToJSONArray: JSONArray
        private get() {
            val jsonArray = JSONArray()
            for (m in methods) jsonArray.put(m!!.toJSONObject())
            return jsonArray
        }
    private val valuesToJSONArray: JSONArray
        private get() {
            val jsonArray = JSONArray()
            for (v in values) jsonArray.put(v!!.toJSONObject())
            return jsonArray
        }

    companion object {
        private const val JSON_CLASS_NAME = "ClassName"
        private const val JSON_CLASS_INDEX = "ClassIndex"
        private const val JSON_CLASS_CLASS_TYPE = "ClassClassType"
        private const val JSON_CLASS_ATTRIBUTES = "ClassAttributes"
        private const val JSON_CLASS_METHODS = "ClassMethods"
        private const val JSON_CLASS_VALUES = "ClassValues"
        private const val JSON_CLASS_NORMAL_XPOS = "ClassNormalXPos"
        private const val JSON_CLASS_NORMAL_YPOS = "ClassNormalYPos"
        private const val JSON_CLASS_ATTRIBUTE_COUNT = "ClassAttributeCount"
        private const val JSON_CLASS_METHOD_COUNT = "ClassMethodCount"
        private const val JSON_CLASS_VALUE_COUNT = "ClassValueCount"

        //we need to first create classes with their names
        //in order to have them usable to create UmlTyped objects
        fun fromJSONObject(jsonObject: JSONObject): UmlClass? {
            return try {
                UmlClass(jsonObject.getString(JSON_CLASS_NAME))
            } catch (e: JSONException) {
                null
            }
        }

        //and then populate them with their attributes
        fun populateUmlClassFromJSONObject(jsonObject: JSONObject, project: UmlProject) {
            //read a class JSONObject and populate the already created class
            try {
                val umlClass = project.getUmlClass(jsonObject.getString(JSON_CLASS_NAME))
                umlClass!!.classOrder = jsonObject.getInt(JSON_CLASS_INDEX)
                umlClass.umlClassType = UmlClassType.valueOf(
                    jsonObject.getString(
                        JSON_CLASS_CLASS_TYPE
                    )
                )
                umlClass.attributes = getAttributesFromJSONArray(
                    jsonObject.getJSONArray(
                        JSON_CLASS_ATTRIBUTES
                    )
                )
                umlClass.methods = getMethodsFromJSONArray(
                    jsonObject.getJSONArray(
                        JSON_CLASS_METHODS
                    )
                )
                umlClass.values = getValuesFromJSONArray(jsonObject.getJSONArray(JSON_CLASS_VALUES))
                umlClass.umlClassNormalXPos = jsonObject.getInt(JSON_CLASS_NORMAL_XPOS).toFloat()
                umlClass.umlClassNormalYPos = jsonObject.getInt(JSON_CLASS_NORMAL_YPOS).toFloat()
                umlClass.umlClassAttributeCount = jsonObject.getInt(JSON_CLASS_ATTRIBUTE_COUNT)
                umlClass.umlClassMethodCount = jsonObject.getInt(JSON_CLASS_METHOD_COUNT)
                umlClass.valueCount = jsonObject.getInt(JSON_CLASS_VALUE_COUNT)
            } catch (ignored: JSONException) {
            }
        }

        private fun getAttributesFromJSONArray(jsonArray: JSONArray): ArrayList<UmlClassAttribute?> {
            val umlClassAttributes = ArrayList<UmlClassAttribute?>()
            var jsonAttribute = jsonArray.remove(0) as JSONObject
            while (jsonAttribute != null) {
                umlClassAttributes.add(UmlClassAttribute.Companion.fromJSONObject(jsonAttribute))
                jsonAttribute = jsonArray.remove(0) as JSONObject
            }
            return umlClassAttributes
        }

        private fun getMethodsFromJSONArray(jsonArray: JSONArray): ArrayList<UmlClassMethod?> {
            val umlClassMethods = ArrayList<UmlClassMethod?>()
            var jsonMethod = jsonArray.remove(0) as JSONObject
            while (jsonMethod != null) {
                umlClassMethods.add(UmlClassMethod.Companion.fromJSONObject(jsonMethod))
                jsonMethod = jsonArray.remove(0) as JSONObject
            }
            return umlClassMethods
        }

        private fun getValuesFromJSONArray(jsonArray: JSONArray): ArrayList<UmlEnumValue> {
            val values = ArrayList<UmlEnumValue>()
            var jsonValue = jsonArray.remove(0) as JSONObject
            while (jsonValue != null) {
                values.add(UmlEnumValue.Companion.fromJSONObject(jsonValue)!!)
                jsonValue = jsonArray.remove(0) as JSONObject
            }
            return values
        }
    }
}