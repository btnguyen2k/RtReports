name := "RtReports"

version := "1.0.0"

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % "0.8.0" excludeAll(
    ExclusionRule("com.sun.jdmk", "jmxtools"),
    ExclusionRule("com.sun.jmx", "jmxri"),
    ExclusionRule("javax.jms", "jmx"),
    ExclusionRule("org.slf4j", "slf4j-log4j12"),
    ExclusionRule("org.slf4j", "slf4j-simple"),
    ExclusionRule("org.jboss.netty", "netty")
  ),
  "org.apache.zookeeper" % "zookeeper" % "3.4.5" excludeAll(
    ExclusionRule("org.slf4j", "slf4j-log4j12"),
    ExclusionRule("org.slf4j", "slf4j-simple"),
    ExclusionRule("org.jboss.netty", "netty")
  ),
  "mysql" % "mysql-connector-java" % "5.1.26",
  "com.google.guava" % "guava" % "14.0",
  "com.github.ddth" % "ddth-commons" % "0.1.0",
  "com.github.ddth" %% "play-module-plommon" % "0.4.5",
  "com.typesafe" %% "play-plugins-redis" % "2.1.1",
  "org.apache.thrift" % "libthrift" % "0.9.1" excludeAll(
    ExclusionRule("org.slf4j", "slf4j-log4j12"),
    ExclusionRule("org.slf4j", "slf4j-simple")
  ),
  "com.datastax.cassandra" % "cassandra-driver-core" % "1.0.5" excludeAll(
    ExclusionRule("org.slf4j", "slf4j-log4j12"),
    ExclusionRule("org.slf4j", "slf4j-simple")
  ),
  "org.xerial.snappy" % "snappy-java" % "1.1.1-M1" excludeAll(
    ExclusionRule("org.slf4j", "slf4j-log4j12"),
    ExclusionRule("org.slf4j", "slf4j-simple")
  ),
  javaJdbc,
  javaEbean,
  cache
)     

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype snapshots repository" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "pk11-scratch" at "http://pk11-scratch.googlecode.com/svn/trunk"

play.Project.playJavaSettings
