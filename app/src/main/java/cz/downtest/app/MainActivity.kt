   package cz.downtest.app

import android.Manifest
import android.content.Intent
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import kotlin.random.Random

data class GameMode(
    val name: String,
    val emoji: String,
    val premium: Boolean
)

data class Question(
    val title: String,
    val answers: List<Pair<String, Int>>
)

class MainActivity : AppCompatActivity() {

    private val bg = Color.rgb(15, 15, 25)
    private val panel = Color.rgb(35, 35, 52)
    private val purple = Color.rgb(130, 75, 255)
    private val gold = Color.rgb(255, 195, 60)
    private val red = Color.rgb(230, 65, 75)
    private val white = Color.WHITE
    private val muted = Color.rgb(100, 100, 120)

    private val handler = Handler(Looper.getMainLooper())

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewView: PreviewView? = null
    private var currentMode = "DOWN TEST"

    private val history = mutableListOf<String>()

    private val modes = listOf(
        GameMode("DOWN TEST", "💀", false),
        GameMode("AURA TEST", "✨", false),
        GameMode("GOONER TEST", "😎", false),
        GameMode("TROLL TEST", "🤡", true),
        GameMode("GIGACHAD TEST", "🗿", true),
        GameMode("BRAINROT TEST", "🧠", true),
        GameMode("NPC TEST", "🤖", true),
        GameMode("SIGMA TEST", "🐺", true)
    )

    private val questions = listOf(
        Question(
            "Jak moc posloucháš songu Jan Koller?",
            listOf(
                "Vždycky" to 5,
                "Občas" to 5,
                "Každý týden" to 5,
                "Vůbec neposlouchám" to 5
            )
        ),
        Question(
            "Na jaké YouTubery se koukáš nebo koukal?",
            listOf(
                "Fizi" to 20,
                "Tary" to 15,
                "Porty" to 10,
                "Dodo" to 5
            )
        ),
        Question(
            "Víš, co znamená, když ti někdo řekne, že jsi down?",
            listOf(
                "Vím na 100 %" to 10,
                "Asi jo" to 6,
                "Asi ne" to 3,
                "Vůbec nevím" to 0
            )
        ),
        Question(
            "Jak často říkáš 67?",
            listOf(
                "Hodně často" to 5,
                "Občas" to 5,
                "Řekl jsem to jenom párkrát" to 5,
                "Nikdy jsem to neřekl" to 0
            )
        ),
        Question(
            "Kolik si myslíš, že ti vyjde výsledek DOWN TESTU?",
            listOf(
                "Míň než 50" to 20,
                "Víc než 50" to 10
            )
        )
    )

    private val cameraPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                showCameraScreen()
            } else {
                Toast.makeText(
                    this,
                    "Pro SCAN je potřeba povolit kameru.",
                    Toast.LENGTH_LONG
                ).show()
                showModeScreen(currentMode)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showMenu()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun root(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(bg)
        }
    }

    private fun params(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun label(
        text: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            if (bold) {
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }
    }

    private fun makeButton(text: String): Button {
        return Button(this).apply {
            this.text = text
            textSize = 16f
            isAllCaps = false
            setTextColor(white)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(panel)
        }
    }

    private fun showMenu() {
        stopCamera()

        val page = root()

        page.addView(
            label("💀 DOWN TEST", 32f, purple, true),
            params()
        )

        page.addView(
            label("Vyber si svůj test!", 17f, white),
            params()
        )

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        modes.forEach { mode ->
            val button = makeButton(
                "${mode.emoji}  ${mode.name}" +
                    if (mode.premium) "  🔒 PREMIUM" else ""
            )

            button.textSize = 18f

            list.addView(
                button,
                LinearLayout.LayoutParams(-1, dp(65)).apply {
                    topMargin = dp(8)
                    bottomMargin = dp(5)
                }
            )

            button.setOnClickListener {
                if (mode.premium) {
                    showPremiumDialog(mode.name)
                } else {
                    openMode(mode.name)
                }
            }
        }

        scroll.addView(list)

        page.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        page.addView(
            label("Je to jen joke, neberte to vážně.", 13f, muted),
            params()
        )

        setContentView(page)
    }

    private fun openMode(name: String) {
        currentMode = name

        if (name == "DOWN TEST") {
            showDownChoice()
        } else {
            showModeScreen(name)
        }
    }

    private fun showPremiumDialog(name: String) {
        AlertDialog.Builder(this)
            .setTitle("🔒 PREMIUM")
            .setMessage(
                "$name je zatím zamčený.\n\n" +
                    "Tento režim zatím není dostupný."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDownChoice() {
        stopCamera()

        val page = root()

        page.addView(
            label("💀 DOWN TEST", 30f, purple, true),
            params()
        )

        page.addView(
            label("Vyber si způsob testování", 18f, white),
            params()
        )

        val scan = makeButton("📸 SCAN")
        page.addView(
            scan,
            LinearLayout.LayoutParams(-1, dp(75)).apply {
                topMargin = dp(45)
            }
        )

        scan.setOnClickListener {
            currentMode = "DOWN TEST"
            requestCamera()
        }

        val questionsButton = makeButton("❓ OTÁZKY")
        page.addView(
            questionsButton,
            LinearLayout.LayoutParams(-1, dp(75)).apply {
                topMargin = dp(20)
            }
        )

        questionsButton.setOnClickListener {
            showQuestions()
        }

        val back = makeButton("‹ ZPĚT")
        page.addView(
            back,
            LinearLayout.LayoutParams(-1, dp(55)).apply {
                topMargin = dp(30)
            }
        )

        back.setOnClickListener {
            showMenu()
        }

        setContentView(page)
    }

    private fun showQuestions() {
        stopCamera()

        val randomizedQuestions = questions.shuffled()
        val selected = mutableMapOf<Int, Int>()

        lateinit var renderQuestion: (Int) -> Unit

        renderQuestion = { questionIndex ->
            val page = root()
            page.setPadding(dp(22), dp(30), dp(22), dp(25))
            setContentView(page)

            val question = randomizedQuestions[questionIndex]

            page.addView(
                label("💀 DOWN TEST", 24f, white, true),
                params()
            )

            page.addView(
                label(
                    "OTÁZKA ${questionIndex + 1} / ${randomizedQuestions.size}",
                    15f,
                    purple,
                    true
                ).apply {
                    setPadding(0, dp(18), 0, dp(25))
                },
                params()
            )

            page.addView(
                label(question.title, 25f, white, true).apply {
                    setPadding(dp(5), dp(20), dp(5), dp(30))
                },
                LinearLayout.LayoutParams(-1, 0, 1f)
            )

            val next = makeButton(
                if (questionIndex == randomizedQuestions.lastIndex)
                    "VYHODNOTIT TEST"
                else
                    "DALŠÍ OTÁZKA  →"
            )

            val answerButtons = mutableListOf<Button>()

            question.answers.forEachIndexed { answerIndex, answer ->
                val answerButton = makeButton(answer.first)
                answerButton.textSize = 17f

                answerButton.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        if (selected[questionIndex] == answerIndex)
                            purple else panel
                    )

                page.addView(
                    answerButton,
                    LinearLayout.LayoutParams(-1, dp(65)).apply {
                        bottomMargin = dp(10)
                    }
                )

                answerButtons.add(answerButton)

                answerButton.setOnClickListener {
                    selected[questionIndex] = answerIndex

                    answerButtons.forEachIndexed { index, button ->
                        button.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(
                                if (index == answerIndex) purple else panel
                            )
                    }

                    next.isEnabled = true
                    next.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(purple)
                }
            }

            next.isEnabled = selected.containsKey(questionIndex)
            next.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    if (next.isEnabled) purple else muted
                )

            page.addView(
                next,
                LinearLayout.LayoutParams(-1, dp(65)).apply {
                    topMargin = dp(18)
                }
            )

            next.setOnClickListener {
                if (selected.containsKey(questionIndex)) {
                    if (questionIndex < randomizedQuestions.lastIndex) {
                        renderQuestion(questionIndex + 1)
                    } else {
                        val score = randomizedQuestions.indices.sumOf { index ->
                            randomizedQuestions[index]
                                .answers[selected[index]!!].second + 30
                        }

                        showDownResult(score)
                    }
                }
            }

            val back = makeButton("‹ ZPĚT")

            page.addView(
                back,
                LinearLayout.LayoutParams(-1, dp(50)).apply {
                    topMargin = dp(10)
                }
            )

            back.setOnClickListener {
                if (questionIndex == 0) {
                    showDownChoice()
                } else {
                    renderQuestion(questionIndex - 1)
                }
            }
        }

        renderQuestion(0)
    }

    private fun showDownResult(score: Int) {
        stopCamera()
        history.add("DOWN TEST: $score %")

        if (score > 100) {
            showWarning(score)
            return
        }

        val page = root()

        page.addView(
            label("💀 DOWN TEST", 28f, purple, true),
            params()
        )

        page.addView(
            label("TVŮJ VÝSLEDEK", 17f, white, true).apply {
                setPadding(0, dp(30), 0, dp(15))
            },
            params()
        )

        page.addView(
            label("$score %", 58f, gold, true),
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        page.addView(
            label("Je to jen joke, neberte to vážně.", 14f, muted),
            params()
        )

        val again = makeButton("🔄 ZKUSIT ZNOVU")
        page.addView(
            again,
            LinearLayout.LayoutParams(-1, dp(60)).apply {
                topMargin = dp(20)
            }
        )

        again.setOnClickListener {
            showQuestions()
        }

        val back = makeButton("ZPĚT DO MENU")
        page.addView(
            back,
            LinearLayout.LayoutParams(-1, dp(55)).apply {
                topMargin = dp(10)
            }
        )

        back.setOnClickListener {
            showMenu()
        }

        setContentView(page)
    }

    private fun showWarning(score: Int) {
        stopCamera()

        val page = root()
        page.setBackgroundColor(Color.rgb(45, 8, 15))

        page.addView(
            label("⚠️ WARNING ⚠️", 29f, red, true),
            params()
        )

        page.addView(
            label("SYSTEM ERROR", 25f, red, true).apply {
                setPadding(0, dp(25), 0, dp(15))
            },
            params()
        )

        page.addView(
            label("$score % DOWN", 40f, white, true),
            params()
        )

        page.addView(
            label(
                "ROZBIL JSI APLIKACI!\n\n" +
                    "Jsi tak velkej DOWN, že jsi rozbil celou aplikaci. 💀",
                21f,
                white,
                true
            ),
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        val back = makeButton("ZPĚT DO MENU")
        page.addView(back, params())

        back.setOnClickListener {
            showMenu()
        }

        setContentView(page)
    }

    private fun showModeScreen(name: String) {
        stopCamera()
        currentMode = name

        val mode = modes.firstOrNull { it.name == name }
        val emoji = mode?.emoji ?: "💀"

        val page = root()

        page.addView(
            label("$emoji  $name", 29f, purple, true),
            params()
        )

        page.addView(
            label("Připrav se na skenování!", 17f, white).apply {
                setPadding(0, dp(20), 0, dp(35))
            },
            params()
        )

        val scan = makeButton("📸 START SCAN")
        page.addView(
            scan,
            LinearLayout.LayoutParams(-1, dp(75))
        )

        scan.setOnClickListener {
            requestCamera()
        }

        val historyButton = makeButton("📋 HISTORIE")
        page.addView(
            historyButton,
            LinearLayout.LayoutParams(-1, dp(55)).apply {
                topMargin = dp(15)
            }
        )

        historyButton.setOnClickListener {
            showHistory()
        }

        val back = makeButton("‹ ZPĚT DO MENU")
        page.addView(
            back,
            LinearLayout.LayoutParams(-1, dp(55)).apply {
                topMargin = dp(15)
            }
        )

        back.setOnClickListener {
            showMenu()
        }

        setContentView(page)
    }

    private fun showHistory() {
        val text = if (history.isEmpty()) {
            "Zatím nemáš žádné výsledky."
        } else {
            history.takeLast(8).reversed().joinToString("\n\n")
        }

        AlertDialog.Builder(this)
            .setTitle("📋 HISTORIE")
            .setMessage(text)
            .setPositiveButton("ZAVŘÍT", null)
            .show()
    }

    private fun requestCamera() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showCameraScreen()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showCameraScreen() {
        val page = root()

        page.addView(
            label("📸 $currentMode", 27f, purple, true),
            params()
        )

        previewView = PreviewView(this).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }

        page.addView(
            previewView,
            LinearLayout.LayoutParams(-1, 0, 1f).apply {
                topMargin = dp(20)
                bottomMargin = dp(20)
            }
        )

        page.addView(
            label("Připrav se na skenování!", 15f, white),
            params()
        )

        val scan = makeButton("START SCAN")
        page.addView(
            scan,
            LinearLayout.LayoutParams(-1, dp(65)).apply {
                topMargin = dp(15)
            }
        )

        scan.setOnClickListener {
            scan.isEnabled = false
            scan.text = "SCANNING..."

            val scanningText = label(
                "SCANNING... 0 %",
                25f,
                gold,
                true
            )

            page.addView(
                scanningText,
                params()
            )

            var progress = 0

            val scanRunnable = object : Runnable {
                override fun run() {
                    progress += 10
                    scanningText.text = "SCANNING... $progress %"

                    if (progress < 100) {
                        handler.postDelayed(this, 180)
                    } else {
                        handler.postDelayed({
                            val result = generateScanResult(currentMode)
                            showScanResult(result)
                        }, 400)
                    }
                }
            }

            handler.postDelayed(scanRunnable, 180)
        }

        val back = makeButton("‹ ZPĚT")
        page.addView(
            back,
            LinearLayout.LayoutParams(-1, dp(50)).apply {
                topMargin = dp(10)
            }
        )

        back.setOnClickListener {
            stopCamera()
            if (currentMode == "DOWN TEST") {
                showDownChoice()
            } else {
                showModeScreen(currentMode)
            }
        }

        setContentView(page)
        startCamera()
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)

        future.addListener({
            try {
                val provider = future.get()
                cameraProvider = provider

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider =
                            previewView?.surfaceProvider
                    }

                provider.unbindAll()

                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Kameru se nepodařilo spustit.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCamera() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        previewView = null
        handler.removeCallbacksAndMessages(null)
    }

    private fun generateScanResult(mode: String): Int {
        return when (mode) {
            "DOWN TEST" -> {
                // Skrytý náhodný easter egg.
                if (Random.nextInt(100) < 5) {
                    100
                } else {
                    Random.nextInt(0, 101)
                }
            }

            "AURA TEST" -> {
                Random.nextInt(-1000, 1001)
            }

            "GOONER TEST" -> {
                Random.nextInt(0, 101)
            }

            "TROLL TEST" -> Random.nextInt(0, 101)
            "GIGACHAD TEST" -> Random.nextInt(0, 101)
            "BRAINROT TEST" -> Random.nextInt(0, 101)
            "NPC TEST" -> Random.nextInt(0, 101)
            "SIGMA TEST" -> Random.nextInt(0, 101)

            else -> Random.nextInt(0, 101)
        }
    }

    private fun showScanResult(result: Int) {
        stopCamera()

        val mode = currentMode

        val resultText = if (mode == "AURA TEST") {
            "$result / 1000"
        } else {
            "$result %"
        }

        history.add("$mode: $resultText")

        val page = root()

        page.addView(
            label("✨ $mode", 28f, purple, true),
            params()
        )

        page.addView(
            label("TVŮJ VÝSLEDEK", 17f, white, true).apply {
                setPadding(0, dp(30), 0, dp(15))
            },
            params()
        )

        page.addView(
            label(resultText, 54f, gold, true),
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        val description = when (mode) {
            "DOWN TEST" -> "Tvoje DOWN skóre je $result %! 💀"
            "AURA TEST" -> {
                if (result < 0) {
                    "Tvoje aura dnes potřebuje restart. 💀"
                } else {
                    "Tvoje aura byla změřena! ✨"
                }
            }

            "GOONER TEST" -> "Výsledek tvého testu je $result %!"
            else -> "Výsledek testu: $result %!"
        }

        page.addView(
            label(description, 17f, white),
            params()
        )

        page.addView(
            label("Je to jen joke, neberte to vážně.", 13f, muted),
            params()
        )

        val again = makeButton("🔄 ZKUSIT ZNOVU")
        page.addView(
            again,
            LinearLayout.LayoutParams(-1, dp(60)).apply {
                topMargin = dp(20)
            }
        )

        again.setOnClickListener {
            showModeScreen(mode)
        }

        val share = makeButton("📤 SDÍLET VÝSLEDEK")
        page.addView(
            share,
            LinearLayout.LayoutParams(-1, dp(55)).apply {
                topMargin = dp(10)
            }
        )

        share.setOnClickListener {
            shareText("$mode: $resultText")
        }

        val back = makeButton("ZPĚT DO MENU")
        page.addView(
            back,
            LinearLayout.LayoutParams(-1, dp(55)).apply {
                topMargin = dp(10)
            }
        )

        back.setOnClickListener {
            showMenu()
        }

        setContentView(page)
    }

    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "$text\nJe to jen joke, neberte to vážně. 💀"
            )
        }

        startActivity(
            Intent.createChooser(intent, "Sdílet výsledek")
        )
    }

    override fun onDestroy() {
        stopCamera()
        super.onDestroy()
    }
}