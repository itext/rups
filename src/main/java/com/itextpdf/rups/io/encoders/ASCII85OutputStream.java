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
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An output stream that encodes data according to the {@code ASCII85Decode}
 * filter from the PDF specification.
 */
public class ASCII85OutputStream extends FilterOutputStream implements IFinishable {
    private static final int BASE = 85;
    /**
     * Offset to the first base-85 output char.
     */
    private static final int OFFSET = 33;
    /**
     * Size of the encoding block. After this amount of bytes data is converted
     * and flush to the backing stream.
     */
    private static final int INPUT_LENGTH = 4;
    /**
     * Amount of bytes produced from a block of input bytes.
     */
    private static final int OUTPUT_LENGTH = 5;
    /**
     * Marker written, when all input bytes are zero. Not used for partial
     * blocks.
     */
    private static final byte ALL_ZEROS_MARKER = 'z';
    /**
     * End Of Data marker.
     */
    private static final byte[] EOD = new byte[]{'~', '>'};

    /**
     * Encoding block buffer. Reused for encoding output, when flushing.
     */
    private final byte[] buffer = new byte[OUTPUT_LENGTH];
    /**
     * Bitwise OR of all bytes within the encoding block. Used to quickly
     * check, whether the encoding block contains only zeros.
     */
    private int inputOr = 0;
    /**
     * Input bytes cursor within the buffer.
     */
    private int inputCursor = 0;

    /**
     * Flag for detecting, whether {@link #finish} has been called.
     */
    private final AtomicBoolean finished = new AtomicBoolean(false);

    /**
     * Creates a new {@code ASCIIHexDecode} encoding stream.
     *
     * @param out the output stream to write encoded data to
     */
    public ASCII85OutputStream(OutputStream out) {
        super(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException {
        int value = b & 0xFF;
        buffer[inputCursor] = (byte) value;
        inputOr |= value;
        ++inputCursor;
        writeBufferIfFull();
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
        // Writing the remainder
        if (inputCursor > 0) {
            if (inputOr == 0) {
                // If all zeros, output is just n + 1 exclamation points
                Arrays.fill(buffer, 0, inputCursor + 1, (byte) '!');
            } else {
                Arrays.fill(buffer, inputCursor, INPUT_LENGTH, (byte) 0);
                convertBuffer();
            }
            out.write(buffer, 0, inputCursor + 1);
            resetBuffer();
        }
        out.write(EOD);
        flush();
    }

    private void writeBufferIfFull() throws IOException {
        if (inputCursor < INPUT_LENGTH) {
            return;
        }
        if (inputOr == 0) {
            // Special case, if all zeros
            out.write(ALL_ZEROS_MARKER);
        } else {
            convertBuffer();
            out.write(buffer);
        }
        resetBuffer();
    }

    private void resetBuffer() {
        inputOr = 0;
        inputCursor = 0;
    }

    private void convertBuffer() {
        long num = ((buffer[0] & 0xFFL) << 24)
                | ((buffer[1] & 0xFFL) << 16)
                | ((buffer[2] & 0xFFL) << 8)
                | (buffer[3] & 0xFFL);
        for (int i = OUTPUT_LENGTH - 1; i >= 0; --i) {
            buffer[i] = (byte) (OFFSET + (num % BASE));
            num /= BASE;
        }
    }
}
