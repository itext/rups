/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
    Authors: Apryse Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    APRYSE GROUP. APRYSE GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.rups.view.itext.editor;

import com.itextpdf.kernel.exceptions.PdfException;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.SimpleAttributeSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("UnitTest")
class Latin1FilterTest {
    static Stream<Arguments> stringReplacementMap() {
        return Stream.of(
                // ASCII should remain as-is
                sameString(collectString(IntStream.range(0, 0x80))),
                // Latin-1 Supplement should also remain as-is
                sameString(collectString(IntStream.range(0x80, 0x100))),
                // And both of their combinations
                sameString(collectString(IntStream.range(0, 0x100))),
                // Symbols outside that should get replaced
                Arguments.of("AĠ₪B", "A\u00C4\u00A0\u00E2\u0082\u00AAB"),
                // Should also work properly for symbols outside BMP
                Arguments.of("A\uD808\uDC00B", "A\u00F0\u0092\u0080\u0080B"),
                // Invalid string (i.e. random surrogates) should throw
                Arguments.of("A\uD808", null),
                Arguments.of("A\uDC00", null),
                Arguments.of("A\uD808B", null),
                Arguments.of("A\uDC00B", null)
        );
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("stringReplacementMap")
    void insertString(String inputString, String outputString) throws BadLocationException {
        final int offset = 23;
        final AttributeSet attr = new SimpleAttributeSet();
        final FilterBypass fb = new MockFilterBypass() {
            @Override
            public void insertString(int actualOffset, String actualString, AttributeSet actualAttr) {
                Assertions.assertEquals(offset, actualOffset);
                if (outputString.equals(inputString)) {
                    Assertions.assertSame(outputString, actualString);
                } else {
                    Assertions.assertEquals(outputString, actualString);
                }
                Assertions.assertSame(attr, actualAttr);
            }
        };
        final Latin1Filter filter = new Latin1Filter();
        if (outputString == null) {
            Assertions.assertThrows(
                    PdfException.class,
                    () -> filter.insertString(fb, offset, inputString, attr)
            );
        } else {
            filter.insertString(fb, offset, inputString, attr);
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("stringReplacementMap")
    void replace(String inputString, String outputString) throws BadLocationException {
        final int offset = 23;
        final int length = 42;
        final AttributeSet attr = new SimpleAttributeSet();
        final FilterBypass fb = new MockFilterBypass() {
            @Override
            public void replace(int actualOffset, int actualLength, String actualString, AttributeSet actualAttr) {
                Assertions.assertEquals(offset, actualOffset);
                Assertions.assertEquals(length, actualLength);
                if (outputString.equals(inputString)) {
                    Assertions.assertSame(outputString, actualString);
                } else {
                    Assertions.assertEquals(outputString, actualString);
                }
                Assertions.assertSame(attr, actualAttr);
            }
        };
        final Latin1Filter filter = new Latin1Filter();
        if (outputString == null) {
            Assertions.assertThrows(
                    PdfException.class,
                    () -> filter.replace(fb, offset, length, inputString, attr)
            );
        } else {
            filter.replace(fb, offset, length, inputString, attr);
        }
    }

    private static Arguments sameString(String s) {
        return Arguments.of(s, s);
    }

    private static String collectString(IntStream is) {
        return is.collect(
                StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append
        ).toString();
    }

    private static class MockFilterBypass extends FilterBypass {
        @Override
        public Document getDocument() {
            throw new AssertionError("Unexpected getDocument call");
        }

        @Override
        public void remove(int actualOffset, int actualLength) {
            throw new AssertionError("Unexpected remove call");
        }

        @Override
        public void insertString(int actualOffset, String actualString, AttributeSet actualAttr) {
            throw new AssertionError("Unexpected insertString call");
        }

        @Override
        public void replace(int actualOffset, int actualLength, String actualString, AttributeSet actualAttr) {
            throw new AssertionError("Unexpected replace call");
        }
    }
}