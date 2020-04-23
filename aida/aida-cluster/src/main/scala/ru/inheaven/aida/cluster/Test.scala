package ru.inheaven.aida.cluster
/**
  * @author Anatoly A. Ivanov
  * 29.12.2017 22:46
  */
object Test extends App{

  val sparkSession = SparkSession.builder
    .master("spark://192.168.0.16:7077")
    .appName("my-spark-app")
    .getOrCreate

  val rdd = sparkSession.sparkContext.cassandraTable("test", "kb")

  println(rdd.first)

  sparkSession.stop()

}
