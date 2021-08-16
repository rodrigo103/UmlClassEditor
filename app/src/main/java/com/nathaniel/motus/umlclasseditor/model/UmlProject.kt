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
import android.widget.BaseExpandableListAdapter
import com.nathaniel.motus.umlclasseditor.controller.IOUtils
import java.io.File
import java.util.ArrayList

class UmlProject(//    **********************************************************************************************
    //    Getters and setters
    //    **********************************************************************************************
    var name: String, context: Context?
) {
    val umlClasses: ArrayList<UmlClass?>
    var umlClassCount: Int
    var umlRelations: ArrayList<UmlRelation?>
    private var mAppVersionCode = 0
    var zoom = 1f
    var xOffset = 0f
    var yOffset = 0f
    fun getUmlClass(className: String?): UmlClass? {
        for (c in umlClasses) if (c.getName() == className) return c
        return null
    }

    fun setAppVersionCode(appVersionCode: Int) {
        mAppVersionCode = appVersionCode
    }

    fun findClassByOrder(classOrder: Int): UmlClass? {
        for (c in umlClasses) if (c.getClassOrder() == classOrder) return c
        return null
    }

    //    **********************************************************************************************
    //    Initialization
    //    **********************************************************************************************
    //    **********************************************************************************************
    //    Modifiers
    //    **********************************************************************************************
    fun addUmlClass(umlClass: UmlClass?) {
        umlClasses.add(umlClass)
        umlClassCount++
    }

    fun removeUmlClass(umlClass: UmlClass?) {
        umlClasses.remove(umlClass)
        UmlType.Companion.removeUmlType(umlClass)
        removeRelationsInvolving(umlClass)
        removeAttributesOfType(umlClass as UmlType?)
        removeMethodsOfType(umlClass as UmlType?)
        removeParametersOfType(umlClass as UmlType?)
    }

    fun addUmlRelation(umlRelation: UmlRelation?) {
        umlRelations.add(umlRelation)
    }

    fun removeUmlRelation(umlRelation: UmlRelation?) {
        umlRelations.remove(umlRelation)
    }

    fun removeRelationsInvolving(umlClass: UmlClass?) {
        val umlRelations = ArrayList<UmlRelation?>()
        for (r in this.umlRelations) {
            if (umlClass!!.isInvolvedInRelation(r)) umlRelations.add(r)
        }
        for (r in umlRelations) {
            removeUmlRelation(r)
        }
    }

    fun removeAttributesOfType(umlType: UmlType?) {
        for (c in umlClasses) {
            for (a in c.getAttributes()) {
                if (a.umlType === umlType) c!!.removeAttribute(a)
            }
        }
    }

    fun removeMethodsOfType(umlType: UmlType?) {
        for (c in umlClasses) {
            for (m in c.getMethods()) {
                if (m.umlType === umlType) c!!.removeMethod(m)
            }
        }
    }

    fun removeParametersOfType(umlType: UmlType?) {
        for (c in umlClasses) {
            for (m in c.getMethods()) {
                for (p in m.parameters) {
                    if (p.umlType === umlType) m!!.removeParameter(p)
                }
            }
        }
    }

    fun incrementClassCount() {
        umlClassCount++
    }

    //    **********************************************************************************************
    //    Test methods
    //    **********************************************************************************************
    fun relationAlreadyExistsBetween(firstClass: UmlClass?, secondClass: UmlClass?): Boolean {
        //check whether there already is a relation between two classes
        //this test is not oriented
        var test = false
        for (r in umlRelations) if (r.getRelationOriginClass() === firstClass && r.getRelationEndClass() === secondClass
            || r.getRelationOriginClass() === secondClass && r.getRelationEndClass() === firstClass
        ) test = true
        return test
    }

    fun hasConflictNameWith(umlClass: UmlClass): Boolean {
        var test = false
        for (c in umlClasses) if (c.getName().compareTo(umlClass.name) == 0) test = true
        return test
    }

    fun containsClassNamed(className: String): Boolean {
        //check whether a class with className already exists in this project
        for (c in umlClasses) if (c.getName() != null && c.getName() == className) return true
        return false
    }

    //    **********************************************************************************************
    //    JSON methods
    //    **********************************************************************************************
    fun toJSONObject(context: Context): JSONObject? {
        val jsonObject = JSONObject()
        return try {
            jsonObject.put(JSON_PROJECT_PACKAGE_VERSION_CODE, IOUtils.getAppVersionCode(context))
            jsonObject.put(JSON_PROJECT_ZOOM, zoom.toDouble())
            jsonObject.put(JSON_PROJECT_X_OFFSET, xOffset.toDouble())
            jsonObject.put(JSON_PROJECT_Y_OFFSET, yOffset.toDouble())
            jsonObject.put(JSON_PROJECT_NAME, name)
            jsonObject.put(JSON_PROJECT_CLASSES, classesToJSONArray)
            jsonObject.put(JSON_PROJECT_CLASS_COUNT, umlClassCount)
            jsonObject.put(JSON_PROJECT_RELATIONS, relationsToJSONArray)
            jsonObject
        } catch (e: JSONException) {
            null
        }
    }

    private val classesToJSONArray: JSONArray
        private get() {
            val jsonArray = JSONArray()
            for (c in umlClasses) jsonArray.put(c!!.toJSONObject())
            return jsonArray
        }

    private fun populateClassesFromJSONArray(jsonArray: JSONArray) {
        var jsonObject = jsonArray.remove(0) as JSONObject
        while (jsonObject != null) {
            UmlClass.Companion.populateUmlClassFromJSONObject(jsonObject, this)
            jsonObject = jsonArray.remove(0) as JSONObject
        }
    }

    private val relationsToJSONArray: JSONArray
        private get() {
            val jsonArray = JSONArray()
            for (r in umlRelations) jsonArray.put(r!!.toJSONObject())
            return jsonArray
        }

    //    **********************************************************************************************
    //    Save and load project methods
    //    **********************************************************************************************
    fun save(context: Context) {
        val destination = File(context.filesDir, PROJECT_DIRECTORY)
        if (!destination.exists()) destination.mkdir()
        IOUtils.saveFileToInternalStorage(toJSONObject(context).toString(), File(destination, name))
    }

    fun exportProject(context: Context, toDestination: Uri?) {
        IOUtils.saveFileToExternalStorage(context, toJSONObject(context).toString(), toDestination)
    }

    fun mergeWith(project: UmlProject?) {
        for (c in project!!.umlClasses) {
            while (UmlType.Companion.containsPrimitiveUmlTypeNamed(c.getName())) c.setName(c.getName() + "(1)")
            while (UmlType.Companion.containsCustomUmlTypeNamed(c.getName())) c.setName(c.getName() + "(1)")
            while (c!!.alreadyExists(this)) c.name = c.name + "(1)"
            addUmlClass(c)
        }
        for (r in project.umlRelations) addUmlRelation(r)
    }

    companion object {
        const val JSON_PROJECT_NAME = "ProjectName"
        const val JSON_PROJECT_CLASS_COUNT = "ProjectClassCount"
        const val JSON_PROJECT_CLASSES = "ProjectClasses"
        const val JSON_PROJECT_RELATIONS = "ProjectRelations"
        const val JSON_PROJECT_PACKAGE_VERSION_CODE = "ProjectPackageVersionCode"
        const val JSON_PROJECT_ZOOM = "ProjectZoom"
        const val JSON_PROJECT_X_OFFSET = "ProjectXOffset"
        const val JSON_PROJECT_Y_OFFSET = "ProjectYOffset"
        const val PROJECT_DIRECTORY = "projects"
        fun fromJSONObject(jsonObject: JSONObject, context: Context?): UmlProject? {
            return try {
                val project = UmlProject(jsonObject.getString(JSON_PROJECT_NAME), context)
                project.setAppVersionCode(jsonObject.getInt(JSON_PROJECT_PACKAGE_VERSION_CODE))
                project.zoom = jsonObject.getDouble(JSON_PROJECT_ZOOM).toFloat()
                project.xOffset = jsonObject.getDouble(JSON_PROJECT_X_OFFSET).toFloat()
                project.yOffset = jsonObject.getDouble(JSON_PROJECT_Y_OFFSET).toFloat()
                project.umlClassCount = jsonObject.getInt(JSON_PROJECT_CLASS_COUNT)

                //copy jsonObject because it is cleared in getClassesFromJSONArray
                val jsonObjectCopy = JSONObject(jsonObject.toString())
                for (c in getClassesFromJSONArray(jsonObjectCopy[JSON_PROJECT_CLASSES] as JSONArray)) project.addUmlClass(
                    c
                )
                project.populateClassesFromJSONArray(jsonObject[JSON_PROJECT_CLASSES] as JSONArray)
                project.umlRelations = getRelationsFromJSONArray(
                    jsonObject[JSON_PROJECT_RELATIONS] as JSONArray,
                    project
                )
                project
            } catch (e: JSONException) {
                null
            }
        }

        private fun getClassesFromJSONArray(jsonArray: JSONArray): ArrayList<UmlClass?> {
            //first get classes and their names, to be populated later
            //otherwise, attributes with custom types can't be created
            val classes = ArrayList<UmlClass?>()
            var jsonObject = jsonArray.remove(0) as JSONObject
            while (jsonObject != null) {
                classes.add(UmlClass.Companion.fromJSONObject(jsonObject))
                jsonObject = jsonArray.remove(0) as JSONObject
            }
            return classes
        }

        private fun getRelationsFromJSONArray(
            jsonArray: JSONArray,
            project: UmlProject
        ): ArrayList<UmlRelation?> {
            val relations = ArrayList<UmlRelation?>()
            var jsonObject = jsonArray.remove(0) as JSONObject
            while (jsonObject != null) {
                relations.add(UmlRelation.Companion.fromJSONObject(jsonObject, project))
                jsonObject = jsonArray.remove(0) as JSONObject
            }
            return relations
        }

        fun load(context: Context, projectName: String?): UmlProject? {
            val destination = File(context.filesDir, PROJECT_DIRECTORY)
            val source = File(destination, projectName)
            try {
                return fromJSONObject(
                    JSONObject(IOUtils.getFileFromInternalStorage(source)),
                    context
                )
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }

        fun importProject(context: Context, fromFileUri: Uri?): UmlProject? {
            var umlProject: UmlProject?
            try {
                umlProject = fromJSONObject(
                    JSONObject(
                        IOUtils.readFileFromExternalStorage(
                            context,
                            fromFileUri
                        )
                    ), context
                )
                for (c in umlProject!!.umlClasses) {
                    while (UmlType.Companion.containsPrimitiveUmlTypeNamed(c.getName())) c.setName(c.getName() + "(1)")
                    while (UmlType.Companion.containsCustomUmlTypeNamed(c.getName())) c.setName(c.getName() + "(1)")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                umlProject = null
            }
            return umlProject
        }
    }

    //    **********************************************************************************************
    //    Constructors
    //    **********************************************************************************************
    init {
        umlClasses = ArrayList()
        umlClassCount = 0
        umlRelations = ArrayList()
    }
}