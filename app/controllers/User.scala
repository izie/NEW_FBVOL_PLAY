package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.libs.json._
import scala.util.parsing.json.JSONArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber

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

  def setUserXY(token:String, x:Int, y:Int) = Action { implicit request =>
    println("GAme - Chat / x : "+x+"y : "+y)


    var tx:Int = x
    var ty:Int = y
    var msg:String = "ok"

    if(0 != x || 0 != y){
      var user:User = Users.getUserByToken(token)
      //tx += user.location_x
      //ty += user.location_y
      //if(tx < 0)  tx = 0
      //if(ty < 0)  ty = 0

      //if(tx >= 800) tx = 799
      //if(ty >= 600) ty = 599

      //Users.setUserPosition(tx,ty,token)

      msg = "Ok"
      GameRoom.move(user,tx,ty)
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

  def JumpUser(token:String) = Action { implicit request =>
    println("GAme - JumpUser")

    var user:User = Users.getUserByToken(token)
    var msg:String = "ok"

    GameRoom.jump(user,user.seq_current_room)


    val json:JsValue = JsObject(
      Seq(
        "msg" -> JsString(msg)
      )
    )
    Ok(json)
  }


  def ShootUser(token:String) = Action { implicit request =>
    println("GAme - ShootUser")

    var user:User = Users.getUserByToken(token)
    var msg:String = "ok"

    GameRoom.shoot(user,user.seq_current_room)


    val json:JsValue = JsObject(
      Seq(
        "msg" -> JsString(msg)
      )
    )
    Ok(json)
  }

}