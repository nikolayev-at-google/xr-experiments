package com.google.experiment.soundexplorer.sound

import android.content.Context

class SoundComponents(
    val context: Context,
    val soundPoolManager: SoundPoolManager,
    val composition: SoundCompositionSimple
) {
    var instrument1Component: SoundCompositionSimple.SoundCompositionComponent? = null
    var instrument2Component: SoundCompositionSimple.SoundCompositionComponent? = null
    var instrument3Component: SoundCompositionSimple.SoundCompositionComponent? = null
    var instrument4Component: SoundCompositionSimple.SoundCompositionComponent? = null
    var instrument5Component: SoundCompositionSimple.SoundCompositionComponent? = null
    var instrument6Component: SoundCompositionSimple.SoundCompositionComponent? = null
    var instrument7Component: SoundCompositionSimple.SoundCompositionComponent? = null
    var instrument8Component: SoundCompositionSimple.SoundCompositionComponent? = null
    var instrument9Component: SoundCompositionSimple.SoundCompositionComponent? = null

    suspend fun initialize() {
        val inst01lowId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst01_low.wav"))
        val inst01midId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst01_mid.wav"))
        val inst01highId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst01_high.wav"))

        val inst02lowId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst02_low.wav"))
        val inst02midId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst02_mid.wav"))
        val inst02highId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst02_high.wav"))

        val inst03lowId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst03_low.wav"))
        val inst03midId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst03_mid.wav"))
        val inst03highId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst03_high.wav"))

        val inst04lowId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst04_low.wav"))
        val inst04midId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst04_mid.wav"))
        val inst04highId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst04_high.wav"))

        val inst05lowId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst05_low.wav"))
        val inst05midId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst05_mid.wav"))
        val inst05highId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst05_high.wav"))

        val inst06lowId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst06_low.wav"))
        val inst06midId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst06_mid.wav"))
        val inst06highId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst06_high.wav"))

        val inst07lowId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst07_low.wav"))
        val inst07midId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst07_mid.wav"))
        val inst07highId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst07_high.wav"))

        val inst08lowId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst08_low.wav"))
        val inst08midId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst08_mid.wav"))
        val inst08highId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst08_high.wav"))

        val inst09lowId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst09_low.wav"))
        val inst09midId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst09_mid.wav"))
        val inst09highId = checkNotNull(soundPoolManager.loadSound(context, "audio2/inst09_high.wav"))

        soundPoolManager.start()

        this.instrument1Component = composition.addComponent(inst01lowId, inst01midId, inst01highId)
        this.instrument2Component = composition.addComponent(inst02lowId, inst02midId, inst02highId)
        this.instrument3Component = composition.addComponent(inst03lowId, inst03midId, inst03highId)
        this.instrument4Component = composition.addComponent(inst04lowId, inst04midId, inst04highId)
        this.instrument5Component = composition.addComponent(inst05lowId, inst05midId, inst05highId)
        this.instrument6Component = composition.addComponent(inst06lowId, inst06midId, inst06highId)
        this.instrument7Component = composition.addComponent(inst07lowId, inst07midId, inst07highId)
        this.instrument8Component = composition.addComponent(inst08lowId, inst08midId, inst08highId)
        this.instrument9Component = composition.addComponent(inst09lowId, inst09midId, inst09highId)
    }
}

