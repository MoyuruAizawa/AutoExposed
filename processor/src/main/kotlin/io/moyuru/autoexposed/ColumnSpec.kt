package io.moyuru.autoexposed

import javax.lang.model.type.TypeMirror

data class ColumnSpec(val name: String, val type: TypeMirror)