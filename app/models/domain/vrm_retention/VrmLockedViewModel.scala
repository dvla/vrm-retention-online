package models.domain.vrm_retention

final case class VrmLockedViewModel(registrationNumber: String,
                                               vehicleMake: Option[String],
                                               vehicleModel: Option[String])