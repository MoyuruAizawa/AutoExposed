package io.moyuru.autoexposedsample

import io.moyuru.autoexposed.annotation.Column
import io.moyuru.autoexposed.annotation.Table

@Table
data class User(
    @Column val id: Long,
    @Column(length = 128) val name: String,
    @Column val age: Int,
    @Column val isPaid: Boolean)