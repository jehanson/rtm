package controllers

import play.api.mvc._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready!!!"))
  }

  def priceHistory(ticker: String) = Action {

    val url = s"http://ichart.finance.yahoo.com/table.csv?s=$ticker"

    Async {
      WS.url(url).get().map {
        response =>
          response.status match {
            case 200 => Ok(response.body)
            case _ => NotFound
          }
      }
    }
  }

}