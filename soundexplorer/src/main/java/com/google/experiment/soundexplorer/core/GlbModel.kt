package com.google.experiment.soundexplorer.core

sealed class GlbModel(val assetName: String) {
    object Fake : GlbModel("")
    object GlbModel01 : GlbModel("glb2/01_animated.glb")
    object GlbModel02 : GlbModel("glb2/01_static.glb")
    object GlbModel03 : GlbModel("glb2/02_static.glb")
    object GlbModel04 : GlbModel("glb2/05_static.glb")
    object GlbModel05 : GlbModel("glb2/08_static.glb")
    object GlbModel06 : GlbModel("glb2/10_static.glb")
    object GlbModel07 : GlbModel("glb2/11_static.glb")
    object GlbModel08 : GlbModel("glb2/16_static.glb")
    object GlbModel09 : GlbModel("glb2/18_static.glb")

    companion object {
        val allGlbModels = listOf(
            GlbModel01,
            GlbModel02,
            GlbModel03,
            GlbModel04,
            GlbModel05,
            GlbModel06,
            GlbModel07,
            GlbModel08,
            GlbModel09
        )
    }
}
