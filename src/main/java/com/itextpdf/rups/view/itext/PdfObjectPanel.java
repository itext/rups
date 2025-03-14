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

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfSyntaxParser;
import com.itextpdf.rups.model.IRupsEventListener;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.icons.IconFetcher;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.models.AbstractPdfObjectPanelTableModel;
import com.itextpdf.rups.view.models.DictionaryTableModel;
import com.itextpdf.rups.view.models.DictionaryTableModelButton;
import com.itextpdf.rups.view.models.PdfArrayTableModel;

import java.awt.CardLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class PdfObjectPanel implements IRupsEventListener {

    private static final String ADD_ICON = "add.png";
    private static final String CROSS_ICON = "cross.png";

    /**
     * The layout that will show the info about the PDF object that is being analyzed.
     */
    private final CardLayout layout = new CardLayout();


    /**
     * Table with dictionary entries.
     */
    private final JTable table = new JTable();
    /**
     * The text pane with the info about a PDF object in the bottom panel.
     */
    private final JTextArea text = new JTextArea();

    private final JPanel panel = new JPanel();

    private PdfObjectTreeNode target;

    private boolean editable = false;

    private final List<IPdfObjectPanelEventListener> eventListeners = new ArrayList<>();

    /**
     * Creates a PDF object panel.
     */
    public PdfObjectPanel() {
        // layout
        panel.setLayout(layout);

        // dictionary / array / stream
        final JScrollPane dictScrollPane = new JScrollPane();
        dictScrollPane.setViewportView(table);
        panel.add(dictScrollPane);

        // number / string / ...
        final JScrollPane textScrollPane = new JScrollPane();
        textScrollPane.setViewportView(text);
        text.setEditable(false);
        panel.add(textScrollPane);

        table.addMouseListener(new JTableButtonMouseListener());
    }

    public void addEventListener(IPdfObjectPanelEventListener listener) {
        eventListeners.add(Objects.requireNonNull(listener));
    }

    /**
     * Clear the object panel.
     */
    public void clear() {
        target = null;
        table.setModel(new DefaultTableModel());
        text.setText(null);
        layout.show(panel, Language.TEXT.getString());
    }

    @Override
    public void handleCloseDocument() {
        clear();
        setEditable(false);
    }

    @Override
    public void handleOpenDocument(ObjectLoader loader) {
        clear();
        setEditable(loader.getFile().isOpenedAsOwner());
    }

    @Override
    public void handleNewIndirectObject(PdfObject object) {
        clear();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;

        // Update the state of an existing model
        final AbstractPdfObjectPanelTableModel model = getTableModel();
        if (model != null) {
            model.setEditable(editable);
        }
    }

    /**
     * Shows a PdfObject as text or in a table.
     *
     * @param node   the node's content that needs to be shown.
     * @param parser the pdf syntax parser
     */
    public void render(PdfObjectTreeNode node, PdfSyntaxParser parser) {
        target = node;
        final PdfObject object = node.getPdfObject();
        if (object == null) {
            text.setText(null);
            table.setModel(new DefaultTableModel());
            layout.show(panel, Language.TEXT.getString());
            panel.repaint();
            text.repaint();
            return;
        }

        switch (object.getType()) {
            case PdfObject.DICTIONARY:
            case PdfObject.STREAM:
                final DictionaryTableModel model =
                        new DictionaryTableModel((PdfDictionary) object, parser, panel);
                model.setEditable(editable);
                model.addTableModelListener(new DictionaryModelListener());
                table.setModel(model);
                table.setDefaultRenderer(
                        DictionaryTableModelButton.class,
                        new DictionaryTableModelButton(IconFetcher.getIcon(CROSS_ICON), IconFetcher.getIcon(ADD_ICON))
                );
                layout.show(panel, Language.TABLE.getString());
                panel.repaint();
                break;
            case PdfObject.ARRAY:
                final PdfArrayTableModel arrayModel =
                        new PdfArrayTableModel((PdfArray) object, parser, panel);
                arrayModel.addTableModelListener(new ArrayModelListener());
                arrayModel.setEditable(editable);
                table.setModel(arrayModel);
                table.setDefaultRenderer(
                        DictionaryTableModelButton.class,
                        new DictionaryTableModelButton(IconFetcher.getIcon(CROSS_ICON), IconFetcher.getIcon(ADD_ICON))
                );
                layout.show(panel, Language.TABLE.getString());
                panel.repaint();
                break;
            case PdfObject.STRING:
                text.setText(((PdfString) object).toUnicodeString());
                layout.show(panel, Language.TEXT.getString());
                break;
            default:
                text.setText(object.toString());
                layout.show(panel, Language.TEXT.getString());
                break;
        }
    }

    private AbstractPdfObjectPanelTableModel getTableModel() {
        final TableModel model = table.getModel();
        if (model instanceof AbstractPdfObjectPanelTableModel) {
            return (AbstractPdfObjectPanelTableModel) model;
        }
        return null;
    }

    private void fireEvent(Consumer<IPdfObjectPanelEventListener> func) {
        for (final IPdfObjectPanelEventListener listener: eventListeners) {
            func.accept(listener);
        }
    }

    private final class JTableButtonMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            final int selectedColumn = table.getSelectedColumn();

            if (selectedColumn != ((AbstractPdfObjectPanelTableModel) table.getModel()).getButtonColumn()) {
                return;
            }

            final int selectedRow = table.getSelectedRow();
            final int rowCount = table.getRowCount();

            if (rowCount == 1 || rowCount - 1 == selectedRow) {
                ((AbstractPdfObjectPanelTableModel) table.getModel()).validateTempRow();
                return;
            }

            /*Checking the row or column is valid or not*/
            if (selectedRow < rowCount - 1 && selectedRow >= 0 && target != null) {
                ((AbstractPdfObjectPanelTableModel) table.getModel()).removeRow(selectedRow);
            }
        }
    }

    /**
     * Notify PdfReader Controller about changes in DictionaryModel
     */
    private final class DictionaryModelListener implements TableModelListener {
        @Override
        public void tableChanged(TableModelEvent e) {
            final int row = e.getFirstRow();
            if (row == TableModelEvent.HEADER_ROW || row != e.getLastRow()) {
                return;
            }
            final PdfName key = (PdfName) table.getValueAt(row, 0);
            final PdfObject value = ((PdfDictionary) target.getPdfObject()).get(key, false);
            switch (e.getType()) {
                case TableModelEvent.UPDATE:
                    break;
                case TableModelEvent.DELETE:
                    fireEvent(c -> c.handleDictChildDeleted(target, key));
                    break;
                case TableModelEvent.INSERT:
                    fireEvent(c -> c.handleDictChildAdded(value, target, key, row));
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected event type: " + e.getType());
            }
        }
    }

    /**
     * Notify PdfReader Controller about changes in ArrayModel
     */
    private final class ArrayModelListener implements TableModelListener {
        @Override
        public void tableChanged(TableModelEvent e) {
            final int row = e.getFirstRow();
            if (row < 0 || row != e.getLastRow()) {
                return;
            }
            final PdfObject value = ((PdfArray) target.getPdfObject()).get(row, false);
            switch (e.getType()) {
                case TableModelEvent.UPDATE:
                    // noop
                    break;
                case TableModelEvent.DELETE:
                    fireEvent(c -> c.handleArrayChildDeleted(target, row));
                    break;
                case TableModelEvent.INSERT:
                    fireEvent(c -> c.handleArrayChildAdded(value, target, row));
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected event type: " + e.getType());
            }
        }
    }
}
