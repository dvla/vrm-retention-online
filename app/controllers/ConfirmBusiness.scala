package controllers

import com.google.inject.Inject
import models._
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import utils.helpers.Config
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.VehicleLookup.UserType_Business

final class ConfirmBusiness @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                      config: Config) extends Controller {

  def present = Action { implicit request =>
    val happyPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
      businessDetails <- request.cookies.getModel[BusinessDetailsModel]
    } yield {
      val isBusinessUser = vehicleAndKeeperLookupForm.userType == UserType_Business
      val verifiedBusinessDetails = request.cookies.getModel[BusinessDetailsModel].filter(o => isBusinessUser)
      val viewModel = ConfirmBusinessViewModel(vehicleAndKeeper, verifiedBusinessDetails)
      Ok(views.html.vrm_retention.confirm_business(viewModel))
    }
    val sadPath = Redirect(routes.VehicleLookup.present())
    happyPath.getOrElse(sadPath)
  }

  def exit = Action { implicit request =>
    Redirect(routes.MockFeedback.present()).
      discardingCookies(RelatedCacheKeys.RetainSet)
  }
}