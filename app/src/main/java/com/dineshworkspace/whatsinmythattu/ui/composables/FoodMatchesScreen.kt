package com.dineshworkspace.whatsinmythattu.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.dineshworkspace.tensorimageinterpreter.FoodMatch


@Preview(showBackground = true)
@Composable
fun FoodMatchesComposablePreview(
    @PreviewParameter(FoodMatchesPreviewParameterProvider::class) foodMatches: List<FoodMatch>
) {
    FoodMatchesComposable(foodMatches) {}
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodMatchesScreen(
    foodMatches: List<FoodMatch>, onBackButtonPressed: () -> Unit
) {
    Scaffold(content = {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            color = MaterialTheme.colorScheme.background
        ) {
            FoodMatchesComposable(foodMatches, onBackButtonPressed)
        }
    }, modifier = Modifier.fillMaxSize(), topBar = { AppBar() })
}


@Preview(showBackground = true)
@Composable
fun AppBarPreview() {
    AppBar()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar() {
    TopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary,
    ), title = {
        Text(
            "Food Matches", maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }, navigationIcon = {
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Sharp.ArrowBack,
                contentDescription = "back"
            )
        }
    })
}

@Composable
fun FoodMatchesComposable(
    foodMatches: List<FoodMatch>, onBackButtonPressed: () -> Unit
) {
    Column {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)
        ) {
            items(foodMatches) { item ->
                FoodItem(item)
            }
        }

        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Button(onClick = { onBackButtonPressed.invoke() }) {
                Text(text = "Repick Image")
            }
        }
    }
}

@Composable
fun FoodItem(foodMatch: FoodMatch) {
    Row {
        Text(text = foodMatch.label)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = foodMatch.score.toString())
    }
}