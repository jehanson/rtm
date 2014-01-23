package service

import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit
import org.joda.time.LocalDate
import play.api.libs.ws.WS
import scala.concurrent.Future
import scala.collection.{SortedSet, SortedMap}
import scala.concurrent.ExecutionContext.Implicits.global
import java.net.URLEncoder


object Loader {

  type TimeSeries = SortedMap[LocalDate, Double]

  implicit val dateOrder: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)


  val cache = CacheBuilder.newBuilder()
    .expireAfterWrite(8, TimeUnit.HOURS)
    .build[String, SortedMap[LocalDate, Double]]


  def load(ticker: String): Future[SortedMap[LocalDate, Double]] = {
    val result = cache.getIfPresent(ticker)
    if (result != null) Future.successful(result)
    else {
      val futureResult = fromYahoo(ticker)
      futureResult.onSuccess {
        case x => cache.put(ticker, x)
      }
      futureResult
    }
  }


  def fromYahoo(ticker: String): Future[TimeSeries] = {
    val url = s"http://ichart.finance.yahoo.com/table.csv?s=${URLEncoder.encode(ticker, "UTF-8")}"

    for (validResponse <- WS.url(url).get().filter(_.status == 200)) yield {
      val builder = SortedMap.newBuilder[LocalDate, Double]

      val lineIt = validResponse.body.split('\n').iterator

      val headers = lineIt.next().split(',')

      val dateIdx = headers.indexOf("Date")
      val adjCloseIdx = headers.indexOf("Adj Close")

      for (line <- lineIt) {
        val cols = line.split(',')
        val date = LocalDate.parse(cols(dateIdx))
        val adjClose = cols(adjCloseIdx).toDouble
        builder += date -> adjClose
      }
      builder.result()
    }
  }

  def compare(baseTicker: String, otherTicker: String, targetEqualDate: LocalDate = LocalDate.now()): Future[TimeSeries] = {

    val futures = Seq(load(baseTicker), load(otherTicker))

    Future.sequence(futures).map {
      case Seq(base, other) => merge(base, other, targetEqualDate)
    }
  }

  def merge(base: TimeSeries, other: TimeSeries, targetEqualDate: LocalDate): TimeSeries = {

    val keys: SortedSet[LocalDate] = base.keySet.intersect(other.keySet)

    val equalDate = keys.to(targetEqualDate).lastOption.getOrElse(keys.head)

    def baseRatio = other(equalDate) / base(equalDate)

    val builder = SortedMap.newBuilder[LocalDate, Double]

    for (date <- keys) {
      val ratio = (other(date) / base(date)) / baseRatio
      builder += date -> ratio
    }

    builder.result()
  }


}
