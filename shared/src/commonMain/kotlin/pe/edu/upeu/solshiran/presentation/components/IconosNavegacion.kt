package pe.edu.upeu.solshiran.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun IconoMenu() {
    val color = LocalContentColor.current

    Canvas(
        modifier = Modifier.size(24.dp)
    ) {
        val grosor = 2.dp.toPx()

        listOf(0.25f, 0.5f, 0.75f).forEach { posicion ->
            drawLine(
                color = color,
                start = Offset(
                    x = size.width * 0.15f,
                    y = size.height * posicion
                ),
                end = Offset(
                    x = size.width * 0.85f,
                    y = size.height * posicion
                ),
                strokeWidth = grosor,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun IconoVolver() {
    val color = LocalContentColor.current

    Canvas(
        modifier = Modifier.size(24.dp)
    ) {
        val grosor = 2.dp.toPx()

        val izquierda = Offset(
            size.width * 0.2f,
            size.height * 0.5f
        )

        drawLine(
            color = color,
            start = izquierda,
            end = Offset(
                size.width * 0.8f,
                size.height * 0.5f
            ),
            strokeWidth = grosor,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = izquierda,
            end = Offset(
                size.width * 0.5f,
                size.height * 0.2f
            ),
            strokeWidth = grosor,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = izquierda,
            end = Offset(
                size.width * 0.5f,
                size.height * 0.8f
            ),
            strokeWidth = grosor,
            cap = StrokeCap.Round
        )
    }
}