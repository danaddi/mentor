package com.example.mentor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.navigation.MentorNavGraph
import com.example.mentor.ui.theme.MentorTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var tokenDataStore: TokenDataStore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MentorTheme {
                MentorNavGraph(tokenDataStore = tokenDataStore)
            }
        }
    }
}
