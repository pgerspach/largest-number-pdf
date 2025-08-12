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

  private val numberWithMultiplier: Regex = raw"(\d{1,3}(?:,\d{3})*(?:\.\d+)?|\d+(?:\.\d+)?)\s*(thousand|million|billion|trillion|k|m|b|t)\b".r
  private val regularNumberPattern: Regex = raw"(?<!\d)(\d{1,3}(?:,\d{3})*(?:\.\d+)?|\d+(?:\.\d+)?)(?!\d)".r

  def parseNumbersWithMultipliers(text: String): List[Double] = {
    numberWithMultiplier.findAllIn(text.toLowerCase).matchData.map { m =>
      val number = m.group(1).replace(",", "").toDouble
      val multiplier = multipliers(m.group(2))
      (number * multiplier)
    }.toList
  }

  def parseRegularNumbers(text: String): List[Double] = {
    regularNumberPattern.findAllIn(text).matchData.map { m =>
      m.group(1).replace(",", "").toDouble
    }.toList
  }

  def detectTableMultiplier(text: String): Option[Long] = {
    val lowerText = text.toLowerCase

    // Check for explicit patterns
    if (lowerText.contains("(in millions)") || lowerText.contains("(millions)")) Some(1000000L)
    else if (lowerText.contains("(in thousands)") || lowerText.contains("(thousands)")) Some(1000L)
    else if (lowerText.contains("(in billions)") || lowerText.contains("(billions)")) Some(1000000000L)
    else if (lowerText.contains("(in trillions)") || lowerText.contains("(trillions)")) Some(1000000000000L)
    // Check for currency abbreviations
    else if (lowerText.contains("($m)") || lowerText.contains("(m)")) Some(1000000L)
    else if (lowerText.contains("($k)") || lowerText.contains("(k)")) Some(1000L)
    else if (lowerText.contains("($b)") || lowerText.contains("(b)")) Some(1000000000L)
    else if (lowerText.contains("($t)") || lowerText.contains("(t)")) Some(1000000000000L)
    else None
  }

  def parseTableNumbers(text: String, tableMultiplier: Long): List[Double] = {
    regularNumberPattern.findAllIn(text).matchData.map { m =>
      val number = m.group(1).replace(",", "").toDouble
      number * tableMultiplier
    }.toList
  }

  def parseAllNumbers(text: String): List[Double] = {
    val numbersWithMultipliers = parseNumbersWithMultipliers(text)
    val regularNumbers = parseRegularNumbers(text)
    numbersWithMultipliers ++ regularNumbers
  }

  def parseAllNumbersWithTableContext(lines: List[String]): List[Double] = {
    var currentTableMultiplier: Option[Long] = None
    val allNumbers = scala.collection.mutable.ListBuffer[Double]()

    lines.foreach { line =>
      // Reset table context on empty lines FIRST
      if (line.trim.isEmpty) {
        currentTableMultiplier = None
      } else {
        // Check if this line contains a table header with multiplier
        detectTableMultiplier(line) match {
          case Some(multiplier) =>
            currentTableMultiplier = Some(multiplier)
            // Also parse any numbers in the header line itself
            allNumbers ++= parseAllNumbers(line)
          case None =>
            // Parse numbers based on current context
            currentTableMultiplier match {
              case Some(multiplier) =>
                // In table context - check if line has explicit multipliers first
                val explicitNumbers = parseNumbersWithMultipliers(line)
                if (explicitNumbers.nonEmpty) {
                  allNumbers ++= explicitNumbers
                } else {
                  // Apply table multiplier to bare numbers
                  allNumbers ++= parseTableNumbers(line, multiplier)
                }
              case None =>
                // Normal parsing outside table context
                allNumbers ++= parseAllNumbers(line)
            }
        }
      }
    }

    allNumbers.toList
  }

  def findLargestNumber(text: String): Option[Double] = {
    val allNumbers = parseAllNumbers(text)
    if (allNumbers.nonEmpty) Some(allNumbers.max) else None
  }

  def findLargestNumberWithTableContext(lines: List[String]): Option[Double] = {
    val allNumbers = parseAllNumbersWithTableContext(lines)
    if (allNumbers.nonEmpty) Some(allNumbers.max) else None
  }
}
