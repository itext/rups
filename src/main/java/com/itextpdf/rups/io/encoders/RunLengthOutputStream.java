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
 * An output stream that encodes data according to the {@code RunLengthDecode}
 * filter from the PDF specification.
 */
public class RunLengthOutputStream extends FilterOutputStream implements IFinishable {
    /**
     * Maximum length of a run. Applies to both "unique" and repeating ones.
     */
    private static final int MAX_LENGTH = 128;
    /**
     * End Of Data marker.
     */
    private static final byte EOD = (byte) 128;

    /**
     * Buffer for storing the pending run.
     */
    private final byte[] buffer = new byte[MAX_LENGTH];
    /**
     * Value, that repeats in a repeating run. Set to {@code -1}, when the
     * pending run is a "unique" one.
     */
    private int repeatValue = -1;
    /**
     * Current length of the pending run.
     */
    private int currentLength = 0;

    /**
     * Flag for detecting, whether {@link #finish} has been called.
     */
    private final AtomicBoolean finished = new AtomicBoolean(false);

    /**
     * Creates a new {@code RunLengthDecode} encoding stream.
     *
     * @param out the output stream to write encoded data to
     */
    public RunLengthOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException {
        int value = b & 0xFF;
        // Case for continuing a repeating run
        if (value == repeatValue) {
            ++currentLength;
            if (currentLength == MAX_LENGTH) {
                writePending();
            }
            return;
        }
        /*
         * If there was a repeating run, but we got a different value, then we
         * need to write the current repeating run we had and start a new
         * "unique" run.
         */
        if (repeatValue != -1) {
            writePending();
            buffer[currentLength] = (byte) value;
            ++currentLength;
            return;
        }
        /*
         * As soon as we detect a sequence of 3 or more bytes, which are the
         * same, we need to switch to a repeating run. For this we will write
         * the values before the repeated one as a "unique" run and start a
         * new repeating run at length 3.
         *
         * Technically speaking we can switch to a repeating run at 2 bytes,
         * but in the vast majority of cases this will make the compression
         * ratio worse.
         */
        if (currentLength >= 2
                && buffer[currentLength - 1] == (byte) value
                && buffer[currentLength - 2] == (byte) value) {
            currentLength -= 2;
            writePending();
            repeatValue = value;
            currentLength = 3;
            return;
        }
        // Just continuing (or starting) a "unique" run
        buffer[currentLength] = (byte) value;
        ++currentLength;
        if (currentLength == MAX_LENGTH) {
            writePending();
        }
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
        writePending();
        out.write(EOD);
        flush();
    }

    private void writePending() throws IOException {
        if (currentLength <= 0) {
            return;
        }
        if (repeatValue < 0) {
            // Writing "unique" run
            out.write(currentLength - 1);
            out.write(buffer, 0, currentLength);
        } else {
            // Writing repeating run
            out.write(257 - currentLength);
            out.write(repeatValue);
        }
        resetPending();
    }

    private void resetPending() {
        repeatValue = -1;
        currentLength = 0;
    }
}
