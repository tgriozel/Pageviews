package controllers

import model.{PageviewBody, PageviewEntry, PageviewsStats}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test._
import services.PageviewsStatsService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PageviewsStatsControllerTest extends PlaySpec with Results with MockitoSugar {

  "recordPageview" should {
    "call the service to persist a PageviewEntry" in {
      // Given
      val (userId, pageName, now) = ("tom", "home", "2018-05-03T22:20:27.000Z")
      val entry = PageviewEntry(userId, pageName, 1525386027000l)
      val request = FakeRequest().withBody(PageviewBody(userId, pageName, now))
      val service = mock[PageviewsStatsService]
      when(service.persistPageview(any[String], any[String], any[String])).thenReturn(Future.successful(entry))
      val controller = new PageviewsStatsController(stubControllerComponents(), service)

      // When
      val result = controller.recordPageview().apply(request)

      // Then
      status(result) mustBe OK
      verify(service).persistPageview(userId, pageName, now)
    }
  }

  "getUserStats" should {
    "return the content of the PageviewsStats returned by the service" in {
      // Given
      val service = mock[PageviewsStatsService]
      val stats = PageviewsStats("tom", 7, 10, 2, 120, "sales")
      when(service.retrievePageviewsStats(any[String], any[Int])).thenReturn(Future.successful(stats))
      val controller = new PageviewsStatsController(stubControllerComponents(), service)

      // When
      val result = controller.getUserStats("tom", 7).apply(FakeRequest())

      // Then
      contentType(result) mustBe Some("application/json")
      contentAsString(result) mustBe """{"userId":"tom","daysCount":7,"numberPagesViewed":10,"numberOfDaysActive":2,"secondsSpentOnSite":120,"mostViewedPage":"sales"}"""
    }
  }

}
