package controllers

import play.api._
import play.api.mvc._
import models.{Room, Rooms, Users, User}
import play.api.libs.json._
import scala.util.parsing.json.JSONArray

object User extends Controller {

  def addUser(name:String,  email:String, pic_url:String, token:String) = Action{
    val code:Int = Users.addUser(name, email, pic_url, token)

    val json:JsValue = JsObject(
      Seq("code" -> JsNumber(code))
    )
    Ok(json).withSession(
      "USER_NAME" -> name,
      "USER_EMAIL" -> email,
      "PIC_URL" -> pic_url,
      "USER_TOKEN" -> token
    )
  }

  def getUserProfile(token:String) = Action{
    Ok(views.html.profile(Users.getUserByToken(token)))
  }

  def getUsersProfile(token:String) = Action{
    Ok(views.html.profile(Users.getUserByToken(token)))
  }


  def getUser(token:String) = Action{
    val user:User = Users.getUserByToken(token)

    val json:JsValue = JsObject(
      Seq(
        "id" -> JsString(user.name),
        "x" -> JsNumber(user.location_x),
        "y" -> JsNumber(user.location_y)
      )
    )
    Ok(json)
  }

  def setUserStatusByToken(status:Int, token:String) = Action{
    Users.setUserStatusByToken(token,status)

    var msg:String = "ok"

    val json:JsValue = JsObject(
      Seq(
        "msg" -> JsString(msg)
      )
    )
    Ok(json)
  }

  def setUserCurrentRoom(seq_room:Int, token:String) = Action{
    Users.setUserCurrentRoom(token,seq_room)

    var msg:String = "ok"

    val json:JsValue = JsObject(
      Seq(
        "msg" -> JsString(msg)
      )
    )
    Ok(json)
  }

  def setUserPosition(x:Int, y:Int, token:String) = Action{
    var user:User = Users.getUserByToken(token)
    var msg:String = "ok"

    if(user.location_x != x || user.location_y != y){
      Users.setUserPosition(x,y,token)
      msg = "Ok"
    }else{
      msg = "None"
    }

    val json:JsValue = JsObject(
      Seq(
        "msg" -> JsString(msg)
      )
    )
    Ok(json)
  }

}