package sirjin.summary.repository

import akka.Done
import sirjin.summary.repository.SummaryDataDTO.DailyTotalHistory

import scala.concurrent.Future

trait SummaryDataRepository {
  def update(params: DailyTotalHistory): Future[Done]
}
