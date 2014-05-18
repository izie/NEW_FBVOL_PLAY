import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "FIAMM_INTRA_PLAY"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,

    // WebJars pull in client-side web libraries
    "org.webjars" % "webjars-play" % "2.1.0",
    "org.webjars" % "bootstrap" % "2.3.1",
    "mysql" % "mysql-connector-java" % "5.1.24",
    //"com.typesafe.slick" %% "slick" % "2.0.2",
    //"org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.restfb" % "restfb" % "1.6.14" //this line should be new
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
