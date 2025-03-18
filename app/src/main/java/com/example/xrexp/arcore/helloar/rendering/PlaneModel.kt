package com.example.xrexp.arcore.helloar.rendering

import androidx.xr.arcore.Plane
import androidx.xr.scenecore.Entity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

/** Represents a rendered plane model. */
data class PlaneModel(
    val id: Int,
    val type: Plane.Type,
    val stateFlow: StateFlow<Plane.State>,
    internal val entity: Entity,
    internal val renderJob: Job?,
) {}
