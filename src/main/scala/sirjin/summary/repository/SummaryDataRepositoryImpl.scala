package sirjin.summary.repository

import akka.Done
import io.getquill._
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sirjin.summary.repository.SummaryDataDTO.DailyTotalHistory

import scala.concurrent.Future

class SummaryDataRepositoryImpl extends SummaryDataRepository {
  val logger: Logger                                 = LoggerFactory.getLogger(getClass)
  val ctx                                            = new PostgresAsyncContext(SnakeCase, "ctx")
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  import ctx._

  override def update(params: DailyTotalHistory): Future[Done] = {
    logger.info("UPDATE TO DB : {}", params)
    val a = quote {
      query[DailyTotalHistory]
        .insertValue(lift(params))
        .onConflictUpdate(_.ncId, _.date)(
          (t, e) => t.quantity -> e.quantity,
          (t, e) => t.cycleTime -> e.cycleTime,
          (t, e) => t.inCycleTime -> e.inCycleTime,
          (t, e) => t.waitTime -> e.waitTime,
          (t, e) => t.alarmTime -> e.alarmTime,
          (t, e) => t.noconnTime -> e.noconnTime,
        )
    }

    for (_ <- ctx.run(a)) yield {
      Done
    }
  }
}
