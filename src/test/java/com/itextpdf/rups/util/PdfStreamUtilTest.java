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
package com.itextpdf.rups.util;

import com.itextpdf.kernel.pdf.CompressionConstants;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNull;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.rups.io.encoders.ASCII85CompressionStrategy;
import com.itextpdf.rups.io.encoders.ASCIIHexCompressionStrategy;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static java.nio.charset.StandardCharsets.US_ASCII;

@Tag("UnitTest")
final class PdfStreamUtilTest {
    private static final byte[] TEST_DATA = "ABC123".getBytes(US_ASCII);
    private static final byte[] TEST_DATA_85 = "5sdpn1,A~>".getBytes(US_ASCII);
    private static final byte[] TEST_DATA_HEX = "414243313233>".getBytes(US_ASCII);
    private static final byte[] TEST_DATA_HEX_85 = "1bggB1c$pB1GUaB4o~>".getBytes(US_ASCII);
    private static final byte[] TEST_DATA_HEX_85_HEX = "316267674231632470423147556142346f7e3e>"
            .getBytes(US_ASCII);

    @Test
    void applyFilter_hexOverPlain() throws IOException {
        final PdfStream stream = new PdfStream(TEST_DATA);
        stream.put(PdfName.Length, new PdfNumber(TEST_DATA.length));
        stream.remove(PdfName.Filter);
        stream.remove(PdfName.DecodeParms);

        PdfStreamUtil.applyFilter(stream, new ASCIIHexCompressionStrategy());

        Assertions.assertArrayEquals(TEST_DATA_HEX, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(TEST_DATA_HEX.length), stream.get(PdfName.Length));
        Assertions.assertEquals(PdfName.ASCIIHexDecode, stream.get(PdfName.Filter));
        Assertions.assertFalse(stream.containsKey(PdfName.DecodeParms));
    }

    @Test
    void applyFilter_hexOverPlainWithNulls() throws IOException {
        final PdfStream stream = new PdfStream(TEST_DATA);
        stream.put(PdfName.Length, new PdfNumber(TEST_DATA.length));
        stream.put(PdfName.Filter, PdfNull.PDF_NULL);
        stream.put(PdfName.DecodeParms, PdfNull.PDF_NULL);

        PdfStreamUtil.applyFilter(stream, new ASCIIHexCompressionStrategy());

        Assertions.assertArrayEquals(TEST_DATA_HEX, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(TEST_DATA_HEX.length), stream.get(PdfName.Length));
        Assertions.assertEquals(PdfName.ASCIIHexDecode, stream.get(PdfName.Filter));
        Assertions.assertFalse(stream.containsKey(PdfName.DecodeParms));
    }

    @Test
    void applyFilter_hexWithParamsOverPlain() throws IOException {
        final PdfStream stream = new PdfStream(TEST_DATA);
        stream.put(PdfName.Length, new PdfNumber(TEST_DATA.length));
        stream.remove(PdfName.Filter);
        stream.remove(PdfName.DecodeParms);

        PdfStreamUtil.applyFilter(stream, new ASCIIHexCompressionStrategy() {
            @Override
            public PdfObject getDecodeParams() {
                return new PdfDictionary();
            }
        });

        Assertions.assertArrayEquals(TEST_DATA_HEX, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(TEST_DATA_HEX.length), stream.get(PdfName.Length));
        Assertions.assertEquals(PdfName.ASCIIHexDecode, stream.get(PdfName.Filter));
        Assertions.assertTrue(stream.get(PdfName.DecodeParms).isDictionary());
    }

    @Test
    void applyFilter_85OverHex() throws IOException {
        final PdfStream stream = new PdfStream(TEST_DATA_HEX);
        stream.put(PdfName.Length, new PdfNumber(TEST_DATA_HEX.length));
        stream.put(PdfName.Filter, PdfName.ASCIIHexDecode);
        stream.remove(PdfName.DecodeParms);

        PdfStreamUtil.applyFilter(stream, new ASCII85CompressionStrategy());

        Assertions.assertArrayEquals(TEST_DATA_HEX_85, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(TEST_DATA_HEX_85.length), stream.get(PdfName.Length));
        Assertions.assertTrue(stream.get(PdfName.Filter).isArray());
        Assertions.assertEquals(
                List.of(PdfName.ASCII85Decode, PdfName.ASCIIHexDecode),
                ((PdfArray) stream.get(PdfName.Filter)).toList()
        );
        Assertions.assertFalse(stream.containsKey(PdfName.DecodeParms));
    }

    @Test
    void applyFilter_85WithParamsOverHex() throws IOException {
        final PdfStream stream = new PdfStream(TEST_DATA_HEX);
        stream.put(PdfName.Length, new PdfNumber(TEST_DATA_HEX.length));
        stream.put(PdfName.Filter, PdfName.ASCIIHexDecode);
        stream.remove(PdfName.DecodeParms);

        final PdfDictionary newParamsDict = new PdfDictionary();
        PdfStreamUtil.applyFilter(stream, new ASCII85CompressionStrategy() {
            @Override
            public PdfObject getDecodeParams() {
                return newParamsDict;
            }
        });

        Assertions.assertArrayEquals(TEST_DATA_HEX_85, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(TEST_DATA_HEX_85.length), stream.get(PdfName.Length));
        Assertions.assertTrue(stream.get(PdfName.Filter).isArray());
        Assertions.assertEquals(
                List.of(PdfName.ASCII85Decode, PdfName.ASCIIHexDecode),
                ((PdfArray) stream.get(PdfName.Filter)).toList()
        );
        Assertions.assertTrue(stream.get(PdfName.DecodeParms).isArray());
        Assertions.assertEquals(
                List.of(newParamsDict, PdfNull.PDF_NULL),
                ((PdfArray) stream.get(PdfName.DecodeParms)).toList()
        );
    }

    @Test
    void applyFilter_85OverHexWithDictParams() throws IOException {
        final PdfStream stream = new PdfStream(TEST_DATA_HEX);
        stream.put(PdfName.Length, new PdfNumber(TEST_DATA_HEX.length));
        stream.put(PdfName.Filter, PdfName.ASCIIHexDecode);
        final PdfDictionary oldParamsDict = new PdfDictionary();
        stream.put(PdfName.DecodeParms, oldParamsDict);

        PdfStreamUtil.applyFilter(stream, new ASCII85CompressionStrategy());

        Assertions.assertArrayEquals(TEST_DATA_HEX_85, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(TEST_DATA_HEX_85.length), stream.get(PdfName.Length));
        Assertions.assertTrue(stream.get(PdfName.Filter).isArray());
        Assertions.assertEquals(
                List.of(PdfName.ASCII85Decode, PdfName.ASCIIHexDecode),
                ((PdfArray) stream.get(PdfName.Filter)).toList()
        );
        Assertions.assertTrue(stream.get(PdfName.DecodeParms).isArray());
        Assertions.assertEquals(
                List.of(PdfNull.PDF_NULL, oldParamsDict),
                ((PdfArray) stream.get(PdfName.DecodeParms)).toList()
        );
    }

    @Test
    void applyFilter_85OverHexWithFilterAndParamArrays() throws IOException {
        final PdfStream stream = new PdfStream(TEST_DATA_HEX);
        stream.put(PdfName.Length, new PdfNumber(TEST_DATA_HEX.length));
        stream.put(PdfName.Filter, new PdfArray(PdfName.ASCIIHexDecode));
        final PdfDictionary oldParamsDict = new PdfDictionary();
        stream.put(PdfName.DecodeParms, new PdfArray(oldParamsDict));

        PdfStreamUtil.applyFilter(stream, new ASCII85CompressionStrategy());

        Assertions.assertArrayEquals(TEST_DATA_HEX_85, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(TEST_DATA_HEX_85.length), stream.get(PdfName.Length));
        Assertions.assertTrue(stream.get(PdfName.Filter).isArray());
        Assertions.assertEquals(
                List.of(PdfName.ASCII85Decode, PdfName.ASCIIHexDecode),
                ((PdfArray) stream.get(PdfName.Filter)).toList()
        );
        Assertions.assertTrue(stream.get(PdfName.DecodeParms).isArray());
        Assertions.assertEquals(
                List.of(PdfNull.PDF_NULL, oldParamsDict),
                ((PdfArray) stream.get(PdfName.DecodeParms)).toList()
        );
    }

    @Test
    void applyFilter_85WithParamsOverHexWithEmptyParamArray() throws IOException {
        final PdfStream stream = new PdfStream(TEST_DATA_HEX);
        stream.put(PdfName.Length, new PdfNumber(TEST_DATA_HEX.length));
        stream.put(PdfName.Filter, new PdfArray(PdfName.ASCIIHexDecode));
        stream.put(PdfName.DecodeParms, new PdfArray());

        final PdfDictionary newParamsDict = new PdfDictionary();
        PdfStreamUtil.applyFilter(stream, new ASCII85CompressionStrategy() {
            @Override
            public PdfObject getDecodeParams() {
                return newParamsDict;
            }
        });

        Assertions.assertArrayEquals(TEST_DATA_HEX_85, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(TEST_DATA_HEX_85.length), stream.get(PdfName.Length));
        Assertions.assertTrue(stream.get(PdfName.Filter).isArray());
        Assertions.assertEquals(
                List.of(PdfName.ASCII85Decode, PdfName.ASCIIHexDecode),
                ((PdfArray) stream.get(PdfName.Filter)).toList()
        );
        Assertions.assertTrue(stream.get(PdfName.DecodeParms).isArray());
        Assertions.assertEquals(
                List.of(newParamsDict, PdfNull.PDF_NULL),
                ((PdfArray) stream.get(PdfName.DecodeParms)).toList()
        );
    }

    @Test
    void applyFilter_HexOver85OverHexWithInvalidParamArray() throws IOException {
        final PdfStream stream = new PdfStream(TEST_DATA_HEX_85);
        stream.put(PdfName.Length, new PdfNumber(TEST_DATA_HEX_85.length));
        stream.put(PdfName.Filter, new PdfArray(List.of(PdfName.ASCII85Decode, PdfName.ASCIIHexDecode)));
        final PdfDictionary oldParamsDict = new PdfDictionary();
        stream.put(PdfName.DecodeParms, oldParamsDict);

        PdfStreamUtil.applyFilter(stream, new ASCIIHexCompressionStrategy());

        Assertions.assertArrayEquals(TEST_DATA_HEX_85_HEX, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(
                new PdfNumber(TEST_DATA_HEX_85_HEX.length),
                stream.get(PdfName.Length)
        );
        Assertions.assertTrue(stream.get(PdfName.Filter).isArray());
        Assertions.assertEquals(
                List.of(PdfName.ASCIIHexDecode, PdfName.ASCII85Decode, PdfName.ASCIIHexDecode),
                ((PdfArray) stream.get(PdfName.Filter)).toList()
        );
        Assertions.assertTrue(stream.get(PdfName.DecodeParms).isArray());
        Assertions.assertEquals(
                List.of(PdfNull.PDF_NULL, oldParamsDict, PdfNull.PDF_NULL),
                ((PdfArray) stream.get(PdfName.DecodeParms)).toList()
        );
    }

    @Test
    void setDataWithFilter_ascii85Strategy() throws IOException {
        final PdfStream stream = createHexStream();

        PdfStreamUtil.setDataWithFilter(stream, TEST_DATA, new ASCII85CompressionStrategy());

        Assertions.assertArrayEquals(TEST_DATA_85, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(TEST_DATA_85.length), stream.get(PdfName.Length));
        Assertions.assertEquals(PdfName.ASCII85Decode, stream.get(PdfName.Filter));
        Assertions.assertFalse(stream.containsKey(PdfName.DecodeParms));
    }

    @Test
    void setDataWithFilter_nullStrategy() throws IOException {
        final PdfStream stream = createHexStream();
        final byte[] newData = {0x01, 0x02, 0x03};

        PdfStreamUtil.setDataWithFilter(stream, newData, null);

        Assertions.assertArrayEquals(newData, stream.getBytes(false));
        Assertions.assertArrayEquals(newData, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(newData.length), stream.get(PdfName.Length));
        Assertions.assertFalse(stream.containsKey(PdfName.Filter));
        Assertions.assertFalse(stream.containsKey(PdfName.DecodeParms));
    }

    @Test
    void setDataWithFilter_withDictDecodeParams() throws IOException {
        final PdfStream stream = createHexStream();

        PdfStreamUtil.setDataWithFilter(stream, TEST_DATA, new ASCII85CompressionStrategy() {
            @Override
            public PdfObject getDecodeParams() {
                return new PdfDictionary();
            }
        });

        Assertions.assertArrayEquals(TEST_DATA_85, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(TEST_DATA_85.length), stream.get(PdfName.Length));
        Assertions.assertEquals(PdfName.ASCII85Decode, stream.get(PdfName.Filter));
        Assertions.assertTrue(stream.get(PdfName.DecodeParms).isDictionary());
    }

    @Test
    void setDataWithFilter_withInvalidDecodeParams() throws IOException {
        final PdfStream stream = createHexStream();

        PdfStreamUtil.setDataWithFilter(stream, TEST_DATA, new ASCII85CompressionStrategy() {
            @Override
            public PdfObject getDecodeParams() {
                return new PdfNumber(1);
            }
        });

        Assertions.assertArrayEquals(TEST_DATA_85, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(TEST_DATA_85.length), stream.get(PdfName.Length));
        Assertions.assertEquals(PdfName.ASCII85Decode, stream.get(PdfName.Filter));
        Assertions.assertFalse(stream.containsKey(PdfName.DecodeParms));
    }

    @Test
    void removeAllFilters() {
        final PdfStream stream = createHexStream();

        PdfStreamUtil.removeAllFilters(stream);

        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(false));
        Assertions.assertArrayEquals(TEST_DATA, stream.getBytes(true));
        Assertions.assertEquals(CompressionConstants.NO_COMPRESSION, stream.getCompressionLevel());
        Assertions.assertEquals(new PdfNumber(TEST_DATA.length), stream.get(PdfName.Length));
        Assertions.assertFalse(stream.containsKey(PdfName.Filter));
        Assertions.assertFalse(stream.containsKey(PdfName.DecodeParms));
    }

    private static PdfStream createHexStream() {
        final PdfStream stream = new PdfStream(TEST_DATA_HEX);
        stream.put(PdfName.Length, new PdfNumber(TEST_DATA_HEX.length));
        stream.put(PdfName.Filter, PdfName.ASCIIHexDecode);
        stream.put(PdfName.DecodeParms, PdfNull.PDF_NULL);
        return stream;
    }
}
