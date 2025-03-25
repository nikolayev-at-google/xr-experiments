package com.google.experiment.soundexplorer.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val xxs : Dp    = 2.dp,
    val xs : Dp     = 4.dp,
    val s : Dp      = 8.dp,
    val m : Dp      = 16.dp,
    val l : Dp      = 24.dp,
    val xl : Dp     = 32.dp,
    val xxl : Dp    = 48.dp,
    val xxxl : Dp   = 72.dp,
    val xxxxl : Dp  = 96.dp
)

val LocalSpacing = compositionLocalOf { Spacing() }