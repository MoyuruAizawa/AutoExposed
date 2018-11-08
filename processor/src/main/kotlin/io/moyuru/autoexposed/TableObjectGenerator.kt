package io.moyuru.autoexposed

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.moyuru.autoexposed.spec.ColumnSpec
import io.moyuru.autoexposed.spec.TableObjectSpec
import org.jetbrains.exposed.sql.Column
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

class TableObjectGenerator(private val processingEnv: ProcessingEnvironment) {
    companion object {
        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    fun generate(tableObjSpec: TableObjectSpec) {
        // see https://github.com/square/kotlinpoet/issues/105
        val kaptGeneratedPath = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]?.replace("kaptKotlin", "kapt")
        if (kaptGeneratedPath == null) {
            processingEnv.printMessage(Diagnostic.Kind.ERROR,
                "Can't find the target directory for generated Kotlin files.")
            return
        }
        val kaptGeneratedDir = File(kaptGeneratedPath)

        FileSpec.builder(tableObjSpec.packageName, tableObjSpec.objectName)
            .addType(buildTypeSpec(tableObjSpec))
            .build()
            .writeTo(kaptGeneratedDir)
    }

    private fun buildTypeSpec(tableObjSpec: TableObjectSpec): TypeSpec {
        return TypeSpec.objectBuilder(tableObjSpec.objectName)
            .superclass(org.jetbrains.exposed.sql.Table::class.java)
            .addSuperclassConstructorParameter("%S", tableObjSpec.tableSpec.tableName)
            .addProperties(tableObjSpec.tableSpec.columnSpecList.map(this::buildPropertySpec))
            .build()
    }

    private fun buildPropertySpec(spec: ColumnSpec): PropertySpec {
        return PropertySpec.builder(
            spec.name,
            Column::class.asClassName()
                .parameterizedBy(spec.type.asTypeName().kotlinze().let { if (spec.isNullable) it.asNullable() else it }))
            .initializer(buildPropertyInitializer(spec))
            .build()
    }

    private fun buildPropertyInitializer(spec: ColumnSpec): CodeBlock {
        return CodeBlock.builder().apply {
            if (spec.exposedDataType is ExposedDataType.Varchar)
                add("%L(%S, length = %L)",
                    ExposedDataType.fromTypeMirror(spec.type).funcName,
                    spec.columnName,
                    spec.length)
            else
                add("%L(%S)", ExposedDataType.fromTypeMirror(spec.type).funcName, spec.columnName)

            if (spec.isPrimary) add(".primaryKey()")
            if (spec.autoInclement) add(".autoIncrement()")
            when {
                spec.uniqueIndex -> add(".uniqueIndex()")
                spec.index -> add(".index()")
            }
            if (spec.isNullable) add(".nullable()")
        }.build()
    }

    private fun TypeName.kotlinze(): TypeName {
        fun String.toJava() = "java.lang.$this"
        return when (this.toString()) {
            "Boolean".toJava() -> Boolean::class.asClassName()
            "Byte".toJava() -> Byte::class.asClassName()
            "Short".toJava() -> Short::class.asClassName()
            "Integer".toJava() -> Integer::class.asClassName()
            "Long".toJava() -> Long::class.asClassName()
            "Float".toJava() -> Float::class.asClassName()
            "Double".toJava() -> Double::class.asClassName()
            "String".toJava() -> String::class.asClassName()
            else -> this
        }
    }
}