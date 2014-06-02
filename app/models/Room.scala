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

  val withPlayer = {
    get[Pk[Long]]("roominfo.seq") ~
      get[String]("roominfo.id_owner") ~
      get[Option[String]]("roominfo.id_player") ~
      get[String]("roominfo.title") ~
      get[Int]("roominfo.time") ~
      get[Int]("roominfo.privacy") ~
      get[Int]("roominfo.game_status") ~
      Games.simple ~
      Users.simple ~
      Users.simple map {
      case seq ~ id_owner ~ id_player ~ title ~ time ~ privacy ~ game_status ~ gameinfo ~ owner ~ player =>
        Room(seq, id_owner, id_player, title, time, privacy, game_status, gameinfo,  owner, player)
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

  def getRoom(seq:Long):Room = DB.withConnection {
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

  def getSeqRoomByOwner(token:String):Long = DB.withConnection {
    println("=== Rooms - getRoomByOwner()")
    implicit connection =>
      SQL(
        """
            select seq from roominfo where id_owner={token} order by seq desc limit 0,1
        """
      ).on(
          'token -> token
        ).as(scalar[Long].single)
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

  def getUsersInRoom(seq_room:Int):Seq[User] = DB.withConnection {
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
              B.seq_current_room = A.seq
        """
      ).on(
          'seq -> seq_room
        ).as(Users.simple *)
  }

  def getViewersInRoom(seq_room:Long):Seq[User] = DB.withConnection {
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
              A.id_owner <> B.token AND
              A.id_player <> B.token
        """
      ).on(
          'seq -> seq_room
        ).as(Users.simple *)
  }

  def setUserJoinForPlay(seq_room:Int, token:String) = DB.withConnection {

    implicit connection =>
      println("=== Rooms - setUserJoinForPlay()")
      SQL(
        """
            update
              roominfo
            set
              id_player = {token}
            where
              seq={seq}
        """
      ).on(
          'seq -> seq_room,
          'token -> token
        ).executeUpdate

      Users.setUserStatusByToken(token, 2)
      Users.setUserCurrentRoom(token, seq_room)
  }

  def setUserJoinForView(seq_room:Int, token:String) = DB.withConnection {
    implicit connection =>
      println("=== Rooms - setUserJoinForView()")
    // 유저정보 업데이트
      SQL(
        """
            update
              userinfo
            set
              seq_current_room = {seq},
              current_location = 3
            where
              token = {token}
        """
      ).on(
          'token -> token,
          'seq -> seq_room
        ).execute

      Users.setUserStatusByToken(token, 3)
      Users.setUserCurrentRoom(token, seq_room)
  }

  def removeRoom(seq_room:Long) = DB.withConnection {

    implicit connection =>
      println("=== Rooms - setUserOutAsOwner()")
      SQL(
        """
            delete from
              roominfo
            where
              seq = {seq}
        """
      ).on(
          'seq -> seq_room
        ).execute

      val users:Seq[User] = getViewersInRoom(seq_room)

      users.foreach{ c=>
        Users.setUserStatusByToken(c.token, 1)
        Users.setUserCurrentRoom(c.token, 0)
      }
  }

  def addRoom(title:String,  time:Int, token:String):Long = DB.withConnection {

    implicit connection =>
      println("=== Users - addRoom()")
      val user:User = Users.getUserByToken(token);

      //println("user seq : "+user.seq)
      SQL(
        """
            INSERT INTO roominfo(
              id_owner,
              title,
              time,
              privacy,
              game_status
            )
            VALUES(
              {token},
              {title},
              {time},
              1,
              1
            )
        """
      ).on(
          'token -> token,
          'title -> title,
          'time -> time
        ).executeUpdate

      val seq_room:Long = getSeqRoomByOwner(token)

      addDefaultGameInfo(seq_room)

      Users.setUserStatusByToken(token, 2)
      Users.setUserCurrentRoom(token, seq_room)

      seq_room

  }

  def addDefaultGameInfo(seq_room:Long) = DB.withConnection {

    implicit connection =>
      println("=== Users - addDefaultGameInfo()")

      //println("user seq : "+user.seq)
      SQL(
        """
            INSERT INTO gameinfo(
              seq_room,
              turn,
              score_owner,
              score_enemy
            )
            VALUES(
              {seq_room},
              0,0,0
            )
        """
      ).on(
          'seq_room -> seq_room
        ).executeUpdate

  }

}