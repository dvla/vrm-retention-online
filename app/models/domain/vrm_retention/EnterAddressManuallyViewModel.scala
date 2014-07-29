package models.domain.vrm_retention

final case class EnterAddressManuallyViewModel(registrationNumber: String,
                                               vehicleMake: Option[String],
                                               vehicleModel: Option[String],
                                               businessName: String,
                                               businessContact: String,
                                               businessPostCode: String)