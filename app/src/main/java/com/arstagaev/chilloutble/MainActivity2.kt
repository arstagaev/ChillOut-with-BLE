package com.arstagaev.chilloutble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.arstagaev.chilloutble.ui.theme.ChillOutBLETheme
import kotlinx.coroutines.delay

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            var isA by remember { mutableStateOf(true) }
            var isB by remember { mutableStateOf(true) }
            var isC by remember { mutableStateOf(true) }
            ChillOutBLETheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    LaunchedEffect(key1 = isA) {
                        delay(1000)
                    }
                    Box(modifier = Modifier.fillMaxSize().background(Color.Red)) {

                         Button(onClick = {
                             if (isA) {
                                 isA = !isA
                             }

                         }) {

                         }

                    }
//                    if (isC) {
//                        Box(modifier = Modifier.fillMaxSize().background(Color.Blue)) {
//
//                            Button(onClick = {
//                                isC = !isC
//                            }) {
//
//                            }
//
//                        }
//                    }
//                    if (isB) {
//                        Box(modifier = Modifier.fillMaxSize().background(Color.Yellow)) {
//
//                            Button(onClick = {
//                                isB = !isB
//                            }) {
//
//                            }
//
//                        }
//                    }
//                    if (isA) {
//                        Box(modifier = Modifier.fillMaxSize().background(Color.Red)) {
//
//                            Button(onClick = {
//                                isA = !isA
//                            }) {
//
//                            }
//
//                        }
//                    }


                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChillOutBLETheme {
        Greeting("Android")
    }
}