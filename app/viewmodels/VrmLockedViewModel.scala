package viewmodels

final case class VrmLockedViewModel(registrationNumber: String,
                                    vehicleMake: Option[String],
                                    vehicleModel: Option[String])