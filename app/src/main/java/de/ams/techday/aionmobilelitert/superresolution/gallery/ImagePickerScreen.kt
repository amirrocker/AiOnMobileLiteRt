/*
 * Copyright 2024 The Google AI Edge Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ams.techday.aionmobilelitert.superresolution.gallery

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.ams.techday.aionmobilelitert.commons.Delegate
import java.io.InputStream
import kotlin.math.roundToInt

@Composable
fun ImagePickerScreen(
    modifier: Modifier = Modifier,
    delegate: Delegate,
    onInferenceTimeChanged: (Int) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val viewModel: ImagePickerViewModel =
        viewModel(factory = ImagePickerViewModel.getFactory(context))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = uiState.inferenceTime) {
        onInferenceTimeChanged(uiState.inferenceTime)
    }

    LaunchedEffect(key1 = delegate) {
        viewModel.setDelegate(delegate)
    }

    // Register ActivityResult handler
    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val contentResolver = context.contentResolver
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                viewModel.selectImage(bitmap)
            }
        }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        if (uiState.originalBitmap != null) {
            Image(modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        with(density) {
                            viewModel.selectOffset(
//                                offset, Size(maxWidth.toPx(), maxHeight.toPx())
                                offset, Size(400.dp.toPx(), 400.dp.toPx())
                            )
                        }
                    }
                }
                .detectDrag(
                    onDrag = {
                        with(density) {
                            viewModel.selectOffset(
//                                it, Size(maxWidth.toPx(), maxHeight.toPx())
                                it, Size(400.dp.toPx(), 400.dp.toPx())
                            )
                        }
                    },
                ),
                bitmap = uiState.originalBitmap!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )

            val selectPoint = uiState.selectPoint
            if (selectPoint.offset != null) {
                Box(modifier = Modifier
                    .size(
                        (selectPoint.boxSize / density.density).dp,
                        (selectPoint.boxSize / density.density).dp
                    )
                    .offset {
                        IntOffset(
                            selectPoint.offset.x.roundToInt(),
                            selectPoint.offset.y.roundToInt(),
                        )
                    }
                    .border(border = BorderStroke(width = 3.dp, color = Color.Green)))
            }

            if (uiState.sharpenBitmap != null) {
                Image(
                    modifier = Modifier
                        .size(120.dp, 120.dp)
                        .align(Alignment.TopEnd),
                    bitmap = uiState.sharpenBitmap!!.asImageBitmap(),
                    contentDescription = null,
                )
            }
        }

        FloatingActionButton(modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 80.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            shape = CircleShape,
            onClick = {
                val request =
                    PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                imagePicker.launch(request)
            }) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }


//    Box(
//        modifier = modifier.fillMaxSize(),
//    ) {
//        if (uiState.originalBitmap != null) {
//            Image(modifier = Modifier
//                .fillMaxWidth()
//                .verticalScroll(scrollState)
//                .pointerInput(Unit) {
//                    detectTapGestures { offset ->
//                        with(density) {
//                            viewModel.selectOffset(
////                                offset, Size(maxWidth.toPx(), maxHeight.toPx())
//                                offset, Size(400.dp.toPx(), 400.dp.toPx())
//                            )
//                        }
//                    }
//                }
//                .detectDrag(
//                    onDrag = {
//                        with(density) {
//                            viewModel.selectOffset(
////                                it, Size(maxWidth.toPx(), maxHeight.toPx())
//                                it, Size(400.dp.toPx(), 400.dp.toPx())
//                            )
//                        }
//                    },
//                ),
//                bitmap = uiState.originalBitmap!!.asImageBitmap(),
//                contentDescription = null,
//                contentScale = ContentScale.FillWidth
//            )
//
//            val selectPoint = uiState.selectPoint
//            if (selectPoint.offset != null) {
//                Box(modifier = Modifier
//                    .size(
//                        (selectPoint.boxSize / density.density).dp,
//                        (selectPoint.boxSize / density.density).dp
//                    )
//                    .offset {
//                        IntOffset(
//                            selectPoint.offset.x.roundToInt(),
//                            selectPoint.offset.y.roundToInt(),
//                        )
//                    }
//                    .border(border = BorderStroke(width = 3.dp, color = Color.Green)))
//            }
//
//            if (uiState.sharpenBitmap != null) {
//
//                Image(
//                    modifier = Modifier
//                        .size(120.dp, 120.dp)
//                        .align(Alignment.Center),
//                    bitmap = uiState.sharpenBitmap!!.asImageBitmap(),
//                    contentDescription = null,
//                )
//            }
//
//        }
//        FloatingActionButton(
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(bottom = 80.dp, end = 16.dp),
//            containerColor = MaterialTheme.colorScheme.secondary,
//            shape = CircleShape,
//            onClick = {
//                val request =
//                    PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
//                imagePicker.launch(request)
//            }
//        ) {
//            Icon(Icons.Filled.Add, contentDescription = null)
//        }
//    }
}

/**
 * Detects drag gestures on a composable element.
 *
 * @param onDrag Callback invoked during a drag gesture.
 * @return A modifier that applies drag gesture detection to the composable.
 */
fun Modifier.detectDrag(
    onDrag: (Offset) -> Unit
): Modifier = composed {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    this.pointerInput(interactionSource) {
        detectDragGestures(
            onDragStart = { },
            onDragEnd = { },
        ) { change, _ ->
            change.consume()
            onDrag(change.position)
        }
    }
}
