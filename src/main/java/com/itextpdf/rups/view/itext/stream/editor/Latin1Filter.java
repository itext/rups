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
package com.itextpdf.rups.view.itext.stream.editor;

import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.rups.view.Language;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * A document filter, which retains Latin-1 characters as-is, and for all
 * others returns a "byte equivalent" UTF-8 encoding of characters.
 *
 * <p>
 * This is, pretty much, a hack to allow working with a PDF byte stream as a
 * char stream with trivial conversions. At all points in time the document
 * characters will have codepoints in a 0-255 range and can be freely encoded
 * as Latin-1.
 * </p>
 *
 * <p>
 * Under this filter, if you type, for example, "į" (U+012F), you would get
 * "Ä¯" instead (U+00C4 U+00AF, which is the UTF-8 encoding of the symbol,
 * where each byte is padded to two bytes).
 * </p>
 */
public final class Latin1Filter extends DocumentFilter {
    /**
     * Pre-allocated output buffer for the UTF-8 character encoder.
     */
    private final ByteBuffer utf8CharBuffer = ByteBuffer.allocate(4);

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        super.insertString(fb, offset, generateSubstitute(string), attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        super.replace(fb, offset, length, generateSubstitute(text), attrs);
    }

    private String generateSubstitute(String original) {
        /*
         * If text is encodable in Latin-1, just return the string as-is.
         * This is a very common case, as the majority of PDF content streams
         * contains just ASCII text, so a separate branch at the start should
         * be worth it to avoid any allocations.
         */
        int index = getNonLatin1Index(original);
        if (index >= original.length()) {
            return original;
        }
        /*
         * Otherwise we build a substitute string, where non-Latin-1 chars
         * are replaced with UTF-8 "bytes". We will assume there is only
         * one inconvenient character for pre-allocation (thus +3).
         */
        final CharsetEncoder utf8Encoder = StandardCharsets.UTF_8.newEncoder();
        final StringBuilder substitute = initStringBuilder(original, index);
        while (index < original.length()) {
            /*
             * Encoding 1 non-Latin-1 code point first.
             */
            final char ch = original.charAt(index);
            utf8CharBuffer.clear();
            int end = index + 1;
            if (Character.isHighSurrogate(ch) && end < original.length()) {
                ++end;
            }
            final CharBuffer encoderInput = CharBuffer.wrap(original, index, end);
            final CoderResult result = utf8Encoder.encode(encoderInput, utf8CharBuffer, true);
            if (!result.isUnderflow()) {
                throwException(result);
            }
            for (int j = 0; j < utf8CharBuffer.position(); ++j) {
                substitute.append((char) (utf8CharBuffer.get(j) & 0xFF));
            }
            /*
             * At the end append the possible remaining Latin-1 part.
             */
            index = getNonLatin1Index(original, end);
            substitute.append(original, end, index);
        }
        return substitute.toString();
    }

    private static void throwException(CoderResult cr) {
        try {
            cr.throwException();
        } catch (CharacterCodingException e) {
            throw new PdfException(Language.ERROR_CHARACTER_ENCODING.getString(), e);
        }
    }

    private static int getNonLatin1Index(CharSequence cs) {
        return getNonLatin1Index(cs, 0);
    }

    private static int getNonLatin1Index(CharSequence cs, int start) {
        int index = start;
        while (index < cs.length() && isLatin1(cs.charAt(index))) {
            ++index;
        }
        return index;
    }

    private static boolean isLatin1(char c) {
        return c <= '\u00FF';
    }

    private static StringBuilder initStringBuilder(String str, int nonLatin1Index) {
        int capacity = nonLatin1Index;
        final int suffixLength = str.length() - nonLatin1Index;
        /*
         * For small enough strings just assume the worst case scenario and
         * allocate 4 "bytes" for each char in suffix. Otherwise, just do
         * something more conservative like 1.25 "bytes" per char.
         */
        if (suffixLength <= 1024) {
            capacity += (4 * suffixLength);
        } else {
            capacity += (5 * suffixLength / 4);
        }
        final StringBuilder sb = new StringBuilder(capacity);
        // Immediately add the Latin-1 part
        sb.append(str, 0, nonLatin1Index);
        return sb;
    }
}
