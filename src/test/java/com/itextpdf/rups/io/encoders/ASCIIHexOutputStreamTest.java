/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
package com.itextpdf.rups.io.encoders;

import com.itextpdf.rups.io.util.CloseableByteArrayOutputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class ASCIIHexOutputStreamTest {
    @Test
    public void encodeTest() throws IOException {
        byte[] input = new byte[256];
        for (int i = 0; i < input.length; ++i) {
            input[i] = (byte) i;
        }
        String expected = "000102030405060708090a0b0c0d0e0f101112131415161718"
                + "191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435"
                + "363738393a3b3c3d3e3f404142434445464748494a4b4c4d4e4f505152"
                + "535455565758595a5b5c5d5e5f606162636465666768696a6b6c6d6e6f"
                + "707172737475767778797a7b7c7d7e7f808182838485868788898a8b8c"
                + "8d8e8f909192939495969798999a9b9c9d9e9fa0a1a2a3a4a5a6a7a8a9"
                + "aaabacadaeafb0b1b2b3b4b5b6b7b8b9babbbcbdbebfc0c1c2c3c4c5c6"
                + "c7c8c9cacbcccdcecfd0d1d2d3d4d5d6d7d8d9dadbdcdddedfe0e1e2e3"
                + "e4e5e6e7e8e9eaebecedeeeff0f1f2f3f4f5f6f7f8f9fafbfcfdfeff>";

        CloseableByteArrayOutputStream baos = new CloseableByteArrayOutputStream();
        try (ASCIIHexOutputStream encoder = new ASCIIHexOutputStream(baos)) {
            encoder.write(input);
        }
        Assertions.assertEquals(expected, toString(baos));
    }

    @Test
    public void emptyStreamTest() throws IOException {
        CloseableByteArrayOutputStream baos = new CloseableByteArrayOutputStream();
        ASCIIHexOutputStream encoder = new ASCIIHexOutputStream(baos);
        Assertions.assertEquals("", toString(baos));

        encoder.finish();
        Assertions.assertEquals(">", toString(baos));

        encoder.close();
        Assertions.assertEquals(">", toString(baos));
    }

    @Test
    public void finishableImplTest() throws IOException {
        CloseableByteArrayOutputStream baos = new CloseableByteArrayOutputStream();
        ASCIIHexOutputStream encoder = new ASCIIHexOutputStream(baos);

        encoder.write(new byte[]{0x1F, 0x3A, 0x7F, 0x59});
        encoder.flush();
        Assertions.assertEquals("1f3a7f59", toString(baos));

        // Should add EOD
        encoder.finish();
        Assertions.assertFalse(baos.isClosed());
        Assertions.assertEquals("1f3a7f59>", toString(baos));

        // Should be noop, since idempotent
        encoder.finish();
        Assertions.assertFalse(baos.isClosed());
        Assertions.assertEquals("1f3a7f59>", toString(baos));

        // Should not append data, since finished
        encoder.close();
        Assertions.assertTrue(baos.isClosed());
        Assertions.assertEquals("1f3a7f59>", toString(baos));
    }

    private static String toString(java.io.ByteArrayOutputStream baos) {
        return baos.toString(StandardCharsets.US_ASCII);
    }
}
