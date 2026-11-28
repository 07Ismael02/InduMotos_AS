package pe.indumotos.mobile.domain

import kotlin.time.Instant

// Lima no aplica horario de verano: siempre UTC-5.
private const val LIMA_OFFSET_SECONDS = -5 * 3600L
private val zoned = Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}.*(Z|[+-]\d{2}:\d{2})""")
private val local = Regex("""(\d{4})-(\d{2})-(\d{2})(?:T(\d{2}):(\d{2}).*)?""")

/**
 * Muestra una fecha del servidor como «dd/mm/aaaa hh:mm» en hora de Lima.
 * Las fechas con zona se convierten; las que no la tienen solo se reordenan.
 * Si el texto no es una fecha reconocible se devuelve sin cambios.
 */
fun limaTimestamp(value: String): String {
    if (zoned.matches(value)) {
        val seconds = runCatching { Instant.parse(value).epochSeconds }.getOrNull() ?: return value
        val limaSeconds = seconds + LIMA_OFFSET_SECONDS
        val (year, month, day) = civilDate(limaSeconds.floorDiv(86400L))
        val minutes = limaSeconds.mod(86400L) / 60
        return "${two(day)}/${two(month)}/$year ${two(minutes / 60)}:${two(minutes % 60)}"
    }
    val parts = local.matchEntire(value) ?: return value
    val (year, month, day, hour, minute) = parts.destructured
    val date = "$day/$month/$year"
    return if (hour.isEmpty()) date else "$date $hour:$minute"
}

private fun two(value: Long) = value.toString().padStart(2, '0')

// Conversión de días desde 1970-01-01 a fecha civil (algoritmo de Howard Hinnant).
private fun civilDate(days: Long): Triple<Long, Long, Long> {
    val z = days + 719468
    val era = z.floorDiv(146097L)
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val day = doy - (153 * mp + 2) / 5 + 1
    val month = if (mp < 10) mp + 3 else mp - 9
    val year = yoe + era * 400 + if (month <= 2) 1 else 0
    return Triple(year, month, day)
}
