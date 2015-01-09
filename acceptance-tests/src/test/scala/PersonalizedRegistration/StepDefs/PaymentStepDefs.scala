package PersonalizedRegistration.StepDefs

import _root_.common.CommonStepDefs
import cucumber.api.java.After
import cucumber.api.java.en.{Given, Then, When}
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.{PatienceConfig, eventually}
import org.scalatest.selenium.WebBrowser._
import pages._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

import scala.concurrent.duration.DurationInt

final class PaymentStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  //  private implicit val webDriver: EventFiringWebDriver = {
  //    import com.typesafe.config.ConfigFactory
  //    val conf = ConfigFactory.load()
  //    conf.getString("browser.type") match {
  //      case "firefox" => new WebBrowserFirefoxDriver
  //      case _ => new WebBrowserDriver
  //    }
  //  }
  val beforeYouStart = new BeforeYouStartPageSteps(timeout)
  val vehicleLookup = new VehicleLookupPageSteps(timeout)
  val payment = new PaymentPageSteps(timeout)
  val success = new SuccessPaymentPageSteps(timeout)
  val paymentFailure = new PaymentFailurePageSteps(timeout)
  val paymentCallBack = new PaymentCallbackPageSteps(timeout)
  val vehicleNotFound = new VehicleNotFoundPageSteps(timeout)
  val vrmLocked = new VrmLockedPageSteps(timeout)
  val vehicleLookupFailure = new VehicleLookupFailurePageSteps(timeout)
  val setupBusinessDetails = new SetupBusinessDetailsPageSteps(timeout)
  val businessChooseYourAddress = new BusinessChooseYourAddressPageSteps(timeout)
  val confirmBusiness = new ConfirmBusinessPageSteps(timeout)
  val timeout = PatienceConfig(timeout = 30.seconds)
  lazy val user = new CommonStepDefs(
    timeout,
    beforeYouStart,
    vehicleLookup,
    vehicleNotFound,
    vrmLocked,
    confirmBusiness,
    setupBusinessDetails,
    businessChooseYourAddress
  )

  @Given("^that I have started the PR Retention Service for payment$")
  def `that I have started the PR Retention Service for payment`() {
    user.`start the PR service`
  }

  @Given("^I search and confirm the vehicle to be registered$")
  def `i search and confirm the vehicle to be registered`() = {
    vehicleLookup.
      `is displayed`.
      enter("A1", "11111111111", "AA11AA").
      `keeper is acting`.
      `find vehicle`
    user.confirmDetails
  }

  @When("^I enter payment details as \"(.*?)\",\"(.*?)\" and \"(.*?)\"$")
  def `i enter payment details as <CardName>,<CardNumber> and <SecurityCode>`(cardName: String, cardNumber: String, cardExpiry: String) = {
    payment
      .`is displayed`
      .enter(cardName, cardNumber, cardExpiry)
      .`expiryDate`
  }

  @When("^proceed to the payment$")
  def `proceed to the payment`() = {
    payment.`paynow`
  }

  @Then("^following \"(.*?)\" should be displayed$")
  def `following should be displayed`(Message: String) = {
    eventually {
      pageSource contains (Message)
    }
    if (Message == "Payment Successful") {
      pageTitle contains (Message)
    }
    else if (Message == "Payment Cancelled or Not Authorised") {
      pageTitle contains ("/payment-not-authorised")
    }
    else
      fail(s"not the message we expected: $Message")
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
