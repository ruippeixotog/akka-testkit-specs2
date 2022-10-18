import ReleaseTransformations._

ThisBuild / organization := "net.ruippeixotog"

scalaVersion := "2.13.8"

lazy val core = (project in file("core"))
  .settings(commonSettings)

lazy val classic = (project in file("classic"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val typed = (project in file("typed"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val bundle = (project in file("bundle"))
  .settings(commonSettings)
  .dependsOn(classic, typed)

lazy val commonSettings = Seq(
  // format: off
  crossScalaVersions := Seq("2.12.17", "2.13.8"),

  libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "4.17.0"),

  scalafmtOnCompile := true,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
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
  setNextVersion,
  commitNextVersion,
  releaseStepCommandAndRemaining("sonatypeReleaseAll"),
  pushChanges
)
