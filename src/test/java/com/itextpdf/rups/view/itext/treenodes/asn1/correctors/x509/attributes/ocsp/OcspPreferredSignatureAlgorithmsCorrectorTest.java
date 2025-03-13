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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.ocsp;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class OcspPreferredSignatureAlgorithmsCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        OcspPreferredSignatureAlgorithmsCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "preferredSignatureAlgorithms");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        OcspPreferredSignatureAlgorithmsCorrector.INSTANCE.correct(node, "psa");
        validateDefaultNode(node, "psa");
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        OcspPreferredSignatureAlgorithmsCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_InvalidSequenceEntry() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new ASN1Integer(42)
                )
        );
        OcspPreferredSignatureAlgorithmsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "preferredSignatureAlgorithms", node);
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 42", node.getChildAt(0));
    }

    @Test
    void correct_EmptyPreferredSignatureAlgorithmSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence()
                )
        );
        OcspPreferredSignatureAlgorithmsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "preferredSignatureAlgorithms", node);
        Asn1TestUtil.assertNodeMatches(0, "preferredSignatureAlgorithm", node.getChildAt(0));
    }

    @Test
    void correct_TaggedPreferredSignatureAlgorithmTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 0, new DERSequence()),
                                new DERTaggedObject(true, 1, new DERSequence()),
                        })
                )
        );
        OcspPreferredSignatureAlgorithmsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "preferredSignatureAlgorithms", node);
        {
            final AbstractAsn1TreeNode psa = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(2, "preferredSignatureAlgorithm", psa);
            Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT SEQUENCE", psa.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT SEQUENCE", psa.getChildAt(1));
        }
    }

    @Test
    void correct_EmptyPreferredSignatureAlgorithmFields() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERSequence(),
                                new DERSequence(),
                        })
                )
        );
        OcspPreferredSignatureAlgorithmsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "preferredSignatureAlgorithms", node);
        {
            final AbstractAsn1TreeNode psa = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(2, "preferredSignatureAlgorithm", psa);
            Asn1TestUtil.assertNodeMatches(0, "sigIdentifier", psa.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "pubKeyAlgIdentifier", psa.getChildAt(1));
        }
    }

    @Test
    void correct_TaggedSMimeCapabilityFields() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERSequence(),
                                new DERSequence(new ASN1Encodable[] {
                                        new DERTaggedObject(true, 0,
                                                new ASN1ObjectIdentifier("1.2.840.10045.2.1")   // ecPublicKey
                                        ),
                                        new DERTaggedObject(true, 1,
                                                new ASN1ObjectIdentifier("1.2.840.10045.3.1.7") // prime256v1
                                        ),
                                }),
                        })
                )
        );
        OcspPreferredSignatureAlgorithmsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "preferredSignatureAlgorithms", node);
        {
            final AbstractAsn1TreeNode psa = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(2, "preferredSignatureAlgorithm", psa);
            Asn1TestUtil.assertNodeMatches(0, "sigIdentifier", psa.getChildAt(0));
            {
                final AbstractAsn1TreeNode pubKeyAlgIdentifier = psa.getChildAt(1);
                Asn1TestUtil.assertNodeMatches(2, "pubKeyAlgIdentifier", pubKeyAlgIdentifier);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "[0] EXPLICIT OBJECT IDENTIFIER: 1.2.840.10045.2.1 (/iso/member-body/us"
                                + "/ansi-X9-62/keyType/ecPublicKey)",
                        pubKeyAlgIdentifier.getChildAt(0)
                );
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "parameters: 1.2.840.10045.3.1.7 (/iso/member-body/us"
                                + "/ansi-X9-62/curves/prime/prime256v1)",
                        pubKeyAlgIdentifier.getChildAt(1)
                );
            }
        }
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(2, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode firstPsa = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "preferredSignatureAlgorithm", firstPsa);
            {
                final AbstractAsn1TreeNode sigIdentifier = firstPsa.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(1, "sigIdentifier", sigIdentifier);
                Asn1TestUtil.assertNodeMatches(0,
                        "algorithm: 1.2.840.10045.4.3.4 (/iso/member-body/us/ansi-X9-62/signatures"
                                + "/ecdsa-with-SHA2/ecdsa-with-SHA512)", sigIdentifier.getChildAt(0));
            }
        }
        {
            final AbstractAsn1TreeNode secondPsa = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(2, "preferredSignatureAlgorithm", secondPsa);
            {
                final AbstractAsn1TreeNode sigIdentifier = secondPsa.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(1, "sigIdentifier", sigIdentifier);
                Asn1TestUtil.assertNodeMatches(
                        0, "algorithm: 1.2.840.10045.4.3.2 (/iso/member-body/us/ansi-X9-62/signatures"
                                + "/ecdsa-with-SHA2/ecdsa-with-SHA256)",
                        sigIdentifier.getChildAt(0)
                );
            }
            {
                final AbstractAsn1TreeNode pubKeyAlgIdentifier = secondPsa.getChildAt(1);
                Asn1TestUtil.assertNodeMatches(2, "pubKeyAlgIdentifier", pubKeyAlgIdentifier);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "capabilityID: 1.2.840.10045.2.1 (/iso/member-body/us/ansi-X9-62/keyType"
                                + "/ecPublicKey)",
                        pubKeyAlgIdentifier.getChildAt(0)
                );
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "parameters: 1.2.840.10045.3.1.7 (/iso/member-body/us/ansi-X9-62/curves"
                                + "/prime/prime256v1)",
                        pubKeyAlgIdentifier.getChildAt(1)
                );
            }
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(
                                new DERSequence(
                                        new ASN1ObjectIdentifier("1.2.840.10045.4.3.4")     // ecdsa-with-SHA512
                                )
                        ),
                        new DERSequence(new ASN1Encodable[] {
                                new DERSequence(
                                        new ASN1ObjectIdentifier("1.2.840.10045.4.3.2")     // ecdsa-with-SHA256
                                ),
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1ObjectIdentifier("1.2.840.10045.2.1"),      // ecPublicKey
                                        new ASN1ObjectIdentifier("1.2.840.10045.3.1.7"),    // prime256v1
                                }),
                        }),
                })
        );
    }
}