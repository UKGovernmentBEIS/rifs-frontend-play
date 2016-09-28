package controllers

import play.api.mvc.{Action, Controller}

class StartPageController extends Controller {

  def startPage = Action {
    Ok(views.html.startPage())
  }

}
