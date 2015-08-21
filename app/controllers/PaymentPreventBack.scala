package controllers

import com.google.inject.Inject
import play.api.mvc.Action
import play.api.mvc.Controller
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class PaymentPreventBack @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                           config: Config,
                                           dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService
                                          ) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.payment_prevent_back())
  }

  def returnToSuccess = Action { implicit request =>
    Redirect(routes.SuccessPayment.present())
  }
}
