package io.moyuru.autoexposed.annotation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Column(
    val length: Int = 0,
    val autoIncrement: Boolean = false,
    val uniqueIndex: Boolean = false)