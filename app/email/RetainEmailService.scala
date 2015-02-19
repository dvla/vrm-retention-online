package email

import models._
import org.apache.commons.mail.HtmlEmail
import play.twirl.api.HtmlFormat
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

trait RetainEmailService {

  def sendEmail(emailAddress: String,
                vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                eligibilityModel: EligibilityModel,
                retainModel: RetainModel,
                transactionId: String,
                confirmFormModel: Option[ConfirmFormModel],
                businessDetailsModel: Option[BusinessDetailsModel],
                isKeeper: Boolean,
                trackingId: String)

  def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                  eligibilityModel: EligibilityModel,
                  retainModel: RetainModel,
                  transactionId: String,
                  confirmFormModel: Option[ConfirmFormModel],
                  businessDetailsModel: Option[BusinessDetailsModel],
                  isKeeper: Boolean): HtmlFormat.Appendable
}