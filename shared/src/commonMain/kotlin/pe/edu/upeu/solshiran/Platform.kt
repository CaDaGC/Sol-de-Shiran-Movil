package pe.edu.upeu.solshiran

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform