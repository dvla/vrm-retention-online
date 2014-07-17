package models.domain.vrm_retention

final case class EnterAddressManuallyViewModel(registrationNumber: String,
                                               vehicleMake: String,
                                               vehicleModel: String,
                                               businessName: String,
                                               businessPostCode: String)