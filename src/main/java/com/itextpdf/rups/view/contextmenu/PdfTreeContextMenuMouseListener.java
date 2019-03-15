/*
    * $Id$

    This file is part of the iText (R) project.
    Copyright (c) 2007-2019 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
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

import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.rups.view.itext.PdfTree;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseEvent;

/**
 * Additional check to see whether the selected node is a Stream (and not an image stream)
 *
 * @author Michael Demey
 */
public class PdfTreeContextMenuMouseListener extends ContextMenuMouseListener {

    public PdfTreeContextMenuMouseListener(JPopupMenu popup, JComponent component) {
        super(popup, component);
    }

    @Override
    public boolean showPopupHook(MouseEvent event) {
        PdfTree tree = (PdfTree) component;

        int row = tree.getClosestRowForLocation(event.getX(), event.getY());
        tree.setSelectionRow(row);
        TreeSelectionModel selectionModel = ((PdfTree) component).getSelectionModel();
        TreePath[] paths = selectionModel.getSelectionPaths();

        if (paths == null || paths.length < 1) {
            return false;
        }

        PdfObjectTreeNode lastPath = ((PdfObjectTreeNode) paths[0].getLastPathComponent());

        if (!lastPath.isStream()) {
            return false;
        }

        // Make sure the selected item isn't an image
        PdfStream stream = (PdfStream) lastPath.getPdfObject();
        return !PdfName.Image.equals(stream.get(PdfName.Subtype));
    }
}
