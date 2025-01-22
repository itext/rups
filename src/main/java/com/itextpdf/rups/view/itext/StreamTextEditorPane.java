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
package com.itextpdf.rups.view.itext;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.model.IRupsEventListener;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.contentstream.ParseTreeNode;
import com.itextpdf.rups.model.contentstream.ParseTreeNodeType;
import com.itextpdf.rups.model.contentstream.PdfContentStreamParser;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.contextmenu.ContextMenuMouseListener;
import com.itextpdf.rups.view.contextmenu.StreamPanelContextMenu;
import com.itextpdf.rups.view.itext.editor.Latin1Filter;
import com.itextpdf.rups.view.itext.editor.PdfFoldParser;
import com.itextpdf.rups.view.itext.editor.PdfTokenMaker;
import com.itextpdf.rups.view.itext.editor.PdfTokenPainterFactory;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.tree.TreeNode;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.DefaultTokenPainterFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.ExpandedFoldRenderStrategy;
import org.fife.ui.rtextarea.RTextScrollPane;

public final class StreamTextEditorPane extends RTextScrollPane implements IRupsEventListener {
    /**
     * MIME type for a PDF content stream.
     */
    private static final String MIME_PDF = "application/pdf";
    /**
     * MIME type for plain text.
     */
    private static final String MIME_PLAIN_TEXT = "plain/text";

    /**
     * Char buffer with a single LF character.
     */
    private static final char[] LF_TEXT = {'\n'};
    /**
     * Max text line width after which it will be forcefully split.
     */
    private static final int MAX_LINE_LENGTH = 2048;
    private static final int MAX_NUMBER_OF_EDITS = 8192;

    private static final Method GET_INPUT_STREAM_METHOD;

    private final StreamPanelContextMenu popupMenu;
    private final BeepingUndoManager undoManager;

    //Todo: Remove that field after proper application structure will be implemented.
    private final PdfReaderController controller;
    private PdfObjectTreeNode target;
    private boolean editable = false;

    static {
        /*
         * Registering PDF content type, so that we could use PDF syntax
         * highlighting in RSyntaxTextArea.
         */
        final AbstractTokenMakerFactory tokenMakerFactory =
                (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        tokenMakerFactory.putMapping(MIME_PDF, PdfTokenMaker.class.getName());
        FoldParserManager.get().addFoldParserMapping(MIME_PDF, new PdfFoldParser());
        /*
         * There doesn't seem to be a good way to detect, whether you can call
         * setData on a PdfStream or not in advance. It cannot be called if a
         * PDF stream was created from an InputStream, so we will be testing
         * that via the protected method.
         */
        try {
            GET_INPUT_STREAM_METHOD = PdfStream.class.getDeclaredMethod("getInputStream");
            GET_INPUT_STREAM_METHOD.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Constructs a SyntaxHighlightedStreamPane.
     *
     * @param controller the pdf reader controller
     */
    public StreamTextEditorPane(PdfReaderController controller) {
        super(createTextArea());
        this.controller = controller;
        // This will make sure, that the arrow for folding code blocks are
        // always visible
        getGutter().setExpandedFoldRenderStrategy(ExpandedFoldRenderStrategy.ALWAYS);

        popupMenu = new StreamPanelContextMenu(getTextArea(), this);
        getTextArea().setComponentPopupMenu(popupMenu);
        getTextArea().addMouseListener(new ContextMenuMouseListener(popupMenu, getTextArea()));

        undoManager = new BeepingUndoManager();
        getDocument().addUndoableEditListener(undoManager);
        getTextArea().registerKeyboardAction(
                e -> undoManager.undo(),
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_FOCUSED
        );
        getTextArea().registerKeyboardAction(
                e -> undoManager.redo(),
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_FOCUSED
        );
    }

    @Override
    public RSyntaxTextArea getTextArea() {
        return (RSyntaxTextArea) super.getTextArea();
    }

    public RSyntaxDocument getDocument() {
        return getDocument(getTextArea());
    }

    /**
     * Renders the content stream of a PdfObject or empties the text area.
     *
     * @param target the node of which the content stream needs to be rendered
     */
    public void render(PdfObjectTreeNode target) {
        setUndoEnabled(false);
        this.target = target;
        final PdfStream stream = getTargetStream();
        if (stream == null) {
            clearPane();
            return;
        }

        // Assuming that this will stop parsing for a moment...
        getTextArea().setVisible(false);
        String textToSet;
        String mimeToSet;
        boolean editableToSet;
        /*
         * TODO: Differentiate between different content. See below.
         *
         * Images should be rendered as images (this was before the syntax
         * highlight changes). Or at least as hex binary data.
         *
         * Fonts, binary XMP or just random binary data should be displayed
         * as hex.
         *
         * XML data should be edited as XML with proper encoding and saved
         * as such.
         *
         * Only PDF content streams should be altered and parsed in a custom
         * way.
         */
        try {
            if (isFont(stream) || isImage(stream)) {
                textToSet = getText(stream, false);
                mimeToSet = MIME_PLAIN_TEXT;
                editableToSet = false;
            } else {
                textToSet = prepareContentStreamText(getText(stream, true));
                mimeToSet = MIME_PDF;
                editableToSet = true;
            }
            setTextEditableRoutine(true);
        } catch (RuntimeException e) {
            LoggerHelper.error(Language.ERROR_UNEXPECTED_EXCEPTION.getString(), e, getClass());
            textToSet = "";
            mimeToSet = MIME_PLAIN_TEXT;
            editableToSet = false;
        }
        setContentType(mimeToSet);
        getTextArea().setText(textToSet);
        getTextArea().setCaretPosition(0);
        setTextEditableRoutine(editableToSet);
        setUndoEnabled(true);
        getTextArea().setVisible(true);

        repaint();
    }

    public void saveToTarget() {
        /*
         * FIXME: With indirect objects with multiple references, this will
         *        change the tree only in one of them.
         * FIXME: This doesn't change Length...
         */
        if (controller != null && ((PdfDictionary) target.getPdfObject()).containsKey(PdfName.Filter)) {
            controller.deleteTreeNodeDictChild(target, PdfName.Filter);
        }
        /*
         * In the current state, stream node could contain ASN1. data, which
         * is parsed and added as tree nodes. After editing, it won't be valid,
         * so we must remove them.
         */
        if (controller != null) {
            int i = 0;
            while (i < target.getChildCount()) {
                final TreeNode child = target.getChildAt(i);
                if (child instanceof PdfObjectTreeNode) {
                    ++i;
                } else {
                    controller.deleteTreeChild(target, i);
                    // Will assume it being just a shift...
                }
            }
        }
        final byte[] streamData = getTextArea().getText().getBytes(StandardCharsets.ISO_8859_1);
        getTargetStream().setData(streamData);
        if (controller != null) {
            controller.selectNode(target);
        }
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        setTextEditableRoutine(editable);
    }

    @Override
    public void handleCloseDocument() {
        clearPane();
        setEditable(false);
    }

    @Override
    public void handleOpenDocument(ObjectLoader loader) {
        clearPane();
        setEditable(loader.getFile().isOpenedAsOwner());
    }

    private void setTextEditableRoutine(boolean editable) {
        // If pane is read-only or in a temp read-only state
        if (!this.editable || !editable) {
            getTextArea().setEditable(false);
            popupMenu.setSaveToStreamEnabled(false);
            return;
        }

        getTextArea().setEditable(true);
        final PdfStream targetStream = getTargetStream();
        if (targetStream != null) {
            popupMenu.setSaveToStreamEnabled(isStreamEditable(targetStream));
        } else {
            popupMenu.setSaveToStreamEnabled(false);
        }
    }

    private PdfStream getTargetStream() {
        if (target == null) {
            return null;
        }
        final PdfObject obj = target.getPdfObject();
        if (obj instanceof PdfStream) {
            return (PdfStream) obj;
        }
        return null;
    }

    private void clearPane() {
        target = null;
        setUndoEnabled(false);
        getTextArea().setText("");
        setTextEditableRoutine(false);
    }

    private void setContentType(String mime) {
        setContentType(getTextArea(), mime);
    }

    private void setUndoEnabled(boolean enabled) {
        if (enabled) {
            undoManager.setLimit(MAX_NUMBER_OF_EDITS);
        } else {
            undoManager.discardAllEdits();
            undoManager.setLimit(0);
        }
    }

    /**
     * Modifies the PDF content stream text to make it suitable for usage in
     * a code editor.
     *
     * <p>
     * At the moment this just splits lines after operators, if lines are too
     * long. If the are long lines in the code editor, is is noticeably
     * laggier.
     * </p>
     *
     * @param originalText PDF content stream text to modify.
     *
     * @return Modified PDF content stream text.
     */
    private static String prepareContentStreamText(String originalText) {
        boolean hasOnlyShortLines = true;
        int startIndex = 0;
        while (startIndex < originalText.length()) {
            int lineFeedIndex = originalText.indexOf('\n', startIndex);
            if (lineFeedIndex == -1) {
                lineFeedIndex = originalText.length();
            }
            final int length = lineFeedIndex - startIndex;
            if (length > MAX_LINE_LENGTH) {
                hasOnlyShortLines = false;
                break;
            }
            startIndex = lineFeedIndex + 1;
        }
        if (hasOnlyShortLines) {
            return originalText;
        }

        /*
         * TODO: Make this logic smarter.
         *
         * At the moment if lines are too big, we just replace all whitespace
         * after an operator with LF. This is not ideal and destructive. This
         * was prompted by a document, where lines were denoted with just CR
         * and the text area does not treat them as end-of-line indicators.
         */
        final ParseTreeNode tree = PdfContentStreamParser.parse(originalText);
        ParseTreeNode child = tree.getFirstChild();
        while (child != null) {
            if (child.getType() == ParseTreeNodeType.OPERATOR) {
                ParseTreeNode next = child.getNext();
                while (next != null && next.getType() == ParseTreeNodeType.WHITESPACE) {
                    next = next.remove();
                }
                child = child.addNext(ParseTreeNodeType.WHITESPACE, LF_TEXT, 0, LF_TEXT.length);
            }
            child = child.getNext();
        }
        return tree.getFullText();
    }

    private static RSyntaxTextArea createTextArea() {
        final RSyntaxTextArea textArea = new RSyntaxTextArea();
        /*
         * First we will set up our custom painter with our Latin-1 filter
         * "hack". The way it works is that with the filter applied, any
         * character greater than U+00FF will be replaced with a UTF-8 byte
         * representation. As in the internal char array can actually be
         * interpreted as a byte array, which wastes twice as much space...
         *
         * To make it easier to work with possible binary content of a PDF
         * stream we will use a custom token painter. It will paint non-ASCII
         * character as their hex-codes instead of their Latin-1 mapped
         * glyphs.
         *
         * Both the filter and painter should be replaced with default, when
         * we display a non-content stream. For example, for XML-based
         * metadata we should just use the regular XML editor available. But
         * by default we will just assume a PDF content stream.
         */
        setContentType(textArea, MIME_PDF);
        // This will allow to fold code blocks (like BT/ET blocks)
        textArea.setCodeFoldingEnabled(true);
        // This will automatically add tabulations, when you enter a new line
        // after a "q" operator, for example
        textArea.setAutoIndentEnabled(true);
        // This will mark identical names and operators, when cursor is on
        // them after a short delay
        textArea.setMarkOccurrences(true);
        textArea.setMarkOccurrencesDelay(500);
        return textArea;
    }

    private static void setContentType(RSyntaxTextArea textArea, String mime) {
        if (MIME_PDF.equals(mime)) {
            getDocument(textArea).setDocumentFilter(new Latin1Filter());
            textArea.setTokenPainterFactory(new PdfTokenPainterFactory());
        } else {
            getDocument(textArea).setDocumentFilter(null);
            textArea.setTokenPainterFactory(new DefaultTokenPainterFactory());
        }
        textArea.setSyntaxEditingStyle(mime);
    }

    private static String getText(PdfStream stream, boolean decoded) {
        return new String(stream.getBytes(decoded), StandardCharsets.ISO_8859_1);
    }

    private static RSyntaxDocument getDocument(RSyntaxTextArea textArea) {
        return (RSyntaxDocument) textArea.getDocument();
    }

    private static boolean isImage(PdfStream stream) {
        return PdfName.Image.equals(stream.getAsName(PdfName.Subtype));
    }

    private static boolean isFont(PdfStream stream) {
        return stream.get(PdfName.Length1) != null;
    }

    private static boolean isStreamEditable(PdfStream stream) {
        try {
            return (GET_INPUT_STREAM_METHOD.invoke(stream) == null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
