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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import javax.swing.text.TabExpander;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenPainter;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

/**
 * Special {@link TokenPainter} implementation for working with
 * {@link PdfTokenMaker}.
 *
 * <p>
 * As of now, base logic of the this painter is the same as
 * {@link org.fife.ui.rsyntaxtextarea.DefaultTokenPainter} with most of the
 * code copied from there, but with the following changes:
 * </p>
 *
 * <ol>
 *     <li>
 *     Tokens of the {@link PdfTokenTypes#BINARY_DATA} are not painted as text,
 *     but as boxes with hex codes of the lower byte of each character.
 *     </li>
 *     <li>
 *     For all other tokens, only HT, LF and visible ASCII characters are
 *     painted as characters. Other character are painted as if they belong
 *     to a {@link PdfTokenTypes#BINARY_DATA} token.
 *     </li>
 * </ol>
 *
 * <p>
 * This painter should be used together with the {@link Latin1Filter}, as it
 * expects the input character arrays to, essentially, be a byte array, where
 * each byte is padded to two bytes.
 * </p>
 */
public final class PdfTokenPainter implements TokenPainter {
    /**
     * Character used to pad the hex representation of "binary characters"
     * horizontally.
     *
     * <p>
     * Currently this is THIN SPACE U+2009.
     * </p>
     */
    private static final char PAD_CHAR = '\u2009';
    /**
     * Border stroke used to highlight "binary characters". It is drawn over
     * the whole sequence, not individual characters.
     */
    private static final BasicStroke BINARY_BORDER_STROKE = new BasicStroke(
            1F, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER
    );
    /**
     * Array for mapping 4-bit integers to their hex display character.
     */
    private static final char[] NIBBLE_TO_HEX_MAP = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    };
    /**
     * Array for mapping a single byte character code to whether it should be
     * rendered as binary data or not.
     */
    private static final boolean[] IS_BINARY_MAP = {
             true,  true,  true,  true,  true,  true,  true,  true,  true, false,
            false,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
             true,  true,  true,  true,  true,  true,
    };

    /**
     * Cache for {@link FontMetrics} data from {@link RSyntaxTextArea}.
     *
     * <p>
     * This cache is pretty important, as this code is performance-sensitive
     * and {@link RSyntaxTextArea#getFontMetricsForTokenType(int)} calls are
     * heavy enough to light up in the profiler.
     * </p>
     */
    private final FontMetricsCache fontMetricsCache = new FontMetricsCache();
    /**
     * Buffer used to group characters, which could be rendered the same way
     * (binary or text) in one go.
     *
     * <p>
     * This is just a pre-allocated buffer object for performance.
     * </p>
     */
    private final FlushBuffer flushBuffer = new FlushBuffer(null, 0, 0, FlushType.TEXT);
    /**
     * Temporary buffer used to store display text representation of binary
     * data.
     *
     * <p>
     * This is just a pre-allocated buffer for performance.
     * </p>
     */
    private final RawBuffer binaryDisplayBuffer = new RawBuffer(new char[0]);

    /**
     * Creates a new token painter.
     *
     * @param textArea Text area, for which token painter will be used for.
     */
    public PdfTokenPainter(RSyntaxTextArea textArea) {
        // This is pretty important to synchronize our font cache with
        // possible style changes
        textArea.addPropertyChangeListener(
                RSyntaxTextArea.SYNTAX_SCHEME_PROPERTY,
                fontMetricsCache
        );
    }

    @Override
    public float nextX(Token token, int charCount, float x, RSyntaxTextArea host, TabExpander e) {
        final FontMetrics fontMetrics = fontMetricsCache.get(host, token);
        final char[] text = token.getTextArray();
        final int textBegin = token.getTextOffset();
        final int textEnd = textBegin + charCount;
        flushBuffer.reset(text, textBegin);
        float nextX = x;
        for (int textIndex = textBegin; textIndex < textEnd; ++textIndex) {
            final char ch = text[textIndex];

            if (isBinaryData(token, ch)) {
                if (flushBuffer.type != FlushType.BINARY) {
                    nextX += charsWidth(fontMetrics, getFlushChars());
                    flushBuffer.moveOffset(textIndex, FlushType.BINARY);
                }
                ++flushBuffer.length;
            } else if (ch != '\t') {
                if (flushBuffer.type != FlushType.TEXT) {
                    nextX += charsWidth(fontMetrics, getFlushChars());
                    flushBuffer.moveOffset(textIndex, FlushType.TEXT);
                }
                ++flushBuffer.length;
            } else {
                nextX += charsWidth(fontMetrics, getFlushChars());
                nextX = e.nextTabStop(nextX, 0);
                flushBuffer.moveOffset(textIndex + 1, FlushType.TEXT);
            }
        }
        nextX += charsWidth(fontMetrics, getFlushChars());

        return nextX;
    }

    @Override
    public float paint(Token token, Graphics2D g, float x, float y, RSyntaxTextArea host, TabExpander e) {
        return paintImpl(token, g, x, y, host, e, 0, false, false);
    }

    @Override
    public float paint(Token token, Graphics2D g, float x, float y, RSyntaxTextArea host, TabExpander e,
            float clipStart) {
        return paintImpl(token, g, x, y, host, e, clipStart, false, false);
    }

    @Override
    public float paint(Token token, Graphics2D g, float x, float y, RSyntaxTextArea host, TabExpander e,
            float clipStart, boolean paintBG) {
        return paintImpl(token, g, x, y, host, e, clipStart, !paintBG, false);
    }

    @Override
    public float paintSelected(Token token, Graphics2D g, float x, float y, RSyntaxTextArea host, TabExpander e,
            boolean useSTC) {
        return paintImpl(token, g, x, y, host, e, 0, true, useSTC);
    }

    @Override
    public float paintSelected(Token token, Graphics2D g, float x, float y, RSyntaxTextArea host, TabExpander e,
            float clipStart, boolean useSTC) {
        return paintImpl(token, g, x, y, host, e, clipStart, true, useSTC);
    }

    private float paintImpl(Token token, Graphics2D g, float x, float y,
            RSyntaxTextArea host, TabExpander e, float clipStart,
            boolean selected, boolean useSTC) {
        g.setFont(host.getFontForToken(token));
        final FontMetrics fm = fontMetricsCache.get(host, token);
        final Color bg = getBackgroundColor(token, host, selected);
        final Color fg = getForegroundColor(token, host, useSTC);
        final char[] text = token.getTextArray();
        final int textBegin = token.getTextOffset();
        final int textEnd = textBegin + token.length();
        flushBuffer.reset(text, textBegin);
        float nextX = x;
        for (int textIndex = textBegin; textIndex < textEnd; ++textIndex) {
            final char ch = text[textIndex];

            if (isBinaryData(token, ch)) {
                if (flushBuffer.type != FlushType.BINARY) {
                    nextX = drawFlushChars(g, fm, fg, bg, nextX, y, clipStart);
                    flushBuffer.moveOffset(textIndex, FlushType.BINARY);
                }
                ++flushBuffer.length;
            } else if (ch != '\t') {
                if (flushBuffer.type != FlushType.TEXT) {
                    nextX = drawFlushChars(g, fm, fg, bg, nextX, y, clipStart);
                    flushBuffer.moveOffset(textIndex, FlushType.TEXT);
                }
                ++flushBuffer.length;
            } else {
                final float currX = drawFlushChars(g, fm, fg, bg, nextX, y, clipStart);
                nextX = e.nextTabStop(currX, 0);
                if (nextX >= clipStart) {
                    drawBackground(g, fm, bg, currX, y, (int) (nextX - currX));
                }
                flushBuffer.moveOffset(textIndex + 1, FlushType.TEXT);
            }
        }
        nextX = drawFlushChars(g, fm, fg, bg, nextX, y, clipStart);

        // Underline
        if (nextX >= clipStart && host.getUnderlineForToken(token)) {
            final int underlineY = (int) y + 1;
            g.drawLine((int) x, underlineY, (int) nextX, underlineY);
        }

        // Ignoring PaintTabLines from DefaultTokenPainter for now

        return nextX;
    }

    /**
     * Returns the character sequence to paint for the current flush buffer
     * state.
     *
     * @return The character sequence to paint for the current flush buffer
     * state.
     */
    private RawBuffer getFlushChars() {
        switch (flushBuffer.type) {
            default:
            case TEXT:
                return flushBuffer;
            case BINARY:
                return prepareBinaryDataDisplay(flushBuffer);
        }
    }
    /**
     * Renders characters currently stored in the flush buffer.
     *
     * @param g         The graphics context in which to draw.
     * @param fm        Font metrics of the font used. Should already be set in the
     *                  context.
     * @param fg        Foreground color to use.
     * @param bg        Background color to use.
     * @param x         The x-coordinate at which to draw.
     * @param y         The y-coordinate at which to draw.
     * @param clipStart Whether to start clipping, or {@code 0} to clip nothing.
     *
     * @return The x-coordinate representing the end of the painted text.
     */
    private float drawFlushChars(Graphics2D g, FontMetrics fm, Color fg, Color bg, float x, float y, float clipStart) {
        final RawBuffer chars = getFlushChars();
        final int width = charsWidth(fm, chars);
        if (width <= 0) {
            return x;
        }

        final float nextX = x + width;
        if (nextX >= clipStart) {
            drawBackground(g, fm, bg, x, y, width);

            // Box (to signify binary block)
            g.setColor(fg);
            if (flushBuffer.type == FlushType.BINARY) {
                g.setStroke(BINARY_BORDER_STROKE);
                g.drawRect(
                        (int) (x),
                        (int) (y - fm.getAscent() + BINARY_BORDER_STROKE.getLineWidth()),
                        width,
                        (int) (fm.getHeight() - 2 * BINARY_BORDER_STROKE.getLineWidth())
                );
            }

            // Text
            g.drawChars(chars.text, chars.offset, chars.length, (int) x, (int) y);
        }

        return nextX;
    }

    /**
     * Prepare the binary data for painting based on input bytes. At the end
     * {@code binaryDisplayBuffer} will be filled with display characters.
     *
     * @param bytes Bytes to display.
     *
     * @return Filled {@code binaryDisplayBuffer}.
     */
    private RawBuffer prepareBinaryDataDisplay(RawBuffer bytes) {
        int bytesIndex = bytes.offset;
        // 2 pad chars + 2 hex digits
        final int displaySize = 4 * bytes.length;
        if (binaryDisplayBuffer.text.length < displaySize) {
            binaryDisplayBuffer.text = new char[displaySize];
        }
        for (int i = 0; i < displaySize; i += 4) {
            final int ch = bytes.text[bytesIndex];
            assert 0 < ch && ch <= 0xFF;
            binaryDisplayBuffer.text[i]     = PAD_CHAR;
            binaryDisplayBuffer.text[i + 1] = NIBBLE_TO_HEX_MAP[(ch >>> 4) & 0xF];
            binaryDisplayBuffer.text[i + 2] = NIBBLE_TO_HEX_MAP[ch & 0xF];
            binaryDisplayBuffer.text[i + 3] = PAD_CHAR;
            bytesIndex++;
        }
        binaryDisplayBuffer.length = displaySize;
        return binaryDisplayBuffer;
    }

    /**
     * Returns foreground color for token.
     *
     * @param token  The token to render.
     * @param host   The text area this token is in.
     * @param useSTC Whether to use the text area's "selected text color."
     *
     * @return Foreground color for the token.
     */
    private static Color getForegroundColor(Token token, RSyntaxTextArea host, boolean useSTC) {
        if (useSTC) {
            return host.getSelectedTextColor();
        }
        return host.getForegroundForToken(token);
    }

    /**
     * Returns background color for token.
     *
     * @param token    The token to render.
     * @param host     The text area this token is in.
     * @param selected Whether token is selected.
     *
     * @return Background color for the token.
     */
    private static Color getBackgroundColor(Token token, RSyntaxTextArea host, boolean selected) {
        if (selected) {
            return null;
        }
        return host.getBackgroundForToken(token);
    }

    /**
     * Returns whether the character should be painted as binary data.
     *
     * @param token Token to which the character belongs.
     * @param ch    Character to be painted.
     *
     * @return Whether the character should be painted as binary data.
     */
    private static boolean isBinaryData(Token token, char ch) {
        // With how the text area is set up with a "byte" filter, this should
        // always be true
        assert ch < IS_BINARY_MAP.length;
        return (token.getType() == PdfTokenTypes.BINARY_DATA) || IS_BINARY_MAP[ch];
    }

    private static int charsWidth(FontMetrics fontMetrics, RawBuffer chars) {
        return fontMetrics.charsWidth(chars.text, chars.offset, chars.length);
    }

    private static void drawBackground(Graphics2D g, FontMetrics fm, Color bg, float x, float y, int width) {
        if (bg == null) {
            return;
        }
        g.setColor(bg);
        g.fillRect((int) x, (int) y - fm.getAscent(), width, fm.getHeight());
    }

    /**
     * Type selection for flush buffers.
     */
    private enum FlushType {
        TEXT,
        BINARY,
    }

    /**
     * The most basic version of {@link java.nio.CharBuffer}.
     *
     * <p>
     * This is used for performance reasons, as it allows us to limit call
     * counts and index checks.
     * </p>
     */
    private static class RawBuffer {
        protected char[] text;
        protected int offset;
        protected int length;

        protected RawBuffer(char[] text) {
            this.text = text;
            this.offset = 0;
            this.length = text.length;
        }

        protected RawBuffer(char[] text, int offset, int length) {
            this.text = text;
            this.offset = offset;
            this.length = length;
        }
    }

    /**
     * Raw buffer, which tracks, which type of data is stored in it.
     */
    private static final class FlushBuffer extends RawBuffer {
        private FlushType type;

        private FlushBuffer(char[] text, int offset, int length, FlushType type) {
            super(text, offset, length);
            this.type = type;
        }

        private void moveOffset(int offset, FlushType type) {
            this.offset = offset;
            this.length = 0;
            this.type = type;
        }

        private void reset(char[] text, int offset) {
            this.text = text;
            this.offset = offset;
            this.length = 0;
            this.type = FlushType.TEXT;
        }
    }

    /**
     * Cache for {@link RSyntaxTextArea#getFontMetricsForTokenType(int)}
     * results.
     *
     * <p>
     * This was made because for some reason that method light up pretty hard
     * during profiling, so caching its value improves performance.
     * </p>
     */
    private static final class FontMetricsCache implements PropertyChangeListener {
        /**
         * Text area, for which cache currently stores result.
         */
        private RSyntaxTextArea currentHost = null;
        /**
         * Call result storage.
         */
        private final FontMetrics[] cache = new FontMetrics[TokenTypes.DEFAULT_NUM_TOKEN_TYPES];

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // If style changed, we need to reset the cache, otherwise the
            // results might be wrong
            reset();
        }

        /**
         * Calls {@code host.getFontMetricsForToken(token)} through the cache.
         *
         * @param host  Text area to call the method on.
         * @param token Token to use as the argument.
         *
         * @return Result of the call.
         */
        private FontMetrics get(RSyntaxTextArea host, Token token) {
            final int type = token.getType();
            if (host != currentHost) {
                reset();
                currentHost = host;
            }
            FontMetrics result = cache[type];
            if (result == null) {
                result = host.getFontMetricsForTokenType(type);
                cache[type] = result;
            }
            return result;
        }

        /**
         * Resets cache to its initial empty state.
         */
        private void reset() {
            Arrays.fill(cache, null);
        }
    }
}
