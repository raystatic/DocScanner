package com.easyscan.docscanner.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

class Converters {

    @TypeConverter
    fun fromStringToFile(value: String?): File? {
        val type: Type = object : TypeToken<File?>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromFileToString(file: File?): String? {
        val gson = Gson()
        return gson.toJson(file)
    }

}