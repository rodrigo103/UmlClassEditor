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
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.BaseExpandableListAdapter
import com.nathaniel.motus.umlclasseditor.controller.IOUtils
import java.io.File
import java.util.ArrayList

open class UmlType {
    //type of attributes and parameters, such as int, String, etc.
    //for project types, i.e project classes, it will be extended by UmlClass
    //custom types will be added by user without any class definition
    //type list is at static level
    enum class TypeLevel {
        PRIMITIVE, CUSTOM, PROJECT
    }

    //    **********************************************************************************************
    //    Getters and setters
    //    **********************************************************************************************
    open var name: String? = null
    var typeLevel: TypeLevel
        protected set

    //    **********************************************************************************************
    //    Constructors
    //    **********************************************************************************************
    constructor() {
        typeLevel = TypeLevel.PROJECT
    }

    constructor(name: String?, typeLevel: TypeLevel) {
        this.name = name
        this.typeLevel = typeLevel
        umlTypes.add(this)
    }

    fun upgradeToProjectUmlType() {
        //upgrade a class created without type to Project UmlType
        typeLevel = TypeLevel.PROJECT
        umlTypes.add(this)
    }

    //    **********************************************************************************************
    //    Test methods
    //    **********************************************************************************************
    val isPrimitiveUmlType: Boolean
        get() = typeLevel == TypeLevel.PRIMITIVE
    val isCustomUmlType: Boolean
        get() = typeLevel == TypeLevel.CUSTOM
    val isProjectUmlType: Boolean
        get() = typeLevel == TypeLevel.PROJECT

    companion object {
        val umlTypes = ArrayList<UmlType?>()
        private const val CUSTOM_TYPES_FILENAME = "custom_types"
        const val JSON_PACKAGE_VERSION_CODE = "PackageVersionCode"
        const val JSON_CUSTOM_TYPES = "CustomTypes"
        fun createUmlType(name: String?, typeLevel: TypeLevel) {
            val t = UmlType(name, typeLevel)
        }

        fun valueOf(name: String, inUmlTypes: ArrayList<UmlType?>): UmlType? {
            for (t in inUmlTypes) if (t!!.name == name) return t
            return null
        }

        //    **********************************************************************************************
        //    JSON methods
        //    **********************************************************************************************
        val customUmlTypesToJSONArray: JSONArray
            get() {
                val jsonArray = JSONArray()
                for (t in umlTypes) if (t!!.isCustomUmlType) jsonArray.put(
                    t.name
                )
                return jsonArray
            }

        fun createCustomUmlTypesFromJSONArray(jsonArray: JSONArray) {
            var typeName = jsonArray.remove(0) as String
            while (typeName != null) {
                while (containsUmlTypeNamed(typeName)) typeName = "$typeName(1)"
                createUmlType(typeName, TypeLevel.CUSTOM)
                typeName = jsonArray.remove(0) as String
            }
        }

        //    **********************************************************************************************
        //    Modifiers
        //    **********************************************************************************************
        fun clearProjectUmlTypes() {
            for (i in umlTypes.size - 1 downTo 1) if (umlTypes[i]!!.isProjectUmlType) umlTypes.removeAt(
                i
            )
        }

        fun clearUmlTypes() {
            umlTypes.clear()
        }

        fun removeUmlType(umlType: UmlType?) {
            umlTypes.remove(umlType)
        }

        fun initializePrimitiveUmlTypes(context: Context) {
            val standardTypes = context.resources.getStringArray(R.array.standard_types)
            for (i in standardTypes.indices) createUmlType(standardTypes[i], TypeLevel.PRIMITIVE)
        }

        fun containsPrimitiveUmlTypeNamed(name: String?): Boolean {
            for (t in umlTypes) if (t!!.name == name && t.isPrimitiveUmlType) return true
            return false
        }

        fun containsCustomUmlTypeNamed(name: String?): Boolean {
            for (t in umlTypes) if (t!!.name == name && t.isCustomUmlType) return true
            return false
        }

        fun containsProjectUmlTypeNamed(name: String): Boolean {
            for (t in umlTypes) if (t!!.name == name && t.isProjectUmlType) return true
            return false
        }

        fun containsUmlTypeNamed(name: String?): Boolean {
            for (t in umlTypes) if (t!!.name == name) return true
            return false
        }

        //    **********************************************************************************************
        //    Save and load methods
        //    **********************************************************************************************
        fun saveCustomUmlTypes(context: Context) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put(JSON_CUSTOM_TYPES, customUmlTypesToJSONArray)
                jsonObject.put(JSON_PACKAGE_VERSION_CODE, IOUtils.getAppVersionCode(context))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            IOUtils.saveFileToInternalStorage(
                jsonObject.toString(),
                File(context.filesDir, CUSTOM_TYPES_FILENAME)
            )
        }

        fun initializeCustomUmlTypes(context: Context) {
            try {
                val jsonObject = JSONObject(
                    IOUtils.getFileFromInternalStorage(
                        File(
                            context.filesDir,
                            CUSTOM_TYPES_FILENAME
                        )
                    )
                )
                Log.i("TEST", "Loaded custom types")
                val jsonCustomTypes = jsonObject.getJSONArray(JSON_CUSTOM_TYPES)
                createCustomUmlTypesFromJSONArray(jsonCustomTypes)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        fun exportCustomUmlTypes(context: Context, toDestination: Uri?) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put(JSON_CUSTOM_TYPES, customUmlTypesToJSONArray)
                jsonObject.put(JSON_PACKAGE_VERSION_CODE, IOUtils.getAppVersionCode(context))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            IOUtils.saveFileToExternalStorage(context, jsonObject.toString(), toDestination)
        }

        fun importCustomUmlTypes(context: Context, fromDestination: Uri?) {
            try {
                val jsonObject =
                    JSONObject(IOUtils.readFileFromExternalStorage(context, fromDestination))
                createCustomUmlTypesFromJSONArray(jsonObject.getJSONArray(JSON_CUSTOM_TYPES))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}