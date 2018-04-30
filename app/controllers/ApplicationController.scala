package controllers

import javax.inject._

import play.api.mvc._
import play.api.libs.json._

@Singleton
class ApplicationController @Inject()(components: ControllerComponents) extends AbstractController(components) {

  def ping: Action[AnyContent] = Action {
    Ok("pong")
  }

}
