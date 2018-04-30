package controllers

import org.scalatestplus.play._
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test._

class ApplicationControllerTest extends PlaySpec with Results {

  "ping" should {
    "reply pong" in {
      // Given
      val controller = new ApplicationController(stubControllerComponents())

      // When
      val result = controller.ping()(FakeRequest())

      // Then
      status(result) mustBe OK
      contentType(result) mustBe Some("text/plain")
      contentAsString(result) mustBe "pong"
    }
  }

}
