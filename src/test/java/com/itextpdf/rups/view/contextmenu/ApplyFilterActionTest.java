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

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.io.encoders.ASCII85CompressionStrategy;
import com.itextpdf.rups.mock.NoopProgressDialog;
import com.itextpdf.rups.model.IRupsEventListener;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfFile;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class ApplyFilterActionTest {
    private static final String TEST_PDF_PATH = "./src/test/resources/com/itextpdf/rups"
            + "/controller/hello_world.pdf";
    private static final int TEST_PDF_NON_STREAM_OBJ_ID = 1;
    private static final int TEST_PDF_STREAM_OBJ_ID = 5;

    private PdfReaderController controller;
    private PdfFile pdfFile;

    @BeforeEach
    void beforeEach() throws Exception {
        controller = new PdfReaderController(null, null);
        pdfFile = PdfFile.openAsOwner(new File(TEST_PDF_PATH));
        // Using a noop listener here to prevent threading issues
        final ObjectLoader loader = new ObjectLoader(
                new IRupsEventListener() {}, pdfFile, "Test loader", new NoopProgressDialog()
        );
        loader.execute();
        loader.get();
        controller.handleOpenDocument(loader);
    }

    @AfterEach
    void afterEach() {
        if (controller != null) {
            controller.handleCloseDocument();
            controller = null;
        }
        if (pdfFile != null) {
            final PdfDocument doc = pdfFile.getPdfDocument();
            if (doc != null) {
                doc.close();
            }
            pdfFile = null;
        }
    }

    @Test
    void actionPerformed_success() {
        final PdfObjectTreeNode node = selectTestStreamNode();
        final byte[] originalData = getStream(node).getBytes();
        final ApplyFilterAction action = new ApplyFilterAction(
                "",
                controller.getPdfTree(),
                controller,
                ASCII85CompressionStrategy::new
        );
        action.actionPerformed(null);
        // Data should get re-encoded and the dictionary should be updated
        final PdfStream stream = getStream(node);
        Assertions.assertArrayEquals(originalData, stream.getBytes());
        Assertions.assertEquals(
                new PdfNumber(stream.getBytes(false).length),
                stream.get(PdfName.Length)
        );
        Assertions.assertEquals(PdfName.ASCII85Decode, stream.get(PdfName.Filter));
        Assertions.assertFalse(stream.containsKey(PdfName.DecodeParms));
        // Since controller was passed, children should be wiped: they will be
        // recreated later via handlePdfTreeNodeClicked
        Assertions.assertEquals(0, node.getChildCount());
    }

    @Test
    void actionPerformed_withoutController() {
        final PdfObjectTreeNode node = selectTestStreamNode();
        final byte[] originalData = getStream(node).getBytes();
        final ApplyFilterAction action = new ApplyFilterAction(
                "",
                controller.getPdfTree(),
                null,
                ASCII85CompressionStrategy::new
        );
        action.actionPerformed(null);
        // Data should get re-encoded and the dictionary should be updated
        final PdfStream stream = getStream(node);
        Assertions.assertArrayEquals(originalData, stream.getBytes());
        Assertions.assertEquals(
                new PdfNumber(stream.getBytes(false).length),
                stream.get(PdfName.Length)
        );
        Assertions.assertEquals(PdfName.ASCII85Decode, stream.get(PdfName.Filter));
        Assertions.assertFalse(stream.containsKey(PdfName.DecodeParms));
        // The length child should remain, as action has no access to the
        // controller to wipe them
        Assertions.assertEquals(1, node.getChildCount());
    }

    @Test
    void actionPerformed_failure() {
        final PdfObjectTreeNode node = selectTestStreamNode();
        final byte[] originalData = getStream(node).getBytes();
        final ApplyFilterAction action = new ApplyFilterAction(
                "",
                controller.getPdfTree(),
                controller,
                () -> null
        );
        action.actionPerformed(null);
        // Data should remain as-is
        final PdfStream stream = getStream(node);
        Assertions.assertArrayEquals(originalData, stream.getBytes(true));
        Assertions.assertArrayEquals(originalData, stream.getBytes(false));
        Assertions.assertEquals(new PdfNumber(originalData.length), stream.get(PdfName.Length));
        Assertions.assertFalse(stream.containsKey(PdfName.Filter));
        Assertions.assertFalse(stream.containsKey(PdfName.DecodeParms));
        // The length child should remain, as the tree wasn't rebuilt because
        // of the failure
        Assertions.assertEquals(1, node.getChildCount());
    }

    @Test
    void actionPerformed_noSelection() {
        controller.getPdfTree().clearSelection();
        final ApplyFilterAction action = new ApplyFilterAction(
                "",
                controller.getPdfTree(),
                controller,
                () -> {
                    Assertions.fail("Strategy should not have been called");
                    return null;
                }
        );
        action.actionPerformed(null);
    }

    @Test
    void actionPerformed_nonStreamSelected() {
        controller.selectNode(TEST_PDF_NON_STREAM_OBJ_ID);
        final ApplyFilterAction action = new ApplyFilterAction(
                "",
                controller.getPdfTree(),
                controller,
                () -> {
                    Assertions.fail("Strategy should not have been called");
                    return null;
                }
        );
        action.actionPerformed(null);
    }

    private PdfObjectTreeNode selectTestStreamNode() {
        controller.selectNode(TEST_PDF_STREAM_OBJ_ID);
        final PdfObjectTreeNode node = (PdfObjectTreeNode) controller.getPdfTree()
                .getLastSelectedPathComponent();
        Assertions.assertNotNull(node);
        Assertions.assertTrue(node.isStream());
        // This should create the /Length child
        controller.handlePdfTreeNodeClicked(node);
        Assertions.assertEquals(1, node.getChildCount());
        return node;
    }

    private static PdfStream getStream(PdfObjectTreeNode node) {
        return (PdfStream) node.getPdfObject();
    }
}
