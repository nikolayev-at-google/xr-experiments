package com.google.experiment.soundexplorer.cur

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.Subspace
import androidx.xr.scenecore.Session
import com.google.experiment.soundexplorer.core.GlbModel
import com.google.experiment.soundexplorer.core.GlbModelRepository
import com.google.experiment.soundexplorer.sound.SoundComposition
import com.google.experiment.soundexplorer.ui.SoundObjectComponent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var modelRepository : GlbModelRepository
    private val viewModel : MainViewModel by viewModels()

    private var soundComponents: Array<SoundComposition.SoundCompositionComponent>? = null
    private var soundObjects: Array<SoundObjectComponent>? = null

    fun createSoundObjects(
        session : Session,
        modelRepository : GlbModelRepository,
        glbModels : Array<GlbModel>
    ): Array<SoundObjectComponent> {
        var soundObjs = Array<SoundObjectComponent?>(checkNotNull(soundComponents).size) { null }
        for (i in soundObjs.indices) {
            soundObjs[i] = SoundObjectComponent.createSoundObject(
                session,
                session.activitySpace,
                modelRepository,
                glbModels[i],
                checkNotNull(soundComponents)[i],
                mainExecutor,
                lifecycleScope)

            checkNotNull(soundObjs[i]).hidden = true
        }
        return soundObjs.map({o -> checkNotNull(o)}).toTypedArray()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val session = LocalSession.current
            if (session == null) {
                return@setContent
            }

            this.viewModel.initializeSoundComposition(session)

            val soundsLoaded = this.viewModel.soundPool.soundsLoaded.collectAsState()
            if (!soundsLoaded.value) { // do something while sounds are loading?
                return@setContent
            }

            if (soundComponents == null) {
                soundComponents = arrayOf(
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst01lowId, viewModel.soundPool.inst01midId, viewModel.soundPool.inst01highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst02lowId, viewModel.soundPool.inst02midId, viewModel.soundPool.inst02highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst03lowId, viewModel.soundPool.inst03midId, viewModel.soundPool.inst03highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst04lowId, viewModel.soundPool.inst04midId, viewModel.soundPool.inst04highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst05lowId, viewModel.soundPool.inst05midId, viewModel.soundPool.inst05highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst06lowId, viewModel.soundPool.inst06midId, viewModel.soundPool.inst06highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst07lowId, viewModel.soundPool.inst07midId, viewModel.soundPool.inst07highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst08lowId, viewModel.soundPool.inst08midId, viewModel.soundPool.inst08highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst09lowId, viewModel.soundPool.inst09midId, viewModel.soundPool.inst09highId))
            }

            if (this.soundObjects == null) {
                this.soundObjects = createSoundObjects(
                    session,
                    modelRepository,
                    GlbModel.allGlbAnimatedModels.toTypedArray()
                )
            }

            Subspace {
                MainScreen(modelRepository, checkNotNull(soundObjects))
            }
        }

    }

}