/*
 * Copyright 2001-2013 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * TODO Check for licensing issues as the code below is based on code found in Scalatest
 */

package helpers.webbrowser

import org.openqa.selenium.WebElement

object Helper {

  def isInputField(webElement: WebElement, name: String): Boolean = webElement.getTagName.toLowerCase == "input" && webElement.getAttribute("type").toLowerCase == name

  def isTextField(webElement: WebElement): Boolean = isInputField(webElement, "text")

  def isPasswordField(webElement: WebElement): Boolean = isInputField(webElement, "password")

  def isCheckBox(webElement: WebElement): Boolean = isInputField(webElement, "checkbox")

  def isRadioButton(webElement: WebElement): Boolean = isInputField(webElement, "radio")

  def isEmailField(webElement: WebElement): Boolean = isInputField(webElement, "text")

  def isColorField(webElement: WebElement): Boolean = isInputField(webElement, "color")

  def isDateField(webElement: WebElement): Boolean = isInputField(webElement, "date")

  def isDateTimeField(webElement: WebElement): Boolean = isInputField(webElement, "datetime")

  def isDateTimeLocalField(webElement: WebElement): Boolean = isInputField(webElement, "datetime-local")

  def isMonthField(webElement: WebElement): Boolean = isInputField(webElement, "month")

  def isNumberField(webElement: WebElement): Boolean = isInputField(webElement, "number")

  def isRangeField(webElement: WebElement): Boolean = isInputField(webElement, "range")

  def isSearchField(webElement: WebElement): Boolean = isInputField(webElement, "search")

  def isTelField(webElement: WebElement): Boolean = isInputField(webElement, "tel")

  def isTimeField(webElement: WebElement): Boolean = isInputField(webElement, "time")

  def isUrlField(webElement: WebElement): Boolean = isInputField(webElement, "url")

  def isWeekField(webElement: WebElement): Boolean = isInputField(webElement, "week")

  def isTextArea(webElement: WebElement): Boolean = webElement.getTagName.toLowerCase == "textarea"
}