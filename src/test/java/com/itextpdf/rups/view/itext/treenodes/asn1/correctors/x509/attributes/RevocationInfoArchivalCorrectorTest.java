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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class RevocationInfoArchivalCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        RevocationInfoArchivalCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "revocationInfoArchival");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        RevocationInfoArchivalCorrector.INSTANCE.correct(node, "RIA");
        validateDefaultNode(node, "RIA");
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        RevocationInfoArchivalCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_EmptyRootSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence()
        );
        RevocationInfoArchivalCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "revocationInfoArchival", node);
    }

    @Test
    void correct_UniversalRootFieldTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(),
                        new DERSequence(),
                        new DERSequence(),
                })
        );
        RevocationInfoArchivalCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "revocationInfoArchival", node);
        Asn1TestUtil.assertNodeMatches(0, "SEQUENCE", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "SEQUENCE", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "SEQUENCE", node.getChildAt(2));
    }

    @Test
    void correct_InvalidRootFieldUnderlyingTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0, new DERSet()),
                        new DERTaggedObject(true, 1, new DERSet()),
                        new DERTaggedObject(true, 2, new DERSet()),
                })
        );
        RevocationInfoArchivalCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "revocationInfoArchival", node);
        Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT SET", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT SET", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "[2] EXPLICIT SET", node.getChildAt(2));
    }

    @Test
    void correct_TaggedSequenceEntryTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0, new DERSequence(
                                new DERTaggedObject(false, 3, new DERSequence())
                        )),
                        new DERTaggedObject(true, 1, new DERSequence(
                                new DERTaggedObject(false, 4, new DERSequence())
                        )),
                        new DERTaggedObject(true, 2, new DERSequence(
                                new DERTaggedObject(false, 5, new DERSequence())
                        )),
                })
        );
        RevocationInfoArchivalCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "revocationInfoArchival", node);
        {
            final AbstractAsn1TreeNode crls = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "crls", crls);
            Asn1TestUtil.assertNodeMatches(0, "[3] IMPLICIT SEQUENCE", crls.getChildAt(0));
        }
        {
            final AbstractAsn1TreeNode ocsps = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(1, "ocsps", ocsps);
            Asn1TestUtil.assertNodeMatches(0, "[4] IMPLICIT SEQUENCE", ocsps.getChildAt(0));
        }
        {
            final AbstractAsn1TreeNode otherRevInfos = node.getChildAt(2);
            Asn1TestUtil.assertNodeMatches(1, "otherRevInfos", otherRevInfos);
            Asn1TestUtil.assertNodeMatches(0, "[5] IMPLICIT SEQUENCE", otherRevInfos.getChildAt(0));
        }
    }

    @Test
    void correct_EmptyOtherRevInfoSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 2, new DERSequence(
                                new DERSequence()
                        )),
                })
        );
        RevocationInfoArchivalCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "revocationInfoArchival", node);
        {
            final AbstractAsn1TreeNode otherRevInfos = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "otherRevInfos", otherRevInfos);
            Asn1TestUtil.assertNodeMatches(0, "otherRevInfo", otherRevInfos.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidOtherRevInfoFieldTypesSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 2, new DERSequence(
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1Integer(1),
                                        new ASN1Integer(2),
                                })
                        )),
                })
        );
        RevocationInfoArchivalCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "revocationInfoArchival", node);
        {
            final AbstractAsn1TreeNode otherRevInfos = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "otherRevInfos", otherRevInfos);
            {
                final AbstractAsn1TreeNode otherRevInfo = otherRevInfos.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "otherRevInfo", otherRevInfo);
                Asn1TestUtil.assertNodeMatches(0, "INTEGER: 1", otherRevInfo.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "INTEGER: 2", otherRevInfo.getChildAt(1));
            }
        }
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(3, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode crls = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "crls", crls);
            Asn1TestUtil.assertNodeMatches(0, "crl", crls.getChildAt(0));
        }
        {
            final AbstractAsn1TreeNode ocsps = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(1, "ocsps", ocsps);
            Asn1TestUtil.assertNodeMatches(0, "ocspResponse", ocsps.getChildAt(0));
        }
        {
            final AbstractAsn1TreeNode otherRevInfos = node.getChildAt(2);
            Asn1TestUtil.assertNodeMatches(1, "otherRevInfos", otherRevInfos);
            {
                final AbstractAsn1TreeNode otherRevInfo = otherRevInfos.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "otherRevInfo", otherRevInfo);
                Asn1TestUtil.assertNodeMatches(0,
                        "type: 1.3.6.1.5.5.7.48.1.1 (/iso/identified-organization/dod/internet"
                                + "/security/mechanisms/pkix/ad/id-ad-ocsp/id-pkix-ocsp-basic)",
                        otherRevInfo.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "value: ", otherRevInfo.getChildAt(1));
            }
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        /*
         * No need to fill the whole crls/ocsps here, as they are massive...
         * They will be tested separately anyway.
         */
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0, new DERSequence(
                                new DERSequence()
                        )),
                        new DERTaggedObject(true, 1, new DERSequence(
                                new DERSequence()
                        )),
                        new DERTaggedObject(true, 2, new DERSequence(
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.1"),   // id-pkix-ocsp-basic
                                        new DEROctetString(new byte[0]),
                                })
                        )),
                })
        );
    }
}