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
package com.itextpdf.rups.view.itext;

import com.itextpdf.rups.io.listeners.PdfTreeExpansionListener;
import com.itextpdf.rups.io.listeners.PdfTreeNavigationListener;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.IRupsEventListener;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.icons.IconTreeCellRenderer;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * A JTree that shows the object hierarchy of a PDF document.
 */
public final class PdfTree extends JTree implements IRupsEventListener {

    /**
     * The root of the PDF tree.
     */
    private PdfTrailerTreeNode root;

    /**
     * Constructs a PDF tree.
     */
    public PdfTree() {
        super();
        final PdfTreeNavigationListener listener = new PdfTreeNavigationListener();
        addKeyListener(listener);
        addMouseListener(listener);
        setCellRenderer(new IconTreeCellRenderer());
        addTreeExpansionListener(new PdfTreeExpansionListener());
        reset();
    }

    /**
     * Getter for the root node
     *
     * @return the PDF Trailer node
     */
    public PdfTrailerTreeNode getRoot() {
        return root;
    }

    /**
     * Select a specific node in the tree.
     * Typically this method will be called from a different tree,
     * such as the pages, outlines or form tree.
     *
     * @param node the node that has to be selected
     */
    public void selectNode(DefaultMutableTreeNode node) {
        if (node != null) {
            final TreePath path = new TreePath(node.getPath());
            setSelectionPath(path);
            scrollPathToVisible(path);
        }
    }

    @Override
    public void handleCloseDocument() {
        reset();
    }

    @Override
    public void handleOpenDocument(ObjectLoader loader) {
        root.setTrailer(loader.getFile().getPdfDocument().getTrailer());
        root.setUserObject(String.format(Language.PDF_OBJECT_TREE.getString(), loader.getLoaderName()));
        loader.getNodes().expandNode(root);
        setModel(new DefaultTreeModel(root));
    }

    private void reset() {
        root = new PdfTrailerTreeNode();
        setModel(new DefaultTreeModel(root));
    }
}
