package sirjin.summary.repository

class SummaryDataDTO {}

case class SummaryData(
    shopId: Int = 0,
    date: String,
    ncId: String,
    partCount : Int,
    cuttingTime: Int,
    inCycleTime: Int,
    waitTime: Int,
    alarmTime: Int,
    noConnectionTime: Int,
    operationRate: Float
)
