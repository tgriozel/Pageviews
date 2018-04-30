package modules

import play.api.inject._
import repositories.{InfluxProvider, PageviewsStatsRepository}
import services.PageviewsStatsService

class PageviewsModule extends SimpleModule((env, conf) =>
  Seq(
    bind[PageviewsStatsService].toSelf.eagerly(),
    bind[InfluxProvider].toSelf.eagerly(),
    bind[PageviewsStatsRepository].toSelf.eagerly()
  )
)
