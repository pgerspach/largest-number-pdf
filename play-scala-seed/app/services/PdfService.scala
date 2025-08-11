package services

import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.RandomAccessReadBufferedFile
import org.apache.pdfbox.text.PDFTextStripper
import play.api.{Application, Logging}

import java.util.Scanner
import java.util.regex.Pattern
import javax.inject.{Inject, Singleton}
import scala.util.Try

import scala.jdk.CollectionConverters._

@Singleton
class PdfService @Inject() (app: Application) extends Logging {
  def readPdf() : Unit = {
    val tryDocument = Try {
      Loader.loadPDF(new RandomAccessReadBufferedFile(app.path + "/app/pdf_files/FY25_Air_Force_Working_Capital_Fund.pdf"))
    }
    if (tryDocument.isFailure) {
      logger.error("Failed to load PDF: " + tryDocument.failed.get.getMessage)
      return
    }
    val document = tryDocument.get
    val text = new PDFTextStripper().getText(document)
    val scanner = new Scanner(text)
    var largestNumber = Long.MinValue
    while (scanner.hasNextLine) {
      val numbersInLine = Pattern.compile("\\D+")
        .split(scanner.nextLine())
        .toList
        .filter(s => !s.isBlank)
        .map(_.toInt)
      if(numbersInLine.nonEmpty) {
        val largestNumberInLine = numbersInLine.max
        if (largestNumberInLine > largestNumber) {
          largestNumber = largestNumberInLine
        }
      }
    }
    logger.error(s"Largest number found in PDF: $largestNumber")
  }
}