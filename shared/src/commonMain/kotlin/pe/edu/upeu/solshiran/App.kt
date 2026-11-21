package pe.edu.upeu.solshiran

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import pe.edu.upeu.solshiran.presentation.auth.LoginScreen

@Composable
fun App() {
    MaterialTheme {
        Surface {
            LoginScreen()
        }
    }
}