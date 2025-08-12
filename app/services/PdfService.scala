package services

import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.RandomAccessReadBufferedFile
import org.apache.pdfbox.text.PDFTextStripper
import play.api.{Application, Logging}

import java.util.Scanner
import javax.inject.{Inject, Singleton}
import scala.util.{Success, Try}

@Singleton
class PdfService @Inject() (app: Application) extends Logging {

  private val cache = scala.collection.mutable.Map[String, Option[Double]]()


  def getLargestNumberFromPdfFile(fileName: String): Try[Option[Double]] = {
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
      var largestNumber: Option[Double] = None

      while (scanner.hasNextLine) {
        val line = scanner.nextLine()
        val lineNumbers = NumberParser.parseAllNumbers(line)

        if (lineNumbers.nonEmpty) {
          val largestNumberInLine = lineNumbers.max
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