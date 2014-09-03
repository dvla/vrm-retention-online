package email

import play.twirl.api.HtmlFormat
import viewmodels.{EligibilityModel, RetainModel, VehicleAndKeeperDetailsModel}

trait EmailService {

  def sendEmail(emailAddress: String,
                vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                eligibilityModel: EligibilityModel,
                retainModel: RetainModel,
                transactionId: String)

  def populateEmailTemplate(emailAddress: String,
                            vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                            eligibilityModel: EligibilityModel,
                            retainModel: RetainModel,
                            transactionId: String,
                            crownContentId: String): HtmlFormat.Appendable
}