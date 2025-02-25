package com.example.smsdetection;

//import ai.mlc.mlcchat.ui.theme.MLCChatTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.smsdetection.model.NavView


class MainActivity2 : ComponentActivity() {

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
            ) {
//                MLCChatTheme {
                NavView()
//                }
            }
        }
    }
}