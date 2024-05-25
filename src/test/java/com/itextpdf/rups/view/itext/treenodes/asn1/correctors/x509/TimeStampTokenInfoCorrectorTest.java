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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class TimeStampTokenInfoCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        TimeStampTokenInfoCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "tstInfo");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        TimeStampTokenInfoCorrector.INSTANCE.correct(node, "tsti");
        validateDefaultNode(node, "tsti");
    }

    @Test
    void correct_WithoutOptionalFields() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new ASN1ObjectIdentifier("1.3.6.1.4.1.38064.1.3.6.1"),
                        new DERSequence(),
                        new ASN1Integer(777),
                        new DERGeneralizedTime("202405011337"),
                })
        );
        TimeStampTokenInfoCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(5, "tstInfo", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(
                0,
                "policy: 1.3.6.1.4.1.38064.1.3.6.1 (/iso/identified-organization/dod/internet"
                        + "/private/enterprise/38064/1/3/6/1)",
                node.getChildAt(1)
        );
        Asn1TestUtil.assertNodeMatches(0, "messageImprint", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "serialNumber: 777", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(
                0,
                "genTime: 202405011337 (2024-05-01T13:37:00Z)",
                node.getChildAt(4)
        );
    }

    @Test
    void correct_InvalidRequiredFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Enumerated(1),
                        new ASN1Enumerated(2),
                        new ASN1Enumerated(3),
                        new ASN1Enumerated(4),
                        new ASN1Enumerated(5),
                })
        );
        TimeStampTokenInfoCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(5, "tstInfo", node);
        Asn1TestUtil.assertNodeMatches(0, "ENUMERATED: 1", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "ENUMERATED: 2", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "ENUMERATED: 3", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "ENUMERATED: 4", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(0, "ENUMERATED: 5", node.getChildAt(4));
    }

    @Test
    void correct_InvalidMessageImprintFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new ASN1ObjectIdentifier("1.3.6.1.4.1.38064.1.3.6.1"),
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 1, new ASN1Integer(1)),
                                new DERTaggedObject(true, 2, new ASN1Integer(2)),
                        }),
                })
        );
        TimeStampTokenInfoCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "tstInfo", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(
                0,
                "policy: 1.3.6.1.4.1.38064.1.3.6.1 (/iso/identified-organization/dod/internet"
                        + "/private/enterprise/38064/1/3/6/1)",
                node.getChildAt(1)
        );
        {
            final AbstractAsn1TreeNode messageImprint = node.getChildAt(2);
            Asn1TestUtil.assertNodeMatches(2, "messageImprint", messageImprint);
            Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT INTEGER: 1", messageImprint.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "[2] EXPLICIT INTEGER: 2", messageImprint.getChildAt(1));
        }
    }

    @Test
    void correct_EmptyAccuracySequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new ASN1ObjectIdentifier("1.3.6.1.4.1.38064.1.3.6.1"),
                        new DERSequence(),
                        new ASN1Integer(777),
                        new DERGeneralizedTime("202405011337"),
                        new DERSequence(),
                })
        );
        TimeStampTokenInfoCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(6, "tstInfo", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(
                0,
                "policy: 1.3.6.1.4.1.38064.1.3.6.1 (/iso/identified-organization/dod/internet"
                        + "/private/enterprise/38064/1/3/6/1)",
                node.getChildAt(1)
        );
        Asn1TestUtil.assertNodeMatches(0, "messageImprint", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "serialNumber: 777", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(
                0,
                "genTime: 202405011337 (2024-05-01T13:37:00Z)",
                node.getChildAt(4)
        );
        Asn1TestUtil.assertNodeMatches(0, "accuracy", node.getChildAt(5));
    }

    @Test
    void correct_InvalidAccuracySecondsType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new ASN1ObjectIdentifier("1.3.6.1.4.1.38064.1.3.6.1"),
                        new DERSequence(),
                        new ASN1Integer(777),
                        new DERGeneralizedTime("202405011337"),
                        new DERSequence(
                                new ASN1Enumerated(1)
                        ),
                })
        );
        TimeStampTokenInfoCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(6, "tstInfo", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(
                0,
                "policy: 1.3.6.1.4.1.38064.1.3.6.1 (/iso/identified-organization/dod/internet"
                        + "/private/enterprise/38064/1/3/6/1)",
                node.getChildAt(1)
        );
        Asn1TestUtil.assertNodeMatches(0, "messageImprint", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "serialNumber: 777", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(
                0,
                "genTime: 202405011337 (2024-05-01T13:37:00Z)",
                node.getChildAt(4)
        );
        {
            final AbstractAsn1TreeNode accuracy = node.getChildAt(5);
            Asn1TestUtil.assertNodeMatches(1, "accuracy", accuracy);
            Asn1TestUtil.assertNodeMatches(0, "ENUMERATED: 1", accuracy.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidAccuracyMillisType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new ASN1ObjectIdentifier("1.3.6.1.4.1.38064.1.3.6.1"),
                        new DERSequence(),
                        new ASN1Integer(777),
                        new DERGeneralizedTime("202405011337"),
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1Integer(1),
                                new ASN1Integer(2),
                        }),
                })
        );
        TimeStampTokenInfoCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(6, "tstInfo", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(
                0,
                "policy: 1.3.6.1.4.1.38064.1.3.6.1 (/iso/identified-organization/dod/internet"
                        + "/private/enterprise/38064/1/3/6/1)",
                node.getChildAt(1)
        );
        Asn1TestUtil.assertNodeMatches(0, "messageImprint", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "serialNumber: 777", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(
                0,
                "genTime: 202405011337 (2024-05-01T13:37:00Z)",
                node.getChildAt(4)
        );
        {
            final AbstractAsn1TreeNode accuracy = node.getChildAt(5);
            Asn1TestUtil.assertNodeMatches(2, "accuracy", accuracy);
            Asn1TestUtil.assertNodeMatches(0, "seconds: 1", accuracy.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "INTEGER: 2", accuracy.getChildAt(1));
        }
    }

    @Test
    void correct_InvalidAccuracyMicrosType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new ASN1ObjectIdentifier("1.3.6.1.4.1.38064.1.3.6.1"),
                        new DERSequence(),
                        new ASN1Integer(777),
                        new DERGeneralizedTime("202405011337"),
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1Integer(1),
                                new DERTaggedObject(false, 0, new ASN1Integer(2)),
                                new ASN1Integer(3),
                        }),
                })
        );
        TimeStampTokenInfoCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(6, "tstInfo", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(
                0,
                "policy: 1.3.6.1.4.1.38064.1.3.6.1 (/iso/identified-organization/dod/internet"
                        + "/private/enterprise/38064/1/3/6/1)",
                node.getChildAt(1)
        );
        Asn1TestUtil.assertNodeMatches(0, "messageImprint", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "serialNumber: 777", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(
                0,
                "genTime: 202405011337 (2024-05-01T13:37:00Z)",
                node.getChildAt(4)
        );
        {
            final AbstractAsn1TreeNode accuracy = node.getChildAt(5);
            Asn1TestUtil.assertNodeMatches(3, "accuracy", accuracy);
            Asn1TestUtil.assertNodeMatches(0, "seconds: 1", accuracy.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "millis: 2", accuracy.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "INTEGER: 3", accuracy.getChildAt(2));
        }
    }

    @Test
    void correct_InvalidAccuracyTaggedBaseTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new ASN1ObjectIdentifier("1.3.6.1.4.1.38064.1.3.6.1"),
                        new DERSequence(),
                        new ASN1Integer(777),
                        new DERGeneralizedTime("202405011337"),
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1Integer(1),
                                new DERTaggedObject(false, 0, new ASN1Enumerated(2)),
                                new DERTaggedObject(false, 1, new ASN1Enumerated(3)),
                        }),
                })
        );
        TimeStampTokenInfoCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(6, "tstInfo", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(
                0,
                "policy: 1.3.6.1.4.1.38064.1.3.6.1 (/iso/identified-organization/dod/internet"
                        + "/private/enterprise/38064/1/3/6/1)",
                node.getChildAt(1)
        );
        Asn1TestUtil.assertNodeMatches(0, "messageImprint", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "serialNumber: 777", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(
                0,
                "genTime: 202405011337 (2024-05-01T13:37:00Z)",
                node.getChildAt(4)
        );
        {
            final AbstractAsn1TreeNode accuracy = node.getChildAt(5);
            Asn1TestUtil.assertNodeMatches(3, "accuracy", accuracy);
            Asn1TestUtil.assertNodeMatches(0, "seconds: 1", accuracy.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "[0] IMPLICIT ENUMERATED: 2", accuracy.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "[1] IMPLICIT ENUMERATED: 3", accuracy.getChildAt(2));
        }
    }

    @Test
    void correct_InvalidOptionalType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new ASN1ObjectIdentifier("1.3.6.1.4.1.38064.1.3.6.1"),
                        new DERSequence(),
                        new ASN1Integer(777),
                        new DERGeneralizedTime("202405011337"),
                        new ASN1Enumerated(1),
                })
        );
        TimeStampTokenInfoCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(6, "tstInfo", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(
                0,
                "policy: 1.3.6.1.4.1.38064.1.3.6.1 (/iso/identified-organization/dod/internet"
                        + "/private/enterprise/38064/1/3/6/1)",
                node.getChildAt(1)
        );
        Asn1TestUtil.assertNodeMatches(0, "messageImprint", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "serialNumber: 777", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(
                0,
                "genTime: 202405011337 (2024-05-01T13:37:00Z)",
                node.getChildAt(4)
        );
        Asn1TestUtil.assertNodeMatches(0, "ENUMERATED: 1", node.getChildAt(5));
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        TimeStampTokenInfoCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_InvalidUnknownVersion() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new ASN1Integer(9)
                )
        );
        TimeStampTokenInfoCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "tstInfo", node);
        Asn1TestUtil.assertNodeMatches(0, "version: 9", node.getChildAt(0));
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(10, expectedVariableName, node);
        Asn1TestUtil.assertNodeMatches(0, "version: 1 (v1)", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(
                0,
                "policy: 1.3.6.1.4.1.38064.1.3.6.1 (/iso/identified-organization/dod/internet"
                        + "/private/enterprise/38064/1/3/6/1)",
                node.getChildAt(1)
        );
        {
            final AbstractAsn1TreeNode messageImprint = node.getChildAt(2);
            Asn1TestUtil.assertNodeMatches(2, "messageImprint", messageImprint);
            {
                final AbstractAsn1TreeNode hashAlgorithm = messageImprint.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "hashAlgorithm", hashAlgorithm);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "algorithm: 2.16.840.1.101.3.4.2.1 (/joint-iso-itu-t/country/us"
                                + "/organization/gov/csor/nistAlgorithms/hashAlgs/sha256)",
                        hashAlgorithm.getChildAt(0)
                );
                Asn1TestUtil.assertNodeMatches(0, "parameters: NULL", hashAlgorithm.getChildAt(1));
            }
            Asn1TestUtil.assertNodeMatches(0, "hashedMessage: 0x11223344", messageImprint.getChildAt(1));
        }
        Asn1TestUtil.assertNodeMatches(0, "serialNumber: 777", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(
                0,
                "genTime: 202405011337 (2024-05-01T13:37:00Z)",
                node.getChildAt(4)
        );
        {
            final AbstractAsn1TreeNode accuracy = node.getChildAt(5);
            Asn1TestUtil.assertNodeMatches(3, "accuracy", accuracy);
            Asn1TestUtil.assertNodeMatches(0, "seconds: 1", accuracy.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "millis: 500", accuracy.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "micros: 250", accuracy.getChildAt(2));
        }
        Asn1TestUtil.assertNodeMatches(0, "ordering: TRUE", node.getChildAt(6));
        Asn1TestUtil.assertNodeMatches(0, "nonce: 123456789", node.getChildAt(7));
        Asn1TestUtil.assertNodeMatches(0, "tsa: ssl.com", node.getChildAt(8));
        {
            final AbstractAsn1TreeNode extensions = node.getChildAt(9);
            Asn1TestUtil.assertNodeMatches(1, "extensions", extensions);
            Asn1TestUtil.assertNodeMatches(0, "extension", extensions.getChildAt(0));
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        /*
         * No need to fill extensions here, as they can be massive...
         * It will be tested separately anyway.
         */
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(1),
                        new ASN1ObjectIdentifier("1.3.6.1.4.1.38064.1.3.6.1"),
                        new DERSequence(new ASN1Encodable[] {
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1ObjectIdentifier("2.16.840.1.101.3.4.2.1"), // sha256
                                        DERNull.INSTANCE,
                                }),
                                new DEROctetString(new byte[] {0x11, 0x22, 0x33, 0x44}),
                        }),
                        new ASN1Integer(777),
                        new DERGeneralizedTime("202405011337"),
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1Integer(1),
                                new DERTaggedObject(false, 0, new ASN1Integer(500)),
                                new DERTaggedObject(false, 1, new ASN1Integer(250)),
                        }),
                        ASN1Boolean.getInstance(true),
                        new ASN1Integer(123456789),
                        new DERTaggedObject(true, 0,
                                new DERTaggedObject(false, 2, new DERIA5String("ssl.com"))
                        ),
                        new DERTaggedObject(false, 1, new DERSequence(
                                new DERSequence()
                        )),
                })
        );
    }
}