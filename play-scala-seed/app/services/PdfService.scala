package services

import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.RandomAccessReadBufferedFile
import org.apache.pdfbox.text.PDFTextStripper
import play.api.{Application, Logging}

import java.util.Scanner
import java.util.regex.Pattern
import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class PdfService @Inject() (app: Application) extends Logging {
  def readPdf(): Try[Option[Long]] = {
    val tryDocument = Try {
      Loader.loadPDF(new RandomAccessReadBufferedFile(app.path + "/app/pdf_files/FY25_Air_Force_Working_Capital_Fund.pdf"))
    }
    tryDocument.map { document =>
      val text = new PDFTextStripper().getText(document)
      val scanner = new Scanner(text)
      var largestNumber: Option[Long] = None
      while (scanner.hasNextLine) {
        val numbersInLine = Pattern.compile("\\D+")
          .split(scanner.nextLine())
          .toList
          .filter(s => !s.isBlank)
          .map(_.toInt)
        if (numbersInLine.nonEmpty) {
          val largestNumberInLine = numbersInLine.max
          if (largestNumber.isEmpty || largestNumberInLine > largestNumber.get) {
            largestNumber = Some(largestNumberInLine)
          }
        }
      }
      largestNumber
    }
  }
}