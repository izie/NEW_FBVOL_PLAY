package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current

case class Game(seq:Pk[Long],  seq_room:Int,  turn:Int, score_owner:Int, score_enemy:Int)

object Games{
  val simple = {
    get[Pk[Long]]("gameinfo.seq") ~
    get[Int]("gameinfo.seq_room") ~
      get[Int]("gameinfo.turn") ~
      get[Int]("gameinfo.score_owner") ~
      get[Int]("gameinfo.score_enemy")  map {
      case seq ~ seq_room ~ turn ~ score_owner ~ score_enemy  =>
        Game(seq , seq_room , turn , score_owner , score_enemy)
    }
  }
}