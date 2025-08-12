package services

import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.RandomAccessReadBufferedFile
import org.apache.pdfbox.text.PDFTextStripper
import play.api.{Application, Logging}

import java.util.Scanner
import java.util.regex.Pattern
import javax.inject.{Inject, Singleton}
import scala.util.{Success, Try}
import scala.util.matching.Regex

@Singleton
class PdfService @Inject() (app: Application) extends Logging {

  private val cache = scala.collection.mutable.Map[String, Option[Long]]()

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

  private def parseNumberWithMultiplier(text: String): List[Long] = {
    numberWithMultiplier.findAllIn(text.toLowerCase).matchData.map { m =>
      val number = m.group(1).toDouble
      val multiplier = multipliers(m.group(2))
      (number * multiplier).toLong
    }.toList
  }

  def readPdf(fileName: String): Try[Option[Long]] = {
    // Check if the result is already cached
    cache.get(fileName) match {
      case Some(result) => return Success(result)
      case None => // Continue to read the PDF
    }
    val tryDocument = Try {
      Loader.loadPDF(new RandomAccessReadBufferedFile(app.path + s"/app/pdf_files/${fileName}"))
    }
    tryDocument.map { document =>
      val text = new PDFTextStripper().getText(document)
      val scanner = new Scanner(text)
      var largestNumber: Option[Long] = None

      while (scanner.hasNextLine) {
        val line = scanner.nextLine()

        // Parse numbers with text multipliers (e.g., "2.5 million")
        val numbersWithMultipliers = parseNumberWithMultiplier(line)

        // Parse regular numbers
        val regularNumbers = Pattern.compile("\\D+")
          .split(line)
          .toList
          .filter(s => !s.isBlank)
          .flatMap(s => Try(s.toLong).toOption)

        val allNumbers = numbersWithMultipliers ++ regularNumbers

        if (allNumbers.nonEmpty) {
          val largestNumberInLine = allNumbers.max
          if (largestNumber.isEmpty || largestNumberInLine > largestNumber.get) {
            largestNumber = Some(largestNumberInLine)
          }
        }
      }
      scanner.close()
      cache(fileName) = largestNumber
      largestNumber
    }
  }
}