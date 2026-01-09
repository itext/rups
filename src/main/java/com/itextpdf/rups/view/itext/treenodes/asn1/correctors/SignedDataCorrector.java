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
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types.CertificateSetCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types.CmsVersionCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types.EncapsulatedContentInfoCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types.RevocationInfoChoicesCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types.SetOfAttributeCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.AlgorithmIdentifierCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.CertificateSerialNumberCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.NameCorrector;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;

/**
 * Corrector for the SignedData type, as it is defined in RFC 5652.
 *
 * <pre>
 * SignedData ::= SEQUENCE {
 *   version            CMSVersion,
 *   digestAlgorithms   DigestAlgorithmIdentifiers,
 *   encapContentInfo   EncapsulatedContentInfo,
 *   certificates       [0] IMPLICIT CertificateSet OPTIONAL,
 *   crls               [1] IMPLICIT RevocationInfoChoices OPTIONAL,
 *   signerInfos        SignerInfos
 * }
 *
 * DigestAlgorithmIdentifiers ::= SET OF DigestAlgorithmIdentifier
 *
 * DigestAlgorithmIdentifier ::= AlgorithmIdentifier
 *
 * SignerInfos ::= SET OF SignerInfo
 *
 * SignerInfo ::= SEQUENCE {
 *   version                CMSVersion,
 *   sid                    SignerIdentifier,
 *   digestAlgorithm        DigestAlgorithmIdentifier,
 *   signedAttrs            [0] IMPLICIT SignedAttributes OPTIONAL,
 *   signatureAlgorithm     SignatureAlgorithmIdentifier,
 *   signature              SignatureValue,
 *   unsignedAttrs          [1] IMPLICIT UnsignedAttributes OPTIONAL
 * }
 *
 * SignerIdentifier ::= CHOICE {
 *   issuerAndSerialNumber  IssuerAndSerialNumber,
 *   subjectKeyIdentifier   [0] IMPLICIT SubjectKeyIdentifier
 * }
 *
 * IssuerAndSerialNumber ::= SEQUENCE {
 *   issuer Name,
 *   serialNumber CertificateSerialNumber
 * }
 *
 * SubjectKeyIdentifier ::= OCTET STRING.
 *
 * SignedAttributes ::= SET SIZE (1..MAX) OF Attribute
 *
 * SignatureAlgorithmIdentifier ::= AlgorithmIdentifier
 *
 * SignatureValue ::= OCTET STRING
 *
 * UnsignedAttributes ::= SET SIZE (1..MAX) OF Attribute
 * </pre>
 *
 * <p><tt>ExtendedCertificate</tt>, <tt>AttributeCertificateV1</tt> and
 * <tt>AttributeCertificateV2</tt> correction is not implemented and those
 * subtrees will remain untouched (with the exception of the subtree root node
 * name)</p>
 */
public final class SignedDataCorrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final SignedDataCorrector INSTANCE = new SignedDataCorrector();

    private SignedDataCorrector() {
        // singleton class
    }

    /**
     * OBJECT IDENTIFIER for the type, which is handled by the corrector.
     */
    public static final String OID = "1.2.840.113549.1.7.2";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "signedData";
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
            CmsVersionCorrector.INSTANCE.correct(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            correctDigestAlgorithms(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            EncapsulatedContentInfoCorrector.INSTANCE.correct(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode certificates = node.getChildAt(i);
            if (isImplicitContextSpecificType(certificates, 0)) {
                final ASN1TaggedObject certificatesObj = fixImplicitContextSpecificObject(
                        (Asn1TaggedObjectTreeNode) certificates,
                        ASN1Set::getInstance
                );
                CertificateSetCorrector.INSTANCE.correct(
                        certificates, getBaseObject(certificatesObj), "certificates"
                );
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode crls = node.getChildAt(i);
            if (isImplicitContextSpecificType(crls, 1)) {
                final ASN1TaggedObject crlsObj = fixImplicitContextSpecificObject(
                        (Asn1TaggedObjectTreeNode) crls,
                        ASN1Set::getInstance
                );
                RevocationInfoChoicesCorrector.INSTANCE.correct(crls, getBaseObject(crlsObj));
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            correctSignerInfos(node.getChildAt(i));
        }
    }

    /**
     * <pre>
     * DigestAlgorithmIdentifiers ::= SET OF DigestAlgorithmIdentifier
     *
     * DigestAlgorithmIdentifier ::= AlgorithmIdentifier
     * </pre>
     */
    private static void correctDigestAlgorithms(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Set.class)) {
            return;
        }
        node.setRfcFieldName("digestAlgorithms");
        for (final AbstractAsn1TreeNode algorithmIdentifier : node) {
            AlgorithmIdentifierCorrector.INSTANCE.correct(algorithmIdentifier, "digestAlgorithm");
        }
    }

    /**
     * <pre>
     * SignerInfos ::= SET OF SignerInfo
     * </pre>
     */
    private static void correctSignerInfos(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Set.class)) {
            return;
        }
        node.setRfcFieldName("signerInfos");
        for (final AbstractAsn1TreeNode signerInfo : node) {
            correctSignerInfo(signerInfo);
        }
    }

    /**
     * <pre>
     * SignerInfo ::= SEQUENCE {
     *   version                CMSVersion,
     *   sid                    SignerIdentifier,
     *   digestAlgorithm        DigestAlgorithmIdentifier,
     *   signedAttrs            [0] IMPLICIT SignedAttributes OPTIONAL,
     *   signatureAlgorithm     SignatureAlgorithmIdentifier,
     *   signature              SignatureValue,
     *   unsignedAttrs          [1] IMPLICIT UnsignedAttributes OPTIONAL
     * }
     * </pre>
     */
    private static void correctSignerInfo(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("signerInfo");
        int i = 0;
        if (node.getChildCount() > i) {
            CmsVersionCorrector.INSTANCE.correct(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            correctSignerIdentifier(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            AlgorithmIdentifierCorrector.INSTANCE.correct(node.getChildAt(i), "digestAlgorithm");
            ++i;
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode signedAttrs = node.getChildAt(i);
            if (isImplicitContextSpecificType(signedAttrs, 0)) {
                final ASN1TaggedObject signedAttrsObj = fixImplicitContextSpecificObject(
                        (Asn1TaggedObjectTreeNode) signedAttrs, ASN1Set::getInstance
                );
                SetOfAttributeCorrector.INSTANCE.correct(
                        signedAttrs,
                        getBaseObject(signedAttrsObj),
                        "signedAttrs"
                );
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            AlgorithmIdentifierCorrector.INSTANCE.correct(node.getChildAt(i), "signatureAlgorithm");
            ++i;
        }
        if (node.getChildCount() > i) {
            correctPrimitiveUniversalType(node.getChildAt(i), ASN1OctetString.class, "signature");
            ++i;
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode unsignedAttrs = node.getChildAt(i);
            if (isImplicitContextSpecificType(unsignedAttrs, 1)) {
                final ASN1TaggedObject unsignedAttrsObj = fixImplicitContextSpecificObject(
                        (Asn1TaggedObjectTreeNode) unsignedAttrs, ASN1Set::getInstance
                );
                SetOfAttributeCorrector.INSTANCE.correct(
                        unsignedAttrs,
                        getBaseObject(unsignedAttrsObj),
                        "unsignedAttrs"
                );
            }
        }
    }

    /**
     * <pre>
     * SignerIdentifier ::= CHOICE {
     *   issuerAndSerialNumber  IssuerAndSerialNumber,
     *   subjectKeyIdentifier   [0] IMPLICIT SubjectKeyIdentifier
     * }
     *
     * SubjectKeyIdentifier ::= OCTET STRING.
     * </pre>
     */
    private static void correctSignerIdentifier(AbstractAsn1TreeNode node) {
        if (isUniversalType(node, ASN1Sequence.class)) {
            correctIssuerAndSerialNumber(node);
        } else if (isImplicitContextSpecificType(node, 0)) {
            final ASN1TaggedObject obj = fixImplicitContextSpecificObject(
                    (Asn1TaggedObjectTreeNode) node,
                    ASN1OctetString::getInstance
            );
            if (isUniversalType(getBaseObject(obj), ASN1OctetString.class)) {
                node.setRfcFieldName("sid");
            }
        }
    }

    /**
     * <pre>
     * IssuerAndSerialNumber ::= SEQUENCE {
     *   issuer Name,
     *   serialNumber CertificateSerialNumber
     * }
     * </pre>
     */
    private static void correctIssuerAndSerialNumber(AbstractAsn1TreeNode node) {
        node.setRfcFieldName("sid");
        if (node.getChildCount() > 0) {
            NameCorrector.INSTANCE.correct(node.getChildAt(0), "issuer");
        }
        if (node.getChildCount() > 1) {
            CertificateSerialNumberCorrector.INSTANCE.correct(node.getChildAt(1), "serialNumber");
        }
    }
}
