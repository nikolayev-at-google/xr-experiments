package com.google.experiment.soundexplorer.core

sealed class GlbModel(val assetName: String) {
    object Fake : GlbModel("")
    object GlbModel01 : GlbModel("glb3/01_bloomspire_static.glb")
    object GlbModel02 : GlbModel("glb3/02_pumpod_static.glb")
    object GlbModel03 : GlbModel("glb3/03_swirlnut_static.glb")
    object GlbModel04 : GlbModel("glb3/04_twistbud_static.glb")
    object GlbModel05 : GlbModel(assetName = "glb3/05_squube_static.glb")
    object GlbModel06 : GlbModel("glb3/06_cello_static.glb")
    object GlbModel07 : GlbModel("glb3/07_munchkin_static.glb")
    object GlbModel08 : GlbModel("glb3/08_pluff_static.glb")
    object GlbModel09 : GlbModel("glb3/09_pillowtri_static.glb")

    object GlbModel01Anim : GlbModel("glb3/01_bloomspire_animated.glb")
    object GlbModel02Anim : GlbModel("glb3/02_pumpod_animated.glb")
    object GlbModel03Anim : GlbModel("glb3/03_swirlnut_animated.glb")
    object GlbModel04Anim : GlbModel("glb3/04_twistbud_animated.glb")
    object GlbModel05Anim : GlbModel(assetName = "glb3/05_squube_animated.glb")
    object GlbModel06Anim : GlbModel("glb3/06_cello_animated.glb")
    object GlbModel07Anim : GlbModel("glb3/07_munchkin_animated.glb")
    object GlbModel08Anim : GlbModel("glb3/08_pluff_animated.glb")
    object GlbModel09Anim : GlbModel("glb3/09_pillowtri_animated.glb")
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