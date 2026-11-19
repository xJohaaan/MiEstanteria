package com.example.miestanteria

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.miestanteria.ui.navigation.AppNavHost
import com.example.miestanteria.ui.theme.AppTheme
import com.example.miestanteria.ui.theme.MiEstanteriaTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

/** Unica Activity: aloja la navegacion entre pantallas hechas con Jetpack Compose. */
class MainActivity : ComponentActivity() {

    private val pedirRedLocal = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pedirPermisoRedLocalSiHaceFalta()
        val themePreference = (application as MiEstanteriaApp).container.themePreference
        
        setContent {
            val currentTheme by themePreference.tema.collectAsState(
                initial = AppTheme.SYSTEM
            )
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (currentTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> systemDarkTheme
            }
            
            MiEstanteriaTheme(darkTheme = darkTheme) {
                AppNavHost()
            }
        }
    }

    /**
     * En desarrollo el servidor corre en el PC (red local). Desde Android 17 (API 37)
     * eso requiere el permiso ACCESS_LOCAL_NETWORK, declarado solo en src/debug.
     */
    private fun pedirPermisoRedLocalSiHaceFalta() {
        if (!BuildConfig.DEBUG || Build.VERSION.SDK_INT < 37) return
        val permiso = "android.permission.ACCESS_LOCAL_NETWORK"
        if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
            pedirRedLocal.launch(permiso)
        }
    }
}
