package sirjin.summary

import akka.Done
import akka.actor.typed.ActorSystem
import akka.kafka.CommitterSettings
import akka.kafka.ConsumerSettings
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Committer
import akka.kafka.scaladsl.Consumer
import akka.stream.RestartSettings
import akka.stream.scaladsl.RestartSource
import com.google.protobuf.any.{ Any => ScalaPBAny }
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import sirjin.machine.proto
import sirjin.machine.proto.MachineDataUpdated
import sirjin.summary.repository.SummaryDataDTO.DailyTotalHistory
import sirjin.summary.repository.SummaryDataRepositoryImpl

import java.time.LocalDate
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.control.NonFatal

object MachineEventConsumer {
  private val logger = LoggerFactory.getLogger(getClass)
  val repo           = new SummaryDataRepositoryImpl

  def init(system: ActorSystem[_]): Unit = {
    implicit val sys: ActorSystem[_]  = system
    implicit val ec: ExecutionContext = system.executionContext

    val topic = system.settings.config
      .getString("machine-data-service.kafka.topic")
    val consumerSettings =
      ConsumerSettings(system, new StringDeserializer, new ByteArrayDeserializer).withGroupId("sirjin-summary")
    val committerSettings = CommitterSettings(system)

    RestartSource
      .onFailuresWithBackoff(RestartSettings(minBackoff = 1.second, maxBackoff = 30.seconds, randomFactor = 0.1)) {
        () =>
          Consumer
            .committableSource(consumerSettings, Subscriptions.topics(topic))
            .mapAsync(1) { msg =>
              handleRecord(msg.record).map(_ => msg.committableOffset)
            }
            .via(Committer.flow(committerSettings))
      }
      .run()
    logger.info("started Kafka consumer")
  }

  private def handleRecord(
      record: ConsumerRecord[String, Array[Byte]]
  ): Future[Done] = {
    val bytes   = record.value()
    val x       = ScalaPBAny.parseFrom(bytes)
    val typeUrl = x.typeUrl
    try {
      val inputBytes = x.value.newCodedInput()
      val event = typeUrl match {
        case "machine-data-service/sirjinmachine.MachineDataUpdated" =>
          proto.MachineDataUpdated.parseFrom(inputBytes)
        case _ =>
          throw new IllegalArgumentException(s"unknown record type [$typeUrl]")
      }

      event match {
        case evt: MachineDataUpdated =>
          repo.update(
            DailyTotalHistory(
              date = LocalDate.now(),
              ncId = evt.ncId,
              quantity = evt.partCount,
              cycleTime = evt.cuttingTime,
              inCycleTime = evt.inCycleTime,
              waitTime = evt.waitTime,
              alarmTime = evt.alarmTime,
              noconnTime = evt.noConnectionTime,
            )
          )
        case _ =>
          logger.info("record type : {}", event)
          Future.successful(Done)
      }

      Future.successful(Done)
    } catch {
      case NonFatal(e) =>
        logger.error("Could not process event of type [{}]", typeUrl, e)
        // continue with next
        Future.successful(Done)
    }
  }
}
