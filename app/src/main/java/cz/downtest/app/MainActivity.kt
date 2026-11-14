   
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
    val emoji: String,
    val premium: Boolean = false
)

data class Question(
    val title: String,
    val answers: List<Pair<String, Int>>
)

class MainActivity : AppCompatActivity() {

    private val purple = Color.rgb(145, 70, 255)
    private val gold = Color.rgb(255, 210, 50)
    private val white = Color.WHITE
    private val red = Color.RED
    private val backgroundColor = Color.rgb(18, 12, 30)

    private val handler = Handler(Looper.getMainLooper())
    private val history = mutableListOf<String>()

    private var currentMode = "DOWN TEST"
    private var questionType = "DOWN TEST"
    private var questionIndex = 0
    private var randomizedQuestions = listOf<Question>()
    private val selectedAnswers = mutableMapOf<Int, Int>()

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewView: PreviewView? = null

    private val modes = listOf(
        GameMode("DOWN TEST", "💀"),
        GameMode("AURA TEST", "✨"),
        GameMode("GOONER TEST", "😎"),
        GameMode("TROLL TEST", "🤡", true),
        GameMode("GIGACHAD TEST", "🗿", true),
        GameMode("BRAINROT TEST", "🧠", true),
        GameMode("NPC TEST", "🤖", true),
        GameMode("SIGMA TEST", "🐺", true)
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
                    "Pro skenování potřebuješ povolit kameru.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showMenu()
    }


    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun root(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(25), dp(20), dp(25))
            setBackgroundColor(backgroundColor)
        }
    }

    private fun params(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(10)
        }
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
            setTextColor(white)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(purple)
            isAllCaps = false
        }
    }

    private fun showMenu() {
        val page = root()

        page.addView(
            label("💀 DOWN TEST", 32f, purple, true),
            params()
        )

        page.addView(
            label("Vyber si svůj test!", 19f, white),
            params()
        )

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        modes.forEach { mode ->
            val button = makeButton(
                "${mode.emoji} ${mode.name}" +
                    if (mode.premium) " 🔒 PREMIUM" else ""
            )

            list.addView(button, params())

            button.setOnClickListener {
                if (mode.premium) {
                    AlertDialog.Builder(this)
                        .setTitle("🔒 PREMIUM")
                        .setMessage("Tento režim je zatím zamčený.")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    currentMode = mode.name
                    if (mode.name == "DOWN TEST" ||
                        mode.name == "AURA TEST" ||
                        mode.name == "GOONER TEST"
                    ) {
                        showTestChoice(mode.name)
                    } else {
                        showModeScreen(mode.name)
                    }
                }
            }
        }

        scroll.addView(list)
        page.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(page)
    }

    private fun showTestChoice(name: String) {
        currentMode = name

        val page = root()

        page.addView(
            label("💀 $name", 30f, purple, true),
            params()
        )

        page.addView(
            label("Vyber si způsob testování", 18f, white),
            params()
        )

        val scanButton = makeButton("📸 SCAN")
        val questionsButton = makeButton("📝 OTÁZKY")
        val backButton = makeButton("⬅ ZPĚT")

        page.addView(scanButton, params())
        page.addView(questionsButton, params())
        page.addView(backButton, params())

        scanButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showCameraScreen()
            } else {
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        questionsButton.setOnClickListener {
            startQuestions(name)
        }

        backButton.setOnClickListener {
            showMenu()
        }

        setContentView(page)
    }


    private fun downQuestions(): List<Question> = listOf(
        Question(
            "Jak moc posloucháš songu Jan Koller?",
            listOf(
                "Vždycky" to 20,
                "Občas" to 15,
                "Každý týden" to 10,
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
                "Vím na 100 %" to 20,
                "Asi jo" to 15,
                "Asi ne" to 10,
                "Vůbec nevím" to 5
            )
        ),
        Question(
            "Jak často říkáš 67?",
            listOf(
                "Hodně často" to 20,
                "Občas" to 15,
                "Řekl jsem to jen párkrát" to 10,
                "Nikdy jsem to neřekl" to 5
            )
        ),
        Question(
            "Kolik si myslíš, že ti vyjde výsledek DOWN TESTU?",
            listOf(
                "Míň než 50" to 20,
                "Víc než 50" to 15,
                "Přesně 50" to 10,
                "Nevím" to 5
            )
        ),
        Question(
            "Co uděláš, když někdo řekne 67?",
            listOf(
                "Okamžitě to zopakuju" to 20,
                "Začnu se smát" to 15,
                "Jen se podívám" to 10,
                "Nevím, o čem mluví" to 5
            )
        ),
        Question(
            "Jak často zapomínáš, co jsi chtěl říct?",
            listOf(
                "Pořád" to 20,
                "Docela často" to 15,
                "Občas" to 10,
                "Nikdy" to 5
            )
        ),
        Question(
            "Co uděláš, když ti někdo řekne sigma?",
            listOf(
                "Začnu dělat sigma pózu" to 20,
                "Řeknu skibidi" to 15,
                "Zasměju se" to 10,
                "Zeptám se, co to znamená" to 5
            )
        ),
        Question(
            "Jak dlouho vydržíš bez mobilu?",
            listOf(
                "Ani minutu" to 20,
                "Pár hodin" to 15,
                "Celý den" to 10,
                "Klidně týden" to 5
            )
        ),
        Question(
            "Jak moc jsi někdy mimo?",
            listOf(
                "Jsem úplně jiná galaxie" to 20,
                "Dost často" to 15,
                "Občas" to 10,
                "Většinou jsem v obraze" to 5
            )
        )
    )

    private fun auraQuestions(): List<Question> = listOf(
        Question(
            "Co uděláš, když někdo začne dělat frajera?",
            listOf(
                "Zůstanu v klidu" to 100,
                "Zasměju se" to 50,
                "Začnu se předvádět taky" to 0,
                "Nechám se vyprovokovat" to -100
            )
        ),
        Question(
            "Jak se zachováš, když někdo potřebuje pomoc?",
            listOf(
                "Pomůžu mu okamžitě" to 100,
                "Nabídnu mu pomoc" to 50,
                "Počkám, jestli pomůže někdo jiný" to 0,
                "Budu ho ignorovat" to -100
            )
        ),
        Question(
            "Co uděláš, když prohraješ hru?",
            listOf(
                "Pogratuluju soupeři" to 100,
                "Zkusím to znovu" to 50,
                "Trochu se naštvu" to 0,
                "Začnu se hádat" to -100
            )
        ),
        Question(
            "Jak se chováš k novým lidem?",
            listOf(
                "Přivítám je a zapojím do party" to 100,
                "Normálně se s nimi bavím" to 50,
                "Čekám, až začnou oni" to 0,
                "Dělám, že neexistují" to -100
            )
        ),
        Question(
            "Co uděláš, když uděláš chybu?",
            listOf(
                "Přiznám ji a napravím" to 100,
                "Zasměju se tomu" to 50,
                "Snažím se to přejít" to 0,
                "Svedu to na někoho jiného" to -100
            )
        ),
        Question(
            "Jak reaguješ na kompliment?",
            listOf(
                "Poděkuju" to 100,
                "Poděkuju a pochválím taky" to 50,
                "Začnu se smát" to 0,
                "Řeknu, že to není pravda" to -100
            )
        ),
        Question(
            "Co uděláš, když se tvoji kamarádi hádají?",
            listOf(
                "Pokusím se je usmířit" to 100,
                "Vyslechnu obě strany" to 50,
                "Radši odejdu" to 0,
                "Ještě přiliju olej do ohně" to -100
            )
        ),
        Question(
            "Jaký máš přístup k novým výzvám?",
            listOf(
                "Jdu do toho bez strachu" to 100,
                "Nejdřív si to promyslím" to 50,
                "Moc se mi nechce" to 0,
                "Vzdám to předem" to -100
            )
        ),
        Question(
            "Co uděláš, když někdo řekne špatný vtip?",
            listOf(
                "Zasměju se, abych ho podpořil" to 100,
                "Zkusím přidat vlastní vtip" to 50,
                "Jen se pousměju" to 0,
                "Začnu ho kritizovat" to -100
            )
        ),
        Question(
            "Jak by tě popsali tvoji kamarádi?",
            listOf(
                "Spolehlivý člověk" to 100,
                "Vtipálek" to 50,
                "Tichý člověk" to 0,
                "Chaotik" to -100
            )
        )
    )



    private fun goonerQuestions(): List<Question> = listOf(
        Question("Jak rychle poznáš nový internetový meme?", listOf("Okamžitě" to 20, "Většinou rychle" to 15, "Někdy" to 10, "Skoro nikdy" to 5)),
        Question("Co uděláš, když někdo napíše 67?", listOf("Hned odpovím 67" to 20, "Pošlu meme" to 15, "Zasměju se" to 10, "Nevím, o co jde" to 5)),
        Question("Jak dobře znáš brainrot memy?", listOf("Znám skoro všechny" to 20, "Znám hodně z nich" to 15, "Jen pár" to 10, "Vůbec" to 5)),
        Question("Jak často posíláš kamarádům vtipná videa?", listOf("Každý den" to 20, "Často" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Poznáš meme jen podle jedné hlášky?", listOf("Jasně, skoro vždy" to 20, "Většinou" to 15, "Někdy" to 10, "Vůbec" to 5)),
        Question("Co uděláš, když někdo řekne sigma?", listOf("Přidám sigma pózu" to 20, "Řeknu vlastní hlášku" to 15, "Zasměju se" to 10, "Nevím, co to znamená" to 5)),
        Question("Kolik internetových trendů znáš?", listOf("Obrovské množství" to 20, "Docela dost" to 15, "Několik" to 10, "Skoro žádné" to 5)),
        Question("Dokážeš vysvětlit meme, kterému se všichni smějí?", listOf("Jasně a ještě přidám kontext" to 20, "Většinou ano" to 15, "Někdy" to 10, "Vůbec" to 5)),
        Question("Jak často používáš meme hlášky v konverzaci?", listOf("Pořád" to 20, "Často" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Jaký je tvůj meme level?", listOf("Meme legenda" to 20, "Expert" to 15, "Fanoušek" to 10, "Začátečník" to 5))
    )

    private fun startQuestions(name: String) {
        questionType = name
        currentMode = name
        questionIndex = 0
        selectedAnswers.clear()

        randomizedQuestions = (
            when (name) {
                "AURA TEST" -> auraQuestions()
                "GOONER TEST" -> goonerQuestions()
                else -> downQuestions()
            }
        ).shuffled()

        showQuestion()
    }

    private fun showQuestion() {
        val question = randomizedQuestions[questionIndex]
        val page = root()

        page.addView(
            label("💀 $questionType – OTÁZKY", 25f, purple, true),
            params()
        )

        page.addView(
            label(
                "Otázka ${questionIndex + 1} / ${randomizedQuestions.size}",
                16f,
                gold,
                true
            ),
            params()
        )

        page.addView(
            label(question.title, 22f, white, true).apply {
                setPadding(dp(5), dp(25), dp(5), dp(25))
            },
            params()
        )

        question.answers.forEachIndexed { index, answer ->
            val button = makeButton(answer.first)

            if (selectedAnswers[questionIndex] == index) {
                button.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(gold)
            }

            page.addView(
                button,
                LinearLayout.LayoutParams(-1, dp(58)).apply {
                    topMargin = dp(7)
                    bottomMargin = dp(7)
                }
            )

            button.setOnClickListener {
                selectedAnswers[questionIndex] = index
                showQuestion()
            }
        }

        val backButton = makeButton("⬅ ZPĚT")
        val nextButton = makeButton(
            if (questionIndex == randomizedQuestions.lastIndex) {
                "VYHODNOTIT TEST"
            } else {
                "DALŠÍ ➜"
            }
        )

        page.addView(backButton, params())
        page.addView(nextButton, params())

        backButton.setOnClickListener {
            if (questionIndex > 0) {
                questionIndex--
                showQuestion()
            } else {
                showTestChoice(questionType)
            }
        }

        nextButton.setOnClickListener {
            if (!selectedAnswers.containsKey(questionIndex)) {
                Toast.makeText(
                    this,
                    "Nejdřív vyber odpověď!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (questionIndex < randomizedQuestions.lastIndex) {
                questionIndex++
                showQuestion()
            } else {
                finishQuestions()
            }
        }

        setContentView(page)
    }

    private fun finishQuestions() {
        val score = randomizedQuestions.indices.sumOf { index ->
            val answerIndex = selectedAnswers[index]!!
            randomizedQuestions[index].answers[answerIndex].second
        }

        when (questionType) {
            "AURA TEST" -> showAuraResult(score)
            "GOONER TEST" -> showGoonerResult(score)
            else -> showDownResult(score)
        }
    }


    private fun showDownResult(score: Int) {
        val percentage = (score / 2).coerceIn(0, 100)
        val page = root()
        history.add("DOWN TEST: $percentage %")
        page.addView(label("💀 DOWN TEST", 30f, purple, true), params())
        if (percentage >= 100) {
            page.addView(label("ERROR", 34f, red, true), params())
            page.addView(
                label("APLIKACI SI ROZBIL, PROTOŽE JSEŠ AŽ TAK VELKEJ DOWN 💀", 22f, white, true),
                params()
            )
        } else {
            page.addView(label("$percentage %", 42f, gold, true), params())
        }
        val description = when {
            percentage >= 75 -> "Tohle je pořádný DOWN! 💀"
            percentage >= 50 -> "Docela slušný DOWN!"
            percentage >= 25 -> "Trochu DOWN."
            else -> "Nízký DOWN level."
        }
        page.addView(label(description, 20f, white, true), params())
        val againButton = makeButton("🔄 ZKUSIT ZNOVU")
        val menuButton = makeButton("🏠 HLAVNÍ MENU")
        page.addView(againButton, params())
        page.addView(menuButton, params())
        againButton.setOnClickListener { startQuestions("DOWN TEST") }
        menuButton.setOnClickListener { showMenu() }
        setContentView(page)
    }

    private fun showGoonerResult(score: Int) {
        val percentage = (score / 2).coerceIn(0, 100)
        val page = root()
        history.add("GOONER TEST: $percentage %")
        page.addView(label("😎 GOONER MEME TEST", 30f, purple, true), params())
        page.addView(label("$percentage %", 42f, gold, true), params())
        val description = when {
            percentage >= 90 -> "MEME LEGENDA! 🔥"
            percentage >= 70 -> "MEME EXPERT! 😎"
            percentage >= 50 -> "MEME FAN!"
            else -> "MEME ZAČÁTEČNÍK!"
        }
        page.addView(label(description, 20f, white, true), params())
        val againButton = makeButton("🔄 ZKUSIT ZNOVU")
        val menuButton = makeButton("🏠 HLAVNÍ MENU")
        page.addView(againButton, params())
        page.addView(menuButton, params())
        againButton.setOnClickListener { startQuestions("GOONER TEST") }
        menuButton.setOnClickListener { showMenu() }
        setContentView(page)
    }

    private fun showAuraResult(score: Int) {
        val page = root()

        history.add("AURA TEST: $score / 1000")

        page.addView(
            label("✨ AURA TEST", 30f, purple, true),
            params()
        )

        page.addView(
            label("$score / 1000", 42f, gold, true),
            params()
        )

        val description = when {
            score >= 700 -> "Obrovská aura! 🔥"
            score >= 300 -> "Dobrá aura! 😎"
            score >= 0 -> "Neutrální aura."
            score >= -400 -> "Aura trochu pokulhává. 😅"
            else -> "Aura je dneska na dovolené. 💀"
        }

        page.addView(
            label(description, 20f, white, true),
            params()
        )

        val againButton = makeButton("🔄 ZKUSIT ZNOVU")
        val menuButton = makeButton("🏠 HLAVNÍ MENU")

        page.addView(againButton, params())
        page.addView(menuButton, params())

        againButton.setOnClickListener {
            startQuestions("AURA TEST")
        }

        menuButton.setOnClickListener {
            showMenu()
        }

        setContentView(page)
    }


    private fun showModeScreen(name: String) {
        currentMode = name
        val page = root()

        page.addView(label(name, 30f, purple, true), params())
        page.addView(label("Vyber, co chceš udělat.", 18f, white), params())

        val scanButton = makeButton("📷 START SCAN")
        val historyButton = makeButton("📜 HISTORIE")
        val backButton = makeButton("⬅ ZPĚT")

        page.addView(scanButton, params())
        page.addView(historyButton, params())
        page.addView(backButton, params())

        scanButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showCameraScreen()
            } else {
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        historyButton.setOnClickListener { showHistory() }
        backButton.setOnClickListener { showMenu() }

        setContentView(page)
    }

    private fun showHistory() {
        val page = root()

        page.addView(label("📜 HISTORIE", 30f, purple, true), params())

        if (history.isEmpty()) {
            page.addView(
                label("Zatím tu nemáš žádné výsledky.", 17f, white),
                params()
            )
        } else {
            history.asReversed().forEachIndexed { index, item ->
                page.addView(
                    label("${index + 1}. $item", 17f, white),
                    params()
                )
            }
        }

        val clearButton = makeButton("🗑 SMAZAT HISTORII")
        val backButton = makeButton("⬅ ZPĚT")

        page.addView(clearButton, params())
        page.addView(backButton, params())

        clearButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Smazat historii?")
                .setMessage("Opravdu chceš smazat všechny výsledky?")
                .setNegativeButton("NE", null)
                .setPositiveButton("ANO") { _, _ ->
                    history.clear()
                    showHistory()
                }
                .show()
        }

        backButton.setOnClickListener {
            showMenu()
        }

        setContentView(page)
    }

    private fun showCameraScreen() {
        val page = root()

        page.addView(label("📷 $currentMode", 27f, purple, true), params())
        page.addView(label("Připrav se na skenování!", 17f, white), params())

        val camera = PreviewView(this).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
        previewView = camera

        page.addView(
            camera,
            LinearLayout.LayoutParams(-1, 0, 1f).apply {
                topMargin = dp(15)
                bottomMargin = dp(15)
            }
        )

        val status = label("Kamera se připravuje...", 18f, gold, true)
        page.addView(status, params())

        val scanButton = makeButton("🔍 SPUSTIT SKEN")
        val backButton = makeButton("⬅ ZPĚT")

        page.addView(scanButton, params())
        page.addView(backButton, params())

        scanButton.setOnClickListener {
            scanButton.isEnabled = false
            status.text = "Skenování probíhá..."

            handler.postDelayed({
                scanButton.isEnabled = true

                val score = when (currentMode) {
                    "AURA TEST" -> Random.nextInt(-1000, 1001)
                    "DOWN TEST" -> Random.nextInt(0, 101)
                    else -> Random.nextInt(0, 101)
                }

                showScanResult(score)
            }, 3000)
        }

        backButton.setOnClickListener {
            stopCamera()
            if (currentMode == "DOWN TEST" || currentMode == "AURA TEST") {
                showTestChoice(currentMode)
            } else {
                showModeScreen(currentMode)
            }
        }

        setContentView(page)
        startCamera()
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)

        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView?.surfaceProvider)
                }

                provider.unbindAll()
                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview
                )
            } catch (_: Exception) {
                Toast.makeText(
                    this,
                    "Kameru se nepodařilo spustit.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCamera() {
        handler.removeCallbacksAndMessages(null)

        try {
            cameraProvider?.unbindAll()
        } catch (_: Exception) {
        }

        cameraProvider = null
        previewView = null
    }

    private fun showScanResult(score: Int) {
        stopCamera()
        history.add("$currentMode SCAN: " + if (currentMode == "AURA TEST") "$score / 1000" else "$score %")

        val page = root()
        page.addView(label("📊 VÝSLEDEK SCANU", 28f, purple, true), params())

        val resultText = if (currentMode == "AURA TEST") {
            "$score / 1000"
        } else {
            "${score.coerceIn(0, 100)} %"
        }

        page.addView(label(resultText, 40f, gold, true), params())

        val description = when (currentMode) {
            "AURA TEST" -> when {
                score >= 700 -> "Obrovská aura! 🔥"
                score >= 300 -> "Dobrá aura! 😎"
                score >= 0 -> "Neutrální aura."
                score >= -400 -> "Aura trochu pokulhává. 😅"
                else -> "Aura je dneska na dovolené. 💀"
            }
            "DOWN TEST" -> when {
                score >= 75 -> "Tohle je pořádný DOWN! 💀"
                score >= 50 -> "Docela slušný DOWN!"
                score >= 25 -> "Trochu DOWN."
                else -> "Nízký DOWN level."
            }
            else -> "Skenování režimu $currentMode dokončeno!"
        }

        page.addView(label(description, 20f, white, true), params())

        val againButton = makeButton("🔄 ZKUSIT ZNOVU")
        val menuButton = makeButton("🏠 HLAVNÍ MENU")

        page.addView(againButton, params())
        page.addView(menuButton, params())

        againButton.setOnClickListener { showCameraScreen() }
        menuButton.setOnClickListener { showMenu() }

        setContentView(page)
    }

    override fun onDestroy() {
        stopCamera()
        super.onDestroy()
    }
}


