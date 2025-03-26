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
package com.itextpdf.rups.view.itext.stream;

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
import com.itextpdf.rups.view.contextmenu.StreamPanelContextMenu;
import com.itextpdf.rups.view.itext.stream.editor.PdfSyntaxTextArea;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

import java.awt.BorderLayout;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import javax.swing.JPanel;
import javax.swing.tree.TreeNode;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.ExpandedFoldRenderStrategy;
import org.fife.ui.rtextarea.RTextScrollPane;

public final class StreamTextEditorPane extends JPanel implements IRupsEventListener {
    /**
     * Char buffer with a single LF character.
     */
    private static final char[] LF_TEXT = {'\n'};
    /**
     * Max text line width after which it will be forcefully split.
     */
    private static final int MAX_LINE_LENGTH = 2048;

    private static final Method GET_INPUT_STREAM_METHOD;

    private final RTextScrollPane textScrollPane;
    private final StreamPanelContextMenu popupMenu;

    //Todo: Remove that field after proper application structure will be implemented.
    private final PdfReaderController controller;
    private PdfObjectTreeNode target;
    private boolean editable = false;

    static {
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
        super(new BorderLayout());
        this.controller = controller;

        final PdfSyntaxTextArea textArea = new PdfSyntaxTextArea();
        this.textScrollPane = new RTextScrollPane(textArea);
        // This will make sure, that the arrow for folding code blocks are
        // always visible
        this.textScrollPane.getGutter().setExpandedFoldRenderStrategy(
                ExpandedFoldRenderStrategy.ALWAYS
        );
        add(this.textScrollPane);

        final ErrorStrip errorStrip = new ErrorStrip(textArea);
        add(errorStrip, BorderLayout.LINE_END);

        popupMenu = new StreamPanelContextMenu(getTextArea(), this);
        // TODO: Augment existing menu with our own options
//        getTextArea().setComponentPopupMenu(popupMenu);
//        getTextArea().addMouseListener(new ContextMenuMouseListener(popupMenu, getTextArea()));
    }

    public PdfSyntaxTextArea getTextArea() {
        return (PdfSyntaxTextArea) textScrollPane.getTextArea();
    }

    /**
     * Renders the content stream of a PdfObject or empties the text area.
     *
     * @param target the node of which the content stream needs to be rendered
     */
    public void render(PdfObjectTreeNode target) {
        getTextArea().discardAllEdits();
        this.target = target;
        final PdfStream stream = getTargetStream();
        if (stream == null) {
            clearPane();
            return;
        }

        // Assuming that this will stop parsing for a moment...
        getTextArea().setVisible(false);
        String textToSet;
        String styleToSet;
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
                styleToSet = PdfSyntaxTextArea.SYNTAX_STYLE_BINARY;
                editableToSet = false;
            } else {
                textToSet = prepareContentStreamText(getText(stream, true));
                styleToSet = PdfSyntaxTextArea.SYNTAX_STYLE_PDF;
                editableToSet = true;
            }
            setTextEditableRoutine(true);
        } catch (RuntimeException e) {
            LoggerHelper.error(Language.ERROR_UNEXPECTED_EXCEPTION.getString(), e, getClass());
            textToSet = "";
            styleToSet = SyntaxConstants.SYNTAX_STYLE_NONE;
            editableToSet = false;
        }
        getTextArea().setSyntaxEditingStyle(styleToSet);
        getTextArea().setText(textToSet);
        getTextArea().setCaretPosition(0);
        getTextArea().discardAllEdits();
        setTextEditableRoutine(editableToSet);
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
        getTextArea().setText("");
        getTextArea().discardAllEdits();
        setTextEditableRoutine(false);
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

    private static String getText(PdfStream stream, boolean decoded) {
        return new String(stream.getBytes(decoded), StandardCharsets.ISO_8859_1);
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
