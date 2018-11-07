package io.moyuru.autoexposed

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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

    private fun buildPropertySpec(columnSpec: ColumnSpec): PropertySpec {
        return PropertySpec.builder(
            columnSpec.name,
            Column::class.asClassName().parameterizedBy(columnSpec.type.asTypeName().correctStringType()))
            .initializer(buildPropertyInitializer(columnSpec))
            .build()
    }

    private fun buildPropertyInitializer(columnSpec: ColumnSpec): CodeBlock {
        return when {
            columnSpec.exposedDataType is ExposedDataType.Varchar ->
                CodeBlock.of("%L(%S, length = %L)",
                    ExposedDataType.fromTypeMirror(columnSpec.type).funcName, columnSpec.name, columnSpec.length)
            else ->
                CodeBlock.of("%L(%S)", ExposedDataType.fromTypeMirror(columnSpec.type).funcName, columnSpec.name)
        }
    }

    private fun TypeName.correctStringType() =
        if (this.toString() == "java.lang.String") ClassName("kotlin", "String") else this
}