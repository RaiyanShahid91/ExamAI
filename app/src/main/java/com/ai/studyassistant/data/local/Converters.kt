package com.ai.studyassistant.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(list: List<String>): String = gson.toJson(list)

    @TypeConverter
    fun toStringList(json: String): List<String> =
        gson.fromJson(json, Array<String>::class.java).toList()
}
