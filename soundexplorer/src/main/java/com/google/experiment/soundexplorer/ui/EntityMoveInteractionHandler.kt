package com.google.experiment.soundexplorer.ui

import android.util.Log
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InputEventListener
import kotlin.math.abs

class EntityMoveInteractionHandler(
    val entity: Entity,
    val deadZone: Float = 0.0f,
    val onTap: InputEventListener? = null
) : InputEventListener {

    private val EPSILON = 0.001f

    data class InteractionData(
        // val initialRayOrigin: Vector3,
        // val initialRayDirection: Vector3,
        val initialHitPoint: Vector3,
        // val initialEntityRotation: Quaternion,
        val initialHitDistance: Float,
        val initialHitOffsetFromObjOrigin: Vector3,
        var performedMove: Boolean
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
                // inputEvent.origin,
                // inputEvent.direction,
                hitPoint,
                // entity.getPose().rotation,
                (hitPoint - inputEvent.origin).length,
                hitPoint - interactionPlaneP,
                false)

        } else if (inputEvent.action == InputEvent.ACTION_UP) {
            val ci = this.currentInteraction
            if (ci == null || !ci.performedMove) {
                // bubble the event as a tap if it wasn't handled
                this.onTap?.onInputEvent(inputEvent)
            }
            this.currentInteraction = null
        } else if (inputEvent.action == InputEvent.ACTION_MOVE) {
            val ci = this.currentInteraction
            if (ci == null) {
                return
            }

            val targetPosition = (inputEvent.direction.toNormalized() * ci.initialHitDistance) + inputEvent.origin

            val distance = (targetPosition - ci.initialHitPoint).length

            if (distance > deadZone) {
                this.currentInteraction?.performedMove = true
                this.entity.setPose(Pose(targetPosition - ci.initialHitOffsetFromObjOrigin, this.entity.getPose().rotation))
            }

            // val currentPosition = this.entity.getPose().translation

        }
    }
}
