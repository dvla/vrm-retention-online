package controllers

import com.google.inject.Inject
import play.api.data.Form
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, Call, Controller}
import uk.gov.dvla.vehicles.presentation.common
import utils.helpers.Config
import webserviceclients.emailservice.EmailService
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.controllers.FeedbackBase
import common.model.FeedbackForm
import common.model.FeedbackForm.Form.emailMapping
import common.model.FeedbackForm.Form.feedback
import common.model.FeedbackForm.Form.nameMapping
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding

class FeedbackController @Inject()(val emailService: EmailService)
                                  (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                   config: Config,
                                   dateService: DateService) extends Controller with FeedbackBase {

  override val emailConfiguration = config.emailConfiguration

  private[controllers] val form = Form(
    FeedbackForm.Form.Mapping
  )

  implicit val controls: Map[String, Call] = Map(
    "submit" -> controllers.routes.FeedbackController.submit()
  )

  def present() = Action { implicit request =>
    Ok(views.html.vrm_retention.feedback(form))
  }

  def submit: Action[AnyContent] = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => BadRequest(views.html.vrm_retention.feedback(formWithReplacedErrors(invalidForm))),
      validForm => {
        val trackingId = request.cookies.trackingId
        sendFeedback(validForm, Messages("common_feedback.subject"), trackingId)
        Ok(views.html.vrm_retention.feedbackSuccess())
      }
    )
  }

  private def formWithReplacedErrors(form: Form[FeedbackForm]) =
    form.replaceError(
      feedback, FormError(key = feedback, message = "error.feedback", args = Seq.empty)
    ).replaceError(
      nameMapping, FormError(key = nameMapping, message = "error.feedbackName", args = Seq.empty)
    ).replaceError(
      emailMapping, FormError(key = emailMapping, message = "error.email", args = Seq.empty)
    ).distinctErrors
}