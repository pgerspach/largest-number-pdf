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
      // Extract text from the PDF document
      val text = new PDFTextStripper().getText(document)
      val lines = text.split("\n").toList

      // Use table-aware parsing to handle multipliers in headers
      val largestNumber = NumberParser.findLargestNumberWithTableContext(lines)

      cache(fileName) = largestNumber
      largestNumber
    }
  }
}
