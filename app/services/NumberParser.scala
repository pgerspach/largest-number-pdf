package services

import scala.util.matching.Regex

object NumberParser {
  
  private val multipliers = Map(
    "thousand" -> 1000L,
    "million" -> 1000000L,
    "billion" -> 1000000000L,
    "trillion" -> 1000000000000L,
    "k" -> 1000L,
    "m" -> 1000000L,
    "b" -> 1000000000L,
    "t" -> 1000000000000L
  )

  private val numberWithMultiplier: Regex = raw"(\d+(?:\.\d+)?)\s*(thousand|million|billion|trillion|k|m|b|t)\b".r
  private val regularNumberPattern: Regex = raw"(\d+(?:\.\d+)?)".r

  def parseNumbersWithMultipliers(text: String): List[Double] = {
    numberWithMultiplier.findAllIn(text.toLowerCase).matchData.map { m =>
      val number = m.group(1).toDouble
      val multiplier = multipliers(m.group(2))
      (number * multiplier)
    }.toList
  }

  def parseRegularNumbers(text: String): List[Double] = {
    regularNumberPattern.findAllIn(text).matchData.map { m =>
      m.group(1).toDouble
    }.toList
  }

  def parseAllNumbers(text: String): List[Double] = {
    val numbersWithMultipliers = parseNumbersWithMultipliers(text)
    val regularNumbers = parseRegularNumbers(text)
    numbersWithMultipliers ++ regularNumbers
  }

  def findLargestNumber(text: String): Option[Double] = {
    val allNumbers = parseAllNumbers(text)
    if (allNumbers.nonEmpty) Some(allNumbers.max) else None
  }
}