package controllers

import play.api._
import play.api.mvc._
import models.{Rooms, Room, Users, User}
import play.api.libs.json._
import scala.util.parsing.json.JSONArray

object Room extends Controller {


  def getRooms() = Action{
    val rooms:Seq[Room] = Rooms.getRooms
    println(rooms.toString())
    Ok(views.html.roomlist(rooms))
  }

  def getPlayersProfile(seq_room:Int, token:String) = Action{
    val room:Room = Rooms.getRoom(seq_room)
    val user:User = Rooms.getUsers(seq_room)

    Ok(views.html.playerProfile(room.owner,user,token))
  }

  def addRoom(title:String,  time:Int, token:String) = Action {
    val user:User = Users.getUserByToken(token)
    val seq_room:Long = Rooms.addRoom(title, time, token)

    val json:JsValue = JsObject(
      Seq(
        "seq" -> JsNumber(seq_room)
      )
    )
    Ok(json)

  }

  def delRoom(seq_room:Int) = Action {

    Rooms.removeRoom(seq_room)
    val json:JsValue = JsObject(
      Seq(
        "code" -> JsNumber(100)
      )
    )
    Ok(json)

  }



}