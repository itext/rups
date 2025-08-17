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

import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.contentstream.ParseTreeNode;
import com.itextpdf.rups.model.contentstream.ParseTreeNodeType;
import com.itextpdf.rups.model.contentstream.PdfContentStreamParser;
import com.itextpdf.rups.model.contentstream.PdfOperators;
import com.itextpdf.rups.view.Language;

import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice.Level;

/**
 * Basic static analyzer for PDF content streams.
 */
public final class PdfParser extends AbstractParser {
    /**
     * MIME type for a PDF content stream.
     */
    private static final String SYNTAX_STYLE_PDF = "application/pdf";

    /**
     * Document, that is being currently processed.
     */
    private RSyntaxDocument currentDoc = null;
    /**
     * Current parse result.
     */
    private final DefaultParseResult currentResult = new DefaultParseResult(this);
    /**
     * Pre-allocated content stream parser.
     */
    private final PdfContentStreamParser parser = new PdfContentStreamParser();

    @Override
    public ParseResult parse(RSyntaxDocument doc, String style) {
        clearResult(doc);
        // We only handle PDF
        if (!SYNTAX_STYLE_PDF.equals(style)) {
            return currentResult;
        }

        currentDoc = doc;
        try {
            handleFullPdf();
        } catch (BadLocationException e) {
            currentResult.setError(e);
            LoggerHelper.error(Language.ERROR_UNEXPECTED_EXCEPTION.getString(), e, getClass());
        }
        currentDoc = null;
        return currentResult;
    }

    private void handleFullPdf() throws BadLocationException {
        /*
         * TODO: We shouldn't constantly re-parse this...
         *
         * We should have the parse tree stored next to the text area and only
         * update it, when text area is changed.
         */
        final ParseTreeNode root = parseText();
        handleNode(root);
    }

    private void handleNode(ParseTreeNode node) {
        processNotClosed(node);
        processOperandTypes(node);
        processWastefulWhitespace(node);
        if (node.getType() == ParseTreeNodeType.UNKNOWN) {
            addErrorNotice(node, Language.PARSER_UNEXPECTED_TOKEN);
        }

        // Now handle children
        ParseTreeNode child = node.getFirstChild();
        while (child != null) {
            handleNode(child);
            child = child.getNext();
        }
    }

    /**
     * Handle PARSER_NOT_CLOSED_* errors.
     *
     * @param node Current node.
     */
    private void processNotClosed(ParseTreeNode node) {
        if (processNotClosedArray(node)) {
            return;
        }
        if (processNotClosedDictionary(node)) {
            return;
        }
        if (processNotClosedStringHex(node)) {
            return;
        }
        processNotClosedStringLiteral(node);
    }

    /**
     * Handle PARSER_NOT_CLOSED_ARRAY error.
     *
     * @param node Current node.
     *
     * @return True, if node is an array.
     */
    private boolean processNotClosedArray(ParseTreeNode node) {
        if (node.getType() != ParseTreeNodeType.ARRAY) {
            return false;
        }
        if (node.getLastChild().getType() != ParseTreeNodeType.ARRAY_CLOSE) {
            addErrorNotice(node, Language.PARSER_NOT_CLOSED_ARRAY);
        }
        return true;
    }

    /**
     * Handle PARSER_NOT_CLOSED_DICTIONARY error.
     *
     * @param node Current node.
     *
     * @return True, if node is a dictionary.
     */
    private boolean processNotClosedDictionary(ParseTreeNode node) {
        if (node.getType() != ParseTreeNodeType.DICTIONARY) {
            return false;
        }
        if (node.getLastChild().getType() != ParseTreeNodeType.DICTIONARY_CLOSE) {
            addErrorNotice(node, Language.PARSER_NOT_CLOSED_DICTIONARY);
        }
        return true;
    }

    /**
     * Handle PARSER_NOT_CLOSED_STRING_HEX error.
     *
     * @param node Current node.
     *
     * @return True, if node is a hexadecimal string.
     */
    private boolean processNotClosedStringHex(ParseTreeNode node) {
        if (node.getType() != ParseTreeNodeType.STRING_HEX) {
            return false;
        }
        if (node.getLastChild().getType() != ParseTreeNodeType.STRING_HEX_CLOSE) {
            addErrorNotice(node, Language.PARSER_NOT_CLOSED_STRING_HEX);
        }
        return true;
    }

    /**
     * Handle PARSER_NOT_CLOSED_STRING_LITERAL error.
     *
     * @param node Current node.
     *
     * @return True, if node is a literal string.
     */
    private boolean processNotClosedStringLiteral(ParseTreeNode node) {
        if (node.getType() != ParseTreeNodeType.STRING_LITERAL) {
            return false;
        }
        // If string literal doesn't end with a close, balance is broken already
        if (node.getLastChild().getType() != ParseTreeNodeType.STRING_LITERAL_CLOSE) {
            addErrorNotice(node, Language.PARSER_NOT_CLOSED_STRING_LITERAL);
            return true;
        }
        // Calculating parentheses balance
        ParseTreeNode walker = node.getFirstChild();
        int balance = 0;
        while (walker != null) {
            while (walker != null) {
                if (walker.getType() == ParseTreeNodeType.STRING_LITERAL_OPEN) {
                    ++balance;
                } else if (walker.getType() == ParseTreeNodeType.STRING_LITERAL_CLOSE) {
                    --balance;
                }
                walker = walker.getNext();
            }
        }
        if (balance != 0) {
            addErrorNotice(node, Language.PARSER_NOT_CLOSED_STRING_LITERAL);
        }
        return true;
    }

    /**
     * Handle PARSER_OPERAND_TYPES_* errors.
     *
     * @param node Current node.
     */
    private void processOperandTypes(ParseTreeNode node) {
        // Quick exit, if not an operand
        if (node.getType() != ParseTreeNodeType.OPERATOR) {
            return;
        }
        if (node.is(PdfOperators.c)) {
            processNumericOperands(node, 6, Language.PARSER_OPERAND_TYPES_C);
        } else if (node.is(PdfOperators.h)) {
            processNumericOperands(node, 0, Language.PARSER_OPERAND_TYPES_H);
        } else if (node.is(PdfOperators.l)) {
            processNumericOperands(node, 2, Language.PARSER_OPERAND_TYPES_L);
        } else if (node.is(PdfOperators.m)) {
            processNumericOperands(node, 2, Language.PARSER_OPERAND_TYPES_M);
        } else if (node.is(PdfOperators.re)) {
            processNumericOperands(node, 4, Language.PARSER_OPERAND_TYPES_RE);
        } else if (node.is(PdfOperators.v)) {
            processNumericOperands(node, 4, Language.PARSER_OPERAND_TYPES_V);
        } else if (node.is(PdfOperators.y)) {
            processNumericOperands(node, 4, Language.PARSER_OPERAND_TYPES_Y);
        }
    }

    /**
     * Common processing for "Operator X expects Y numeric operands"
     *
     * @param node          Node of the operator.
     * @param expectedCount Expected numeric operand count.
     * @param errorMessage  Message to add on error.
     */
    private void processNumericOperands(ParseTreeNode node, int expectedCount, Language errorMessage) {
        int operandCount = 0;
        int numericCount = 0;
        ParseTreeNode firstRelevantNode = node;
        ParseTreeNode walker = node;
        while (walker.hasPrev()) {
            walker = walker.getPrev();
            final ParseTreeNodeType type = walker.getType();
            // Skipping these, as they don't matter
            if (type == ParseTreeNodeType.WHITESPACE || type == ParseTreeNodeType.COMMENT) {
                continue;
            }
            // At this point no operands left
            if (type == ParseTreeNodeType.OPERATOR || type == ParseTreeNodeType.UNKNOWN) {
                break;
            }
            ++operandCount;
            firstRelevantNode = walker;
            if (walker.getType() == ParseTreeNodeType.NUMERIC) {
                ++numericCount;
            }
        }
        if (expectedCount != operandCount || expectedCount != numericCount) {
            addErrorNotice(firstRelevantNode, node, errorMessage);
        }
    }

    /**
     * Handle PARSER_WASTEFUL_WHITESPACE error.
     *
     * @param node Current node.
     */
    private void processWastefulWhitespace(ParseTreeNode node) {
        if (node.getType() != ParseTreeNodeType.WHITESPACE) {
            return;
        }
        final char[] text = node.getTextArray();
        final int begin = node.getTextOffset();
        final int end = begin + node.getTextCount();
        int index = node.getTextOffset();
        while (index < end && text[index] != '\n') {
            ++index;
        }
        if (index < end && index != begin) {
            addNotice(begin, index, Level.INFO, Language.PARSER_WASTEFUL_WHITESPACE);
        }
    }

    private ParseTreeNode parseText() throws BadLocationException {
        parser.reset();
        parser.append(currentDoc.getText(0, currentDoc.getLength()));
        return parser.result();
    }

    private void clearResult(RSyntaxDocument doc) {
        currentResult.clearNotices();
        currentResult.setParsedLines(0, getLineCount(doc) - 1);
    }

    /**
     * Adds a parser notice.
     *
     * @param startOffset Start index for the notice.
     * @param endOffset   End index for the notice.
     * @param level       Level of the notice.
     * @param message     Localized notice message.
     */
    private void addNotice(int startOffset, int endOffset, ParserNotice.Level level, Language message) {
        final int length = endOffset - startOffset;
        final int lineIndex = getLineIndex(currentDoc, startOffset);
        final DefaultParserNotice notice =
                new DefaultParserNotice(this, message.getString(), lineIndex, startOffset, length);
        notice.setLevel(level);
        currentResult.addNotice(notice);
    }

    /**
     * Adds a parser notice.
     *
     * @param firstNode First node, included in the notice.
     * @param lastNode  Last node, included in the notice.
     * @param level     Level of the notice.
     * @param message   Localized notice message.
     */
    private void addNotice(ParseTreeNode firstNode, ParseTreeNode lastNode,
            ParserNotice.Level level, Language message) {
        addNotice(firstNode.getStartOffset(), lastNode.getEndOffset(), level, message);
    }

    /**
     * Adds a parser error notice.
     *
     * @param firstNode First node, included in the notice.
     * @param lastNode  Last node, included in the notice.
     * @param message   Localized notice message.
     */
    private void addErrorNotice(ParseTreeNode firstNode, ParseTreeNode lastNode, Language message) {
        addNotice(firstNode, lastNode, Level.ERROR, message);
    }

    /**
     * Adds a parser error notice.
     *
     * @param node    Node, included in the notice.
     * @param message Localized notice message.
     */
    private void addErrorNotice(ParseTreeNode node, Language message) {
        addNotice(node, node, Level.ERROR, message);
    }

    /**
     * Adds a parser warning notice.
     *
     * @param firstNode First node, included in the notice.
     * @param lastNode  Last node, included in the notice.
     * @param message   Localized notice message.
     */
    private void addWarningNotice(ParseTreeNode firstNode, ParseTreeNode lastNode, Language message) {
        addNotice(firstNode, lastNode, Level.WARNING, message);
    }

    /**
     * Adds a parser warning notice.
     *
     * @param node    Node, included in the notice.
     * @param message Localized notice message.
     */
    private void addWarningNotice(ParseTreeNode node, Language message) {
        addNotice(node, node, Level.WARNING, message);
    }
    /**
     * Adds a parser info notice.
     *
     * @param firstNode First node, included in the notice.
     * @param lastNode  Last node, included in the notice.
     * @param message   Localized notice message.
     */
    private void addInfoNotice(ParseTreeNode firstNode, ParseTreeNode lastNode, Language message) {
        addNotice(firstNode, lastNode, Level.INFO, message);
    }

    /**
     * Adds a parser info notice.
     *
     * @param node    Node, included in the notice.
     * @param message Localized notice message.
     */
    private void addInfoNotice(ParseTreeNode node, Language message) {
        addNotice(node, node, Level.INFO, message);
    }

    /**
     * Returns the line count in the document.
     *
     * @param doc Document to get line count for.
     *
     * @return The line count.
     */
    private static int getLineCount(RSyntaxDocument doc) {
        return doc.getDefaultRootElement().getElementCount();
    }

    /**
     * Returns the line index for a specific offset.
     *
     * @param doc    Document to search for the offset in.
     * @param offset Offset to find the line for.
     *
     * @return The line index.
     */
    private static int getLineIndex(RSyntaxDocument doc, int offset) {
        return doc.getDefaultRootElement().getElementIndex(offset);
    }
}
