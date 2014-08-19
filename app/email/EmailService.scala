package email

import viewmodels.{VehicleAndKeeperDetailsModel, EligibilityModel, RetainModel}

trait EmailService {

  def sendEmail(emailAddress: String,
                vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                eligibilityModel: EligibilityModel,
                retainModel: RetainModel)
}