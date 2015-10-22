package pages.vrm_retention

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id, Element}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_retention.BeforeYouStart.NextId

object BeforeYouStartPage extends Page {

  def address = s"$applicationContext/before-you-start"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Take a Registration Number off a Vehicle"
  final val titleCy: String = "Tynnu Rhif Cofrestru oddi ar Gerbyd"

  def startNow(implicit driver: WebDriver) = find(id(NextId)).get

  def footer(implicit driver: WebDriver) = driver.findElement(By.id("footer"))

  def footerMetaInner(implicit driver: WebDriver) = footer.findElement(By.className("footer-meta-inner"))

  def footerItem(index: Int)(implicit driver: WebDriver) = footerMetaInner.findElements(By.tagName("li")).get(index)
}
