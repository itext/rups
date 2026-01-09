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
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("UnitTest")
public class RunLengthOutputStreamTest {
    public static Iterable<Object[]> encodeTestArguments() {
        return Arrays.asList(
                new Object[]{
                        new byte[0],
                        new byte[]{(byte) 0x80},
                        "Empty input"
                },
                new Object[]{
                        // We do not collapse 2 elem repeating runs
                        concat(uniqueRun(1), repeatingRun(2, 0x42), uniqueRun(2),
                               repeatingRun(3, 0x43),
                               uniqueRun(3),
                               repeatingRun(4, 0x44),
                               uniqueRun(4)),
                        concat(  4, uniqueRun(1), repeatingRun(2, 0x42), uniqueRun(2),
                               254, 0x43,
                                 2, uniqueRun(3),
                               253, 0x44,
                                 3, uniqueRun(4),
                               0x80),
                        "Variable run types"
                },
                new Object[]{
                        uniqueRun(300, 0x00),
                        concat(127, uniqueRun(128, 0x00),
                               127, uniqueRun(128, 0x80),
                                43, uniqueRun( 44, 0x00),
                               0x80),
                        "Long unique run"
                },
                new Object[]{
                        repeatingRun(300, 0xAD),
                        concat(129, 0xAD, 129, 0xAD, 213, 0xAD, 0x80),
                        "Long repeating run"
                },
                new Object[]{
                        concat(uniqueRun(128, 0x40), repeatingRun(128, 0x60)),
                        concat(127, uniqueRun(128, 0x40), 129, 0x60, 0x80),
                        "128 unique run + 128 repeating run"
                },
                new Object[]{
                        concat(repeatingRun(128, 0x40), uniqueRun(128, 0x60)),
                        concat(129, 0x40, 127, uniqueRun(128, 0x60), 0x80),
                        "128 repeating run + 128 unique run"
                }
        );
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("encodeTestArguments")
    public void encodeTest(byte[] input, byte[] output, String name) throws IOException {
        CloseableByteArrayOutputStream baos = new CloseableByteArrayOutputStream();
        try (RunLengthOutputStream encoder = new RunLengthOutputStream(baos)) {
            encoder.write(input);
        }
        Assertions.assertArrayEquals(output, baos.toByteArray());
    }

    @Test
    public void finishableImplTest() throws IOException {
        CloseableByteArrayOutputStream baos = new CloseableByteArrayOutputStream();
        RunLengthOutputStream encoder = new RunLengthOutputStream(baos);

        encoder.write(uniqueRun(3, 0x00));
        encoder.write(repeatingRun(3, 0x00));
        encoder.write(repeatingRun(3, 0xFF));
        encoder.write(uniqueRun(3, 0x81));
        encoder.flush();
        Assertions.assertArrayEquals(
                concat(2, uniqueRun(3, 0x00), 254, 0x00, 254, 0xFF),
                baos.toByteArray()
        );

        // Should add encoded pending block and EOD
        encoder.finish();
        Assertions.assertFalse(baos.isClosed());
        Assertions.assertArrayEquals(
                concat(2, uniqueRun(3, 0x00), 254, 0x00, 254, 0xFF, 2, uniqueRun(3, 0x81), 0x80),
                baos.toByteArray()
        );

        // Should be noop, since idempotent
        encoder.finish();
        Assertions.assertFalse(baos.isClosed());
        Assertions.assertArrayEquals(
                concat(2, uniqueRun(3, 0x00), 254, 0x00, 254, 0xFF, 2, uniqueRun(3, 0x81), 0x80),
                baos.toByteArray()
        );

        // Should not append data, since finished
        encoder.close();
        Assertions.assertTrue(baos.isClosed());
        Assertions.assertArrayEquals(
                concat(2, uniqueRun(3, 0x00), 254, 0x00, 254, 0xFF, 2, uniqueRun(3, 0x81), 0x80),
                baos.toByteArray()
        );
    }

    private static byte[] concat(Object... values) {
        int size = 0;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] instanceof Integer) {
                ++size;
            } else if (values[i] instanceof byte[]) {
                size += ((byte[]) values[i]).length;
            } else {
                throw new IllegalArgumentException("unexpected type");
            }
        }
        byte[] result = new byte[size];
        int offset = 0;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] instanceof Integer) {
                result[offset] = (byte) (((Integer) values[i]) & 0xFF);
                ++offset;
            } else {
                byte[] arr = (byte[]) values[i];
                System.arraycopy(arr, 0, result, offset, arr.length);
                offset += arr.length;
            }
        }
        return result;
    }

    private static byte[] uniqueRun(int length) {
        return uniqueRun(length, 0);
    }

    private static byte[] uniqueRun(int length, int offset) {
        byte[] run = new byte[length];
        for (int i = 0; i < length; ++i) {
            run[i] = (byte) ((offset + i) & 0xFF);
        }
        return run;
    }

    private static byte[] repeatingRun(int length, int value) {
        byte[] run = new byte[length];
        Arrays.fill(run, (byte) (value & 0xFF));
        return run;
    }
}
