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

import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.{By, WebDriver, WebElement}

import scala.collection.JavaConverters._

trait Element {

  val underlying: WebElement

  def location: Point = Point(underlying.getLocation.getX, underlying.getLocation.getY)

  def size: Dimension = Dimension(underlying.getSize.getWidth, underlying.getSize.getHeight)

  def isDisplayed: Boolean = underlying.isDisplayed

  def isEnabled: Boolean = underlying.isEnabled

  def isSelected: Boolean = underlying.isSelected

  def tagName: String = underlying.getTagName

  def attribute(name: String): Option[String] = Option(underlying.getAttribute(name))

  def text: String = {
    val txt = underlying.getText
    if (txt != null) txt else "" // Just in case, I'm not sure if Selenium would ever return null here
  }

  override def equals(other: Any): Boolean = underlying.equals(other)

  override def hashCode: Int = underlying.hashCode

  override def toString: String = underlying.toString
}

final class TextField(val underlying: WebElement) extends Element {

  if (!Helper.isTextField(underlying))
    throw new TestFailedException("Element " + underlying + " is not text field.")

  def value: String = underlying.getAttribute("value")

  def value_=(value: String) {
    underlying.clear()
    underlying.sendKeys(value)
  }

  def clear() {
    underlying.clear()
  }
}

final class TextArea(val underlying: WebElement) extends Element {

  if (!Helper.isTextArea(underlying))
    throw new TestFailedException("Element " + underlying + " is not text area.")

  def value: String = underlying.getAttribute("value")

  def value_=(value: String) {
    underlying.clear()
    underlying.sendKeys(value)
  }

  def clear() {
    underlying.clear()
  }
}

final class PasswordField(val underlying: WebElement) extends Element {

  if (!Helper.isPasswordField(underlying))
    throw new TestFailedException("Element " + underlying + " is not password field.")

  def value: String = underlying.getAttribute("value")

  def value_=(value: String) {
    underlying.clear()
    underlying.sendKeys(value)
  }

  def clear() {
    underlying.clear()
  }
}

trait ValueElement extends Element {

  val underlying: WebElement

  def checkCorrectType(isA: (WebElement) => Boolean, typeDescription: String) = {
    if (!isA(underlying))
      throw new TestFailedException("Element " + underlying + " is not " + typeDescription + " field.")
  }

  def value: String = underlying.getAttribute("value")

  def value_=(value: String) {
    underlying.clear()
    underlying.sendKeys(value)
  }

  def clear() {
    underlying.clear()
  }
}

final class EmailField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isEmailField, "text")
}

final class ColorField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isColorField, "color")
}

final class DateField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isDateField, "date")
}

final class DateTimeField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isDateTimeField, "datetime")
}

final class DateTimeLocalField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isDateTimeLocalField, "datetime-local")
}

final class MonthField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isMonthField, "month")
}

final class NumberField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isNumberField, "number")
}

final class RangeField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isRangeField, "range")
}

final class SearchField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isSearchField, "search")
}

final class TelField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isTelField, "tel")
}

final class TimeField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isTimeField, "time")
}

final class UrlField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isUrlField, "url")
}

final class WeekField(val underlying: WebElement) extends Element with ValueElement {

  checkCorrectType(Helper.isWeekField, "week")
}

final class RadioButton(val underlying: WebElement) extends Element {

  if (!Helper.isRadioButton(underlying))
    throw new TestFailedException("Element " + underlying + " is not radio button.")

  def value: String = underlying.getAttribute("value")
}

final class RadioButtonGroup(groupName: String, driver: WebDriver) {

  private def groupElements = driver.findElements(By.name(groupName)).asScala.toList.filter(Helper.isRadioButton(_))

  if (groupElements.length == 0)
    throw new TestFailedException("No radio buttons with group name '" + groupName + "' was found.")

  def value: String = selection match {
    case Some(v) => v
    case None =>
      throw new TestFailedException("The radio button group on which value was invoked contained no selected radio button.")
  }

  def selection: Option[String] = {
    groupElements.find(_.isSelected) match {
      case Some(radio) =>
        Some(radio.getAttribute("value"))
      case None =>
        None
    }
  }

  def value_=(value: String) {
    groupElements.find(_.getAttribute("value") == value) match {
      case Some(radio) =>
        radio.click()
      case None =>
        throw new TestFailedException("Radio button value '" + value + "' not found for group '" + groupName + "'.")
    }
  }
}

final class Checkbox(val underlying: WebElement) extends Element {

  if (!Helper.isCheckBox(underlying))
    throw new TestFailedException("Element " + underlying + " is not check box.")

  def select() {
    if (!underlying.isSelected)
      underlying.click()
  }

  def clear() {
    if (underlying.isSelected())
      underlying.click()
  }

  def value: String = underlying.getAttribute("value")
}

final class MultiSelOptionSeq(underlying: collection.immutable.IndexedSeq[String]) extends collection.immutable.IndexedSeq[String] {

  def apply(idx: Int): String = underlying.apply(idx)

  def length: Int = underlying.length

  def +(value: String): MultiSelOptionSeq = {
    if (!underlying.contains(value))
      new MultiSelOptionSeq(underlying :+ value)
    else
      this
  }

  def -(value: String): MultiSelOptionSeq = {
    if (underlying.contains(value))
      new MultiSelOptionSeq(underlying.filter(_ != value))
    else
      this
  }
}

final class SingleSel(val underlying: WebElement) extends Element {

  if (underlying.getTagName.toLowerCase != "select")
    throw new TestFailedException("Element " + underlying + " is not select.")
  private val select = new Select(underlying)
  if (select.isMultiple)
    throw new TestFailedException("Element " + underlying + " is not a single-selection list.")

  def selection = {
    val first = select.getFirstSelectedOption
    if (first == null)
      None
    else
      Some(first.getAttribute("value"))
  }

  def value: String = selection match {
    case Some(v) => v
    case None =>
      throw new TestFailedException("The single selection list on which value was invoked had no selection.")
  }

  def value_=(value: String) {
    try {
      select.selectByValue(value)
    }
    catch {
      case e: org.openqa.selenium.NoSuchElementException =>
        throw new TestFailedException(e.getMessage)
    }
  }

  def getOptions = select.getOptions
}

final class MultiSel(val underlying: WebElement) extends Element {

  if (underlying.getTagName.toLowerCase != "select")
    throw new TestFailedException("Element " + underlying + " is not select.")
  private val select = new Select(underlying)
  if (!select.isMultiple)
    throw new TestFailedException("Element " + underlying + " is not a multi-selection list.")

  def clear(value: String) {
    select.deselectByValue(value)
  }

  def values: MultiSelOptionSeq = {
    val elementSeq = Vector.empty ++ select.getAllSelectedOptions.asScala
    new MultiSelOptionSeq(elementSeq.map(_.getAttribute("value")))
  }

  def values_=(values: collection.Seq[String]) {
    try {
      clearAll()
      values.foreach(select.selectByValue(_))
    }
    catch {
      case e: org.openqa.selenium.NoSuchElementException =>
        throw new TestFailedException(e.getMessage)
    }
  }

  def clearAll() {
    select.deselectAll()
  }
}

final case class Point(x: Int, y: Int)

final case class Dimension(width: Int, height: Int)