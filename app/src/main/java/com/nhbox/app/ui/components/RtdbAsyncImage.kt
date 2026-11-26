package com.nhbox.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import com.nhbox.app.data.repository.ImageRtdbRepository
import com.nhbox.app.ui.theme.NhCardBg
import com.nhbox.app.ui.theme.NhMutedGray
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ImageRepoEntryPoint {
    fun imageRtdbRepository(): ImageRtdbRepository
}

@Composable
fun RtdbAsyncImage(
    source: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val isRtdb = source.startsWith("rtdb://")

    if (source.isBlank()) {
        Box(
            modifier = modifier.background(NhCardBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Movie,
                contentDescription = contentDescription,
                tint = NhMutedGray
            )
        }
        return
    }

    if (isRtdb) {
        val imageId = source.removePrefix("rtdb://")
        var bitmap by remember(imageId) { mutableStateOf<Bitmap?>(null) }
        var loaded by remember(imageId) { mutableStateOf(false) }

        LaunchedEffect(imageId) {
            val repo = EntryPointAccessors.fromApplication(
                context.applicationContext,
                ImageRepoEntryPoint::class.java
            ).imageRtdbRepository()
            bitmap = repo.getBitmap(imageId)
            loaded = true
        }

        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        } else {
            Box(
                modifier = modifier.background(NhCardBg),
                contentAlignment = Alignment.Center
            ) {
                if (loaded) {
                    Icon(
                        imageVector = Icons.Outlined.Movie,
                        contentDescription = contentDescription,
                        tint = NhMutedGray
                    )
                }
            }
        }
    } else {
        SubcomposeAsyncImage(
            model = source,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            loading = {
                Box(modifier = Modifier.fillMaxSize().background(NhCardBg))
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize().background(NhCardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Movie,
                        contentDescription = contentDescription,
                        tint = NhMutedGray
                    )
                }
            }
        )
    }
}
