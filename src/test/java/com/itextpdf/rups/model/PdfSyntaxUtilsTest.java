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
package com.itextpdf.rups.model;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfBoolean;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.test.ExtendedITextTest;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
class PdfSyntaxUtilsTest extends ExtendedITextTest {

    @Test
    void testSimpleString() {
        Assertions.assertEquals("(hello world)", PdfSyntaxUtils.getSyntaxString(new PdfString("hello world")));
    }

    @Test
    void testSimpleDictionary() {
        PdfDictionary pdfDictionary = new PdfDictionary();
        pdfDictionary.put(PdfName.ON, PdfBoolean.TRUE);
        Assertions.assertEquals("<< /ON true >>", PdfSyntaxUtils.getSyntaxString(pdfDictionary));
    }

    @Test
    void testSimpleArray() {
        PdfArray array = new PdfArray(Arrays.asList(new PdfNumber(1), new PdfString("hello")));
        Assertions.assertEquals("[ 1 (hello) ]", PdfSyntaxUtils.getSyntaxString(array));
    }

    @Test
    void testIndirectRef() {
        final PdfDocument doc = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
        final PdfIndirectReference ref = new PdfString("test")
                .makeIndirect(doc)
                .getIndirectReference();
        Assertions.assertEquals(ref.getObjNumber() + " 0 R", PdfSyntaxUtils.getSyntaxString(ref));
    }
}
