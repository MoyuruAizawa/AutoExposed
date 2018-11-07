package io.moyuru.autoexposed

import io.moyuru.autoexposed.annotation.Table
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedAnnotationTypes(
    "io.moyuru.autoexposed.annotation.Table",
    "io.moyuru.autoexposed.annotation.Column"
)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class Processor : AbstractProcessor() {
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val parser = TableParser(processingEnv)
        val generator = TableObjectGenerator(processingEnv)

        roundEnv.getElementsAnnotatedWith(Table::class.java)
            .map { it as TypeElement }
            .map(parser::parse)
            .forEach(generator::generate)
        return true
    }
}