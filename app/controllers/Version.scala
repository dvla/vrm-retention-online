package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.controllers.Version.Suffix
import common.webserviceclients.addresslookup.ordnanceservey.OrdnanceSurveyConfig
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupConfig
import utils.helpers.Config

class Version @Inject()(//vehicleAndKeeperConfig: VehicleAndKeeperLookupConfig,
//                        osAddressLookupConfig: OrdnanceSurveyConfig,
                        config: Config) extends common.controllers.Version(
  config.auditMicroServiceUrlBase + Suffix,
  config.emailServiceMicroServiceUrlBase + Suffix,
//  osAddressLookupConfig.baseUrl + Suffix,
  config.paymentSolveMicroServiceUrlBase + Suffix,
//  vehicleAndKeeperConfig.vehicleAndKeeperLookupMicroServiceBaseUrl + Suffix,
  config.vrmRetentionEligibilityMicroServiceUrlBase + Suffix,
  config.vrmRetentionRetainMicroServiceUrlBase + Suffix
)