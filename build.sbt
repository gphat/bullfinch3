name := "bullfinch"

version := "3.0"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "org.specs2"                %% "specs2"                 % "1.11"           % "test",
  "com.codahale"              %% "logula"                 % "2.1.3",
  "com.codahale"              %% "jerkson"                % "0.5.0",
  "com.twitter"               %% "grabbyhands"            % "1.4"
)

resolvers ++= Seq(
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "twitter.com" at "http://maven.twttr.com/",
  "codahale" at "http://repo.codahale.com"
)