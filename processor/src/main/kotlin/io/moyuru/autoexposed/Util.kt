package io.moyuru.autoexposed

import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

fun ProcessingEnvironment.printMessage(kind: Diagnostic.Kind, string: String) = messager.printMessage(kind, string)