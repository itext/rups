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
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector;

import org.bouncycastle.asn1.ASN1BMPString;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.ASN1VisibleString;

/**
 * Corrector for the userNotice policy qualifier, as it is defined in RFC 5280.
 *
 * <pre>
 * UserNotice ::= SEQUENCE {
 *   noticeRef      NoticeReference OPTIONAL,
 *   explicitText   DisplayText OPTIONAL
 * }
 *
 * NoticeReference ::= SEQUENCE {
 *   organization   DisplayText,
 *   noticeNumbers  SEQUENCE OF INTEGER
 * }
 *
 * DisplayText ::= CHOICE {
 *   ia5String      IA5String       (SIZE (1..200)),
 *   visibleString  VisibleString   (SIZE (1..200)),
 *   bmpString      BMPString       (SIZE (1..200)),
 *   utf8String     UTF8String      (SIZE (1..200))
 * }
 * </pre>
 */
public final class UserNoticeCorrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final UserNoticeCorrector INSTANCE = new UserNoticeCorrector();

    private UserNoticeCorrector() {
        // singleton class
    }

    /**
     * OBJECT IDENTIFIER for the type, which is handled by the corrector.
     */
    public static final String OID = "1.3.6.1.5.5.7.2.2";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "userNotice";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void correct(AbstractAsn1TreeNode node, ASN1Primitive obj, String variableName) {
        if (!isUniversalType(obj, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName(variableName);
        int i = 0;
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode noticeRef = node.getChildAt(i);
            if (isUniversalType(noticeRef, ASN1Sequence.class)) {
                correctNoticeReference(noticeRef);
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            correctDisplayText(node.getChildAt(i), "explicitText");
        }
    }

    /**
     * <pre>
     * NoticeReference ::= SEQUENCE {
     *   organization   DisplayText,
     *   noticeNumbers  SEQUENCE OF INTEGER
     * }
     * </pre>
     */
    private static void correctNoticeReference(AbstractAsn1TreeNode node) {
        node.setRfcFieldName("noticeRef");
        if (node.getChildCount() > 0) {
            correctDisplayText(node.getChildAt(0), "organization");
        }
        if (node.getChildCount() > 1) {
            final AbstractAsn1TreeNode noticeNumbers = node.getChildAt(1);
            if (isUniversalType(noticeNumbers, ASN1Sequence.class)) {
                noticeNumbers.setRfcFieldName("noticeNumbers");
                for (final AbstractAsn1TreeNode noticeNumber : noticeNumbers) {
                    correctPrimitiveUniversalType(noticeNumber, ASN1Integer.class, "noticeNumber");
                }
            }
        }
    }

    /**
     * <pre>
     * DisplayText ::= CHOICE {
     *   ia5String      IA5String       (SIZE (1..200)),
     *   visibleString  VisibleString   (SIZE (1..200)),
     *   bmpString      BMPString       (SIZE (1..200)),
     *   utf8String     UTF8String      (SIZE (1..200))
     * }
     * </pre>
     */
    private static void correctDisplayText(AbstractAsn1TreeNode node, String variableName) {
        if (isUniversalType(node, ASN1IA5String.class)
                || isUniversalType(node, ASN1VisibleString.class)
                || isUniversalType(node, ASN1BMPString.class)
                || isUniversalType(node, ASN1UTF8String.class)) {
            node.setRfcFieldName(variableName);
        }
    }
}
