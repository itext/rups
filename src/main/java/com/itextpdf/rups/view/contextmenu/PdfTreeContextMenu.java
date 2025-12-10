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

import com.itextpdf.brotlicompressor.BrotliStreamCompressionStrategy;
import com.itextpdf.kernel.pdf.FlateCompressionStrategy;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.io.encoders.ASCII85CompressionStrategy;
import com.itextpdf.rups.io.encoders.ASCIIHexCompressionStrategy;
import com.itextpdf.rups.io.encoders.RunLengthCompressionStrategy;
import com.itextpdf.rups.util.ExcludeFromGeneratedJacocoReport;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.itext.PdfTree;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 * Convenience class for the popup menu for the PdfTree panel.
 *
 * @author Michael Demey
 */
// Excluding from coverage, as this is just logic for creating a UI pop-up menu
@ExcludeFromGeneratedJacocoReport
public final class PdfTreeContextMenu extends JPopupMenu {
    private final PdfTree parentTree;

    private final JMenuItem inspectObjectMenu;
    private final JMenuItem saveRawBytesToFileMenu;
    private final JMenuItem saveToFileMenu;
    private final JSeparator filterSectionSeparator;
    private final JMenu applyFilterSubMenu;
    private final JMenuItem removeAllFiltersMenu;

    public PdfTreeContextMenu(PdfTree parentTree, PdfReaderController controller) {
        this.parentTree = parentTree;

        inspectObjectMenu = createJMenuItem(new InspectObjectAction(
                Language.INSPECT_OBJECT.getString(),
                parentTree
        ));
        saveRawBytesToFileMenu = createJMenuItem(new SaveToFilePdfTreeAction(
                Language.SAVE_RAW_BYTES_TO_FILE.getString(),
                parentTree,
                true
        ));
        saveToFileMenu = createJMenuItem(new SaveToFilePdfTreeAction(
                Language.SAVE_TO_FILE.getString(),
                parentTree,
                false
        ));
        removeAllFiltersMenu = createJMenuItem(new RemoveAllFiltersAction(
                Language.REMOVE_ALL_FILTERS.getString(),
                parentTree,
                controller
        ));
        final JMenuItem applyAscii85DecodeMenu = createJMenuItem(new ApplyFilterAction(
                PdfName.ASCII85Decode.getValue(),
                parentTree,
                controller,
                ASCII85CompressionStrategy::new
        ));
        final JMenuItem applyAsciiHexDecodeMenu = createJMenuItem(new ApplyFilterAction(
                PdfName.ASCIIHexDecode.getValue(),
                parentTree,
                controller,
                ASCIIHexCompressionStrategy::new
        ));
        final JMenuItem applyBrotliDecodeMenu = createJMenuItem(new ApplyFilterAction(
                PdfName.BrotliDecode.getValue(),
                parentTree,
                controller,
                BrotliStreamCompressionStrategy::new
        ));
        final JMenuItem applyFlateDecodeMenu = createJMenuItem(new ApplyFilterAction(
                PdfName.FlateDecode.getValue(),
                parentTree,
                controller,
                FlateCompressionStrategy::new
        ));
        final JMenuItem applyRunLengthDecodeMenu = createJMenuItem(new ApplyFilterAction(
                PdfName.RunLengthDecode.getValue(),
                parentTree,
                controller,
                RunLengthCompressionStrategy::new
        ));

        filterSectionSeparator = new JPopupMenu.Separator();

        applyFilterSubMenu = new JMenu(Language.APPLY_FILTER.getString());
        applyFilterSubMenu.add(applyAscii85DecodeMenu);
        applyFilterSubMenu.add(applyAsciiHexDecodeMenu);
        applyFilterSubMenu.add(applyBrotliDecodeMenu);
        applyFilterSubMenu.add(applyFlateDecodeMenu);
        applyFilterSubMenu.add(applyRunLengthDecodeMenu);

        add(inspectObjectMenu);
        add(saveRawBytesToFileMenu);
        add(saveToFileMenu);
        add(filterSectionSeparator);
        add(applyFilterSubMenu);
        add(removeAllFiltersMenu);
    }

    public void prepareForNode(IPdfContextMenuTarget node) {
        inspectObjectMenu.setEnabled(node.supportsInspectObject());
        saveRawBytesToFileMenu.setEnabled(node.supportsSave());
        saveToFileMenu.setEnabled(node.supportsSave());
        filterSectionSeparator.setVisible(node.isPdfStreamNode());
        filterSectionSeparator.setEnabled(parentTree.isMutable());
        applyFilterSubMenu.setVisible(node.isPdfStreamNode());
        applyFilterSubMenu.setEnabled(parentTree.isMutable());
        removeAllFiltersMenu.setVisible(node.isPdfStreamNode());
        removeAllFiltersMenu.setEnabled(parentTree.isMutable());
    }

    private static JMenuItem createJMenuItem(AbstractRupsAction rupsAction) {
        final JMenuItem jMenuItem = new JMenuItem();
        jMenuItem.setText((String) rupsAction.getValue(Action.NAME));
        jMenuItem.setAction(rupsAction);
        return jMenuItem;
    }
}
