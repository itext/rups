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

import com.itextpdf.io.source.IFinishable;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An output stream that encodes data according to the {@code ASCIIHexDecode}
 * filter from the PDF specification.
 */
public class ASCIIHexOutputStream extends FilterOutputStream implements IFinishable {
    /**
     * End Of Data marker.
     */
    private static final byte EOD = '>';
    /**
     * Array for mapping nibble values to the corresponding lowercase
     * hexadecimal characters.
     */
    private static final byte[] CHAR_MAP = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /**
     * Buffer for storing the output hex char pair.
     */
    private final byte[] buffer = new byte[2];

    /**
     * Flag for detecting, whether {@link #finish} has been called.
     */
    private final AtomicBoolean finished = new AtomicBoolean(false);

    /**
     * Creates a new {@code ASCIIHexDecode} encoding stream.
     *
     * @param out the output stream to write encoded data to
     */
    public ASCIIHexOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException {
        int value = (b & 0xFF);
        // Writing via a 2-elem buffer, in case `write(byte[])` on the
        // underlying stream is more performant
        buffer[0] = CHAR_MAP[value >> 4];
        buffer[1] = CHAR_MAP[value & 0x0F];
        out.write(buffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        finish();
        super.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish() throws IOException {
        if (finished.getAndSet(true)) {
            return;
        }
        out.write(EOD);
        flush();
    }
}
