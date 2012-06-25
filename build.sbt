name := "bullfinch"

version := "3.0"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "org.specs2"                %% "specs2"                 % "1.11"           % "test",
  "org.hsqldb"                % "hsqldb"                  % "2.2.8",
  "com.codahale"              %% "logula"                 % "2.1.3",
  "commons-dbcp"              % "commons-dbcp"            % "1.4",
  "net.liftweb"               %% "lift-json"              % "2.4",
  "mysql"                     % "mysql-connector-java"    % "5.1.20",
  "spy"                       % "spymemcached"            % "2.8.1"
)

resolvers ++= Seq(
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "couchbase" at "http://files.couchbase.com/maven2/",
  "twitter.com" at "http://maven.twttr.com/",
  "codahale" at "http://repo.codahale.com"
)