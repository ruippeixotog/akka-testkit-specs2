import ReleaseTransformations._

ThisBuild / organization := "net.ruippeixotog"

ThisBuild / scalaVersion := "2.13.16"

lazy val core = (project in file("core"))
  .settings(commonSettings)

lazy val akkaClassic = (project in file("akka/classic"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val akkaTyped = (project in file("akka/typed"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val akkaBundle = (project in file("akka"))
  .settings(commonSettings)
  .dependsOn(akkaClassic, akkaTyped)

lazy val pekkoClassic = (project in file("pekko/classic"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val pekkoTyped = (project in file("pekko/typed"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val pekkoBundle = (project in file("pekko"))
  .settings(commonSettings)
  .dependsOn(pekkoClassic, pekkoTyped)

lazy val commonSettings = Seq(
  // format: off
  crossScalaVersions := Seq("2.13.16", "3.3.6"),

  libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "4.21.0"),

  scalacOptions ++= (
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) => List("-Xsource:3")
      case _ => List("-rewrite", "-source", "3.3-migration")
    }
  ),

  scalafmtOnCompile := true,

  publishTo := {
    val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
    if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
    else localStaging.value
  },

  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ => false },

  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  homepage := Some(url("https://github.com/ruippeixotog/akka-testkit-specs2")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/ruippeixotog/akka-testkit-specs2"),
    "scm:git:https://github.com/ruippeixotog/akka-testkit-specs2.git")),
  developers := List(
    Developer("ruippeixotog", "Rui Gonçalves", "ruippeixotog@gmail.com", url("https://github.com/ruippeixotog")))
  // format: on
)

// do not publish the root project
publish / skip := true
publishArtifact := false

releaseCrossBuild := true
releaseTagComment := s"Release ${(ThisBuild / version).value}"
releaseCommitMessage := s"Set version to ${(ThisBuild / version).value}"

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  releaseStepCommand("sonaRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
