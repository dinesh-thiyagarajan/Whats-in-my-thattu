package com.dineshworkspace.whatsinmythattu.ui.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.dineshworkspace.tensorimageinterpreter.FoodMatch
import com.dineshworkspace.whatsinmythattu.R
import com.dineshworkspace.whatsinmythattu.ui.theme.ConfidenceHigh
import com.dineshworkspace.whatsinmythattu.ui.theme.ConfidenceLow
import com.dineshworkspace.whatsinmythattu.ui.theme.ConfidenceMedium
import com.dineshworkspace.whatsinmythattu.ui.viewModels.ImageInterpreterViewModel
import kotlinx.coroutines.delay

@Preview(showBackground = true)
@Composable
fun FoodMatchesComposablePreview(
    @PreviewParameter(FoodMatchesPreviewParameterProvider::class) foodMatches: List<FoodMatch>
) {
    FoodMatchesComposable(foodMatches = foodMatches)
}

class FoodMatchesPreviewParameterProvider : PreviewParameterProvider<List<FoodMatch>> {
    override val values = sequenceOf(
        listOf(
            FoodMatch(0.92f, "Rose Milk", ""),
            FoodMatch(0.85f, "Egg Puffs", ""),
            FoodMatch(0.67f, "Mutton Dosai", ""),
            FoodMatch(0.45f, "Sambar Soru", "")
        )
    )
}

@Composable
fun FoodMatchesScreen(
    imageInterpreterViewModel: ImageInterpreterViewModel,
    onBackButtonPressed: () -> Unit
) {
    val foodMatches = imageInterpreterViewModel.foodMatches.collectAsState()

    Scaffold(
        topBar = { FoodMatchesAppBar(onBackButtonPressed = onBackButtonPressed) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (foodMatches.value.isEmpty()) {
            EmptyState(modifier = Modifier.padding(paddingValues))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${foodMatches.value.size} matches found",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                FoodMatchesComposable(foodMatches.value)
            }
        }
    }

    BackHandler {
        onBackButtonPressed.invoke()
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No matches found",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try taking another photo or picking a different image",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodMatchesAppBar(onBackButtonPressed: () -> Unit) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Text(
                "Food Matches",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = { onBackButtonPressed.invoke() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

@Composable
fun FoodMatchesComposable(foodMatches: List<FoodMatch>) {
    GridListWithRoundedCardViews(data = foodMatches)
}

private fun getResourceId(index: Int): Int {
    return when (index) {
        1 -> R.drawable.ic_food_1
        2 -> R.drawable.ic_food_2
        3 -> R.drawable.ic_food_3
        4 -> R.drawable.ic_food_4
        5 -> R.drawable.ic_food_5
        6 -> R.drawable.ic_food_6
        7 -> R.drawable.ic_food_7
        8 -> R.drawable.ic_food_8
        else -> R.drawable.ic_food_1
    }
}

@Composable
fun GridListWithRoundedCardViews(data: List<FoodMatch>, numColumns: Int = 2) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(numColumns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(data) { index, match ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(index * 80L)
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300)) + slideInVertically(
                    tween(300),
                    initialOffsetY = { it / 4 }
                )
            ) {
                FoodMatchCard(
                    imageResId = getResourceId(match.imageRandomId),
                    title = match.displayName.ifEmpty { match.label },
                    confidence = match.score,
                    confidenceText = match.confidencePercent
                )
            }
        }
    }
}

@Composable
fun FoodMatchCard(
    imageResId: Int,
    title: String,
    confidence: Float,
    confidenceText: String
) {
    val confidenceColor = when {
        confidence >= 0.10f -> ConfidenceHigh
        confidence >= 0.05f -> ConfidenceMedium
        else -> ConfidenceLow
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = imageResId)
                        .apply { scale(Scale.FIT) }
                        .build()
                ),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { confidence.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(MaterialTheme.shapes.small),
                        color = confidenceColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = confidenceText,
                        style = MaterialTheme.typography.labelMedium,
                        color = confidenceColor
                    )
                }
            }
        }
    }
}
