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
package com.itextpdf.rups.view.contextmenu;

import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.rups.Rups;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.itext.PdfTree;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Custom action to save raw bytes of a stream to a file from the PdfTree view.
 *
 * @author Michael Demey
 */
public final class SaveToFilePdfTreeAction extends AbstractRupsAction {

    private final boolean saveRawBytes;

    public SaveToFilePdfTreeAction(String name, Component invoker, boolean raw) {
        super(name, invoker);
        saveRawBytes = raw;
    }

    public void actionPerformed(ActionEvent event) {
        SwingUtilities.invokeLater(this::handleAction);
    }

    private void handleAction() {
        // get saving location
        final JFileChooser fileChooser = new JFileChooser();

        if (saveRawBytes) {
            fileChooser.setDialogTitle(fileChooser.getDialogTitle() + Language.RAW_BYTES.getString());
        }

        final int choice = fileChooser.showSaveDialog(null);
        // Early exit on cancel
        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final Path path = fileChooser.getSelectedFile().toPath();

        // Get path to the node
        final PdfTree tree = (PdfTree) invoker;
        final TreeSelectionModel selectionModel = tree.getSelectionModel();
        final TreePath[] paths = selectionModel.getSelectionPaths();

        // get the bytes and write away
        try (final OutputStream fos = Files.newOutputStream(path)) {
            writeBytes(fos, paths, saveRawBytes);
        } catch (IOException e) {
            final String errorMessage = Language.ERROR_WRITING_FILE.getString();
            LoggerHelper.error(errorMessage, e, getClass());
            Rups.showBriefMessage(errorMessage);
        }
    }

    private static void writeBytes(OutputStream os, TreePath[] paths, boolean raw)
            throws IOException {
        if (paths.length == 0) {
            return;
        }
        final Object node = paths[0].getLastPathComponent();
        if (node instanceof PdfObjectTreeNode) {
            writeBytes(os, (PdfObjectTreeNode) node, raw);
        } else if (node instanceof AbstractAsn1TreeNode) {
            writeBytes(os, (AbstractAsn1TreeNode) node, raw);
        }
    }

    private static void writeBytes(OutputStream os, PdfObjectTreeNode node, boolean raw)
            throws IOException {
        final PdfObject object = node.getPdfObject();
        switch (object.getType()) {
            case PdfObject.STREAM:
                writeBytes(os, (PdfStream) object, raw);
                break;
            case PdfObject.STRING:
                writeBytes(os, (PdfString) object, raw);
                break;
            default:
                // noop
        }
    }

    private static void writeBytes(OutputStream os, PdfStream stream, boolean raw)
            throws IOException {
        os.write(stream.getBytes(!raw));
    }

    private static void writeBytes(OutputStream os, PdfString string, boolean raw)
            throws IOException {
        final byte[] data;
        if (raw) {
            data = string.getValueBytes();
        } else {
            data = string.toUnicodeString().getBytes(StandardCharsets.UTF_8);
        }
        os.write(data);
    }

    private static void writeBytes(OutputStream os, AbstractAsn1TreeNode node, boolean raw)
            throws IOException {
        if (raw) {
            node.getAsn1Primitive().encodeTo(os);
        } else {
            node.toDisplayJson(os);
            // Ensure blank line at the end for *nix tools sake
            os.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        }
    }
}
