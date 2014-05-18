package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._

// Case classes for our data
case class User(seq:Pk[Long],  name:String,  email:String,  pic_url:String, win:Int,  lose:Int,  total:Int, token:String,joinDate:String,  current_location:Int, location_x:Int, location_y:Int, seq_current_room:Int)

object Users{
  val simple = {
    get[Pk[Long]]("userinfo.seq") ~
      get[String]("userinfo.name") ~
      get[String]("userinfo.email") ~
      get[String]("userinfo.pic_url") ~
      get[Int]("userinfo.win") ~
      get[Int]("userinfo.lose") ~
      get[Int]("userinfo.total") ~
      get[String]("userinfo.token") ~
      get[String]("userinfo.joinDate") ~
      get[Int]("userinfo.current_location") ~
      get[Int]("userinfo.location_x") ~
      get[Int]("userinfo.location_y") ~
      get[Int]("userinfo.seq_current_room")  map {
      case seq ~ name ~ email ~ pic_url ~ win ~ lose ~ total ~ token ~ joinDate ~ current_location ~ location_x ~ location_y ~ seq_current_room  =>
        User(seq , name , email , pic_url , win , lose , total , token , joinDate , current_location , location_x , location_y , seq_current_room)
    }
  }
  implicit object PkFormat extends Format[Pk[Long]] {
    def reads(json: JsValue): JsResult[Pk[Long]] = JsSuccess (
      json.asOpt[Long].map(id => Id(id)).getOrElse(NotAssigned)
    )
    def writes(id: Pk[Long]): JsValue = id.map(JsNumber(_)).getOrElse(JsNull)
  }
  var current_user:Seq[User] = getUsers

    def updateUsers = {
      current_user = getUsers
    }

    def printUsers = {
      current_user.foreach{ c=>
        println(c.name +" / "+c.token+" / "+c.current_location)
      }
    }

    // User process section
    def setUserStatusByToken(token:String, status:Int) = DB.withConnection {

      implicit connection =>
        println("=== Users - setUserStatusByToken()")
        SQL(
          """
          update userinfo set current_location = {status} where token = {token}
          """
        ).on(
            'status -> status,
            'token -> token
          ).executeUpdate()
        this
      // current location : 0 offline 1 room 2 into the room(player) 3 into the room(watcher)
    }

  // User process section
  def setUserCurrentRoom(token:String, seq_room:Int) = DB.withConnection {

    implicit connection =>
      println("=== Users - setUserStatusByToken()")
      SQL(
        """
          update userinfo set seq_current_room = {seq_room} where token = {token}
        """
      ).on(
          'seq_room -> seq_room,
          'token -> token
        ).executeUpdate()
      this
    // current location : 0 offline 1 room 2 into the room(player) 3 into the room(watcher)
  }

    def getUsers:List[User] = DB.withConnection {

      implicit connection =>
        println("=== Users - getUsers()")
        SQL(
          """
            select * from userinfo
          """
        ).as(Users.simple *)
    }

    def addUser(name:String,  email:String, pic_url:String, token:String):Int = DB.withConnection {

      implicit connection =>
        println("=== Users - addUser()")
        val user:User = getUserByToken(token);

        //println("user seq : "+user.seq)
        if(user == null) { // 없음.
          SQL(
            """
              INSERT INTO userinfo(
                name,
                email,
                pic_url,
                win,
                lose,
                total,
                token,
                joinDate,
                current_location,
                location_x,
                location_y,
                seq_current_room
              )
              VALUES(
                {name},
                {email},
                {pic_url},
                0,
                0,
                0,
                {token},
                now(),
                1,
                0,
                0,
                0
              )
            """
          ).on(
              'name -> name,
              'email -> email,
              'pic_url -> pic_url,
              'token -> token
            ).executeUpdate()

          setUserStatusByToken(token,1)
          printUsers
          updateUsers
          100
        }else{ // 있음.
          setUserStatusByToken(token,1)
          printUsers
          updateUsers
          101
        }
    }

  def getUserByToken(token:String):User = DB.withConnection {
    println("=== Users - getUserByToken()")
    implicit connection =>
      SQL(
        """
            select * from userinfo where token={token}
        """
      ).on(
        'token -> token
      ).as(Users.simple.singleOpt).getOrElse(null)
  }

  def setUserPosition(x:Int, y:Int, token:String) = DB.withConnection {

    implicit connection =>
      println("=== Users - setUserPosition()")
      SQL(
        """
      update userinfo set location_x = {x}, location_y = {y} where token = {token}
        """
      ).on(
          'x -> x,
          'y -> y,
          'token -> token
        ).executeUpdate()
    // current location : 0 offline 1 room 2 into the room(player) 3 into the room(watcher)
  }
}
