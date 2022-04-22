package sirjin.summary.repository

import akka.Done
import io.getquill._
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.concurrent.Future

class SummaryDataRepositoryImpl extends SummaryDataRepository {
  val logger: Logger                                 = LoggerFactory.getLogger(getClass)
  val ctx                                            = new PostgresAsyncContext(SnakeCase, "ctx")
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  import ctx._

  override def update(params: SummaryData): Future[Done] = {
    logger.info("UPDATE TO DB : {}", params)
    val a = quote {
      query[SummaryData]
        .insertValue(lift(params))
        .onConflictUpdate(_.shopId, _.ncId, _.date)(
          (t, e) => t.cuttingTime -> e.cuttingTime,
          (t, e) => t.inCycleTime -> e.inCycleTime,
          (t, e) => t.waitTime -> e.waitTime,
          (t, e) => t.alarmTime -> e.alarmTime,
          (t, e) => t.noConnectionTime -> e.noConnectionTime,
          (t, e) => t.operationRate -> e.operationRate
        )
    }
    for {
      _ <- ctx.run(a)
    } yield {
      Done
    }
  }
}
