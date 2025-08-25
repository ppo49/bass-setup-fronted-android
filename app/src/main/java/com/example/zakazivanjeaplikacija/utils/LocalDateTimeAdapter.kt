package com.example.zakazivanjeaplikacija.utils

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Custom Gson TypeAdapter za serijalizaciju i deserijalizaciju LocalDateTime.
 * Koristi ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss).
 */

//nmp sa neta ne diraj jer radi ovako

class LocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {


    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME


    override fun serialize(
        src: LocalDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return if (src == null) JsonNull.INSTANCE else JsonPrimitive(src.format(formatter))
    }


    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDateTime {
        if (json == null || json.isJsonNull) {
            return LocalDateTime.now()
        }
        try {
            return LocalDateTime.parse(json.asString, formatter)
        } catch (e: DateTimeParseException) {
            throw JsonParseException("Ne mogu parsirati datum i vreme: " + json.asString, e)
        }
    }
}
