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

import com.itextpdf.io.source.IFinishable;
import com.itextpdf.kernel.pdf.CompressionConstants;
import com.itextpdf.kernel.pdf.IStreamCompressionStrategy;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNull;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class PdfStreamUtil {
    private PdfStreamUtil() {
        // Static class
    }

    public static void applyFilter(PdfStream stream, IStreamCompressionStrategy strategy)
            throws IOException {
        // Creating all the data first, so that the stream is still untouched
        // in case of an exception
        final byte[] encodedBytes = encode(stream.getBytes(false), strategy);
        final PdfObject oldFilterValue = stream.get(PdfName.Filter);
        final int oldFilterCount = getFilterCount(oldFilterValue);
        final PdfObject newFilterValue = createNewFilterValue(oldFilterValue, strategy);
        final PdfObject newDecodeParamsValue = createNewDecodeParamsValue(
                stream.get(PdfName.DecodeParms), oldFilterCount, strategy
        );
        // Now applying everything to the stream itself
        stream.setData(encodedBytes);
        stream.setCompressionLevel(CompressionConstants.NO_COMPRESSION);
        stream.put(PdfName.Length, new PdfNumber(encodedBytes.length));
        stream.put(PdfName.Filter, newFilterValue);
        if (newDecodeParamsValue.isNull()) {
            stream.remove(PdfName.DecodeParms);
        } else {
            stream.put(PdfName.DecodeParms, newDecodeParamsValue);
        }
    }

    public static void setDataWithFilter(PdfStream stream, byte[] data, IStreamCompressionStrategy strategy)
            throws IOException {
        byte[] encodedBytes = data;
        PdfObject filterValue = PdfNull.PDF_NULL;
        PdfObject decodeParamsValue = PdfNull.PDF_NULL;
        if (strategy != null) {
            encodedBytes = encode(data, strategy);
            filterValue = strategy.getFilterName();
            decodeParamsValue = getDecodeParams(strategy);
        }
        stream.setData(encodedBytes);
        stream.setCompressionLevel(CompressionConstants.NO_COMPRESSION);
        stream.put(PdfName.Length, new PdfNumber(encodedBytes.length));
        if (filterValue.isNull()) {
            stream.remove(PdfName.Filter);
        } else {
            stream.put(PdfName.Filter, filterValue);
        }
        if (decodeParamsValue.isNull()) {
            stream.remove(PdfName.DecodeParms);
        } else {
            stream.put(PdfName.DecodeParms, decodeParamsValue);
        }
    }

    public static void removeAllFilters(PdfStream stream) {
        final byte[] decodedBytes = stream.getBytes();
        stream.setData(decodedBytes);
        stream.setCompressionLevel(CompressionConstants.NO_COMPRESSION);
        stream.put(PdfName.Length, new PdfNumber(decodedBytes.length));
        stream.remove(PdfName.Filter);
        stream.remove(PdfName.DecodeParms);
    }

    private static byte[] encode(byte[] original, IStreamCompressionStrategy strategy)
            throws IOException {
        final ByteArrayOutputStream target = new ByteArrayOutputStream(original.length);
        // At the moment we need to pass a stream, but the only information
        // used is the compression level, so we will make a dummy
        final PdfStream dummyStream = new PdfStream();
        dummyStream.setCompressionLevel(CompressionConstants.DEFAULT_COMPRESSION);
        try (final OutputStream compressor = strategy.createNewOutputStream(target, dummyStream)) {
            compressor.write(original);
            ((IFinishable) compressor).finish();
        }
        return target.toByteArray();
    }

    private static PdfObject createNewFilterValue(PdfObject oldValue, IStreamCompressionStrategy strategy) {
        if (oldValue == null) {
            return strategy.getFilterName();
        }
        if (oldValue.isArray()) {
            final PdfArray newValue = new PdfArray(strategy.getFilterName());
            newValue.addAll((PdfArray) oldValue);
            return newValue;
        }
        if (oldValue.isName()) {
            final PdfArray newValue = new PdfArray(strategy.getFilterName());
            newValue.add(oldValue);
            return newValue;
        }
        return strategy.getFilterName();
    }

    private static PdfObject createNewDecodeParamsValue(
            PdfObject oldValue,
            int oldFilterCount,
            IStreamCompressionStrategy strategy
    ) {
        final PdfObject prependDecodeParams = getDecodeParams(strategy);
        // Assuming current DecodeParams are valid...
        if (oldValue != null && oldValue.isArray()) {
            final PdfArray oldValueArray = (PdfArray) oldValue;
            final PdfArray newValue = new PdfArray(prependDecodeParams);
            // We will also handle cases, when there was a size mismatch already
            for (int i = 0; i < Math.min(oldFilterCount, oldValueArray.size()); ++i) {
                newValue.add(oldValueArray.get(i, false));
            }
            for (int i = Math.min(oldFilterCount, oldValueArray.size()); i < oldFilterCount; ++i) {
                newValue.add(PdfNull.PDF_NULL);
            }
            return newValue;
        }
        if (oldValue != null && oldValue.isDictionary()) {
            final PdfArray newValue = new PdfArray(prependDecodeParams);
            newValue.add(oldValue);
            // We will also handle cases, when there was a size mismatch already
            for (int i = 1; i < oldFilterCount; ++i) {
                newValue.add(PdfNull.PDF_NULL);
            }
            return newValue;
        }
        if (!prependDecodeParams.isNull() && oldFilterCount > 0) {
            final PdfArray newValue = new PdfArray(prependDecodeParams);
            for (int i = 0; i < oldFilterCount; ++i) {
                newValue.add(PdfNull.PDF_NULL);
            }
            return newValue;
        }
        return prependDecodeParams;
    }

    private static int getFilterCount(PdfObject filterValue) {
        if (filterValue == null) {
            return 0;
        }
        if (filterValue.isArray()) {
            return ((PdfArray) filterValue).size();
        }
        if (filterValue.isName()) {
            return 1;
        }
        return 0;
    }

    private static PdfObject getDecodeParams(IStreamCompressionStrategy strategy) {
        final PdfObject decodeParams = strategy.getDecodeParams();
        if (decodeParams == null || !decodeParams.isDictionary()) {
            return PdfNull.PDF_NULL;
        }
        return decodeParams;
    }
}
