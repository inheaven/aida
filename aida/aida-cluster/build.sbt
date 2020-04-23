name := "Scala test"

version := "0.1"

scalaVersion := "2.11.12"

resolvers += Resolver.mavenLocal // for testing
resolvers += "DataStax Repo" at "https://repo.datastax.com/public-repos/"

val dseVersion = "5.1.4"

// Please make sure that following DSE version matches your DSE cluster version.
// Exclusions are solely for running integrated testing
// Warning Sbt 0.13.13 or greater is required due to a bug with dependency resolution
libraryDependencies += (
  "com.datastax.dse" % "dse-spark-dependencies" % dseVersion % "provided"
  )



