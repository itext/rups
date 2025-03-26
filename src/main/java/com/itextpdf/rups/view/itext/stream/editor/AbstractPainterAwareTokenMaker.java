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
package com.itextpdf.rups.view.itext.stream.editor;

import org.fife.ui.rsyntaxtextarea.TokenMakerBase;

/**
 * Base class for our custom token makers.
 *
 * <p>
 * This class really wants to just implement TokenMaker, as {@code firstToken},
 * {@code currentToken}, {@code previousToken} and {@code tokenFactory} from
 * {@link TokenMakerBase} are of no use here. But just implementing the
 * interface would force us to copy a lot of code from
 * the library, and, for some reason {@code DefaultOccurrenceMarker} is marked
 * as package-private, so we would need to reimplement that as well.
 * </p>
 *
 * <p>
 * So, at the moment, these fields from TokenMakerBase should be ignored. For
 * token manipulation, {@code firstRupsToken} and {@code lastRupsToken} should
 * be used instead.
 * </p>
 *
 * <p>
 * This class is expected to be used with a text area, which has a
 * {@link Latin1Filter} on the underlying document. This is used as a way to
 * represent a byte stream as a string.
 * </p>
 */
public abstract class AbstractPainterAwareTokenMaker extends TokenMakerBase {
    /**
     * First token in the output token list. Should be used instead of
     * {@code firstToken}.
     */
    protected PainterAwareToken firstRupsToken = null;
    /**
     * Last token in the output token list. Should be used instead of
     * {@code lastToken}.
     */
    protected PainterAwareToken lastRupsToken = null;

    @Override
    public void addNullToken() {
        final PainterAwareToken token = new PainterAwareToken();
        token.setLanguageIndex(getLanguageIndex());
        addToken(token);
    }

    @Override
    public void addToken(char[] array, int start, int end, int tokenType, int startOffset, boolean hyperlink) {
        final PainterAwareToken token = new PainterAwareToken(
                array, start, end, startOffset, tokenType, getLanguageIndex()
        );
        token.setHyperlink(hyperlink);
        addToken(token);
    }

    @Override
    protected void resetTokenList() {
        firstRupsToken = null;
        lastRupsToken = null;
        super.resetTokenList();
    }

    /**
     * Appends a PdfToken to the output token list.
     *
     * @param token Token to append.
     */
    protected void addToken(PainterAwareToken token) {
        if (firstRupsToken == null) {
            firstRupsToken = token;
        } else {
            lastRupsToken.setNextToken(token);
        }
        lastRupsToken = token;
    }
}
