package com.example.reisi_test_module.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reisi_test_module.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootView(provisionMode: String, currentStreamingState: Int, onSaveClicked: (Int) -> Unit) {

    var context = LocalContext.current
    var streamingState by remember { mutableIntStateOf(currentStreamingState) }
    val rememberCoroutineScope = rememberCoroutineScope()
    var streamingModeDescription = ""


    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(4.dp)
                    .align(Alignment.CenterVertically),
                text = stringResource(R.string.provision_mode),
                fontSize = 12.sp,

                color = Color.DarkGray
            )
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(4.dp)
                    .align(Alignment.CenterVertically),
                text = provisionMode,
                fontSize = 16.sp,
                color = Color.Blue
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            RadioButtonWithText(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(8.dp)
                    .fillMaxWidth(),
                streamingState = streamingState,
                radioButtonIndex = 0,
                radioButtonTitle = stringResource(R.string.not_control),
                onRadioButtonClicked = { clickedIndex ->
                    streamingState = clickedIndex
                },
                onTextClicked = { clickedIndex ->
                    streamingModeDescription = getStreamingModeDescription(clickedIndex)
                    showDescriptionToast(context, streamingModeDescription)
                }
            )

            RadioButtonWithText(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(8.dp)
                    .fillMaxWidth(),
                streamingState = streamingState,
                radioButtonIndex = 1,
                radioButtonTitle = stringResource(R.string.disable),
                onRadioButtonClicked = { clickedIndex ->
                    streamingState = clickedIndex
                },
                onTextClicked = { clickedIndex ->
                    streamingModeDescription = getStreamingModeDescription(clickedIndex)
                    showDescriptionToast(context, streamingModeDescription)
                }
            )

            RadioButtonWithText(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(8.dp)
                    .fillMaxWidth(),
                streamingState = streamingState,
                radioButtonIndex = 2,
                radioButtonTitle = stringResource(R.string.enable),
                onRadioButtonClicked = { clickedIndex ->
                    streamingState = clickedIndex
                },
                onTextClicked = { clickedIndex ->
                    streamingModeDescription = getStreamingModeDescription(clickedIndex)
                    showDescriptionToast(context, streamingModeDescription)
                }
            )

            RadioButtonWithText(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(8.dp)
                    .fillMaxWidth(),
                streamingState = streamingState,
                radioButtonIndex = 3,
                radioButtonTitle = stringResource(R.string.same_managed_account),
                onRadioButtonClicked = { clickedIndex ->
                    streamingState = clickedIndex
                },
                onTextClicked = { clickedIndex ->
                    streamingModeDescription = getStreamingModeDescription(clickedIndex)
                    showDescriptionToast(context, streamingModeDescription)
                }
            )
        }

        Button(
            modifier = Modifier
                .wrapContentHeight()
                .wrapContentWidth()
                .align(Alignment.CenterHorizontally),
            colors = ButtonColors(
                contentColor = Color.Gray,
                containerColor = Color.Blue,
                disabledContentColor = Color.Gray,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(14.dp),
            onClick = { onSaveClicked(streamingState) }
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = "Save",
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

private fun showDescriptionToast(context: Context, streamingModeDescription: String) {
    Toast.makeText(
        context,
        streamingModeDescription,
        Toast.LENGTH_LONG
    ).show()
}

@Composable
fun RadioButtonWithText(
    modifier: Modifier = Modifier,
    streamingState: Int,
    radioButtonIndex: Int,
    radioButtonTitle: String,
    onRadioButtonClicked: (Int) -> Unit,
    onTextClicked: (Int) -> Unit
) {
    Row(modifier) {
        RadioButton(
            onClick = { onRadioButtonClicked(radioButtonIndex) },
            selected = streamingState == radioButtonIndex,
            colors = RadioButtonColors(
                selectedColor = Color.Blue,
                unselectedColor = Color.Gray,
                disabledSelectedColor = Color.Black,
                disabledUnselectedColor = Color.Gray
            )
        )
        Text(
            modifier = Modifier
                .wrapContentSize()
                .padding(2.dp)
                .align(Alignment.CenterVertically)
                .clickable {
                    onTextClicked(radioButtonIndex)
                },
            text = radioButtonTitle,
            fontSize = 12.sp,
            color = Color.DarkGray,
        )
    }
}

@Preview
@Composable
fun RootPreview() {
    RootView(
        "preview mode",
        3
    ) { }
}

fun getStreamingModeDescription(modeIndex: Int): String {
    return when (modeIndex) {
        0 -> "Device is not controlled with any policy from you"
        1 -> "Device nearby app streaming feature will be disable"
        2 -> "Device nearby app streaming feature will be enable"
        else -> "Device will be allowed for interaction only with the apps that have the same protections"
    }
}