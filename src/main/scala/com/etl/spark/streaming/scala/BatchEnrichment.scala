package com.etl.spark.streaming.scala

import com.etl.spark.streaming.scala.configurations.{HadoopClientConfig, SparkAppConfig}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

object BatchEnrichment extends SparkAppConfig with HadoopClientConfig {

  import spark.implicits._

  val rawTrips: RDD[String] = spark.sparkContext.textFile(s"$stagingDir/trips/trips.txt")
  val tripsRdd: RDD[Trip] = rawTrips.filter(!_.contains("trip_id")).map(Trip(_))
  val tripsDF: DataFrame = tripsRdd.toDF()
  tripsDF.createOrReplaceTempView("tblTrip")

  val routesDF: DataFrame = spark.read
    .schema(Route.routeSchema)
    .csv(s"$stagingDir/routes/routes.txt")
  routesDF.createOrReplaceTempView("tblRoute")

  val calendarDatesDF: DataFrame = spark.read
    .schema(CalendarDate.calDateSchema)
    .csv(s"$stagingDir/calendar_dates/calendar_dates.txt")
  calendarDatesDF.createOrReplaceTempView("tblCalDate")

  val enrichedTripsDF: DataFrame = spark.sql(
    s"""SELECT
       |    trips.tripId,
       |    trips.serviceId,
       |    trips.routeId,
       |    trips.tripHeadSign,
       |    trips.wheelchairAccessible,
       |    calenders.date,
       |    calenders.exceptionType,
       |    routes.routeLongName,
       |    routes.routeColor
       | FROM tblTrip trips
       | LEFT JOIN tblRoute routes
       | ON trips.routeId = routes.routeId
       | LEFT JOIN tblCalDate calenders
       | ON trips.serviceId = calenders.serviceId""".stripMargin
  )
}
