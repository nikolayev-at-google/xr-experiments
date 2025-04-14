package com.google.experiment.soundexplorer.sound

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

class SoundExplorerSoundPool(context: Context) {

    val manager = SoundPoolManager(27)

    val soundsLoaded: StateFlow<Boolean>
        get() { return this.manager.soundsLoaded }

    val inst01lowId = checkNotNull(manager.loadSound(context, "audio2/inst01_low.wav"))
    val inst01midId = checkNotNull(manager.loadSound(context, "audio2/inst01_mid.wav"))
    val inst01highId = checkNotNull(manager.loadSound(context, "audio2/inst01_high.wav"))

    val inst02lowId = checkNotNull(manager.loadSound(context, "audio2/inst02_low.wav"))
    val inst02midId = checkNotNull(manager.loadSound(context, "audio2/inst02_mid.wav"))
    val inst02highId = checkNotNull(manager.loadSound(context, "audio2/inst02_high.wav"))

    val inst03lowId = checkNotNull(manager.loadSound(context, "audio2/inst03_low.wav"))
    val inst03midId = checkNotNull(manager.loadSound(context, "audio2/inst03_mid.wav"))
    val inst03highId = checkNotNull(manager.loadSound(context, "audio2/inst03_high.wav"))

    val inst04lowId = checkNotNull(manager.loadSound(context, "audio2/inst04_low.wav"))
    val inst04midId = checkNotNull(manager.loadSound(context, "audio2/inst04_mid.wav"))
    val inst04highId = checkNotNull(manager.loadSound(context, "audio2/inst04_high.wav"))

    val inst05lowId = checkNotNull(manager.loadSound(context, "audio2/inst05_low.wav"))
    val inst05midId = checkNotNull(manager.loadSound(context, "audio2/inst05_mid.wav"))
    val inst05highId = checkNotNull(manager.loadSound(context, "audio2/inst05_high.wav"))

    val inst06lowId = checkNotNull(manager.loadSound(context, "audio2/inst06_low.wav"))
    val inst06midId = checkNotNull(manager.loadSound(context, "audio2/inst06_mid.wav"))
    val inst06highId = checkNotNull(manager.loadSound(context, "audio2/inst06_high.wav"))

    val inst07lowId = checkNotNull(manager.loadSound(context, "audio2/inst07_low.wav"))
    val inst07midId = checkNotNull(manager.loadSound(context, "audio2/inst07_mid.wav"))
    val inst07highId = checkNotNull(manager.loadSound(context, "audio2/inst07_high.wav"))

    val inst08lowId = checkNotNull(manager.loadSound(context, "audio2/inst08_low.wav"))
    val inst08midId = checkNotNull(manager.loadSound(context, "audio2/inst08_mid.wav"))
    val inst08highId = checkNotNull(manager.loadSound(context, "audio2/inst08_high.wav"))

    val inst09lowId = checkNotNull(manager.loadSound(context, "audio2/inst09_low.wav"))
    val inst09midId = checkNotNull(manager.loadSound(context, "audio2/inst09_mid.wav"))
    val inst09highId = checkNotNull(manager.loadSound(context, "audio2/inst09_high.wav"))

}
