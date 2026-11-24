package pe.indumotos.mobile

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import android.widget.Toast
import android.app.Activity
import android.app.KeyguardManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import pe.indumotos.mobile.domain.*
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : ComponentActivity() {
    private var appActive by mutableStateOf(false)
    private var unlockReceiver: ((Boolean, String?) -> Unit)? = null
    private val deviceUnlock =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val receiver = unlockReceiver;
            unlockReceiver = null
            receiver?.invoke(
                result.resultCode == Activity.RESULT_OK,
                if (result.resultCode == Activity.RESULT_OK) null else "Se canceló el desbloqueo del dispositivo."
            )
        }

    @Suppress("DEPRECATION")
    private fun unlockLocal(receiver: (Boolean, String?) -> Unit) {
        val guard = getSystemService(KeyguardManager::class.java)
        if (!guard.isDeviceSecure) {
            receiver(
                false,
                "Configura un PIN, patrón o contraseña de bloqueo para consultar datos offline."
            );
            return
        }
        if (unlockReceiver != null) {
            receiver(false, "Ya hay un desbloqueo en curso.");
            return
        }
        val intent = guard.createConfirmDeviceCredentialIntent(
            "Indumotos AS",
            "Desbloquea los datos guardados de tu cuenta"
        )
        if (intent == null) {
            receiver(false, "No se pudo abrir el desbloqueo seguro.");
            return
        }
        unlockReceiver = receiver;
        deviceUnlock.launch(intent)
    }

    override fun onStart() {
        super.onStart();
        appActive = true
    }

    override fun onStop() {
        appActive = false;
        super.onStop()
    }

    private var photoReceiver: ((PhotoSelection) -> Unit)? = null
    private var cameraFile: File? = null
    private val camera = registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val receiver = photoReceiver;
        photoReceiver = null
        val file = cameraFile;
        cameraFile = null
        if (!saved || file == null) {
            file?.delete();
            receiver?.invoke(PhotoSelection());
            return@registerForActivityResult
        }
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    PhotoSelection(listOf(readCameraPhoto(file)))
                } catch (e: Exception) {
                    PhotoSelection(error = e.message ?: "No se pudo procesar la fotografía.")
                } finally {
                    file.delete()
                }
            }
            receiver?.invoke(result)
        }
    }
    private val photoPicker =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            val receiver = photoReceiver;
            photoReceiver = null
            if (uris.isEmpty()) {
                receiver?.invoke(PhotoSelection());
                return@registerForActivityResult
            }
            lifecycleScope.launch {
                val selection = withContext(Dispatchers.IO) {
                    runCatching {
                        require(uris.size in 1..3) { "Selecciona entre una y tres fotos." }
                        PhotoSelection(uris.mapIndexed { index, uri ->
                            val bytes = contentResolver.openInputStream(uri)?.use { input ->
                                val out = ByteArrayOutputStream();

                                val buffer = ByteArray(8192)
                                while (true) {
                                    val count = input.read(buffer);
                                    if (count < 0) break;
                                    require(
                                        out.size() + count <= 5 * 1024 * 1024
                                    ) { "Cada foto debe pesar hasta 5 MB." };
                                    out.write(
                                        buffer,
                                        0,
                                        count
                                    )
                                }
                                out.toByteArray()
                            } ?: error("No se pudo leer una foto.")
                            val mime = when {
                                bytes.size > 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() -> "image/png"
                                bytes.size > 2 && bytes[0] == 0xff.toByte() && bytes[1] == 0xd8.toByte() -> "image/jpeg"
                                else -> error("Selecciona únicamente fotos JPG/PNG.")
                            }
                            val bounds = BitmapFactory.Options()
                                .apply { inJustDecodeBounds = true };
                            BitmapFactory.decodeByteArray(
                                bytes,
                                0,
                                bytes.size,
                                bounds
                            )
                            require(bounds.outWidth >= 600 && bounds.outHeight >= 600 && bounds.outWidth.toLong() * bounds.outHeight <= 20_000_000) { "Cada foto debe medir al menos 600 × 600 px y como máximo 20 MP." }
                            GuidePhoto("Foto ${index + 1}", mime, bytes)
                        })
                    }.getOrElse {
                        PhotoSelection(
                            error = it.message ?: "No se pudieron leer las fotos."
                        )
                    }
                }
                receiver?.invoke(selection)
            }
        }

    private fun selectPhotos(receiver: (PhotoSelection) -> Unit) {
        if (photoReceiver != null) {
            receiver(PhotoSelection(error = "Termina la selección de fotos en curso."));
            return
        }
        photoReceiver = receiver
        try {
            photoPicker.launch(arrayOf("image/jpeg", "image/png"))
        } catch (e: Exception) {
            photoReceiver = null;
            receiver(PhotoSelection(error = "No se pudo abrir la galería."))
        }
    }

    private fun capturePhoto(receiver: (PhotoSelection) -> Unit) {
        if (photoReceiver != null) {
            receiver(PhotoSelection(error = "Termina la selección de fotos en curso."));
            return
        }
        photoReceiver = receiver
        try {
            val folder = File(cacheDir, "capturas").apply { mkdirs() }
            val file = File.createTempFile("foto-", ".jpg", folder);
            cameraFile = file
            camera.launch(FileProvider.getUriForFile(this, "$packageName.capturas", file))
        } catch (e: Exception) {
            cameraFile?.delete();
            cameraFile = null;
            photoReceiver = null;
            receiver(
                PhotoSelection(
                    error = "No se pudo abrir la cámara. Puedes elegir una imagen de galería."
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = getSharedPreferences("appearance", MODE_PRIVATE)
        val stored = settings.getString("theme", null)
        val appearance = Appearance.entries.firstOrNull { it.name == stored } ?: Appearance.SYSTEM

        setContent {
            App(
                backendUrl = BuildConfig.BACKEND_URL,
                pickGuidePhotos = ::selectPhotos,
                takePhoto = ::capturePhoto,
                initialTheme = appearance,
                onAppearanceChanged = { settings.edit().putString("theme", it.name).apply() },
                onDarkTheme = { dark -> window.decorView.post { updateSystemBars(dark) } },
                installSystemBack = { enabled, handler -> BackHandler(enabled, handler) },
                onExitHint = {
                    Toast.makeText(
                        this,
                        "Pulsa Atrás otra vez para salir",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onExitRequested = { finish() },
                unlockOffline = ::unlockLocal,
                isActive = appActive
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun updateSystemBars(dark: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val lightBars = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            window.insetsController?.setSystemBarsAppearance(if (dark) 0 else lightBars, lightBars)
        } else {
            var lightBars = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                lightBars = lightBars or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            val decor = window.decorView
            decor.systemUiVisibility = (decor.systemUiVisibility and lightBars.inv()) or
                    (if (dark) 0 else lightBars)
        }
    }
}
