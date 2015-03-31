package controllers

import com.google.inject.Inject
import play.api.data.Form
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.controllers.FeedbackBase
import uk.gov.dvla.vehicles.presentation.common.model.FeedbackForm
import uk.gov.dvla.vehicles.presentation.common.model.FeedbackForm.Form.emailMapping
import uk.gov.dvla.vehicles.presentation.common.model.FeedbackForm.Form.feedback
import uk.gov.dvla.vehicles.presentation.common.model.FeedbackForm.Form.nameMapping
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import webserviceclients.emailservice.EmailService

class FeedbackController @Inject()(val emailService: EmailService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
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

  private def formWithReplacedErrors(form: Form[FeedbackForm]) = {
    form.replaceError(
      feedback, FormError(key = feedback, message = "error.feedback", args = Seq.empty)
    ).replaceError(
        nameMapping, FormError(key = nameMapping, message = "error.feedbackName", args = Seq.empty)
      ).replaceError(
        emailMapping, FormError(key = emailMapping, message = "error.email", args = Seq.empty)
      ).distinctErrors
  }
}