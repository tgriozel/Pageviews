package repositories

import javax.inject.{Inject, Named}

import com.paulgoldbaum.influxdbclient.{Database, InfluxDB}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


class InfluxProvider @Inject()(@Named("influxdb.host") dbHost: String,
                               @Named("influxdb.port") dbPort: Int,
                               @Named("influxdb.db") dbName: String) {

  private val influxDB = InfluxDB.connect(dbHost, dbPort)
  private val dbReference = influxDB.selectDatabase(dbName)
  private val dbFuture = dbReference.exists().flatMap { doesExist =>
    if (!doesExist) dbReference.create().map(_ => dbReference) else Future.successful(dbReference)
  }
  private val preparedDb = Await.result(dbFuture, 10 seconds)

  def db: Database = preparedDb

}
