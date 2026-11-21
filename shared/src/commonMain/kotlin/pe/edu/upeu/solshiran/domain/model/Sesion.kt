package pe.edu.upeu.solshiran.domain.model

data class Sesion(
    val token: String,
    val idUsuario: Long,
    val nombreUsuario: String,
    val correo: String,
    val rol: String,
    val expiraEn: Long
) {
    fun estaVigente(ahoraEnSegundos: Long): Boolean {
        return token.isNotBlank() && expiraEn > ahoraEnSegundos
    }
}