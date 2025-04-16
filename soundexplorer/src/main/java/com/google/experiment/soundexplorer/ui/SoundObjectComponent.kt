package com.google.experiment.soundexplorer.ui

import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Component
import androidx.xr.scenecore.ContentlessEntity
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InputEventListener
import androidx.xr.scenecore.InteractableComponent
import androidx.xr.scenecore.Session
import com.google.experiment.soundexplorer.core.GlbModel
import com.google.experiment.soundexplorer.core.GlbModelRepository
import com.google.experiment.soundexplorer.sound.SoundComposition
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.Executor

class SoundObjectComponent(
    val session : Session,
    val modelRepository : GlbModelRepository,
    val glbModel : GlbModel,
    val soundComponent: SoundComposition.SoundCompositionComponent,
    val mainExecutor: Executor,
    val coroutineScope: CoroutineScope
) : Component {

    companion object {
        fun createSoundObject(
            session : Session,
            parentEntity: Entity,
            modelRepository : GlbModelRepository,
            glbModel : GlbModel,
            soundComponent: SoundComposition.SoundCompositionComponent,
            mainExecutor: Executor,
            coroutineScope: CoroutineScope
        ): SoundObjectComponent {
            // Create contentless wrapper entities for the sound object
            // We need to defer loading the gltf model as it takes too long to load to do it at app launch.

            val manipulationEntity = ContentlessEntity.create(session, "ObjectManipEntity", Pose.Identity)

            manipulationEntity.setParent(parentEntity)

            manipulationEntity.addComponent(soundComponent)

            val soc = SoundObjectComponent(
                session, modelRepository, glbModel, soundComponent, mainExecutor, coroutineScope)

            manipulationEntity.addComponent(soc)

            return soc
        }
    }

    private var isInitialized = false
    private var entity: Entity? = null

    override fun onAttach(entity: Entity): Boolean {
        this.entity = entity
        return true
    }

    override fun onDetach(entity: Entity) {
        // todo - properly handle detach
        this.entity = null
    }

    fun setHidden(hidden: Boolean) {
        val e = this.entity
        if (e == null) {
            throw IllegalStateException("Tried to set hidden state on sound object when entity was detached!")
        }

        e.setHidden(hidden)
    }

    suspend fun initialize(initialLocation: Pose) {
        if (this.isInitialized) {
            return
        }

        val gltfModel = modelRepository.getOrLoadModel(glbModel).getOrNull() as GltfModel?
        if (gltfModel == null) {
            throw IllegalArgumentException("Failed to load model " + glbModel.assetName)
        }

        val e = checkNotNull(this.entity)

        e.setPose(initialLocation)

        // Object Manipulation -> Local Programmatic Animation -> Model

        val animationEntity = ContentlessEntity.create(session, "AnimationEntity", Pose.Identity)
        val gltfModelEntity = GltfModelEntity.create(session, gltfModel)
        animationEntity.setParent(e)
        gltfModelEntity.setParent(animationEntity)

        val tapHandler = object : InputEventListener {
            override fun onInputEvent(ie: InputEvent) {
                when (ie.action) {
                    InputEvent.ACTION_UP -> {
                        gltfModelEntity.startAnimation(loop = false)

                        if (!soundComponent.isPlaying) {
                            soundComponent.play()
                        } else {
                            soundComponent.stop()
                        }
                    }
                }
            }
        }

        val lowBehavior = {
                e: Entity, dT: Double ->
            e.setPose(Pose(
                e.getPose().translation,
                e.getPose().rotation * Quaternion.fromAxisAngle(Vector3.Up, 20.0f * dT.toFloat())
            ))
        }

        val mediumBehavior = {
                e: Entity, dT: Double ->
            e.setPose(Pose(
                e.getPose().translation,
                e.getPose().rotation * Quaternion.fromAxisAngle(Vector3.One, 40.0f * dT.toFloat())
            ))
        }

        val highBehavior = {
                e: Entity, dT: Double ->
            e.setPose(Pose(
                e.getPose().translation,
                e.getPose().rotation * Quaternion.fromAxisAngle(Vector3.Right, 70.0f * dT.toFloat())
            ))
        }

        val simComponent = SimpleSimulationComponent(coroutineScope, mediumBehavior)

        gltfModelEntity.addComponent(simComponent)

        soundComponent.onPropertyChanged = {
            if (!soundComponent.isPlaying) {
                simComponent.paused = true
            } else {
                simComponent.paused = false
                simComponent.updateFn = when (soundComponent.soundType) {
                    SoundComposition.SoundSampleType.LOW -> lowBehavior
                    SoundComposition.SoundSampleType.MEDIUM -> mediumBehavior
                    SoundComposition.SoundSampleType.HIGH -> highBehavior
                }
            }
        }

        gltfModelEntity.addComponent(InteractableComponent.create(session, mainExecutor,
            SoundEntityMovementHandler(
                e,
                soundComponent,
                heightToChangeSound = 0.2f,
                debounceThreshold = 0.05f)))

        gltfModelEntity.addComponent(InteractableComponent.create(session, mainExecutor,
            EntityMoveInteractionHandler(
                e,
                linearAcceleration = 3.0f,
                deadZone = 0.02f,
                onInputEventBubble = tapHandler)))

        this.isInitialized = true
    }
}
