package pl.netbio.internetusageanalyzer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import pl.netbio.internetusageanalyzer.ui.components.GlassGlowBackground
import pl.netbio.internetusageanalyzer.ui.navigation.AppNavigation

@Composable
fun NetBioApp() {
    val navController = rememberNavController()
    Box(modifier = Modifier.fillMaxSize()) {
        GlassGlowBackground(modifier = Modifier.fillMaxSize())
        AppNavigation(navController = navController)
    }
}
