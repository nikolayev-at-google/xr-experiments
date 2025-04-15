package com.google.experiment.soundexplorer.ui

import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InputEventListener
import com.google.experiment.soundexplorer.sound.SoundComposition

class SoundEntityMovementHandler(
    // entity whose position will be tracked to determine if the sound should change
    val entity: Entity,
    // sound component that is used to change the sound type
    val soundComponent: SoundComposition.SoundCompositionComponent,
    // difference in height relative to the initial location at which the sound changes
    heightToChangeSound: Float,
    // additional height difference to transition from higher -> lower states (to avoid jitter)
    val debounceThreshold: Float = 0.05f
) : InputEventListener {

    private val initialHeight = entity.getPose().translation.y

    private val lowMediumHeightThreshold = initialHeight - heightToChangeSound
    private val mediumHighHeightThreshold = initialHeight + heightToChangeSound

    override fun onInputEvent(inputEvent: InputEvent) {
        if (inputEvent.action != InputEvent.ACTION_MOVE) {
            return
        }

        val currentHeight = this.entity.getPose().translation.y

        when (this.soundComponent.soundType) {
            SoundComposition.SoundSampleType.LOW -> {
                if (currentHeight > mediumHighHeightThreshold) {
                    this.soundComponent.soundType = SoundComposition.SoundSampleType.HIGH
                } else if (currentHeight > lowMediumHeightThreshold) {
                    this.soundComponent.soundType = SoundComposition.SoundSampleType.MEDIUM
                }
            }
            SoundComposition.SoundSampleType.MEDIUM -> {
                if (currentHeight < lowMediumHeightThreshold - debounceThreshold) {
                    this.soundComponent.soundType = SoundComposition.SoundSampleType.LOW
                } else if (currentHeight > mediumHighHeightThreshold) {
                    this.soundComponent.soundType = SoundComposition.SoundSampleType.HIGH
                }
            }
            SoundComposition.SoundSampleType.HIGH -> {
                if (currentHeight < lowMediumHeightThreshold - debounceThreshold) {
                    this.soundComponent.soundType = SoundComposition.SoundSampleType.LOW
                } else if (currentHeight < mediumHighHeightThreshold - debounceThreshold) {
                    this.soundComponent.soundType = SoundComposition.SoundSampleType.MEDIUM
                }
            }
        }
    }
}
