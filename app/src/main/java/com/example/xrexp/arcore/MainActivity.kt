package com.example.xrexp.arcore

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xrexp.arcore.handtracking.HandTrackingActivity as HandTrackingActivity
import com.example.xrexp.arcore.helloar.HelloArActivity as HelloArActivity
import com.example.xrexp.arcore.persistentanchors.PersistentAnchorsActivity as PersistentAnchorsActivity
import java.text.SimpleDateFormat
import java.util.Locale

/** Entrypoint for testing various ARCore for Android XR functionalities. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { WhiteboxHomeScreen() }
    }
}

@Composable
fun WhiteboxHomeScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = Color.White) {
        Column() {
            Text(
                "AR Whitebox Test Application",
                modifier = Modifier.padding(20.dp),
                fontSize = 30.sp,
                color = Color.Black,
            )
            VersionInfoCard()
            WhiteboxSessionMenu()
        }
    }
}

@Composable
fun VersionInfoCard() {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Build Fingerprint: ${Build.FINGERPRINT}")
            Text(
                "Date: ${SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.ENGLISH).format(Build.TIME)}"
            )
            Text("CL Number: N/A")
        }
    }
}

@Composable
fun WhiteboxSessionMenu() {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Test Activity List",
            modifier = Modifier.padding(20.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
        HorizontalDivider()
        TextButton(
            onClick = { context.startActivity(Intent(context, HelloArActivity::class.java)) }
        ) {
            Text("Hello AR")
        }
        TextButton(
            onClick = {
                context.startActivity(Intent(context, PersistentAnchorsActivity::class.java))
            }
        ) {
            Text("Persistent Anchors")
        }
        TextButton(
            onClick = { context.startActivity(Intent(context, HandTrackingActivity::class.java)) }
        ) {
            Text("Hand Tracking")
        }
    }
}
