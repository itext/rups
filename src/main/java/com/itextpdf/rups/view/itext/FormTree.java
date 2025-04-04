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

import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.TreeNodeFactory;
import com.itextpdf.rups.model.XfaFile;
import com.itextpdf.rups.model.IRupsEventListener;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.icons.IconTreeCellRenderer;
import com.itextpdf.rups.view.itext.treenodes.FormTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;
import com.itextpdf.rups.view.itext.treenodes.XfaTreeNode;
import org.dom4j.DocumentException;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.io.IOException;
import java.util.Enumeration;

/**
 * A JTree visualizing information about the Interactive Form of the
 * PDF file (if any). Normally shows a tree view of the field hierarchy
 * and individual XDP packets.
 */
public final class FormTree extends JTree implements TreeSelectionListener, IRupsEventListener {

    /**
     * Nodes in the FormTree correspond with nodes in the main PdfTree.
     */
    private final PdfReaderController controller;

    /**
     * Treeview of the XFA file.
     */
    private final XfaTree xfaTree;
    /**
     * Textview of the XFA file.
     */
    private final XfaTextArea xfaTextArea;

    /**
     * Creates a new FormTree.
     *
     * @param controller a PdfReaderController
     */
    public FormTree(PdfReaderController controller) {
        super();
        this.controller = controller;
        setCellRenderer(new IconTreeCellRenderer());
        setModel(new DefaultTreeModel(new FormTreeNode()));
        addTreeSelectionListener(this);
        xfaTree = new XfaTree();
        xfaTree.clear();
        xfaTextArea = new XfaTextArea();
    }

    /**
     * Method that can be used recursively to load the fields hierarchy into the tree.
     *
     * @param factory    a factory that can produce new PDF object nodes
     * @param formNode   the parent node in the form tree
     * @param objectNode the object node that will be used to create a child node
     */
    private static void loadFields(TreeNodeFactory factory, FormTreeNode formNode, PdfObjectTreeNode objectNode) {
        if (objectNode == null) {
            return;
        }
        factory.expandNode(objectNode);
        if (objectNode.isIndirectReference()) {
            loadFields(factory, formNode, (PdfObjectTreeNode) objectNode.getFirstChild());
        } else if (objectNode.isArray()) {
            Enumeration<TreeNode> children = objectNode.children();
            while (children.hasMoreElements()) {
                loadFields(factory, formNode, (PdfObjectTreeNode) children.nextElement());
            }
        } else if (objectNode.isDictionary()) {
            FormTreeNode leaf = new FormTreeNode(objectNode);
            formNode.add(leaf);
            PdfObjectTreeNode kids = factory.getChildNode(objectNode, PdfName.Kids);
            loadFields(factory, leaf, kids);
        }
    }

    /**
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent evt) {
        if (controller == null) {
            return;
        }
        final FormTreeNode selectedNode = (FormTreeNode) this.getLastSelectedPathComponent();
        if (selectedNode == null) {
            return;
        }
        final PdfObjectTreeNode node = selectedNode.getCorrespondingPdfObjectNode();
        if (node != null) {
            controller.selectNode(node);
        }
    }

    public XfaTree getXfaTree() {
        return xfaTree;
    }

    public XfaTextArea getXfaTextArea() {
        return xfaTextArea;
    }

    /**
     * Method that will load the nodes that refer to XFA streams.
     *
     * @param formNode   the parent node in the form tree
     * @param objectNode the object node that will be used to create a child node
     */
    static void loadXfa(TreeNodeFactory factory, XfaTreeNode formNode, PdfObjectTreeNode objectNode) {
        if (objectNode == null) {
            return;
        }
        factory.expandNode(objectNode);
        if (objectNode.isIndirectReference()) {
            loadXfa(factory, formNode, (PdfObjectTreeNode) objectNode.getFirstChild());
        } else if (objectNode.isArray()) {
            Enumeration<TreeNode> children = objectNode.children();
            PdfObjectTreeNode key;
            PdfObjectTreeNode value;
            while (children.hasMoreElements()) {
                key = (PdfObjectTreeNode) children.nextElement();
                value = (PdfObjectTreeNode) children.nextElement();
                if (value.isIndirectReference()) {
                    factory.expandNode(value);
                    value = (PdfObjectTreeNode) value.getFirstChild();
                }
                formNode.addPacket(key.getPdfObject().toString(), value);
            }
        } else if (objectNode.isStream()) {
            formNode.addPacket(Language.FORM_XDP.getString(), objectNode);
        }
    }

    @Override
    public void handleCloseDocument() {
        xfaTree.clear();
        xfaTextArea.clear();
        setModel(new DefaultTreeModel(new FormTreeNode()));
    }

    @Override
    public void handleOpenDocument(ObjectLoader loader) {
        final TreeNodeFactory factory = loader.getNodes();
        final PdfTrailerTreeNode trailer = controller.getPdfTree().getRoot();
        final PdfObjectTreeNode catalog = factory.getChildNode(trailer, PdfName.Root);
        final PdfObjectTreeNode form = factory.getChildNode(catalog, PdfName.AcroForm);
        if (form == null) {
            return;
        }
        final PdfObjectTreeNode fields = factory.getChildNode(form, PdfName.Fields);
        final FormTreeNode root = new FormTreeNode();
        if (fields != null) {
            final FormTreeNode node = new FormTreeNode(fields);
            node.setUserObject(Language.FORM_FIELDS.getString());
            loadFields(factory, node, fields);
            root.add(node);
        }
        final PdfObjectTreeNode xfa = factory.getChildNode(form, PdfName.XFA);
        if (xfa != null) {
            final XfaTreeNode node = new XfaTreeNode(xfa);
            node.setUserObject(Language.FORM_XFA.getString());
            loadXfa(factory, node, xfa);
            root.add(node);
            try {
                final XfaFile xfaFile = new XfaFile(node);
                xfaTree.load(xfaFile);
                xfaTextArea.load(xfaFile);
            } catch (IOException e) {
                LoggerHelper.warn(Language.ERROR_LOADING_XFA.getString(), e, getClass());
            } catch (DocumentException e) {
                LoggerHelper.error(Language.ERROR_PARSING_XML.getString(), e, getClass());
            }
        }
        setModel(new DefaultTreeModel(root));
    }
}
