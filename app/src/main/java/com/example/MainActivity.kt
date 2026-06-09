package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.RecoveryRepository
import com.example.ui.MainAppScreen
import com.example.ui.RecoveryViewModel
import com.example.ui.RecoveryViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Obtain App Room Database instance and repositories
    val database = AppDatabase.getDatabase(applicationContext)
    val dao = database.recoveryDao()
    val repository = RecoveryRepository(dao)
    
    // Bind VM
    val viewModel = ViewModelProvider(
        this, 
        RecoveryViewModelFactory(application, repository)
    )[RecoveryViewModel::class.java]

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = com.example.ui.theme.ObsidianBlack
        ) {
          MainAppScreen(viewModel = viewModel)
        }
      }
    }
  }
}

