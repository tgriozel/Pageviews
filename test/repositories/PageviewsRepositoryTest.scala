package repositories

import com.paulgoldbaum.influxdbclient.Parameter.Consistency.Consistency
import com.paulgoldbaum.influxdbclient.Parameter.Precision
import com.paulgoldbaum.influxdbclient.Parameter.Precision.Precision
import com.paulgoldbaum.influxdbclient._
import model.PageviewEntry
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.AsyncFlatSpec
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class PageviewsRepositoryTest extends AsyncFlatSpec with MockitoSugar {

  behavior of "persistPageview"

  it should "issue a query to the driver to store the entry" in {
    // Given
    val entry = PageviewEntry("tom", "databases", 1525455000000l)
    val expectedPoint = Point("pageview", entry.epochMillis)
      .addTag("userId", entry.userId)
      .addField("pageName", entry.pageName)
      .addField("dateEpochMillis", entry.dateEpochMillis)
    val db = mock[Database]
    val provider = mock[InfluxProvider]
    val repository = new PageviewsStatsRepository(provider)
    when(provider.db).thenReturn(db)
    when(db.write(any[Point], any[Precision], any[Consistency], any[String])).thenReturn(Future.successful(true))

    // When
    repository.persistPageview(entry).map { result =>
      // Then
      verify(db).write(expectedPoint, Precision.MILLISECONDS, null, null)
      result should be(entry)
    }
  }

  behavior of "getSortedPageviews"

  it should "issue a query to the driver to retrieve entries" in {
    // Given
    val entry = PageviewEntry("tom", "databases", 1525455000000l)
    val easyExit = new Exception("timeout")
    val db = mock[Database]
    val provider = mock[InfluxProvider]
    val repository = new PageviewsStatsRepository(provider)
    when(provider.db).thenReturn(db)
    when(db.query(any[String], any[Precision])).thenReturn(Future.failed(easyExit))

    // When
    repository.getSortedPageviews("tom", 1525400000000l).failed.map { result =>
      // Then
      verify(db).query("SELECT * FROM pageview WHERE userId = 'tom' AND time > 1525400000000000000 ORDER BY time",
        Precision.MILLISECONDS)
      result should be(easyExit)
    }
  }

}
