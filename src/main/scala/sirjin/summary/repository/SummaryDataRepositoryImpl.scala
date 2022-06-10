package sirjin.summary.repository

import io.getquill._
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sirjin.summary.repository.SummaryDataDTO.DailyTotalHistory

import scala.concurrent.Future

class SummaryDataRepositoryImpl(ctx: PostgresAsyncContext[SnakeCase]) extends SummaryDataRepository {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  import ctx._

  override def update(params: DailyTotalHistory): Future[Unit] = {
    logger.info("UPDATE TO DB : {}", params)
    val a = quote {
      query[DailyTotalHistory]
        .insertValue(lift(params))
        .onConflictUpdate(_.shopId, _.ncId, _.date)(
          (t, e) => t.quantity -> e.quantity,
          (t, e) => t.cycleTime -> e.cycleTime,
          (t, e) => t.inCycleTime -> e.inCycleTime,
          (t, e) => t.waitTime -> e.waitTime,
          (t, e) => t.alarmTime -> e.alarmTime,
          (t, e) => t.noconnTime -> e.noconnTime,
          (t, e) => t.opRate -> e.opRate
        )
    }

    for {
      _ <- ctx.run(a)
    } yield ()
  }
}
