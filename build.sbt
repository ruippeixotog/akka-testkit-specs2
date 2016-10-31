import scalariform.formatter.preferences._

name := "akka-testkit-specs2"
organization := "net.ruippeixotog"
version := "0.2.0-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases")

libraryDependencies ++= Seq(
  "org.specs2"            %% "specs2-core"        % "3.8.5.1",
  "com.typesafe.akka"     %% "akka-testkit"       % "2.4.12")

scalariformPreferences := scalariformPreferences.value
  .setPreference(DanglingCloseParenthesis, Prevent)
  .setPreference(DoubleIndentClassDeclaration, true)

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
pomExtra :=
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
