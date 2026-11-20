package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.MidnightBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object DrawableBitmapCache {
    private val cache = LruCache<String, ImageBitmap>(16)

    fun get(resId: Int, maxDimPx: Int): ImageBitmap? = cache.get("$resId:$maxDimPx")

    fun put(resId: Int, maxDimPx: Int, bitmap: ImageBitmap) {
        cache.put("$resId:$maxDimPx", bitmap)
    }
}

/**
 * Decodes large JPEG/PNG drawables asynchronously off the UI thread with downsampling
 * (`inSampleSize`) and caches the resulting [ImageBitmap] in memory so first-frame
 * rendering and SurfaceSyncGroup transactions never stall on main-thread bitmap decoding.
 */
@Composable
fun SampledDrawableImage(
    @DrawableRes resId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxDimPx: Int = 480,
    contentScale: ContentScale = ContentScale.Crop,
    colorFilter: ColorFilter? = null
) {
    val context = LocalContext.current
    var bitmap by remember(resId, maxDimPx) {
        mutableStateOf(DrawableBitmapCache.get(resId, maxDimPx))
    }

    LaunchedEffect(resId, maxDimPx) {
        if (bitmap == null) {
            val decoded = withContext(Dispatchers.IO) {
                runCatching {
                    val res = context.resources
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeResource(res, resId, bounds)
                    var sampleSize = 1
                    val maxSrc = maxOf(bounds.outWidth, bounds.outHeight)
                    while (maxSrc / (sampleSize * 2) >= maxDimPx && maxDimPx > 0) {
                        sampleSize *= 2
                    }
                    val opts = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }
                    BitmapFactory.decodeResource(res, resId, opts)?.asImageBitmap()
                }.getOrNull()
            }
            if (decoded != null) {
                DrawableBitmapCache.put(resId, maxDimPx, decoded)
                bitmap = decoded
            }
        }
    }

    val loaded = bitmap
    if (loaded != null) {
        Image(
            bitmap = loaded,
            contentDescription = contentDescription,
            contentScale = contentScale,
            colorFilter = colorFilter,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(listOf(MidnightBase, CosmicIndigo))
            )
        )
    }
}
