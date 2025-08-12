package controllers

import model.LargestNumberResult

import javax.inject._
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import services.PdfService

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents, pdfService: PdfService) extends BaseController with Logging {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def largestNumber() = Action { implicit request: Request[AnyContent] =>
    val fileName = request.getQueryString("file")
    if (fileName.isEmpty) {
      logger.error("No file name provided in the request.")
      BadRequest("File name is required.")
    }
    val largestNumber = pdfService.readPdf(fileName.get)
    largestNumber.fold(
      error => {
        logger.error("Error reading PDF: " + error.getMessage)
        InternalServerError("Failed to read PDF file.")
      },
      number => if (number.isEmpty) {
        NotFound("No numbers found in the PDF.")
      } else {
        val result = LargestNumberResult(fileName.get, number)
        val resultJson = Json.toJson(result)
        Ok(resultJson)
      }
    )
  }
}
