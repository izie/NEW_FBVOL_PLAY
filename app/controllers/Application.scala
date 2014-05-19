package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import models._

import akka.actor._
import scala.concurrent.duration._

object Application extends Controller {

  /**
   * Just display the home page.
   */
  def index = Action { implicit request =>
    Users.updateUsers
    Ok(views.html.index())
  }

  /**
   * Display the chat room page.
   */
  def chatRoom(token: Option[String]) = Action { implicit request =>
    token.filterNot(_.isEmpty).map { username =>
      val user:User = Users.getUserByToken(token.getOrElse(""))
      val rooms:Seq[Room] = Rooms.getRooms
      Ok(views.html.chatRoom(user,rooms))
    }.getOrElse {
      Redirect(routes.Application.index).flashing(
        "error" -> "Please choose a valid username."
      )
    }
  }

  def chatRoomJs(token: String) = Action { implicit request =>
    val user:User = Users.getUserByToken(token)
    Ok(views.js.chatRoom(user))
  }

  /**
   * Handles the chat websocket.
   */
  def chat(token: String) = WebSocket.async[JsValue] { request  =>

    ChatRoom.join(token)

  }

  /**
   * Display the chat room page.
   */
  def gameRoom(token: String, seq_room:Long) = Action { implicit request =>
      val user:User = Users.getUserByToken(token)
      val room:Room = Rooms.getRoom(seq_room)

      Ok(views.html.gameRoom(user,room))
  }

  def gameRoomJS(token: String, seq_room:Long) = Action { implicit request =>
    val user:User = Users.getUserByToken(token)
    val room:Room = Rooms.getRoom(seq_room)
    Ok(views.js.gameRoom(user,room))
  }

  /**
   * Handles the chat websocket.
   */
  def game(token: String, seq_room:Long) = WebSocket.async[JsValue] { request  =>
    println("GAme - Chat")
    GameRoom.join(token,seq_room)

  }

  def setUserXY(token:String, x:Int, y:Int) = Action { implicit request =>
    println("GAme - Chat")
    GameRoom.move(token,x,y)
    Ok("")
  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        User.addUser,
        User.getUserProfile,
        User.setUserCurrentRoom,
        User.setUserStatusByToken,
        Room.getRooms,
        Room.getPlayersProfile

      )
    ).as("text/javascript")
  }

}
