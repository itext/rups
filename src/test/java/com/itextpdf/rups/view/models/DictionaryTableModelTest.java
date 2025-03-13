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
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.model.PdfSyntaxParser;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class DictionaryTableModelTest {
    /*
     * DictionaryTableModel somewhat abuses the fact, that there is a TreeMap
     * behind the PdfDictionary implementation. As in the keys are sorted
     * lexicographically ascending. Because of that, the test rely on that as
     * well, which should be a good think, as they will break, if this ever
     * changes...
     */
    private static final PdfNumber EIGHT = new PdfNumber(8);

    @Test
    void editable() {
        final PdfDictionary dict = new PdfDictionary(Map.of(PdfName.Length, EIGHT));
        final DictionaryTableModel model = new DictionaryTableModel(dict, new PdfSyntaxParser(), null);
        Assertions.assertEquals(2, model.getButtonColumn());

        Assertions.assertFalse(model.isEditable());
        Assertions.assertEquals(1, model.getRowCount());
        Assertions.assertEquals(2, model.getColumnCount());
        Assertions.assertFalse(model.isCellEditable(0, 0));
        Assertions.assertFalse(model.isCellEditable(0, 1));
        Assertions.assertFalse(model.isCellEditable(0, 2));

        model.setEditable(true);
        Assertions.assertTrue(model.isEditable());
        Assertions.assertEquals(2, model.getRowCount());
        Assertions.assertEquals(3, model.getColumnCount());
        Assertions.assertTrue(model.isCellEditable(0, 0));
        Assertions.assertTrue(model.isCellEditable(0, 1));
        Assertions.assertFalse(model.isCellEditable(0, 2));
        Assertions.assertTrue(model.isCellEditable(1, 0));
        Assertions.assertTrue(model.isCellEditable(1, 1));
        Assertions.assertFalse(model.isCellEditable(1, 2));
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
        final PdfDictionary dict = createTestDict();
        final DictionaryTableModel model = new DictionaryTableModel(dict, new PdfSyntaxParser(), null);
        // Filter row
        Assertions.assertSame(PdfName.Filter, model.getValueAt(0, 0));
        Assertions.assertEquals(PdfName.ASCIIHexDecode.toString(), model.getValueAt(0, 1));
        Assertions.assertNull(model.getValueAt(0, 2));
        // Length row
        Assertions.assertSame(PdfName.Length, model.getValueAt(1, 0));
        Assertions.assertEquals(EIGHT.toString(), model.getValueAt(1, 1));
        Assertions.assertNull(model.getValueAt(1, 2));
        // Last row is the new entry row
        Assertions.assertEquals("/", model.getValueAt(2, 0));
        Assertions.assertEquals("", model.getValueAt(2, 1));
        Assertions.assertNull(model.getValueAt(2, 2));
        // No rows after that
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> model.getValueAt(3, 0));
    }

    @Test
    void setValueAt_UpdateNonStringObject() {
        executeNoChangeExpectedTest((DictionaryTableModel model) ->
                model.setValueAt(1, 0, 0)
        );
    }

    @Test
    void setValueAt_UpdateBlankString() {
        executeNoChangeExpectedTest((DictionaryTableModel model) ->
                model.setValueAt("  ", 1, 1)
        );
    }

    @Test
    void setValueAt_UpdateInvalidKey() {
        executeNoChangeExpectedTest((DictionaryTableModel model) ->
                model.setValueAt("[0 1 2]", 0, 0)
        );
    }

    @Test
    void setValueAt_UpdateValidKey() {
        final PdfDictionary dict = createTestDict();
        final DictionaryTableModel model = new DictionaryTableModel(dict, new PdfSyntaxParser(), null);
        model.setEditable(true);
        // Expecting a key-pair update
        model.setValueAt(PdfName.F.toString(), 0, 0);
        Assertions.assertEquals(2, dict.size());
        Assertions.assertSame(PdfName.ASCIIHexDecode, dict.get(PdfName.F));
        Assertions.assertSame(EIGHT, dict.get(PdfName.Length));
    }

    @Test
    void setValueAt_UpdateKeyWithoutSlash() {
        final PdfDictionary dict = createTestDict();
        final DictionaryTableModel model = new DictionaryTableModel(dict, new PdfSyntaxParser(), null);
        model.setEditable(true);
        // Expecting a key-pair update, it should add the slash on its own
        model.setValueAt("F", 0, 0);
        Assertions.assertEquals(2, dict.size());
        Assertions.assertSame(PdfName.ASCIIHexDecode, dict.get(PdfName.F));
        Assertions.assertSame(EIGHT, dict.get(PdfName.Length));
    }

    @Test
    void setValueAt_UpdateInvalidValue() {
        executeNoChangeExpectedTest((DictionaryTableModel model) ->
                model.setValueAt("[0 1 2>", 0, 1)
        );
    }

    @Test
    void setValueAt_UpdateValidValue() {
        final PdfDictionary dict = createTestDict();
        final DictionaryTableModel model = new DictionaryTableModel(dict, new PdfSyntaxParser(), null);
        model.setEditable(true);
        // Expecting a key-pair update
        model.setValueAt("[2 4]", 1, 1);
        Assertions.assertEquals(2, dict.size());
        Assertions.assertSame(PdfName.ASCIIHexDecode, dict.get(PdfName.Filter));
        Assertions.assertArrayEquals(
                new PdfArray(new int[] {2, 4}).toIntArray(),
                dict.getAsArray(PdfName.Length).toIntArray()
        );
    }

    @Test
    void setValueAt_UpdateNewRow() {
        final PdfDictionary dict = createTestDict();
        final DictionaryTableModel model = new DictionaryTableModel(dict, new PdfSyntaxParser(), null);
        model.setEditable(true);
        // Temp values in the model should change
        final Set<Entry<PdfName, PdfObject>> entriesBefore = copyDictEntries(dict);
        model.setValueAt("/CS", 2, 0);
        model.setValueAt("/RGB", 2, 1);
        Assertions.assertEquals("/CS", model.getValueAt(2, 0));
        Assertions.assertEquals("/RGB", model.getValueAt(2, 1));
        // Slash addition for key should also work here
        model.setValueAt("W", 2, 0);
        Assertions.assertEquals("/W", model.getValueAt(2, 0));
        // Button row shouldn't modify anything
        model.setValueAt("/CMYK", 2, 2);
        Assertions.assertNull(model.getValueAt(2, 2));
        // Only temp values in the model should change, not the backing dict
        Assertions.assertEquals(entriesBefore, dict.entrySet());
    }

    @Test
    void removeRow() {
        final PdfDictionary dict = createTestDict();
        final DictionaryTableModel model = new DictionaryTableModel(dict, new PdfSyntaxParser(), null);
        model.setEditable(true);
        // Deleting rows one-by-one
        model.removeRow(1);
        Assertions.assertEquals(1, dict.size());
        Assertions.assertSame(PdfName.ASCIIHexDecode, dict.get(PdfName.Filter));
        // Should no longer work, as there are not enough rows
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> model.removeRow(1));
        model.removeRow(0);
        Assertions.assertTrue(dict.isEmpty());
    }

    @Test
    void validateTempRow_BlankValue() {
        executeNoChangeExpectedTest((DictionaryTableModel model) -> {
            model.setValueAt("  ", 2, 1);
            model.validateTempRow();
        });
    }

    @Test
    void validateTempRow_InvalidKey() {
        executeNoChangeExpectedTest((DictionaryTableModel model) -> {
            model.setValueAt("[0 1 2]", 2, 0);
            model.setValueAt("/ASCII85Decode", 2, 1);
            model.validateTempRow();
        });
    }

    @Test
    void validateTempRow_InvalidValue() {
        executeNoChangeExpectedTest((DictionaryTableModel model) -> {
            model.setValueAt("/F", 2, 0);
            model.setValueAt("[0>", 2, 1);
            model.validateTempRow();
        });
    }

    @Test
    void validateTempRow_InvalidBoth() {
        executeNoChangeExpectedTest((DictionaryTableModel model) -> {
            model.setValueAt("[0 1 2]", 2, 0);
            model.setValueAt("[0>", 2, 1);
            model.validateTempRow();
        });
    }

    @Test
    void validateTempRow_DuplicateKey() {
        executeNoChangeExpectedTest((DictionaryTableModel model) -> {
            model.setValueAt("/Filter", 2, 0);
            model.setValueAt("/ASCII85Decode", 2, 1);
            model.validateTempRow();
        });
    }

    @Test
    void validateTempRow_Valid() {
        final PdfDictionary dict = createTestDict();
        final DictionaryTableModel model = new DictionaryTableModel(dict, new PdfSyntaxParser(), null);
        model.setEditable(true);
        // This should add a new key-pair and reset the temp values
        model.setValueAt("/F", 2, 0);
        model.setValueAt("/ASCII85Decode", 2, 1);
        model.validateTempRow();
        Assertions.assertEquals(3, dict.size());
        Assertions.assertSame(PdfName.ASCIIHexDecode, dict.get(PdfName.Filter));
        Assertions.assertSame(EIGHT, dict.get(PdfName.Length));
        // This won't be the same, as it might create a new object
        Assertions.assertEquals(PdfName.ASCII85Decode, dict.get(PdfName.F));
        Assertions.assertEquals("/", model.getValueAt(3, 0));
        Assertions.assertEquals("", model.getValueAt(3, 1));
    }

    @Test
    void getColumnName() {
        final PdfDictionary dict = createTestDict();
        final DictionaryTableModel model = new DictionaryTableModel(dict, new PdfSyntaxParser(), null);
        model.setEditable(true);
        Assertions.assertNull(model.getColumnName(-1));
        Assertions.assertNotNull(model.getColumnName(0));
        Assertions.assertNotNull(model.getColumnName(1));
        Assertions.assertNotEquals(model.getColumnName(0), model.getColumnName(1));
        Assertions.assertEquals("", model.getColumnName(2));
        Assertions.assertNull(model.getColumnName(3));
    }

    private static void executeNoChangeExpectedTest(Consumer<DictionaryTableModel> consumer) {
        final PdfDictionary dict = createTestDict();
        final DictionaryTableModel model = new DictionaryTableModel(dict, new PdfSyntaxParser(), null);
        model.setEditable(true);
        final Set<Entry<PdfName, PdfObject>> entriesBefore = copyDictEntries(dict);
        consumer.accept(model);
        Assertions.assertEquals(entriesBefore, dict.entrySet());
    }

    private static PdfDictionary createTestDict() {
        final PdfDictionary dict = new PdfDictionary();
        dict.put(PdfName.Length, EIGHT);
        dict.put(PdfName.Filter, PdfName.ASCIIHexDecode);
        return dict;
    }

    private static Set<Entry<PdfName, PdfObject>> copyDictEntries(PdfDictionary dict) {
        return ((PdfDictionary) dict.clone()).entrySet();
    }
}