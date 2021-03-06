package uk.gov.dvla.vehicles.retain.gatling

import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.http.Predef._
import Headers.{headers_accept_html, headers_x_www_form_urlencoded}

class Chains(data: RecordSeqFeederBuilder[String]) {

  private val beforeYouStartPageTitle = "Take a registration number off a vehicle"
  private val vehicleLookupPageTitle = "Enter details"
  private val setupBusinessDetailsPageTitle = "Provide your business details"
  private val businessChooseYourAddressPageTitle = "Select your business address"
  private val confirmPageTitle = "Confirm keeper details"
  private val paymentPageTitle = "Payment"
  private val fullscreenSolvePaymentPageTitle = "Complete your order - the-logic-group.com Checkout"
  private val confirmBusinessPageTitle = "Confirm your business details"
  private val successPageTitle = "Summary"
  private val vehicleLookupFailurePageTitle = "Unable to find vehicle record"
  private val vehicleLookupToDirectToPaperTitle = "This registration number cannot be retained online"
  private val vehicleLookupToNotEligibleToTransactPageTitle = "This registration number cannot be retained"

  def assetsAreAccessible =
    exec(
      http("apple-touch-icon-57x57.png")
        .get( s"""/assets/lib/vehicles-presentation-common/images/apple-touch-icon-57x57.png""")
    )
      .exec(
        http("apple-touch-icon-72x72.png")
          .get( s"""/assets/lib/vehicles-presentation-common/images/apple-touch-icon-72x72.png""")
      )
      .exec(
        http("apple-touch-icon-76x76.png")
          .get( s"""/assets/lib/vehicles-presentation-common/images/apple-touch-icon-76x76.png""")
      )
      .exec(
        http("apple-touch-icon-114x114.png")
          .get( s"""/assets/lib/vehicles-presentation-common/images/apple-touch-icon-114x114.png""")
      )
      .exec(
        http("apple-touch-icon-120x120.png")
          .get( s"""/assets/lib/vehicles-presentation-common/images/apple-touch-icon-120x120.png""")
      )
      .exec(
        http("apple-touch-icon-144x144.png")
          .get( s"""/assets/lib/vehicles-presentation-common/images/apple-touch-icon-144x144.png""")
      )
      .exec(
        http("apple-touch-icon-144x144.png")
          .get( s"""/assets/lib/vehicles-presentation-common/images/apple-touch-icon-152x152.png""")
      )
      .exec(
        http("favicon.ico")
          .get( s"""/assets/lib/vehicles-presentation-common/images/favicon.ico""")
      )
      .exec(
        http("gov-uk-email.jpg")
          .get( s"""/assets/images/gov-uk-email.jpg""")
      )
      .exec(
        http("govuk-crest.png")
          .get( s"""/assets/lib/vehicles-presentation-common/images/govuk-crest.png""")
          .headers(Map(
          """Accept""" -> """image/png,image/*;q=0.8,*/*;q=0.5""",
          """If-Modified-Since""" -> """Thu, 22 May 2014 14:25:18 GMT""",
          """If-None-Match""" -> """0464ba08d53d88645ca77f9907c082c8c10d563b"""))
      )
      .exec(
        http("pdf-icon-1.png")
          .get( s"""/assets/images/pdf-icon-1.png""")
      )

      /* javascript */
      .exec(
        http("common.js")
          .get( s"""/assets/lib/vehicles-presentation-common/javascripts/common.js""")
          .headers(Map(
          """Accept""" -> """*/*""",
          """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:10:42 GMT""",
          """If-None-Match""" -> """5f859f72e7cc426915cf32f2643ee5fc494b04a8"""))
      )

      .exec(
        http("jquery.min.js")
          .get( s"""/assets/lib/jquery/jquery.min.js""")
          .headers(Map(
          """Accept""" -> """*/*""",
          """If-Modified-Since""" -> """Tue, 06 Aug 2013 09:49:32 GMT""",
          """If-None-Match""" -> """858bab5a8e8f73a1d706221ed772a4f740e168d5"""))
      )


      /* css */
      .exec(
        http("print.min.css")
          .get( s"""/assets/print.min.css""")
          .headers(Map(
          """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:08:08 GMT""",
          """If-None-Match""" -> """b2b112249c52769ac41acd83e388f550e4c39c6f"""))
      )
      .exec(
        http("screen.min.css")
          .get( s"""/assets/screen.min.css""")
          .headers(Map(
          """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:08:06 GMT""",
          """If-None-Match""" -> """59f34576dba4629e6e960e1d514fe573775e9999"""))
      )

  def beforeYouStart =
    exitBlockOnFail(
      exec(
        http("beforeYouStart")
          .get(s"/before-you-start")
          .headers(headers_accept_html)
          // Assertions
          .check(status.is(200))
          .check(regex(beforeYouStartPageTitle).exists) // Page title
      )
    )

  def beforeYouStartToVehicleLookup =
    exitBlockOnFail(
      exec(
        http("beforeYouStartToVehicleLookup")
          .get(s"/vehicle-lookup") // Gatlin doesn't click on links, you need to go to the next page.
          .headers(headers_accept_html)
          .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
          // Assertions
          .check(status.is(200))
          .check(regex(vehicleLookupPageTitle).exists) // Page title
      )
    )

  def vehicleLookupToConfirm =
    exitBlockOnFail(
      feed(data)
        .exec(
          http("vehicleLookupToConfirm")
            .post(s"/vehicle-lookup")
            .headers(headers_x_www_form_urlencoded)
            .formParam("vehicle-registration-number", "${vehicleRegistrationNumber}")
            .formParam("document-reference-number", "${documentReferenceNumber}")
            .formParam("postcode", "${postcode}")
            .formParam("keeper-consent", "${keeperConsent}")
            .formParam("csrf_prevention_token", "${csrf_prevention_token}")
            .formParam("action", "")
            .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
            // Assertions
            .check(status.is(200))
            .check(regex(confirmPageTitle).exists) // Page title
            .check(regex("${expected_registrationNumberFormatted}").exists)
            .check(regex("${expected_addressLine1}").exists)
            .check(regex("${expected_addressLine2}").exists)
            .check(regex("${expected_addressLine3}").exists)
            .check(regex("${expected_postTown}").exists)
            .check(regex("${expected_postcodeFormatted}").exists)
            .check(regex("${expected_make}").exists)
            .check(regex("${expected_model}").exists)
        )
    )

  def vehicleLookupToSetupBusinessDetails =
    exitBlockOnFail(
      feed(data)
        .exec(
          http("vehicleLookupToSetupBusinessDetails")
            .post(s"/vehicle-lookup")
            .headers(headers_x_www_form_urlencoded)
            .formParam("vehicle-registration-number", "${vehicleRegistrationNumber}")
            .formParam("document-reference-number", "${documentReferenceNumber}")
            .formParam("postcode", "${postcode}")
            .formParam("keeper-consent", "${keeperConsent}")
            .formParam("csrf_prevention_token", "${csrf_prevention_token}")
            .formParam("action", "")
            .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
            // Assertions
            .check(status.is(200))
            .check(regex(setupBusinessDetailsPageTitle).exists) // Page title
            .check(regex("${expected_registrationNumberFormatted}").exists)
            .check(regex("${expected_make}").exists)
            .check(regex("${expected_model}").exists)
        )
    )

  def setupBusinessDetailsToBusinessChooseYourAddress =
    exitBlockOnFail(
      feed(data)
        .exec(
          http("setupBusinessDetailsToBusinessChooseYourAddress")
            .post(s"/setup-business-details")
            .headers(headers_x_www_form_urlencoded)
            .formParam("business-name", "${businessName}")
            .formParam("contact-name", "${businessContact}")
            .formParam("contact-email", "${businessEmail}")
            .formParam("business-postcode", "${businessPostcode}")
            .formParam("csrf_prevention_token", "${csrf_prevention_token}")
            .formParam("action", "")
            .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
            // Assertions
            .check(status.is(200))
            .check(regex(businessChooseYourAddressPageTitle).exists) // Page title
            .check(regex("${expected_registrationNumberFormatted}").exists)
            .check(regex("${expected_make}").exists)
            .check(regex("${expected_model}").exists)
        )
    )

  def businessChooseYourAddressToConfirmBusiness =
    exitBlockOnFail(
      feed(data)
        .exec(
          http("businessChooseYourAddressToConfirmBusiness")
            .post(s"/business-choose-your-address")
            .headers(headers_x_www_form_urlencoded)
//            .formParam("vrm_retention_businessChooseYourAddress_addressSelect", "${addressSelect}") // Use UPRN
            .formParam("address-select", "0") // UPRN disabled for Northern Ireland
            .formParam("csrf_prevention_token", "${csrf_prevention_token}")
            .formParam("action", "")
            // Assertions
            .check(status.is(200))
            .check(regex(confirmBusinessPageTitle).exists) // Page title
            .check(regex("${expected_registrationNumberFormatted}").exists)
            .check(regex("${expected_addressLine1}").exists)
            .check(regex("${expected_postTown}").exists)
            .check(regex("${expected_postcodeFormatted}").exists)
            .check(regex("${expected_make}").exists)
            .check(regex("${expected_model}").exists)
            .check(regex("${expected_businessName}").exists)
            .check(regex("${expected_businessContact}").exists)
            .check(regex("${expected_businessEmail}").exists)
            .check(regex("${expected_businessPostcode}").exists)
            .check(regex("${expected_businessAddressLine1}").exists)
            .check(regex("${expected_businessAddressLine2}").exists)
        )
    )

  def businessChooseYourAddressToConfirm =
    exitBlockOnFail(
      feed(data)
        .exec(
          http("businessChooseYourAddressToConfirm")
            .post(s"/business-choose-your-address")
            .headers(headers_x_www_form_urlencoded)
            .formParam("address-select", "${addressSelect}")
            .formParam("csrf_prevention_token", "${csrf_prevention_token}")
            .formParam("action", "")
            .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
            // Assertions
            .check(status.is(200))
            .check(regex(confirmPageTitle).exists) // Page title
            .check(regex("${expected_registrationNumberFormatted}").exists)
            .check(regex("${expected_addressLine1}").exists)
            .check(regex("${expected_addressLine2}").exists)
            .check(regex("${expected_addressLine3}").exists)
            .check(regex("${expected_postTown}").exists)
            .check(regex("${expected_postcodeFormatted}").exists)
            .check(regex("${expected_make}").exists)
            .check(regex("${expected_model}").exists)
            .check(regex("${expected_businessName}").exists)
            .check(regex("${expected_businessContact}").exists)
            .check(regex("${expected_businessEmail}").exists)
            .check(regex("${expected_businessPostcode}").exists)
            .check(regex("${expected_businessAddressLine1}").exists)
            .check(regex("${expected_businessAddressLine2}").exists)
        )
    )

  def confirmBusinessToIframePayment =
    exitBlockOnFail(
      exec(
        http("confirmBusinessToPayment")
          .get(s"/payment/begin") // Gatlin doesn't click on links, you need to go to the next page.
          .headers(headers_accept_html)
          .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
          // Assertions
          .check(status.is(200))
          .check(regex(paymentPageTitle).exists) // Page title
      )
    )

  def confirmToIframePayment =
    exitBlockOnFail(
      exec(
        http("confirmToPayment")
          .get(s"/payment/begin") // Gatlin doesn't click on links, you need to go to the next page.
          .headers(headers_accept_html)
          .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
          // Assertions
          .check(status.is(200))
          .check(regex(paymentPageTitle).exists) // Page title
      )
    )

  def paymentCallbackToRetainToSuccess =
    exitBlockOnFail(
      feed(data).
        exec(
          http("paymentCallbackToRetainToSuccess")
            .get(s"/retain") // Gatlin doesn't click on links, you need to go to the next page.
            .headers(headers_accept_html)
            // Assertions
            .check(status.is(200))
            .check(regex(successPageTitle).exists) // Page title
            .check(regex("${expected_vehicleRegistrationNumber}").exists)
        )
    )

  def vehicleLookupToVehicleLookupFailure =
    exitBlockOnFail(
      feed(data)
        .exec(
          http("vehicleLookupToVehicleLookupFailure")
            .post(s"/vehicle-lookup")
            .headers(headers_x_www_form_urlencoded)
            .formParam("vehicle-registration-number", "${vehicleRegistrationNumber}")
            .formParam("document-reference-number", "${documentReferenceNumber}")
            .formParam("postcode", "${postcode}")
            .formParam("keeper-consent", "${keeperConsent}")
            .formParam("csrf_prevention_token", "${csrf_prevention_token}")
            .formParam("action", "")
            // Assertions
            .check(status.is(200))
            .check(regex(vehicleLookupFailurePageTitle).exists) // Page title
            .check(regex("${expected_registrationNumberFormatted}").exists)
        )
    )

  def vehicleLookupToDirectToPaper =
    exitBlockOnFail(
      feed(data)
        .exec(
          http("vehicleLookupToDirectToPaper")
            .post(s"/vehicle-lookup")
            .headers(headers_x_www_form_urlencoded)
            .formParam("vehicle-registration-number", "${vehicleRegistrationNumber}")
            .formParam("document-reference-number", "${documentReferenceNumber}")
            .formParam("postcode", "${postcode}")
            .formParam("keeper-consent", "${keeperConsent}")
            .formParam("csrf_prevention_token", "${csrf_prevention_token}")
            .formParam("action", "")
            // Assertions
            .check(status.is(200))
            .check(regex(vehicleLookupToDirectToPaperTitle).exists) // Page title
            .check(regex("${expected_registrationNumberFormatted}").exists)
        )
    )

  def vehicleLookupToNotEligibleToTransact =
    exitBlockOnFail(
      feed(data)
        .exec(
          http("vehicleLookupToNotEligibleToTransact")
            .post(s"/vehicle-lookup")
            .headers(headers_x_www_form_urlencoded)
            .formParam("vehicle-registration-number", "${vehicleRegistrationNumber}")
            .formParam("document-reference-number", "${documentReferenceNumber}")
            .formParam("postcode", "${postcode}")
            .formParam("keeper-consent", "${keeperConsent}")
            .formParam("csrf_prevention_token", "${csrf_prevention_token}")
            .formParam("action", "")
            // Assertions
            .check(status.is(200))
            .check(regex(vehicleLookupToNotEligibleToTransactPageTitle).exists) // Page title
            .check(regex("${expected_registrationNumberFormatted}").exists)
        )
    )
}
