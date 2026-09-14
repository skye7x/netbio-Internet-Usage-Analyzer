package com.bzygordev.netbio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.bzygordev.netbio.ui.components.GlassGlowBackground
import com.bzygordev.netbio.ui.navigation.AppNavigation

@Composable
fun NetBioApp() {
    val navController = rememberNavController()
    Box(modifier = Modifier.fillMaxSize()) {
        GlassGlowBackground(modifier = Modifier.fillMaxSize())
        AppNavigation(navController = navController)
    }
}
