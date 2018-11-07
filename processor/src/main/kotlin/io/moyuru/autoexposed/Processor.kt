package io.moyuru.autoexposed

import com.squareup.kotlinpoet.*
import io.moyuru.autoexposed.annotation.Table
import org.jetbrains.exposed.sql.Column
import java.io.File
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes(
    "io.moyuru.autoexposed.annotation.Table",
    "io.moyuru.autoexposed.annotation.Column"
)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class Processor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(Table::class.java).forEach { generateTable(it) }
        return true
    }

    private fun generateTable(element: Element) {
        val tableSpec = extractTableSpec(element)
        val tableObjSpec =
            TableObjectSpec(element.enclosingElement.toString(), "${tableSpec.tableName}Table", tableSpec)
        val fileSpec = FileSpec.builder(tableObjSpec.packageName, tableObjSpec.objectName)
            .addType(buildTableObject(tableObjSpec, tableSpec))
            .build()

        // see https://github.com/square/kotlinpoet/issues/105
        val kaptGeneratedDirPath = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            ?.replace("kaptKotlin", "kapt") ?: run {
            processingEnv.messager
                .printMessage(Diagnostic.Kind.ERROR, "Can't find the target directory for generated Kotlin files.")
            return
        }
        val kaptGeneratedDir = File(kaptGeneratedDirPath)
        if (!kaptGeneratedDir.parentFile.exists()) kaptGeneratedDir.parentFile.mkdirs()
        fileSpec.writeTo(kaptGeneratedDir)
    }

    private fun buildTableObject(tableObjSpec: TableObjectSpec, tableSpec: TableSpec): TypeSpec {
        return TypeSpec.objectBuilder(tableObjSpec.objectName)
            .superclass(org.jetbrains.exposed.sql.Table::class.java)
            .addSuperclassConstructorParameter("%S", tableSpec.tableName)
            .addProperties(buildColumn(tableSpec))
            .build()
    }

    private fun buildColumn(tableSpec: TableSpec): List<PropertySpec> {
        return tableSpec.columnSpecList
            .map {
                PropertySpec.builder(
                    it.name,
                    Column::class.asClassName().parameterizedBy(it.type.asTypeName().correctStringType())
                ).initializer("%L(%S)", ExposedDataType.fromTypeMirror(it.type).funcName, it.name)
                    .build()
            }
    }

    private fun extractTableSpec(element: Element): TableSpec {
        val columnSpecList = element.enclosedElements
            .filter { it.kind == ElementKind.FIELD }
            .map(this::extractColumnSpec)
        return TableSpec(element.simpleName.toString(), columnSpecList)
    }

    private fun extractColumnSpec(element: Element) = ColumnSpec(element.simpleName.toString(), element.asType())

    private fun TypeName.correctStringType() =
        if (this.toString() == "java.lang.String") ClassName("kotlin", "String") else this
}