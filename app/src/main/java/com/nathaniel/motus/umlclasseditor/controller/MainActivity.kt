package com.nathaniel.motus.umlclasseditor.controller

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.MenuCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.nathaniel.motus.umlclasseditor.R
import com.nathaniel.motus.umlclasseditor.model.*
import com.nathaniel.motus.umlclasseditor.model.UmlRelation.UmlRelationType
import com.nathaniel.motus.umlclasseditor.model.UmlType.TypeLevel
import com.nathaniel.motus.umlclasseditor.view.*
import com.nathaniel.motus.umlclasseditor.view.GraphView.GraphViewObserver
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), FragmentObserver, GraphViewObserver,
    NavigationView.OnNavigationItemSelectedListener {
    //    **********************************************************************************************
    //    Getters and setters
    //    **********************************************************************************************
    override var project: UmlProject? = null

    //    GraphViewObserver
    override val isExpectingTouchLocation = false
    private var mPurpose = FragmentObserver.Purpose.NONE
    private var mToolbar: Toolbar? = null
    private var mDrawerLayout: DrawerLayout? = null
    private var mNavigationView: NavigationView? = null
    private var mMenuHeaderProjectNameText: TextView? = null
    private var mFirstBackPressedTime: Long = 0
    private var mOnBackPressedCallback: OnBackPressedCallback? = null

    //    **********************************************************************************************
    //    Fragments declaration
    //    **********************************************************************************************
    private var mGraphFragment: GraphFragment? = null
    private var mClassEditorFragment: ClassEditorFragment? = null
    private var mAttributeEditorFragment: AttributeEditorFragment? = null
    private var mMethodEditorFragment: MethodEditorFragment? = null
    private var mParameterEditorFragment: ParameterEditorFragment? = null

    //    **********************************************************************************************
    //    Views declaration
    //    **********************************************************************************************
    private var mMainActivityFrame: FrameLayout? = null
    private var mGraphView: GraphView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Instantiate views
        mMainActivityFrame = findViewById(R.id.activity_main_frame)
        UmlType.Companion.clearUmlTypes()
        UmlType.Companion.initializePrimitiveUmlTypes(this)
        UmlType.Companion.initializeCustomUmlTypes(this)
        preferences
        configureToolbar()
        configureDrawerLayout()
        configureNavigationView()
        configureAndDisplayGraphFragment(R.id.activity_main_frame)
        createOnBackPressedCallback()
        setOnBackPressedCallback()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main_toolbar_menu, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()
        mGraphView = findViewById(R.id.graphview)
        mGraphView?.setUmlProject(project)
        Log.i("TEST", "onStart")
    }

    override fun onDestroy() {
        super.onDestroy()
        project!!.save(applicationContext)
        Log.i("TEST", "save : project")
        savePreferences()
        Log.i("TEST", "save : preferences")
        UmlType.Companion.saveCustomUmlTypes(this)
        Log.i("TEST", "save : custom types")
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    //    **********************************************************************************************
    //    Configuration methods
    //    **********************************************************************************************
    private fun configureToolbar() {
        mToolbar = findViewById(R.id.main_activity_toolbar)
        setSupportActionBar(mToolbar)
    }

    private fun configureDrawerLayout() {
        mDrawerLayout = findViewById(R.id.activity_main_drawer)
        val toggle = ActionBarDrawerToggle(
            this,
            mDrawerLayout,
            mToolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        mDrawerLayout?.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun configureNavigationView() {
        mNavigationView = findViewById(R.id.activity_main_navigation_view)
        mMenuHeaderProjectNameText = mNavigationView?.getHeaderView(0)
            ?.findViewById(R.id.activity_main_navigation_view_header_project_name_text)
        updateNavigationView()
        mNavigationView?.setNavigationItemSelectedListener(this)
    }

    private fun updateNavigationView() {
        mMenuHeaderProjectNameText?.text = (project?.name)
    }

    private fun savePreferences() {
        val preferences = getPreferences(MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(SHARED_PREFERENCES_PROJECT_NAME, project?.name)
        editor.apply()
    }

    private val preferences: Unit
        private get() {
            val preferences = getPreferences(MODE_PRIVATE)
            val projectName = preferences.getString(SHARED_PREFERENCES_PROJECT_NAME, null)
            Log.i("TEST", "Loaded preferences")
            if (projectName != null) {
                project = UmlProject.Companion.load(applicationContext, projectName)
            } else {
                project = UmlProject("NewProject", applicationContext)
            }
        }

    private fun createOnBackPressedCallback() {
        mOnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackButtonPressed()
            }
        }
    }

    private fun setOnBackPressedCallback() {
        this.onBackPressedDispatcher.addCallback(this, mOnBackPressedCallback!!)
    }

    private fun onBackButtonPressed() {
        if (Calendar.getInstance().timeInMillis - mFirstBackPressedTime > DOUBLE_BACK_PRESSED_DELAY) {
            mFirstBackPressedTime = Calendar.getInstance().timeInMillis
            Toast.makeText(this, "Press back again to leave", Toast.LENGTH_SHORT).show()
        } else finish()
    }

    //    **********************************************************************************************
    //    Fragment management
    //    **********************************************************************************************
    private fun configureAndDisplayGraphFragment(viewContainerId: Int) {
        //handle graph fragment

//        mGraphFragment=new GraphFragment();
        mGraphFragment = GraphFragment.Companion.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(viewContainerId, mGraphFragment!!, GRAPH_FRAGMENT_TAG)
            .commitNow()
    }

    private fun configureAndDisplayClassEditorFragment(
        viewContainerId: Int,
        xLocation: Float,
        yLocation: Float,
        classOrder: Int
    ) {
        //handle class editor fragment
        if (mClassEditorFragment == null) {
            mClassEditorFragment =
                ClassEditorFragment.Companion.newInstance(xLocation, yLocation, classOrder)
            supportFragmentManager.beginTransaction()
                .hide(mGraphFragment!!)
                .add(viewContainerId, mClassEditorFragment!!, CLASS_EDITOR_FRAGMENT_TAG)
                .commitNow()
        } else {
            mClassEditorFragment!!.updateClassEditorFragment(xLocation, yLocation, classOrder)
            supportFragmentManager.beginTransaction()
                .hide(mGraphFragment!!)
                .show(mClassEditorFragment!!)
                .commitNow()
        }
    }

    private fun configureAndDisplayAttributeEditorFragment(
        viewContainerId: Int,
        attributeOrder: Int,
        classOrder: Int
    ) {
        if (mAttributeEditorFragment == null) {
            mAttributeEditorFragment = AttributeEditorFragment.Companion.newInstance(
                mClassEditorFragment!!.tag, attributeOrder, classOrder
            )
            supportFragmentManager.beginTransaction()
                .hide(mClassEditorFragment!!)
                .add(viewContainerId, mAttributeEditorFragment!!, ATTRIBUTE_EDITOR_FRAGMENT_TAG)
                .commitNow()
        } else {
            mAttributeEditorFragment!!.updateAttributeEditorFragment(attributeOrder, classOrder)
            supportFragmentManager.beginTransaction()
                .hide(mClassEditorFragment!!)
                .show(mAttributeEditorFragment!!)
                .commitNow()
        }
    }

    private fun configureAndDisplayMethodEditorFragment(
        viewContainerId: Int,
        methodOrder: Int,
        classOrder: Int
    ) {
        if (mMethodEditorFragment == null) {
            mMethodEditorFragment = MethodEditorFragment.Companion.newInstance(
                mClassEditorFragment!!.tag,
                methodOrder,
                classOrder
            )
            supportFragmentManager.beginTransaction()
                .hide(mClassEditorFragment!!)
                .add(viewContainerId, mMethodEditorFragment!!, METHOD_EDITOR_FRAGMENT_TAG)
                .commitNow()
        } else {
            mMethodEditorFragment!!.updateMethodEditorFragment(methodOrder, classOrder)
            supportFragmentManager.beginTransaction()
                .hide(mClassEditorFragment!!)
                .show(mMethodEditorFragment!!)
                .commitNow()
        }
    }

    private fun configureAndDisplayParameterEditorFragment(
        viewContainerId: Int,
        parameterOrder: Int,
        methodOrder: Int,
        classOrder: Int
    ) {
        if (mParameterEditorFragment == null) {
            mParameterEditorFragment = ParameterEditorFragment.Companion.newInstance(
                mMethodEditorFragment!!.tag, parameterOrder, methodOrder, classOrder
            )
            supportFragmentManager.beginTransaction()
                .hide(mMethodEditorFragment!!)
                .add(viewContainerId, mParameterEditorFragment!!, PARAMETER_EDITOR_FRAGMENT_TAG)
                .commitNow()
        } else {
            mParameterEditorFragment!!.updateParameterEditorFragment(
                parameterOrder,
                methodOrder,
                classOrder
            )
            supportFragmentManager.beginTransaction()
                .hide(mMethodEditorFragment!!)
                .show(mParameterEditorFragment!!)
                .commitNow()
        }
    }

    //    **********************************************************************************************
    //    Callback methods
    //    **********************************************************************************************
    //    GraphFragmentObserver
    override fun setPurpose(purpose: FragmentObserver.Purpose) {
        mPurpose = purpose
    }

    override fun closeClassEditorFragment(fragment: Fragment?) {
        supportFragmentManager.beginTransaction()
            .hide(fragment!!)
            .show(mGraphFragment!!)
            .commitNow()
        mGraphView!!.invalidate()
    }

    override fun closeAttributeEditorFragment(fragment: Fragment?) {
        supportFragmentManager.beginTransaction()
            .hide(fragment!!)
            .show(mClassEditorFragment!!)
            .commit()
        mClassEditorFragment!!.updateLists()
    }

    override fun closeMethodEditorFragment(fragment: Fragment?) {
        supportFragmentManager.beginTransaction()
            .hide(fragment!!)
            .show(mClassEditorFragment!!)
            .commitNow()
        mClassEditorFragment!!.updateLists()
    }

    override fun closeParameterEditorFragment(fragment: Fragment?) {
        supportFragmentManager.beginTransaction()
            .hide(fragment!!)
            .show(mMethodEditorFragment!!)
            .commitNow()
        mMethodEditorFragment!!.updateLists()
    }

    override fun openAttributeEditorFragment(attributeOrder: Int, classOrder: Int) {
        configureAndDisplayAttributeEditorFragment(
            R.id.activity_main_frame,
            attributeOrder,
            classOrder
        )
    }

    override fun openMethodEditorFragment(methodOrder: Int, classOrder: Int) {
        configureAndDisplayMethodEditorFragment(R.id.activity_main_frame, methodOrder, classOrder)
    }

    override fun openParameterEditorFragment(
        parameterOrder: Int,
        methodOrder: Int,
        classOrder: Int
    ) {
        configureAndDisplayParameterEditorFragment(
            R.id.activity_main_frame,
            parameterOrder,
            methodOrder,
            classOrder
        )
    }

    override fun createClass(xLocation: Float, yLocation: Float) {
        configureAndDisplayClassEditorFragment(R.id.activity_main_frame, xLocation, yLocation, -1)
    }

    override fun editClass(umlClass: UmlClass?) {
        configureAndDisplayClassEditorFragment(
            R.id.activity_main_frame,
            0f,
            0f,
            umlClass?.classOrder!!
        )
    }

    override fun createRelation(
        startClass: UmlClass?,
        endClass: UmlClass?,
        relationType: UmlRelationType?
    ) {
        if (!project!!.relationAlreadyExistsBetween(startClass, endClass)) project!!.addUmlRelation(
            UmlRelation(startClass, endClass, relationType)
        )
    }

    //    **********************************************************************************************
    //    Navigation view events
    //    **********************************************************************************************
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val menuId = item.itemId
        if (menuId == R.id.drawer_menu_new_project) {
            drawerMenuNewProject()
        } else if (menuId == R.id.drawer_menu_load_project) {
            drawerMenuLoadProject()
        } else if (menuId == R.id.drawer_menu_save_as) {
            drawerMenuSaveAs()
        } else if (menuId == R.id.drawer_menu_merge_project) {
            drawerMenuMerge()
        } else if (menuId == R.id.drawer_menu_delete_project) {
            drawerMenuDeleteProject()
        }
        mDrawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    //    **********************************************************************************************
    //    Navigation view called methods
    //    **********************************************************************************************
    private fun drawerMenuSaveAs() {
        val builder = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.setText(project?.name)
        builder.setTitle("Save as")
            .setMessage("Enter new name :")
            .setView(editText)
            .setNegativeButton("CANCEL") { dialogInterface, i -> }
            .setPositiveButton("OK") { dialogInterface, i -> saveAs(editText.text.toString()) }
            .create()
            .show()
    }

    private fun drawerMenuNewProject() {
        project!!.save(this)
        UmlType.Companion.clearProjectUmlTypes()
        project = UmlProject("NewProject", this)
        mGraphView!!.setUmlProject(project)
        updateNavigationView()
    }

    private fun drawerMenuLoadProject() {
        project!!.save(this)
        val spinner = Spinner(this)
        spinner.adapter = projectDirectoryAdapter()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Load project")
            .setMessage("Choose project to load :")
            .setView(spinner)
            .setNegativeButton("CANCEL") { dialogInterface, i -> }
            .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int) {
                    val fileName = spinner.selectedItem.toString()
                    if (fileName != null) {
                        UmlType.Companion.clearProjectUmlTypes()
                        project = UmlProject.Companion.load(applicationContext, fileName)
                        mGraphView!!.setUmlProject(project)
                        updateNavigationView()
                    }
                }
            })
            .create()
            .show()
    }

    private fun drawerMenuDeleteProject() {
        val context: Context = this
        val spinner = Spinner(this)
        spinner.adapter = projectDirectoryAdapter()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete project")
            .setMessage("Choose project to delete :")
            .setView(spinner)
            .setNegativeButton("CANCEL") { dialogInterface, i -> }
            .setPositiveButton("OK") { dialogInterface, i ->
                val fileName = spinner.selectedItem.toString()
                if (fileName != null) {
                    val pathName = File(filesDir, UmlProject.Companion.PROJECT_DIRECTORY)
                    val file = File(pathName, fileName)
                    val alert = AlertDialog.Builder(context)
                    alert.setTitle("Delete Project")
                        .setMessage("Are you sure you want to delete $fileName ?")
                        .setNegativeButton("NO") { dialogInterface, i -> }
                        .setPositiveButton("YES") { dialogInterface, i -> file.delete() }
                        .create()
                        .show()
                }
            }
            .create()
            .show()
    }

    private fun drawerMenuMerge() {
        val spinner = Spinner(this)
        spinner.adapter = projectDirectoryAdapter()
        val currentContext: Context = this
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Merge project")
            .setMessage("Choose project to merge")
            .setView(spinner)
            .setNegativeButton("CANCEL") { dialogInterface, i -> }
            .setPositiveButton("OK") { dialogInterface, i ->
                val fileName = spinner.selectedItem.toString()
                if (fileName != null) {
                    val project: UmlProject =
                        UmlProject.Companion.load(applicationContext, fileName)!!
                    project.mergeWith(project)
                    mGraphView!!.invalidate()
                }
            }
            .create()
            .show()
    }

    private fun projectDirectoryAdapter(): ArrayAdapter<String?> {
        //Create an array adapter to set a spinner with all project file names
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, IOUtils.sortedFiles(
                File(
                    filesDir, UmlProject.Companion.PROJECT_DIRECTORY
                )
            )
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    //    **********************************************************************************************
    //    Option menu events
    //    **********************************************************************************************
    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val itemId = menuItem.itemId
        if (itemId == R.id.toolbar_menu_export) {
            if (sWriteExternalStoragePermission) menuItemExport()
        } else if (itemId == R.id.toolbar_menu_import) {
            if (sReadExternalStoragePermission) menuItemImport()
        } else if (itemId == R.id.toolbar_menu_create_custom_type) {
            menuCreateCustomType()
        } else if (itemId == R.id.toolbar_menu_delete_custom_types) {
            menuDeleteCustomTypes()
        } else if (itemId == R.id.toolbar_menu_export_custom_types) {
            if (sWriteExternalStoragePermission) menuExportCustomTypes()
        } else if (itemId == R.id.toolbar_menu_import_custom_types) {
            if (sReadExternalStoragePermission) menuImportCustomTypes()
        } else if (itemId == R.id.toolbar_menu_help) {
            menuHelp()
        }
        return true
    }

    //    **********************************************************************************************
    //    Menu item called methods
    //    **********************************************************************************************
    private fun menuItemExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "text/*"
        startActivityForResult(intent, INTENT_CREATE_DOCUMENT_EXPORT_PROJECT)
    }

    private fun menuItemImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        startActivityForResult(intent, INTENT_OPEN_DOCUMENT_IMPORT_PROJECT)
    }

    private fun menuCreateCustomType() {
        val editText = EditText(this)
        val context = applicationContext
        val adb = AlertDialog.Builder(this)
        adb.setTitle("Create custom type")
            .setMessage("Enter custom type name :")
            .setView(editText)
            .setNegativeButton("CANCEL") { dialogInterface, i -> }
            .setPositiveButton("OK") { dialogInterface, i ->
                val typeName = editText.text.toString()
                if (typeName == "") Toast.makeText(
                    context,
                    "Failed : name cannot be blank",
                    Toast.LENGTH_SHORT
                ).show() else if (UmlType.Companion.containsUmlTypeNamed(typeName)) Toast.makeText(
                    context,
                    "Failed : this name is already used",
                    Toast.LENGTH_SHORT
                ).show() else {
                    UmlType.Companion.createUmlType(typeName, TypeLevel.CUSTOM)
                    Toast.makeText(context, "Custom type created", Toast.LENGTH_SHORT).show()
                }
            }
            .create()
            .show()
    }

    private fun menuDeleteCustomTypes() {
        val listView = ListView(this)
        val listArray = mutableListOf<String>()
        for (t in UmlType.Companion.umlTypes) if (t!!.isCustomUmlType) listArray.add(t.name!!)
        Collections.sort(listArray, TypeNameComparator())
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, listArray)
        listView.choiceMode = AbsListView.CHOICE_MODE_MULTIPLE
        listView.adapter = adapter
        val adb = AlertDialog.Builder(this)
        adb.setTitle("Delete custom types")
            .setMessage("Check custom types to delete")
            .setView(listView)
            .setNegativeButton("CANCEL") { dialogInterface, i -> }
            .setPositiveButton("OK") { dialogInterface, i ->
                val checkMapping = listView.checkedItemPositions
                var t: UmlType
                for (j in 0 until checkMapping.size()) {
                    if (checkMapping.valueAt(j)) {
                        t = UmlType.Companion.valueOf(
                            listView.getItemAtPosition(
                                checkMapping.keyAt(j)
                            ).toString(), UmlType.Companion.umlTypes
                        )!!
                        UmlType.Companion.removeUmlType(t)
                        project!!.removeParametersOfType(t)
                        project!!.removeMethodsOfType(t)
                        project!!.removeAttributesOfType(t)
                        mGraphView!!.invalidate()
                    }
                }
            }
            .create()
            .show()
    }

    private fun menuExportCustomTypes() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "text/*"
        startActivityForResult(intent, INTENT_CREATE_DOCUMENT_EXPORT_CUSTOM_TYPES)
    }

    private fun menuImportCustomTypes() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        startActivityForResult(intent, INTENT_OPEN_DOCUMENT_IMPORT_CUSTOM_TYPES)
    }

    private fun menuHelp() {
        val adb = AlertDialog.Builder(this)
        adb.setTitle("Help")
            .setMessage(Html.fromHtml(IOUtils.readRawHtmlFile(this, R.raw.help_html)))
            .setPositiveButton("OK") { dialog, which -> }
            .create()
            .show()
    }

    //    **********************************************************************************************
    //    Intents
    //    **********************************************************************************************
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == INTENT_CREATE_DOCUMENT_EXPORT_PROJECT && resultCode == RESULT_OK) {
            val fileNameUri = data!!.data
            project!!.exportProject(this, fileNameUri)
        } else if (requestCode == INTENT_OPEN_DOCUMENT_IMPORT_PROJECT && resultCode == RESULT_OK) {
            val fileNameUri = data!!.data
            UmlType.Companion.clearProjectUmlTypes()
            project = UmlProject.Companion.importProject(this, fileNameUri)
            mGraphView!!.setUmlProject(project)
        } else if (requestCode == INTENT_CREATE_DOCUMENT_EXPORT_CUSTOM_TYPES && resultCode == RESULT_OK) {
            val fileNameUri = data!!.data
            UmlType.Companion.exportCustomUmlTypes(this, fileNameUri)
        } else if (requestCode == INTENT_OPEN_DOCUMENT_IMPORT_CUSTOM_TYPES && resultCode == RESULT_OK) {
            val fileNameUri = data!!.data
            UmlType.Companion.importCustomUmlTypes(this, fileNameUri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults[WRITE_EXTERNAL_STORAGE_INDEX] == PackageManager.PERMISSION_GRANTED) sWriteExternalStoragePermission =
            true else sWriteExternalStoragePermission = false
        if (requestCode == REQUEST_PERMISSION && grantResults[READ_EXTERNAL_STORAGE_INDEX] == PackageManager.PERMISSION_GRANTED) sReadExternalStoragePermission =
            true else sReadExternalStoragePermission = false
    }

    //    **********************************************************************************************
    //    Project management methods
    //    **********************************************************************************************
    private fun saveAs(projectName: String) {
        project?.name = (projectName)
        updateNavigationView()
        project!!.save(applicationContext)
    }

    //    **********************************************************************************************
    //    Check permissions
    //    **********************************************************************************************
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionString = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) requestPermissions(permissionString, REQUEST_PERMISSION)
        }
    }

    companion object {
        private var sWriteExternalStoragePermission = true
        private var sReadExternalStoragePermission = true
        private const val WRITE_EXTERNAL_STORAGE_INDEX = 0
        private const val READ_EXTERNAL_STORAGE_INDEX = 1
        private const val DOUBLE_BACK_PRESSED_DELAY: Long = 2000
        private const val GRAPH_FRAGMENT_TAG = "graphFragment"
        private const val CLASS_EDITOR_FRAGMENT_TAG = "classEditorFragment"
        private const val ATTRIBUTE_EDITOR_FRAGMENT_TAG = "attributeEditorFragment"
        private const val METHOD_EDITOR_FRAGMENT_TAG = "methodEditorFragment"
        private const val PARAMETER_EDITOR_FRAGMENT_TAG = "parameterEditorFragment"
        private const val SHARED_PREFERENCES_PROJECT_NAME = "sharedPreferencesProjectName"
        private const val INTENT_CREATE_DOCUMENT_EXPORT_PROJECT = 1000
        private const val INTENT_OPEN_DOCUMENT_IMPORT_PROJECT = 2000
        private const val INTENT_CREATE_DOCUMENT_EXPORT_CUSTOM_TYPES = 3000
        private const val INTENT_OPEN_DOCUMENT_IMPORT_CUSTOM_TYPES = 4000
        private const val REQUEST_PERMISSION = 5000
    }
}