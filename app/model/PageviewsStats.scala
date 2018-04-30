package model

case class PageviewsStats(userId: String,
                          daysCount: Int,
                          numberPagesViewed: Int,
                          numberOfDaysActive: Int,
                          secondsSpentOnSite: Long,
                          mostViewedPage: String)
