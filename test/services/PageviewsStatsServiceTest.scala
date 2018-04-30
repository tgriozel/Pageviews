package services

import model.{PageviewEntry, PageviewsStats}
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.AsyncFlatSpec
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar
import repositories.PageviewsStatsRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PageviewsStatsServiceTest extends AsyncFlatSpec with MockitoSugar {

  behavior of "persistPageview"

  it should "ask for the repository to persist the entry" in {
    // Given
    val expectedEntry = PageviewEntry("tom", "home", 1525455000000l)
    val repository = mock[PageviewsStatsRepository]
    val service = new PageviewsStatsService(repository)
    when(repository.persistPageview(expectedEntry)).thenReturn(Future.successful(expectedEntry))

    // When
    service.persistPageview("tom", "home", "2018-05-04T17:30:00.000Z").map { result =>
      // Then
      result should be(expectedEntry)
    }
  }

  behavior of "retrievePageviewsStats"

  it should "ask for the correct set of pageviews and compute stats through its internal helpers" in {
    // Given
    val pageviews = Seq(
      PageviewEntry("tom", "home", 1525455000000l),
      PageviewEntry("tom", "engineering", 1525456000000l),
      PageviewEntry("tom", "engineering", 1525456000000l)
    )
    val repository = mock[PageviewsStatsRepository]
    val service = spy(new PageviewsStatsService(repository))
    when(repository.getSortedPageviews(eqTo("tom"), any[Long])).thenReturn(Future.successful(pageviews))
    when(service.totalViewCount(pageviews)).thenReturn(3)
    when(service.activeDaysCount(pageviews)).thenReturn(1)
    when(service.activeTimeInSeconds(pageviews)).thenReturn(360)
    when(service.mostViewedPage(pageviews)).thenReturn(Some("engineering"))

    // When
    service.retrievePageviewsStats("tom", 7).map { result =>
      // Then
      verify(repository).getSortedPageviews(eqTo("tom"), any[Long])
      result should be(PageviewsStats("tom", 7, 3, 1, 360, "engineering"))
    }
  }

  it should "not encounter any problem in the absence of pageviews" in {
    // Given
    val pageviews = Seq.empty[PageviewEntry]
    val repository = mock[PageviewsStatsRepository]
    val service = spy(new PageviewsStatsService(repository))
    when(repository.getSortedPageviews(eqTo("tom"), any[Long])).thenReturn(Future.successful(pageviews))
    when(service.totalViewCount(pageviews)).thenReturn(0)
    when(service.activeDaysCount(pageviews)).thenReturn(0)
    when(service.activeTimeInSeconds(pageviews)).thenReturn(0)
    when(service.mostViewedPage(pageviews)).thenReturn(None)

    // When
    service.retrievePageviewsStats("tom", 7).map { result =>
      // Then
      verify(repository).getSortedPageviews(eqTo("tom"), any[Long])
      result should be(PageviewsStats("tom", 7, 0, 0, 0, ""))
    }
  }

  behavior of "totalViewCount"

  it should "return the total count of pageviews" in {
    // Given
    val pageviews = Seq(
      PageviewEntry("tom", "engineering", 1525455000000l),
      PageviewEntry("tom", "about", 1525473000000l)
    )
    val service = new PageviewsStatsService(mock[PageviewsStatsRepository])

    // When
    val result = service.totalViewCount(pageviews)

    // Then
    result should be(2)
  }

  behavior of "activeDaysCount"

  it should "return the number of days where at least one click happened" in {
    // Given
    val pageviews = Seq(
      PageviewEntry("tom", "engineering", 1125455550000l),
      PageviewEntry("tom", "engineering", 1225455550000l),
      PageviewEntry("tom", "engineering", 1325455550000l),
      PageviewEntry("tom", "engineering", 1425455550000l),
      PageviewEntry("tom", "engineering", 1525455551000l),
      PageviewEntry("tom", "engineering", 1525455552000l),
      PageviewEntry("tom", "engineering", 1525455553000l),
      PageviewEntry("tom", "engineering", 1525455554000l)
    )
    val service = new PageviewsStatsService(mock[PageviewsStatsRepository])

    // When
    val result = service.activeDaysCount(pageviews)

    // Then
    result should be(5)
  }

  it should "return 0 if there is no pageview" in {
    // Given
    val pageviews = Seq.empty[PageviewEntry]
    val service = new PageviewsStatsService(mock[PageviewsStatsRepository])

    // When
    val result = service.activeDaysCount(pageviews)

    // Then
    result should be(0)
  }

  behavior of "mostViewedPage"

  it should "return the most viewed page among all pages" in {
    // Given
    val pageviews = Seq(
      PageviewEntry("tom", "home", 1525453200000l),
      PageviewEntry("tom", "engineering", 1525455000000l),
      PageviewEntry("tom", "engineering", 1525455060000l),
      PageviewEntry("tom", "about", 1525455090000l),
      PageviewEntry("tom", "engineering", 1525455300000l),
      PageviewEntry("tom", "about", 1525473000000l),
      PageviewEntry("tom", "sales", 1525473120000l),
      PageviewEntry("tom", "sales", 1525491000000l)
    )
    val service = new PageviewsStatsService(mock[PageviewsStatsRepository])

    // When
    val result = service.mostViewedPage(pageviews)

    // Then
    result should be(Some("engineering"))
  }

  it should "return the most recent of the most viewed pages in case of a tie" in {
    // Given
    val pageviews = Seq(
      PageviewEntry("tom", "engineering", 1525455000000l),
      PageviewEntry("tom", "engineering", 1525455060000l),
      PageviewEntry("tom", "about", 1525455090000l),
      PageviewEntry("tom", "about", 1525473000000l)
    )
    val service = new PageviewsStatsService(mock[PageviewsStatsRepository])

    // When
    val result = service.mostViewedPage(pageviews)

    // Then
    result should be(Some("about"))
  }

  it should "return None if there is no pageview" in {
    // Given
    val pageviews = Seq.empty[PageviewEntry]
    val service = new PageviewsStatsService(mock[PageviewsStatsRepository])

    // When
    val result = service.mostViewedPage(pageviews)

    // Then
    result should be(None)
  }

  behavior of "activeTimeInSeconds"

  it should "return the total active time corresponding to the pageviews" in {
    // Given
    val pageviews = Seq(
      PageviewEntry("tom", "engineering", 1525453200000l), // 5:00:00 pm
      PageviewEntry("tom", "engineering", 1525455000000l), // 5:30:00 pm
      PageviewEntry("tom", "engineering", 1525455060000l), // 5:31:00 pm
      PageviewEntry("tom", "engineering", 1525455090000l), // 5:31:30 pm
      PageviewEntry("tom", "engineering", 1525455300000l), // 5:35:00 pm
      PageviewEntry("tom", "engineering", 1525473000000l), // 6:00:00 pm
      PageviewEntry("tom", "engineering", 1525473120000l), // 6:02:00 pm
      PageviewEntry("tom", "engineering", 1525491000000l)  // 7:00:00 pm
    )
    val service = new PageviewsStatsService(mock[PageviewsStatsRepository])

    // When
    val activeTimeInSeconds = service.activeTimeInSeconds(pageviews)

    // Then
    activeTimeInSeconds should be(120 + 210 + 120 + 240 + 120)
  }

  it should "compute activity time correctly for one pageview" in {
    // Given
    val pageviews = Seq(PageviewEntry("tom", "engineering", 1525453200000l)) // 5:00:00 pm
    val service = new PageviewsStatsService(mock[PageviewsStatsRepository])

    // When
    val activeTimeInSeconds = service.activeTimeInSeconds(pageviews)

    // Then
    activeTimeInSeconds should be(120)
  }

  it should "compute activity time correctly for no pageview" in {
    // Given
    val pageviews = Seq.empty[PageviewEntry]
    val service = new PageviewsStatsService(mock[PageviewsStatsRepository])

    // When
    val activeTimeInSeconds = service.activeTimeInSeconds(pageviews)

    // Then
    activeTimeInSeconds should be(0)
  }

}
