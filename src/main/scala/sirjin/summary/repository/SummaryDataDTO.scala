package sirjin.summary.repository

import java.time.LocalDate

object SummaryDataDTO {
  case class DailyTotalHistory(
      shopId: String = "0",
      date: LocalDate,
      ncId: String,
      quantity: Int,
      cycleTime: Int,
      inCycleTime: Int,
      waitTime: Int,
      alarmTime: Int,
      noconnTime: Int,
      opRate: Float = 0f,
  )
}
