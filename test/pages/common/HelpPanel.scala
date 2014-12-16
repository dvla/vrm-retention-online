package pages.common

import mappings.common.Help.HelpId
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._

object HelpPanel {

  def help(implicit driver: WebDriver) = find(id(HelpId)).get
}