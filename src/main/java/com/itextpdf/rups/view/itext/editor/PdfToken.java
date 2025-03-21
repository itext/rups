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

import java.awt.Rectangle;
import java.lang.reflect.Method;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenImpl;
import org.fife.ui.rsyntaxtextarea.TokenPainter;

/**
 * {@link Token} implementation, which respect the painter of the text area,
 * when calculating lengths and offsets.
 *
 * <p>
 * Overridden code is <i>heavily</i> inspired by the original implementation
 * in {@link TokenImpl}.
 * </p>
 */
public final class PdfToken extends TokenImpl {
    /*
     * For some reason caret positioning logic in RSyntaxTextArea does not
     * take the painter into the account. It calls methods within the Token
     * interface, which try to calculate the text width on its own.
     *
     * This works fine in the default configuration, when both painter and
     * TokenImpl has the same logic to calculate text width (i.e. just
     * rendering text as-is). But since we want to show non-ASCII symbols
     * differently, this no longer works.
     *
     * Ideally, we should just reuse methods in the Painter to calculate
     * widths of what will be drawn. And this would work, but for some reason
     * RSyntaxTextArea#getTokenPainter is declared package-private and cannot
     * be accessed by a custom implementation.
     *
     * So we have a nasty reflection here for the time being to get access to
     * that painter instead of hardcoding our own here.
     */
    private static final Method GET_TOKEN_PAINTER_METHOD;

    static {
        try {
            GET_TOKEN_PAINTER_METHOD = RSyntaxTextArea.class.getDeclaredMethod("getTokenPainter");
            GET_TOKEN_PAINTER_METHOD.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public PdfToken() {
    }

    public PdfToken(Segment line, int beg, int end, int startOffset, int type, int languageIndex) {
        super(line, beg, end, startOffset, type, languageIndex);
    }

    public PdfToken(char[] line, int beg, int end, int startOffset, int type, int languageIndex) {
        super(line, beg, end, startOffset, type, languageIndex);
    }

    public PdfToken(Token t2) {
        super(t2);
    }

    @Override
    public int getListOffset(RSyntaxTextArea textArea, TabExpander e, float x0, float x) {
        int offset = getOffset();

        // If the coordinate in question is before this line's start, quit.
        if (x0 >= x) {
            return offset;
        }

        final TokenPainter painter = getTokenPainter(textArea);
        Token token = this;
        float startX = x0;
        float avgWidthPerChar = 0;
        while (token != null && token.isPaintable()) {
            final float endX = painter.nextX(token, token.length(), startX, textArea, e);
            // Found the token for the offset
            if (x < endX) {
                avgWidthPerChar = (endX - startX) / token.length();
                break;
            }
            startX = endX;
            offset += token.length();
            token = token.getNextToken();
        }

        // If we didn't find anything, return the end position of the text.
        if (token == null || !token.isPaintable()) {
            return offset;
        }

        // Search for the char offset now
        final int hint = (int) ((x - startX) / avgWidthPerChar);
        final int charCount = getCharCountBeforeX(textArea, e, painter, token, startX, x, hint);
        offset += charCount;

        // Checking if closer to next char
        if (charCount < token.length()) {
            final float prevX = painter.nextX(token, charCount, startX, textArea, e);
            final float nextX = painter.nextX(token, charCount + 1, startX, textArea, e);
            if ((x - prevX) > (nextX - x)) {
                ++offset;
            }
        }

        return offset;
    }

    @Override
    public int getOffsetBeforeX(RSyntaxTextArea textArea, TabExpander e, float startX, float endBeforeX) {
        final int textLength = length();
        // Same as in TokenImpl, 1 length token always fit to avoid inf loop
        if (textLength <= 1) {
            return getOffset();
        }
        final TokenPainter painter = getTokenPainter(textArea);
        final int charCount = getCharCountBeforeX(textArea, e, painter, this, startX, endBeforeX, 2);
        return getOffset() + charCount - 1;
    }

    @Override
    public float getWidthUpTo(int numChars, RSyntaxTextArea textArea, TabExpander e, float x0) {
        final TokenPainter painter = getTokenPainter(textArea);
        return painter.nextX(this, numChars, x0, textArea, e) - x0;
    }

    @Override
    public Rectangle listOffsetToView(RSyntaxTextArea textArea, TabExpander e, int pos, int x0, Rectangle rect) {
        final TokenPainter painter = getTokenPainter(textArea);
        Token token = this;
        float startX = x0;
        while (token != null && token.isPaintable()) {
            if (token.containsPosition(pos)) {
                final int charOffset = pos - token.getOffset();
                final float endX = painter.nextX(token, charOffset + 1, startX, textArea, e);
                if (charOffset > 0) {
                    startX = painter.nextX(token, charOffset, startX, textArea, e);
                }
                rect.x = (int) startX;
                rect.width = (int) (endX - startX);
                return rect;
            }
            startX = painter.nextX(token, token.length(), startX, textArea, e);
            token = token.getNextToken();
        }

        // If we didn't find anything, we're at the end of the line. Return
        // a width of 1 (so selection highlights don't extend way past line's
        // text). A ConfigurableCaret will know to paint itself with a larger
        // width.
        rect.x = (int) startX;
        rect.width = 1;
        return rect;
    }

    private static int getCharCountBeforeX(RSyntaxTextArea textArea, TabExpander e, TokenPainter painter,
            Token token, float startX, float endBeforeX, int hint) {
        final float width = endBeforeX - startX;
        int left = 0;
        int right = token.length();
        int current = Math.max(1, Math.min(hint, token.length()));
        while (left < right) {
            final float x = painter.nextX(token, current, startX, textArea, e);
            final float avgWidthPerChar = (x - startX) / current;
            final int expectedCharCount = (int) (width / avgWidthPerChar);
            if (x <= endBeforeX) {
                left = current;
                current = Math.min(expectedCharCount + 1, right);
            } else {
                right = current - 1;
                current = Math.max(expectedCharCount, left);
            }
        }
        return left;
    }

    private static TokenPainter getTokenPainter(RSyntaxTextArea host) {
        try {
            return (TokenPainter) GET_TOKEN_PAINTER_METHOD.invoke(host);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
