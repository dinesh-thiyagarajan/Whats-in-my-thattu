package com.dineshworkspace.whatsinmythattu.ui.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.dineshworkspace.whatsinmythattu.ui.viewModels.ImageInterpreterViewModel


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
            FoodMatch(9.0f, "Rose Milk", ""),
            FoodMatch(9.0f, "Egg Puffs", ""),
            FoodMatch(9.0f, "Mutton Dosai", ""),
            FoodMatch(9.0f, "Sambar Soru", ""),
        )
    )
}

@Composable
fun FoodMatchesScreen(
    imageInterpreterViewModel: ImageInterpreterViewModel, onBackButtonPressed: () -> Unit,
) {
    val foodMatches = imageInterpreterViewModel.foodMatches.collectAsState()

    Scaffold(
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Suggestions")
                    Spacer(modifier = Modifier.height(20.dp))
                    FoodMatchesComposable(foodMatches.value)
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar(onBackButtonPressed = onBackButtonPressed) })

    BackHandler {
        onBackButtonPressed.invoke()
    }
}


@Preview(showBackground = true)
@Composable
fun AppBarPreview() {
    AppBar {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(onBackButtonPressed: () -> Unit) {
    TopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary,
    ), title = {
        Text(
            "Food Matches", maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }, navigationIcon = {
        IconButton(onClick = {
            onBackButtonPressed.invoke()
        }) {
            Icon(
                imageVector = Icons.Sharp.ArrowBack,
                contentDescription = "back"
            )
        }
    })
}

@Composable
fun FoodMatchesComposable(
    foodMatches: List<FoodMatch>
) {
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
        else -> R.drawable.ic_food_1 // Default fallback
    }
}

@Composable
fun GridListWithRoundedCardViews(
    data: List<FoodMatch>,
    numColumns: Int = 2
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(numColumns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(data.size) { index ->
            RoundedCardView(
                imageResId = getResourceId(data[index].imageRandomId),
                title = data[index].label,
                description = "Score: ${data[index].score}%",
            )
        }
    }
}

@Composable
fun RoundedCardView(imageResId: Int, title: String, description: String) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray, //Card background color
            contentColor = Color.Black  //Card content color,e.g.text
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder
                        (LocalContext.current).data(data = imageResId)
                        .apply(block = fun ImageRequest.Builder.() {
                            scale(Scale.FIT)
                        }).build()
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}