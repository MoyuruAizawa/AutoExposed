package io.moyuru.autoexposed

import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

fun ProcessingEnvironment.printMessage(kind: Diagnostic.Kind, string: String) = messager.printMessage(kind, string)

fun String.toSnakeCase(): String {
    if (isBlank()) return this

    val sb = StringBuilder(length * 2)
    forEachIndexed { i, c ->
        when {
            i == 0 && c.isUpperCase() -> sb.append(c.toLowerCase())
            c.isUpperCase() -> {
                sb.append("_")
                sb.append(c.toLowerCase())
            }
            else -> sb.append(c)
        }
    }
    return sb.toString()
}