package com.example.xrexp.m3
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.xr.compose.spatial.SpatialDialog

@Composable
internal fun DetailPaneDialog() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SimpleDialog()
        MaterialAlertDialog()
        XrElevatedDialog()
    }
}

@Composable
private fun SimpleDialog() {
    DialogWithShowButton("Compose UI Dialog") { showDialog ->
        Dialog(onDismissRequest = { showDialog.value = false }) {
            Card(Modifier.fillMaxWidth(0.8f).fillMaxHeight(0.5f)) {
                Text("This is a simple Dialog with a Material Card inside.")
            }
        }
    }
}

@Composable
private fun MaterialAlertDialog() {
    val context = LocalContext.current
    DialogWithShowButton("Material Alert Dialog") { showDialog ->
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            text = { Text("This is a Material AlertDialog") },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Confirm button clicked", Toast.LENGTH_LONG).show()
                        showDialog.value = false
                    },
                ) {
                    Text("Confirm")
                }
            },
        )
    }
}

@Composable
private fun XrElevatedDialog() {
    DialogWithShowButton("XR ElevatedDialog") { showDialog ->
        SpatialDialog(onDismissRequest = { showDialog.value = false }) {
            Card(Modifier.fillMaxWidth(0.8f).fillMaxHeight(0.5f)) {
                Text("This is an XR ElevatedDialog with a Material Card inside.")
            }
        }
    }
}

@Composable
private fun DialogWithShowButton(
    text: String,
    content: @Composable (MutableState<Boolean>) -> Unit,
) {
    val showDialog = remember { mutableStateOf(false) }
    Button(onClick = { showDialog.value = true }) { Text(text) }
    if (showDialog.value) {
        content(showDialog)
    }
}
