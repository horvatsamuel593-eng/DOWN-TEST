package cz.downtest.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
    private val muted = Color.rgb(184, 187, 202)

    private data class GameMode(
        val name: String, val emoji: String, val description: String,
        val premium: Boolean, val percent: Boolean, val color: Int
    )

    private data class Question(
        val title: String,
        val answers: List<Pair<String, Int>>
    )

    private val modes = listOf(
        GameMode("DOWN TEST", "💀", "Zjisti svoje DOWN skóre", false, true, purple),
        GameMode("AURA TEST", "✨", "Zjisti svou náhodnou auru", false, false, gold),
        GameMode("GOONER TEST", "🔥", "Náhodné meme skóre", false, true, red),
        GameMode("TROLL TEST", "🗿", "Jak velký troll jsi?", true, true, purple),
        GameMode("GIGACHAD TEST", "💪", "Tvoje náhodná Chad energie", true, false, gold),
        GameMode("BRAINROT TEST", "🧠", "Brainrot level", true, true, red),
        GameMode("NPC TEST", "🎮", "Jak moc NPC dnes jsi?", true, true, purple),
        GameMode("SIGMA TEST", "🐺", "Tvůj náhodný Sigma level", true, false, gold)
    )

    private val questions = listOf(
        Question("1. Jak moc posloucháš songu Jan Koller?", listOf(
            "Vždycky" to 5,
            "Občas" to 5,
            "Každý týden" to 5,
            "Vůbec neposlouchám" to 5
        )),
        Question("2. Na jaké YouTubery se koukáš nebo koukal?", listOf(
            "Fizi" to 20,
            "Tary" to 15,
            "Porty" to 10,
            "Dodo" to 5
        )),
        Question("3. Víš, co znamená, když ti někdo řekne, že jsi down?", listOf(
            "Vím na 100 %" to 10,
            "Asi jo" to 6,
            "Asi ne" to 3,
            "Vůbec nevím" to 0
        )),
        Question("4. Jak často říkáš 67?", listOf(
            "Hodně často" to 5,
            "Občas" to 5,
            "Řekl jsem to jenom párkrát" to 5,
            "Nikdy jsem to neřekl" to 0
        )),
        Question("5. Kolik si myslíš, že ti vyjde výsledek DOWN TESTU?", listOf(
            "Míň než 50" to 20,
            "Víc než 50" to 10
        ))
    )

    private val histories = mutableMapOf<Int, MutableList<String>>()
    private var selectedMode = 0
    private var cameraProvider: ProcessCameraProvider? = null

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
        } else if (::status.isInitialized) {
            status.text = "Kamera nebyla povolena."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showMenu()
    }

    private fun dp(value: Int) =
        (value * resources.displayMetrics.density).toInt()

    private fun params() =
        LinearLayout.LayoutParams(-1, -2)

    private fun root(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(24), dp(18), dp(28))
            setBackgroundColor(bg)
        }

    private fun label(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
    }

    private fun makeButton(value: String) =
        Button(this).apply {
            text = value
            textSize = 14f
            setTextColor(white)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(purple)
            isAllCaps = false
        }    private fun showMenu() {
        stopCamera()
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val page = root()
        scroll.addView(page)
        setContentView(scroll)

        val heading = label("DOWN TEST 💀", 31f, white, true).apply { gravity = Gravity.CENTER }
        page.addView(heading, params())
        page.addView(label("VYBER SI SVŮJ TEST", 14f, muted).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, dp(24))
        }, params())

        addSection(page, "ZDARMA – 3 REŽIMY", purple)
        modes.forEachIndexed { i, mode -> if (!mode.premium) addModeCard(page, i, mode) }
        addSection(page, "PREMIUM – 5 REŽIMŮ", gold)
        modes.forEachIndexed { i, mode -> if (mode.premium) addModeCard(page, i, mode) }

        val premium = makeButton("🔓 OTEVŘÍT VŠECHNY MÓDY")
        page.addView(premium, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(18) })
        premium.setOnClickListener { showPremiumDialog() }
        page.addView(label("Všechny výsledky jsou pouze pro zábavu.", 12f, muted).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, 0)
        }, params())
    }

    private fun addSection(page: LinearLayout, title: String, color: Int) {
        page.addView(label(title, 17f, color, true).apply {
            setPadding(0, dp(12), 0, dp(8))
        }, params())
    }

    private fun addModeCard(page: LinearLayout, index: Int, mode: GameMode) {
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
        info.addView(label("${mode.emoji}  ${mode.name}", 17f, white, true), params())
        info.addView(label(mode.description, 12f, muted).apply { setPadding(0, dp(4), 0, 0) }, params())
        card.addView(info)
        card.addView(label(if (mode.premium) "🔒" else "HRÁT  ›", 15f,
            if (mode.premium) gold else purple, true).apply { gravity = Gravity.CENTER })
        page.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) })
        card.setOnClickListener { if (mode.premium) showPremiumDialog() else openMode(index) }
    }

    private fun openMode(index: Int) {
        selectedMode = index
        if (index == 0) showDownChoice() else showModeScreen()
    }

    private fun showDownChoice() {
        stopCamera()
        val page = root()
        setContentView(page)
        page.addView(label("💀 DOWN TEST", 29f, white, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(30), 0, dp(15))
        }, params())
        page.addView(label("Vyber si způsob testování", 16f, muted).apply {
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(25))
        }, params())

        val scan = makeButton("📸 SCAN")
        page.addView(scan, LinearLayout.LayoutParams(-1, dp(65)))
        scan.setOnClickListener { showModeScreen() }
        val quiz = makeButton("📝 OTÁZKY")
        page.addView(quiz, LinearLayout.LayoutParams(-1, dp(65)).apply { topMargin = dp(15) })
        quiz.setOnClickListener { showQuestions() }
        val back = makeButton("‹ ZPĚT DO NABÍDKY")
        page.addView(back, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(35) })
        back.setOnClickListener { showMenu() }
    }

    private fun showQuestions() {
        stopCamera()
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val page = root()
        scroll.addView(page)
        setContentView(scroll)

        val selected = mutableMapOf<Int, Int>()
        val buttons = mutableListOf<Triple<Int, Int, Button>>()
        val calculate = makeButton("VYHODNOTIT TEST")
        calculate.isEnabled = false
        calculate.backgroundTintList = android.content.res.ColorStateList.valueOf(muted)

        page.addView(label("💀 DOWN TEST – OTÁZKY", 25f, white, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(12))
        }, params())
        page.addView(label("Vyber jednu odpověď u každé otázky.", 14f, muted).apply {
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(20))
        }, params())

        questions.forEachIndexed { qi, question ->
            page.addView(label(question.title, 17f, white, true).apply {
                setPadding(0, dp(14), 0, dp(8))
            }, params())
            question.answers.forEachIndexed { ai, answer ->
                val answerButton = makeButton(answer.first)
                answerButton.backgroundTintList = android.content.res.ColorStateList.valueOf(panel)
                page.addView(answerButton, LinearLayout.LayoutParams(-1, -2).apply {
                    bottomMargin = dp(5)
                })
                buttons.add(Triple(qi, ai, answerButton))
                answerButton.setOnClickListener {
                    selected[qi] = ai
                    buttons.forEach { item ->
                        if (item.first == qi) {
                            item.third.backgroundTintList =
                                android.content.res.ColorStateList.valueOf(if (item.second == ai) purple else panel)
                        }
                    }
                    calculate.isEnabled = selected.size == questions.size
                    calculate.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        if (calculate.isEnabled) purple else muted
                    )
                }
            }
        }

        page.addView(calculate, LinearLayout.LayoutParams(-1, dp(60)).apply { topMargin = dp(22) })
        calculate.setOnClickListener {
            if (selected.size != questions.size) return@setOnClickListener
            val score = questions.indices.sumOf { questions[it].answers[selected[it]!!].second }
            showDownResult(score)
        }
        val back = makeButton("‹ ZPĚT")
        page.addView(back, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        back.setOnClickListener { showDownChoice() }
    }

    private fun showDownResult(total: Int) {
        val list = histories.getOrPut(0) { mutableListOf() }
        list.add(0, "$total %")
        if (list.size > 8) list.removeAt(list.lastIndex)

        val page = root()
        setContentView(page)
        page.addView(label("💀 DOWN TEST – VÝSLEDEK", 25f, white, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(25), 0, dp(25))
        }, params())

        if (total > 100) {
            page.addView(label("💥 ERROR 💥", 34f, red, true).apply { gravity = Gravity.CENTER }, params())
            page.addView(label("ROZBIL JSI APLIKACI TÍM, JAK VELKEJ DOWN JSEŠ. 💀", 23f, red, true).apply {
                gravity = Gravity.CENTER
            }, params())
        } else {
            page.addView(label("$total %", 58f, purple, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(25), 0, dp(20))
            }, params())
            page.addView(label("Tvoje DOWN skóre", 18f, white).apply { gravity = Gravity.CENTER }, params())
        }

        val share = makeButton("SDÍLET VÝSLEDEK")
        page.addView(share, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(30) })
        share.setOnClickListener { shareText("Můj DOWN TEST výsledek: $total % 💀") }

        val again = makeButton("ZKUSIT ZNOVU")
        page.addView(again, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        again.setOnClickListener { showQuestions() }
        val menu = makeButton("ZPĚT DO MENU")
        page.addView(menu, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        menu.setOnClickListener { showMenu() }
    }    private fun showModeScreen() {
        stopCamera()
        val mode = modes[selectedMode]
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val page = root()
        scroll.addView(page)
        setContentView(scroll)

        val back = makeButton("‹ ZPĚT DO NABÍDKY")
        page.addView(back, params())
        back.setOnClickListener {
            stopCamera()
            if (selectedMode == 0) showDownChoice() else showMenu()
        }

        page.addView(label("${mode.emoji} ${mode.name}", 27f, white, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, dp(5))
        }, params())
        page.addView(label(mode.description, 14f, muted).apply { gravity = Gravity.CENTER }, params())

        val cameraCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), dp(13), dp(13), dp(13))
            setBackgroundColor(panel)
        }
        page.addView(cameraCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(22) })

        previewView = PreviewView(this).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
        cameraCard.addView(previewView, LinearLayout.LayoutParams(-1, dp(245)))
        status = label("Připravuji kameru…", 14f, muted).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(8))
        }
        cameraCard.addView(status, params())
        scanButton = makeButton("SPUSTIT SCAN")
        scanButton.isEnabled = false
        cameraCard.addView(scanButton, params())
        scanButton.setOnClickListener { runScan() }

        result = label("${mode.name}: --", 29f, mode.color, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(8))
        }
        page.addView(result, params())
        page.addView(label("Výsledek je náhodný a slouží pouze pro zábavu.", 13f, muted).apply {
            gravity = Gravity.CENTER
        }, params())

        val share = makeButton("SDÍLET VÝSLEDEK")
        page.addView(share, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) })
        share.setOnClickListener {
            shareText("Můj výsledek v ${mode.name}: ${result.text}. A jaký máš ty?")
        }

        page.addView(label("HISTORIE", 18f, white, true).apply {
            setPadding(0, dp(24), 0, dp(8))
        }, params())
        history = label("Zatím žádné výsledky.", 14f, muted)
        page.addView(history, params())
        updateHistory()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                val provider = future.get()
                cameraProvider = provider
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                provider.unbindAll()
                provider.bindToLifecycle(
                    this@MainActivity,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview
                )
                status.text = "Kamera je připravena!"
                scanButton.isEnabled = true
            } catch (e: Exception) {
                status.text = "Kameru se nepodařilo spustit."
                scanButton.isEnabled = false
                Toast.makeText(this@MainActivity, "Chyba kamery: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
            cameraProvider = null
        } catch (_: Exception) {
            // Kamera už může být odpojená.
        }
    }

    private fun runScan() {
        if (!::result.isInitialized || !::scanButton.isInitialized) return
        val mode = modes[selectedMode]
        val score = Random.nextInt(0, 101)
        result.text = if (mode.percent) "${mode.name}: $score %" else "${mode.name}: $score"
        val list = histories.getOrPut(selectedMode) { mutableListOf() }
        list.add(0, result.text.toString())
        if (list.size > 8) list.removeAt(list.lastIndex)
        updateHistory()
        if (::status.isInitialized) status.text = "Scan dokončen!"
        scanButton.isEnabled = true
    }

    private fun updateHistory() {
        if (!::history.isInitialized) return
        val list = histories[selectedMode]
        history.text = if (list.isNullOrEmpty()) {
            "Zatím žádné výsledky."
        } else {
            list.mapIndexed { index, value -> "${index + 1}. $value" }.joinToString("\n")
        }
    }

    private fun shareText(message: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        startActivity(Intent.createChooser(intent, "Sdílet výsledek"))
    }

    private fun showPremiumDialog() {
        AlertDialog.Builder(this)
            .setTitle("🔒 PREMIUM")
            .setMessage("Tento režim je zatím zamčený.\n\nPremium režimy budou dostupné v budoucí verzi aplikace.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroy() {
        stopCamera()
        super.onDestroy()
    }
} 
    