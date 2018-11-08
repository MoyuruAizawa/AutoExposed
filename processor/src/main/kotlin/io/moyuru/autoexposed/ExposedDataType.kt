package io.moyuru.autoexposed

import com.squareup.kotlinpoet.asTypeName
import java.math.BigDecimal
import javax.activation.UnsupportedDataTypeException
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

sealed class ExposedDataType(val funcName: String) {
    companion object {
        fun fromTypeMirror(tm: TypeMirror): ExposedDataType {
            val e by lazy { UnsupportedDataTypeException("${tm.kind.name} is not supported") }
            return when (tm.kind ?: throw NullPointerException()) {
                TypeKind.BOOLEAN -> Bool()
                TypeKind.BYTE -> Integer()
                TypeKind.SHORT -> Integer()
                TypeKind.INT -> Integer()
                TypeKind.LONG -> Long()
                TypeKind.CHAR -> Char()
                TypeKind.FLOAT -> Float()
                TypeKind.DOUBLE -> Double()
                TypeKind.VOID -> throw e
                TypeKind.NONE -> throw e
                TypeKind.NULL -> throw e
                TypeKind.ARRAY -> throw e
                TypeKind.DECLARED -> when (tm.asTypeName()) {
                    java.lang.Boolean::class.java.asTypeName() -> Bool()
                    java.lang.Byte::class.java.asTypeName() -> Integer()
                    java.lang.Short::class.java.asTypeName() -> Integer()
                    java.lang.Integer::class.java.asTypeName() -> Integer()
                    java.lang.Long::class.java.asTypeName() -> Long()
                    java.lang.Float::class.java.asTypeName() -> Float()
                    java.lang.Double::class.java.asTypeName() -> Double()
                    java.lang.String::class.java.asTypeName() -> Varchar()
                    String::class.java.asTypeName() -> Varchar()
                    org.joda.time.LocalDate::class.java.asTypeName() -> Date()
                    org.joda.time.LocalDateTime::class.java.asTypeName() -> DateTime()
                    org.joda.time.DateTime::class.java.asTypeName() -> DateTime()
                    BigDecimal::class.java.asTypeName() -> Decimal()
                    ByteArray::class.java.asTypeName() -> Binary()
                    java.sql.Blob::class.java.asTypeName() -> Blob()
                    else -> throw UnsupportedDataTypeException("${tm.asTypeName()}")
                }
                TypeKind.ERROR -> throw e
                TypeKind.TYPEVAR -> throw e
                TypeKind.WILDCARD -> throw e
                TypeKind.PACKAGE -> throw e
                TypeKind.EXECUTABLE -> throw e
                TypeKind.OTHER -> throw e
                TypeKind.UNION -> throw e
                TypeKind.INTERSECTION -> throw e
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