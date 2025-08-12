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

    "detect table multipliers in headers" in {
      val header1 = "Revenue (in millions)"
      val header2 = "Budget (thousands)"

      NumberParser.detectTableMultiplier(header1) mustBe Some(1000000L)
      NumberParser.detectTableMultiplier(header2) mustBe Some(1000L)
    }

    "apply table multipliers to bare numbers" in {
      val lines = List(
        "Revenue (in millions)",
        "Product A    2.5",
        "Product B    1.2",
        "Total        3.7"
      )

      val result = NumberParser.parseAllNumbersWithTableContext(lines)
      result must contain allOf(2500000.0, 1200000.0, 3700000.0)
    }

    "handle mixed explicit and table multipliers" in {
      val lines = List(
        "Budget (in millions)",
        "Category A   2.5",
        "Category B   500 thousand",  // Explicit multiplier overrides table
        "Category C   1.0"
      )

      val result = NumberParser.parseAllNumbersWithTableContext(lines)
      result must contain allOf(2500000.0, 500000.0, 1000000.0)
    }

    "reset table context on non-table patterns" in {
      val lines = List(
        "Revenue (in millions)",
        "Item 1    2.5",
        "This is a paragraph of normal text that clearly indicates we have moved away from the table structure.",  // Non-table text resets context
        "Item 2    1000"  // Should be parsed as 1000, not 1000 million
      )

      val result = NumberParser.parseAllNumbersWithTableContext(lines)
      result must contain allOf(2500000.0, 1000.0)
    }

    "maintain table context for table-like rows" in {
      val lines = List(
        "Budget (in millions)",
        "Category A    2.5",
        "Category B    1.2",  // Still looks like a table row
        "Category C    3.7"   // Should all be in millions
      )

      val result = NumberParser.parseAllNumbersWithTableContext(lines)
      result must contain allOf(2500000.0, 1200000.0, 3700000.0)
    }

    "reset context on table end indicators" in {
      val lines = List(
        "Revenue (in millions)",
        "Item A    2.5",
        "Notes: The following data is unrelated.",  // Table end indicator
        "Regular number 1000"  // Should not be in millions
      )

      val result = NumberParser.parseAllNumbersWithTableContext(lines)
      result must contain allOf(2500000.0, 1000.0)
    }

    "find largest number with table context" in {
      val lines = List(
        "Budget Analysis (in billions)",
        "Defense     750.2",
        "Education   80.5",
        "Healthcare  1.2 trillion",  // Explicit multiplier is larger
        "Total       831.7"
      )

      val result = NumberParser.findLargestNumberWithTableContext(lines)
      result mustBe Some(1200000000000.0)  // 1.2 trillion
    }

    "parse numbers with commas" in {
      val text = "Revenue was 1,234,567.89 dollars"
      val result = NumberParser.parseRegularNumbers(text)
      result mustBe List(1234567.89)
    }

    "parse comma-separated numbers with multipliers" in {
      val text = "Budget: 2,500.75 million"
      val result = NumberParser.parseNumbersWithMultipliers(text)
      result mustBe List(2500750000.0)
    }

    "handle mixed comma and non-comma numbers" in {
      val text = "Values: 1,234, 567.89, and 2,500,000"
      val result = NumberParser.parseRegularNumbers(text)
      result must contain allOf(1234.0, 567.89, 2500000.0)
    }

    "apply table multipliers to comma-separated numbers" in {
      val lines = List(
        "Revenue (in millions)",
        "Product A    2,500.25",
        "Product B    1,200",
        "Total        3,700.25"
      )

      val result = NumberParser.parseAllNumbersWithTableContext(lines)
      result must contain allOf(2500250000.0, 1200000000.0, 3700250000.0)
    }

    "find largest comma-separated number" in {
      val text = "Values: 1,234.56, 2,500 million, 999,999"
      val result = NumberParser.findLargestNumber(text)
      result mustBe Some(2500000000.0)  // 2,500 million
    }
  }
}