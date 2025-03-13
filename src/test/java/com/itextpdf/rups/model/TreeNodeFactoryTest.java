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
package com.itextpdf.rups.model;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1SequenceTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import javax.swing.tree.TreeNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class TreeNodeFactoryTest {
    private static final PdfName SV = new PdfName("SV");
    private static final PdfName SVCert = new PdfName("SVCert");

    @Test
    void expandNode_Asn1SigDictionaryWithValidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. "/Type: /Sig" in the signature dictionary (which is optional).
         * 3. "/Contents: 0x3000" in the signature dictionary (empty ASN.1
         *    SEQUENCE)
         *
         * As a result there SHOULD be an ASN.1 subtree.
         */
        doSigTest(PdfName.Sig, PdfName.Sig, new PdfString(new byte[] {0x30, 0x00}), "contentInfo");
    }

    @Test
    void expandNode_Asn1SigDictionaryWithInvalidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. "/Type: /Sig" in the signature dictionary (which is optional).
         * 3. "/Contents: 0x0100" in the signature dictionary (ASN.1 BOOLEAN
         *    without data, which is invalid)
         *
         * As a result there SHOULD NOT be an ASN.1 subtree, as the ASN.1
         * object is invalid.
         */
        doSigTest(PdfName.Sig, PdfName.Sig, new PdfString(new byte[] {0x01, 0x00}), null);
    }

    @Test
    void expandNode_Asn1SigDictionaryWithTimeStampType() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. "/Type: /DocTimeStamp" in the signature dictionary.
         * 3. "/Contents: 0x3000" in the signature dictionary (empty ASN.1
         *    SEQUENCE)
         *
         * As a result there SHOULD be an ASN.1 subtree.
         */
        doSigTest(PdfName.Sig, PdfName.DocTimeStamp, new PdfString(new byte[] {0x30, 0x00}), "contentInfo");
    }

    @Test
    void expandNode_Asn1SigDictionaryWithInvalidTypeType() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. "/Type: /Tx" in the signature dictionary, which is incorrect.
         * 3. "/Contents: 0x3000" in the signature dictionary (empty ASN.1
         *    SEQUENCE)
         *
         * As a result there SHOULD NOT be an ASN.1 subtree, as dictionary
         * has an unexpected type.
         */
        doSigTest(PdfName.Sig, PdfName.Tx, new PdfString(new byte[] {0x30, 0x00}), null);
    }

    @Test
    void expandNode_Asn1SigDictionaryWithNonStringContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. "/Type: /Sig" in the signature dictionary.
         * 3. "/Contents: 1" in the signature dictionary, which is a number.
         *
         * As a result there SHOULD NOT be an ASN.1 subtree, as contents
         * should be a string.
         */
        doSigTest(PdfName.Sig, PdfName.Sig, new PdfNumber(1), null);
    }

    @Test
    void expandNode_Asn1SigDictionaryWithoutDictType() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. "/Type" key in the signature dictionary is missing.
         * 3. "/Contents: 0x3000" in the signature dictionary (empty ASN.1
         *    SEQUENCE)
         *
         * As a result there SHOULD be an ASN.1 subtree.
         */
        doSigTest(PdfName.Sig, null, new PdfString(new byte[] {0x30, 0x00}), "contentInfo");
    }

    @Test
    void expandNode_Asn1SigDictionaryWithInvalidFormFieldType() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Tx" in the form field.
         * 2. "/Type" key in the signature dictionary is missing.
         * 3. "/Contents: 0x3000" in the signature dictionary (empty ASN.1
         *    SEQUENCE)
         *
         * As a result there SHOULD NOT be an ASN.1 subtree, as it is not a
         * signature form field.
         */
        doSigTest(PdfName.Tx, null, new PdfString(new byte[] {0x30, 0x00}), null);
    }

    @Test
    void expandNode_Asn1VriCrlStreamWithValidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/VRI" dictionary with a "/CRL" key, which is
         *    an array with a single stream.
         * 2. "0x3000" as stream data (empty ASN.1 SEQUENCE)
         *
         * As a result there SHOULD be an ASN.1 subtree.
         */
        doVriTest(PdfName.CRL, new byte[] {0x30, 0x00}, "crl");
    }

    @Test
    void expandNode_Asn1VriCrlStreamWithInvalidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/VRI" dictionary with a "/CRL" key, which is
         *    an array with a single stream.
         * 2. "0x0100" as stream data (ASN.1 BOOLEAN without data, which is
         *    invalid).
         *
         * As a result there SHOULD NOT be an ASN.1 subtree.
         */
        doVriTest(PdfName.CRL, new byte[] {0x01, 0x00}, null);
    }

    @Test
    void expandNode_Asn1VriCertStreamWithValidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/VRI" dictionary with a "/Cert" key, which is
         *    an array with a single stream.
         * 2. "0x3000" as stream data (empty ASN.1 SEQUENCE)
         *
         * As a result there SHOULD be an ASN.1 subtree.
         */
        doVriTest(PdfName.Cert, new byte[] {0x30, 0x00}, "certificate");
    }

    @Test
    void expandNode_Asn1VriCertStreamWithInvalidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/VRI" dictionary with a "/Cert" key, which is
         *    an array with a single stream.
         * 2. "0x0100" as stream data (ASN.1 BOOLEAN without data, which is
         *    invalid).
         *
         * As a result there SHOULD NOT be an ASN.1 subtree.
         */
        doVriTest(PdfName.Cert, new byte[] {0x01, 0x00}, null);
    }

    @Test
    void expandNode_Asn1VriOcspStreamWithValidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/VRI" dictionary with a "/OCSP" key, which is
         *    an array with a single stream.
         * 2. "0x3000" as stream data (empty ASN.1 SEQUENCE)
         *
         * As a result there SHOULD be an ASN.1 subtree.
         */
        doVriTest(PdfName.OCSP, new byte[] {0x30, 0x00}, "ocspResponse");
    }

    @Test
    void expandNode_Asn1VriOcspStreamWithInvalidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/VRI" dictionary with a "/OCSP" key, which is
         *    an array with a single stream.
         * 2. "0x0100" as stream data (ASN.1 BOOLEAN without data, which is
         *    invalid).
         *
         * As a result there SHOULD NOT be an ASN.1 subtree.
         */
        doVriTest(PdfName.OCSP, new byte[] {0x01, 0x00}, null);
    }

    @Test
    void expandNode_Asn1VriTsStreamWithValidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/VRI" dictionary with a "/TS" key, which is
         *    a single stream.
         * 2. "0x3000" as stream data (empty ASN.1 SEQUENCE)
         *
         * As a result there SHOULD be an ASN.1 subtree.
         */
        doTsTest(new byte[] {0x30, 0x00}, "contentInfo");
    }

    @Test
    void expandNode_Asn1VriTsStreamWithInvalidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/VRI" dictionary with a "/TS" key, which is
         *    a single stream.
         * 2. "0x0100" as stream data (ASN.1 BOOLEAN without data, which is
         *    invalid).
         *
         * As a result there SHOULD NOT be an ASN.1 subtree.
         */
        doTsTest(new byte[] {0x01, 0x00}, null);
    }

    @Test
    void expandNode_Asn1DssCrlsStreamWithValidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/DSS" dictionary with a "/CRLs" key, which points to
         *    an array with a single stream.
         * 2. "0x3000" as stream data (empty ASN.1 SEQUENCE)
         *
         * As a result there SHOULD be an ASN.1 subtree.
         */
        doDssTest(PdfName.CRLs, new byte[] {0x30, 0x00}, "crl");
    }

    @Test
    void expandNode_Asn1DssCrlsStreamWithInvalidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/DSS" dictionary with a "/CRLs" key, which points to
         *    an array with a single stream.
         * 2. "0x0100" as stream data (ASN.1 BOOLEAN without data, which is
         *    invalid).
         *
         * As a result there SHOULD NOT be an ASN.1 subtree.
         */
        doDssTest(PdfName.CRLs, new byte[] {0x01, 0x00}, null);
    }

    @Test
    void expandNode_Asn1DssCertsStreamWithValidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/DSS" dictionary with a "/Certs" key, which points to
         *    an array with a single stream.
         * 2. "0x3000" as stream data (empty ASN.1 SEQUENCE)
         *
         * As a result there SHOULD be an ASN.1 subtree.
         */
        doDssTest(PdfName.Certs, new byte[] {0x30, 0x00}, "certificate");
    }

    @Test
    void expandNode_Asn1DssCertsStreamWithInvalidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/DSS" dictionary with a "/Certs" key, which points to
         *    an array with a single stream.
         * 2. "0x0100" as stream data (ASN.1 BOOLEAN without data, which is
         *    invalid).
         *
         * As a result there SHOULD NOT be an ASN.1 subtree.
         */
        doDssTest(PdfName.Certs, new byte[] {0x01, 0x00}, null);
    }

    @Test
    void expandNode_Asn1DssOcspsStreamWithValidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/DSS" dictionary with a "/Certs" key, which points to
         *    an array with a single stream.
         * 2. "0x3000" as stream data (empty ASN.1 SEQUENCE)
         *
         * As a result there SHOULD be an ASN.1 subtree.
         */
        doDssTest(PdfName.OCSPs, new byte[] {0x30, 0x00}, "ocspResponse");
    }

    @Test
    void expandNode_Asn1DssOcspsStreamWithInvalidContents() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. There is a "/DSS" dictionary with a "/Certs" key, which points to
         *    an array with a single stream.
         * 2. "0x0100" as stream data (ASN.1 BOOLEAN without data, which is
         *    invalid).
         *
         * As a result there SHOULD NOT be an ASN.1 subtree.
         */
        doDssTest(PdfName.OCSPs, new byte[] {0x01, 0x00}, null);
    }

    @Test
    void expandNode_Asn1SVCertDictionaryWithValidData() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. Seed value dictionary is stored under "/SV" key.
         * 3. "/Type: /SV" in the seed value dictionary (which is optional).
         * 4. "/Type: /SVCert" in the certificate seed value dictionary (which
         *    is optional).
         * 5. String data is "0x3000" (empty ASN.1 SEQUENCE)
         *
         * As a result there SHOULD be an ASN.1 subtree.
         *
         * Checking both keys at the same time.
         */
        doSVCertTest(PdfName.Sig, SV, SVCert, new PdfString(new byte[] {0x30, 0x00}), "certificate");
    }

    @Test
    void expandNode_Asn1SVCertWithInvalidData() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. Seed value dictionary is stored under "/SV" key.
         * 3. "/Type: /SV" in the seed value dictionary (which is optional).
         * 4. "/Type: /SVCert" in the certificate seed value dictionary (which
         *    is optional).
         * 5. String data is "0x0100" (ASN.1 BOOLEAN without data, which is
         *    invalid).
         *
         * As a result there SHOULD NOT be an ASN.1 subtree, as the ASN.1
         * object is invalid.
         *
         * Checking both keys at the same time.
         */
        doSVCertTest(PdfName.Sig, SV, SVCert, new PdfString(new byte[] {0x01, 0x00}), null);
    }

    @Test
    void expandNode_Asn1SVCertWithInvalidTypeType() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. Seed value dictionary is stored under "/SV" key.
         * 3. "/Type: /SV" in the seed value dictionary (which is optional).
         * 4. "/Type: /Tx" in the certificate seed value dictionary, which is
         *    incorrect.
         * 5. String data is "0x3000" (empty ASN.1 SEQUENCE)
         *
         * As a result there SHOULD NOT be an ASN.1 subtree.
         *
         * Checking both keys at the same time.
         */
        doSVCertTest(PdfName.Sig, SV, PdfName.Tx, new PdfString(new byte[] {0x30, 0x00}), null);
    }

    @Test
    void expandNode_Asn1SVCertWithNonStringData() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. Seed value dictionary is stored under "/SV" key.
         * 3. "/Type: /SV" in the seed value dictionary (which is optional).
         * 4. "/Type: /SVCert" in the certificate seed value dictionary (which
         *    is optional).
         * 5. Data is a number (1), which is incorrect.
         *
         * As a result there SHOULD NOT be an ASN.1 subtree.
         *
         * Checking both keys at the same time.
         */
        doSVCertTest(PdfName.Sig, SV, SVCert, new PdfNumber(1), null);
    }

    @Test
    void expandNode_Asn1SVCertWithoutSVCertType() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. Seed value dictionary is stored under "/SV" key.
         * 3. "/Type: /SV" in the seed value dictionary (which is optional).
         * 4. "/Type" key in the certificate seed value dictionary is missing.
         * 5. String data is "0x3000" (empty ASN.1 SEQUENCE)
         *
         * As a result there SHOULD be an ASN.1 subtree.
         *
         * Checking both keys at the same time.
         */
        doSVCertTest(PdfName.Sig, SV, null, new PdfString(new byte[] {0x30, 0x00}), "certificate");
    }

    @Test
    void expandNode_Asn1SVCertWithoutSVType() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. Seed value dictionary is stored under "/SV" key.
         * 3. "/Type" in the seed value dictionary is missing.
         * 4. "/Type" key in the certificate seed value dictionary is missing.
         * 5. String data is "0x3000" (empty ASN.1 SEQUENCE).
         *
         * As a result there SHOULD be an ASN.1 subtree.
         *
         * Checking both keys at the same time.
         */
        doSVCertTest(PdfName.Sig, null, null, new PdfString(new byte[] {0x30, 0x00}), "certificate");
    }

    @Test
    void expandNode_Asn1SVCertWithInvalidSVType() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Sig" in the form field.
         * 2. Seed value dictionary is stored under "/SV" key.
         * 3. "/Type: /Tx" in the seed value dictionary, which is incorrect.
         * 4. "/Type" key in the certificate seed value dictionary is missing.
         * 5. String data is "0x3000" (empty ASN.1 SEQUENCE).
         *
         * As a result there SHOULD NOT be an ASN.1 subtree.
         *
         * Checking both keys at the same time.
         */
        doSVCertTest(PdfName.Sig, PdfName.Tx, null, new PdfString(new byte[] {0x30, 0x00}), null);
    }

    @Test
    void expandNode_Asn1SVCertWithInvalidFormFieldType() {
        /*
         * Checking on a test document, where the following markers are
         * present:
         *
         * 1. "/FT: /Tx" in the form field, which is incorrect.
         * 2. Seed value dictionary is stored under "/SV" key.
         * 3. "/Type" key in the seed value dictionary is missing.
         * 4. "/Type" key in the certificate seed value dictionary is missing.
         * 5. String data is "0x3000" (empty ASN.1 SEQUENCE).
         *
         * As a result there SHOULD NOT be an ASN.1 subtree.
         *
         * Checking both keys at the same time.
         */
        doSVCertTest(PdfName.Tx, null, null, new PdfString(new byte[] {0x30, 0x00}), null);
    }

    @Test
    void addNewIndirectObject() {
        final PdfDocument doc = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
        final TreeNodeFactory factory = new TreeNodeFactory(createIndirectObjectFactory(doc));
        final PdfString object = new PdfString("TEST");
        factory.addNewIndirectObject(object);
        final PdfIndirectReference ref = object.getIndirectReference();
        Assertions.assertNotNull(ref);
        Assertions.assertSame(doc, ref.getDocument());
        final PdfObjectTreeNode node = factory.getNode(ref.getObjNumber());
        Assertions.assertNotNull(node);
        Assertions.assertSame(object, node.getPdfObject());
    }

    private static void doSigTest(PdfName ft, PdfName type, PdfObject contents, String expectedLeafNodeName) {
        final PdfDocument doc = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
        final PdfDictionary sigDict = createSigDict(doc, type, contents);
        final PdfDictionary sigFormField = createSigFormField(doc, ft, sigDict);
        final PdfDictionary acroFormDict = createAcroFormDict(doc, sigFormField);
        doc.getCatalog().getPdfObject().put(PdfName.AcroForm, acroFormDict);

        final TreeNodeFactory factory = new TreeNodeFactory(createIndirectObjectFactory(doc));
        PdfObjectTreeNode node = PdfObjectTreeNode.getInstance(sigFormField);
        Assertions.assertNotNull(node);
        expandAll(factory, node);

        node = node.getDictionaryChildNode(PdfName.V);
        Assertions.assertNotNull(node);
        Assertions.assertTrue(node.isIndirectReference());
        Assertions.assertEquals(1, node.getChildCount());
        node = (PdfObjectTreeNode) node.getChildAt(0);
        Assertions.assertNotNull(node);
        node = node.getDictionaryChildNode(PdfName.Contents);
        Assertions.assertNotNull(node);
        if (expectedLeafNodeName == null) {
            Assertions.assertEquals(0, node.getChildCount());
        } else {
            Assertions.assertEquals(1, node.getChildCount());
            Assertions.assertInstanceOf(Asn1SequenceTreeNode.class, node.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, expectedLeafNodeName, (AbstractAsn1TreeNode) node.getChildAt(0));
        }
    }

    private static void doDssTest(PdfName key, byte[] data, String expectedLeafNodeName) {
        final PdfDocument doc = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
        final PdfStream asn1Stream = makeIndirect(doc, new PdfStream(data));
        final PdfDictionary dss = makeIndirect(doc, new PdfDictionary(Map.of(
                key, makeIndirectRef(doc, new PdfArray(asn1Stream.getIndirectReference()))
        )));
        doc.getCatalog().getPdfObject().put(PdfName.DSS, dss);

        final TreeNodeFactory factory = new TreeNodeFactory(createIndirectObjectFactory(doc));
        PdfObjectTreeNode node = PdfObjectTreeNode.getInstance(dss);
        Assertions.assertNotNull(node);
        expandAll(factory, node);

        // Getting stream array reference node
        node = node.getDictionaryChildNode(key);
        Assertions.assertNotNull(node);
        Assertions.assertTrue(node.isIndirectReference());
        Assertions.assertEquals(1, node.getChildCount());
        // Getting stream array node
        node = (PdfObjectTreeNode) node.getChildAt(0);
        Assertions.assertTrue(node.isArray());
        Assertions.assertEquals(1, node.getChildCount());
        // Getting stream reference node
        node = (PdfObjectTreeNode) node.getChildAt(0);
        Assertions.assertTrue(node.isIndirectReference());
        Assertions.assertEquals(1, node.getChildCount());
        // Getting stream node
        node = (PdfObjectTreeNode) node.getChildAt(0);
        Assertions.assertTrue(node.isStream());
        if (expectedLeafNodeName == null) {
            Assertions.assertEquals(0, node.getChildCount());
        } else {
            Assertions.assertEquals(1, node.getChildCount());
            Assertions.assertInstanceOf(Asn1SequenceTreeNode.class, node.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, expectedLeafNodeName, (AbstractAsn1TreeNode) node.getChildAt(0));
        }
    }

    private static void doVriTest(PdfName key, byte[] data, String expectedLeafNodeName) {
        final PdfDocument doc = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
        final PdfStream asn1Stream = makeIndirect(doc, new PdfStream(data));
        final PdfDictionary vriLeaf = makeIndirect(doc, new PdfDictionary(Map.of(
                key, new PdfArray(asn1Stream.getIndirectReference()),
                PdfName.Type, PdfName.VRI
        )));
        final PdfDictionary vri = makeIndirect(doc, new PdfDictionary(Map.of(
                new PdfName("112233445566778899AABBCCDDEEFF1122334455"), vriLeaf.getIndirectReference()
        )));
        final PdfDictionary dss = makeIndirect(doc, new PdfDictionary(Map.of(
                PdfName.VRI, vri.getIndirectReference()
        )));
        doc.getCatalog().getPdfObject().put(PdfName.DSS, dss);

        final TreeNodeFactory factory = new TreeNodeFactory(createIndirectObjectFactory(doc));
        PdfObjectTreeNode node = PdfObjectTreeNode.getInstance(vriLeaf);
        Assertions.assertNotNull(node);
        expandAll(factory, node);

        // Getting stream array node
        node = node.getDictionaryChildNode(key);
        Assertions.assertNotNull(node);
        Assertions.assertTrue(node.isArray());
        Assertions.assertEquals(1, node.getChildCount());
        // Getting stream reference node
        node = (PdfObjectTreeNode) node.getChildAt(0);
        Assertions.assertTrue(node.isIndirectReference());
        Assertions.assertEquals(1, node.getChildCount());
        // Getting stream node
        node = (PdfObjectTreeNode) node.getChildAt(0);
        Assertions.assertTrue(node.isStream());
        if (expectedLeafNodeName == null) {
            Assertions.assertEquals(0, node.getChildCount());
        } else {
            Assertions.assertEquals(1, node.getChildCount());
            Assertions.assertInstanceOf(Asn1SequenceTreeNode.class, node.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, expectedLeafNodeName, (AbstractAsn1TreeNode) node.getChildAt(0));
        }
    }

    private static void doTsTest(byte[] data, String expectedLeafNodeName) {
        /*
         * I have not found a document with a timestamp in /DSS in the wild,
         * so this is based on the spec.
         */
        final PdfDocument doc = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
        final PdfDictionary vriLeaf = makeIndirect(doc, new PdfDictionary(Map.of(
                new PdfName("TS"), new PdfStream(data),
                PdfName.Type, PdfName.VRI
        )));
        final PdfDictionary vri = makeIndirect(doc, new PdfDictionary(Map.of(
                new PdfName("112233445566778899AABBCCDDEEFF1122334455"), vriLeaf.getIndirectReference()
        )));
        final PdfDictionary dss = makeIndirect(doc, new PdfDictionary(Map.of(
                PdfName.VRI, vri.getIndirectReference()
        )));
        doc.getCatalog().getPdfObject().put(PdfName.DSS, dss);

        final TreeNodeFactory factory = new TreeNodeFactory(createIndirectObjectFactory(doc));
        PdfObjectTreeNode node = PdfObjectTreeNode.getInstance(vriLeaf);
        Assertions.assertNotNull(node);
        expandAll(factory, node);

        // Getting stream node
        node = node.getDictionaryChildNode(new PdfName("TS"));
        Assertions.assertNotNull(node);
        Assertions.assertTrue(node.isStream());
        if (expectedLeafNodeName == null) {
            Assertions.assertEquals(0, node.getChildCount());
        } else {
            Assertions.assertEquals(1, node.getChildCount());
            Assertions.assertInstanceOf(Asn1SequenceTreeNode.class, node.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, expectedLeafNodeName, (AbstractAsn1TreeNode) node.getChildAt(0));
        }
    }

    private static void doSVCertTest(
            PdfName ft,
            PdfName svType,
            PdfName svCertType,
            PdfObject data,
            String expectedLeafNodeName
    ) {
        /*
         * I have not found a document with a seed value dictionary in the
         * wild, so this is based on the spec. Tests both /Subject and
         * /Issuer.
         */
        final PdfDocument doc = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
        final PdfDictionary svCertDict = makeIndirect(doc, new PdfDictionary());
        if (svCertType != null) {
            svCertDict.put(PdfName.Type, svCertType);
        }
        svCertDict.put(PdfName.Subject, new PdfArray(data));
        svCertDict.put(new PdfName("Issuer"), new PdfArray(data.clone()));
        final PdfDictionary svDict = makeIndirect(doc, new PdfDictionary());
        if (svType != null) {
            svDict.put(PdfName.Type, svType);
        }
        svDict.put(PdfName.Cert, svCertDict);
        final PdfDictionary sigFormField = makeIndirect(doc, new PdfDictionary());
        if (ft != null) {
            sigFormField.put(PdfName.FT, ft);
        }
        sigFormField.put(SV, svDict.getIndirectReference());
        final PdfDictionary acroFormDict = createAcroFormDict(doc, sigFormField);
        doc.getCatalog().getPdfObject().put(PdfName.AcroForm, acroFormDict);

        final TreeNodeFactory factory = new TreeNodeFactory(createIndirectObjectFactory(doc));
        PdfObjectTreeNode node = PdfObjectTreeNode.getInstance(sigFormField);
        Assertions.assertNotNull(node);
        expandAll(factory, node);

        // Getting /SV dictionary reference
        node = node.getDictionaryChildNode(SV);
        Assertions.assertNotNull(node);
        Assertions.assertTrue(node.isIndirectReference());
        Assertions.assertEquals(1, node.getChildCount());
        // Getting /SV dictionary
        node = (PdfObjectTreeNode) node.getChildAt(0);
        Assertions.assertTrue(node.isDictionary());
        // Getting /SVCert
        node = node.getDictionaryChildNode(PdfName.Cert);
        Assertions.assertNotNull(node);
        Assertions.assertTrue(node.isDictionary());
        // Getting and checking /Subject data
        {
            PdfObjectTreeNode subject = node.getDictionaryChildNode(PdfName.Subject);
            Assertions.assertNotNull(subject);
            Assertions.assertTrue(subject.isArray());
            Assertions.assertEquals(1, subject.getChildCount());
            subject = (PdfObjectTreeNode) subject.getChildAt(0);
            if (expectedLeafNodeName == null) {
                Assertions.assertEquals(0, subject.getChildCount());
            } else {
                Assertions.assertEquals(1, subject.getChildCount());
                Assertions.assertInstanceOf(Asn1SequenceTreeNode.class, subject.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, expectedLeafNodeName, (AbstractAsn1TreeNode) subject.getChildAt(0));
            }
        }
        // Getting and checking /Issuer data
        {
            PdfObjectTreeNode issuer = node.getDictionaryChildNode(new PdfName("Issuer"));
            Assertions.assertNotNull(issuer);
            Assertions.assertTrue(issuer.isArray());
            Assertions.assertEquals(1, issuer.getChildCount());
            issuer = (PdfObjectTreeNode) issuer.getChildAt(0);
            if (expectedLeafNodeName == null) {
                Assertions.assertEquals(0, issuer.getChildCount());
            } else {
                Assertions.assertEquals(1, issuer.getChildCount());
                Assertions.assertInstanceOf(Asn1SequenceTreeNode.class, issuer.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, expectedLeafNodeName, (AbstractAsn1TreeNode) issuer.getChildAt(0));
            }
        }
    }

    private static PdfDictionary createSigDict(PdfDocument doc, PdfName type, PdfObject contents) {
        final PdfDictionary sig = makeIndirect(doc, new PdfDictionary());
        sig.put(PdfName.Filter, PdfName.Adobe_PPKLite);
        sig.put(PdfName.Contents, contents);
        if (type != null) {
            sig.put(PdfName.Type, type);
        }
        return sig;
    }

    private static PdfDictionary createSigFormField(PdfDocument doc, PdfName ft, PdfDictionary v) {
        final PdfDictionary field = makeIndirect(doc, new PdfDictionary());
        field.put(PdfName.V, v.getIndirectReference());
        if (ft != null) {
            field.put(PdfName.FT, ft);
        }
        return field;
    }

    private static PdfDictionary createAcroFormDict(PdfDocument doc, PdfDictionary field) {
        final PdfArray fields = new PdfArray(field.getIndirectReference());
        final PdfDictionary acroForm = makeIndirect(doc, new PdfDictionary());
        acroForm.put(PdfName.Fields, fields);
        return acroForm;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static IndirectObjectFactory createIndirectObjectFactory(PdfDocument doc) {
        final IndirectObjectFactory factory = new IndirectObjectFactory(doc);
        while (factory.storeNextObject()) {
            // Empty
        }
        return factory;
    }

    private static void expandAll(TreeNodeFactory factory, PdfObjectTreeNode node) {
        factory.expandNode(node);
        for (int i = 0; i < node.getChildCount(); ++i) {
            final TreeNode child = node.getChildAt(i);
            if (child instanceof PdfObjectTreeNode) {
                expandAll(factory, (PdfObjectTreeNode) child);
            }
        }
    }

    private static PdfIndirectReference makeIndirectRef(PdfDocument doc, PdfObject obj) {
        return makeIndirect(doc, obj).getIndirectReference();
    }

    private static <T extends PdfObject> T makeIndirect(PdfDocument doc, T obj) {
        obj.makeIndirect(doc);
        return obj;
    }
}