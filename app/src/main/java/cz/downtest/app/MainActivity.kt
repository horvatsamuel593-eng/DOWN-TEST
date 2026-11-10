   
private fun showQuestions() {
    stopCamera()

    val selected = mutableMapOf<Int, Int>()

    lateinit var renderQuestion: (Int) -> Unit

    renderQuestion = { questionIndex ->

        val page = root()
        page.setPadding(dp(22), dp(30), dp(22), dp(25))
        setContentView(page)

        val question = questions[questionIndex]

        page.addView(
            label("💀 DOWN TEST", 24f, white, true).apply {
                gravity = Gravity.CENTER
            },
            params()
        )

        page.addView(
            label("OTÁZKA ${questionIndex + 1} / ${questions.size}", 15f, purple, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(18), 0, dp(25))
            },
            params()
        )

        page.addView(
            label(question.title, 25f, white, true).apply {
                gravity = Gravity.CENTER
                setPadding(dp(5), dp(20), dp(5), dp(30))
            },
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        val answerButtons = mutableListOf<Button>()

        question.answers.forEachIndexed { answerIndex, answer ->

            val answerButton = makeButton(answer.first)

            answerButton.textSize = 17f
            answerButton.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    if (selected[questionIndex] == answerIndex) purple else panel
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

        val next = makeButton(
            if (questionIndex == questions.lastIndex)
                "VYHODNOTIT TEST"
            else
                "DALŠÍ OTÁZKA  →"
        )

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
            if (!selected.containsKey(questionIndex)) return@setOnClickListener

            if (questionIndex < questions.lastIndex) {
                renderQuestion(questionIndex + 1)
            } else {
                val score = questions.indices.sumOf { index ->
                    questions[index].answers[selected[index]!!].second
                }

                showDownResult(score)
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
