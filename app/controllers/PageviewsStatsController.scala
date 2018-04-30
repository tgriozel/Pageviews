package controllers

import javax.inject._

import model.{PageviewBody, PageviewsStats}
import play.api.libs.json.Json
import play.api.Logger
import play.api.mvc._
import services.PageviewsStatsService

import scala.concurrent.ExecutionContext

@Singleton
class PageviewsStatsController @Inject()(components: ControllerComponents,
                                         service: PageviewsStatsService)
                                        (implicit ec: ExecutionContext)
  extends AbstractController(components) {

  private implicit val writes = Json.format[PageviewsStats]
  private implicit val read = Json.reads[PageviewBody]

  def recordPageview(): Action[PageviewBody] = Action.async(parse.json[PageviewBody]) { request =>
    val PageviewBody(userId, pageName, dateTime) = request.body
    service.persistPageview(userId, pageName, dateTime)
      .map { _ =>
        Ok
      }
      .recover { case e =>
        Logger.error("An error occurred while persisting pageview", e)
        InternalServerError
      }
  }

  def getUserStats(userId: String, daysCount: Int): Action[AnyContent] = Action.async {
    service.retrievePageviewsStats(userId, daysCount)
      .map { stats: PageviewsStats =>
        Ok(Json.toJson(stats))
      }
      .recover { case e =>
        Logger.error("An error occurred while getting user stats", e)
        InternalServerError
      }
  }

}
