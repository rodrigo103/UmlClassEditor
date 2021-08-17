package com.nathaniel.motus.umlclasseditor.model

import com.nathaniel.motus.umlclasseditor.model.UmlType
import com.nathaniel.motus.umlclasseditor.model.UmlType.TypeLevel
import org.json.JSONException
import org.json.JSONObject

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
        return if (typeMultiplicity != TypeMultiplicity.ARRAY) umlType === methodParameter!!.umlType && typeMultiplicity == methodParameter!!.typeMultiplicity else umlType === methodParameter!!.umlType && typeMultiplicity == methodParameter!!.typeMultiplicity && arrayDimension == methodParameter.arrayDimension
    }

    //    **********************************************************************************************
    //    JSON methods
    //    **********************************************************************************************
    fun toJSONObject(): JSONObject? {
        val jsonObject = JSONObject()
        return try {
            jsonObject.put(JSON_METHOD_PARAMETER_NAME, name)
            jsonObject.put(JSON_METHOD_PARAMETER_INDEX, parameterOrder)
            jsonObject.put(JSON_METHOD_PARAMETER_TYPE, umlType?.name)
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
                        UmlType.Companion?.umlTypes
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
                        UmlType.Companion?.umlTypes
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