/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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

import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.view.itext.PdfTree;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

import javax.swing.tree.TreePath;

public abstract class AbstractPdfStreamAction extends AbstractRupsAction {
    protected final transient PdfReaderController controller;

    protected AbstractPdfStreamAction(String name, PdfTree invoker, PdfReaderController controller) {
        super(name, invoker);
        this.controller = controller;
    }

    protected PdfObjectTreeNode getTargetPdfStreamNode() {
        final PdfTree tree = (PdfTree) invoker;
        final Object node = tree.getLastSelectedPathComponent();
        if (!(node instanceof PdfObjectTreeNode)) {
            return null;
        }
        final PdfObjectTreeNode objectNode = (PdfObjectTreeNode) node;
        if (!objectNode.isPdfStreamNode()) {
            return null;
        }
        return objectNode;
    }

    protected void forceTreeRebuild(PdfObjectTreeNode root) {
        // We need to delete all children from the tree node to force them to
        // be regenerated after the update. Presumably there should be a
        // better way to do this, but this works fine for now
        if (controller != null) {
            final TreePath path = new TreePath(root.getPath());
            boolean wasExpanded = controller.getPdfTree().isExpanded(path);
            controller.deleteAllTreeChildren(root);
            controller.selectNode(root);
            if (wasExpanded) {
                controller.getPdfTree().expandPath(path);
            } else {
                controller.getPdfTree().collapsePath(path);
            }
        }
    }
}
