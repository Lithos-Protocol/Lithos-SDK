ThisBuild / version := "0.0.1"

ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file("."))
  .settings(
    name := "Lithos",
    idePackagePrefix := Some("work.lithos")
  )

libraryDependencies ++= Seq(
  "org.ergoplatform" %% "ergo-appkit" % "5.0.4",
  "io.github.k-singh" %% "plasma-toolkit" % "1.0.3",
  "org.slf4j" % "slf4j-jdk14" % "2.0.5",
  "org.scalatest" %% "scalatest" % "3.2.14" % "test"
)

resolvers ++= Seq(
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "SonaType" at "https://oss.sonatype.org/content/groups/public",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "New Sonatype Releases" at "https://s01.oss.sonatype.org/content/repositories/releases/",
  "Bintray" at "https://jcenter.bintray.com/"
)