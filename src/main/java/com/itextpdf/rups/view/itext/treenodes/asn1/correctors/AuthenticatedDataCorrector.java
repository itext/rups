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
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TaggedObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types.CmsVersionCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types.EncapsulatedContentInfoCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types.OriginatorInfoCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types.RecipientInfosCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types.SetOfAttributeCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.AlgorithmIdentifierCorrector;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;

/**
 * Corrector for the AuthenticatedData type, as it is defined in RFC 5652.
 *
 * <pre>
 * AuthenticatedData ::= SEQUENCE {
 *   version            CMSVersion,
 *   originatorInfo     [0] IMPLICIT OriginatorInfo OPTIONAL,
 *   recipientInfos     RecipientInfos,
 *   macAlgorithm       MessageAuthenticationCodeAlgorithm,
 *   digestAlgorithm    [1] IMPLICIT DigestAlgorithmIdentifier OPTIONAL,
 *   encapContentInfo   EncapsulatedContentInfo,
 *   authAttrs          [2] IMPLICIT AuthAttributes OPTIONAL,
 *   mac                MessageAuthenticationCode,
 *   unauthAttrs        [3] IMPLICIT UnauthAttributes OPTIONAL
 * }
 *
 * MessageAuthenticationCodeAlgorithm ::= AlgorithmIdentifier
 *
 * DigestAlgorithmIdentifier ::= AlgorithmIdentifier
 *
 * AuthAttributes ::= SET SIZE (1..MAX) OF Attribute
 *
 * MessageAuthenticationCode ::= OCTET STRING
 *
 * UnauthAttributes ::= SET SIZE (1..MAX) OF Attribute
 * </pre>
 */
public final class AuthenticatedDataCorrector extends AbstractSequenceCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final AuthenticatedDataCorrector INSTANCE = new AuthenticatedDataCorrector();

    private AuthenticatedDataCorrector() {
        // singleton class
    }

    /**
     * OBJECT IDENTIFIER for the type, which is handled by the corrector.
     */
    public static final String OID = "1.2.840.113549.1.9.16.1.2";

    /**
     * Static array of field correctors for the root sequence.
     */
    private static final SequenceFieldCorrector[] FIELD_CORRECTORS = {
            AuthenticatedDataCorrector::correctVersion,
            AuthenticatedDataCorrector::correctOriginatorInfo,
            AuthenticatedDataCorrector::correctRecipientInfos,
            AuthenticatedDataCorrector::correctMacAlgorithm,
            AuthenticatedDataCorrector::correctDigestAlgorithm,
            AuthenticatedDataCorrector::correctEncapContentInfo,
            AuthenticatedDataCorrector::correctAuthAttrs,
            AuthenticatedDataCorrector::correctMac,
            AuthenticatedDataCorrector::correctUnauthAttrs
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "authData";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SequenceFieldCorrector[] getFieldCorrectors() {
        return FIELD_CORRECTORS;
    }

    private static boolean correctVersion(AbstractAsn1TreeNode node) {
        CmsVersionCorrector.INSTANCE.correct(node, "version");
        return true;
    }

    private static boolean correctOriginatorInfo(AbstractAsn1TreeNode node) {
        if (!isImplicitContextSpecificType(node, 0)) {
            return false;
        }
        final ASN1TaggedObject obj = fixImplicitContextSpecificObject(
                (Asn1TaggedObjectTreeNode) node, ASN1Sequence::getInstance
        );
        OriginatorInfoCorrector.INSTANCE.correct(node, getBaseObject(obj));
        return true;
    }

    private static boolean correctRecipientInfos(AbstractAsn1TreeNode node) {
        RecipientInfosCorrector.INSTANCE.correct(node);
        return true;
    }

    private static boolean correctMacAlgorithm(AbstractAsn1TreeNode node) {
        AlgorithmIdentifierCorrector.INSTANCE.correct(node, "macAlgorithm");
        return true;
    }

    private static boolean correctDigestAlgorithm(AbstractAsn1TreeNode node) {
        if (!isImplicitContextSpecificType(node, 1)) {
            return false;
        }
        final ASN1TaggedObject obj = fixImplicitContextSpecificObject(
                (Asn1TaggedObjectTreeNode) node, ASN1Sequence::getInstance
        );
        AlgorithmIdentifierCorrector.INSTANCE.correct(
                node, getBaseObject(obj), "digestAlgorithm"
        );
        return true;
    }

    private static boolean correctEncapContentInfo(AbstractAsn1TreeNode node) {
        EncapsulatedContentInfoCorrector.INSTANCE.correct(node);
        return true;
    }

    private static boolean correctAuthAttrs(AbstractAsn1TreeNode node) {
        return correctAttrs(node, 2, "authAttrs");
    }

    private static boolean correctMac(AbstractAsn1TreeNode node) {
        correctPrimitiveUniversalType(node, ASN1OctetString.class, "mac");
        return true;
    }

    private static boolean correctUnauthAttrs(AbstractAsn1TreeNode node) {
        return correctAttrs(node, 3, "unauthAttrs");
    }

    private static boolean correctAttrs(AbstractAsn1TreeNode node, int tagNo, String variableName) {
        if (!isImplicitContextSpecificType(node, tagNo)) {
            return false;
        }
        final ASN1TaggedObject obj = fixImplicitContextSpecificObject(
                (Asn1TaggedObjectTreeNode) node, ASN1Set::getInstance
        );
        SetOfAttributeCorrector.INSTANCE.correct(node, getBaseObject(obj), variableName);
        return true;
    }
}
