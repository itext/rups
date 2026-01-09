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

import com.itextpdf.kernel.pdf.IStreamCompressionStrategy;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;

import java.io.OutputStream;

/**
 * A compression strategy that uses the {@code ASCIIHexDecode} filter for PDF
 * streams.
 *
 * <p>
 * This strategy implements the {@link IStreamCompressionStrategy} interface
 * and provides {@code ASCIIHexDecode} encoding.
 *
 * <p>
 * The strategy ensures, that streams are saved using just 7-bit ASCII
 * characters, but it doubles the sizes of streams compared to just saving
 * them as-is. So calling this a "compression strategy" is a misnomer.
 */
public class ASCIIHexCompressionStrategy implements IStreamCompressionStrategy {
    /**
     * Constructs a new {@link ASCIIHexCompressionStrategy} instance.
     */
    public ASCIIHexCompressionStrategy() {
        // empty constructor
    }

    /**
     * Returns the name of the compression filter.
     *
     * @return {@link PdfName#ASCIIHexDecode} representing the {@code ASCIIHexDecode} filter
     */
    @Override
    public PdfName getFilterName() {
        return PdfName.ASCIIHexDecode;
    }

    /**
     * Returns the decode parameters for the {@code ASCIIHexDecode} filter.
     * <p>
     * This implementation returns {@code null} as no special decode parameters
     * are required for standard ASCIIHex compression.
     *
     * @return {@code null} as no decode parameters are needed
     */
    @Override
    public PdfObject getDecodeParams() {
        return null;
    }

    /**
     * Creates a new output stream with ASCIIHex compression applied.
     * <p>
     * This method wraps the original output stream in a {@link ASCIIHexOutputStream}
     * that applies ASCIIHex compression.
     *
     * @param original the original output stream to wrap
     * @param stream   the PDF stream containing compression configuration
     *
     * @return a new {@link ASCIIHexOutputStream} that compresses data using the ASCIIHex algorithm
     */
    @Override
    public OutputStream createNewOutputStream(OutputStream original, PdfStream stream) {
        return new ASCIIHexOutputStream(original);
    }
}
