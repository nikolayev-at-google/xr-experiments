package com.google.experiment.soundexplorer.core

sealed class GlbModel(val assetName: String) {
    object Fake : GlbModel("")
    object GlbModel07Static : GlbModel("glb4/01_bloomspire_static.glb") // 7
    object GlbModel01Static : GlbModel("glb4/02_pumpod_static.glb") // 1
    object GlbModel04Static : GlbModel("glb4/03_swirlnut_static.glb") // 4
    object GlbModel05Static : GlbModel("glb4/04_twistbud_static.glb") // 5
    object GlbModel06Static : GlbModel(assetName = "glb4/05_squube_static.glb") // 6
    object GlbModel08Static : GlbModel("glb4/06_cello_static.glb") // 8
    object GlbModel09Static : GlbModel("glb4/07_munchkin_static.glb") // 9
    object GlbModel02Static : GlbModel("glb4/08_pluff_static.glb") // 2
    object GlbModel03Static : GlbModel("glb4/09_pillowtri_static.glb") // 3

    object GlbModel07Inactive : GlbModel("glb4/01_bloomspire_inactive.glb")
    object GlbModel01Inactive : GlbModel("glb4/02_pumpod_inactive.glb")
    object GlbModel04Inactive : GlbModel("glb4/03_swirlnut_inactive.glb")
    object GlbModel05Inactive : GlbModel("glb4/04_twistbud_inactive.glb")
    object GlbModel06Inactive : GlbModel("glb4/05_squube_inactive.glb")
    object GlbModel08Inactive : GlbModel("glb4/06_cello_inactive.glb")
    object GlbModel09Inactive : GlbModel("glb4/07_munchkin_inactive.glb")
    object GlbModel02Inactive : GlbModel("glb4/08_pluff_inactive.glb")
    object GlbModel03Inactive : GlbModel("glb4/09_pillowtri_inactive.glb")

    object GlbModel07Animated : GlbModel("glb4/01_bloomspire_animated.glb")
    object GlbModel01Animated : GlbModel("glb4/02_pumpod_animated.glb")
    object GlbModel04Animated : GlbModel("glb4/03_swirlnut_animated.glb")
    object GlbModel05Animated : GlbModel("glb4/04_twistbud_animated.glb")
    object GlbModel06Animated : GlbModel(assetName = "glb4/05_squube_animated.glb")
    object GlbModel08Animated : GlbModel("glb4/06_cello_animated.glb")
    object GlbModel09Animated : GlbModel("glb4/07_munchkin_animated.glb")
    object GlbModel02Animated : GlbModel("glb4/08_pluff_animated.glb")
    object GlbModel03Animated : GlbModel("glb4/09_pillowtri_animated.glb")
    companion object {
        val allGlbStaticModels = listOf(
            GlbModel01Static,
            GlbModel02Static,
            GlbModel03Static,
            GlbModel04Static,
            GlbModel05Static,
            GlbModel06Static,
            GlbModel07Static,
            GlbModel08Static,
            GlbModel09Static
        )
        val allGlbInactiveModels = listOf(
            GlbModel01Inactive,
            GlbModel02Inactive,
            GlbModel03Inactive,
            GlbModel04Inactive,
            GlbModel05Inactive,
            GlbModel06Inactive,
            GlbModel07Inactive,
            GlbModel08Inactive,
            GlbModel09Inactive
        )
        val allGlbAnimatedModels = listOf(
            GlbModel01Animated,
            GlbModel02Animated,
            GlbModel03Animated,
            GlbModel04Animated,
            GlbModel05Animated,
            GlbModel06Animated,
            GlbModel07Animated,
            GlbModel08Animated,
            GlbModel09Animated
        )
    }
}