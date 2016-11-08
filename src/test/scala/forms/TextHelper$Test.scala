package forms

import org.scalatest.{Matchers, WordSpecLike}

class TextHelper$Test extends WordSpecLike with Matchers {

  import TextHelper._

  "splitLines" should {
    "convert 2 or more consecutive newline characters into Paragraphs" in {
      splitLines("foo\n\nbar") shouldBe List(Paragraph("foo"), Paragraph("bar"))
      splitLines("foo\n\n\n\nbar") shouldBe List(Paragraph("foo"), Paragraph("bar"))
    }
    "convert 2 or more consecutive cr-nl pairs into Paragraphs" in {
      splitLines("foo\r\n\r\nbar") shouldBe List(Paragraph("foo"), Paragraph("bar"))
      splitLines("foo\r\n\r\n\r\nbar") shouldBe List(Paragraph("foo"), Paragraph("bar"))
    }

    "convert a single newline into a break" in {
      splitLines("foo\nbar") shouldBe List(Text("foo"), Break, Text("bar"))
    }
  }

}
