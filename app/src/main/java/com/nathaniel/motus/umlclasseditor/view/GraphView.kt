package com.nathaniel.motus.umlclasseditor.view

import android.view.View.OnTouchListener
import com.nathaniel.motus.umlclasseditor.view.GraphFragment
import com.nathaniel.motus.umlclasseditor.model.UmlProject
import com.nathaniel.motus.umlclasseditor.view.GraphView.TouchMode
import com.nathaniel.motus.umlclasseditor.model.UmlClass
import com.nathaniel.motus.umlclasseditor.view.GraphView.GraphViewObserver
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
import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.BaseExpandableListAdapter

class GraphView : View, OnTouchListener {
    internal enum class TouchMode {
        DRAG, ZOOM
    }

    private var mGraphFragment: GraphFragment? = null
    private var mZoom = 0f
    private var mXOffset = 0f
    private var mYOffset = 0f
    private var mUmlProject: UmlProject? = null
    private var mLastTouchX = 0f
    private var mLastTouchY = 0f
    private var mOldDist = 0f
    private var mNewDist = 0f
    private var mTouchMode = TouchMode.DRAG
    private var mPrimaryPointerIndex = 0
    private var mXMidPoint = 0f
    private var mYMidpoint = 0f
    private var mOldXMidPoint = 0f
    private var mOldYMidPoint = 0f
    private var plainTextPaint: Paint? = null
    private var italicTextPaint: Paint? = null
    private var underlinedTextPaint: Paint? = null
    private var linePaint: Paint? = null
    private var dashPaint: Paint? = null
    private var solidBlackPaint: Paint? = null
    private var solidWhitePaint: Paint? = null
    private var mMovingClass: UmlClass? = null
    private var mCallback: GraphViewObserver? = null
    private var mActionDownEventTime: Long = 0
    private var mFirstClickTime: Long = 0
    private var mFirstClickX = 0f
    private var mFirstClickY = 0f

    //    **********************************************************************************************
    //    Constructors
    //    **********************************************************************************************
    constructor(context: Context?) : super(context) {
        init(-1f, -1, -1)
    }

    constructor(context: Context?, zoom: Float, xOffset: Int, yOffset: Int) : super(context) {
        init(zoom, xOffset, yOffset)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    private fun init(zoom: Float, xOffset: Int, yOffset: Int) {
        mZoom = if (zoom != -1f) zoom else 1f
        mXOffset = if (xOffset != -1) xOffset.toFloat() else 0f
        mYOffset = if (yOffset != -1) yOffset.toFloat() else 0f
        plainTextPaint = Paint()
        plainTextPaint!!.color = Color.DKGRAY
        italicTextPaint = Paint()
        italicTextPaint!!.color = Color.DKGRAY
        italicTextPaint!!.typeface = Typeface.defaultFromStyle(Typeface.ITALIC)
        underlinedTextPaint = Paint()
        underlinedTextPaint!!.color = Color.DKGRAY
        underlinedTextPaint!!.flags = Paint.UNDERLINE_TEXT_FLAG
        linePaint = Paint()
        linePaint!!.color = Color.DKGRAY
        linePaint!!.style = Paint.Style.STROKE
        dashPaint = Paint()
        dashPaint!!.color = Color.DKGRAY
        dashPaint!!.style = Paint.Style.STROKE
        dashPaint!!.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0F)
        solidBlackPaint = Paint()
        solidBlackPaint!!.color = Color.DKGRAY
        solidBlackPaint!!.style = Paint.Style.FILL_AND_STROKE
        solidWhitePaint = Paint()
        solidWhitePaint!!.color = Color.WHITE
        solidWhitePaint!!.style = Paint.Style.FILL
        setOnTouchListener(this)
        createCallbackToParentActivity()
    }

    private fun init(attrs: AttributeSet?) {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.GraphView)
        val zoom = attr.getFloat(R.styleable.GraphView_zoom, -1f)
        val xOffset = attr.getInt(R.styleable.GraphView_xOffset, -1)
        val yOffset = attr.getInt(R.styleable.GraphView_yOffset, -1)
        init(zoom, xOffset, yOffset)
    }

    //    **********************************************************************************************
    //    Callback interface
    //    **********************************************************************************************
    interface GraphViewObserver {
        val isExpectingTouchLocation: Boolean
        fun createClass(xLocation: Float, yLocation: Float)
        fun editClass(umlClass: UmlClass?)
        fun createRelation(
            startClass: UmlClass?,
            endClass: UmlClass?,
            relationType: UmlRelationType?
        )
    }

    //    **********************************************************************************************
    //    Getters and setter
    //    **********************************************************************************************
    fun setUmlProject(umlProject: UmlProject?) {
        mUmlProject = umlProject
        mZoom = mUmlProject?.zoom!!
        mXOffset = mUmlProject?.xOffset!!
        mYOffset = mUmlProject?.yOffset!!
        this.invalidate()
    }

    fun setGraphFragment(graphFragment: GraphFragment?) {
        mGraphFragment = graphFragment
    }

    fun setZoom(zoom: Float) {
        mZoom = zoom
    }

    fun setXOffset(XOffset: Float) {
        mXOffset = XOffset
    }

    fun setYOffset(YOffset: Float) {
        mYOffset = YOffset
    }

    //    **********************************************************************************************
    //    Overridden methods
    //    **********************************************************************************************
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        updateProjectGeometricalParameters()
        for (c in mUmlProject?.umlClasses!!) drawUmlClass(canvas, c)
        for (r in mUmlProject?.umlRelations!!) drawRelation(canvas, r)
    }

    //    **********************************************************************************************
    //    Drawing methods
    //    **********************************************************************************************
    fun drawUmlClass(canvas: Canvas, umlClass: UmlClass?) {
        plainTextPaint!!.textSize = FONT_SIZE * mZoom
        italicTextPaint!!.textSize = FONT_SIZE * mZoom
        underlinedTextPaint!!.textSize = FONT_SIZE * mZoom

        //Update class dimensions
        updateUmlClassNormalDimensions(umlClass)
        drawUmlClassHeader(canvas, umlClass)
        if (umlClass?.umlClassType == UmlClassType.ENUM) drawValueBox(canvas, umlClass) else {
            drawAttributeBox(canvas, umlClass)
            drawMethodBox(canvas, umlClass)
        }
    }

    private fun drawUmlClassHeader(canvas: Canvas, umlClass: UmlClass?) {
        canvas.drawRect(
            visibleX(umlClass!!.umlClassNormalXPos),
            visibleY(umlClass.umlClassNormalYPos),
            visibleX(umlClass.umlClassNormalXPos + umlClass.umlClassNormalWidth),
            visibleY(umlClass.umlClassNormalYPos + getClassHeaderNormalHeight(umlClass)),
            linePaint!!
        )
        when (umlClass.umlClassType) {
            UmlClassType.INTERFACE -> {
                canvas.drawText(
                    "<< Interface >>",
                    visibleX(
                        umlClass.umlClassNormalXPos + (umlClass.umlClassNormalWidth - getTextNormalWidth(
                            "<< Interface >>"
                        )) / 2
                    ),
                    visibleY(umlClass.umlClassNormalYPos + FONT_SIZE + INTERLINE),
                    plainTextPaint!!
                )
                canvas.drawText(
                    umlClass.name!!,
                    visibleX(
                        umlClass.umlClassNormalXPos + (umlClass.umlClassNormalWidth - getTextNormalWidth(
                            umlClass.name
                        )) / 2
                    ),
                    visibleY(umlClass.umlClassNormalYPos + FONT_SIZE * 2 + INTERLINE * 2),
                    plainTextPaint!!
                )
            }
            UmlClassType.ABSTRACT_CLASS -> canvas.drawText(
                umlClass.name!!,
                visibleX(
                    umlClass.umlClassNormalXPos + (umlClass.umlClassNormalWidth - getTextNormalWidth(
                        umlClass.name
                    )) / 2
                ),
                visibleY(umlClass.umlClassNormalYPos + FONT_SIZE + INTERLINE),
                italicTextPaint!!
            )
            else -> canvas.drawText(
                umlClass.name!!,
                visibleX(
                    umlClass.umlClassNormalXPos + (umlClass.umlClassNormalWidth - getTextNormalWidth(
                        umlClass.name
                    )) / 2
                ),
                visibleY(umlClass.umlClassNormalYPos + FONT_SIZE + INTERLINE),
                plainTextPaint!!
            )
        }
    }

    private fun drawAttributeBox(canvas: Canvas, umlClass: UmlClass?) {
        canvas.drawRect(
            visibleX(umlClass?.umlClassNormalXPos!!),
            visibleY(umlClass.umlClassNormalYPos + getClassHeaderNormalHeight(umlClass)),
            visibleX(umlClass.umlClassNormalXPos + umlClass.umlClassNormalWidth),
            visibleY(
                umlClass.umlClassNormalYPos + getClassHeaderNormalHeight(umlClass) + getAttributeBoxNormalHeight(
                    umlClass
                )
            ),
            linePaint!!
        )
        var currentY =
            umlClass.umlClassNormalYPos + getClassHeaderNormalHeight(umlClass) + INTERLINE + FONT_SIZE
        for (a in umlClass.attributes) {
            if (a!!.isStatic) canvas.drawText(
                a.attributeCompleteString,
                visibleX(umlClass.umlClassNormalXPos + INTERLINE),
                visibleY(currentY),
                underlinedTextPaint!!
            ) else canvas.drawText(
                a.attributeCompleteString,
                visibleX(umlClass.umlClassNormalXPos + INTERLINE),
                visibleY(currentY),
                plainTextPaint!!
            )
            currentY = currentY + FONT_SIZE + INTERLINE
        }
    }

    private fun drawMethodBox(canvas: Canvas, umlClass: UmlClass?) {
        canvas.drawRect(
            visibleX(umlClass?.umlClassNormalXPos!!),
            visibleY(
                umlClass?.umlClassNormalYPos + getClassHeaderNormalHeight(umlClass) + getAttributeBoxNormalHeight(
                    umlClass
                )
            ),
            visibleX(umlClass.umlClassNormalXPos + umlClass.umlClassNormalWidth),
            visibleY(
                umlClass.umlClassNormalYPos + getClassHeaderNormalHeight(umlClass) + getAttributeBoxNormalHeight(
                    umlClass
                ) + getMethodBoxNormalHeight(umlClass)
            ),
            linePaint!!
        )
        var currentY =
            umlClass.umlClassNormalYPos + getClassHeaderNormalHeight(umlClass) + getAttributeBoxNormalHeight(
                umlClass
            ) + INTERLINE + FONT_SIZE
        for (m in umlClass!!.methods) {
            if (m!!.isStatic) canvas.drawText(
                m.methodCompleteString,
                visibleX(umlClass.umlClassNormalXPos + INTERLINE),
                visibleY(currentY),
                underlinedTextPaint!!
            ) else canvas.drawText(
                m.methodCompleteString,
                visibleX(umlClass.umlClassNormalXPos + INTERLINE),
                visibleY(currentY),
                plainTextPaint!!
            )
            currentY = currentY + FONT_SIZE + INTERLINE
        }
    }

    private fun drawValueBox(canvas: Canvas, umlClass: UmlClass?) {
        canvas.drawRect(
            visibleX(umlClass?.umlClassNormalXPos!!),
            visibleY(umlClass.umlClassNormalYPos + getClassHeaderNormalHeight(umlClass)),
            visibleX(umlClass.umlClassNormalXPos + umlClass.umlClassNormalWidth),
            visibleY(
                umlClass.umlClassNormalYPos + getClassHeaderNormalHeight(umlClass) + getValueBoxNormalHeight(
                    umlClass
                )
            ),
            linePaint!!
        )
        var currentY =
            umlClass.umlClassNormalYPos + getClassHeaderNormalHeight(umlClass) + INTERLINE + FONT_SIZE
        for (v in umlClass.values) {
            canvas.drawText(
                v!!.name!!,
                visibleX(umlClass.umlClassNormalXPos + INTERLINE),
                visibleY(currentY),
                plainTextPaint!!
            )
            currentY = currentY + FONT_SIZE + INTERLINE
        }
    }

    private fun drawRelation(canvas: Canvas, umlRelation: UmlRelation?) {
        val originAbsoluteLeft = umlRelation!!.relationOriginClass!!.umlClassNormalXPos
        val originAbsoluteRight = umlRelation.relationOriginClass!!.normalRightEnd
        val originAbsoluteTop = umlRelation.relationOriginClass!!.umlClassNormalYPos
        val originAbsoluteBottom = umlRelation.relationOriginClass!!.normalBottomEnd
        val endAbsoluteLeft = umlRelation.relationEndClass!!.umlClassNormalXPos
        val endAbsoluteRight = umlRelation.relationEndClass!!.normalRightEnd
        val endAbsoluteTop = umlRelation.relationEndClass!!.umlClassNormalYPos
        val endAbsoluteBottom = umlRelation.relationEndClass!!.normalBottomEnd
        var absoluteXOrigin = 0f
        var absoluteYOrigin = 0f
        var absoluteXEnd = 0f
        var absoluteYEnd = 0f

        //End in South quarter of Origin
        if (umlRelation?.relationEndClass?.isSouthOf(umlRelation.relationOriginClass)!!) {
            val lowerXLimit =
                originAbsoluteLeft - endAbsoluteTop + originAbsoluteBottom - umlRelation.relationEndClass!!.umlClassNormalWidth
            val upperXLimit = originAbsoluteRight + endAbsoluteTop - originAbsoluteBottom
            absoluteXEnd = endAbsoluteRight -
                    umlRelation.relationEndClass?.umlClassNormalWidth!! / (upperXLimit - lowerXLimit) *
                    (endAbsoluteLeft - lowerXLimit)
            absoluteYEnd = endAbsoluteTop
            absoluteXOrigin = originAbsoluteLeft +
                    umlRelation.relationOriginClass?.umlClassNormalWidth!! / (upperXLimit - lowerXLimit) *
                    (endAbsoluteLeft - lowerXLimit)
            absoluteYOrigin = originAbsoluteBottom
        }

        //End in North quarter or Origin
        if (umlRelation.relationEndClass?.isNorthOf(umlRelation.relationOriginClass)!!) {
            val lowerXLimit =
                originAbsoluteLeft - originAbsoluteTop + endAbsoluteBottom - umlRelation.relationEndClass!!.umlClassNormalWidth
            val upperXLimit = originAbsoluteRight + originAbsoluteTop - endAbsoluteBottom
            absoluteXEnd = endAbsoluteRight -
                    umlRelation.relationEndClass?.umlClassNormalWidth!! / (upperXLimit - lowerXLimit) *
                    (endAbsoluteLeft - lowerXLimit)
            absoluteYEnd = endAbsoluteBottom
            absoluteXOrigin = originAbsoluteLeft +
                    umlRelation.relationOriginClass?.umlClassNormalWidth!! / (upperXLimit - lowerXLimit) *
                    (endAbsoluteLeft - lowerXLimit)
            absoluteYOrigin = originAbsoluteTop
        }

        //End in West quarter of Origin
        if (umlRelation?.relationEndClass!!.isWestOf(umlRelation.relationOriginClass)) {
            val lowerYLimit =
                originAbsoluteTop - originAbsoluteLeft + endAbsoluteRight - umlRelation?.relationEndClass!!.umlClassNormalHeight
            val upperYLimit = originAbsoluteBottom + originAbsoluteLeft - endAbsoluteRight
            absoluteXEnd = endAbsoluteRight
            absoluteYEnd = endAbsoluteBottom -
                    umlRelation?.relationEndClass!!.umlClassNormalHeight / (upperYLimit - lowerYLimit) *
                    (endAbsoluteTop - lowerYLimit)
            absoluteXOrigin = originAbsoluteLeft
            absoluteYOrigin = originAbsoluteTop +
                    umlRelation?.relationOriginClass!!.umlClassNormalHeight / (upperYLimit - lowerYLimit) *
                    (endAbsoluteTop - lowerYLimit)
        }

        //End in East quarter of Origin
        if (umlRelation?.relationEndClass!!.isEastOf(umlRelation?.relationOriginClass)) {
            val lowerYLimit =
                originAbsoluteTop - endAbsoluteLeft + originAbsoluteRight - umlRelation?.relationEndClass?.umlClassNormalHeight!!
            val upperYLimit = originAbsoluteBottom + endAbsoluteLeft - originAbsoluteRight
            absoluteXEnd = endAbsoluteLeft
            absoluteYEnd = endAbsoluteBottom -
                    umlRelation.relationEndClass!!.umlClassNormalHeight / (upperYLimit - lowerYLimit) *
                    (endAbsoluteTop - lowerYLimit)
            absoluteXOrigin = originAbsoluteRight
            absoluteYOrigin = originAbsoluteTop +
                    umlRelation.relationOriginClass!!.umlClassNormalHeight / (upperYLimit - lowerYLimit) *
                    (endAbsoluteTop - lowerYLimit)
        }
        //update relation coordinates
        umlRelation.xOrigin = (absoluteXOrigin)
        umlRelation.yOrigin = (absoluteYOrigin)
        umlRelation.xEnd = (absoluteXEnd)
        umlRelation.yEnd = (absoluteYEnd)
        val path = Path()
        path.moveTo(visibleX(absoluteXOrigin), visibleY(absoluteYOrigin))
        path.lineTo(visibleX(absoluteXEnd), visibleY(absoluteYEnd))
        when (umlRelation.umlRelationType) {
            UmlRelationType.INHERITANCE -> {
                canvas.drawPath(path, linePaint!!)
                drawSolidWhiteArrow(
                    canvas,
                    visibleX(absoluteXOrigin),
                    visibleY(absoluteYOrigin),
                    visibleX(absoluteXEnd),
                    visibleY(absoluteYEnd)
                )
            }
            UmlRelationType.ASSOCIATION -> canvas.drawPath(path, linePaint!!)
            UmlRelationType.AGGREGATION -> {
                canvas.drawPath(path, linePaint!!)
                drawSolidWhiteRhombus(
                    canvas,
                    visibleX(absoluteXOrigin),
                    visibleY(absoluteYOrigin),
                    visibleX(absoluteXEnd),
                    visibleY(absoluteYEnd)
                )
            }
            UmlRelationType.COMPOSITION -> {
                canvas.drawPath(path, linePaint!!)
                drawSolidBlackRhombus(
                    canvas,
                    visibleX(absoluteXOrigin),
                    visibleY(absoluteYOrigin),
                    visibleX(absoluteXEnd),
                    visibleY(absoluteYEnd)
                )
            }
            UmlRelationType.DEPENDENCY -> {
                canvas.drawPath(path, dashPaint!!)
                drawArrow(
                    canvas,
                    visibleX(absoluteXOrigin),
                    visibleY(absoluteYOrigin),
                    visibleX(absoluteXEnd),
                    visibleY(absoluteYEnd)
                )
            }
            UmlRelationType.REALIZATION -> {
                canvas.drawPath(path, dashPaint!!)
                drawSolidWhiteArrow(
                    canvas,
                    visibleX(absoluteXOrigin),
                    visibleY(absoluteYOrigin),
                    visibleX(absoluteXEnd),
                    visibleY(absoluteYEnd)
                )
            }
            else -> {
            }
        }
    }

    private fun drawArrow(
        canvas: Canvas,
        xOrigin: Float,
        yOrigin: Float,
        xEnd: Float,
        yEnd: Float
    ) {
        //draw an arrow at the end of the segment
        canvas.save()
        canvas.rotate(getAngle(xEnd, yEnd, xOrigin, yOrigin), xEnd, yEnd)
        val path = Path()
        path.moveTo(xEnd + ARROW_SIZE * mZoom, yEnd - ARROW_SIZE * 1.414f / 2f * mZoom)
        path.lineTo(xEnd, yEnd)
        path.lineTo(xEnd + ARROW_SIZE * mZoom, yEnd + ARROW_SIZE * 1.414f / 2f * mZoom)
        canvas.drawPath(path, linePaint!!)
        canvas.restore()
    }

    private fun drawSolidWhiteArrow(
        canvas: Canvas,
        xOrigin: Float,
        yOrigin: Float,
        xEnd: Float,
        yEnd: Float
    ) {
        //draw a solid white arrow at the end of the segment
        canvas.save()
        canvas.rotate(getAngle(xEnd, yEnd, xOrigin, yOrigin), xEnd, yEnd)
        val path = Path()
        path.moveTo(xEnd, yEnd)
        path.lineTo(xEnd + ARROW_SIZE * mZoom, yEnd - ARROW_SIZE * 1.414f / 2f * mZoom)
        path.lineTo(xEnd + ARROW_SIZE * mZoom, yEnd + ARROW_SIZE * 1.414f / 2f * mZoom)
        path.close()
        canvas.drawPath(path, solidWhitePaint!!)
        canvas.drawPath(path, linePaint!!)
        canvas.restore()
    }

    private fun drawSolidWhiteRhombus(
        canvas: Canvas,
        xOrigin: Float,
        yOrigin: Float,
        xEnd: Float,
        yEnd: Float
    ) {
        //draw a solid white rhombus at the end of the segment
        canvas.save()
        canvas.rotate(getAngle(xEnd, yEnd, xOrigin, yOrigin), xEnd, yEnd)
        val path = Path()
        path.moveTo(xEnd, yEnd)
        path.lineTo(xEnd + ARROW_SIZE * mZoom, yEnd - ARROW_SIZE * 1.414f / 2f * mZoom)
        path.lineTo(xEnd + ARROW_SIZE * 2f * mZoom, yEnd)
        path.lineTo(xEnd + ARROW_SIZE * mZoom, yEnd + ARROW_SIZE * 1.414f / 2f * mZoom)
        path.close()
        canvas.drawPath(path, solidWhitePaint!!)
        canvas.drawPath(path, linePaint!!)
        canvas.restore()
    }

    private fun drawSolidBlackRhombus(
        canvas: Canvas,
        xOrigin: Float,
        yOrigin: Float,
        xEnd: Float,
        yEnd: Float
    ) {
        //draw a solid black rhombus at the end of the segment
        canvas.save()
        canvas.rotate(getAngle(xEnd, yEnd, xOrigin, yOrigin), xEnd, yEnd)
        val path = Path()
        path.moveTo(xEnd, yEnd)
        path.lineTo(xEnd + ARROW_SIZE * mZoom, yEnd - ARROW_SIZE * 1.414f / 2f * mZoom)
        path.lineTo(xEnd + ARROW_SIZE * 2f * mZoom, yEnd)
        path.lineTo(xEnd + ARROW_SIZE * mZoom, yEnd + ARROW_SIZE * 1.414f / 2f * mZoom)
        path.close()
        canvas.drawPath(path, solidBlackPaint!!)
        canvas.restore()
    }

    //    **********************************************************************************************
    //    UI events
    //    **********************************************************************************************
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mActionDownEventTime = event.eventTime
                mLastTouchX = event.x
                mLastTouchY = event.y
                mMovingClass = getTouchedClass(mLastTouchX, mLastTouchY)
                mTouchMode = TouchMode.DRAG
                mPrimaryPointerIndex = 0
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mOldDist = spacing(event)
                calculateMidPoint(event)
                mOldXMidPoint = mXMidPoint
                mOldYMidPoint = mYMidpoint
                if (mOldDist > 10f) {
                    mTouchMode = TouchMode.ZOOM
                    mMovingClass = null
                }
            }
            MotionEvent.ACTION_MOVE -> if (mTouchMode == TouchMode.DRAG) {
                if (mMovingClass == null) {
                    mXOffset = mXOffset + event.x - mLastTouchX
                    mYOffset = mYOffset + event.y - mLastTouchY
                } else {
                    mMovingClass!!.umlClassNormalXPos = (mMovingClass?.umlClassNormalXPos!! + (event.x - mLastTouchX) / mZoom)
                    mMovingClass!!.umlClassNormalYPos = (mMovingClass?.umlClassNormalYPos!! + (event.y - mLastTouchY) / mZoom)
                }
                mLastTouchX = event.x
                mLastTouchY = event.y
            } else if (mTouchMode == TouchMode.ZOOM) {
                mNewDist = spacing(event)
                mZoom = mZoom * mNewDist / mOldDist
                calculateMidPoint(event)
                mXOffset = mXMidPoint + (mXOffset - mOldXMidPoint) * mNewDist / mOldDist
                mYOffset = mYMidpoint + (mYOffset - mOldYMidPoint) * mNewDist / mOldDist
                mOldDist = mNewDist
                mOldXMidPoint = mXMidPoint
                mOldYMidPoint = mYMidpoint
            }
            MotionEvent.ACTION_POINTER_UP -> {
                mTouchMode = TouchMode.DRAG
                if (event.actionIndex == mPrimaryPointerIndex) {
                    mPrimaryPointerIndex = (1 + mPrimaryPointerIndex) % 2
                }
                mLastTouchX = event.getX(mPrimaryPointerIndex)
                mLastTouchY = event.getY(mPrimaryPointerIndex)
            }
            MotionEvent.ACTION_UP -> {

                //double click
                if (event.eventTime - mActionDownEventTime <= CLICK_DELAY && event.eventTime - mFirstClickTime <= DOUBLE_CLICK_DELAY && distance(
                        mLastTouchX,
                        mLastTouchY,
                        mFirstClickX,
                        mFirstClickY
                    ) <= DOUBLE_CLICK_DISTANCE_MAX
                ) {
                    if (getTouchedClass(mLastTouchX, mLastTouchY) != null) mCallback!!.editClass(
                        getTouchedClass(mLastTouchX, mLastTouchY)
                    ) else if (getTouchedRelation(mLastTouchX, mLastTouchY) != null) {
                        promptDeleteRelation(getTouchedRelation(mLastTouchX, mLastTouchY), this)
                    } else adjustViewToProject()
                }

                //simple click
                if (event.eventTime - mActionDownEventTime <= CLICK_DELAY) {
                    mFirstClickTime = event.eventTime
                    mFirstClickX = event.x
                    mFirstClickY = event.y

                    //locate new class
                    if (mGraphFragment!!.isExpectingTouchLocation) {
                        mGraphFragment!!.isExpectingTouchLocation = (false)
                        mGraphFragment!!.clearPrompt()
                        mCallback!!.createClass(absoluteX(mLastTouchX), absoluteY(mLastTouchY))

                        //touch relation end class
                    } else if (mGraphFragment!!.isExpectingEndClass
                        && getTouchedClass(mLastTouchX, mLastTouchY) != null && getTouchedClass(
                            mLastTouchX,
                            mLastTouchY
                        ) !== mGraphFragment!!.startClass
                    ) {
                        mGraphFragment!!.endClass = (getTouchedClass(mLastTouchX, mLastTouchY))
                        mGraphFragment!!.isExpectingEndClass = (false)
                        mGraphFragment!!.clearPrompt()
                        mCallback!!.createRelation(
                            mGraphFragment!!.startClass,
                            mGraphFragment!!.endClass,
                            mGraphFragment!!.umlRelationType
                        )

                        //touch relation origin class
                    } else if (mGraphFragment!!.isExpectingStartClass && getTouchedClass(
                            mLastTouchX,
                            mLastTouchY
                        ) != null
                    ) {
                        mGraphFragment!!.startClass = (getTouchedClass(mLastTouchX, mLastTouchY))
                        mGraphFragment!!.isExpectingStartClass = (false)
                        mGraphFragment!!.isExpectingEndClass = (true)
                        mGraphFragment!!.setPrompt("Choose end class")
                    }
                }
            }
            else -> return false
        }
        invalidate()
        return true
    }

    //    **********************************************************************************************
    //    Initialization methods
    //    **********************************************************************************************
    private fun createCallbackToParentActivity() {
        mCallback = this.context as GraphViewObserver
    }

    //    **********************************************************************************************
    //    Calculation methods
    //    **********************************************************************************************
    private fun spacing(event: MotionEvent): Float {
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    private fun calculateMidPoint(event: MotionEvent) {
        mXMidPoint = (event.getX(0) + event.getX(1)) / 2
        mYMidpoint = (event.getY(0) + event.getY(1)) / 2
    }

    private fun updateUmlClassNormalDimensions(umlClass: UmlClass?) {
        //you must use the actual dimension normalized at zoom=1
        //because text width is not a linear function of zoom
//        plainTextPaint.setTextSize(FONT_SIZE*mZoom);
//        umlClass.setUmlClassNormalWidth((getUmlClassMaxTextWidth(umlClass, plainTextPaint)+INTERLINE*2f*mZoom)/mZoom);
//        umlClass.setUmlClassNormalHeight(INTERLINE*3f+(FONT_SIZE+INTERLINE)*(1f+umlClass.getAttributeList().size()+umlClass.getMethodList().size()));
        when (umlClass!!.umlClassType) {
            UmlClassType.ENUM -> {
                umlClass.umlClassNormalWidth = (
                    Math.max(
                        getClassHeaderNormalWidth(umlClass),
                        getValueBoxNormalWidth(umlClass)
                    )
                )
                umlClass.umlClassNormalHeight = (
                    getClassHeaderNormalHeight(umlClass) + getValueBoxNormalHeight(
                        umlClass
                    )
                )
            }
            else -> {
                var currentWidth = 0f
                if (getClassHeaderNormalWidth(umlClass) > currentWidth) currentWidth =
                    getClassHeaderNormalWidth(umlClass)
                if (getAttributeBoxNormalWidth(umlClass) > currentWidth) currentWidth =
                    getAttributeBoxNormalWidth(umlClass)
                if (getMethodBoxNormalWidth(umlClass) > currentWidth) currentWidth =
                    getMethodBoxNormalWidth(umlClass)
                umlClass.umlClassNormalWidth = (currentWidth)
                umlClass.umlClassNormalHeight = (
                    getClassHeaderNormalHeight(umlClass) + getAttributeBoxNormalHeight(
                        umlClass
                    ) + getMethodBoxNormalHeight(umlClass)
                )
            }
        }
    }

    private fun getClassHeaderNormalWidth(umlClass: UmlClass?): Float {
        //you may have to take into account <<interface>>
        return when (umlClass!!.umlClassType) {
            UmlClassType.INTERFACE -> Math.max(
                getTextNormalWidth("<< Interface >>"),
                getTextNormalWidth(umlClass.name)
            ) + 2 * INTERLINE
            else -> getTextNormalWidth(umlClass.name) + 2 * INTERLINE
        }
    }

    private fun getClassHeaderNormalHeight(umlClass: UmlClass?): Float {
        return when (umlClass!!.umlClassType) {
            UmlClassType.INTERFACE -> FONT_SIZE * 2 + 3 * INTERLINE
            else -> FONT_SIZE + 2 * INTERLINE
        }
    }

    private fun getAttributeBoxNormalWidth(umlClass: UmlClass?): Float {
        var currentWidth = 0f
        for (a in umlClass!!.attributes) if (getTextNormalWidth(a!!.attributeCompleteString) > currentWidth) currentWidth =
            getTextNormalWidth(a.attributeCompleteString)
        return currentWidth + 2 * INTERLINE
    }

    private fun getAttributeBoxNormalHeight(umlClass: UmlClass?): Float {
        return umlClass!!.attributes.size * FONT_SIZE + (umlClass!!.attributes.size + 1) * INTERLINE
    }

    private fun getMethodBoxNormalWidth(umlClass: UmlClass?): Float {
        var currentWidth = 0f
        for (m in umlClass!!.methods) if (getTextNormalWidth(m!!.methodCompleteString) > currentWidth) currentWidth =
            getTextNormalWidth(m.methodCompleteString)
        return currentWidth + 2 * INTERLINE
    }

    private fun getMethodBoxNormalHeight(umlClass: UmlClass?): Float {
        return umlClass!!.methods.size * FONT_SIZE + (umlClass.methods.size + 1) * INTERLINE
    }

    private fun getValueBoxNormalWidth(umlClass: UmlClass?): Float {
        var currentWidth = 0f
        for (v in umlClass!!.values) if (getTextNormalWidth(v!!.name) > currentWidth) currentWidth =
            getTextNormalWidth(v.name)
        return currentWidth + 2 * INTERLINE
    }

    private fun getValueBoxNormalHeight(umlClass: UmlClass?): Float {
        return umlClass!!.values.size * FONT_SIZE + (umlClass.values.size + 1) * INTERLINE
    }

    private fun getTextNormalWidth(text: String?): Float {
        plainTextPaint!!.textSize = FONT_SIZE * mZoom
        return plainTextPaint!!.measureText(text) / mZoom
    }

    private fun getAngle(xOrigin: Float, yOrigin: Float, xEnd: Float, yEnd: Float): Float {
        //calculate angle between segment and horizontal
        return (Math.copySign(
            Math.abs(
                Math.acos(
                    (xEnd - xOrigin) / Math.sqrt(
                        ((xEnd - xOrigin) * (xEnd - xOrigin) + (yEnd - yOrigin) * (yEnd - yOrigin)).toDouble()
                    )
                )
            ), (yEnd - yOrigin).toDouble()
        ) /
                Math.PI * 180f).toFloat()
    }

    //return the length of the rectangle that can contain all the project
    private val absoluteProjectWidth: Float
        private get() {
            //return the length of the rectangle that can contain all the project
            var minX = 1000000f
            var maxX = -1000000f
            for (c in mUmlProject!!.umlClasses) {
                minX = Math.min(c!!.umlClassNormalXPos, minX)
                maxX = Math.max(c.normalRightEnd, maxX)
            }
            return maxX - minX
        }

    //return the height of the rectangle that can contain all the project
    private val absoluteProjectHeight: Float
        private get() {
            //return the height of the rectangle that can contain all the project
            var minY = 1000000f
            var maxY = -1000000f
            for (c in mUmlProject!!.umlClasses) {
                minY = Math.min(c!!.umlClassNormalYPos, minY)
                maxY = Math.max(c.normalBottomEnd, maxY)
            }
            return maxY - minY
        }
    private val absoluteProjectLeft: Float
        private get() {
            var minX = 1000000f
            for (c in mUmlProject!!.umlClasses) minX = Math.min(c!!.umlClassNormalXPos, minX)
            return minX
        }
    private val absoluteProjectRight: Float
        private get() {
            var maxX = -1000000f
            for (c in mUmlProject!!.umlClasses) maxX = Math.max(c!!.normalRightEnd, maxX)
            return maxX
        }
    private val absoluteProjectTop: Float
        private get() {
            var minY = 1000000f
            for (c in mUmlProject!!.umlClasses) minY = Math.min(c!!.umlClassNormalYPos, minY)
            return minY
        }
    private val absoluteProjectBottom: Float
        private get() {
            var maxY = -1000000f
            for (c in mUmlProject!!.umlClasses) maxY = Math.max(c!!.normalBottomEnd, maxY)
            return maxY
        }

    private fun adjustViewToProject() {
        val xZoom = this.measuredWidth / absoluteProjectWidth
        val yZoom = this.measuredHeight / absoluteProjectHeight
        if (xZoom <= yZoom) {
            mZoom = xZoom
            mXOffset = -absoluteProjectLeft * mZoom
            mYOffset =
                -absoluteProjectTop * mZoom + (this.measuredHeight - absoluteProjectHeight * mZoom) / 2f
        } else {
            mZoom = yZoom
            mYOffset = -absoluteProjectTop * mZoom
            mXOffset =
                -absoluteProjectLeft * mZoom + (this.measuredWidth - absoluteProjectWidth * mZoom) / 2f
        }
        this.invalidate()
    }

    //    **********************************************************************************************
    //    Coordinates transformations
    //    "visible" refers to the screen referential
    //    "absolute" refers to the absolute referential, whose coordinates are class attributes
    //    **********************************************************************************************
    private fun visibleX(absoluteX: Float): Float {
        return mXOffset + mZoom * absoluteX
    }

    private fun visibleY(absoluteY: Float): Float {
        return mYOffset + mZoom * absoluteY
    }

    private fun absoluteX(visibleX: Float): Float {
        return (visibleX - mXOffset) / mZoom
    }

    private fun absoluteY(visibleY: Float): Float {
        return (visibleY - mYOffset) / mZoom
    }

    //    **********************************************************************************************
    //    Other methods
    //    **********************************************************************************************
    private fun getTouchedClass(visibleX: Float, visibleY: Float): UmlClass? {
        for (c in mUmlProject!!.umlClasses) {
            if (c!!.containsPoint(absoluteX(visibleX), absoluteY(visibleY))) return c
        }
        return null
    }

    fun getTouchedRelation(visibleX: Float, visibleY: Float): UmlRelation? {
        for (r in mUmlProject!!.umlRelations) {
            if (distance(
                    absoluteX(visibleX),
                    absoluteY(visibleY),
                    r!!.xOrigin,
                    r.yOrigin,
                    r.xEnd,
                    r.yEnd
                ) <= 20 && absoluteX(visibleX) >= Math.min(r.xOrigin, r.xEnd) - 20 && absoluteX(
                    visibleX
                ) <= Math.max(r.xOrigin, r.xEnd) + 20 && absoluteY(visibleY) >= Math.min(
                    r.yOrigin,
                    r.yEnd
                ) - 20 && absoluteY(visibleY) <= Math.max(r.yOrigin, r.yEnd) + 20
            ) return r
        }
        return null
    }

    private fun distance(
        dotX: Float,
        dotY: Float,
        originX: Float,
        originY: Float,
        endX: Float,
        endY: Float
    ): Float {
        //calculate the distance between a dot and a line
        //uX and uY are coordinates of a normal vector perpendicular to the line
        val uX: Float
        val uY: Float
        if (originX == endX) {
            uX = 0f
            uY = 1f
        } else if (originY == endY) {
            uX = 1f
            uY = 0f
        } else {
            uX =
                (1f / Math.sqrt((1f + (endX - originX) * (endX - originX) / (endY - originY) / (endY - originY)).toDouble())).toFloat()
            uY =
                ((originX - endX) / (endY - originY) / Math.sqrt((1f + (endX - originX) * (endX - originX) / (endY - originY) / (endY - originY)).toDouble())).toFloat()
        }
        return Math.abs((dotX - originX) * uX + (dotY - originY) * uY)
    }

    private fun distance(X1: Float, Y1: Float, X2: Float, Y2: Float): Float {
        //calculate the distance between two points M1(X1,Y1) and M2(X2,Y2)
        return Math.sqrt(((X1 - X2) * (X1 - X2) + (Y1 - Y2) * (Y1 - Y2)).toDouble()).toFloat()
    }

    private fun updateProjectGeometricalParameters() {
        mUmlProject!!.zoom = (mZoom)
        mUmlProject!!.xOffset = (mXOffset)
        mUmlProject!!.yOffset = (mYOffset)
    }

    //    **********************************************************************************************
    //    Interaction methods
    //    **********************************************************************************************
    private fun promptDeleteRelation(umlRelation: UmlRelation?, view: View) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete relation")
            .setMessage("Are you sure you want to delete this relation ?")
            .setNegativeButton("NO") { dialog, which -> }
            .setPositiveButton("YES") { dialog, which ->
                mUmlProject!!.removeUmlRelation(umlRelation)
                view.invalidate()
            }
        val dialog = builder.create()
        dialog.show()
    }

    companion object {
        private const val CLICK_DELAY: Long = 200
        private const val DOUBLE_CLICK_DELAY: Long = 500
        private const val DOUBLE_CLICK_DISTANCE_MAX = 10f

        //    **********************************************************************************************
        //    Standard drawing dimensions (in dp)
        //    **********************************************************************************************
        private const val FONT_SIZE = 20f
        private const val INTERLINE = 10f
        private const val ARROW_SIZE = 10f
    }
}