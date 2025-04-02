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
final class TimeStampTokenCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        TimeStampTokenCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "timeStampToken");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        TimeStampTokenCorrector.INSTANCE.correct(node, "TST");
        validateDefaultNode(node, "TST");
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        TimeStampTokenCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(2, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode contentType = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "contentType: 1.2.840.113549.1.7.2 (/iso/member-body/us/rsadsi/pkcs/pkcs-7"
                            + "/signedData)",
                    contentType
            );
        }
        {
            final AbstractAsn1TreeNode content = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(4, "content", content);
            Asn1TestUtil.assertNodeMatches(0, "version: 5 (v5)", content.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "digestAlgorithms", content.getChildAt(1));
            {
                final AbstractAsn1TreeNode eci = content.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(2, "encapContentInfo", eci);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "eContentType: 1.2.840.113549.1.9.16.1.4 (/iso/member-body/us/rsadsi/pkcs"
                                + "/pkcs-9/smime/ct/id-ct-TSTInfo)",
                        eci.getChildAt(0)
                );
                Asn1TestUtil.assertNodeMatches(0, "eContent: ", eci.getChildAt(1));
            }
            Asn1TestUtil.assertNodeMatches(0, "signerInfos", content.getChildAt(3));
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        /*
         * No need to fill the whole signedData here, as it is massive...
         * It will be tested separately anyway.
         */
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1ObjectIdentifier("1.2.840.113549.1.7.2"),   // signedData
                        new DERTaggedObject(true, 0, new DERSequence(new ASN1Encodable[] {
                                new ASN1Integer(5),
                                new DERSet(),
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.1.4"),  // id-ct-TSTInfo
                                        new DERTaggedObject(true, 0, new DEROctetString(new byte[0])),
                                }),
                                new DERSet(),
                        })),
                })
        );
    }
}
