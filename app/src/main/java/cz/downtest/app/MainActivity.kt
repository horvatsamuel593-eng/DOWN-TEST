
package cz.downtest.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val bg = Color.rgb(23, 25, 35)
    private val panel = Color.rgb(36, 39, 56)
    private val purple = Color.rgb(139, 92, 246)
    private val gold = Color.rgb(245, 190, 70)
    private val red = Color.rgb(235, 85, 110)
    private val white = Color.WHITE
    private val muted = Color.rgb(184, 187, 202)

    data class GameMode(
        val name: String,
        val emoji: String,
        val description: String,
        val premium: Boolean,
        val maxScore: Int,
        val percent: Boolean,
        val color: Int
    )

    data class Question(
        val title: String,
        val answers: List<Pair<String, Int>>
    )

    private val modes = listOf(
        GameMode("DOWN TEST", "💀", "Zjisti svoje DOWN skóre", false, 100, true, purple),
        GameMode("AURA TEST", "✨", "Zjisti svou náhodnou auru", false, 1000, false, gold),
        GameMode("GOONER TEST", "🔥", "Náhodné meme skóre", false, 100, true, red),
        GameMode("TROLL TEST", "🗿", "Jak velký troll jsi?", true, 100, true, purple),
        GameMode("GIGACHAD TEST", "💪", "Tvoje náhodná Chad energie", true, 1000, false, gold),
        GameMode("BRAINROT TEST", "🧠", "Brainrot level", true, 100, true, red),
        GameMode("NPC TEST", "🎮", "Jak moc NPC dnes jsi?", true, 100, true, purple),
        GameMode("SIGMA TEST", "🐺", "Tvůj náhodný Sigma level", true, 1000, false, gold)
    )

    private val questions = listOf(
        Question(
            "1. Jak moc posloucháš song Jana Kollera?",
            listOf(
                "Vždycky" to 5,
                "Občas" to 5,
                "Každý týden" to 5,
                "Vůbec ho neposlouchám" to 5
            )
        ),
        Question(
            "2. Na jaké YouTubery se koukáš nebo koukal?",
            listOf(
                "Fizi" to 20,
                "Tary" to 15,
                "Porty" to 10,
                "Dodo" to 5
            )
        ),
        Question(
            "3. Víš, co znamená, když ti někdo řekne, že jsi down?",
            listOf(
                "Vím na 100 %" to 10,
                "Asi jo" to 6,
                "Asi ne" to 3,
                "Vůbec nevím" to 0
            )
        ),
        Question(
            "4. Jak často říkáš 67?",
            listOf(
                "Hodně často" to 5,
                "Občas" to 5,
                "Řekl jsem to jenom párkrát" to 5,
                "Nikdy jsem to neřekl" to 0
            )
        ),
        Question(
            "5. Kolik si myslíš, že ti vyjde výsledek DOWN TESTU?",
            listOf(
                "Míň než 50" to 20,
                "Víc než 50" to 10
            )
        )
    )

    private val histories = mutableMapOf<Int, MutableList<Int>>()
    private var selectedMode = 0

    private lateinit var previewView: PreviewView
    private lateinit var status: TextView
    private lateinit var result: TextView
    private lateinit var history: TextView
    private lateinit var scanButton: Button

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            if (::status.isInitialized) {
                status.text = "Kamera nebyla povolena."
            }
            Toast.makeText(this, "Pro náhled je potřeba povolit kameru.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showMenu()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun match(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(-1, -2)
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    private fun button(value: String): Button {
        return Button(this).apply {
            text = value
            textSize = 15f
            setTextColor(white)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(purple)
            isAllCaps = false
        }
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
        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
        }

        val root = newRoot()
        scroll.addView(root)
        setContentView(scroll)

        root.addView(
            text("DOWN TEST 💀", 31f, white, true).apply {
                gravity = Gravity.CENTER
            },
            match()
        )

        root.addView(
            text("VYBER SI SVŮJ TEST", 14f, muted).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(5), 0, dp(24))
            },
            match()
        )

        addSection(root, "ZDARMA – 3 REŽIMY", purple)

        modes.forEachIndexed { index, mode ->
            if (!mode.premium) addModeCard(root, index, mode)
        }

        addSection(root, "PREMIUM – 5 REŽIMŮ", gold)

        modes.forEachIndexed { index, mode ->
            if (mode.premium) addModeCard(root, index, mode)
        }

        val premium = button("🔓 OTEVŘÍT VŠECHNY MÓDY – 20 Kč")
        root.addView(
            premium,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(18)
            }
        )

        premium.setOnClickListener {
            showPremiumDialog()
        }

        root.addView(
            text("Všechny výsledky jsou pouze pro zábavu.", 12f, muted).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(18), 0, 0)
            },
            match()
        )
    }

    private fun addSection(root: LinearLayout, label: String, color: Int) {
        root.addView(
            text(label, 17f, color, true).apply {
                setPadding(0, dp(12), 0, dp(8))
            },
            match()
        )
    }

    private fun addModeCard(root: LinearLayout, index: Int, mode: GameMode) {
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

        info.addView(
            text("${mode.emoji}  ${mode.name}", 17f, white, true),
            match()
        )

        info.addView(
            text(mode.description, 12f, muted).apply {
                setPadding(0, dp(4), 0, 0)
            },
            match()
        )

        card.addView(info)

        card.addView(
            text(
                if (mode.premium) "🔒" else "HRÁT  ›",
                15f,
                if (mode.premium) gold else purple,
                true
            ).apply {
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(8), dp(4), dp(8))
            }
        )

        root.addView(
            card,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(9)
            }
        )

        card.setOnClickListener {
            if (mode.premium) {
                showPremiumDialog()
            } else {
                selectedMode = index
                if (index == 0) showDownChoice() else showModeScreen()
            }
        }
    }

    private fun showPremiumDialog() {
        AlertDialog.Builder(this)
            .setTitle("🔒 Prémiové režimy")
            .setMessage("Tento režim zatím není dostupný. Prémiové odemykání ještě není nastavené.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDownChoice() {
        val root = newRoot()
        setContentView(root)

        root.addView(
            text("💀 DOWN TEST", 29f, white, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(30), 0, dp(15))
            },
            match()
        )

        root.addView(
            text("Vyber si způsob testování", 16f, muted).apply {
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(25))
            },
            match()
        )

        val scan = button("📸 SCAN")
        root.addView(scan, LinearLayout.LayoutParams(-1, dp(65)))
        scan.setOnClickListener {
            showModeScreen()
        }

        val quiz = button("📝 OTÁZKY")
        root.addView(
            quiz,
            LinearLayout.LayoutParams(-1, dp(65)).apply {
                topMargin = dp(15)
            }
        )
        quiz.setOnClickListener {
            showDownQuestions()
        }

        val back = button("‹ ZPĚT DO NABÍDKY")
        root.addView(
            back,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(35)
            }
        )
        back.setOnClickListener {
            showMenu()
        }
    }

    private fun showDownQuestions() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
        }

        val root = newRoot()
        scroll.addView(root)
        setContentView(scroll)

        val selected = mutableMapOf<Int, Int>()
        val answerButtons = mutableListOf<Triple<Int, Int, Button>>()

        root.addView(
            text("💀 DOWN TEST – OTÁZKY", 25f, white, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(12))
            },
            match()
        )

        root.addView(
            text("Vyber jednu odpověď u každé otázky.", 14f, muted).apply {
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(20))
            },
            match()
        )

        questions.forEachIndexed { questionIndex, question ->
            root.addView(
                text(question.title, 17f, white, true).apply {
                    setPadding(0, dp(14), 0, dp(8))
                },
                match()
            )

            question.answers.forEachIndexed { answerIndex, answer ->
                val answerButton = button(answer.first)
                answerButton.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(panel)

                root.addView(
                    answerButton,
                    LinearLayout.LayoutParams(-1, -2).apply {
                        bottomMargin = dp(5)
                    }
                )

                answerButtons.add(
                    Triple(questionIndex, answerIndex, answerButton)
                )

                answerButton.setOnClickListener {
                    selected[questionIndex] = answerIndex

                    answerButtons.forEach { item ->
                        if (item.first == questionIndex) {
                            item.third.backgroundTintList =
                                android.content.res.ColorStateList.valueOf(
                                    if (item.second == answerIndex) purple else panel
                                )
                        }
                    }

                    val calculate = root.findViewWithTag<Button>("calculate")
                    calculate?.isEnabled = selected.size == questions.size
                    calculate?.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(
                            if (selected.size == questions.size) purple else muted
                        )
                }
            }
        }

        val calculate = button("VYHODNOTIT TEST").apply {
            tag = "calculate"
            isEnabled = false
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(muted)
        }

        root.addView(
            calculate,
            LinearLayout.LayoutParams(-1, dp(60)).apply {
                topMargin = dp(22)
            }
        )

        calculate.setOnClickListener {
            if (selected.size != questions.size) return@setOnClickListener

            val total = questions.indices.sumOf { index ->
                val answerIndex = selected[index]!!
                questions[index].answers[answerIndex].second
            }

            showDownResult(total)
        }

        val back = button("‹ ZPĚT")
        root.addView(
            back,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(12)
            }
        )
        back.setOnClickListener {
            showDownChoice()
        }
    }

    private fun showDownResult(total: Int) {
        val scores = histories.getOrPut(0) { mutableListOf() }
        scores.add(0, total)
        if (scores.size > 8) scores.removeAt(scores.lastIndex)

        val root = newRoot()
        setContentView(root)

        root.addView(
            text("💀 DOWN TEST – VÝSLEDEK", 25f, white, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(25), 0, dp(25))
            },
            match()
        )

        if (total > 100) {
            root.addView(
                text("💥 ERROR 💥", 34f, red, true).apply {
                    gravity = Gravity.CENTER
                },
                match()
            )

            root.addView(
                text(
                    "ROZBIL JSI APLIKACI TÍM, JAK VELKEJ DOWN JSEŠ. 💀",
                    23f,
                    red,
                    true
                ).apply {
                    gravity = Gravity.CENTER
                },
                match()
            )
        } else {
            root.addView(
                text("$total %", 58f, purple, true).apply {
                    gravity = Gravity.CENTER
                    setPadding(0, dp(25), 0, dp(20))
                },
                match()
            )

            root.addView(
                text("Tvoje DOWN skóre", 18f, white).apply {
                    gravity = Gravity.CENTER
                },
                match()
            )
        }

        val share = button("SDÍLET VÝSLEDEK")
        root.addView(
            share,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(30)
            }
        )

        share.setOnClickListener {
            shareText("Můj DOWN TEST výsledek: $total % 💀")
        }

        val again = button("ZKUSIT ZNOVU")
        root.addView(
            again,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(12)
            }
        )
        again.setOnClickListener {
            showDownQuestions()
        }

        val menu = button("ZPĚT DO MENU")
        root.addView(
            menu,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(12)
            }
        )
        menu.setOnClickListener {
            showMenu()
        }
    }

    private fun showModeScreen() {
        val mode = modes[selectedMode]

        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
        }

        val root = newRoot()
        scroll.addView(root)
        setContentView(scroll)

        val back = button("‹ ZPĚT")
        root.addView(back, match())
        back.setOnClickListener {
            stopCamera()
            if (selectedMode == 0) showDownChoice() else showMenu()
        }

        root.addView(
            text("${mode.emoji} ${mode.name}", 27f, white, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(22), 0, dp(5))
            },
            match()
        )

        root.addView(
            text(mode.description, 14f, muted).apply {
                gravity = Gravity.CENTER
            },
            match()
        )

        val cameraCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), dp(13), dp(13), dp(13))
            setBackgroundColor(panel)
        }

        root.addView(
            cameraCard,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(22)
            }
        )

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

        result = text("${mode.name}: --", 29f, mode.color, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(8))
        }
        root.addView(result, match())

        root.addView(
            text("Výsledek je náhodný a slouží pouze pro zábavu.", 13f, muted).apply {
                gravity = Gravity.CENTER
            },
            match()
        )

        val share = button("SDÍLET VÝSLEDEK")
        root.addView(
            share,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(14)
            }
        )
        share.setOnClickListener {
            shareText("Můj výsledek v ${mode.name}: ${result.text}")
        }

        root.addView(
            text("HISTORIE", 18f, white, true).apply {
                setPadding(0, dp(24), 0, dp(8))
            },
            match()
        )

        history = text("Zatím žádné výsledky.", 14f, muted)
        root.addView(history, match())

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }

        updateHistory()
    }

    private fun startCamera() {
        if (!::previewView.isInitialized) return

        val future = ProcessCameraProvider.getInstance(this)

        future.addListener({
            try {
                val provider = future.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)

                provider.unbindAll()
                provider.bindToLifecycle(
                    this,
               
    