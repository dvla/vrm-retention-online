package models.domain.vrm_retention

final case class VehicleLookupFailureViewModel(registrationNumber: String,
                                               vehicleMake: Option[String],
                                               vehicleModel: Option[String])