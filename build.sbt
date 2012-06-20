name := "bullfinch"

version := "3.0"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "org.specs2"                %% "specs2"                 % "1.11"           % "test",
  "com.codahale"              %% "logula"                 % "2.1.3",
  "com.codahale"              %% "jerkson"                % "0.5.0",
  "commons-dbcp"              % "commons-dbcp"            % "1.4",
  "spy"                       % "spymemcached"            % "2.8.1"
)

resolvers ++= Seq(
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "couchbase" at "http://files.couchbase.com/maven2/",
  "twitter.com" at "http://maven.twttr.com/",
  "codahale" at "http://repo.codahale.com"
)