package controllers

import play.api._
import play.api.mvc._
import models.{Users, User}

object Game extends Controller {

  def index = Action {
    //Ok(views.html.index("Your new application is ready."))

    Ok(views.html.game(""))


  }

}