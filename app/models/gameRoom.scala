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

  def join(user:User, seq_room:Long):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? JoinGame(user,seq_room)).map {

      case Connected(enumerator) =>

        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          default ! TalkGame(user, (event \ "text").as[String])
        }.map { _ =>
          default ! QuitGame(user)
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

  def ready(user:User, seq_room:Long):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? ReadyGame(user,seq_room)).map {

      case Connected(enumerator) =>

        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          default ! TalkGame(user, (event \ "text").as[String])
        }.map { _ =>
          default ! QuitGame(user)
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

  def move(user:User, x:Int, y:Int):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? MoveGame(user,x,y)).map {

      case Connected(enumerator) =>

        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          default ! TalkGame(user, (event \ "text").as[String])
        }.map { _ =>
          default ! QuitGame(user)
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

  def jump(user:User,seq_room:Long):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? JumpGame(user,seq_room)).map {

      case Connected(enumerator) =>

        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          default ! TalkGame(user, (event \ "text").as[String])
        }.map { _ =>
          default ! QuitGame(user)
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

  def shoot(user:User,seq_room:Long):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? ShootGame(user,seq_room)).map {

      case Connected(enumerator) =>

        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          default ! TalkGame(user, (event \ "text").as[String])
        }.map { _ =>
          default ! QuitGame(user)
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

  var members = Set.empty[User]
  val (gameEnumerator, gameChannel) = Concurrent.broadcast[JsValue]

  def receive = {

    case MoveGame(user,x,y) => {
      //Users.setUserPosition(x,y,user.token)
      notifyAll_Move("move", user, "has entered the room", x,y)
    }
    case JumpGame(user,seq_room) => {
      //Users.setUserPosition(x,y,user.token)
      notifyAll("jump", user, "has entered the room",seq_room)
    }
    case ShootGame(user,seq_room) => {
      //Users.setUserPosition(x,y,user.token)
      notifyAll("shoot", user, "has entered the room",seq_room)
    }
    case ReadyGame(user,seq_room) => {
      //Users.setUserPosition(x,y,user.token)
      notifyAll("ready", user, "has entered the room",seq_room)
    }
    case JoinGame(user,seq_room) => {

      if(members.contains(user)) {
        sender ! CannotConnect("This username is already used")
      } else {
        members = members + user
        sender ! Connected(gameEnumerator)
        self ! NotifyJoinGame(user,seq_room)
      }
    }

    case NotifyJoinGame(user,seq_room) => {
      Users.setUserCurrentRoom(user.token,seq_room)
      notifyAll("join", user, "has entered the room", seq_room)
      val room:Room = Rooms.getRoom(seq_room)
      println("check : "+room.id_owner + "//" + user.token)
      if(room.id_owner != user.token) self ! NotifyRoomOwner(user, room)
    }

    case NotifyRoomOwner(user,room) => {
      println("addOwner!!")
      notifyAll("addOwner", room.owner, "has entered the room", room.seq.get)
    }

    case TalkGame(token, text) => {
      notifyAll("talk", token, text)
    }

    case QuitGame(user) => {
      members = members - user
      notifyAll("quit", user, "has left the room")
    }

  }

  def notifyAll(action:String, user:User, msg:String, seq_room:Long=0) {
    //val user:User = Users.getUserByToken(token)
    val room:Room = Rooms.getRoom(seq_room)
    println(user.token + "//" + room.owner.token)
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
        "user_type" -> JsNumber(user_type)
      )
    )
    gameChannel.push(msg2)
  }

  def notifyAll_Move(action:String, user:User, msg:String, x:Int, y:Int) {
    //val user:User = Users.getUserByToken(token)


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
        "x" -> JsNumber(x),
        "y" -> JsNumber(y),
        "current_room_seq" -> JsNumber(user.seq_current_room)
      )
    )
    gameChannel.push(msg2)
  }

}

case class PlayGame(user:User, seq_room:Long)
case class ReadyGame(user:User, seq_room:Long)
case class ShootGame(user:User,seq_room:Long)
case class JumpGame(user:User,seq_room:Long)
case class MoveGame(user:User,x:Int,y:Int)
case class JoinGame(user:User,seq_room:Long)
case class QuitGame(user: User)
case class TalkGame(user: User, text: String)
case class NotifyJoinGame(user: User, seq_room:Long)
case class NotifyRoomOwner(user: User, room:Room)

//case class Connected(enumerator:Enumerator[JsValue])
//case class CannotConnect(msg: String)
