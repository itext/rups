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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TaggedObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.AlgorithmIdentifierCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.CertificateCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.CertificateSerialNumberCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.CrlCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.NameCorrector;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1Integer;
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
 * CMSVersion ::= INTEGER { v0(0), v1(1), v2(2), v3(3), v4(4), v5(5) }
 *
 * DigestAlgorithmIdentifiers ::= SET OF DigestAlgorithmIdentifier
 *
 * DigestAlgorithmIdentifier ::= AlgorithmIdentifier
 *
 * EncapsulatedContentInfo ::= SEQUENCE {
 *   eContentType   ContentType,
 *   eContent       [0] EXPLICIT OCTET STRING OPTIONAL
 * }
 *
 * ContentType ::= OBJECT IDENTIFIER
 *
 * CertificateSet ::= SET OF CertificateChoices
 *
 * CertificateChoices ::= CHOICE {
 *   certificate            Certificate,
 *   extendedCertificate    [0] IMPLICIT ExtendedCertificate,       -- Obsolete
 *   v1AttrCert             [1] IMPLICIT AttributeCertificateV1,    -- Obsolete
 *   v2AttrCert             [2] IMPLICIT AttributeCertificateV2,
 *   other                  [3] IMPLICIT OtherCertificateFormat
 * }
 *
 * OtherCertificateFormat ::= SEQUENCE {
 *   otherCertFormat    OBJECT IDENTIFIER,
 *   otherCert          ANY DEFINED BY otherCertFormat
 * }
 *
 * RevocationInfoChoices ::= SET OF RevocationInfoChoice
 *
 * RevocationInfoChoice ::= CHOICE {
 *   crl CertificateList,
 *   other [1] IMPLICIT OtherRevocationInfoFormat
 * }
 *
 * OtherRevocationInfoFormat ::= SEQUENCE {
 *   otherRevInfoFormat     OBJECT IDENTIFIER,
 *   otherRevInfo           ANY DEFINED BY otherRevInfoFormat
 * }
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
 * Attribute ::= SEQUENCE {
 *   attrType       OBJECT IDENTIFIER,
 *   attrValues     SET OF AttributeValue
 * }
 *
 * AttributeValue ::= ANY
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
            correctCmsVersion(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            correctDigestAlgorithms(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            correctEncapContentInfo(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode certificates = node.getChildAt(i);
            if (isImplicitContextSpecificType(certificates, 0)) {
                correctCertificates((Asn1TaggedObjectTreeNode) certificates);
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode crls = node.getChildAt(i);
            if (isImplicitContextSpecificType(crls, 1)) {
                correctCrls((Asn1TaggedObjectTreeNode) crls);
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            correctSignerInfos(node.getChildAt(i));
        }
    }

    private static final String[] CMS_VERSION_LABELS = {"v0", "v1", "v2", "v3", "v4", "v5"};

    /**
     * <pre>
     * CMSVersion ::= INTEGER { v0(0), v1(1), v2(2), v3(3), v4(4), v5(5) }
     * </pre>
     */
    private static void correctCmsVersion(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Integer.class)) {
            return;
        }
        node.setRfcFieldName("version");
        final BigInteger nodeValue = ((ASN1Integer) node.getAsn1Primitive()).getValue();
        if (isNumberInRange(nodeValue, CMS_VERSION_LABELS.length)) {
            node.setValueExplanation(CMS_VERSION_LABELS[nodeValue.intValue()]);
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
     * EncapsulatedContentInfo ::= SEQUENCE {
     *   eContentType   ContentType,
     *   eContent       [0] EXPLICIT OCTET STRING OPTIONAL
     * }
     *
     * ContentType ::= OBJECT IDENTIFIER
     * </pre>
     */
    private static void correctEncapContentInfo(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("encapContentInfo");
        String oid = null;
        if (node.getChildCount() > 0) {
            oid = correctUniversalObjectIdentifier(node.getChildAt(0), "eContentType");
        }
        if (node.getChildCount() > 1) {
            final AbstractAsn1TreeNode eContent = node.getChildAt(1);
            if (isExplicitContextSpecificType(eContent, 0, ASN1OctetString.class)) {
                eContent.setRfcFieldName("eContent");
                /*
                 * If there is a corrector for type, then we assume DER-encoded
                 * value us stored.
                 */
                final AbstractCorrector contentCorrector = OidCorrectorMapper.get(oid);
                if (contentCorrector instanceof DefaultCorrector) {
                    return;
                }
                final AbstractAsn1TreeNode child = Asn1TreeNodeFactory.fromPrimitive(
                        ((ASN1OctetString) getBaseObjectUnchecked(eContent)).getOctets()
                );
                if (child != null) {
                    contentCorrector.correct(child);
                    eContent.add(child);
                }
            }
        }
    }

    /**
     * <pre>
     * CertificateSet ::= SET OF CertificateChoices
     * </pre>
     */
    private static void correctCertificates(Asn1TaggedObjectTreeNode node) {
        final ASN1TaggedObject obj = fixImplicitContextSpecificObject(node, ASN1Set::getInstance);
        if (!isUniversalType(getBaseObject(obj), ASN1Set.class)) {
            return;
        }
        node.setRfcFieldName("certificates");
        for (final AbstractAsn1TreeNode cert : node) {
            correctCertificateChoices(cert);
        }
    }

    /**
     * <pre>
     * CertificateChoices ::= CHOICE {
     *   certificate            Certificate,
     *   extendedCertificate    [0] IMPLICIT ExtendedCertificate,       -- Obsolete
     *   v1AttrCert             [1] IMPLICIT AttributeCertificateV1,    -- Obsolete
     *   v2AttrCert             [2] IMPLICIT AttributeCertificateV2,
     *   other                  [3] IMPLICIT OtherCertificateFormat
     * }
     * </pre>
     */
    private static void correctCertificateChoices(AbstractAsn1TreeNode node) {
        if (isUniversalType(node)) {
            CertificateCorrector.INSTANCE.correct(node, "certificate");
        } else if (isImplicitContextSpecificType(node, 0)) {
            /*
             * Even though ExtendedCertificate type is pretty small, I
             * have not found any example usage of them. Also, it is
             * obsolete in CMS and was just taken from PKCS #6 for
             * backwards compatibility. Might as well not waste time on
             * this.
             */
            correctImplicitSequenceNode((Asn1TaggedObjectTreeNode) node, "extendedCertificate");
        } else if (isImplicitContextSpecificType(node, 1)) {
            /*
             * Same as with ExtendedCertificate. Did not manage to find it
             * in the wild, and it is obsolete anyway, so might as well
             * not waste time here either.
             */
            correctImplicitSequenceNode((Asn1TaggedObjectTreeNode) node, "v1AttrCert");
        } else if (isImplicitContextSpecificType(node, 2)) {
            /*
             * This one is not obsolete, but the X.509 Attribute
             * Certificate type is pretty big, and since everybody just
             * uses X.509 Public-Key Certificates anyway, it seems wasteful
             * to support this at the moment.
             */
            correctImplicitSequenceNode((Asn1TaggedObjectTreeNode) node, "v2AttrCert");
        } else if (isImplicitContextSpecificType(node, 3)) {
            correctOtherCertificateFormat((Asn1TaggedObjectTreeNode) node);
        }
    }

    /**
     * <pre>
     * OtherCertificateFormat ::= SEQUENCE {
     *   otherCertFormat    OBJECT IDENTIFIER,
     *   otherCert          ANY DEFINED BY otherCertFormat
     * }
     * </pre>
     */
    private static void correctOtherCertificateFormat(Asn1TaggedObjectTreeNode node) {
        final ASN1TaggedObject obj = fixImplicitContextSpecificObject(node, ASN1Sequence::getInstance);
        if (!isUniversalType(getBaseObject(obj), ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("other");
        String oid = null;
        if (node.getChildCount() > 0) {
            oid = correctUniversalObjectIdentifier(node.getChildAt(0), "otherCertFormat");
        }
        if (node.getChildCount() > 1) {
            OidCorrectorMapper.get(oid).correct(node.getChildAt(1), "otherCert");
        }
    }

    /**
     * <pre>
     * RevocationInfoChoices ::= SET OF RevocationInfoChoice
     * </pre>
     */
    private static void correctCrls(Asn1TaggedObjectTreeNode node) {
        final ASN1TaggedObject obj = fixImplicitContextSpecificObject(node, ASN1Set::getInstance);
        if (!isUniversalType(getBaseObject(obj), ASN1Set.class)) {
            return;
        }
        node.setRfcFieldName("crls");
        for (final AbstractAsn1TreeNode revocationInfoChoice : node) {
            correctRevocationInfoChoice(revocationInfoChoice);
        }
    }

    /**
     * <pre>
     * RevocationInfoChoice ::= CHOICE {
     *   crl CertificateList,
     *   other [1] IMPLICIT OtherRevocationInfoFormat
     * }
     * </pre>
     */
    private static void correctRevocationInfoChoice(AbstractAsn1TreeNode node) {
        if (isUniversalType(node)) {
            CrlCorrector.INSTANCE.correct(node);
        } else if (isImplicitContextSpecificType(node, 1)) {
            correctOtherRevocationInfoFormat((Asn1TaggedObjectTreeNode) node);
        }
    }

    /**
     * <pre>
     * OtherRevocationInfoFormat ::= SEQUENCE {
     *   otherRevInfoFormat     OBJECT IDENTIFIER,
     *   otherRevInfo           ANY DEFINED BY otherRevInfoFormat
     * }
     * </pre>
     */
    private static void correctOtherRevocationInfoFormat(Asn1TaggedObjectTreeNode node) {
        final ASN1TaggedObject obj = fixImplicitContextSpecificObject(node, ASN1Sequence::getInstance);
        if (!isUniversalType(getBaseObject(obj), ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("other");
        String oid = null;
        if (node.getChildCount() > 0) {
            oid = correctUniversalObjectIdentifier(node.getChildAt(0), "otherRevInfoFormat");
        }
        if (node.getChildCount() > 1) {
            OidCorrectorMapper.get(oid).correct(node.getChildAt(1), "otherRevInfo");
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
            correctCmsVersion(node.getChildAt(i));
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
                correctAttributes((Asn1TaggedObjectTreeNode) signedAttrs, "signedAttrs");
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
                correctAttributes((Asn1TaggedObjectTreeNode) unsignedAttrs, "unsignedAttrs");
            }
        }
    }

    /**
     * <pre>
     * Attributes ::= SET SIZE (1..MAX) OF Attribute
     * </pre>
     */
    private static void correctAttributes(Asn1TaggedObjectTreeNode node, String variableName) {
        final ASN1TaggedObject obj = fixImplicitContextSpecificObject(node, ASN1Set::getInstance);
        if (!isUniversalType(getBaseObject(obj), ASN1Set.class)) {
            return;
        }
        node.setRfcFieldName(variableName);
        for (final AbstractAsn1TreeNode attribute : node) {
            correctAttribute(attribute);
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

    /**
     * <pre>
     * Attribute ::= SEQUENCE {
     *   attrType       OBJECT IDENTIFIER,
     *   attrValues     SET OF AttributeValue
     * }
     *
     * AttributeValue ::= ANY
     * </pre>
     */
    private static void correctAttribute(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("attribute");
        String oid = null;
        if (node.getChildCount() > 0) {
            oid = correctUniversalObjectIdentifier(node.getChildAt(0), "attrType");
        }
        if (node.getChildCount() > 1) {
            final AbstractAsn1TreeNode attrValues = node.getChildAt(1);
            if (isUniversalType(attrValues, ASN1Set.class)) {
                attrValues.setRfcFieldName("attrValues");
                final AbstractCorrector corrector = OidCorrectorMapper.get(oid);
                for (final AbstractAsn1TreeNode attribute : attrValues) {
                    corrector.correct(attribute);
                }
            }
        }
    }

    private static void correctImplicitSequenceNode(Asn1TaggedObjectTreeNode node, String variableName) {
        final ASN1TaggedObject obj = fixImplicitContextSpecificObject(node, ASN1Sequence::getInstance);
        if (!isUniversalType(getBaseObject(obj), ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName(variableName);
    }
}
