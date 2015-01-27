package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._

class Config2Impl extends Config2 {

  val isCsrfPreventionEnabled = getProperty[Boolean]("csrf.prevention")
}
