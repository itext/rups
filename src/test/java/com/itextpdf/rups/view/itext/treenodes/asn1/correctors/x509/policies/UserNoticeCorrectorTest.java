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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.policies;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.DERVisibleString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class UserNoticeCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        UserNoticeCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "userNotice");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        UserNoticeCorrector.INSTANCE.correct(node, "usr");
        validateDefaultNode(node, "usr");
    }

    @Test
    void correct_EmptySequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence()
        );
        UserNoticeCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "userNotice", node);
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        UserNoticeCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_InvalidSequenceTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(42),
                        new ASN1Integer(43),
                })
        );
        UserNoticeCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "userNotice", node);
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 42", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 43", node.getChildAt(1));
    }

    @Test
    void correct_EmptyNoticeReferenceSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence()
                )
        );
        UserNoticeCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "userNotice", node);
        Asn1TestUtil.assertNodeMatches(0, "noticeRef", node.getChildAt(0));
    }

    @Test
    void correct_InvalidNoticeReferenceFieldTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERPrintableString("Test"),
                                new DERSet(),
                        })
                )
        );
        UserNoticeCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "userNotice", node);
        {
            final AbstractAsn1TreeNode noticeRef = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(2, "noticeRef", noticeRef);
            Asn1TestUtil.assertNodeMatches(0, "PrintableString: Test", noticeRef.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "SET", noticeRef.getChildAt(1));
        }
    }

    @Test
    void correct_InvalidNoticeNumberType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERBMPString("Test"),
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1Integer(1),
                                        new ASN1Enumerated(2),
                                }),
                        })
                )
        );
        UserNoticeCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "userNotice", node);
        {
            final AbstractAsn1TreeNode noticeRef = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(2, "noticeRef", noticeRef);
            Asn1TestUtil.assertNodeMatches(0, "organization: Test", noticeRef.getChildAt(0));
            {
                final AbstractAsn1TreeNode noticeNumbers = noticeRef.getChildAt(1);
                Asn1TestUtil.assertNodeMatches(2, "noticeNumbers", noticeNumbers);
                Asn1TestUtil.assertNodeMatches(0, "noticeNumber: 1", noticeNumbers.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "ENUMERATED: 2", noticeNumbers.getChildAt(1));
            }
        }
    }

    @Test
    void correct_Ia5StringOrganization() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERIA5String("Test")
                        )
                )
        );
        UserNoticeCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "userNotice", node);
        {
            final AbstractAsn1TreeNode noticeRef = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "noticeRef", noticeRef);
            Asn1TestUtil.assertNodeMatches(0, "organization: Test", noticeRef.getChildAt(0));
        }
    }

    @Test
    void correct_VisibleStringOrganization() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERVisibleString("Test")
                        )
                )
        );
        UserNoticeCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "userNotice", node);
        {
            final AbstractAsn1TreeNode noticeRef = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "noticeRef", noticeRef);
            Asn1TestUtil.assertNodeMatches(0, "organization: Test", noticeRef.getChildAt(0));
        }
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(2, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode noticeRef = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(2, "noticeRef", noticeRef);
            Asn1TestUtil.assertNodeMatches(0, "organization: iText", noticeRef.getChildAt(0));
            {
                final AbstractAsn1TreeNode noticeNumbers = noticeRef.getChildAt(1);
                Asn1TestUtil.assertNodeMatches(2, "noticeNumbers", noticeNumbers);
                Asn1TestUtil.assertNodeMatches(0, "noticeNumber: 1", noticeNumbers.getChildAt(0));
                Asn1TestUtil.assertNodeMatches(0, "noticeNumber: 2", noticeNumbers.getChildAt(1));
            }
        }
        Asn1TestUtil.assertNodeMatches(0, "explicitText: Shut it down!", node.getChildAt(1));
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new DERUTF8String("iText"),
                                new DERSequence(new ASN1Encodable[] {
                                        new ASN1Integer(1),
                                        new ASN1Integer(2),
                                }),
                        }),
                        new DERUTF8String("Shut it down!"),
                })
        );
    }
}
