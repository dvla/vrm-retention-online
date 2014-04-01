package modules

import app.ConfigProperties._
import com.tzavellas.sse.guice.ScalaModule
import play.api.Logger
import services._
import services.address_lookup.{AddressLookupWebService, AddressLookupService}
import services.vehicle_lookup.{VehicleLookupServiceImpl, VehicleLookupService, VehicleLookupWebServiceImpl, VehicleLookupWebService}
import services.dispose_service.{DisposeWebServiceImpl, DisposeWebService, DisposeServiceImpl, DisposeService}

/**
 * Provides real implementations of traits
 * Note the use of sse-guice, which is a library that makes the Guice internal DSL more scala friendly
 * eg we can write this:
 * bind[Service].to[ServiceImpl].in[Singleton]
 * instead of this:
 * bind(classOf[Service]).to(classOf[ServiceImpl]).in(classOf[Singleton])
 *
 * Look in build.scala for where we import the sse-guice library
 */
object DevModule extends ScalaModule {
  def configure() {
    Logger.debug("Guice is loading DevModule")

    getProperty("addressLookupService.type", "ordnanceSurvey") match {
      case "ordnanceSurvey" => ordnanceSurveyAddressLookup()
      case _ => gdsAddressLookup()
    }
    bind[VehicleLookupWebService].to[VehicleLookupWebServiceImpl].asEagerSingleton()
    bind[VehicleLookupService].to[VehicleLookupServiceImpl].asEagerSingleton()
    bind[DisposeWebService].to[DisposeWebServiceImpl].asEagerSingleton()
    bind[DisposeService].to[DisposeServiceImpl].asEagerSingleton()
  }

  private def ordnanceSurveyAddressLookup() = {
    Logger.debug("IoC ordnance survey address lookup service")
    bind[AddressLookupService].to[services.address_lookup.ordnance_survey.AddressLookupServiceImpl].asEagerSingleton()
    bind[AddressLookupWebService].to[services.address_lookup.ordnance_survey.WebServiceImpl].asEagerSingleton()
  }

  private def gdsAddressLookup() = {
    Logger.debug("IoC gds address lookup service")
    bind[AddressLookupService].to[services.address_lookup.gds.AddressLookupServiceImpl].asEagerSingleton()
    bind[AddressLookupWebService].to[services.address_lookup.gds.WebServiceImpl].asEagerSingleton()
  }
}