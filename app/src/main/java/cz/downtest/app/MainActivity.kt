   
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

        page.addView(
            scanButton,
            LinearLayout.LayoutParams(-1, dp(65)).apply {
                topMargin = dp(20)
                bottomMargin = dp(8)
            }
        )

        page.addView(
            questionsButton,
            LinearLayout.LayoutParams(-1, dp(65)).apply {
                topMargin = dp(8)
                bottomMargin = dp(15)
            }
        )

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
            label(
                question.title,
                22f,
                white,
                true
            ).apply {
                setPadding(dp(5), dp(25), dp(5), dp(25))
            },
            params()
        )

        question.answers.forEachIndexed { index, answer ->
            val button = makeButton(answer.first)

            if (selectedAnswers[questionIndex] == index) {
                button.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(purple)
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

        val backButton = makeButton("‹ ZPĚT")
        val nextButton = makeButton(
            if (questionIndex == randomizedQuestions.lastIndex) {
                "VYHODNOTIT TEST"
            } else {
                "DALŠÍ ➜"
            }
        )

        page.addView(
            backButton,
            LinearLayout.LayoutParams(-1, dp(55)).apply {
                topMargin = dp(20)
            }
        )

        page.addView(
            nextButton,
            LinearLayout.LayoutParams(-1, dp(55)).apply {
                topMargin = dp(8)
            }
        )

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


    private fun showDownResult(score: Int) {
        val page = root()

        if (score > 100) {
            history.add("DOWN TEST: ERROR")

            page.addView(
                label("ERROR", 34f, red, true),
                params()
            )

            page.addView(
                label(
                    "APLIKACI SI ROZBIL, PROTOŽE JSEŠ AŽ TAK VELKEJ DOWN 💀",
                    23f,
                    white,
                    true
                ).apply {
                    setPadding(dp(5), dp(35), dp(5), dp(35))
                },
                params()
            )
        } else {
            history.add("DOWN TEST: $score %")

            page.addView(
                label("💀 DOWN TEST", 30f, purple, true),
                params()
            )

            page.addView(
                label("$score %", 42f, gold, true).apply {
                    setPadding(0, dp(30), 0, dp(20))
                },
                params()
            )

            val description = when {
                score >= 75 -> "Tohle je pořádný DOWN! 💀"
                score >= 50 -> "Docela slušný DOWN!"
                score >= 25 -> "Trochu DOWN."
                else -> "Tvoje DOWN úroveň je nízká."
            }

            page.addView(
                label(description, 20f, white, true),
                params()
            )
        }

        val againButton = makeButton("🔄 ZKUSIT ZNOVU")
        val menuButton = makeButton("🏠 HLAVNÍ MENU")

        page.addView(
            againButton,
            LinearLayout.LayoutParams(-1, dp(58)).apply {
                topMargin = dp(30)
            }
        )
        page.addView(menuButton, params())

        againButton.setOnClickListener {
            startQuestions("DOWN TEST")
        }

        menuButton.setOnClickListener {
            showMenu()
        }

        setContentView(page)
    }

    private fun showModeScreen(name: String) {
        currentMode = name

        val page = root()

        page.addView(
            label(name, 30f, purple, true),
            params()
        )

        page.addView(
            label("Vyber, co chceš udělat.", 18f, white),
            params()
        )

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

        historyButton.setOnClickListener {
            showHistory()
        }

        backButton.setOnClickListener {
            showMenu()
        }

        setContentView(page)
    }

    private fun showHistory() {
        val page = root()

        page.addView(
            label("📜 HISTORIE", 30f, purple, true),
            params()
        )

        if (history.isEmpty()) {
            page.addView(
                label("Zatím tu nemáš žádné výsledky.", 17f, white),
                params()
            )
        } else {
            history.asReversed().forEachIndexed { index, item ->
                page.addView(
                    label("${index + 1}. $item", 17f, white).apply {
                        setPadding(0, dp(8), 0, dp(8))
                    },
                    params()
                )
            }
        }

        val clearButton = makeButton("🗑 SMAZAT HISTORII")
        val backButton = makeButton("⬅ ZPĚT")

        page.addView(
            clearButton,
            LinearLayout.LayoutParams(-1, dp(55)).apply {
                topMargin = dp(20)
            }
        )
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
            if (currentMode == "DOWN TEST" || currentMode == "AURA TEST") {
                showTestChoice(currentMode)
            } else {
                showModeScreen(currentMode)
            }
        }

        setContentView(page)
    }


    private fun showCameraScreen() {
        val page = root()

        page.addView(
            label("📷 $currentMode", 27f, purple, true),
            params()
        )

        page.addView(
            label("Připrav se na skenování!", 17f, white),
            params()
        )

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
            } catch (e: Exception) {
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
        history.add("$currentMode SCAN: $score")

        val page = root()

        page.addView(
            label("📊 VÝSLEDEK SCANU", 28f, purple, true),
            params()
        )

        val resultText = if (currentMode == "AURA TEST") {
            "$score / 1000"
        } else {
            "$score %"
        }

        page.addView(
            label(resultText, 40f, gold, true).apply {
                setPadding(0, dp(35), 0, dp(25))
            },
            params()
        )

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

        page.addView(
            label(description, 20f, white, true),
            params()
        )

        val againButton = makeButton("🔄 ZKUSIT ZNOVU")
        val menuButton = makeButton("🏠 HLAVNÍ MENU")

        page.addView(
            againButton,
            LinearLayout.LayoutParams(-1, dp(58)).apply {
                topMargin = dp(30)
            }
        )
        page.addView(menuButton, params())

        againButton.setOnClickListener {
            showCameraScreen()
        }

        menuButton.setOnClickListener {
            showMenu()
        }

        setContentView(page)
    }

    override fun onDestroy() {
        stopCamera()
        super.onDestroy()
    }
}


