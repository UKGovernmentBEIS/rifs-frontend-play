package controllers

import play.api.mvc.{Action, Controller}

class HealthCheckController extends Controller {
  def ping = Action {
    Ok("alive")
  }
}
