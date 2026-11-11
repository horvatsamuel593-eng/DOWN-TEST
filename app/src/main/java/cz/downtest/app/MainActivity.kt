   
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

