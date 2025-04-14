package com.google.experiment.soundexplorer.ui

import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InputEventListener
import kotlin.math.abs

class EntityMoveInteractionHandler(
    val entity: Entity,
    val linearAcceleration: Float,
    val deadZone: Float = 0.0f,
    val onInputEventBubble: InputEventListener? = null
) : InputEventListener {

    private val EPSILON = 0.001f

    data class InteractionData(
        val initialHitPoint: Vector3,
        val initialHitDistance: Float,
        val initialHitOffsetFromObjOrigin: Vector3,
        val timeStartNs: Long,

        var lastUpdateTimeNs: Long = timeStartNs,
        var currentLinearVelocity: Double = 0.0,
        var performedMove: Boolean = false
    )

    private var currentInteraction: InteractionData? = null

    private fun intersectRayWithPlane(rayOrigin: Vector3, rayDirection: Vector3, planeNormal: Vector3, planePoint: Vector3): Vector3? {
        val dirDotN = rayDirection dot planeNormal
        if (abs(dirDotN) < EPSILON) {
            return null
        }
        val t = ((planePoint - rayOrigin) dot planeNormal) / dirDotN
        if (t <= 0.0f) {
            return null
        }
        return (rayDirection * t) + rayOrigin
    }

    override fun onInputEvent(inputEvent: InputEvent) {

        if (inputEvent.action == InputEvent.ACTION_DOWN) {
            // inputEvent.hitInfo info doesn't appear to be available yet, so for now construct a plane to approximate the initial ray hit location
            val interactionPlaneP = entity.getPose().translation
            val interactionPlaneN = -inputEvent.direction.toNormalized()

            val hitPoint = intersectRayWithPlane(inputEvent.origin, inputEvent.direction, interactionPlaneN, interactionPlaneP)
            if (hitPoint == null) {
                return
            }

            this.currentInteraction = InteractionData(
                initialHitPoint = hitPoint,
                initialHitDistance = (hitPoint - inputEvent.origin).length,
                initialHitOffsetFromObjOrigin = hitPoint - interactionPlaneP,
                timeStartNs = System.nanoTime())

        } else if (inputEvent.action == InputEvent.ACTION_UP) {
            val ci = this.currentInteraction
            if (ci == null || !ci.performedMove) {
                // bubble the event as a tap if it wasn't handled
                this.onInputEventBubble?.onInputEvent(inputEvent)
            }
            this.currentInteraction = null

        } else if (inputEvent.action == InputEvent.ACTION_MOVE) {
            val ci = this.currentInteraction
            if (ci == null) {
                return
            }

            val targetPosition = (inputEvent.direction.toNormalized() * ci.initialHitDistance) + inputEvent.origin

            if (!ci.performedMove &&
                (((targetPosition - ci.initialHitPoint).lengthSquared) < (deadZone * deadZone))) {
                return
            }

            this.currentInteraction?.performedMove = true

            val currentTimeNs = System.nanoTime()
            val deltaTimeNs = (currentTimeNs - ci.lastUpdateTimeNs)
            val deltaTimeS = deltaTimeNs.toDouble() * 0.000000001

            this.currentInteraction?.lastUpdateTimeNs = currentTimeNs

            // todo- consider accounting for rotation

            val targetEntityPosition = targetPosition - ci.initialHitOffsetFromObjOrigin
            val displacementToGoal = targetEntityPosition - this.entity.getPose().translation

            if (displacementToGoal.lengthSquared < EPSILON) {
                return
            }

            val distanceToStop = (ci.currentLinearVelocity * ci.currentLinearVelocity) / (2.0f * linearAcceleration)
            val distanceToGoal = displacementToGoal.length

            val linearAccel: Double =
                if (distanceToStop >= distanceToGoal) {
                    // need to slow down
                    -(ci.currentLinearVelocity * ci.currentLinearVelocity) / (2.0f * distanceToGoal)
                } else {
                    // ok to speed up
                    linearAcceleration.toDouble()
                }

            val linearVel = ci.currentLinearVelocity + (linearAccel * deltaTimeS)
            val linearDisplacement = linearVel * deltaTimeS

            if (abs(linearDisplacement) >= distanceToGoal) {
                this.entity.setPose(Pose(targetEntityPosition, this.entity.getPose().rotation))
                this.currentInteraction?.currentLinearVelocity = 0.0
                return
            }

            val entityPosition = this.entity.getPose().translation +
                    (displacementToGoal.toNormalized() * linearDisplacement.toFloat())
            this.currentInteraction?.currentLinearVelocity = linearVel

            this.entity.setPose(Pose(entityPosition, this.entity.getPose().rotation))

        } else {
            this.onInputEventBubble?.onInputEvent(inputEvent)
        }
    }
}
