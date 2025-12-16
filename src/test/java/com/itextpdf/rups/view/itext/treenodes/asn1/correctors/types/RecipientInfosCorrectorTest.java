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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DLSet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class RecipientInfosCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        RecipientInfosCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "recipientInfos");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        RecipientInfosCorrector.INSTANCE.correct(node, "ri");
        validateDefaultNode(node, "ri");
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(new DERSequence());
        RecipientInfosCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "SEQUENCE", node);
    }

    @Test
    void correct_EmptyRootSet() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(new DERSet());
        RecipientInfosCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "recipientInfos", node);
    }

    @Test
    void correct_InvalidRecipientInfoType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSet(new DERTaggedObject(true, 9, new DERSequence()))
        );
        RecipientInfosCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "recipientInfos", node);
        Asn1TestUtil.assertNodeMatches(0, "[9] EXPLICIT SEQUENCE", node.getChildAt(0));
    }

    @Test
    void correct_InvalidOtherRecipientInfoBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSet(new DERTaggedObject(false, 4, new ASN1Integer(1)))
        );
        RecipientInfosCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "recipientInfos", node);
        Asn1TestUtil.assertNodeMatches(0, "[4] IMPLICIT INTEGER: 1", node.getChildAt(0));
    }

    @Test
    void correct_EmptyOtherRecipientInfoSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSet(new DERTaggedObject(false, 4, new DERSequence()))
        );
        RecipientInfosCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "recipientInfos", node);
        Asn1TestUtil.assertNodeMatches(0, "ori", node.getChildAt(0));
    }

    @Test
    void correct_InvalidOtherRecipientInfoOriType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSet(
                        new DERTaggedObject(false, 4, new DERSequence(
                                new ASN1Integer(1)
                        ))
                )
        );
        RecipientInfosCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "recipientInfos", node);
        {
            final AbstractAsn1TreeNode ori = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "ori", ori);
            Asn1TestUtil.assertNodeMatches(0, "INTEGER: 1", ori.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidRecipientInfoBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSet(
                        new DERTaggedObject(false, 1, new ASN1Integer(1))
                )
        );
        RecipientInfosCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "recipientInfos", node);
        Asn1TestUtil.assertNodeMatches(0, "[1] IMPLICIT INTEGER: 1", node.getChildAt(0));
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(5, expectedVariableName, node);
        Asn1TestUtil.assertNodeMatches(0, "ktri", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "kari", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "kekri", node.getChildAt(2));
        {
            final AbstractAsn1TreeNode pwri = node.getChildAt(3);
            Asn1TestUtil.assertNodeMatches(1, "pwri", pwri);
            Asn1TestUtil.assertNodeMatches(0, "version: 0 (v0)", pwri.getChildAt(0));
        }
        {
            final AbstractAsn1TreeNode ori = node.getChildAt(4);
            Asn1TestUtil.assertNodeMatches(2, "ori", ori);
            Asn1TestUtil.assertNodeMatches(0, "oriType: 1.2 (/iso/member-body)", ori.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "oriValue: NULL", ori.getChildAt(1));
        }
    }

    private static AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DLSet(new ASN1Encodable[] {
                        new DERSequence(),
                        new DERTaggedObject(false, 1, new DERSequence()),
                        new DERTaggedObject(false, 2, new DERSequence()),
                        new DERTaggedObject(false, 3,
                                // Not filling fully here, since PasswordRecipientInfo is
                                // covered separately
                                new DERSequence(
                                        new ASN1Integer(0)
                                )
                        ),
                        new DERTaggedObject(false, 4, new DERSequence(new ASN1Encodable[] {
                                // member-body
                                new ASN1ObjectIdentifier("1.2"),
                                DERNull.INSTANCE,
                        })),
                })
        );
    }
}
