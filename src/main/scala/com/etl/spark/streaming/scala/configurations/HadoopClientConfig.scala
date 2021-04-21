package com.etl.spark.streaming.scala.configurations

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

trait HadoopClientConfig {
  val stagingDir = "/user/bdsf2001/manik/project5"

  val conf = new Configuration()
  val hadoopConfDir: String = System.getenv("HADOOP_CONF_DIR")
  conf.addResource(new Path(s"$hadoopConfDir/core-site.xml"))
  conf.addResource(new Path(s"$hadoopConfDir/hdfs-site.xml"))

  val fileSystem: FileSystem = FileSystem.get(conf)
}
