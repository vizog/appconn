val scalaV = "2.12.6"
val akkaV = "2.5.26"
val akkaHttpV = "10.1.11"
val reaktiveV = "0.16.2"
val alpakkaV = "1.1.2"
val akkaManagementV = "0.16.0"

organization := "com.vahid"
name := "app-connections"
scalaVersion := scalaV
javacOptions ++= Seq("-source", "1.8")
javacOptions in(Compile, Keys.compile) ++= Seq("-target", "1.8", "-Xlint", "-Xlint:-processing", "-Xlint:-serial", "-Werror")
enablePlugins(ProtobufPlugin)
// Run using the JAR-packaged protoc binary, so protoc doesn't have to be locally installed
protobufRunProtoc in ProtobufConfig := { args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v261" +: args.toArray)
}
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-cluster" % akkaV,
  "com.typesafe.akka" %% "akka-remote" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "com.typesafe.akka" %% "akka-persistence" % akkaV,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaV,
  "com.typesafe.akka" %% "akka-distributed-data" % akkaV,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaV,
  "com.lightbend.akka" %% "akka-stream-alpakka-udp" % alpakkaV,
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.83",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "org.slf4j" % "slf4j-log4j12" % "1.7.12",
  "com.github.ben-manes.caffeine" % "caffeine" % "2.5.5", // caching library
  "io.vavr" % "vavr" % "0.10.2",
  "com.lightbend.akka.discovery" %% "akka-discovery-config" % akkaManagementV,
  "com.google.protobuf" % "protobuf-java" % "2.6.1",

  "junit" % "junit" % "4.11" % "test",
  "org.assertj" % "assertj-core" % "3.2.0" % "test",
  "org.mockito" % "mockito-core" % "2.2.27" % "test",
  "info.solidsoft.mockito" % "mockito-java8" % "2.0.0" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.forgerock.cuppa" % "cuppa" % "1.3.1" % "test",
  "org.forgerock.cuppa" % "cuppa-junit" % "1.3.1" % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaV % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "test",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.15.2" % "test"
)
Test / parallelExecution := false