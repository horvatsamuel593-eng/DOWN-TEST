   
package cz.downtest.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
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
    val answers: List<Pair<String, Int>>,
    val imageUrl: String? = null
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
    private var footballLives = 3
    private var randomizedQuestions = listOf<Question>()
    private val selectedAnswers = mutableMapOf<Int, Int>()

    private var tts: TextToSpeech? = null
    private var audioPlayer: MediaPlayer? = null

    private fun playClip(clipNumber: Int, onComplete: (() -> Unit)? = null) {
        audioPlayer?.release()
        val clipId = when (clipNumber) {
            in 1..14 -> resources.getIdentifier("cast_${clipNumber.toString().padStart(2, '0')}", "raw", packageName)
            else -> 0
        }
        if (clipId == 0) { onComplete?.invoke(); return }
        audioPlayer = MediaPlayer.create(this, clipId)
        audioPlayer?.setOnCompletionListener { player -> player.release(); audioPlayer = null; onComplete?.invoke() }
        audioPlayer?.setOnErrorListener { player, _, _ -> player.release(); audioPlayer = null; onComplete?.invoke(); true }
        audioPlayer?.start()
    }

    private fun playRandomClip(range: IntRange, onComplete: (() -> Unit)? = null) {
        playClip(range.random(), onComplete)
    }

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
        GameMode("SIGMA TEST", "🐺", true),
        GameMode("FOOTBALL PASS", "⚽", true)
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
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = java.util.Locale("cs", "CZ")
                tts?.setPitch(1.12f)
                tts?.setSpeechRate(1.12f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onError(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        // Football quiz advances when the correct-answer audio clip finishes.
                    }
                })
            }
        }
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
                if (mode.name == "FOOTBALL PASS") {
                    showFootballPassScreen()
                } else if (mode.premium) {
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
        Question(            "Jak často říkáš 67?",
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
        Question("Jak moc tě dokážou pohltit internetové memy?", listOf("Úplně mě vtáhnou 💀" to 20, "Docela dost" to 15, "Jen občas" to 10, "Vůbec" to 5)),
        Question("Jak dlouho vydržíš v „goon mode“ podle internetových memů?", listOf("Jsem nezastavitelný" to 20, "Několik hodin" to 15, "Jen pár minut" to 10, "Ani nevím, co to je" to 5)),
        Question("Proč nejčastěji chodíš na internet?", listOf("Abych se odreagoval, když se nudím" to 20, "Kvůli videím a zábavě" to 15, "Vyhledávám si různé věci" to 10, "Nakupuju různé věci, třeba nábytek" to 5)),
        Question("Dokážeš se od svého telefonu na chvíli odtrhnout?", listOf("Bez problémů" to 20, "Většinou ano" to 15, "Moc ne" to 10, "Telefon je můj život" to 5)),
        Question("Jak moc znáš gooning memy?", listOf("Jsem expert" to 20, "Znám jich hodně" to 15, "Znám jen pár" to 10, "Vůbec nevím, o čem mluvíš" to 5)),
        Question("Jak bys reagoval, kdyby někdo řekl „goon mode activated“?", listOf("Okamžitě pochopím" to 20, "Začnu se smát" to 15, "Budu dělat, že nic" to 10, "Zeptám se, co to znamená" to 5)),
        Question("Jak často používáš slovo gooning v konverzaci?", listOf("Každý den" to 20, "Každý týden" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Jak dobře dokážeš ovládat svoje internetové návyky?", listOf("Naprosto dokonale" to 20, "Docela dobře" to 15, "Někdy mám problém" to 10, "Internet mě ovládá" to 5)),
        Question("Jak moc bereš gooning memy vážně?", listOf("Vůbec, je to jen meme" to 20, "Trochu" to 15, "Docela dost" to 10, "Beru to až moc vážně" to 5)),
        Question("Jaký máš podle sebe gooner level?", listOf("Level 1000" to 20, "Level 500" to 15, "Level 100" to 10, "Level 0" to 5))
    )

    private fun trollQuestions(): List<Question> = listOf(
        Question("Jak často děláš kamarádům naschvály?", listOf("Pořád" to 20, "Často" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Jak reaguješ, když se někdo nachytá na tvůj prank?", listOf("Směju se jako blázen" to 20, "Zasměju se" to 15, "Řeknu mu pravdu" to 10, "Je mi to jedno" to 5)),
        Question("Jaký prank máš nejradši?", listOf("Překvapení" to 20, "Falešné zprávy" to 15, "Vtipné zvuky" to 10, "Žádný" to 5)),
        Question("Dokážeš udržet vážnou tvář?", listOf("Nikdy" to 20, "Skoro nikdy" to 15, "Většinou ano" to 10, "Vždycky" to 5)),
        Question("Jak často říkáš „to byl jen prank“?", listOf("Každý den" to 20, "Často" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Co uděláš, když někdo nachytá tebe?", listOf("Vymyslím odvetu" to 20, "Zasměju se" to 15, "Trochu se naštvu" to 10, "Neřeším to" to 5)),
        Question("Jaký máš troll level?", listOf("1000" to 20, "500" to 15, "100" to 10, "0" to 5)),
        Question("Jak dlouho dokážeš někoho napínat?", listOf("Celý den" to 20, "Několik hodin" to 15, "Pár minut" to 10, "Vůbec" to 5)),
        Question("Jak reaguješ na špatný vtip?", listOf("Udělám ještě horší" to 20, "Zasměju se" to 15, "Protočím oči" to 10, "Ignoruju ho" to 5)),
        Question("Jsi ve třídě známý jako troll?", listOf("Totální legenda" to 20, "Docela jo" to 15, "Trochu" to 10, "Vůbec" to 5))
    )

    private fun gigachadQuestions(): List<Question> = listOf(
        Question("Jak moc si věříš?", listOf("Maximálně" to 20, "Hodně" to 15, "Tak napůl" to 10, "Moc ne" to 5)),
        Question("Pomůžeš kamarádovi, když potřebuje?", listOf("Vždycky" to 20, "Většinou" to 15, "Někdy" to 10, "Nikdy" to 5)),
        Question("Jak reaguješ na prohru?", listOf("Pogratuluju soupeři" to 20, "Zkusím to znovu" to 15, "Trochu se naštvu" to 10, "Vzdám to" to 5)),
        Question("Jak moc jsi odvážný?", listOf("Extrémně" to 20, "Hodně" to 15, "Normálně" to 10, "Spíš ne" to 5)),
        Question("Co uděláš, když tě někdo urazí?", listOf("Zachovám klid" to 20, "Zasměju se" to 15, "Odpovím mu" to 10, "Naštvu se" to 5)),
        Question("Jak často dodržíš slib?", listOf("Vždycky" to 20, "Skoro vždycky" to 15, "Občas" to 10, "Skoro nikdy" to 5)),
        Question("Jaký je tvůj GIGACHAD level?", listOf("1000" to 20, "500" to 15, "100" to 10, "0" to 5)),
        Question("Dokážeš přiznat chybu?", listOf("Bez problémů" to 20, "Většinou ano" to 15, "Nerad" to 10, "Nikdy" to 5)),
        Question("Jak reaguješ na výzvy?", listOf("Jdu do toho" to 20, "Nejdřív přemýšlím" to 15, "Moc se mi nechce" to 10, "Vzdám to" to 5)),
        Question("Jak by tě popsali kamarádi?", listOf("GIGACHAD" to 20, "Sigma" to 15, "Normální člověk" to 10, "NPC" to 5))
    )

    private fun brainrotQuestions(): List<Question> = listOf(
        Question("Jak často používáš brainrot hlášky?", listOf("Pořád" to 20, "Často" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Znáš význam slova Skibidi?", listOf("Samozřejmě" to 20, "Asi ano" to 15, "Trochu" to 10, "Vůbec" to 5)),
        Question("Co uděláš, když někdo řekne 67?", listOf("Okamžitě odpovím" to 20, "Začnu se smát" to 15, "Jen se podívám" to 10, "Nevím, co to znamená" to 5)),
        Question("Jak často sleduješ brainrot videa?", listOf("Každý den" to 20, "Několikrát týdně" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Jak dobře znáš internetové trendy?", listOf("Jsem expert" to 20, "Znám jich hodně" to 15, "Znám pár" to 10, "Vůbec" to 5)),
        Question("Co znamená „rizz“?", listOf("Umění zaujmout" to 20, "Druh jídla" to 15, "Název hry" to 10, "Nevím" to 5)),
        Question("Jaký brainrot meme je nejlepší?", listOf("Skibidi" to 20, "67" to 15, "Sigma" to 10, "Jiný" to 5)),
        Question("Dokážeš mluvit celý den jen v meme hláškách?", listOf("Jasně" to 20, "Několik hodin" to 15, "Chvíli" to 10, "Ne" to 5)),
        Question("Jak často posíláš brainrot kamarádům?", listOf("Pořád" to 20, "Často" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Jaký máš brainrot level?", listOf("1000" to 20, "500" to 15, "100" to 10, "0" to 5))
    )

    private fun npcQuestions(): List<Question> = listOf(
        Question("Jak často děláš pořád to samé?", listOf("Každý den" to 20, "Často" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Co uděláš, když tě někdo pozdraví?", listOf("Pozdravím zpátky" to 20, "Usměju se" to 15, "Jen se podívám" to 10, "Nevím, co dělat" to 5)),
        Question("Jak často říkáš stejnou větu?", listOf("Pořád" to 20, "Často" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Jak reaguješ na nečekanou situaci?", listOf("Okamžitě ji vyřeším" to 20, "Chvíli přemýšlím" to 15, "Zmatkuju" to 10, "Zamrznu" to 5)),
        Question("Jaký máš herní NPC level?", listOf("Boss NPC" to 20, "Vedlejší postava" to 15, "Obyčejné NPC" to 10, "Hlavní NPC" to 5)),
        Question("Jak často se ztratíš ve vlastních myšlenkách?", listOf("Pořád" to 20, "Často" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Dokážeš vymyslet vlastní hlášku?", listOf("Pořád" to 20, "Často" to 15, "Někdy" to 10, "Nikdy" to 5)),
        Question("Co děláš, když čekáš?", listOf("Vymýšlím něco" to 20, "Kouknu na mobil" to 15, "Jen stojím" to 10, "Zapomenu, na co čekám" to 5)),
        Question("Jak často se chováš jako postava z hry?", listOf("Pořád" to 20, "Často" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Jaký máš NPC level?", listOf("1000" to 20, "500" to 15, "100" to 10, "0" to 5))
    )

    private fun sigmaQuestions(): List<Question> = listOf(
        Question("Jak moc jsi samostatný?", listOf("Maximálně" to 20, "Hodně" to 15, "Tak napůl" to 10, "Vůbec" to 5)),
        Question("Radši pracuješ sám, nebo v týmu?", listOf("Sám" to 20, "Spíš sám" to 15, "Spíš v týmu" to 10, "V týmu" to 5)),
        Question("Jak reaguješ na kritiku?", listOf("Vezmu si z ní ponaučení" to 20, "Vyslechnu ji" to 15, "Moc ji neřeším" to 10, "Naštvu se" to 5)),
        Question("Jak často dodržuješ svoje cíle?", listOf("Vždycky" to 20, "Většinou" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Co uděláš, když tě někdo podcení?", listOf("Ukážu, co umím" to 20, "Neřeším to" to 15, "Zasměju se" to 10, "Naštvu se" to 5)),
        Question("Jak moc jsi tajemný?", listOf("Extrémně" to 20, "Docela dost" to 15, "Trochu" to 10, "Vůbec" to 5)),
        Question("Jaký je tvůj Sigma level?", listOf("1000" to 20, "500" to 15, "100" to 10, "0" to 5)),
        Question("Dokážeš zůstat v klidu pod tlakem?", listOf("Vždycky" to 20, "Většinou" to 15, "Někdy" to 10, "Nikdy" to 5)),
        Question("Jak často jdeš vlastní cestou?", listOf("Pořád" to 20, "Často" to 15, "Občas" to 10, "Nikdy" to 5)),
        Question("Co je podle tebe největší Sigma vlastnost?", listOf("Sebevědomí" to 20, "Samostatnost" to 15, "Klid" to 10, "Věrnost kamarádům" to 5))
    )


    private fun showFootballPassScreen() {
        val page = root()
        page.addView(label("⚽ FOOTBALL PASS", 30f, gold, true), params())
        page.addView(label("4 fotbalové testy • cena plánovaného passu 30 Kč", 16f, white), params())
        page.addView(label("Nákup zatím není aktivní. FOOTBALL IQ TEST si můžeš zatím vyzkoušet v náhledovém režimu.", 15f, white), params())

        val iqButton = makeButton("⚽ FOOTBALL IQ TEST – NÁHLED")
        val barcaButton = makeButton("❤️💙 BARÇA FAN TEST 🔒")
        val madridButton = makeButton("🤍 MADRID TEST 🔒")
        val czechButton = makeButton("🇨🇿 CZECH FOOTBALL IQ 🔒")
        val backButton = makeButton("⬅ ZPĚT")

        page.addView(iqButton, params())
        page.addView(barcaButton, params())
        page.addView(madridButton, params())
        page.addView(czechButton, params())
        page.addView(backButton, params())

        iqButton.setOnClickListener { startQuestions("FOOTBALL IQ TEST") }
        listOf(barcaButton, madridButton, czechButton).forEach { button ->
            button.setOnClickListener {
                Toast.makeText(this, "Tento test bude doplněn později.", Toast.LENGTH_SHORT).show()
            }
        }
        backButton.setOnClickListener { showMenu() }
        setContentView(page)
    }

    private fun footballIqQuestions(): List<Question> = listOf(
        Question("1/5 – POZNEJ KLUB PODLE ZNAKU", listOf("Metalist 1925 Charkov" to 1, "FK Jablonec" to 0, "FC Argeș" to 0, "Zimbru Kišiněv" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/FC_Metalist_1925_Kharkiv_logo.png"),
        Question("2/5 – POZNEJ KLUB PODLE ZNAKU", listOf("FC Saarbrücken" to 0, "SV Elversberg" to 1, "Wehen Wiesbaden" to 0, "Jahn Regensburg" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/SV_Elversberg_logo.webp"),
        Question("3/5 – POZNEJ KLUB PODLE ZNAKU", listOf("Raith Rovers" to 0, "Dunfermline Athletic" to 1, "Greenock Morton" to 0, "Ayr United" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/-2019-08-23_Dunfermline_Athletic_F.C._football_Pin_badge.JPG"),
        Question("4/5 – POZNEJ KLUB PODLE ZNAKU", listOf("FC Voluntari" to 0, "FC Botoșani" to 1, "Petrolul Ploiești" to 0, "UTA Arad" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/FC_Boto%C8%99ani_logo.png"),
        Question("5/5 – POZNEJ KLUB PODLE ZNAKU", listOf("FC Vaduz" to 1, "FC Lugano" to 0, "FC Winterthur" to 0, "FC Sion" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/FCV_Bildmarke_1-Farbig_Rot.png"),
        Question("6. Který klub vyhrál první ročník Poháru mistrů evropských zemí v roce 1956?", listOf("AC Milan" to 0, "Real Madrid" to 1, "Benfica" to 0, "Stade de Reims" to 0)),
        Question("7. Kdo vstřelil hattrick ve finále MS 1966?", listOf("Bobby Charlton" to 0, "Geoff Hurst" to 1, "Pelé" to 0, "Eusébio" to 0)),
        Question("8. Která země vyhrála EURO 1992, přestože se původně nekvalifikovala?", listOf("Švédsko" to 0, "Nizozemsko" to 0, "Dánsko" to 1, "Skotsko" to 0)),
        Question("9. Který klub porazil Barcelonu 4:0 ve finále Ligy mistrů 1994?", listOf("Juventus" to 0, "AC Milan" to 1, "Ajax" to 0, "Manchester United" to 0)),
        Question("10. Kdo získal Zlatý míč v roce 1995?", listOf("George Weah" to 1, "Ronaldo" to 0, "Jürgen Klinsmann" to 0, "Alan Shearer" to 0)),
        Question("11/15 – POZNEJ STADION", listOf("Allianz Arena" to 0, "Signal Iduna Park" to 1, "Veltins-Arena" to 0, "Olympiastadion Berlin" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Signal_iduna_park.jpg"),
        Question("12/15 – POZNEJ STADION", listOf("Maracanã" to 0, "Estadio Azteca" to 1, "Monumental" to 0, "Wembley" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Estadio_azteca.jpg"),
        Question("13/15 – POZNEJ STADION", listOf("Anfield" to 1, "Stamford Bridge" to 0, "Emirates Stadium" to 0, "Old Trafford" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Anfield_Stadium.jpg"),
        Question("14/15 – POZNEJ STADION", listOf("Stadio Olimpico" to 0, "San Siro" to 1, "Allianz Stadium" to 0, "Diego Armando Maradona Stadium" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/San_Siro_Stadium_01.jpg"),
        Question("15/15 – POZNEJ STADION", listOf("Estádio do Dragão" to 0, "José Alvalade" to 0, "Estádio da Luz" to 1, "Santiago Bernabéu" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Estadio_da_luz.jpg"),
        Question("16/20 – POZNEJ HRÁČE", listOf("Andrea Pirlo" to 1, "Xabi Alonso" to 0, "Gennaro Gattuso" to 0, "Andrea Barzagli" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Andrea_Pirlo.jpg"),
        Question("17/20 – POZNEJ HRÁČE", listOf("Didier Drogba" to 0, "Samuel Eto'o" to 1, "Emmanuel Adebayor" to 0, "Nicolas Anelka" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Samuel_Eto%27o.jpg"),
        Question("18/20 – POZNEJ HRÁČE", listOf("Thierry Henry" to 0, "Dennis Bergkamp" to 1, "Robin van Persie" to 0, "Patrick Vieira" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Bergkamp.jpg"),
        Question("19/20 – POZNEJ HRÁČE", listOf("Edgar Davids" to 0, "Clarence Seedorf" to 1, "Patrick Vieira" to 0, "Claude Makélélé" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/C-Seedorf.jpg"),
        Question("20/20 – POZNEJ HRÁČE", listOf("Carlos Tevez" to 0, "Sergio Agüero" to 1, "Gonzalo Higuaín" to 0, "Radamel Falcao" to 0), imageUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Sergio_Ag%C3%BCero.jpg")
    )

    private fun startQuestions(name: String) {
        questionType = name
        currentMode = name
        questionIndex = 0
        footballLives = 3
        selectedAnswers.clear()

        randomizedQuestions = (
            when (name) {
                "AURA TEST" -> auraQuestions()
                "GOONER TEST" -> goonerQuestions()                "TROLL TEST" -> trollQuestions()
                "GIGACHAD TEST" -> gigachadQuestions()
                "BRAINROT TEST" -> brainrotQuestions()
                "NPC TEST" -> npcQuestions()
                "SIGMA TEST" -> sigmaQuestions()
                "FOOTBALL IQ TEST" -> footballIqQuestions()
                else -> downQuestions()
            }
        ).shuffled()

        showQuestion()
        if (name == "FOOTBALL IQ TEST") playClip(14)
    }

    private fun showQuestion() {
        val question = randomizedQuestions[questionIndex]
        val isFootball = questionType == "FOOTBALL IQ TEST"
        val page = root()

        if (isFootball) {
            page.setBackgroundColor(Color.rgb(48, 51, 57))
        }

        page.addView(
            label(if (isFootball) "⚽ FOOTBALL PASS" else "💀 $questionType – OTÁZKY", 25f, if (isFootball) white else purple, true),
            params()
        )
        page.addView(
            label("Otázka ${questionIndex + 1} / ${randomizedQuestions.size}", 16f, gold, true),
            params()
        )
        if (isFootball) {
            page.addView(label("❤️ ŽIVOTY: $footballLives / 3", 18f, if (footballLives == 1) Color.rgb(255, 110, 110) else gold, true), params())
        }
        page.addView(
            label(question.title, 22f, white, true).apply {
                setPadding(dp(5), dp(25), dp(5), dp(25))
            },
            params()
        )

        question.imageUrl?.let { imageUrl ->
            val questionImage = ImageView(this).apply {
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER
                setBackgroundColor(Color.rgb(65, 68, 74))
                contentDescription = "Obrázek k fotbalové otázce"
            }
            page.addView(questionImage, LinearLayout.LayoutParams(-1, dp(220)).apply {
                bottomMargin = dp(14)
            })
            Thread {
                try {
                    val connection = java.net.URL(imageUrl).openConnection()
                    connection.connectTimeout = 10000
                    connection.readTimeout = 15000
                    connection.getInputStream().use { stream ->
                        val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                        if (bitmap != null) runOnUiThread { questionImage.setImageBitmap(bitmap) }
                    }
                } catch (_: Exception) {
                    runOnUiThread { questionImage.setBackgroundColor(Color.rgb(80, 55, 58)) }
                }
            }.start()
        }

        question.answers.forEachIndexed { index, answer ->
            val button = makeButton(answer.first)
            if (isFootball) {
                button.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(82, 86, 94))
            }
            if (selectedAnswers[questionIndex] == index) {
                val selectedIsCorrect = answer.second == 1
                button.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    if (isFootball && !selectedIsCorrect) Color.rgb(205, 48, 55)
                    else if (isFootball) Color.rgb(48, 145, 92)
                    else gold
                )
            }
            page.addView(button, LinearLayout.LayoutParams(-1, dp(58)).apply {
                topMargin = dp(7)
                bottomMargin = dp(7)
            })
            button.setOnClickListener {
                if (isFootball) {
                    if (selectedAnswers[questionIndex]?.let { randomizedQuestions[questionIndex].answers[it].second == 1 } == true) {
                        return@setOnClickListener
                    }
                    val isCorrect = answer.second == 1
                    if (!isCorrect) {
                        footballLives--
                        playRandomClip(6..10) { playRandomClip(11..12) }
                        if (footballLives <= 0) {
                            Toast.makeText(this, "Došly ti životy! Začínáš znovu od první otázky.", Toast.LENGTH_LONG).show()
                            startQuestions("FOOTBALL IQ TEST")
                        } else {
                            selectedAnswers[questionIndex] = index
                            showQuestion()
                        }
                        return@setOnClickListener
                    }
                    selectedAnswers[questionIndex] = index
                    button.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(48, 190, 105))
                    showQuestion()
                    playRandomClip(1..5) { advanceFootballQuestion() }
                } else {
                    selectedAnswers[questionIndex] = index
                    showQuestion()
                }
            }
        }

        if (!isFootball) {
            val backButton = makeButton("⬅ ZPĚT")
            val nextButton = makeButton(if (questionIndex == randomizedQuestions.lastIndex) "VYHODNOTIT TEST" else "DALŠÍ ➜")
            page.addView(backButton, params())
            page.addView(nextButton, params())
            backButton.setOnClickListener {
                if (questionIndex > 0) { questionIndex--; showQuestion() }
                else showTestChoice(questionType)
            }
            nextButton.setOnClickListener {
                if (!selectedAnswers.containsKey(questionIndex)) {
                    Toast.makeText(this, "Nejdřív vyber odpověď!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (questionIndex < randomizedQuestions.lastIndex) { questionIndex++; showQuestion() }
                else finishQuestions()
            }
        }

        if (isFootball) {
            val scroll = ScrollView(this).apply {
                isFillViewport = true
                isSmoothScrollingEnabled = true
                setBackgroundColor(Color.rgb(48, 51, 57))
                addView(page)
            }
            setContentView(scroll)
        } else {
            setContentView(page)
        }
    }

    private fun advanceFootballQuestion() {
        if (questionIndex < randomizedQuestions.lastIndex) {
            questionIndex++
            showQuestionWithSlide(findViewById(android.R.id.content))
        } else {
            finishQuestions()
        }
    }

    private fun showQuestionWithSlide(oldPage: View) {
        showQuestion()
        val newPage = findViewById<android.view.ViewGroup>(android.R.id.content).getChildAt(0)
        newPage.translationX = resources.displayMetrics.widthPixels.toFloat()
        newPage.animate()
            .translationX(0f)
            .setDuration(320)
            .start()
    }

    private fun finishQuestions() {
        val score = randomizedQuestions.indices.sumOf { index ->
            val answerIndex = selectedAnswers[index]!!
            randomizedQuestions[index].answers[answerIndex].second
        }

        when (questionType) {
            "AURA TEST" -> showAuraResult(score)
            "GOONER TEST" -> showGoonerResult(score)
            "TROLL TEST", "GIGACHAD TEST", "BRAINROT TEST", "NPC TEST", "SIGMA TEST" -> showPremiumModeResult(score)
            "FOOTBALL IQ TEST" -> showFootballIqResult(score)
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


    private fun showFootballIqResult(score: Int) {
        val page = root()
        val percentage = (score * 5).coerceIn(0, 100)
        history.add("FOOTBALL IQ TEST: $score / 20")
        page.addView(label("⚽ FOOTBALL IQ TEST", 28f, gold, true), params())
        page.addView(label("$score / 20 správně", 25f, white, true), params())
        page.addView(label("$percentage %", 42f, gold, true), params())
        val rank = when {
            score >= 18 -> "FOTBALOVÝ GENIUS 🏆"
            score >= 15 -> "SVĚTOVÁ TŘÍDA 🔥"
            score >= 11 -> "VELMI DOBRÉ ZNALOSTI ⚽"
            score >= 7 -> "SOLIDNÍ FANOUŠEK"
            else -> "JEŠTĚ JE CO STUDOVAT"
        }
        page.addView(label(rank, 20f, white, true), params())
        page.addView(label("Toto je zatím náhledová verze bez skutečných obrázků a nákupu passu.", 14f, white), params())
        val againButton = makeButton("🔄 ZKUSIT ZNOVU")
        val passButton = makeButton("⬅ ZPĚT NA FOOTBALL PASS")
        page.addView(againButton, params())
        page.addView(passButton, params())
        againButton.setOnClickListener { startQuestions("FOOTBALL IQ TEST") }
        passButton.setOnClickListener { showFootballPassScreen() }
        setContentView(page)
    }

    private fun showPremiumModeResult(score: Int) {
        val percentage = (score / 2).coerceIn(0, 100)
        val page = root()
        history.add("$questionType: $percentage %")
        page.addView(label("✨ $questionType", 30f, purple, true), params())
        page.addView(label("$percentage %", 42f, gold, true), params())
        val description = when {
            percentage >= 90 -> "Absolutní legenda! 🔥"
            percentage >= 70 -> "Skvělý výsledek! 😎"
            percentage >= 50 -> "Docela dobrý výsledek!"
            else -> "Ještě máš co zlepšovat! 💀"
        }
        page.addView(label(description, 20f, white, true), params())
        val againButton = makeButton("🔄 ZKUSIT ZNOVU")
        val menuButton = makeButton("🏠 HLAVNÍ MENU")
        page.addView(againButton, params())
        page.addView(menuButton, params())
        againButton.setOnClickListener { startQuestions(questionType) }
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

        val description = when {            score >= 700 -> "Obrovská aura! 🔥"
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
        audioPlayer?.release()
        audioPlayer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}

