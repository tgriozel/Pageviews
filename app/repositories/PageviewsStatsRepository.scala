package repositories

import javax.inject._

import com.paulgoldbaum.influxdbclient.Parameter.Precision
import com.paulgoldbaum.influxdbclient.Parameter.Precision.Precision
import com.paulgoldbaum.influxdbclient.Point
import model.PageviewEntry

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PageviewsStatsRepository @Inject()(provider: InfluxProvider)
                                        (implicit ec: ExecutionContext) {

  private val precision = Precision.MILLISECONDS
  private val multFactor = precision.asInstanceOf[Precision] match {
    case Precision.NANOSECONDS =>  1l
    case Precision.MICROSECONDS => 1000l
    case Precision.MILLISECONDS => 1000000l
    case Precision.SECONDS =>      1000000000l
    case Precision.MINUTES =>      60000000000l
    case Precision.HOURS =>        3600000000000l
  }

  /*
   * Our pageview entries will be stored in InfluxDB with the following properties:
   * - epochMillis ('time' in the db) will be the primary key, it also has a primary index on it
   * - userId will be the tag (also indexed)
   * - pageName will be a field (not indexed)
   * - dateEpochMillis will be a field (not indexed)
   */

  def persistPageview(entry: PageviewEntry): Future[PageviewEntry] = {
    val point = Point("pageview", entry.epochMillis)
      .addTag("userId", entry.userId)
      .addField("pageName", entry.pageName)
      .addField("dateEpochMillis", entry.dateEpochMillis)
    provider.db.write(point, precision).map(_ => entry)
  }

  def getSortedPageviews(userId: String, epochMillis: Long): Future[Seq[PageviewEntry]] = {
    val query = s"SELECT * FROM pageview WHERE userId = '$userId' AND time > ${epochMillis * multFactor} ORDER BY time"
    provider.db.query(query, precision).map { result =>
      result.series.headOption.map { seriesHead =>
        seriesHead.records.map { r =>
          PageviewEntry(
            r("userId").asInstanceOf[String],
            r("pageName").asInstanceOf[String],
            r("time").asInstanceOf[BigDecimal].toLong / multFactor
          )
        }
      }.getOrElse(Seq.empty[PageviewEntry])
    }
  }

}
