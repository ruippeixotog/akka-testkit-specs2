import ReleaseTransformations._
import scalariform.formatter.preferences._

name := "akka-testkit-specs2"
organization := "net.ruippeixotog"

scalaVersion := "2.13.1"
crossScalaVersions := Seq("2.12.10", "2.13.1")

resolvers ++= Seq(
  Resolver.bintrayRepo("scalaz", "releases"))

libraryDependencies ++= Seq(
  "org.specs2"            %% "specs2-core"        % "4.5.1",
  "com.typesafe.akka"     %% "akka-testkit"       % "2.6.1")

scalariformPreferences := scalariformPreferences.value
  .setPreference(DanglingCloseParenthesis, Prevent)
  .setPreference(DoubleIndentConstructorArguments, true)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ => false }

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))
homepage := Some(url("https://github.com/ruippeixotog/akka-testkit-specs2"))
pomExtra := {
  <scm>
    <url>https://github.com/ruippeixotog/akka-testkit-specs2</url>
    <connection>scm:git:https://github.com/ruippeixotog/akka-testkit-specs2.git</connection>
  </scm>
  <developers>
    <developer>
      <id>ruippeixotog</id>
      <name>Rui Gonçalves</name>
      <url>http://www.ruippeixotog.net</url>
    </developer>
  </developers>
}

releaseCrossBuild := true
releaseTagComment := s"Release ${(version in ThisBuild).value}"
releaseCommitMessage := s"Set version to ${(version in ThisBuild).value}"

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
  pushChanges)
