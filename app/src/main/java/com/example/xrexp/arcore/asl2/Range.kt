package com.example.xrexp.arcore.asl2

data class Range<T : Comparable<T>>(val min: T, val max: T) {
    fun contains(value: T): Boolean = value >= min && value <= max
}