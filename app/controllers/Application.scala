package controllers

import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.{Json, JsArray}
import service.Loader
import scala.collection.SortedMap
import org.joda.time.LocalDate

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready!!!"))
  }

  def priceHistory(ticker: String) = Action {

    val url = s"http://ichart.finance.yahoo.com/table.csv?s=$ticker"

    Async {

      Loader.load(ticker)
        .map {
        data =>
          timeSeriesResult(data)
      }.recover {
        case _ => NotFound
      }
    }
  }

  def compare(baseTicker: String, otherTicker: String) = Action {
    Async {
      Loader.compare(baseTicker, otherTicker)
        .map(timeSeriesCsv)
        .recover {
        case _ => NotFound
      }
    }

  }


  def timeSeriesResult(data: SortedMap[LocalDate, Double]): Result = {

    val seq = for ((date, value) <- data.toSeq) yield Json.obj("d" -> date, "v" -> value)

    Ok(JsArray(seq))
  }

  def timeSeriesCsv(data: SortedMap[LocalDate, Double]): Result = {
    var builder = StringBuilder.newBuilder
    for ((date, value) <- data.toSeq) {
      builder.append(date)
      builder.append(",")
      builder.append(value)
      builder.append("\n")
    }
    Ok(builder.result())
  }


  def csvToJson(data: String): JsArray = {
    val lineIt = data.split('\n').iterator

    val headers = lineIt.next().split(',')

    val dateIdx = headers.indexOf("Date")
    val adjCloseIdx = headers.indexOf("Adj Close")


    val result = for (line <- lineIt) yield {
      val cols = line.split(',')
      val date = cols(dateIdx)
      val adjClose = cols(adjCloseIdx)
      Json.obj("date" -> date, "adjClose" -> adjClose)
    }

    JsArray(result.toSeq)
  }

}