package io.moyuru.autoexposed

import io.moyuru.autoexposed.annotation.Column
import io.moyuru.autoexposed.spec.ColumnSpec
import io.moyuru.autoexposed.spec.TableObjectSpec
import io.moyuru.autoexposed.spec.TableSpec
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

class TableParser(private val processingEnv: ProcessingEnvironment) {
    fun parse(element: TypeElement): TableObjectSpec {
//        val table = typeElement.getAnnotation(Table::class.java)
        val tableName = element.simpleName.toString()
        val columns = parseColumn(element)
        val tableSpec = TableSpec(tableName.toSnakeCase(), columns)
        return TableObjectSpec(element.enclosingElement.toString(),
            "${tableName}Table",
            tableSpec)
    }

    private fun parseColumn(typeElement: TypeElement): List<ColumnSpec> {
        val fields = typeElement.enclosedElements.filter { it is VariableElement }
        val columnAnnotatedElement = typeElement.enclosedElements.filter { it.hasColumn() }

        return fields
            .map { fe ->
                val c = columnAnnotatedElement.find { it.simpleName.startsWith(fe.simpleName.toString()) }?.column
                requireNotNull(c)
                fe to c
            }
            .map { (fe, c) ->
                ColumnSpec(fe.simpleName.toString().toSnakeCase(),
                    fe.asType(),
                    ExposedDataType.fromTypeMirror(fe.asType()),
                    c.length)
            }
    }

    private val Element.column get() = getAnnotation(Column::class.java)
    private fun Element.hasColumn() = column != null
}