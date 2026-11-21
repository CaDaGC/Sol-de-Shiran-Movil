package pe.edu.upeu.solshiran.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pe.edu.upeu.solshiran.data.mapper.aDominio
import pe.edu.upeu.solshiran.data.mapper.aDto
import pe.edu.upeu.solshiran.data.remote.dto.LoginResponseDto
import pe.edu.upeu.solshiran.domain.model.Sesion

class AlmacenSesionAndroid(
    context: Context
) : AlmacenSesion {

    private val preferencias = context.applicationContext
        .getSharedPreferences(
            "sesion_segura",
            Context.MODE_PRIVATE
        )

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun guardar(
        sesion: Sesion
    ) = withContext(Dispatchers.IO) {
        val contenido = json.encodeToString(sesion.aDto())

        val cifrador = Cipher.getInstance("AES/GCM/NoPadding")
        cifrador.init(Cipher.ENCRYPT_MODE, obtenerClave())

        val cifrado = cifrador.doFinal(
            contenido.toByteArray(Charsets.UTF_8)
        )

        val iv = Base64.encodeToString(
            cifrador.iv,
            Base64.NO_WRAP
        )

        val datos = Base64.encodeToString(
            cifrado,
            Base64.NO_WRAP
        )

        check(
            preferencias.edit()
                .putString("contenido", "$iv:$datos")
                .commit()
        ) {
            "No se pudo guardar la sesión."
        }
    }

    override suspend fun recuperar(): Sesion? =
        withContext(Dispatchers.IO) {
            val contenido = preferencias
                .getString("contenido", null)
                ?: return@withContext null

            try {
                val partes = contenido.split(":", limit = 2)

                require(partes.size == 2)

                val iv = Base64.decode(
                    partes[0],
                    Base64.NO_WRAP
                )

                val datos = Base64.decode(
                    partes[1],
                    Base64.NO_WRAP
                )

                val cifrador = Cipher.getInstance(
                    "AES/GCM/NoPadding"
                )

                cifrador.init(
                    Cipher.DECRYPT_MODE,
                    obtenerClave(),
                    GCMParameterSpec(128, iv)
                )

                val texto = cifrador
                    .doFinal(datos)
                    .toString(Charsets.UTF_8)

                json.decodeFromString<LoginResponseDto>(texto)
                    .aDominio()
            } catch (_: Exception) {
                preferencias.edit()
                    .remove("contenido")
                    .commit()

                null
            }
        }

    override suspend fun borrar() =
        withContext(Dispatchers.IO) {
            check(
                preferencias.edit()
                    .remove("contenido")
                    .commit()
            ) {
                "No se pudo borrar la sesión."
            }
        }

    private fun obtenerClave(): SecretKey {
        val almacen = KeyStore.getInstance("AndroidKeyStore")
        almacen.load(null)

        val existente = almacen.getKey(
            "sol_shiran_sesion",
            null
        ) as? SecretKey

        if (existente != null) {
            return existente
        }

        val generador = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        generador.init(
            KeyGenParameterSpec.Builder(
                "sol_shiran_sesion",
                KeyProperties.PURPOSE_ENCRYPT or
                        KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(
                    KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .build()
        )

        return generador.generateKey()
    }
}