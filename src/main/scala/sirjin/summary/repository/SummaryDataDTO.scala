package sirjin.summary.repository

class SummaryDataDTO {}

case class SummaryData(
    shopId: Int = 0,
    date: String,
    ncId: String,
    cuttingTime: Int,
    inCycleTime: Int,
    waitTime: Int,
    alarmTime: Int,
    noConnectionTime: Int,
    operationRate: Float
)
