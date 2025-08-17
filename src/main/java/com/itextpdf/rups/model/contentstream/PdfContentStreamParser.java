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
package com.itextpdf.rups.model.contentstream;

import java.util.Arrays;
import javax.swing.text.Segment;

/**
 * A parser, which parses a PDF content stream string into a parse tree.
 *
 * <p>
 * This code is based on the {@link com.itextpdf.io.source.PdfTokenizer}.
 * Ideally we would just use that, but it has some limitations, which make it
 * unusable for our tasks.
 * </p>
 *
 * <ol>
 *     <li>
 *         PdfTokenizer works on byte arrays, while we will be getting strings
 *         instead. Since this would be called often, these conversions and
 *         allocation will add up.
 *     </li>
 *     <li>
 *         Since we are tokenizing for a text editor, we need all the data
 *         from the text string to be present in the resulting tokens.
 *         Unfortunately, PdfTokenizer skips whitespace and it is not present
 *         in the output.
 *     </li>
 *     <li>
 *         We need to know, where the tokens are in the original string, but
 *         PdfTokenizer does not store that information in the result. You can
 *         kind of get that information via the cursor position methods, but
 *         because of the two issues above, it might not be as easy.
 *     </li>
 *     <li>
 *         On invalid input PdfTokenizer throws an exception and you cannot
 *         parse the text further. But in our case we will have intermediate
 *         invalid text, so we should not throw in such cases, but have error
 *         type tokens instead. In such cases this parse will just create
 *         {@link ParseTreeNodeType#UNKNOWN} tokens.
 *     </li>
 * </ol>
 *
 * <p>
 * Currently there are not a lot of composite types in the parse tree, so the
 * resulting representation is pretty low-level. This might get improved in
 * the future to simplify static analysis.
 * </p>
 *
 * <p>
 * It is somewhat assumed, that input text is in a Latin-1 encoding (as in no
 * char exceeds U+00FF), so it might produce ambiguous results for non-Latin-1
 * characters.
 * </p>
 */
public final class PdfContentStreamParser {
    /**
     * "false" string as a char array.
     */
    private static final char[] FALSE = {'f', 'a', 'l', 's', 'e'};
    /**
     * "true" string as a char array.
     */
    private static final char[] TRUE = {'t', 'r', 'u', 'e'};
    /**
     * "null" string as a char array.
     */
    private static final char[] NULL = {'n', 'u', 'l', 'l'};
    /**
     * A length based mapping of PDF content stream operators.
     *
     * <p>
     * If {@code L} is the expected length of the operator string, then at
     * index {@code L - 1} you will get an array of all the possible
     * operators, which has the length of {@code L}.
     * </p>
     *
     * <p>
     * This is done to make the linear search a bit faster. While this can be
     * improved, operator matching doesn't seem to be a bottleneck, so this
     * will suffice for now.
     * </p>
     */
    private static final char[][][] LENGTH_OPERATOR_MAP = {
            {
                    PdfOperators.w,
                    PdfOperators.J,
                    PdfOperators.j,
                    PdfOperators.M,
                    PdfOperators.d,
                    PdfOperators.i,
                    PdfOperators.q,
                    PdfOperators.Q,
                    PdfOperators.m,
                    PdfOperators.l,
                    PdfOperators.c,
                    PdfOperators.v,
                    PdfOperators.y,
                    PdfOperators.h,
                    PdfOperators.S,
                    PdfOperators.s,
                    PdfOperators.f,
                    PdfOperators.F,
                    PdfOperators.B,
                    PdfOperators.b,
                    PdfOperators.n,
                    PdfOperators.W,
                    PdfOperators.SINGLE_QUOTE,
                    PdfOperators.DOUBLE_QUOTE,
                    PdfOperators.G,
                    PdfOperators.g,
                    PdfOperators.K,
                    PdfOperators.k,
            },
            {
                    PdfOperators.ri,
                    PdfOperators.gs,
                    PdfOperators.cm,
                    PdfOperators.re,
                    PdfOperators.f_STAR,
                    PdfOperators.B_STAR,
                    PdfOperators.b_STAR,
                    PdfOperators.W_STAR,
                    PdfOperators.BT,
                    PdfOperators.ET,
                    PdfOperators.Tc,
                    PdfOperators.Tw,
                    PdfOperators.Tz,
                    PdfOperators.TL,
                    PdfOperators.Tf,
                    PdfOperators.Tr,
                    PdfOperators.Ts,
                    PdfOperators.Td,
                    PdfOperators.TD,
                    PdfOperators.Tm,
                    PdfOperators.T_STAR,
                    PdfOperators.Tj,
                    PdfOperators.TJ,
                    PdfOperators.d0,
                    PdfOperators.d1,
                    PdfOperators.CS,
                    PdfOperators.cs,
                    PdfOperators.SC,
                    PdfOperators.sc,
                    PdfOperators.RG,
                    PdfOperators.rg,
                    PdfOperators.Sh,
                    PdfOperators.BI,
                    PdfOperators.ID,
                    PdfOperators.EI,
                    PdfOperators.Do,
                    PdfOperators.MP,
                    PdfOperators.DP,
                    PdfOperators.BX,
                    PdfOperators.EX,
            },
            {
                    PdfOperators.SCN,
                    PdfOperators.scn,
                    PdfOperators.BMC,
                    PdfOperators.BDC,
                    PdfOperators.EMC,
            },
    };

    /**
     * In progress parsing result in a parse tree form.
     */
    private ParseTreeNode result;
    /**
     * Current composite/marker node, that is being appended to.
     */
    private ParseTreeNode currentNode;
    /**
     * Current parentheses balance inside a string literal. This is valid only
     * when current node type is {@link ParseTreeNodeType#STRING_LITERAL}.
     */
    private int stringLiteralParenthesesBalance;

    /**
     * Creates a new PDF content stream parser.
     */
    public PdfContentStreamParser() {
        reset();
    }

    /**
     * Parses the provided PDF content stream string into a parse tree.
     *
     * @param text PDF content stream string to parse.
     *
     * @return Resulting parse tree.
     */
    public static ParseTreeNode parse(String text) {
        final PdfContentStreamParser parser = new PdfContentStreamParser();
        parser.append(text);
        return parser.result();
    }

    /**
     * Resets the parser into its initial state.
     */
    public void reset() {
        result = new ParseTreeNode();
        currentNode = result;
        stringLiteralParenthesesBalance = 0;
    }

    /**
     * Appends the string to be processed by the parser. The string is parsed
     * immediately during this call.
     *
     * @param text String to parse.
     */
    public void append(String text) {
        final char[] textArray = text.toCharArray();
        append(textArray, 0, textArray.length);
    }

    /**
     * Appends a sequence, which repeats a single character, to be processed
     * by the parser. The sequence is parsed immediately during this call.
     *
     * <p>
     * This could be useful, if you want to parse only a part of the stream,
     * but you know, that it starts in the middle of a string literal with a
     * known parentheses balance. In such case you can start parsing with a
     * {@code parser.append('(', balance)} call and append the stream part
     * after. After that you would just skip the added tokens in the result.
     * </p>
     *
     * @param ch    Character to repeat in the sequence.
     * @param count Amount of times to repeat the character. Should not be
     *              negative.
     */
    public void append(char ch, int count) {
        final char[] text = new char[count];
        Arrays.fill(text, ch);
        append(text, 0, text.length);
    }

    /**
     * Appends the character array to be processed by the parser. The
     * characters are parsed immediately during this call.
     *
     * @param text Characters to parse.
     */
    public void append(char[] text) {
        append(text, 0, text.length);
    }

    /**
     * Appends the character array slice to be processed by the parser. The
     * characters are parsed immediately during this call.
     *
     * @param text  Text slice backing array.
     * @param begin Text slice begin index, inclusive.
     * @param end   Text slice end index, exclusive.
     */
    public void append(char[] text, int begin, int end) {
        int index = begin;
        while (index < end) {
            index = appendToken(text, index, end);
        }
    }

    /**
     * Appends the text segment to be processed by the parser. The text is
     * parsed immediately during this call.
     *
     * @param segment Text segment to parse.
     */
    public void append(Segment segment) {
        append(segment.array, segment.offset, segment.offset + segment.count);
    }

    /**
     * Returns the parsing result.
     *
     * @return The parsing result.
     */
    public ParseTreeNode result() {
        return result;
    }

    /*
     * append* methods below are all made the same way. The take an input
     * slice as an input and if a token is parsed, returned index will be
     * incremented forwards. And they are designed in such a way, that they
     * shouldn't parse things there are not supposed to.
     *
     * So to process a slice you would just go through the token types and try
     * appending them. If index wasn't moved, then just try a different type.
     */

    /**
     * Process a single token from the input text and returns the index, where
     * the next token will start.
     *
     * <p>
     * With how the method is designed, it will add one primitive token at
     * most, so to process the whole string, you need to call this in a loop
     * till the return index is outside the string.
     * </p>
     *
     * @param text  Text slice backing array.
     * @param begin Text slice begin index, inclusive.
     * @param end   Text slice end index, exclusive.
     *
     * @return Starting index for the next token.
     */
    private int appendToken(char[] text, int begin, int end) {
        assert begin < end;

        // Special case: we are currently inside a string literal
        if (currentNode.getType() == ParseTreeNodeType.STRING_LITERAL) {
            return appendStringLiteralContinuation(text, begin, end);
        }

        // Special case: we are currently inside a hex string
        if (currentNode.getType() == ParseTreeNodeType.STRING_HEX) {
            return appendStringHexContinuation(text, begin, end);
        }

        /*
         * Everything below is the normal parsing case. Append token calls
         * should be ordered based on how often you would encounter them in a
         * PDF content stream for performance reasons.
         */

        int index = appendWhitespace(text, begin, end);
        if (index > begin) {
            return index;
        }

        index = appendNumeric(text, begin, end);
        if (index > begin) {
            return index;
        }

        index = appendName(text, begin, end);
        if (index > begin) {
            return index;
        }

        index = appendStringLiteralOpen(text, begin);
        if (index > begin) {
            return index;
        }

        // If a hex string or a dictionary is being open
        if (text[begin] == '<') {
            // Opening a dictionary
            if (begin + 1 < end && text[begin + 1] == '<') {
                currentNode = currentNode.addChild(ParseTreeNodeType.DICTIONARY);
                currentNode.addChild(ParseTreeNodeType.DICTIONARY_OPEN, text, begin, 2);
                return begin + 2;
            }
            // Otherwise opening a hex string
            currentNode = currentNode.addChild(ParseTreeNodeType.STRING_HEX);
            currentNode.addChild(ParseTreeNodeType.STRING_HEX_OPEN, text, begin, 1);
            return begin + 1;
        }

        /*
         * Hex string terminator is handled within appendStringHexContinuation.
         * Here we just handle dictionary terminators and rogues tokens.
         */
        if (text[begin] == '>') {
            // Closing a dictionary
            if (begin + 1 < end && text[begin + 1] == '>') {
                currentNode.addChild(ParseTreeNodeType.DICTIONARY_CLOSE, text, begin, 2);
                // If this is actually a dictionary terminator, then finishing the dictionary node
                if (currentNode.getType() == ParseTreeNodeType.DICTIONARY) {
                    currentNode = currentNode.getParent();
                }
                return begin + 2;
            }
            // Otherwise a rogue hex string termination token
            currentNode.addChild(ParseTreeNodeType.STRING_HEX_CLOSE, text, begin, 1);
            return begin + 1;
        }

        // If an array is being open
        if (text[begin] == '[') {
            currentNode = currentNode.addChild(ParseTreeNodeType.ARRAY);
            currentNode.addChild(ParseTreeNodeType.ARRAY_OPEN, text, begin, 1);
            return begin + 1;
        }

        // If an array is being closed
        if (text[begin] == ']') {
            currentNode.addChild(ParseTreeNodeType.ARRAY_CLOSE, text, begin, 1);
            // If this is actually an array terminator, then finishing the array node
            if (currentNode.getType() == ParseTreeNodeType.ARRAY) {
                currentNode = currentNode.getParent();
            }
            return begin + 1;
        }

        index = appendBoolean(text, begin, end);
        if (index > begin) {
            return index;
        }

        index = appendNull(text, begin, end);
        if (index > begin) {
            return index;
        }

        index = appendComment(text, begin, end);
        if (index > begin) {
            return index;
        }

        // This will add something, either an operator or an UNKNOWN token
        return appendPotentialOperator(text, begin, end);
    }

    private int appendStringLiteralContinuation(char[] text, int begin, int end) {
        assert begin < end;
        assert currentNode.getType() == ParseTreeNodeType.STRING_LITERAL;

        int index = appendStringLiteralData(text, begin, end);
        if (index > begin) {
            return index;
        }

        index = appendStringLiteralClose(text, begin);
        if (index > begin) {
            return index;
        }

        return appendStringLiteralOpen(text, begin);
    }

    private int appendStringHexContinuation(char[] text, int begin, int end) {
        assert begin < end;
        assert currentNode.getType() == ParseTreeNodeType.STRING_HEX;

        int index = appendStringHexData(text, begin, end);
        if (index > begin) {
            return index;
        }

        index = appendStringHexClose(text, begin);
        if (index > begin) {
            return index;
        }

        return appendWhitespace(text, begin, end);
    }

    private int appendWhitespace(char[] text, int begin, int end) {
        int index = begin;
        while (index < end && isWhitespace(text[index])) {
            ++index;
        }
        if (index > begin) {
            currentNode.addChild(ParseTreeNodeType.WHITESPACE, text, begin, index - begin);
        }
        return index;
    }

    private int appendComment(char[] text, int begin, int end) {
        assert begin < end;

        int index = begin;
        if (text[index] != '%') {
            return index;
        }

        do {
            ++index;
        } while (index < end && text[index] != '\r' && text[index] != '\n');
        currentNode.addChild(ParseTreeNodeType.COMMENT, text, begin, index - begin);
        return index;
    }

    private int appendBoolean(char[] text, int begin, int end) {
        if (containsAt(FALSE, text, begin, end)) {
            currentNode.addChild(ParseTreeNodeType.BOOLEAN, text, begin, FALSE.length);
            return begin + FALSE.length;
        }
        if (containsAt(TRUE, text, begin, end)) {
            currentNode.addChild(ParseTreeNodeType.BOOLEAN, text, begin, TRUE.length);
            return begin + TRUE.length;
        }
        return begin;
    }

    private int appendNumeric(char[] text, int begin, int end) {
        assert begin < end;

        int index = begin;
        while (index < end && text[index] == '-') {
            ++index;
        }
        while (index < end && ('0' <= text[index] && text[index] <= '9')) {
            ++index;
        }
        if (index < end && text[index] == '.') {
            do {
                ++index;
            } while (index < end && ('0' <= text[index] && text[index] <= '9'));
        }
        if (index > begin) {
            currentNode.addChild(ParseTreeNodeType.NUMERIC, text, begin, index - begin);
        }
        return index;
    }

    private int appendStringLiteralData(char[] text, int begin, int end) {
        int index = begin;
        while (index < end && text[index] != '(' && text[index] != ')') {
            if (text[index] == '\\') {
                index = Math.min(index + 2, end);
            } else {
                ++index;
            }
        }
        if (index > begin) {
            currentNode.addChild(ParseTreeNodeType.STRING_LITERAL_DATA, text, begin, index - begin);
        }
        return index;
    }

    private int appendStringLiteralOpen(char[] text, int index) {
        if (text[index] != '(') {
            return index;
        }
        if (stringLiteralParenthesesBalance == 0) {
            currentNode = currentNode.addChild(ParseTreeNodeType.STRING_LITERAL);
        }
        currentNode.addChild(ParseTreeNodeType.STRING_LITERAL_OPEN, text, index, 1);
        ++stringLiteralParenthesesBalance;
        return index + 1;
    }

    private int appendStringLiteralClose(char[] text, int index) {
        if (text[index] != ')') {
            return index;
        }
        currentNode.addChild(ParseTreeNodeType.STRING_LITERAL_CLOSE, text, index, 1);
        if (stringLiteralParenthesesBalance == 1) {
            currentNode = currentNode.getParent();
        }
        if (stringLiteralParenthesesBalance > 0) {
            --stringLiteralParenthesesBalance;
        }
        return index + 1;
    }

    private int appendStringHexData(char[] text, int begin, int end) {
        int index = begin;
        while (index < end && text[index] != '>' && !isWhitespace(text[index])) {
            ++index;
        }
        if (index > begin) {
            currentNode.addChild(ParseTreeNodeType.STRING_HEX_DATA, text, begin, index - begin);
        }
        return index;
    }

    private int appendStringHexClose(char[] text, int index) {
        if (text[index] == '>') {
            currentNode.addChild(ParseTreeNodeType.STRING_HEX_CLOSE, text, index, 1);
            currentNode = currentNode.getParent();
            return index + 1;
        }
        return index;
    }

    private int appendName(char[] text, int begin, int end) {
        assert begin < end;

        int index = begin;
        if (text[index] != '/') {
            return index;
        }

        do {
            ++index;
        } while (index < end && !isDelimiterWhitespace(text[index]));
        currentNode.addChild(ParseTreeNodeType.NAME, text, begin, index - begin);
        return index;
    }

    private int appendNull(char[] text, int begin, int end) {
        if (containsAt(NULL, text, begin, end)) {
            currentNode.addChild(ParseTreeNodeType.NULL, text, begin, NULL.length);
            return begin + NULL.length;
        }
        return begin;
    }

    private int appendPotentialOperator(char[] text, int begin, int end) {
        assert begin < end;

        /*
         * At this point it might only be an operator or garbage... Since we
         * need to match the biggest operator, we need to find the end of the
         * token before matching.
         */

        int index = begin + 1;
        while (index < end && !isDelimiterWhitespace(text[index])) {
            ++index;
        }

        final int length = index - begin;
        if (length <= LENGTH_OPERATOR_MAP.length) {
            final char[][] operatorMap = LENGTH_OPERATOR_MAP[length - 1];
            for (final char[] operator : operatorMap) {
                if (equals(operator, text, begin, index)) {
                    currentNode.addChild(ParseTreeNodeType.OPERATOR, text, begin, operator.length);
                    return index;
                }
            }
        }

        currentNode.addChild(ParseTreeNodeType.UNKNOWN, text, begin, length);
        return index;
    }

    private static boolean isWhitespace(char ch) {
        switch (ch) {
            case '\0':
            case '\t':
            case '\n':
            case '\f':
            case '\r':
            case ' ':
                return true;
            default:
                return false;
        }
    }

    private static boolean isDelimiterWhitespace(char ch) {
        switch (ch) {
            case '\0':
            case '\t':
            case '\n':
            case '\f':
            case '\r':
            case ' ':
            case '(':
            case ')':
            case '<':
            case '>':
            case '[':
            case ']':
            case '/':
            case '%':
                return true;
            default:
                return false;
        }
    }

    private static boolean containsAt(char[] expected, char[] text, int begin, int end) {
        final int toIndex = begin + expected.length;
        if (toIndex > end) {
            return false;
        }
        return Arrays.equals(expected, 0, expected.length, text, begin, toIndex);
    }

    private static boolean equals(char[] expected, char[] text, int begin, int end) {
        return Arrays.equals(expected, 0, expected.length, text, begin, end);
    }
}
