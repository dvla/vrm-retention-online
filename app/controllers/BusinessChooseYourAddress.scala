package controllers

import javax.inject.Inject
import views.vrm_retention.{EnterAddressManually, BusinessChooseYourAddress}
import BusinessChooseYourAddress.AddressSelectId
import EnterAddressManually.EnterAddressManuallyCacheKey
import viewmodels.{BusinessChooseYourAddressFormModel, BusinessChooseYourAddressViewModel, BusinessDetailsModel, SetupBusinessDetailsFormModel, VehicleAndKeeperDetailsModel}
import play.api.data.{Form, FormError}
import play.api.i18n.Lang
import play.api.mvc.{Action, Controller, Request}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichSimpleResult}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClientSideSession, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.vrm_retention.business_choose_your_address
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupService

final class BusinessChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                               (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                config: Config) extends Controller {

  private[controllers] val form = Form(BusinessChooseYourAddressFormModel.Form.Mapping)

  def present = Action.async { implicit request =>
    (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
      case (Some(setupBusinessDetailsFormModel), Some(vehicleAndKeeperDetailsModel)) =>
        val businessChooseYourAddressViewModel = BusinessChooseYourAddressViewModel(setupBusinessDetailsFormModel, vehicleAndKeeperDetailsModel)
        val session = clientSideSessionFactory.getSession(request.cookies)
        fetchAddresses(setupBusinessDetailsFormModel)(session, lang).map { addresses =>
          Ok(views.html.vrm_retention.business_choose_your_address(businessChooseYourAddressViewModel, form.fill(),
            addresses))
        }
      case _ => Future {
        Redirect(routes.SetUpBusinessDetails.present())
      }
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
          case (Some(setupBusinessDetailsFormModel), Some(vehicleAndKeeperDetailsModel)) =>
            val businessChooseYourAddressViewModel = BusinessChooseYourAddressViewModel(setupBusinessDetailsFormModel, vehicleAndKeeperDetailsModel)
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            fetchAddresses(setupBusinessDetailsFormModel).map { addresses =>
              BadRequest(business_choose_your_address(businessChooseYourAddressViewModel,
                formWithReplacedErrors(invalidForm),
                addresses)
              )
            }
          case _ => Future {
            Redirect(routes.SetUpBusinessDetails.present())
          }
        },
      validForm =>
        request.cookies.getModel[SetupBusinessDetailsFormModel] match {
          case Some(setupBusinessDetailsFormModel) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            lookupUprn(validForm,
              setupBusinessDetailsFormModel.businessName,
              setupBusinessDetailsFormModel.businessContact,
              setupBusinessDetailsFormModel.businessEmail)
          case None => Future {
            Redirect(routes.SetUpBusinessDetails.present())
          }
        }
    )
  }

  private def formWithReplacedErrors(form: Form[BusinessChooseYourAddressFormModel])(implicit request: Request[_]) =
    form.replaceError(AddressSelectId, "error.required",
      FormError(key = AddressSelectId,
        message = "vrm_retention_businessChooseYourAddress.address.required", args = Seq.empty)).
      distinctErrors

  private def fetchAddresses(model: SetupBusinessDetailsFormModel)(implicit session: ClientSideSession, lang: Lang) =
    addressLookupService.fetchAddressesForPostcode(model.businessPostcode, session.trackingId)

  private def lookupUprn(model: BusinessChooseYourAddressFormModel, businessName: String, businessContact: String, businessEmail: String)
                        (implicit request: Request[_], session: ClientSideSession) = {
    val lookedUpAddress = addressLookupService.fetchAddressForUprn(model.uprnSelected.toString, session.trackingId)
    lookedUpAddress.map {
      case Some(addressViewModel) =>
        val businessDetailsModel = BusinessDetailsModel(businessName = businessName,
          businessContact = businessContact,
          businessEmail = businessEmail,
          businessAddress = addressViewModel.formatPostcode)
        /* The redirect is done as the final step within the map so that:
         1) we are not blocking threads
         2) the browser does not change page before the future has completed and written to the cache. */
        Redirect(routes.Confirm.present()).
          discardingCookie(EnterAddressManuallyCacheKey).
          withCookie(model).
          withCookie(businessDetailsModel)
      case None => Redirect(routes.UprnNotFound.present())
    }
  }
}