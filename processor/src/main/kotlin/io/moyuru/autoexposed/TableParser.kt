package io.moyuru.autoexposed

import io.moyuru.autoexposed.annotation.Column
import io.moyuru.autoexposed.annotation.PrimaryKey
import io.moyuru.autoexposed.spec.ColumnSpec
import io.moyuru.autoexposed.spec.TableObjectSpec
import io.moyuru.autoexposed.spec.TableSpec
import org.jetbrains.annotations.Nullable
import java.rmi.UnexpectedException
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class TableParser(private val processingEnv: ProcessingEnvironment) {
    fun parse(element: TypeElement): TableObjectSpec {
//        val table = typeElement.getAnnotation(Table::class.java)
        val tableName = element.simpleName.toString()
        val tableSpec = TableSpec(tableName.toSnakeCase(), extractColumns(element))
        return TableObjectSpec(element.enclosingElement.toString(),
            "${tableName}Table",
            tableSpec)
    }

    private fun extractColumns(typeElement: TypeElement): List<ColumnSpec> {
        if (typeElement.enclosedElements.filter { it.hasPrimaryKey() }.size > 1) {
            processingEnv.printMessage(Diagnostic.Kind.ERROR, "PrimaryKey must be only one.")
            return emptyList()
        }

        return typeElement.enclosedElements.filter { it.kind == ElementKind.FIELD }
            .zip(typeElement.enclosedElements.filter { it.hasPrimaryKey() || it.hasColumn() })
            .map { (fieldElement, annotatedElement) ->
                val specBuilder = { length: Int, autoIncrement: Boolean, uniqueIndex: Boolean, index: Boolean ->
                    ColumnSpec(name = fieldElement.simpleName.toString(),
                        columnName = fieldElement.simpleName.toString().toSnakeCase(),
                        type = fieldElement.asType(),
                        exposedDataType = ExposedDataType.fromTypeMirror(fieldElement.asType()),
                        length = length,
                        autoInclement = autoIncrement,
                        uniqueIndex = uniqueIndex,
                        index = index,
                        isNullable = fieldElement.getAnnotation(Nullable::class.java) != null,
                        isPrimary = annotatedElement.hasPrimaryKey())
                }

                when {
                    annotatedElement.hasPrimaryKey() ->
                        annotatedElement.primaryKey.run { specBuilder(length, autoIncrement, false, false) }
                    annotatedElement.hasColumn() ->
                        annotatedElement.column.run { specBuilder(length, autoIncrement, uniqueIndex, index) }
                    else -> throw UnexpectedException("Unexpected Annotation")
                }
            }
    }

    private val Element.column get() = getAnnotation(Column::class.java)
    private val Element.primaryKey get() = getAnnotation(PrimaryKey::class.java)
    private fun Element.hasColumn() = column != null
    private fun Element.hasPrimaryKey() = primaryKey != null
}