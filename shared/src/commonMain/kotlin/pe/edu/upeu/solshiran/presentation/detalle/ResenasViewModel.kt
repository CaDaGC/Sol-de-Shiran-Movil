package pe.edu.upeu.solshiran.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.solshiran.data.remote.ResenasApiService
import pe.edu.upeu.solshiran.data.remote.dto.PublicarResenaDto
import pe.edu.upeu.solshiran.data.remote.dto.ResenaDto
import pe.edu.upeu.solshiran.domain.repository.AuthRepository

data class ResenasUiState(
    val opiniones: List<ResenaDto> = emptyList(),
    val pagina: Int = 0,
    val hayMas: Boolean = false,
    val cargando: Boolean = true,
    val error: String? = null,
    val esCliente: Boolean = false,
    val comprobando: Boolean = true,
    val idReserva: Long? = null,
    val errorElegibilidad: String? = null,
    val enviando: Boolean = false,
    val mensaje: String? = null
)

class ResenasViewModel(
    private val idCabana: Long,
    private val api: ResenasApiService,
    private val auth: AuthRepository
) : ViewModel() {
    private val _estado = MutableStateFlow(ResenasUiState())
    val estado = _estado.asStateFlow()
    private var carga: Job? = null
    private var ultimaPaginaSolicitada = 0

    init {
        cargar()
        verificarElegibilidad()
    }

    fun cargar(pagina: Int = 0) {
        if (pagina > 0 && (carga?.isActive == true || !_estado.value.hayMas)) return
        if (pagina == 0) carga?.cancel()
        ultimaPaginaSolicitada = pagina
        _estado.update { it.copy(cargando = true, error = null) }
        carga = viewModelScope.launch {
            try {
                val respuesta = api.listar(idCabana, pagina)
                _estado.update {
                    it.copy(
                        opiniones = if (pagina == 0) respuesta.opiniones else it.opiniones + respuesta.opiniones,
                        pagina = pagina,
                        hayMas = respuesta.hayMas,
                        cargando = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = e.message ?: "No se pudieron cargar las resenas.") }
            }
        }
    }

    fun cargarMas() = cargar(_estado.value.pagina + 1)

    fun reintentarCarga() = cargar(ultimaPaginaSolicitada)

    fun verificarElegibilidad() {
        viewModelScope.launch {
            try {
                val sesion = auth.recuperarSesion()
                if (sesion?.rol != "CLIENTE") {
                    _estado.update { it.copy(esCliente = false, idReserva = null, comprobando = false) }
                    return@launch
                }
                _estado.update { it.copy(esCliente = true, comprobando = true, errorElegibilidad = null) }
                val respuesta = api.elegibilidad(idCabana, sesion.token)
                _estado.update { it.copy(idReserva = respuesta.idReserva, comprobando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(idReserva = null, comprobando = false, errorElegibilidad = e.message) }
            }
        }
    }

    fun publicar(puntuacion: Int, comentario: String) {
        val reserva = _estado.value.idReserva ?: return
        val texto = comentario.trim()
        if (_estado.value.enviando || puntuacion !in 1..5 || texto.isEmpty() || texto.length > 2000) return

        _estado.update { it.copy(enviando = true, errorElegibilidad = null, mensaje = null) }
        viewModelScope.launch {
            try {
                val sesion = auth.recuperarSesion()
                    ?: throw IllegalStateException("Inicia sesion para publicar una resena.")
                api.publicar(idCabana, sesion.token, PublicarResenaDto(reserva, puntuacion, texto))
                _estado.update { it.copy(enviando = false, idReserva = null, mensaje = "Resena publicada.") }
                cargar()
                verificarElegibilidad()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(enviando = false, errorElegibilidad = e.message) }
            }
        }
    }
}
