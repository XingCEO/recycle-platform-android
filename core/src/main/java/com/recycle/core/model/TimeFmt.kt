package com.recycle.core.model

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val TAIPEI = ZoneId.of("Asia/Taipei")
private val FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

fun formatTaipei(iso: String?): String {
    if (iso == null) return "—"
    return try {
        val odt = try {
            OffsetDateTime.parse(iso)
        } catch (e: DateTimeParseException) {
            Instant.parse(iso).atZone(TAIPEI).toOffsetDateTime()
        }
        odt.atZoneSameInstant(TAIPEI).format(FMT)
    } catch (e: Exception) {
        iso.take(16).replace("T", " ")
    }
}
