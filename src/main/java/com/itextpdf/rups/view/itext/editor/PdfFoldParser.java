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

import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.contentstream.ParseTreeNode;
import com.itextpdf.rups.model.contentstream.ParseTreeNodeType;
import com.itextpdf.rups.model.contentstream.PdfContentStreamParser;
import com.itextpdf.rups.model.contentstream.PdfOperators;
import com.itextpdf.rups.view.Language;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldType;

/**
 * Fold parser for handling PDF content streams.
 */
public final class PdfFoldParser implements FoldParser {
    /**
     * Default size to use for the marker stack.
     */
    private static final int DEFAULT_MARKER_STACK_SIZE = 8;
    /**
     * Marker for a marked content sequence fold.
     */
    private static final Object MARKED_CONTENT = new Object();
    /**
     * Marked for a text object block fold.
     */
    private static final Object TEXT_OBJECT = new Object();

    /**
     * Pre-allocated content stream parser.
     */
    private final PdfContentStreamParser parser = new PdfContentStreamParser();

    @Override
    public List<Fold> getFolds(RSyntaxTextArea textArea) {
        try {
            return getFoldsInternal(textArea);
        } catch (BadLocationException e) {
            LoggerHelper.error(Language.ERROR_UNEXPECTED_EXCEPTION.getString(), e, getClass());
            return Collections.emptyList();
        }
    }

    private List<Fold> getFoldsInternal(RSyntaxTextArea textArea)
            throws BadLocationException {
        /*
         * TODO: We shouldn't constantly re-parse this...
         *
         * We should have the parse tree stored next to the text area and only
         * update it, when text area is changed.
         */
        final ParseTreeNode root = parseText(textArea);
        final State state = new State();
        final Iterator<ParseTreeNode> it = root.primitiveNodeIterator();
        while (it.hasNext()) {
            final ParseTreeNode node = it.next();
            if (node.getType() != ParseTreeNodeType.OPERATOR) {
                continue;
            }
            if (node.is(PdfOperators.BT)) {
                // Text object block start
                startNewFold(state, textArea, TEXT_OBJECT, node);
            } else if (inTextObject(state) && node.is(PdfOperators.ET)) {
                // Text object block end
                endCurrentFold(state, node);
            } else if (node.is(PdfOperators.BMC) || node.is(PdfOperators.BDC)) {
                // Marked content sequence start
                startNewFold(state, textArea, MARKED_CONTENT, node);
            } else if (inMarkedContent(state) && node.is(PdfOperators.EMC)) {
                // Marked content sequence end
                endCurrentFold(state, node);
            }
            // TODO: Add more blocks (like less stable q/Q)
        }
        return state.folds;
    }

    private ParseTreeNode parseText(RSyntaxTextArea textArea) {
        parser.reset();
        parser.append(textArea.getText());
        return parser.result();
    }

    /**
     * Starts a new child fold of the specified type.
     *
     * @param state    Current folding algorithm state.
     * @param textArea The text area whose contents should be analyzed.
     * @param marker   Type marker.
     * @param node     Node where fold starts.
     */
    private static void startNewFold(State state, RSyntaxTextArea textArea, Object marker, ParseTreeNode node)
            throws BadLocationException {
        if (state.currentFold != null) {
            state.currentFold = state.currentFold.createChild(FoldType.CODE, node.getTextOffset());
        } else {
            state.currentFold = new Fold(FoldType.CODE, textArea, node.getTextOffset());
            state.folds.add(state.currentFold);
        }
        state.markers.push(marker);
    }

    /**
     * Ends the current fold and sets current fold to parent. If current fold
     * spans only one line, it will be deleted.
     *
     * @param state Current folding algorithm state.
     * @param node  Node where fold ends.
     */
    private static void endCurrentFold(State state, ParseTreeNode node)
            throws BadLocationException {
        if (state.currentFold == null) {
            return;
        }
        state.currentFold.setEndOffset(node.getTextOffset());
        // If it is on a single line, we skip it
        if (state.currentFold.isOnSingleLine()) {
            removeCurrentFold(state);
        } else {
            state.currentFold = state.currentFold.getParent();
            state.markers.pop();
        }
    }

    /**
     * Removes the current fold and set its parent as the new current fold.
     *
     * @param state Current folding algorithm state.
     */
    private static void removeCurrentFold(State state) {
        if (state.currentFold == null) {
            return;
        }
        final Fold parent = state.currentFold.getParent();
        if (!state.currentFold.removeFromParent()) {
            state.folds.remove(state.folds.size() - 1);
        }
        state.currentFold = parent;
        state.markers.pop();
    }

    /**
     * Returns whether we are inside a marked content sequence fold.
     *
     * @param state Current folding algorithm state.
     *
     * @return Whether we are inside a marked content sequence fold.
     */
    private static boolean inMarkedContent(State state) {
        return MARKED_CONTENT == state.markers.peek();
    }

    /**
     * Returns whether we are inside a text object block fold.
     *
     * @param state Current folding algorithm state.
     *
     * @return Whether we are inside a text object block fold.
     */
    private static boolean inTextObject(State state) {
        return TEXT_OBJECT == state.markers.peek();
    }

    /**
     * Folding algorithm state.
     */
    private static final class State {
        private final List<Fold> folds = new ArrayList<>();
        private final Deque<Object> markers = new ArrayDeque<>(DEFAULT_MARKER_STACK_SIZE);
        private Fold currentFold = null;
    }
}
