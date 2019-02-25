import sbtcrossproject.{crossProject, CrossType}
import NativePackagerHelper._
import com.typesafe.sbt.packager.SettingsHelper._


lazy val commonSettings = Seq(
  scalaVersion := "2.12.6",
  organization := "com.ofenbeck"
)

val akkaVersion = "2.5.18"


lazy val NMM_server = (project in file("server")).settings(commonSettings).settings(
  scalafmtOnCompile := true,
  version := "0.0.1-SNAPSHOT",
  name := "NMM_server",
  scalaJSProjects := Seq(NMM_client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  fullResolvers := (Resolver.jcenterRepo +: fullResolvers.value),
  updateOptions := updateOptions.value.withLatestSnapshots(false),
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.2",
    "org.scalacheck" %% "scalacheck" % "1.13.5",
    "org.webjars" %% "webjars-play" % "2.6.3",
    "org.webjars" % "bootstrap" % "3.1.1-2",
    "org.webjars" % "flot" % "0.8.3-1",
    "org.jsoup" % "jsoup" % "1.11.3",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % Test,
    "net.logstash.logback" % "logstash-logback-encoder" % "5.2",

    guice,
    specs2 % Test,
    ws
  )).enablePlugins(PlayScala, UniversalPlugin, UniversalDeployPlugin, SbtWeb)






lazy val NMM_client = (project in file("client")).settings(commonSettings).settings(
  scalafmtOnCompile := true,
  scalaJSUseMainModuleInitializer := false,
  fullResolvers := (Resolver.jcenterRepo +: fullResolvers.value),
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.5",
    "org.singlespaced" %%% "scalajs-d3" % "0.3.4",
    "org.querki" %%% "jquery-facade" % "1.2",
    "com.typesafe.play" %%% "play-json" % "2.6.10",
    "org.scala-js" %%% "scalajs-java-time" % "0.2.5",
    "com.lihaoyi" %%% "scalatags" % "0.6.2",
    "com.ofenbeck" %%% "nmmlogic" % "0.1.1"
  ),

  jsDependencies += "org.webjars" % "jquery" % "2.2.1" / "jquery.js" minified "jquery.min.js",
  updateOptions := updateOptions.value.withLatestSnapshots(false),
  //scalaJSUseMainModuleInitializer := true
).enablePlugins(ScalaJSPlugin, ScalaJSWeb)

// loads the sint_gui_server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen { s: State => "project NMM_server" :: s }