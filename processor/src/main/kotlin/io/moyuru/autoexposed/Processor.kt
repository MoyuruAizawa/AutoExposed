package io.moyuru.autoexposed

import io.moyuru.autoexposed.annotation.Column
import io.moyuru.autoexposed.annotation.Table
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes(
    "io.moyuru.autoexposed.annotation.Table",
    "io.moyuru.autoexposed.annotation.Column"
)
class Processor : AbstractProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(Table::class.java)
            .forEach { generateTable(it as TypeElement) }
        return true
    }

    private fun generateTable(element: TypeElement) {
        val table = element.getAnnotation(Table::class.java)
        val columns = element.enclosedElements
            .filter { it.kind == ElementKind.FIELD }
            .map { it.getAnnotation(Column::class.java) }
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Table: $table, Columns: $columns")
    }
}