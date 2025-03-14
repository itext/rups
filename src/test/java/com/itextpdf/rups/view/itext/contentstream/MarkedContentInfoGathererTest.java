/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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
package com.itextpdf.rups.view.itext.contentstream;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.tagging.StandardRoles;
import com.itextpdf.kernel.pdf.tagutils.TagTreePointer;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.test.ExtendedITextTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Tag("IntegrationTest")
public class MarkedContentInfoGathererTest extends ExtendedITextTest {
    @Test
    public void indexOnePageOneContentStreamMcidCountTest() throws IOException {
        byte[] pdf = onePageOneContentStream();
        MarkedContentInfoGatherer gatherer = new MarkedContentInfoGatherer();
        try (PdfReader r = new PdfReader(new ByteArrayInputStream(pdf));
             PdfDocument pdfDoc = new PdfDocument(r)) {
            gatherer.processPageContent(pdfDoc.getFirstPage());
        }
        Assertions.assertEquals(2, gatherer.getMarkedContentIndex().size());
    }

    @Test
    public void indexTwoPageOneContentStreamMcidCountTest() throws IOException {
        byte[] pdf = twoPageDoc();
        MarkedContentInfoGatherer gatherer = new MarkedContentInfoGatherer();
        try (PdfReader r = new PdfReader(new ByteArrayInputStream(pdf));
             PdfDocument pdfDoc = new PdfDocument(r)) {
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                gatherer.processPageContent(pdfDoc.getPage(i));
                Assertions.assertEquals(2, gatherer.getMarkedContentIndex().size());
                gatherer.reset();
            }
        }
    }

    @Test
    public void indexOnePageOneContentStreamMcidMatchTest() throws IOException {
        byte[] pdf = onePageOneContentStream();
        MarkedContentInfoGatherer gatherer = new MarkedContentInfoGatherer();
        try (PdfReader r = new PdfReader(new ByteArrayInputStream(pdf));
             PdfDocument pdfDoc = new PdfDocument(r)) {
            gatherer.processPageContent(pdfDoc.getFirstPage());
        }
        for (Map.Entry<Integer, MarkedContentInfo> e : gatherer.getMarkedContentIndex().entrySet()) {
            Assertions.assertEquals((int) e.getKey(), e.getValue().getMcid());
        }
    }

    @Test
    public void indexOnePageOneContentStreamIdentifyPageTest() throws IOException {
        byte[] pdf = onePageOneContentStream();
        MarkedContentInfoGatherer gatherer = new MarkedContentInfoGatherer();
        try (PdfReader r = new PdfReader(new ByteArrayInputStream(pdf));
             PdfDocument pdfDoc = new PdfDocument(r)) {
            gatherer.processPageContent(pdfDoc.getFirstPage());
            PdfIndirectReference contentStreamRef = pdfDoc.getFirstPage()
                    .getFirstContentStream()
                    .getIndirectReference();
            for (MarkedContentInfo i : gatherer.getMarkedContentIndex().values()) {
                Assertions.assertEquals(contentStreamRef, i.getStreamRef());
            }
        }
    }

    @Test
    public void indexOnePageTwoContentStreamsIdentifyPageTest() throws IOException {
        byte[] pdf = onePageTwoContentStreams();
        MarkedContentInfoGatherer gatherer = new MarkedContentInfoGatherer();
        try (PdfReader r = new PdfReader(new ByteArrayInputStream(pdf));
             PdfDocument pdfDoc = new PdfDocument(r)) {
            gatherer.processPageContent(pdfDoc.getFirstPage());
            PdfPage pg = pdfDoc.getFirstPage();
            PdfIndirectReference stream1 = pg.getFirstContentStream().getIndirectReference();
            PdfIndirectReference stream2 = pg.getLastContentStream().getIndirectReference();
            PdfIndirectReference[] expectedRefs = {stream1, stream1, stream2, stream2};
            PdfIndirectReference[] actualRefs = new PdfIndirectReference[4];
            for (MarkedContentInfo i : gatherer.getMarkedContentIndex().values()) {
                actualRefs[i.getMcid()] = i.getStreamRef();
            }
            Assertions.assertArrayEquals(expectedRefs, actualRefs);
        }
    }

    @Test
    public void extractMcTextContentTest() throws IOException {
        byte[] pdf = onePageOneContentStream();
        MarkedContentInfoGatherer gatherer = new MarkedContentInfoGatherer();
        try (PdfReader r = new PdfReader(new ByteArrayInputStream(pdf));
             PdfDocument pdfDoc = new PdfDocument(r)) {
            gatherer.processPageContent(pdfDoc.getFirstPage());
        }
        final Map<Integer, MarkedContentInfo> index = gatherer.getMarkedContentIndex();
        Assertions.assertEquals(2, index.size());
        Assertions.assertEquals("Hello ", index.get(0).getExtractedText());
        Assertions.assertEquals("World", index.get(1).getExtractedText());
    }

    @Test
    public void extractMcTextContentTest2() throws IOException {
        byte[] pdf = onePageTwoContentStreams();
        MarkedContentInfoGatherer gatherer = new MarkedContentInfoGatherer();
        try (PdfReader r = new PdfReader(new ByteArrayInputStream(pdf));
             PdfDocument pdfDoc = new PdfDocument(r)) {
            gatherer.processPageContent(pdfDoc.getFirstPage());
        }
        final Map<Integer, MarkedContentInfo> index = gatherer.getMarkedContentIndex();
        Assertions.assertEquals(4, index.size());
        Assertions.assertEquals("Hello ", index.get(0).getExtractedText());
        Assertions.assertEquals("World", index.get(1).getExtractedText());
        Assertions.assertEquals("iText ", index.get(2).getExtractedText());
        Assertions.assertEquals("RUPS", index.get(3).getExtractedText());
    }

    @Test
    public void extractMcTextContentTest3() throws IOException {
        byte[] pdf = onePageWithXObj();
        MarkedContentInfoGatherer gatherer = new MarkedContentInfoGatherer();
        try (PdfReader r = new PdfReader(new ByteArrayInputStream(pdf));
                PdfDocument pdfDoc = new PdfDocument(r)) {
            gatherer.processPageContent(pdfDoc.getFirstPage());
        }
        final Map<Integer, MarkedContentInfo> index = gatherer.getMarkedContentIndex();
        Assertions.assertEquals(4, index.size());
        Assertions.assertEquals("Hello ", index.get(0).getExtractedText());
        Assertions.assertEquals("World", index.get(1).getExtractedText());
        Assertions.assertEquals("iText ", index.get(2).getExtractedText());
        Assertions.assertEquals("RUPS", index.get(3).getExtractedText());
    }

    @Test
    public void indexTwoPageDocIdentifyPageTest() throws IOException {
        byte[] pdf = twoPageDoc();
        MarkedContentInfoGatherer gatherer = new MarkedContentInfoGatherer();
        try (PdfReader r = new PdfReader(new ByteArrayInputStream(pdf));
             PdfDocument pdfDoc = new PdfDocument(r)) {
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                PdfPage pg = pdfDoc.getPage(i);
                gatherer.processPageContent(pg);
                PdfIndirectReference contentStreamRef = pg
                        .getFirstContentStream()
                        .getIndirectReference();
                for (Map.Entry<Integer, MarkedContentInfo> e : gatherer.getMarkedContentIndex().entrySet()) {
                    Assertions.assertEquals(contentStreamRef, e.getValue().getStreamRef());
                }
                gatherer.reset();
            }
        }
    }

    @Test
    public void indexTwoPageDocIdentifyPageTestMultipleStreams() throws IOException {
        byte[] pdf = twoPagesTwoContentStreams();
        MarkedContentInfoGatherer gatherer = new MarkedContentInfoGatherer();
        try (PdfReader r = new PdfReader(new ByteArrayInputStream(pdf));
             PdfDocument pdfDoc = new PdfDocument(r)) {
            gatherer.processPageContent(pdfDoc.getFirstPage());

            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                PdfPage pg = pdfDoc.getPage(i);
                gatherer.processPageContent(pg);
                PdfIndirectReference stream1 = pg.getFirstContentStream().getIndirectReference();
                PdfIndirectReference stream2 = pg.getLastContentStream().getIndirectReference();
                PdfIndirectReference[] expectedRefs = {stream1, stream1, stream2, stream2};
                PdfIndirectReference[] actualRefs = new PdfIndirectReference[4];
                for (MarkedContentInfo info : gatherer.getMarkedContentIndex().values()) {
                    actualRefs[info.getMcid()] = info.getStreamRef();
                }
                Assertions.assertArrayEquals(expectedRefs, actualRefs);
                gatherer.reset();
            }
        }
    }

    private static byte[] onePageOneContentStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter w = new PdfWriter(baos); PdfDocument pdfDocument = new PdfDocument(w)) {
            pdfDocument.setTagged();
            addPageWithOneContentStream(pdfDocument, "Hello ", "World");
        }
        return baos.toByteArray();
    }

    @Test
    public void indexPageWithFormXObjs() throws IOException {
        byte[] pdf = onePageWithXObj();
        MarkedContentInfoGatherer gatherer = new MarkedContentInfoGatherer();
        try (PdfReader r = new PdfReader(new ByteArrayInputStream(pdf));
                PdfDocument pdfDoc = new PdfDocument(r)) {
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                PdfPage pg = pdfDoc.getPage(i);
                gatherer.processPageContent(pg);
                PdfIndirectReference contentStreamRef = pg
                        .getFirstContentStream()
                        .getIndirectReference();
                PdfIndirectReference xobjRef = pg.getResources()
                        .getForm(new PdfName("Fm1"))
                        .getPdfObject()
                        .getIndirectReference();
                PdfIndirectReference[] expectedRefs = {contentStreamRef, contentStreamRef, xobjRef, xobjRef};
                PdfIndirectReference[] actualRefs = new PdfIndirectReference[4];
                for (MarkedContentInfo info : gatherer.getMarkedContentIndex().values()) {
                    actualRefs[info.getMcid()] = info.getStreamRef();
                }
                Assertions.assertArrayEquals(expectedRefs, actualRefs);
                gatherer.reset();
            }
        }
    }

    private static byte[] onePageWithXObj() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter w = new PdfWriter(baos); PdfDocument pdfDocument = new PdfDocument(w)) {
            pdfDocument.setTagged();
            addPageWithFormXObject(pdfDocument);
        }
        return baos.toByteArray();
    }

    private static byte[] onePageTwoContentStreams() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter w = new PdfWriter(baos); PdfDocument pdfDocument = new PdfDocument(w)) {
            pdfDocument.setTagged();
            addPageWithTwoContentStreams(pdfDocument, "Hello ", "World", "iText ", "RUPS");
        }
        return baos.toByteArray();
    }

    private static byte[] twoPagesTwoContentStreams() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter w = new PdfWriter(baos); PdfDocument pdfDocument = new PdfDocument(w)) {
            pdfDocument.setTagged();
            addPageWithTwoContentStreams(pdfDocument, "Hello ", "World", "iText ", "RUPS");
            addPageWithTwoContentStreams(pdfDocument, "Hello2 ", "World2", "iText2 ", "RUPS2");
        }
        return baos.toByteArray();
    }

    private static byte[] twoPageDoc() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter w = new PdfWriter(baos); PdfDocument pdfDocument = new PdfDocument(w)) {
            pdfDocument.setTagged();
            addPageWithOneContentStream(pdfDocument, "Hello ", "World");
            addPageWithOneContentStream(pdfDocument, "Hello2 ", "World2");
        }
        return baos.toByteArray();
    }

    private static void addPageWithOneContentStream(PdfDocument pdfDocument, String str1, String str2)
            throws IOException {

        PdfPage pg = pdfDocument.addNewPage();
        PdfCanvas canvas = new PdfCanvas(pg);
        PdfFont courier = PdfFontFactory.createFont(StandardFonts.COURIER);
        TagTreePointer ptr = pdfDocument.getTagStructureContext().getAutoTaggingPointer()
                .setPageForTagging(pg)
                .addTag(StandardRoles.P)
                .addTag(StandardRoles.SPAN);
        canvas.concatMatrix(1, 0, 0, 1, 20, 700)
                .openTag(ptr.getTagReference())
                .beginText()
                .setFontAndSize(courier, 12)
                .showText(str1)
                .endText()
                .closeTag()
                .concatMatrix(1, 0, 0, 1, 50, 0)
                .openTag(ptr.moveToParent().addTag(StandardRoles.SPAN).getTagReference())
                .beginText()
                .setFontAndSize(courier, 12)
                .showText(str2)
                .endText()
                .closeTag();
        ptr.moveToRoot();
        canvas.concatMatrix(1, 0, 0, 1, 50, 0)
                .beginText().setFontAndSize(courier, 12).showText("Unmarked").endText();
    }

    private static void addPageWithTwoContentStreams(PdfDocument pdfDocument, String str1, String str2,
                                                     String str3, String str4)
            throws IOException {

        PdfFont courier = PdfFontFactory.createFont(StandardFonts.COURIER);
        PdfPage pg = pdfDocument.addNewPage();
        PdfCanvas canvas = new PdfCanvas(pg);
        TagTreePointer ptr = pdfDocument.getTagStructureContext().getAutoTaggingPointer()
                .setPageForTagging(pg)
                .addTag(StandardRoles.P)
                .addTag(StandardRoles.SPAN);
        canvas.concatMatrix(1, 0, 0, 1, 20, 700)
                .openTag(ptr.getTagReference())
                .beginText()
                .setFontAndSize(courier, 12)
                .showText(str1)
                .endText()
                .closeTag()
                .concatMatrix(1, 0, 0, 1, 50, 0)
                .openTag(ptr.moveToParent().addTag(StandardRoles.SPAN).getTagReference())
                .beginText()
                .setFontAndSize(courier, 12)
                .showText(str2)
                .endText()
                .closeTag();

        canvas = new PdfCanvas(pg.newContentStreamAfter(), pg.getResources(), pdfDocument);
        canvas.concatMatrix(1, 0, 0, 1, 20, 500)
                .openTag(ptr.moveToParent().addTag(StandardRoles.SPAN).getTagReference())
                .beginText()
                .setFontAndSize(courier, 12)
                .showText(str3)
                .endText()
                .closeTag()
                .concatMatrix(1, 0, 0, 1, 50, 0)
                .openTag(ptr.moveToParent().addTag(StandardRoles.SPAN).getTagReference())
                .beginText()
                .setFontAndSize(courier, 12)
                .showText(str4)
                .endText()
                .closeTag();
        ptr.moveToRoot();
    }

    private static void addPageWithFormXObject(PdfDocument pdfDocument)
            throws IOException {

        PdfFont courier = PdfFontFactory.createFont(StandardFonts.COURIER);
        PdfPage pg = pdfDocument.addNewPage();

        PdfFormXObject xObj = new PdfFormXObject(new Rectangle(100, 100));
        PdfCanvas xObjCanvas = new PdfCanvas(xObj.getPdfObject(), pg.getResources(), pdfDocument);
        TagTreePointer ptr = pdfDocument.getTagStructureContext().getAutoTaggingPointer()
                .setPageForTagging(pg)
                .addTag(StandardRoles.P)
                .addTag(StandardRoles.SPAN);
        PdfCanvas canvas = new PdfCanvas(pg);
        canvas.saveState()
                .openTag(ptr.getTagReference())
                .concatMatrix(1, 0, 0, 1, 20, 700)
                .beginText()
                .setFontAndSize(courier, 12)
                .showText("Hello ")
                .endText()
                .closeTag()
                .concatMatrix(1, 0, 0, 1, 50, 0)
                .openTag(ptr.moveToParent().addTag(StandardRoles.SPAN).getTagReference())
                .beginText()
                .setFontAndSize(courier, 12)
                .showText("World")
                .endText()
                .closeTag()
                .restoreState();
        xObjCanvas.openTag(ptr.moveToParent().addTag(StandardRoles.SPAN).getTagReference())
                .beginText()
                .setFontAndSize(courier, 12)
                .showText("iText ")
                .endText()
                .closeTag()
                .concatMatrix(1, 0, 0, 1, 50, 0)
                .openTag(ptr.moveToParent().addTag(StandardRoles.SPAN).getTagReference())
                .beginText()
                .setFontAndSize(courier, 12)
                .showText("RUPS")
                .endText()
                .closeTag();
        canvas.addXObjectAt(xObj, 20, 500);
        ptr.moveToRoot();
    }
}
