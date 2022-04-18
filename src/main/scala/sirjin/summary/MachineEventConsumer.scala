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
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import sirjin.machine.proto
import com.google.protobuf.any.{ Any => ScalaPBAny }
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.concurrent.duration.DurationInt

object MachineEventConsumer {
  private val logger =
    LoggerFactory.getLogger(getClass)

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

  private def handleRecord(record: ConsumerRecord[String, Array[Byte]]): Future[Done] = {
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
        case proto.MachineDataUpdated(
              ncId,
              cuttingTime,
              inCycleTime,
              waitTime,
              alarmTime,
              noConnectionTime,
              status,
              alarms,
              partNumber,
              opRate,
              path,
              partCount,
              totalPartCount,
              timestamp,
              toolNumber,
              _
            ) =>
          logger.info(
            "Machine Updated By Kafka {},{},{},{},{},{},{},{},{},{},{},{},{},{},{}",
            ncId,
            cuttingTime,
            inCycleTime,
            waitTime,
            alarmTime,
            noConnectionTime,
            status,
            alarms,
            partNumber,
            opRate,
            path,
            partCount,
            totalPartCount,
            timestamp,
            toolNumber
          )

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
