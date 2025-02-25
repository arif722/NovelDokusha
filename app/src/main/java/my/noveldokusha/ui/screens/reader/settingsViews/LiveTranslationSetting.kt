package my.noveldokusha.ui.screens.reader.settingsViews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import my.noveldokusha.R
import my.noveldokusha.tools.TranslationModelState
import my.noveldokusha.ui.composeViews.MyButton
import my.noveldokusha.utils.*

@Composable
fun LiveTranslationSetting(
    enable: Boolean,
    listOfAvailableModels: SnapshotStateList<TranslationModelState>,
    source: State<TranslationModelState?>,
    target: State<TranslationModelState?>,
    onEnable: (Boolean) -> Unit,
    onSourceChange: (TranslationModelState?) -> Unit,
    onTargetChange: (TranslationModelState?) -> Unit,
    onDownloadTranslationModel: (language: String) -> Unit,
) {
    var modelSelectorExpanded by rememberSaveable { mutableStateOf(false) }
    var modelSelectorExpandedForTarget by rememberSaveable { mutableStateOf(false) }
    var rowSize by remember { mutableStateOf(Size.Zero) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { layoutCoordinates ->
                rowSize = layoutCoordinates.size.toSize()
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .backgroundCircle()
                .outlineCircle()
                .blockInteraction(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MyButton(
                text = stringResource(id = R.string.live_translation),
                onClick = { onEnable(!enable) },
                shape = CircleShape,
                selected = enable,
                outerPadding = 2.dp,
                borderWidth = Dp.Unspecified,
            )
            AnimatedVisibility(visible = enable) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clickableWithUnboundedIndicator {
                                modelSelectorExpanded = !modelSelectorExpanded
                                modelSelectorExpandedForTarget = false
                            }
                    ) {
                        Text(
                            text = source.value?.locale?.displayLanguage
                                ?: stringResource(R.string.language_source_empty_text),
                            modifier = Modifier
                                .padding(6.dp)
                                .ifCase(source == null) { alpha(0.5f) },
                        )
                    }
                    Icon(Icons.Default.ArrowRightAlt, contentDescription = null)
                    Box(
                        modifier = Modifier
                            .clickableWithUnboundedIndicator {
                                modelSelectorExpanded = !modelSelectorExpanded
                                modelSelectorExpandedForTarget = true
                            }
                    ) {
                        Text(
                            text = target.value?.locale?.displayLanguage
                                ?: stringResource(R.string.language_target_empty_text),
                            modifier = Modifier
                                .padding(6.dp)
                                .ifCase(target == null) { alpha(0.5f) },
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = modelSelectorExpanded,
            onDismissRequest = { modelSelectorExpanded = false },
            offset = DpOffset(0.dp, 10.dp),
            modifier = Modifier
                .heightIn(max = 300.dp)
                .width(with(LocalDensity.current) { rowSize.width.toDp() })
        ) {

            DropdownMenuItem(
                onClick = {
                    if (modelSelectorExpandedForTarget) onTargetChange(null)
                    else onSourceChange(null)
                }
            ) {
                Text(
                    text = stringResource(R.string.language_clear_selection),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider()

            listOfAvailableModels.forEach { item ->
                val isAlreadySelected =
                    if (modelSelectorExpandedForTarget) item.language == target.value?.language
                    else item.language == source.value?.language
                DropdownMenuItem(
                    onClick = {
                        if (modelSelectorExpandedForTarget) onTargetChange(item)
                        else onSourceChange(item)
                        modelSelectorExpanded = false
                    },
                    enabled = !isAlreadySelected && item.available
                ) {
                    Box(Modifier.weight(1f)) {
                        Text(
                            text = item.locale.displayLanguage,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .align(Alignment.Center)
                        )
                        if (!item.available) Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .widthIn(min = 22.dp)
                                .height(22.dp)
                                .align(Alignment.CenterEnd)
                        ) {
                            when {
                                item.downloading -> IconButton(onClick = { }, enabled = false) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = MaterialTheme.colors.onPrimary
                                    )
                                }
                                else -> IconButton(
                                    onClick = { onDownloadTranslationModel(item.language) }) {
                                    Icon(
                                        Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = if (item.downloadingFailed) Color.Red
                                        else LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}