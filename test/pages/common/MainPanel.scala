package pages.common

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import views.vrm_retention.Main.BackId

object MainPanel {

  def back(implicit driver: WebDriver) = find(id(BackId)).get
}