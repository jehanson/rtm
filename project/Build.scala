import sbt._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "rtm"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "com.google.guava" % "guava" % "15.0",
    "com.typesafe.play.extras" %% "iteratees-extras" % "1.0.1" // used to provide GZIP filtering in 2.1

  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
