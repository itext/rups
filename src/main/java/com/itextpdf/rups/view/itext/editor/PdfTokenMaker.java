/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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

import com.itextpdf.rups.model.contentstream.ParseTreeNode;
import com.itextpdf.rups.model.contentstream.ParseTreeNodeType;
import com.itextpdf.rups.model.contentstream.PdfContentStreamParser;
import com.itextpdf.rups.model.contentstream.PdfOperators;

import java.util.Iterator;
import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerBase;

/**
 * RSyntaxTextArea token maker, which handles PDF content streams.
 *
 * <p>
 * This class really wants to just implement TokenMaker, as {@code firstToken},
 * {@code currentToken}, {@code previousToken} and {@code tokenFactory} from
 * {@link org.fife.ui.rsyntaxtextarea.TokenMakerBase} are of no use here. But
 * just implementing the interface would force us to copy a lot of code from
 * the library, and, for some reason {@code DefaultOccurrenceMarker} is marked
 * as package-private, so we would need to reimplement that as well.
 * </p>
 *
 * <p>
 * So, at the moment, these fields from TokenMakerBase should be ignored. For
 * token manipulation, {@code firstPdfToken} and {@code lastPdfToken} should
 * be used instead.
 * </p>
 *
 * <p>
 * This class is expected to be used with a text area, which has a
 * {@link Latin1Filter} on the underlying document. This is used as a way to
 * represent a byte stream as a string.
 * </p>
 */
public final class PdfTokenMaker extends TokenMakerBase {
    /**
     * Special internal token type marker to signify, that previous line ended
     * within a hexadecimal string, which was yet to be closed.
     */
    private static final int MULTI_LINE_STRING_HEX = Integer.MIN_VALUE;

    /**
     * Content stream parser used for token parsing.
     */
    private final PdfContentStreamParser pdfContentStreamParser = new PdfContentStreamParser();

    /**
     * First token in the output token list. Should be used instead of
     * {@code firstToken}.
     */
    private PdfToken firstPdfToken = null;
    /**
     * Last token in the output token list. Should be used instead of
     * {@code lastToken}.
     */
    private PdfToken lastPdfToken = null;

    @Override
    public void addNullToken() {
        final PdfToken token = new PdfToken();
        token.setLanguageIndex(getLanguageIndex());
        addToken(token);
    }

    @Override
    public void addToken(char[] array, int start, int end, int tokenType, int startOffset, boolean hyperlink) {
        final PdfToken token = new PdfToken(array, start, end, startOffset, tokenType, getLanguageIndex());
        token.setHyperlink(hyperlink);
        addToken(token);
    }

    @Override
    public boolean getMarkOccurrencesOfTokenType(int type) {
        switch (type) {
            case PdfTokenTypes.NAME:
            case PdfTokenTypes.OPERATOR:
                return true;
            default:
                return false;
        }
    }

    @Override
    public String[] getLineCommentStartAndEnd(int languageIndex) {
        return new String[] { "%", null };
    }

    @Override
    public boolean getShouldIndentNextLineAfter(Token token) {
        if (token == null) {
            return false;
        }
        // TODO: Re-implement automatic indentation
        return false;
    }

    @Override
    protected void resetTokenList() {
        firstPdfToken = null;
        lastPdfToken = null;
        super.resetTokenList();
    }

    @Override
    public Token getTokenList(Segment text, int startTokenType, int startOffset) {
        resetTokenList();
        pdfContentStreamParser.reset();

        /*
         * This is some special handling for multi-line strings.
         *
         * For cases, when previous line contained a part of a hex string, but
         * it wasn't close, we will get a specific negative value to detect
         * that. This means, that current line will continue the hex string
         * body.
         *
         * For cases of literal string we also need to know parentheses
         * balance. So any other negative value will indicate that. And the
         * negation of that number will give the current parentheses balance.
         * Current string will continue inside the composite literal type.
         */
        int leafsToSkip = 0;
        if (startTokenType == MULTI_LINE_STRING_HEX) {
            leafsToSkip = 1;
            pdfContentStreamParser.append('<', leafsToSkip);
        } else if (startTokenType < 0) {
            leafsToSkip = -startTokenType;
            pdfContentStreamParser.append('(', leafsToSkip);
        }

        pdfContentStreamParser.append(text);

        final Iterator<ParseTreeNode> it = pdfContentStreamParser.result().primitiveNodeIterator();
        ParseTreeNode node = null;
        // Skipping artificially added tokens
        for (int i = 0; i < leafsToSkip; ++i) {
            node = it.next();
        }
        while (it.hasNext()) {
            node = it.next();
            addToken(text, node, startOffset);
        }

        handleMultiline(text, node, startOffset);
        return firstPdfToken;
    }

    /**
     * Appends a PdfToken to the output token list.
     *
     * @param token Token to append.
     */
    private void addToken(PdfToken token) {
        if (firstPdfToken == null) {
            firstPdfToken = token;
        } else {
            lastPdfToken.setNextToken(token);
        }
        lastPdfToken = token;
    }

    /**
     * Handles adding internal tokens to preserve information on non-closed
     * strings.
     *
     * @param text        The text from which to get tokens.
     * @param lastLeaf    Last primitive node, which was parsed from
     *                    {@code text}.
     * @param startOffset The offset into the document at which {@code text}
     *                    starts.
     */
    private void handleMultiline(Segment text, ParseTreeNode lastLeaf, int startOffset) {
        if (lastLeaf == null || lastLeaf.isRoot()) {
            addNullToken();
            return;
        }

        // Hex strings cannot be nested, so we only need to check, that last
        // non-leaf element was a non-closed hex string
        if (lastLeaf.getType() != ParseTreeNodeType.STRING_HEX_CLOSE
                && lastLeaf.getParent().getType() == ParseTreeNodeType.STRING_HEX) {
            addInternalToken(text, MULTI_LINE_STRING_HEX, startOffset);
            return;
        }

        // Literal strings preserve parentheses balance, so we need to keep that
        if (lastLeaf.getParent().getType() == ParseTreeNodeType.STRING_LITERAL) {
            ParseTreeNode walker = lastLeaf;
            int balance = 0;
            while (walker != null) {
                if (walker.getType() == ParseTreeNodeType.STRING_LITERAL_OPEN) {
                    ++balance;
                } else if (walker.getType() == ParseTreeNodeType.STRING_LITERAL_CLOSE) {
                    --balance;
                }
                walker = walker.getPrev();
            }
            if (balance > 0) {
                addInternalToken(text, -balance, startOffset);
                return;
            }
        }

        // Otherwise everything is fine, just add a null token
        addNullToken();
    }

    /**
     * Appends an internal token to the output token list.
     *
     * <p>
     * These are token, which are used to signify non-closed strings.
     * </p>
     *
     * @param text        {@code Segment} to get text from.
     * @param type        The token's type.
     * @param startOffset The offset in the document at which this token occurs.
     */
    private void addInternalToken(Segment text, int type, int startOffset) {
        final int index = text.offset + text.count - 1;
        addToken(text.array, index, index, type, startOffset);
    }

    /**
     * Appends a token to the output token list, based on the PDF content
     * stream parse node.
     *
     * @param text          {@code Segment} to get text from.
     * @param primitiveNode PDF content stream parse node to create token from.
     * @param startOffset   The offset in the document at which this token occurs.
     */
    private void addToken(Segment text, ParseTreeNode primitiveNode, int startOffset) {
        final char[] array = primitiveNode.getTextArray();
        final int start = primitiveNode.getTextOffset();
        final int end = start + primitiveNode.getTextCount() - 1;
        addToken(array, start, end, getTokenType(primitiveNode), startOffset + (start - text.offset));
    }

    /**
     * Maps PDF content stream parse node type to a TokenType value.
     *
     * @param leafNode Node to map the type of.
     *
     * @return TokenType value for the node.
     */
    private static int getTokenType(ParseTreeNode leafNode) {
        switch (leafNode.getType()) {
            case WHITESPACE:
                return PdfTokenTypes.WHITESPACE;
            case COMMENT:
                return PdfTokenTypes.COMMENT;
            case BOOLEAN:
                return PdfTokenTypes.BOOLEAN;
            case NUMERIC:
                return PdfTokenTypes.NUMERIC;
            case STRING_LITERAL_DATA:
            case STRING_HEX_DATA:
                return PdfTokenTypes.STRING_DATA;
            case NAME:
                return PdfTokenTypes.NAME;
            case NULL:
                return PdfTokenTypes.NULL;
            case OPERATOR:
                if (leafNode.isOperator(PdfOperators.Do)) {
                    return PdfTokenTypes.FUNCTION;
                } else {
                    return PdfTokenTypes.OPERATOR;
                }
            case BINARY_DATA:
                return PdfTokenTypes.BINARY_DATA;
            case STRING_LITERAL_OPEN:
            case STRING_LITERAL_CLOSE:
            case STRING_HEX_OPEN:
            case STRING_HEX_CLOSE:
            case ARRAY_OPEN:
            case ARRAY_CLOSE:
            case DICTIONARY_OPEN:
            case DICTIONARY_CLOSE:
                return PdfTokenTypes.SEPARATOR;
            default:
                // Other types are not primitive... Should not get them here
                return PdfTokenTypes.ERROR;
        }
    }
}
