   
package cz.downtest.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
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
    val premium: Boolean = false
)

data class Question(
    val text: String,
    val answers: List<String>,
    val points: List<Int>
)

class MainActivity : AppCompatActivity() {

    private val backgroundColor = Color.rgb(15, 15, 20)
    private val cardColor = Color.rgb(30, 30, 40)
    private val accentColor = Color.rgb(120, 70, 255)
    private val whiteColor = Color.WHITE
    private val grayColor = Color.LTGRAY

    private val handler = Handler(Looper.getMainLooper())

    private val history = mutableListOf<String>()

    private var currentMode = "DOWN TEST"
    private var questions = listOf<Question>()
    private var currentQuestion = 0
    private var selectedAnswers = mutableListOf<Int>()
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewView: PreviewView? = null

    private val modes = listOf(
        GameMode("DOWN TEST"),
        GameMode("AURA TEST"),
        GameMode("GOONER TEST"),
        GameMode("TROLL TEST", true),
        GameMode("GIGACHAD TEST", true),
        GameMode("BRAINROT TEST", true),
        GameMode("NPC TEST", true),
        GameMode("SIGMA TEST", true)
    )

    private val cameraPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Kamera nebyla povolena.",
                    Toast.LENGTH_SHORT
                ).show()
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
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(20), dp(20), dp(20))
            setBackgroundColor(backgroundColor)
        }
    }

    private fun params(
        width: Int = LinearLayout.LayoutParams.MATCH_PARENT,
        height: Int = LinearLayout.LayoutParams.WRAP_CONTENT
    ): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(width, height).apply {
            setMargins(0, dp(7), 0, dp(7))
        }
    }

    private fun label(
        text: String,
        size: Float = 22f
    ): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(whiteColor)
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(12), dp(10), dp(12))
        }
    }

    private fun makeButton(
        text: String,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            this.text = text
            textSize = 16f
            setOnClickListener { action() }
        }
    }

    private fun showMenu() {
        stopCamera()

        val layout = root()
        layout.addView(label("DOWN TEST", 34f))
        layout.addView(label("Vyber si svůj test", 18f))

        modes.forEach { mode ->
            val buttonText =
                if (mode.premium) "🔒 ${mode.name}" else mode.name

            layout.addView(
                makeButton(buttonText) {
                    if (mode.premium) {
                        AlertDialog.Builder(this)
                            .setTitle("PREMIUM")
                            .setMessage(
                                "Tento režim je zatím zamčený."
                            )
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        currentMode = mode.name
                        showTestChoice(mode.name)
                    }
                },
                params()
            )
        }

        layout.addView(
            makeButton("HISTORIE") {
                showHistory()
            },
            params()
        )

        setContentView(layout)
    }

    private fun showTestChoice(name: String) {
        val layout = root()

        layout.addView(label(name, 30f))
        layout.addView(label("Vyber způsob testování", 18f))

        layout.addView(
            makeButton("SCAN") {
                showCameraScreen()
            },
            params()
        )

        if (name == "DOWN TEST" || name == "AURA TEST") {
            layout.addView(
                makeButton("OTÁZKY") {
                    startQuestions(name)
                },
                params()
            )
        }

        if (name == "GOONER TEST") {
            layout.addView(
                makeButton("OTÁZKY") {
                    startQuestions(name)
                },
                params()
            )
        }

        layout.addView(
            makeButton("ZPĚT") {
                showMenu()
            },
            params()
        )

        setContentView(layout)
    }


    private fun downQuestions(): List<Question> {
        val answers = listOf(
            "Rozhodně ano",
            "Občas",
            "Spíš ne",
            "Vůbec ne"
        )

        return listOf(
            "Jak často používáš slovo 67?",
            "Jak dobře znáš internetové memy?",
            "Jak často sleduješ YouTube?",
            "Jak moc jsi mimo?",
            "Jak často zapomeneš, co jsi chtěl říct?",
            "Co uděláš, když někdo řekne sigma?",
            "Jak moc tě baví brainrot?",
            "Jak často používáš slovo rizz?",
            "Jak rychle pochopíš nový meme?",
            "Jak moc jsi DOWN?"
        ).map { Question(it, answers, listOf(20, 15, 10, 5)) }
    }

    private fun auraQuestions(): List<Question> {
        val answers = listOf(
            "Obrovská aura",
            "Dobrá aura",
            "Neutrální aura",
            "Ztráta aury"
        )

        return listOf(
            "Pomůžeš kamarádovi, když potřebuje pomoc?",
            "Jak reaguješ na trapnou situaci?",
            "Dokážeš zachovat klid?",
            "Jak se chováš k ostatním?",
            "Umíš přiznat svou chybu?",
            "Dokážeš někoho rozesmát?",
            "Jak zvládáš těžké situace?",
            "Pomůžeš někomu, koho neznáš?",
            "Jak moc si věříš?",
            "Jak bys popsal svou auru?"
        ).map { Question(it, answers, listOf(100, 50, 0, -100)) }
    }

    private fun goonerQuestions(): List<Question> {
        val answers = listOf(
            "Jsem meme expert",
            "Znám jich docela dost",
            "Znám jen pár",
            "Vůbec se v nich nevyznám"
        )
        val memeQuestions = listOf(
            "Jak rychle poznáš nový internetový meme?",
            "Co uděláš, když někdo napíše 67?",
            "Jak dobře znáš populární brainrot memy?",
            "Jak často posíláš kamarádům vtipná videa?",
            "Poznáš meme jen podle jedné hlášky?",
            "Jak reaguješ, když někdo řekne sigma?",
            "Kolik internetových trendů znáš?",
            "Dokážeš vysvětlit meme, kterému se všichni smějí?",
            "Jak často používáš meme hlášky v běžné konverzaci?",
            "Jaký je tvůj meme level?"
        )
        return memeQuestions.map {
            Question(it, answers, listOf(20, 15, 10, 5))
        }
    }


    private fun startQuestions(name: String) {
        currentMode = name

        questions = when (name) {
            "DOWN TEST" -> downQuestions()
            "AURA TEST" -> auraQuestions()
            "GOONER TEST" -> goonerQuestions()
            else -> downQuestions()
        }.shuffled()

        currentQuestion = 0
        selectedAnswers.clear()
        showQuestion()
    }

    private fun showQuestion() {
        val question = questions[currentQuestion]
        val layout = root()

        layout.addView(
            label("$currentMode\nOtázka ${currentQuestion + 1}/10", 25f)
        )

        layout.addView(label(question.text, 22f))

        question.answers.forEachIndexed { index, answer ->
            layout.addView(
                makeButton(answer) {
                    selectedAnswers.add(index)
                    if (currentQuestion < questions.lastIndex) {
                        currentQuestion++
                        showQuestion()
                    } else {
                        finishQuestions()
                    }
                },
                params()
            )
        }

        layout.addView(
            makeButton("ZRUŠIT TEST") {
                showTestChoice(currentMode)
            },
            params()
        )

        setContentView(layout)
    }

    private fun finishQuestions() {
        var score = 0

        questions.forEachIndexed { index, question ->
            val answerIndex = selectedAnswers[index]
            score += question.points[answerIndex]
        }

        when (currentMode) {
            "DOWN TEST" -> showDownResult(score)
            "AURA TEST" -> showAuraResult(score)
            "GOONER TEST" -> showGoonerResult(score)
        }
    }

    private fun showDownResult(score: Int) {
        val percentage = (score / 2).coerceIn(0, 100)
        history.add("DOWN TEST: $percentage%")
        val layout = root()
        layout.addView(label("DOWN TEST", 30f))
        layout.addView(label("$percentage%", 36f))
        layout.addView(label("Test dokončen!", 20f))
        layout.addView(makeButton("MENU") { showMenu() }, params())
        setContentView(layout)
    }

    private fun showAuraResult(score: Int) {
        history.add("AURA TEST: $score / 1000")

        val layout = root()
        layout.addView(label("AURA TEST", 30f))
        layout.addView(label("$score / 1000", 36f))

        val resultText = when {
            score >= 700 -> "Obrovská aura!"
            score >= 300 -> "Dobrá aura!"
            score >= 0 -> "Neutrální aura."
            score >= -500 -> "Ztrácíš auru!"
            else -> "AURA V KRITICKÉM STAVU!"
        }

        layout.addView(label(resultText, 22f))
        layout.addView(makeButton("MENU") { showMenu() }, params())

        setContentView(layout)
    }

    private fun showGoonerResult(score: Int) {
        val percentage = (score / 2).coerceIn(0, 100)
        history.add("GOONER TEST: $percentage%")
        val layout = root()
        layout.addView(label("GOONER TEST", 30f))
        layout.addView(label("$percentage%", 36f))
        val resultText = when {
            percentage >= 90 -> "MEME LEGEND!"
            percentage >= 70 -> "MEME EXPERT!"
            percentage >= 50 -> "MEME FAN!"
            else -> "MEME NOVÁČEK!"
        }
        layout.addView(label(resultText, 22f))
        layout.addView(makeButton("MENU") { showMenu() }, params())
        setContentView(layout)
    }


    private fun showHistory() {
        val layout = root()
        layout.addView(label("HISTORIE", 30f))

        if (history.isEmpty()) {
            layout.addView(label("Zatím žádné výsledky.", 18f))
        } else {
            history.asReversed().forEach {
                layout.addView(label(it, 18f))
            }
        }

        layout.addView(
            makeButton("ZPĚT") { showMenu() },
            params()
        )

        setContentView(layout)
    }

    private fun showCameraScreen() {
        stopCamera()

        val layout = root()
        layout.addView(label("$currentMode SCAN", 28f))

        previewView = PreviewView(this).apply {
            layoutParams = params(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(350)
            )
        }
        layout.addView(previewView)

        layout.addView(
            makeButton("SPUSTIT SCAN") {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    startCamera()
                    handler.postDelayed({
                        val score = when (currentMode) {
                            "AURA TEST" -> Random.nextInt(-1000, 1001)
                            else -> Random.nextInt(0, 101)
                        }
                        stopCamera()
                        showScanResult(score)
                    }, 3000)
                } else {
                    cameraPermission.launch(Manifest.permission.CAMERA)
                }
            },
            params()
        )

        layout.addView(
            makeButton("ZPĚT") {
                stopCamera()
                showTestChoice(currentMode)
            },
            params()
        )

        setContentView(layout)
    }

    private fun startCamera() {
        val view = previewView ?: return

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = view.surfaceProvider
            }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview
                )
            } catch (exception: Exception) {
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
    }

    private fun showScanResult(score: Int) {
        val isAura = currentMode == "AURA TEST"
        val displayedScore = if (isAura) score else score.coerceIn(0, 100)
        history.add("$currentMode SCAN: " + if (isAura) "$displayedScore / 1000" else "$displayedScore%")
        val layout = root()
        layout.addView(label("$currentMode SCAN", 30f))
        if (isAura) {
            layout.addView(label("$displayedScore / 1000", 36f))
            val result = when {
                score >= 700 -> "Obrovská aura!"
                score >= 300 -> "Dobrá aura!"
                score >= 0 -> "Neutrální aura."
                score >= -500 -> "Ztrácíš auru!"
                else -> "AURA V KRITICKÉM STAVU!"
            }
            layout.addView(label(result, 22f))
        } else {
            layout.addView(label("$displayedScore%", 36f))
            layout.addView(label("Výsledek je pouze pro zábavu!", 18f))
        }

        layout.addView(
            makeButton("ZKUSIT ZNOVU") {
                showCameraScreen()
            },
            params()
        )

        layout.addView(
            makeButton("MENU") {
                showMenu()
            },
            params()
        )

        setContentView(layout)
    }

    override fun onDestroy() {
        stopCamera()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}


