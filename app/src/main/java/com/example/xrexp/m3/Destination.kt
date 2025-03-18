package com.example.xrexp.m3

internal enum class Destination {
    Dialog,
}

internal val Destination.label: String
    get() =
        when (this) {
            Destination.Dialog -> "Dialog"
        }
