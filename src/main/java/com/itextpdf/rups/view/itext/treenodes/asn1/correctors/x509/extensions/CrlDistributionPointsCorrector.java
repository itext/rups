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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1BooleanTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TaggedObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.GeneralNamesCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.RelativeDistinguishedNameCorrector;

import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;

/**
 * Corrector for the crlDistributionPoints extension attribute, as it is
 * defined in RFC 5280.
 *
 * <pre>
 * CRLDistributionPoints ::= SEQUENCE SIZE (1..MAX) OF DistributionPoint
 *
 * DistributionPoint ::= SEQUENCE {
 *   distributionPointName  [0] EXPLICIT DistributionPointName OPTIONAL,
 *   reasons                [1] IMPLICIT ReasonFlags OPTIONAL,
 *   cRLIssuer              [2] IMPLICIT GeneralNames OPTIONAL
 * }
 *
 * DistributionPointName ::= CHOICE {
 *   fullName                   [0] IMPLICIT GeneralNames,
 *   nameRelativeToCRLIssuer    [1] IMPLICIT RelativeDistinguishedName
 * }
 *
 * ReasonFlags ::= BIT STRING {
 *   unused                 (0),
 *   keyCompromise          (1),
 *   cACompromise           (2),
 *   affiliationChanged     (3),
 *   superseded             (4),
 *   cessationOfOperation   (5),
 *   certificateHold        (6),
 *   privilegeWithdrawn     (7),
 *   aACompromise           (8)
 * }
 * </pre>
 */
public final class CrlDistributionPointsCorrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final CrlDistributionPointsCorrector INSTANCE = new CrlDistributionPointsCorrector();

    private CrlDistributionPointsCorrector() {
        // singleton class
    }

    /**
     * OBJECT IDENTIFIER for the type, which is handled by the corrector.
     */
    public static final String OID = "2.5.29.31";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "crlDistributionPoints";
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
        for (final AbstractAsn1TreeNode distributionPoint : node) {
            correctDistributionPoint(distributionPoint);
        }
    }

    /**
     * <pre>
     * DistributionPoint ::= SEQUENCE {
     *   distributionPointName  [0] EXPLICIT DistributionPointName OPTIONAL,
     *   reasons                [1] IMPLICIT ReasonFlags OPTIONAL,
     *   cRLIssuer              [2] IMPLICIT GeneralNames OPTIONAL
     * }
     * </pre>
     */
    private static void correctDistributionPoint(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("distributionPoint");
        int i = 0;
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode distributionPoint = node.getChildAt(i);
            if (isExplicitContextSpecificType(distributionPoint, 0)) {
                correctDistributionPointName((Asn1TaggedObjectTreeNode) distributionPoint);
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode reasons = node.getChildAt(i);
            if (isImplicitContextSpecificType(reasons, 1)) {
                correctReasonFlags((Asn1TaggedObjectTreeNode) reasons);
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode issuer = node.getChildAt(i);
            if (isImplicitContextSpecificType(issuer, 2)) {
                final ASN1TaggedObject issuerObj = fixImplicitContextSpecificObject(
                        (Asn1TaggedObjectTreeNode) issuer,
                        ASN1Sequence::getInstance
                );
                GeneralNamesCorrector.INSTANCE.correct(issuer, getBaseObject(issuerObj), "crlIssuer");
            }
        }
    }

    /**
     * <pre>
     * DistributionPointName ::= CHOICE {
     *   fullName                   [0] IMPLICIT GeneralNames,
     *   nameRelativeToCRLIssuer    [1] IMPLICIT RelativeDistinguishedName
     * }
     * </pre>
     */
    private static void correctDistributionPointName(Asn1TaggedObjectTreeNode node) {
        final ASN1Primitive obj = getBaseObject(node);
        if (isImplicitContextSpecificType(obj, 0)) {
            final ASN1TaggedObject newObj = fixImplicitContextSpecificObject(
                    node,
                    (ASN1TaggedObject) obj,
                    ASN1Sequence::getInstance
            );
            GeneralNamesCorrector.INSTANCE.correct(
                    node,
                    getBaseObject(newObj),
                    "distributionPointName"
            );
        } else if (isImplicitContextSpecificType(obj, 1)) {
            final ASN1TaggedObject newObj = fixImplicitContextSpecificObject(
                    node,
                    (ASN1TaggedObject) obj,
                    ASN1Set::getInstance
            );
            RelativeDistinguishedNameCorrector.INSTANCE.correct(
                    node,
                    getBaseObject(newObj),
                    "distributionPointName"
            );
        }
    }

    private static final String[] FLAG_NAMES = {
            "unused",
            "keyCompromise",
            "caCompromise",
            "affiliationChanged",
            "superseded",
            "cessationOfOperation",
            "certificateHold",
            "privilegeWithdrawn",
            "aaCompromise",
    };

    /**
     * <pre>
     * ReasonFlags ::= BIT STRING {
     *   unused                 (0),
     *   keyCompromise          (1),
     *   cACompromise           (2),
     *   affiliationChanged     (3),
     *   superseded             (4),
     *   cessationOfOperation   (5),
     *   certificateHold        (6),
     *   privilegeWithdrawn     (7),
     *   aACompromise           (8)
     * }
     * </pre>
     */
    private static void correctReasonFlags(Asn1TaggedObjectTreeNode node) {
        final ASN1TaggedObject obj =
                fixImplicitContextSpecificObject(node, ASN1BitString::getInstance);
        final ASN1Primitive baseObj = getBaseObject(obj);
        if (!isUniversalType(baseObj, ASN1BitString.class)) {
            return;
        }
        node.setRfcFieldName("reasons");
        // Will add nodes to "decipher" the bit string
        final byte[] flags = ((ASN1BitString) baseObj).getBytes();
        for (int i = 0; i < FLAG_NAMES.length; ++i) {
            node.add(new Asn1BooleanTreeNode(FLAG_NAMES[i], hasFlag(flags, i)));
        }
    }
}
