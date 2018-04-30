package model

import java.time.Instant
import java.time.temporal.ChronoUnit

case class PageviewEntry(userId: String,
                         pageName: String,
                         epochMillis: Long) {

  val dateEpochMillis: Long = Instant.ofEpochMilli(epochMillis).truncatedTo(ChronoUnit.DAYS).toEpochMilli

}
