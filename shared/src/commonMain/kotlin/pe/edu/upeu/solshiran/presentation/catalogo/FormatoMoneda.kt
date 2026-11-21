package pe.edu.upeu.solshiran.presentation.catalogo

import kotlin.math.roundToLong

fun formatearSoles(valor: Double): String {
    if (!valor.isFinite() || valor < 0) {
        return "No disponible"
    }

    val centimos = (valor * 100).roundToLong()
    val parteEntera = centimos / 100
    val parteDecimal = (centimos % 100)
        .toString()
        .padStart(2, '0')

    return "S/ $parteEntera.$parteDecimal"
}