
package cz.downtest.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val bg = Color.rgb(23, 25, 35)
    private val panel = Color.rgb(36, 39, 56)
    private val purple = Color.rgb(139, 92, 246)
    private val gold = Color.rgb(245, 190, 70)
    private val red = Color.rgb(235, 85, 110)

    private lateinit var previewView: PreviewView
    private lateinit var status: TextView
    private lateinit var result: TextView
    private lateinit var history: TextView
    private lateinit var scanButton: Button
    private lateinit var modeButton: Button
    private lateinit var title: TextView
    private lateinit var subtitle: TextView

    private val downScores = mutableListOf<Int>()
    private val auraScores = mutableListOf<Int>()
    private val goonerScores = mutableListOf<Int>()

    // 0 = DOWN, 1 = AURA, 2 = GOONER
    private var currentMode = 0
    private var scanning = false

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(
            this,
            "Bez povolení kamery nelze zobrazit náhled.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun buildUi() {
        val scroll = android.widget.ScrollView(this).apply {
            setBackgroundColor(bg)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(26), dp(18), dp(28))
        }

        scroll.addView(root)
        setContentView(scroll)

        title = text("DOWN TEST 💀", 30f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
        }
        root.addView(title, matchWrap())

        subtitle = text("Zábavný náhodný test", 15f, 0xFFB8BBCA.toInt()).apply {
            gravity = Gravity.CENTER
        }
        root.addView(subtitle, matchWrap())

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(16))
            setBackgroundColor(panel)
        }

        root.addView(card, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(22)
        })

        previewView = PreviewView(this).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }

        card.addView(previewView, LinearLayout.LayoutParams(-1, dp(245)))

        status = text("Připravuji kameru…", 14f, 0xFFCBC8DA.toInt()).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(4))
        }
        card.addView(status, matchWrap())

        scanButton = button("SPUSTIT SCAN")
        scanButton.isEnabled = false
        card.addView(scanButton, matchWrap())
        scanButton.setOnClickListener { runScan() }

        result = text("Down: -- %", 34f, 0xFFC4B5FD.toInt(), true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, dp(6))
        }
        root.addView(result, matchWrap())

        root.addView(text(
            "Výsledek je náhodný a slouží jen pro zábavu.",
            13f,
            0xFFB8BBCA.toInt()
        ).apply {
            gravity = Gravity.CENTER
        }, matchWrap())

        val share = button("SDÍLET VÝSLEDEK")
        root.addView(share, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(12)
        })

        share.setOnClickListener {
            val modeName = when (currentMode) {
                1 -> "AURA TEST"
                2 -> "GOONER TEST"
                else -> "DOWN TEST"
            }

            val send = android.content.Intent(
                android.content.Intent.ACTION_SEND
            ).apply {
                type = "text/plain"
                putExtra(
                    android.content.Intent.EXTRA_TEXT,
                    "Můj výsledek v $modeName: ${result.text}. A jaký máš ty?"
                )
            }

            startActivity(
                android.content.Intent.createChooser(send, "Sdílet výsledek")
            )
        }

        val historyTitle = text("HISTORIE", 18f, Color.WHITE, true).apply {
            setPadding(0, dp(24), 0, dp(8))
        }
        root.addView(historyTitle, matchWrap())

        history = text("Zatím žádné výsledky.", 14f, 0xFFB8BBCA.toInt())
        root.addView(history, matchWrap())

        root.addView(text(
            "Kamera slouží pouze k živému náhledu. Aplikace neanalyzuje obličej, neurčuje inteligenci ani zdravotní stav a snímky neukládá.",
            12f,
            0xFF9296A8.toInt()
        ).apply {
            setPadding(0, dp(22), 0, dp(22))
        }, matchWrap())

        modeButton = button("✨ PŘEPNOUT NA AURA TEST")
        root.addView(modeButton, matchWrap())

        modeButton.setOnClickListener {
            if (!scanning) {
                currentMode = (currentMode + 1) % 3
                updateMode()
            }
        }

        updateMode()
    }

    private fun updateMode() {
        when (currentMode) {
            1 -> {
                title.text = "AURA TEST ✨"
                subtitle.text = "Zábavný náhodný Aura test"
                result.text = "Aura: --"
                result.setTextColor(gold)
                modeButton.text = "🔥 PŘEPNOUT NA GOONER TEST"
                modeButton.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(gold)
            }

            2 -> {
                title.text = "GOONER TEST 🔥"
                subtitle.text = "Zábavné náhodné meme skóre"
                result.text = "Gooner: -- %"
                result.setTextColor(red)
                modeButton.text = "💀 ZPĚT NA DOWN TEST"
                modeButton.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(red)
            }

            else -> {
                title.text = "DOWN TEST 💀"
                subtitle.text = "Zábavný náhodný test"
                result.text = "Down: -- %"
                result.setTextColor(0xFFC4B5FD.toInt())
                modeButton.text = "✨ PŘEPNOUT NA AURA TEST"
                modeButton.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(purple)
            }
        }

        refreshHistory()
    }

    private fun refreshHistory() {
        val scores = when (currentMode) {
            1 -> auraScores
            2 -> goonerScores
            else -> downScores
        }

        val label = when (currentMode) {
            1 -> "Aura"
            2 -> "Gooner"
            else -> "Down"
        }

        history.text = if (scores.isEmpty()) {
            "Zatím žádné výsledky."
        } else {
            scores.mapIndexed { index, score ->
                when (currentMode) {
                    1 -> "${index + 1}. $label: $score / 1000"
                    else -> "${index + 1}. $label: $score %"
                }
            }.joinToString("\n")
        }
    }

    private fun runScan() {
        if (scanning) return

        scanning = true
        scanButton.isEnabled = false
        modeButton.isEnabled = false

        status.text = "Probíhá skenování…"
        result.text = "SCANNING…"

        result.animate().alpha(0.35f).setDuration(350).withEndAction {
            result.animate().alpha(1f).setDuration(350).start()
        }.start()

        val selectedMode = currentMode

        android.os.Handler(mainLooper).postDelayed({
            when (selectedMode) {
                1 -> {
                    val value = Random.nextInt(0, 1001)
                    result.text = String.format(
                        Locale.getDefault(),
                        "Aura: %d / 1000",
                        value
                    )

                    auraScores.add(0, value)
                    if (auraScores.size > 8) {
                        auraScores.removeAt(auraScores.lastIndex)
                    }
                }

                2 -> {
                    val value = Random.nextInt(0, 101)
                    result.text = String.format(
                        Locale.getDefault(),
                        "Gooner: %d %%",
                        value
                    )

                    goonerScores.add(0, value)
                    if (goonerScores.size > 8) {
                        goonerScores.removeAt(goonerScores.lastIndex)
                    }
                }

                else -> {
                    val value = Random.nextInt(0, 101)
                    result.text = String.format(
                        Locale.getDefault(),
                        "Down: %d %%",
                        value
                    )

                    downScores.add(0, value)
                    if (downScores.size > 8) {
                        downScores.removeAt(downScores.lastIndex)
                    }
                }
            }

            refreshHistory()
            status.text = "SCAN COMPLETE"
            scanButton.isEnabled = true
            modeButton.isEnabled = true
            scanning = false
        }, 2200)
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)

        future.addListener({
            try {
                val provider = future.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                provider.unbindAll()
                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview
                )

                status.text = "Kamera připravena"
                scanButton.isEnabled = true
            } catch (e: Exception) {
                status.text = "Kameru se nepodařilo spustit"
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun button(label: String) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 15f
        setTextColor(Color.WHITE)
        backgroundTintList =
            android.content.res.ColorStateList.valueOf(purple)
        setPadding(dp(12), dp(8), dp(12), dp(8))
    }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)

    private fun dp(value: Int) =
        (value * resources.displayMetrics.density).toInt()
}
 