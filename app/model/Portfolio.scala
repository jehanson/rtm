package model

case class Portfolio(tickerWeights: Map[String, Double]) {
  val total = tickerWeights.values.sum
}

object Portfolio {
  def parse(str: String): Portfolio = {
    val result = for(part <- str.replaceAll("~",",").split(',')) yield {
      var keys = part.split('*')
      (keys(0), if (keys.length > 1) keys(1).toDouble else 1)
    }

    Portfolio(result.toMap)
  }
}