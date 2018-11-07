package io.moyuru.autoexposed.spec

import io.moyuru.autoexposed.ExposedDataType
import javax.lang.model.type.TypeMirror

data class ColumnSpec(val name: String, val type: TypeMirror, val exposedDataType: ExposedDataType, val length: Int)