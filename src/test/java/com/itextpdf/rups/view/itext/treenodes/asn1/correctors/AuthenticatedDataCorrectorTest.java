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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class AuthenticatedDataCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        AuthenticatedDataCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "authData");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        AuthenticatedDataCorrector.INSTANCE.correct(node, "ad");
        validateDefaultNode(node, "ad");
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(new ASN1Integer(42));
        AuthenticatedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 42", node);
    }

    @Test
    void correct_EmptyRootSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(new DERSequence());
        AuthenticatedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "authData", node);
    }

    @Test
    void correct_WithoutOptionalFields() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(0),
                        new DERSet(),
                        new DERSequence(),
                        new DERSequence(),
                        new DEROctetString(new byte[] {0x21, 0x43, 0x65, 0x77}),
                })
        );
        AuthenticatedDataCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(5, "authData", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 0 (v0)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "recipientInfos", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "macAlgorithm", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "encapContentInfo", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(0, "mac: 0x21436577", node.getChildAt(4));
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(9, expectedVariableName, node);
        Asn1TestUtil.assertNodeMatches(0, "version: 0 (v0)", node.getChildAt(0));
        {
            final AbstractAsn1TreeNode originatorInfo = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(1, "originatorInfo", originatorInfo);
            Asn1TestUtil.assertNodeMatches(0, "crls", originatorInfo.getChildAt(0));
        }
        {
            final AbstractAsn1TreeNode recipientInfos = node.getChildAt(2);
            Asn1TestUtil.assertNodeMatches(1, "recipientInfos", recipientInfos);
            {
                final AbstractAsn1TreeNode pwri = recipientInfos.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(1, "pwri", pwri);
                Asn1TestUtil.assertNodeMatches(0, "version: 0 (v0)", pwri.getChildAt(0));
            }
        }
        {
            final AbstractAsn1TreeNode macAlgorithm = node.getChildAt(3);
            Asn1TestUtil.assertNodeMatches(1, "macAlgorithm", macAlgorithm);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "algorithm: 1.2.840.113549.2.9 (/iso/member-body/us/rsadsi/digestAlgorithm"
                            + "/hmacWithSHA256)",
                    macAlgorithm.getChildAt(0)
            );
        }
        {
            final AbstractAsn1TreeNode digestAlgorithm = node.getChildAt(4);
            Asn1TestUtil.assertNodeMatches(1, "digestAlgorithm", digestAlgorithm);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "algorithm: 2.16.840.1.101.3.4.2.1 (/joint-iso-itu-t/country/us"
                            + "/organization/gov/csor/nistAlgorithms/hashAlgs/id-sha256)",
                    digestAlgorithm.getChildAt(0)
            );
        }
        {
            final AbstractAsn1TreeNode encapContentInfo = node.getChildAt(5);
            Asn1TestUtil.assertNodeMatches(2, "encapContentInfo", encapContentInfo);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "eContentType: 1.0.32004.1.0 (/iso/standard/iso32004/pdfmac/"
                            + "id-ct-pdfMacIntegrityInfo)",
                    encapContentInfo.getChildAt(0)
            );
            {
                final AbstractAsn1TreeNode eContent = encapContentInfo.getChildAt(1);
                Asn1TestUtil.assertNodeMatches(1, "eContent: 0x3000", eContent);
                Asn1TestUtil.assertNodeMatches(0, "pdfMacIntegrityInfo", eContent.getChildAt(0));
            }
        }
        {
            final AbstractAsn1TreeNode authAttrs = node.getChildAt(6);
            Asn1TestUtil.assertNodeMatches(1, "authAttrs", authAttrs);
            {
                final AbstractAsn1TreeNode attr = authAttrs.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "attribute", attr);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "attrType: 1.2.840.113549.1.9.25.4 (/iso/member-body/us/rsadsi/pkcs"
                                + "/pkcs-9/pkcs-9-at/sequenceNumber)",
                        attr.getChildAt(0)
                );
                {
                    final AbstractAsn1TreeNode values = attr.getChildAt(1);
                    Asn1TestUtil.assertNodeMatches(1, "attrValues", values);
                    Asn1TestUtil.assertNodeMatches(0, "sequenceNumber: 2", values.getChildAt(0));
                }
            }
        }
        Asn1TestUtil.assertNodeMatches(0, "mac: 0x12345678", node.getChildAt(7));
        {
            final AbstractAsn1TreeNode unauthAttrs = node.getChildAt(8);
            Asn1TestUtil.assertNodeMatches(1, "unauthAttrs", unauthAttrs);
            {
                final AbstractAsn1TreeNode attr = unauthAttrs.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "attribute", attr);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "attrType: 1.2.840.113549.1.9.25.4 (/iso/member-body/us/rsadsi/pkcs"
                                + "/pkcs-9/pkcs-9-at/sequenceNumber)",
                        attr.getChildAt(0)
                );
                {
                    final AbstractAsn1TreeNode values = attr.getChildAt(1);
                    Asn1TestUtil.assertNodeMatches(1, "attrValues", values);
                    Asn1TestUtil.assertNodeMatches(0, "sequenceNumber: 3", values.getChildAt(0));
                }
            }
        }
    }

    private static AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(0),
                        new DERTaggedObject(false, 0, new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(false, 1, new DERSet())
                        })),
                        new DERSet(
                                new DERTaggedObject(false, 3, new DERSequence(
                                        new ASN1Integer(0)
                                ))
                        ),
                        new DERSequence(
                                // hmacWithSHA256
                                new ASN1ObjectIdentifier("1.2.840.113549.2.9")
                        ),
                        new DERTaggedObject(false, 1, new DERSequence(
                                // id-sha256
                                new ASN1ObjectIdentifier("2.16.840.1.101.3.4.2.1")
                        )),
                        new DERSequence(new ASN1Encodable[] {
                                // id-ct-pdfMacIntegrityInfo
                                new ASN1ObjectIdentifier("1.0.32004.1.0"),
                                new DERTaggedObject(true, 0,
                                        new DEROctetString(new byte[] {0x30, 0x00})
                                ),
                        }),
                        new DERTaggedObject(false, 2, new DERSet(
                                new DERSequence(new ASN1Encodable[] {
                                        // sequenceNumber
                                        new ASN1ObjectIdentifier("1.2.840.113549.1.9.25.4"),
                                        new DERSet(
                                                new ASN1Integer(2)
                                        )
                                })
                        )),
                        new DEROctetString(new byte[] {0x12, 0x34, 0x56, 0x78}),
                        new DERTaggedObject(false, 3, new DERSet(
                                new DERSequence(new ASN1Encodable[] {
                                        // sequenceNumber
                                        new ASN1ObjectIdentifier("1.2.840.113549.1.9.25.4"),
                                        new DERSet(
                                                new ASN1Integer(3)
                                        )
                                })
                        )),
                })
        );
    }
}
