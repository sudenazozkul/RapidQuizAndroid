package com.rapidquiz.ui.result

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rapidquiz.ui.theme.AppBackground

// M4'te uygulanacak. Aktif oturum yoksa Ana Sayfa'ya yönlendirme (guard) de M4'te eklenecek.
@Composable
fun ResultScreen(slug: String, onExit: () -> Unit) {
    AppBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
            Text("Sonuç: $slug (M4)")
            Button(onClick = onExit) { Text("Ana Sayfa") }
        }
    }
}
