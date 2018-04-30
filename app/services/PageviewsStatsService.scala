package services

import java.time.{ZoneId, ZonedDateTime}
import javax.inject._

import model.{PageviewEntry, PageviewsStats}
import repositories.PageviewsStatsRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PageviewsStatsService @Inject()(repository: PageviewsStatsRepository)
                                     (implicit ec: ExecutionContext) {

  private val activityDelayMillis = 120000

  def persistPageview(userId: String, pageName: String, dateTime: String): Future[PageviewEntry] = {
    val epochMillis = ZonedDateTime.parse(dateTime).toInstant.toEpochMilli
    val entry = PageviewEntry(userId, pageName, epochMillis)
    repository.persistPageview(entry)
  }

  def retrievePageviewsStats(userId: String, dayCount: Int): Future[PageviewsStats] = {
    val epochMillisBoundary = ZonedDateTime.now(ZoneId.of("Z")).minusDays(dayCount).toInstant.toEpochMilli
    repository.getSortedPageviews(userId, epochMillisBoundary).map { pageviews =>
      val viewCount = totalViewCount(pageviews)
      val activeDays = activeDaysCount(pageviews)
      val page = mostViewedPage(pageviews).getOrElse("")
      val activeTime = activeTimeInSeconds(pageviews)
      PageviewsStats(userId, dayCount, viewCount, activeDays, activeTime, page)
    }
  }

  def totalViewCount(pageviews: Seq[PageviewEntry]): Int = pageviews.size

  def activeDaysCount(pageviews: Seq[PageviewEntry]): Int = pageviews.groupBy(_.dateEpochMillis).size

  def mostViewedPage(pageviews: Seq[PageviewEntry]): Option[String] = {
    pageviews
      .groupBy(_.pageName)
      .map {case (name, xs) => (name, xs.size)}
      .reduceOption {(x, y) => if (x._2 > y._2) x else y}
      .map(_._1)
  }

  def activeTimeInSeconds(pageviews: Seq[PageviewEntry]): Long = {
    val consecutiveGroups = pageviews.foldLeft(Seq.empty[Seq[PageviewEntry]]) { (acc, entry) =>
      val isConsecutive = acc.headOption.flatMap(_.headOption)
        .exists(_.epochMillis + activityDelayMillis >= entry.epochMillis)
      if (isConsecutive)
        (entry +: acc.head) +: acc.tail
      else
        Seq(entry) +: acc
    }
    consecutiveGroups.map {xs => xs.head.epochMillis - xs.last.epochMillis + activityDelayMillis}.sum / 1000
  }

}
