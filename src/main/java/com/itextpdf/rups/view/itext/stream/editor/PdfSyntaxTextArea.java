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

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.DefaultTokenPainterFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

/**
 * Our custom RSyntaxTextArea.
 */
public final class PdfSyntaxTextArea extends RSyntaxTextArea {
    /**
     * MIME type for generic binary data.
     */
    public static final String SYNTAX_STYLE_BINARY = "application/octet-stream";
    /**
     * MIME type for a PDF content stream.
     */
    public static final String SYNTAX_STYLE_PDF = "application/pdf";

    private static final int DEFAULT_MARK_OCCURRENCES_DELAY = 500;

    static {
        /*
         * Registering PDF content type, so that we could use PDF syntax
         * highlighting in RSyntaxTextArea.
         */
        final AbstractTokenMakerFactory tokenMakerFactory =
                (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        tokenMakerFactory.putMapping(SYNTAX_STYLE_PDF, PdfTokenMaker.class.getName());
        tokenMakerFactory.putMapping(SYNTAX_STYLE_BINARY, BinaryTokenMaker.class.getName());
        FoldParserManager.get().addFoldParserMapping(SYNTAX_STYLE_PDF, new PdfFoldParser());
    }

    public PdfSyntaxTextArea() {
        setCustomDefaults();
    }

    @Override
    public RSyntaxDocument getDocument() {
        return (RSyntaxDocument) super.getDocument();
    }

    @Override
    public void setSyntaxEditingStyle(String styleKey) {
        /*
         * For PDF streams we will set up our custom painter with our Latin-1
         * filter "hack". The way it works is that with the filter applied,
         * any character greater than U+00FF will be replaced with a UTF-8
         * byte representation. As in the internal char array can actually be
         * interpreted as a byte array, which wastes twice as much space...
         *
         * To make it easier to work with possible binary content of a PDF
         * stream we will use a custom token painter. It will paint non-ASCII
         * character as their hex-codes instead of their Latin-1 mapped
         * glyphs.
         *
         * We will use the same scheme for generic binary data, so that you
         * could set/get it into the text area without it getting broken
         * during Unicode conversions.
         *
         * Both the filter and painter should be replaced with default, when
         * we display a non-binary stream. For example, for XML-based metadata
         * we should just use the regular XML editor available.
         */
        if (SYNTAX_STYLE_PDF.equals(styleKey) || SYNTAX_STYLE_BINARY.equals(styleKey)) {
            getDocument().setDocumentFilter(new Latin1Filter());
            setTokenPainterFactory(new PdfTokenPainterFactory());
        } else {
            getDocument().setDocumentFilter(null);
            setTokenPainterFactory(new DefaultTokenPainterFactory());
        }
        super.setSyntaxEditingStyle(styleKey);
    }

    private void setCustomDefaults() {
        /*
         * We will change some default, while we are here anyway.
         *
         * By default, we will just assume generic binary data.
         */
        setSyntaxEditingStyle(PdfSyntaxTextArea.SYNTAX_STYLE_BINARY);
        /*
         * Pretty important to install our custom caret. The default one is
         * invisible, when the text area is not visible, which is very odd and
         * inconvenient. The custom one fixes that.
         */
        setCaret(new CustomConfigurableCaret());
        // This parser will only work, when PDF style is enabled
        addParser(new PdfParser());
        // This will allow to fold code blocks (like BT/ET blocks)
        setCodeFoldingEnabled(true);
        // This will automatically add tabulations, when you enter a new line
        // after a "q" operator, for example
        setAutoIndentEnabled(true);
        // This will mark identical names and operators, when cursor is on
        // them after a short delay
        setMarkOccurrences(true);
        setMarkOccurrencesDelay(DEFAULT_MARK_OCCURRENCES_DELAY);
    }
}
