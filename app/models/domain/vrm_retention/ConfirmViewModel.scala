package models.domain.vrm_retention

final case class ConfirmViewModel(registrationNumber: String,
                                  vehicleMake: String,
                                  vehicleModel: String,
                                  keeperTitle: String,
                                  keeperFirstName: String,
                                  keeperLastName: String,
                                  keeperAddressLine1: String,
                                  keeperAddressLine2: String,
                                  keeperAddressLine3: String,
                                  keeperAddressLine4: String,
                                  keeperPostTown: String,
                                  keeperPostCode: String)
// ,
//                                  businessName: Option[String],
//                                  businessAddressLine1: Option[String],
//                                  businessAddressLine2: Option[String],
//                                  businessAddressLine3: Option[String],
//                                  businessAddressLine4: Option[String],
//                                  businessPostTown: Option[String],
//                                  businessPostCode: Option[String])
