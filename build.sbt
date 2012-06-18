name := "bullfinch"

version := "3.0"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "org.specs2"                %% "specs2"                 % "1.11"           % "test",
  "org.clapper"               %% "grizzled-slf4j"         % "0.6.9"
)

resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                    "releases"  at "http://oss.sonatype.org/content/repositories/releases")