package models

import akka.actor._
import scala.concurrent.duration._
import scala.language.postfixOps

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
/*
object Robot {
  
  def apply(chatRoom: ActorRef) {
    
    // Create an Iteratee that logs all messages to the console.
    val loggerIteratee = Iteratee.foreach[JsValue](event => Logger("robot").info(event.toString))
    
    implicit val timeout = Timeout(1 second)
    // Make the robot join the room
    chatRoom ? (Join("Robot")) map {
      case Connected(robotChannel) => 
        // Apply this Enumerator on the logger.
        robotChannel |>> loggerIteratee
    }
    
    // Make the robot talk every 30 seconds
    Akka.system.scheduler.schedule(
      30 seconds,
      30 seconds,
      chatRoom,
      Talk("Robot", "I'm still alive")
    )
  }
  
}
*/
object GameRoom {

  implicit val timeout = Timeout(1 second)

  lazy val default = {
    val roomActor = Akka.system.actorOf(Props[GameRoom])

    // Create a bot user (just for fun)
    //Robot(roomActor)

    roomActor
  }

  def join(token:String, seq_room:Long):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? JoinGame(token,seq_room)).map {

      case Connected(enumerator) =>

        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          default ! TalkGame(token, (event \ "text").as[String])
        }.map { _ =>
          default ! QuitGame(token)
        }

        (iteratee,enumerator)

      case CannotConnect(error) =>

        // Connection error

        // A finished Iteratee sending EOF
        val iteratee = Done[JsValue,Unit]((),Input.EOF)

        // Send an error and close the socket
        val enumerator =  Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))

        (iteratee,enumerator)

    }

  }

  def move(token:String, x:Int, y:Int):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? MoveGame(token,x,y)).map {

      case Connected(enumerator) =>

        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          default ! TalkGame(token, (event \ "text").as[String])
        }.map { _ =>
          default ! QuitGame(token)
        }

        (iteratee,enumerator)

      case CannotConnect(error) =>

        // Connection error

        // A finished Iteratee sending EOF
        val iteratee = Done[JsValue,Unit]((),Input.EOF)

        // Send an error and close the socket
        val enumerator =  Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))

        (iteratee,enumerator)

    }

  }

}

class GameRoom extends Actor {

  var members = Set.empty[String]
  val (gameEnumerator, gameChannel) = Concurrent.broadcast[JsValue]

  def receive = {

    case MoveGame(token,x,y) => {
      Users.setUserPosition(x,y,token)
      notifyAll_Move("move", token, "has entered the room", x,y)
    }

    case JoinGame(token,seq_room) => {

      if(members.contains(token)) {
        sender ! CannotConnect("This username is already used")
      } else {
        members = members + token
        sender ! Connected(gameEnumerator)
        self ! NotifyJoinGame(token,seq_room)
      }
    }

    case NotifyJoinGame(token,seq_room) => {
      Users.setUserCurrentRoom(token,seq_room)
      notifyAll("join", token, "has entered the room", seq_room)
    }

    case TalkGame(token, text) => {
      notifyAll("talk", token, text)
    }

    case QuitGame(token) => {
      members = members - token
      notifyAll("quit", token, "has left the room")
    }

  }

  def notifyAll(action:String, token:String, msg:String, seq_room:Long=0) {
    val user:User = Users.getUserByToken(token)
    val room:Room = Rooms.getRoom(seq_room)

    var user_type:Int = 3
    if(room.owner.token == user.token)  user_type = 0
    else                                user_type = 1

    val msg2 = JsObject(
      Seq(
        "action" -> JsString(action),
        "msg" -> JsString(msg),
        "seq" -> JsNumber(user.seq.get),
        "name" -> JsString(user.name),
        "token" -> JsString(user.token),
        "email" -> JsString(user.email),
        "pic_url" -> JsString(user.pic_url),
        "win" -> JsNumber(user.win),
        "lose" -> JsNumber(user.lose),
        "total" -> JsNumber(user.total),
        "x" -> JsNumber(user.location_x),
        "y" -> JsNumber(user.location_y),
        "user_type" -> JsNumber(user_type),
        "members" -> JsArray(
          members.toList.map(JsString)
        )
      )
    )
    gameChannel.push(msg2)
  }

  def notifyAll_Move(action:String, token:String, msg:String, x:Int, y:Int) {
    val user:User = Users.getUserByToken(token)


    val msg2 = JsObject(
      Seq(
        "action" -> JsString(action),
        "msg" -> JsString(msg),
        "seq" -> JsNumber(user.seq.get),
        "name" -> JsString(user.name),
        "token" -> JsString(user.token),
        "email" -> JsString(user.email),
        "pic_url" -> JsString(user.pic_url),
        "win" -> JsNumber(user.win),
        "lose" -> JsNumber(user.lose),
        "total" -> JsNumber(user.total),
        "x" -> JsNumber(user.location_x),
        "y" -> JsNumber(user.location_y),
        "current_room_seq" -> JsNumber(user.seq_current_room),
        "members" -> JsArray(
          members.toList.map(JsString)
        )
      )
    )
    gameChannel.push(msg2)
  }

}

case class MoveGame(token:String,x:Int,y:Int)
case class JoinGame(token:String,seq_room:Long)
case class QuitGame(username: String)
case class TalkGame(username: String, text: String)
case class NotifyJoinGame(token: String, seq_room:Long)

//case class Connected(enumerator:Enumerator[JsValue])
//case class CannotConnect(msg: String)
