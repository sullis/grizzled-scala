/*
 ---------------------------------------------------------------------------
  Copyright (c) 2009-2011 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

  * Neither the names "clapper.org", "Grizzled Scala Library", nor the
    names of its contributors may be used to endorse or promote products
    derived from this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  --------------------------------------------------------------------------
*/

import org.scalatest.FunSuite
import grizzled.string._
import grizzled.string.util._
import grizzled.string.GrizzledString._

/**
 * Tests the grizzled.string functions.
 */
class StringUtilTest extends FunSuite {
  test("string to boolean conversions that should succeed") {
    val data = Map(
      "true"  -> Right(true),
      "t"     -> Right(true),
      "yes"   -> Right(true),
      "y"     -> Right(true),
      "1"     -> Right(true),

      "false" -> Right(false),
      "f"     -> Right(false),
      "no"    -> Right(false),
      "n"     -> Right(false),
      "0"     -> Right(false)
    )

    for {(input, expected) <- data;
         permutations = List(input,
                             input.capitalize,
                             input.toUpperCase,
                             " " + input,
                             " " + input + " ",
                             input + " ")
         s <- permutations} {
      assertResult(expected, "\"" + s + "\" -> " + expected.toString)  {
        util.strToBoolean(s)
      }
    }
  }

  test("string to boolean conversions that should fail") {
    val data = List("tru", "tr", "z", "truee", "xtrue",
                    "000", "00", "111", "1a", "0z",
                    "fa", "fal", "fals", "falsee")

    for {input <- data
         permutations = List(input, input.capitalize, input.toUpperCase)
         s <- permutations} {
      assert(util.strToBoolean(s).isLeft)
    }
  }

  test("tokenizing quoted strings") {
    val data = Map(
      "a b c"                        -> List("a", "b", "c"),
      "aa bb cc"                     -> List("aa", "bb", "cc"),
      "\"aa\\\"a\" 'b'"              -> List("aa\"a", "b"),
      "one two '3\" four'"       -> List("one", "two", "3\" four"),
      "\"a'b    c'\" 'b\\'c  d' a\"" -> List("a'b    c'", "b'c  d", "a\"")
    )

    for((input, expected) <- data) {
      assertResult(expected, "\"" + input + "\" -> " + expected.toString) {
        tokenizeWithQuotes(input)
      }
    }

  }

  test("WordWrapper") {
    val s = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "In congue tincidunt fringilla. Sed interdum nibh vitae " +
            "libero fermentum id dictum risus facilisis. Pellentesque " +
            "habitant morbi tristique senectus et netus et malesuada " +
            "fames ac turpis egestas. Sed ante nisi, pharetra ut " +
            "eleifend vitae, congue ut quam. Vestibulum ante ipsum " +
            "primis in."

    val data = Map(
      (s, 79, 0, "", ' ') ->
"""Lorem ipsum dolor sit amet, consectetur adipiscing elit. In congue tincidunt
fringilla. Sed interdum nibh vitae libero fermentum id dictum risus facilisis.
Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac
turpis egestas. Sed ante nisi, pharetra ut eleifend vitae, congue ut quam.
Vestibulum ante ipsum primis in.""",

      (s, 40, 0, "", ' ') ->
"""Lorem ipsum dolor sit amet, consectetur
adipiscing elit. In congue tincidunt
fringilla. Sed interdum nibh vitae
libero fermentum id dictum risus
facilisis. Pellentesque habitant morbi
tristique senectus et netus et malesuada
fames ac turpis egestas. Sed ante nisi,
pharetra ut eleifend vitae, congue ut
quam. Vestibulum ante ipsum primis in.""",

      (s, 40, 5, "", ' ') ->
"""     Lorem ipsum dolor sit amet,
     consectetur adipiscing elit. In
     congue tincidunt fringilla. Sed
     interdum nibh vitae libero
     fermentum id dictum risus
     facilisis. Pellentesque habitant
     morbi tristique senectus et netus
     et malesuada fames ac turpis
     egestas. Sed ante nisi, pharetra ut
     eleifend vitae, congue ut quam.
     Vestibulum ante ipsum primis in.""",

      (s, 60, 0, "foobar: ", ' ') ->
"""foobar: Lorem ipsum dolor sit amet, consectetur adipiscing
        elit. In congue tincidunt fringilla. Sed interdum
        nibh vitae libero fermentum id dictum risus
        facilisis. Pellentesque habitant morbi tristique
        senectus et netus et malesuada fames ac turpis
        egestas. Sed ante nisi, pharetra ut eleifend vitae,
        congue ut quam. Vestibulum ante ipsum primis in.""",

      (s, 60, 0, "foobar: ", '.') ->
"""foobar: Lorem ipsum dolor sit amet, consectetur adipiscing
........elit. In congue tincidunt fringilla. Sed interdum
........nibh vitae libero fermentum id dictum risus
........facilisis. Pellentesque habitant morbi tristique
........senectus et netus et malesuada fames ac turpis
........egestas. Sed ante nisi, pharetra ut eleifend vitae,
........congue ut quam. Vestibulum ante ipsum primis in."""

    )

    for((input, expected) <- data) {
      val (string, width, indent, prefix, indentChar) = input

      assertResult(expected, "\"" + input + "\" -> " + expected.toString) {
        val wrapper = new WordWrapper(width, indent, prefix, indentChar)
        wrapper.wrap(string)
      }
    }
  }

  test("bytesToHexString") {
    val Data = Seq(
      byteArray(Array(0x10, 0x13, 0x99, 0xff)) -> "101399ff"
    )

    for ((bytes, s) <- Data) {

      assertResult(s, s"bytesToHexString yielding: $s") {
        bytesToHexString(bytes)
      }
    }
  }

  test("hexStringToBytes") {
    val Data = Seq(
      "101399ff" -> Some(byteArray(Array(0x10, 0x13, 0x99, 0xff))),
      "fail"     -> None,
      "FFBC9D"   -> Some(byteArray(Array(0xff, 0xbc, 0x9d)))
    )

    def eqByteArray(b1: Array[Byte], b2: Array[Byte]): Boolean = {
      val s1 = b1.toSet
      val s2 = b2.toSet
      s2 == s1
    }

    def eqOpt(o1: Option[Array[Byte]], o2: Option[Array[Byte]]): Boolean = {
      o1.map { b1 => o2.isDefined && eqByteArray(b1, o2.get) }
        .getOrElse( o2.isEmpty )
    }

    for ((s, byteOpt) <- Data) {
      assert(eqOpt(byteOpt, hexStringToBytes(s)), s"hexStringToBytes: $s")
    }
  }

  private def byteArray(b: Array[Int]) = b.map { _.asInstanceOf[Byte] }

}
