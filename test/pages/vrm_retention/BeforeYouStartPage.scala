package pages.vrm_retention

import helpers.webbrowser.{WebDriverFactory, Element, Page, WebBrowserDSL}
import org.openqa.selenium.{By, WebDriver}
import pages.ApplicationContext.applicationContext
import views.vrm_retention.BeforeYouStart.NextId

object BeforeYouStartPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/before-you-start"
  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Take a Registration Number off a Vehicle"
  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  def startNow(implicit driver: WebDriver): Element = find(id(NextId)).get

  def footer(implicit driver: WebDriver) = driver.findElement(By.id("footer"))

  def footerMetaInner(implicit driver: WebDriver) = footer.findElement(By.className("footer-meta-inner"))

  def footerItem(index: Int)(implicit driver: WebDriver) = footerMetaInner.findElements(By.tagName("li")).get(index)
}
