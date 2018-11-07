package io.moyuru.autoexposed

import com.squareup.kotlinpoet.asTypeName
import java.lang.NullPointerException
import java.math.BigDecimal
import javax.activation.UnsupportedDataTypeException
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

sealed class ExposedDataType(val funcName: String) {
    companion object {
        fun fromTypeMirror(tm: TypeMirror): ExposedDataType {
            return when (tm.kind ?: throw NullPointerException()) {
                TypeKind.BOOLEAN -> Bool()
                TypeKind.BYTE -> Integer()
                TypeKind.SHORT -> Integer()
                TypeKind.INT -> Integer()
                TypeKind.LONG -> Long()
                TypeKind.CHAR -> Char()
                TypeKind.FLOAT -> Float()
                TypeKind.DOUBLE -> Double()
                TypeKind.VOID -> throw UnsupportedDataTypeException()
                TypeKind.NONE -> throw UnsupportedDataTypeException()
                TypeKind.NULL -> throw UnsupportedDataTypeException()
                TypeKind.ARRAY -> throw UnsupportedDataTypeException()
                TypeKind.DECLARED -> when (tm.asTypeName()) {
                    String::class.java.asTypeName() -> Varchar()
                    DateTime::class.java.asTypeName() -> DateTime()
                    BigDecimal::class.java.asTypeName() -> Decimal()
                    ByteArray::class.java.asTypeName() -> Binary()
                    java.sql.Blob::class.java.asTypeName() -> Blob()
                    else -> throw UnsupportedDataTypeException()
                }
                TypeKind.ERROR -> throw UnsupportedDataTypeException()
                TypeKind.TYPEVAR -> throw UnsupportedDataTypeException()
                TypeKind.WILDCARD -> throw UnsupportedDataTypeException()
                TypeKind.PACKAGE -> throw UnsupportedDataTypeException()
                TypeKind.EXECUTABLE -> throw UnsupportedDataTypeException()
                TypeKind.OTHER -> throw UnsupportedDataTypeException()
                TypeKind.UNION -> throw UnsupportedDataTypeException()
                TypeKind.INTERSECTION -> throw UnsupportedDataTypeException()
            }
        }
    }

    class Integer() : ExposedDataType("integer")
    class Char() : ExposedDataType("char")
    class Decimal() : ExposedDataType("decimal")
    class Float() : ExposedDataType("float")
    class Double() : ExposedDataType("double")
    class Long() : ExposedDataType("long")
    class Date() : ExposedDataType("date")
    class Bool() : ExposedDataType("bool")
    class DateTime() : ExposedDataType("datetime")
    class Blob() : ExposedDataType("blob")
    class Text() : ExposedDataType("text")
    class Binary() : ExposedDataType("binary")
    class Varchar() : ExposedDataType("varchar")
}