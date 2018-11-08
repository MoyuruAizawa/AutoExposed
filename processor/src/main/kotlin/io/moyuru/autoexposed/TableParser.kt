package io.moyuru.autoexposed

import com.squareup.kotlinpoet.asTypeName
import io.moyuru.autoexposed.annotation.Column
import io.moyuru.autoexposed.annotation.PrimaryKey
import io.moyuru.autoexposed.spec.ColumnSpec
import io.moyuru.autoexposed.spec.TableObjectSpec
import io.moyuru.autoexposed.spec.TableSpec
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

class TableParser(private val processingEnv: ProcessingEnvironment) {
    fun parse(element: TypeElement): TableObjectSpec {
//        val table = typeElement.getAnnotation(Table::class.java)
        val tableName = element.simpleName.toString()
        val columns = listOfNotNull(extractPrimaryKey(element)) + extractColumn(element)

        val tableSpec = TableSpec(tableName.toSnakeCase(), columns)
        return TableObjectSpec(element.enclosingElement.toString(),
            "${tableName}Table",
            tableSpec)
    }

    private fun extractPrimaryKey(typeElement: TypeElement): ColumnSpec? {
        val primaryKeyAnnotatedElement = typeElement.enclosedElements
            .filter { it.hasPrimaryKey() }
            .also {
                if (it.size > 1) {
                    processingEnv.printMessage(Diagnostic.Kind.ERROR, "PrimaryKey must be only one.")
                    return null
                }
            }
            .firstOrNull() ?: return null

        val fields = typeElement.enclosedElements
            .firstOrNull { it is VariableElement && primaryKeyAnnotatedElement.simpleName.startsWith(it.simpleName) }
            ?: return null

        return primaryKeyAnnotatedElement.primaryKey?.let {
            ColumnSpec(fields.simpleName.toString().toSnakeCase(),
                fields.asType(),
                ExposedDataType.fromTypeMirror(fields.asType()),
                it.length,
                isPrimary = true)
        }
    }

    private fun extractColumn(typeElement: TypeElement): List<ColumnSpec> {
        val fields = typeElement.enclosedElements.filter { it is VariableElement }
        val columnAnnotatedElement = typeElement.enclosedElements.filter { it.hasColumn() && !it.hasPrimaryKey() }

        return fields.mapNotNull { fe ->
            val c = columnAnnotatedElement.find { it.simpleName.startsWith(fe.simpleName) }?.column
            if (c != null) fe to c
            else null
        }.map { (fe, c) ->
            val exposedDataType = ExposedDataType.fromTypeMirror(fe.asType())

            if (exposedDataType !is ExposedDataType.Varchar && c.length > 0) {
                processingEnv.printMessage(Diagnostic.Kind.ERROR,
                    "\'${typeElement.simpleName}#${fe.simpleName}: ${fe.asType().asTypeName()}\' length param is supported")
            }

            ColumnSpec(fe.simpleName.toString().toSnakeCase(),
                fe.asType(),
                exposedDataType,
                c.length)
        }
    }

    private val Element.column get() = getAnnotation(Column::class.java)
    private val Element.primaryKey get() = getAnnotation(PrimaryKey::class.java)
    private fun Element.hasColumn() = column != null
    private fun Element.hasPrimaryKey() = primaryKey != null
}