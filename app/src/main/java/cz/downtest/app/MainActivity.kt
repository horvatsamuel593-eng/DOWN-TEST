
package cz.downtest.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageButton
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val bg = Color.rgb(23, 25, 35)
    private val panel = Color.rgb(36, 39, 56)
    private val purple = Color.rgb(139, 92, 246)
    private val gold = Color.rgb(245, 190, 70)
    private val red = Color.rgb(235, 85, 110)
    private val white = Color.WHITE
    private val muted = 0xFFB8BBCA.toInt()

    private data class GameMode(
        val name: String,
        val emoji: String,
        val description: String,
        val premium: Boolean,
        val maxScore: Int,
        val percent: Boolean,
        val color: Int
    )

    private val modes = listOf(
        GameMode("DOWN TEST", "💀", "Zábavný náhodný test", false, 100, true, purple),
        GameMode("AURA TEST", "✨", "Zjisti svou náhodnou auru", false, 1000, false, gold),
        GameMode("GOONER TEST", "🔥", "Náhodné meme skóre", false, 100, true, red),
        GameMode("TROLL TEST", "🗿", "Jak velký troll jsi?", true, 100, true, purple),
        GameMode("GIGACHAD TEST", "💪", "Tvoje náhodná Chad energie", true, 1000, false, gold),
        GameMode("BRAINROT TEST", "🧠", "Brainrot level", true, 100, true, red),
        GameMode("NPC TEST", "🎮", "Jak moc NPC dnes jsi?", true, 100, true, purple),
        GameMode("SIGMA TEST", "🐺", "Tvůj náhodný Sigma level", true, 1000, false, gold)
    )

    private val histories = mutableMapOf<Int, MutableList<Int>>()
    private var selectedMode = 0
    private var scanning = false

    private lateinit var previewView: PreviewView
    private lateinit var status: TextView
    private lateinit var result: TextView
    private lateinit var history: TextView
    private lateinit var scanButton: Button

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (::previewView.isInitialized) startCamera()
        } else {
            status.text = "Kamera nebyla povolena."
            Toast.makeText(
                this,
                "Bez povolení kamery nelze zobrazit náhled.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showMenu()
    }

    private fun newRoot(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(24), dp(18), dp(28))
            setBackgroundColor(bg)
        }
    }

    private fun showMenu() {
        scanning = false

        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
        }
        val root = newRoot()
        scroll.addView(root)
        setContentView(scroll)

        root.addView(text("DOWN TEST 💀", 31f, white, true).apply {
            gravity = Gravity.CENTER
        }, match())

        root.addView(text(
            "VYBER SI SVŮJ TEST",
            14f,
            muted
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, dp(24))
        }, match())

        addSection(root, "ZDARMA – 3 REŽIMY", purple)

        modes.forEachIndexed { index, mode ->
            if (!mode.premium) {
                addModeCard(root, index, mode)
            }
        }

        addSection(root, "PREMIUM – 5 REŽIMŮ", gold)

        modes.forEachIndexed { index, mode ->
            if (mode.premium) {
                addModeCard(root, index, mode)
            }
        }

        val premiumButton = button("🔓 OTEVŘÍT VŠECHNY MÓDY – 20 Kč")
        premiumButton.backgroundTintList =
            android.content.res.ColorStateList.valueOf(purple)

        root.addView(premiumButton, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(18)
        })

        premiumButton.setOnClickListener {
            showPremiumDialog()
        }

        root.addView(text(
            "Všechny výsledky jsou náhodné a slouží pouze pro zábavu.",
            12f,
            muted
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, 0)
        }, match())
    }

    private fun addSection(root: LinearLayout, label: String, color: Int) {
        root.addView(text(label, 17f, color, true).apply {
            setPadding(0, dp(12), 0, dp(8))
        }, match())
    }

    private fun addModeCard(
        root: LinearLayout,
        index: Int,
        mode: GameMode
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(15), dp(15), dp(15), dp(15))
            setBackgroundColor(panel)
        }

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        info.addView(text(
            "${mode.emoji}  ${mode.name}",
            17f,
            white,
            true
        ), match())

        info.addView(text(
            mode.description,
            12f,
            muted
        ).apply {
            setPadding(0, dp(4), 0, 0)
        }, match())

        card.addView(info)

        val action = text(
            if (mode.premium) "🔒" else "HRÁT  ›",
            15f,
            if (mode.premium) gold else purple,
            true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(4), dp(8))
        }

        card.addView(action)

        root.addView(card, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(9)
        })

        card.setOnClickListener {
            if (mode.premium) {
                showPremiumDialog()
            } else {
                openMode(index)
            }
        }
    }

    private fun openMode(index: Int) {
        selectedMode = index
        showModeScreen()
    }

    private fun showModeScreen() {
        val mode = modes[selectedMode]

        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
        }
        val root = newRoot()
        scroll.addView(root)
        setContentView(scroll)

        val back = button("‹ ZPĚT DO NABÍDKY")
        root.addView(back, match())
        back.setOnClickListener {
            if (!scanning) {
                stopCamera()
                showMenu()
            }
        }

        root.addView(text(
            "${mode.emoji} ${mode.name}",
            27f,
            white,
            true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, dp(5))
        }, match())

        root.addView(text(
            mode.description,
            14f,
            muted
        ).apply {
            gravity = Gravity.CENTER
        }, match())

        val cameraCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), dp(13), dp(13), dp(13))
            setBackgroundColor(panel)
        }

        root.addView(cameraCard, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(22)
        })

        previewView = PreviewView(this).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }

        cameraCard.addView(
            previewView,
            LinearLayout.LayoutParams(-1, dp(245))
        )

        status = text("Připravuji kameru…", 14f, muted).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(8))
        }
        cameraCard.addView(status, match())

        scanButton = button("SPUSTIT SCAN")
        scanButton.isEnabled = false
        cameraCard.addView(scanButton, match())
        scanButton.setOnClickListener {
            runScan()
        }

        result = text(
            "${mode.name}: --",
            29f,
            mode.color,
            true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(8))
        }
        root.addView(result, match())

        root.addView(text(
            "Výsledek je náhodný a slouží pouze pro zábavu.",
            13f,
            muted
        ).apply {
            gravity = Gravity.CENTER
        }, match())

        val share = button("SDÍLET VÝSLEDEK")
        root.addView(share, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(14)
        })

        share.setOnClickListener {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Můj výsledek v ${mode.name}: ${result.text}. A jaký máš ty?"
                )
            }
            startActivity(Intent.createChooser(send, "Sdílet výsledek"))
        }

        root.addView(text(
            "HISTORIE",
            18f,
            white,
            true
        ).apply {
            setPadding(0, dp(24), 0, dp(8))
        }, match())

        history = text("Zatím žádné výsledky.", 14f, muted)
        root.addView(history, match())

        root.addView(text(
            "Kamera slouží pouze k živému náhledu. Aplikace neanalyzuje obličej ani zdravotní stav a snímky neukládá.",
            12f,
            0xFF9296A8.toInt()
        ).apply {
            setPadding(0, dp(22), 0, dp(20))
        }, match())

        refreshHistory()

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

    private fun runScan() {
        if (scanning) return

        scanning = true
        scanButton.isEnabled = false
        status.text = "Probíhá skenování…"
        result.text = "SCANNING…"

        result.animate()
            .alpha(0.35f)
            .setDuration(350)
            .withEndAction {
                result.animate().alpha(1f).setDuration(350).start()
            }.start()

        val mode = modes[selectedMode]

        Handler(Looper.getMainLooper()).postDelayed({
            val value = Random.nextInt(0, mode.maxScore + 1)

            result.text = if (mode.percent) {
                "$value %"
            } else {
                "$value / ${mode.maxScore}"
            }

            val scores = histories.getOrPut(selectedMode) {
                mutableListOf()
            }

            scores.add(0, value)

            if (scores.size > 8) {
                scores.removeAt(scores.lastIndex)
            }

            refreshHistory()
            status.text = "SCAN COMPLETE"
            scanButton.isEnabled = true
            scanning = false
        }, 2200)
    }

    private fun refreshHistory() {
        if (!::history.isInitialized) return

        val mode = modes[selectedMode]
        val scores = histories[selectedMode] ?: mutableListOf()

        history.text = if (scores.isEmpty()) {
            "Zatím žádné výsledky."
        } else {
            scores.mapIndexed { index, score ->
                val value = if (mode.percent) {
                    "$score %"
                } else {
                    "$score / ${mode.maxScore}"
                }
                "${index + 1}. $value"
            }.joinToString("\n")
        }
    }

    private fun startCamera() {
        if (!::previewView.isInitialized) return

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

    private fun stopCamera() {
        try {
            ProcessCameraProvider.getInstance(this).get().unbindAll()
        } catch (_: Exception) {
        }
    }

    private fun showPremiumDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔒 PREMIUM")
            .setMessage(
                "Odemkni všech 5 prémiových režimů:\n\n" +
                "🗿 TROLL TEST\n" +
                "💪 GIGACHAD TEST\n" +
                "🧠 BRAINROT TEST\n" +
                "🎮 NPC TEST\n" +
                "🐺 SIGMA TEST\n\n" +
                "Jednorázová cena: 20 Kč\n\n" +
                "Skutečné platby připravíme v další fázi."
            )
            .setPositiveButton("ROZUMÍM", null)
            .show()
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
        if (bold) {
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
    }

    private fun button(label: String) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 14f
        setTextColor(white)
        backgroundTintList =
            android.content.res.ColorStateList.valueOf(purple)
        setPadding(dp(12), dp(8), dp(12), dp(8))
    }

    private fun match() =
        LinearLayout.LayoutParams(-1, -2)

    private fun dp(value: Int) =
        (value * resources.displayMetrics.density).toInt()
}
