package com.etl.spark.streaming.scala

import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

object Route {
  val routeSchema: StructType = StructType(
    List(
      StructField("routeId", IntegerType, nullable = false),
      StructField("agencyId", StringType, nullable = false),
      StructField("routeShortName", IntegerType, nullable = true),
      StructField("routeLongName", StringType, nullable = true),
      StructField("routeType", IntegerType, nullable = true),
      StructField("routeUrl", StringType, nullable = true),
      StructField("routeColor", StringType, nullable = true),
      StructField("routeTextColor", StringType, nullable = true)
    )
  )
}

object CalendarDate {
  val calDateSchema: StructType = StructType(
    List(
      StructField("serviceId", StringType, nullable = false),
      StructField("date", StringType, nullable = true),
      StructField("exceptionType", IntegerType, nullable = true)
    )
  )
}

case class Trip(tripId: String, serviceId: String, routeId: Int, tripHeadSign: String, wheelchairAccessible: Boolean)

object Trip {
  def apply(line: String): Trip = {
    val fields: Array[String] = line.split(",", -1)
    Trip(fields(2), fields(1), fields(0).toInt, fields(3), fields(6).toInt == 1)
  }
}

case class StopTime(tripId: String, arrivalTime: String, departureTime: String, stopId: String, stopSequence: Int)

object StopTime {
  def apply(line: String): StopTime = {
    val fields: Array[String] = line.split(",", -1)
    StopTime(fields(0), fields(1), fields(2), fields(3), fields(4).toInt)
  }
}

case class EnrichedStopTime(tripId: String,
                            serviceId: String,
                            routeId: Int,
                            tripHeadSign: String,
                            date: Option[String],
                            exceptionType: Option[Int],
                            routeLongName: String,
                            routeColor: String,
                            arrivalTime: String,
                            departureTime: String,
                            stopId: String,
                            stopSequence: Int,
                            wheelchairAccessible: Boolean)

object EnrichedStopTime {
  def toCsv(enrichedStopTime: EnrichedStopTime): String = {
    s"${enrichedStopTime.tripId}," +
      s"${enrichedStopTime.serviceId}," +
      s"${enrichedStopTime.routeId}," +
      s"${enrichedStopTime.tripHeadSign}," +
      s"${enrichedStopTime.date.getOrElse("")}," +
      s"${enrichedStopTime.exceptionType.getOrElse("")}," +
      s"${enrichedStopTime.routeLongName}," +
      s"${enrichedStopTime.routeColor}," +
      s"${enrichedStopTime.arrivalTime}," +
      s"${enrichedStopTime.departureTime}," +
      s"${enrichedStopTime.stopId}," +
      s"${enrichedStopTime.stopSequence}"
  }
}
