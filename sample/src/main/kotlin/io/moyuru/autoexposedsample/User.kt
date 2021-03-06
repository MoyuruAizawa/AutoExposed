package io.moyuru.autoexposedsample

import io.moyuru.autoexposed.annotation.Column
import io.moyuru.autoexposed.annotation.PrimaryKey
import io.moyuru.autoexposed.annotation.Table

@Table
data class User(
    @PrimaryKey(autoIncrement = true) val id: Long,
    @Column(length = 128, uniqueIndex = true) val name: String,
    @Column(index = true) val age: Int,
    @Column val gender: String?,
    @Column val isPaid: Boolean)