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
package com.itextpdf.rups.view.models;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfBoolean;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.model.PdfSyntaxParser;

import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class PdfArrayTableModelTest {
    @Test
    void editable() {
        final PdfArray array = new PdfArray(List.of(PdfName.Length));
        final PdfArrayTableModel model = new PdfArrayTableModel(array, new PdfSyntaxParser(), null);
        Assertions.assertEquals(1, model.getButtonColumn());

        Assertions.assertFalse(model.isEditable());
        Assertions.assertEquals(1, model.getRowCount());
        Assertions.assertEquals(1, model.getColumnCount());
        Assertions.assertFalse(model.isCellEditable(0, 0));
        Assertions.assertFalse(model.isCellEditable(0, 1));

        model.setEditable(true);
        Assertions.assertTrue(model.isEditable());
        Assertions.assertEquals(2, model.getRowCount());
        Assertions.assertEquals(2, model.getColumnCount());
        Assertions.assertTrue(model.isCellEditable(0, 0));
        Assertions.assertFalse(model.isCellEditable(0, 1));
        Assertions.assertTrue(model.isCellEditable(1, 0));
        Assertions.assertFalse(model.isCellEditable(1, 1));
    }

    @Test
    void getValueAt() {
        /*
         * As of writing this test, DictionaryTableModel::getValueAt behavior
         * is pretty inconsistent. For existing data rows, first column
         * returns a PDFName and the second one returns a String. But the last
         * row of the not-yet-created row returns a String in the first column.
         * Not changing this for now, as it might break stuff...
         */
        final PdfArray array = createTestArray();
        final PdfArrayTableModel model = new PdfArrayTableModel(array, new PdfSyntaxParser(), null);
        // Length row
        Assertions.assertEquals(PdfName.Length.toString(), model.getValueAt(0, 0));
        Assertions.assertNull(model.getValueAt(0, 1));
        // True row
        Assertions.assertEquals(PdfBoolean.TRUE.toString(), model.getValueAt(1, 0));
        Assertions.assertNull(model.getValueAt(1, 1));
        // Last row is the new entry row
        Assertions.assertEquals("", model.getValueAt(2, 0));
        Assertions.assertNull(model.getValueAt(2, 1));
        // No rows after that
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> model.getValueAt(3, 0));
    }

    @Test
    void setValueAt_UpdateNonStringObject() {
        executeNoChangeExpectedTest((PdfArrayTableModel model) ->
                model.setValueAt(1, 0, 0)
        );
    }

    @Test
    void setValueAt_UpdateBlankString() {
        executeNoChangeExpectedTest((PdfArrayTableModel model) ->
                model.setValueAt("  ", 1, 0)
        );
    }

    @Test
    void setValueAt_UpdateInvalidValue() {
        executeNoChangeExpectedTest((PdfArrayTableModel model) ->
                model.setValueAt("[0 1 2>", 0, 0)
        );
    }

    @Test
    void setValueAt_UpdateValidValue() {
        final PdfArray array = createTestArray();
        final PdfArrayTableModel model = new PdfArrayTableModel(array, new PdfSyntaxParser(), null);
        model.setEditable(true);
        // Expecting a value update
        model.setValueAt("[2 4]", 1, 0);
        Assertions.assertEquals(2, array.size());
        Assertions.assertSame(PdfName.Length, array.get(0));
        Assertions.assertArrayEquals(
                new PdfArray(new int[] {2, 4}).toIntArray(),
                array.getAsArray(1).toIntArray()
        );
    }

    @Test
    void setValueAt_UpdateNewRow() {
        final PdfArray array = createTestArray();
        final PdfArrayTableModel model = new PdfArrayTableModel(array, new PdfSyntaxParser(), null);
        model.setEditable(true);
        // Temp values in the model should change
        final List<PdfObject> entriesBefore = copyArrayEntries(array);
        model.setValueAt("/CS", 2, 0);
        Assertions.assertEquals("/CS", model.getValueAt(2, 0));
        // Only temp values in the model should change, not the backing dict
        Assertions.assertEquals(entriesBefore, array.toList());
    }

    @Test
    void removeRow() {
        final PdfArray array = createTestArray();
        final PdfArrayTableModel model = new PdfArrayTableModel(array, new PdfSyntaxParser(), null);
        model.setEditable(true);
        // Deleting rows one-by-one
        model.removeRow(1);
        Assertions.assertEquals(1, array.size());
        Assertions.assertSame(PdfName.Length, array.get(0));
        // Should no longer work, as there are not enough rows
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> model.removeRow(1));
        model.removeRow(0);
        Assertions.assertTrue(array.isEmpty());
    }

    @Test
    void validateTempRow_BlankValue() {
        executeNoChangeExpectedTest((PdfArrayTableModel model) -> {
            model.setValueAt("  ", 2, 1);
            model.validateTempRow();
        });
    }

    @Test
    void validateTempRow_InvalidValue() {
        executeNoChangeExpectedTest((PdfArrayTableModel model) -> {
            model.setValueAt("[0>", 2, 0);
            model.validateTempRow();
        });
    }

    private static void executeNoChangeExpectedTest(Consumer<PdfArrayTableModel> consumer) {
        final PdfArray array = createTestArray();
        final PdfArrayTableModel model = new PdfArrayTableModel(array, new PdfSyntaxParser(), null);
        model.setEditable(true);
        final List<PdfObject> entriesBefore = copyArrayEntries(array);
        consumer.accept(model);
        Assertions.assertEquals(entriesBefore, array.toList());
    }

    private static PdfArray createTestArray() {
        final PdfArray array = new PdfArray();
        array.add(PdfName.Length);
        array.add(PdfBoolean.TRUE);
        return array;
    }

    private static List<PdfObject> copyArrayEntries(PdfArray array) {
        return ((PdfArray) array.clone()).toList();
    }
}