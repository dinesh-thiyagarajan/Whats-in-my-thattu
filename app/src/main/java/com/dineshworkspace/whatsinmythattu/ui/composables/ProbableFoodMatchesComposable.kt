package com.dineshworkspace.whatsinmythattu.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dineshworkspace.tensorimageinterpreter.ProbableFoodMatch

@Composable
fun ProbableFoodMatchesComposable(foodMatches: List<ProbableFoodMatch>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(foodMatches) { item ->
            ProbableMatchFoodItem(item)
        }
    }
}

@Composable
fun ProbableMatchFoodItem(probableFoodMatch: ProbableFoodMatch) {
    Text(text = probableFoodMatch.label)
}