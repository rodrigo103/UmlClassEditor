package com.nathaniel.motus.umlclasseditor.controller

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
import java.io.*
import java.util.*

object IOUtils {
    fun saveFileToInternalStorage(data: String?, file: File?) {
        try {
            val fileWriter = FileWriter(file)
            fileWriter.append(data)
            fileWriter.flush()
            fileWriter.close()
        } catch (e: IOException) {
            Log.i("TEST", "Saving failed")
        }
    }

    fun getFileFromInternalStorage(file: File): String {
        var projectString = ""
        if (file.exists()) {
            val bufferedReader: BufferedReader
            try {
                bufferedReader = BufferedReader(FileReader(file))
                try {
                    var readString = bufferedReader.readLine()
                    while (readString != null) {
                        projectString = projectString + readString
                        readString = bufferedReader.readLine()
                    }
                } finally {
                    bufferedReader.close()
                }
            } catch (e: IOException) {
                Log.i("TEST", "Loading failed")
            }
        }
        return projectString
    }

    fun saveFileToExternalStorage(context: Context, data: String, externalStorageUri: Uri?) {
        try {
            val outputStream = context.contentResolver.openOutputStream(
                externalStorageUri!!
            )
            outputStream!!.write(data.toByteArray())
            outputStream.flush()
            outputStream.close()
            Log.i("TEST", "Project saved")
        } catch (e: IOException) {
            Log.i("TEST", "Failed saving project")
            Log.i("TEST", e.message!!)
        }
    }

    fun readFileFromExternalStorage(context: Context, externalStorageUri: Uri?): String {
        var data = ""
        try {
            val inputStream = context.contentResolver.openInputStream(
                externalStorageUri!!
            )
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            data = bufferedReader.readLine()
            Log.i("TEST", "Project loaded")
        } catch (e: IOException) {
            Log.i("TEST", "Failed loading project")
        }
        return data
    }

    fun sortedFiles(file: File): ArrayList<String> {
        val files = file.listFiles()
        val fileList = ArrayList<String>()
        for (f in files) fileList.add(f.name)
        Collections.sort(fileList)
        return fileList
    }

    fun readRawHtmlFile(context: Context, rawId: Int): String {
        val inputStream = context.resources.openRawResource(rawId)
        val byteArrayOutputStream = ByteArrayOutputStream()
        var i: Int
        try {
            i = inputStream.read()
            while (i != -1) {
                byteArrayOutputStream.write(i)
                i = inputStream.read()
            }
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return byteArrayOutputStream.toString()
    }

    //    **********************************************************************************************
    //    Side utilities
    //    **********************************************************************************************
    fun getAppVersionCode(context: Context): Int {
        val manager = context.packageManager
        return try {
            val info = manager.getPackageInfo(context.packageName, 0)
            info.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            -1
        }
    }
}