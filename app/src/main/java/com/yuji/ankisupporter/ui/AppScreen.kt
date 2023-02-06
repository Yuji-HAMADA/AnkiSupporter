package com.yuji.ankisupporter.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yuji.ankisupporter.R
import com.yuji.ankisupporter.WordRecognizer

@Composable
fun AppScreen (
    modifier: Modifier = Modifier,
    score: Int,
    inputWord: String,
    onWordChanged: (String) -> Unit,
    onNextButtonClicked: () -> Unit = {}
)
{
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppStatus(
            score = score
        )
        AppLayout(
            inputWord = inputWord,
            onWordChanged = onWordChanged
        )
        Button(
            onClick = onNextButtonClicked
        ) {
            Text("Next")
        }
    }
}

@Composable
fun AppStatus(
    score: Int,
    modifier: Modifier = Modifier
)
{
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .size(48.dp),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
            text = stringResource(R.string.score, score),
            fontSize = 18.sp,
        )
    }
}

@Composable
fun AppLayout(
    inputWord: String,
    onWordChanged: (String) -> Unit
)
{
    Column {
        Button(onClick = { WordRecognizer.startRecognizer() }) {
            Text(stringResource(R.string.record))
        }
        Button(onClick = { }) {
            Text(stringResource(R.string.stop))
        }
        Button(onClick = { }) {
            Text(stringResource(R.string.playback))
        }
        Button(onClick = { }) {
            Text(stringResource(R.string.recognize))
        }
        OutlinedTextField(
            value = inputWord,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = onWordChanged,
            label = { Text(stringResource(R.string.enter_your_word)) },
            isError = false,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { }
            ),
        )
    }
}