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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class OcspResponseCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        OcspResponseCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "ocspResponse");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        OcspResponseCorrector.INSTANCE.correct(node, "OP");
        validateDefaultNode(node, "OP");
    }

    @Test
    void correct_DifferentStatuses() {
        testResponseStatus(0, "successful");
        testResponseStatus(1, "malformedRequest");
        testResponseStatus(2, "internalError");
        testResponseStatus(3, "tryLater");
        testResponseStatus(4, null);
        testResponseStatus(5, "sigRequired");
        testResponseStatus(6, "unauthorized");
        testResponseStatus(7, null);
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        OcspResponseCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_InvalidOcspResponseFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(0),
                        new DERSequence(),
                })
        );
        OcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "ocspResponse", node);
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 0", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "SEQUENCE", node.getChildAt(1));
    }

    @Test
    void correct_InvalidResponseBytesBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Enumerated(0),
                        new DERTaggedObject(true, 0, new ASN1Integer(1)),
                })
        );
        OcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "ocspResponse", node);
        Asn1TestUtil.assertNodeMatches(0, "responseStatus: 0 (successful)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT INTEGER: 1", node.getChildAt(1));
    }

    @Test
    void correct_EmptyResponseBytesSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Enumerated(0),
                        new DERTaggedObject(true, 0, new DERSequence()),
                })
        );
        OcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "ocspResponse", node);
        Asn1TestUtil.assertNodeMatches(0, "responseStatus: 0 (successful)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "responseBytes", node.getChildAt(1));
    }

    @Test
    void correct_InvalidResponseBytesSequenceFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Enumerated(0),
                        new DERTaggedObject(true, 0, new DERSequence(new ASN1Encodable[] {
                                new ASN1Integer(0),
                                new ASN1Integer(1),
                        })),
                })
        );
        OcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "ocspResponse", node);
        Asn1TestUtil.assertNodeMatches(0, "responseStatus: 0 (successful)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(2, "responseBytes", node.getChildAt(1));
        for (int i = 0; i < 2; ++i) {
            Asn1TestUtil.assertNodeMatches(0, "INTEGER: " + i, node.getChildAt(1).getChildAt(i));
        }
    }

    @Test
    void correct_InvalidResponseData() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Enumerated(0),
                        new DERTaggedObject(true, 0, new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.1"),   // id-pkix-ocsp-basic
                                // BOOLEAN without a value
                                new DEROctetString(new byte[] {0x01, 0x00}),
                        })),
                })
        );
        OcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "ocspResponse", node);
        Asn1TestUtil.assertNodeMatches(0, "responseStatus: 0 (successful)", node.getChildAt(0));
        {
            final AbstractAsn1TreeNode responseBytes = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(2, "responseBytes", responseBytes);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "responseType: 1.3.6.1.5.5.7.48.1.1 (/iso/identified-organization/dod/internet"
                            + "/security/mechanisms/pkix/ad/id-ad-ocsp/id-pkix-ocsp-basic)",
                    responseBytes.getChildAt(0)
            );
            Asn1TestUtil.assertNodeMatches(0, "response: 0x0100", responseBytes.getChildAt(1));
        }
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(2, expectedVariableName, node);
        Asn1TestUtil.assertNodeMatches(0, "responseStatus: 0 (successful)", node.getChildAt(0));
        {
            final AbstractAsn1TreeNode responseBytes = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(2, "responseBytes", responseBytes);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "responseType: 1.3.6.1.5.5.7.48.1.1 (/iso/identified-organization/dod/internet"
                            + "/security/mechanisms/pkix/ad/id-ad-ocsp/id-pkix-ocsp-basic)",
                    responseBytes.getChildAt(0)
            );
            {
                final AbstractAsn1TreeNode response = responseBytes.getChildAt(1);
                Asn1TestUtil.assertNodeMatches(1, "response: 0x3000", response);
                Asn1TestUtil.assertNodeMatches(0, "basicOCSPResponse", response.getChildAt(0));
            }
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        /*
         * No need to fill the whole response bytes here, as it can massive...
         * It will be tested separately anyway.
         */
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Enumerated(0),  // successful
                        new DERTaggedObject(true, 0, new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.1"),   // id-pkix-ocsp-basic
                                new DEROctetString(new byte[] {0x30, 0x00}),        // Empty SEQUENCE
                        })),
                })
        );
    }

    private static void testResponseStatus(int value, String description) {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new ASN1Enumerated(value)
                )
        );
        OcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "ocspResponse", node);
        if (description == null) {
            Asn1TestUtil.assertNodeMatches(0, "responseStatus: " + value, node.getChildAt(0));
        } else {
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "responseStatus: " + value + " (" + description + ")",
                    node.getChildAt(0)
            );
        }
    }
}
