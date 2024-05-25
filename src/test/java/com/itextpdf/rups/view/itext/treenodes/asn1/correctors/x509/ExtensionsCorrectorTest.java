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
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class ExtensionsCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        ExtensionsCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "extensions");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        ExtensionsCorrector.INSTANCE.correct(node, "exts");
        validateDefaultNode(node, "exts");
    }

    @Test
    void correct_EmptySequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence()
        );
        ExtensionsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "extensions", node);
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                DERNull.INSTANCE
        );
        ExtensionsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", node);
    }

    @Test
    void correct_InvalidExtensionType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSet()
                )
        );
        ExtensionsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "extensions", node);
        Asn1TestUtil.assertNodeMatches(0, "SET", node.getChildAt(0));
    }

    @Test
    void correct_InvalidExtensionFieldTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1Integer(1),
                                new ASN1Integer(2),
                                new ASN1Integer(3),
                        })
                )
        );
        ExtensionsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "extensions", node);
        {
            final AbstractAsn1TreeNode ext = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "extension", ext);
            Asn1TestUtil.assertNodeMatches(0, "INTEGER: 1", ext.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "INTEGER: 2", ext.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "INTEGER: 3", ext.getChildAt(2));
        }
    }

    @Test
    void correct_InvalidExtnValue() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("2.5.29.14"),
                                // BOOLEAN without a value
                                new DEROctetString(new byte[] {0x01, 0x00}),
                        })
                )
        );
        ExtensionsCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "extensions", node);
        {
            final AbstractAsn1TreeNode ext = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(2, "extension", ext);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "extnID: 2.5.29.14 (/joint-iso-itu-t/ds/certificateExtension"
                            + "/subjectKeyIdentifier)",
                    ext.getChildAt(0)
            );
            Asn1TestUtil.assertNodeMatches(0, "extnValue: 0x0100", ext.getChildAt(1));
        }
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(2, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode ext = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(2, "extension", ext);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "extnID: 2.5.29.14 (/joint-iso-itu-t/ds/certificateExtension"
                            + "/subjectKeyIdentifier)",
                    ext.getChildAt(0)
            );
            Asn1TestUtil.assertNodeMatches(1, "extnValue: 0x04021337", ext.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "subjectKeyIdentifier: 0x1337", ext.getChildAt(1).getChildAt(0));
        }
        {
            final AbstractAsn1TreeNode ext = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(3, "extension", ext);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "extnID: 2.5.29.19 (/joint-iso-itu-t/ds/certificateExtension"
                            + "/basicConstraints)",
                    ext.getChildAt(0)
            );
            Asn1TestUtil.assertNodeMatches(0, "critical: TRUE", ext.getChildAt(1));
            {
                final AbstractAsn1TreeNode extnValue = ext.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(1, "extnValue: 0x30030101FF", extnValue);
                {
                    final AbstractAsn1TreeNode basicConstraints = extnValue.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(1, "basicConstraints", basicConstraints);
                    Asn1TestUtil.assertNodeMatches(0, "ca: TRUE", basicConstraints.getChildAt(0));
                }
            }
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("2.5.29.14"),  // subjectKeyIdentifier
                                new DEROctetString(new byte[] {0x04, 0x02, 0x13, 0x37}),
                        }),
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("2.5.29.19"),  // basicConstraints
                                ASN1Boolean.getInstance(true),
                                new DEROctetString(new byte[] {0x30, 0x03, 0x01, 0x01, (byte) 0xFF}),
                        }),
                })
        );
    }
}