package services

import org.scalatestplus.play._

class NumberParserSpec extends PlaySpec {

  "NumberParser" should {

    "parse regular decimal numbers" in {
      val text = "The price is 123.45 dollars"
      val result = NumberParser.parseRegularNumbers(text)
      result mustBe List(123.45)
    }

    "parse multiple decimal numbers" in {
      val text = "Values: 12.34, 56.78, 90.12"
      val result = NumberParser.parseRegularNumbers(text)
      result mustBe List(12.34, 56.78, 90.12)
    }

    "parse integer numbers" in {
      val text = "Count: 42 items"
      val result = NumberParser.parseRegularNumbers(text)
      result mustBe List(42d)
    }

    "parse numbers with multipliers" in {
      val text = "Budget: 2.5 million dollars"
      val result = NumberParser.parseNumbersWithMultipliers(text)
      result mustBe List(2500000d)
    }

    "parse mixed numbers with abbreviations" in {
      val text = "Sales: 1.2m revenue"
      val result = NumberParser.parseNumbersWithMultipliers(text)
      result mustBe List(1200000d)
    }

    "parse all numbers together" in {
      val text = "Regular: 123.45 and special: 2.5 million"
      val result = NumberParser.parseAllNumbers(text)
      result must contain allOf(123.45, 2500000d)
    }

    "find largest number from mixed text" in {
      val text = "Values: 123.45, 2.5 million, 999"
      val result = NumberParser.findLargestNumber(text)
      result mustBe Some(2500000d)
    }

    "handle empty text" in {
      val text = "No numbers here!"
      val result = NumberParser.findLargestNumber(text)
      result mustBe None
    }

    "handle decimal numbers without integer part" in {
      val text = "Rate: 0.75 percent"
      val result = NumberParser.parseRegularNumbers(text)
      result mustBe List(0.75)
    }
  }
}