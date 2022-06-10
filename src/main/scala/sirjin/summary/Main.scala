package sirjin.summary

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.softwaremill.macwire.wire
import io.getquill.PostgresAsyncContext
import io.getquill.SnakeCase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sirjin.summary.repository.SummaryDataRepository
import sirjin.summary.repository.SummaryDataRepositoryImpl

import scala.util.control.NonFatal

object Main {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    lazy val system                                = ActorSystem[Nothing](Behaviors.empty, "SirjinSummaryService")
    lazy val ctx: PostgresAsyncContext[SnakeCase]  = new PostgresAsyncContext(SnakeCase, "ctx")
    lazy val repo: SummaryDataRepository           = wire[SummaryDataRepositoryImpl]
    val machineEventConsumer: MachineEventConsumer = wire[MachineEventConsumer]
    try {
      AkkaManagement(system).start()
      ClusterBootstrap(system).start()
    } catch {
      case NonFatal(e) =>
        logger.error("Terminating due to initialization failure.", e)
        system.terminate()
    }
  }
}
