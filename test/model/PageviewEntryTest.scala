package model

import java.time.ZonedDateTime

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class PageviewEntryTest extends FlatSpec {

  behavior of "constructor"

  it should "build the right dateEpochMillis for the given epochMillis parameter" in {
    // Given
    val epochMillis1 = ZonedDateTime.parse("1989-08-03T12:00:00.000Z").toInstant.toEpochMilli
    val epochMillis2 = ZonedDateTime.parse("1998-07-12T00:00:00.000Z").toInstant.toEpochMilli

    // When
    val pageviewEntry1 = PageviewEntry("tom", "home", epochMillis1)
    val pageviewEntry2 = PageviewEntry("tom", "home", epochMillis2)

    // Then
    val expectedDateMillis1 = ZonedDateTime.parse("1989-08-03T00:00:00.000Z").toInstant.toEpochMilli
    val expectedDateMillis2 = ZonedDateTime.parse("1998-07-12T00:00:00.000Z").toInstant.toEpochMilli
    pageviewEntry1.dateEpochMillis should be(expectedDateMillis1)
    pageviewEntry2.dateEpochMillis should be(expectedDateMillis2)
  }

}
