package com.ai.studyassistant.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ai.studyassistant.data.model.Question

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(list: List<String>): String = gson.toJson(list)

    @TypeConverter
    fun toStringList(json: String): List<String> =
        gson.fromJson(json, Array<String>::class.java).toList()

    @TypeConverter
    fun fromIntList(list: List<Int>): String = gson.toJson(list)

    @TypeConverter
    fun toIntList(json: String): List<Int> =
        gson.fromJson(json, object : TypeToken<List<Int>>() {}.type)

    @TypeConverter
    fun fromQuestionList(list: List<Question>): String = gson.toJson(list)

    @TypeConverter
    fun toQuestionList(json: String): List<Question> =
        gson.fromJson(json, object : TypeToken<List<Question>>() {}.type)
}
