package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._


// Case classes for our data
//case class Room(roominfo:Room_Ex, owner:User=Nil, player:User=Nil, watcher:Seq[User]=Nil)
case class Room(seq:Pk[Long],  id_owner:String, id_player:Option[String], title:String, time:Int, privacy:Int, game_status:Int,gameinfo:Game, owner:User, player:User=null, watcher:Seq[User]=null)

object Rooms{
  val withUser = {
    get[Pk[Long]]("roominfo.seq") ~
      get[String]("roominfo.id_owner") ~
      get[Option[String]]("roominfo.id_player") ~
      get[String]("roominfo.title") ~
      get[Int]("roominfo.time") ~
      get[Int]("roominfo.privacy") ~
      get[Int]("roominfo.game_status") ~
      Games.simple ~
      Users.simple map {
      case seq ~ id_owner ~ id_player ~ title ~ time ~ privacy ~ game_status ~ gameinfo ~ owner =>
        Room(seq, id_owner, id_player, title, time, privacy, game_status, gameinfo,  owner)
    }
  }
  var current_room:Seq[Room] = getRooms

  def getRooms:Seq[Room] = DB.withConnection {
    println("=== Rooms - getRooms()")
    implicit connection =>
      SQL(
        """
            select
              A.*,B.*,C.*
            from
              roominfo A,
              gameinfo B,
              userinfo C
            where
              A.game_status != 0 AND
              A.seq = B.seq_room AND
              A.id_owner = C.token
        """
      ).as(Rooms.withUser *)
  }

  def getRoom(seq:Int):Room = DB.withConnection {
    println("=== Rooms - getRoom()")
    implicit connection =>
      SQL(
        """
            select
              A.*,B.*,C.*
            from
              roominfo A,
              gameinfo B,
              userinfo C
            where
              A.game_status != 0 AND
              A.seq = B.seq_room AND
              A.id_owner = C.token AND
              A.seq = {seq}
        """
      ).on(
        'seq -> seq
      ).as(Rooms.withUser.singleOpt).getOrElse(null)
  }

  def getUsers(seq_room:Int):User = DB.withConnection {
    println("=== Rooms - getUsers()")
    implicit connection =>
      SQL(
        """
            select
              B.*
            from
              roominfo A,
              userinfo B
            where
              A.seq = {seq} AND
              B.seq_current_room = A.seq AND
              A.id_owner <> B.token
            LIMIT 0,1
        """
      ).on(
        'seq -> seq_room
      ).as(Users.simple.singleOpt).getOrElse(null)
  }
}