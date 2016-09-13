package email

import models.{BusinessDetailsModel, ConfirmFormModel, EligibilityModel}
import play.twirl.api.HtmlFormat
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceSendRequest

trait RetainEmailService {

  def emailRequest(emailAddress: String,
                   vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                   eligibilityModel: EligibilityModel,
                   emailData: EmailData,
                   confirmFormModel: Option[ConfirmFormModel],
                   businessDetailsModel: Option[BusinessDetailsModel],
                   emailFlags: EmailFlags,
                   trackingId: TrackingId): Option[EmailServiceSendRequest]

  def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                  eligibilityModel: EligibilityModel,
                  emailData: EmailData,
                  confirmFormModel: Option[ConfirmFormModel],
                  businessDetailsModel: Option[BusinessDetailsModel],
                  emailFlags: EmailFlags): HtmlFormat.Appendable
}
case class EmailFlags(sendPdf: Boolean, isKeeper: Boolean)
case class EmailData(certificateNumber: String, transactionTimestamp: String, transactionId: String)
