package com.example.xrexp.arcore.helloar.rendering

import androidx.xr.arcore.Anchor
import androidx.xr.scenecore.Entity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

/** Represents a rendered anchor model. */
data class AnchorModel(
    val id: Int,
    val stateFlow: StateFlow<Anchor.State>,
    internal val entity: Entity,
    internal val renderJob: Job?,
) {}
