package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.controllers
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.OrdnanceSurveyConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupConfig
import utils.helpers.Config

class Version @Inject()(vehiclesKeeperConfig: VehicleAndKeeperLookupConfig,
                        osAddressLookupConfig: OrdnanceSurveyConfig,
                        config: Config)
  extends controllers.Version(
    osAddressLookupConfig.baseUrl + "/version",
    vehiclesKeeperConfig.vehicleAndKeeperLookupMicroServiceBaseUrl + "/version",
    //    config.paymentSolveMicroServiceUrlBase + "/version",
    config.vrmRetentionEligibilityMicroServiceUrlBase + "/version",
    config.vrmRetentionRetainMicroServiceUrlBase + "/version"
  )
