import models.{Users, User}
import play.api._

import play.api.mvc._

/**
 * Created by changmatthew on 2014. 5. 12..
 */
object Global extends GlobalSettings {

  override def onStart(app: Application) {
    //Logger.info("Application has started")


  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}