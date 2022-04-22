package sirjin.summary.repository

import akka.Done

import scala.concurrent.Future

trait SummaryDataRepository {
  def update(params: SummaryData): Future[Done]
}
