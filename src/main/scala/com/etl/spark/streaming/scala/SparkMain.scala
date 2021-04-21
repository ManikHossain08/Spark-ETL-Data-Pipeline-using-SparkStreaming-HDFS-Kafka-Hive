package com.etl.spark.streaming.scala

import com.etl.spark.streaming.scala.configurations.{Base, HadoopClientConfig, KafkaConsumerConfig, SparkAppConfig}
import org.apache.spark.sql.Dataset
import org.apache.hadoop.fs.Path
import java.util.UUID

object SparkMain extends App with SparkAppConfig with KafkaConsumerConfig with HadoopClientConfig with Base {

  StagingAndSchemaDeploy.putFilesToHDFS()

  import spark.implicits._

  BatchEnrichment.enrichedTripsDF.createOrReplaceTempView("tblEnrichedTrip")

  kafkaStopTimeStream
    .map(_.value())
    .foreachRDD { stopTimeRDD =>
      val stopTimeDf = stopTimeRDD.map(StopTime(_)).toDF()
      stopTimeDf.createOrReplaceTempView("tblStopTime")
      val enrichedStopTime = spark.sql(
        """SELECT
          |   et.tripId,
          |   et.serviceId,
          |   et.routeId,
          |   et.tripHeadSign,
          |   et.date,
          |   et.exceptionType,
          |   et.routeLongName,
          |   et.routeColor,
          |   st.arrivalTime,
          |   st.departureTime,
          |   st.stopId,
          |   st.stopSequence,
          |   et.wheelchairAccessible
          | from tblStopTime st
          | left join tblEnrichedTrip et
          | on st.tripId == et.tripId
          |""".stripMargin
      )
      partitionAndStoreToHdfs(enrichedStopTime.as[EnrichedStopTime])
    }

  ssc.start()
  ssc.awaitTermination()

  def partitionAndStoreToHdfs(enrichedStopTimeDS: Dataset[EnrichedStopTime]): Unit = {
    enrichedStopTimeDS
      .rdd
      .groupBy(_.wheelchairAccessible)
      .foreach { case (partitionId, enrichedStopTimes) =>
        val partitionDir = new Path(s"$stagingDir/enriched_stop_time/wheelchairaccessible=$partitionId")
        if (!fileSystem.exists(partitionDir)) fileSystem.mkdirs(partitionDir)
        val outFilePath =
          new Path(s"$stagingDir/enriched_stop_time/wheelchairaccessible=$partitionId/${UUID.randomUUID()}")

        val outputStream = fileSystem.create(outFilePath)
        enrichedStopTimes
          .foreach { enrichedStopTime =>
            outputStream.write(EnrichedStopTime.toCsv(enrichedStopTime).getBytes)
            outputStream.write("\n".getBytes)
          }
        outputStream.close()
      }
  }
}
