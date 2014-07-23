package models.domain.vrm_retention

final case class BusinessChooseYourAddressViewModel(registrationNumber: String,
                                                    vehicleMake: String,
                                                    vehicleModel: String,
                                                    businessName: String,
                                                    businessContact: String,
                                                    businessEmail: String,
                                                    businessPostCode: String)