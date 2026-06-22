package com.ai.studyassistant.data.local

import android.content.Context
import com.ai.studyassistant.data.model.Question
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads the bundled offline question bank (assets/questions.json), parsed once and cached in memory. */
object LocalQuestionBank {
    @Volatile private var cache: Map<String, List<Question>>? = null

    suspend fun questionsFor(context: Context, bankKey: String): List<Question> {
        val bank = cache ?: load(context).also { cache = it }
        return bank[bankKey].orEmpty()
    }

    private suspend fun load(context: Context): Map<String, List<Question>> = withContext(Dispatchers.IO) {
        val json = context.assets.open("questions.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<Map<String, List<Question>>>() {}.type
        Gson().fromJson(json, type)
    }
}
