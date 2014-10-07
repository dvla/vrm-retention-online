package controllers

import com.google.inject.Inject
import models._
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import utils.helpers.Config

final class ConfirmBusiness @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                      config: Config) extends Controller {

  def present = Action { implicit request =>
    val happyPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
      businessDetails <- request.cookies.getModel[BusinessDetailsModel]
    } yield {
      Ok("Hello, world")
    }
    val sadPath = Redirect(routes.VehicleLookup.present())
    happyPath.getOrElse(sadPath)
  }
}