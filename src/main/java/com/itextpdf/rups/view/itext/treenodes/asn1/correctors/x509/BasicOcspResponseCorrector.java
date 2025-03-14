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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TaggedObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector;

import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Null;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;

/**
 * Corrector for the BasicOCSPResponse type, as it is defined in RFC 6960.
 *
 * <pre>
 * BasicOCSPResponse ::= SEQUENCE {
 *   tbsResponseData        ResponseData,
 *   signatureAlgorithm     AlgorithmIdentifier,
 *   signature              BIT STRING,
 *   certs                  [0] EXPLICIT SEQUENCE OF Certificate OPTIONAL
 * }
 * </pre>
 */
public final class BasicOcspResponseCorrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final BasicOcspResponseCorrector INSTANCE = new BasicOcspResponseCorrector();

    private BasicOcspResponseCorrector() {
        // singleton class
    }

    /**
     * OBJECT IDENTIFIER for the type, which is handled by the corrector.
     */
    public static final String OID = "1.3.6.1.5.5.7.48.1.1";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "basicOCSPResponse";
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
        if (node.getChildCount() > 0) {
            correctResponseData(node.getChildAt(0));
        }
        if (node.getChildCount() > 1) {
            AlgorithmIdentifierCorrector.INSTANCE.correct(node.getChildAt(1), "signatureAlgorithm");
        }
        if (node.getChildCount() > 2) {
            correctPrimitiveUniversalType(node.getChildAt(2), ASN1BitString.class, "signature");
        }
        if (node.getChildCount() > 3) {
            correctCerts(node.getChildAt(3));
        }
    }

    /**
     * <pre>
     * ResponseData ::= SEQUENCE {
     *   version                [0] EXPLICIT Version DEFAULT v1,
     *   responderID            ResponderID,
     *   producedAt             GeneralizedTime,
     *   responses              SEQUENCE OF SingleResponse,
     *   responseExtensions     [1] EXPLICIT Extensions OPTIONAL
     * }
     * </pre>
     */
    private static void correctResponseData(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("tbsResponseData");
        int i = 0;
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode version = node.getChildAt(i);
            if (isExplicitContextSpecificType(version, 0, ASN1Integer.class)) {
                correctVersion((Asn1TaggedObjectTreeNode) version);
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            correctResponderID(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            correctPrimitiveUniversalType(node.getChildAt(i), ASN1GeneralizedTime.class, "producedAt");
            ++i;
        }
        if (node.getChildCount() > i) {
            correctResponses(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode responseExtensions = node.getChildAt(i);
            if (isExplicitContextSpecificType(responseExtensions, 1)) {
                ExtensionsCorrector.INSTANCE.correct(
                        responseExtensions,
                        getBaseObjectUnchecked(responseExtensions),
                        "responseExtensions"
                );
            }
        }
    }

    private static void correctResponses(AbstractAsn1TreeNode responses) {
        if (!isUniversalType(responses, ASN1Sequence.class)) {
            return;
        }
        responses.setRfcFieldName("responses");
        for (final AbstractAsn1TreeNode response : responses) {
            correctSingleResponse(response);
        }
    }

    /**
     * <pre>
     * ResponderID ::= CHOICE {
     *   byName     [1] EXPLICIT Name,
     *   byKey      [2] EXPLICIT KeyHash
     * }
     *
     * KeyHash ::= OCTET STRING -- SHA-1 hash of responder's public key
     *                          -- (excluding the tag and length fields)
     * </pre>
     */
    private static void correctResponderID(AbstractAsn1TreeNode node) {
        if (isExplicitContextSpecificType(node, 1)) {
            NameCorrector.INSTANCE.correct(node, getBaseObjectUnchecked(node), "responderID");
        } else if (isExplicitContextSpecificType(node, 2, ASN1OctetString.class)) {
            node.setRfcFieldName("responderID");
        }
    }

    /**
     * <pre>
     * SingleResponse ::= SEQUENCE {
     *   certID             CertID,
     *   certStatus         CertStatus,
     *   thisUpdate         GeneralizedTime,
     *   nextUpdate         [0] EXPLICIT GeneralizedTime OPTIONAL,
     *   singleExtensions   [1] EXPLICIT Extensions{{re-ocsp-crl |
     *                                               re-ocsp-archive-cutoff |
     *                                               CrlEntryExtensions, ...}} OPTIONAL
     * }
     * </pre>
     */
    private static void correctSingleResponse(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("singleResponse");
        int i = 0;
        if (node.getChildCount() > i) {
            correctCertID(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            correctCertStatus(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            correctPrimitiveUniversalType(node.getChildAt(i), ASN1GeneralizedTime.class, "thisUpdate");
            ++i;
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode nextUpdate = node.getChildAt(i);
            if (isExplicitContextSpecificType(nextUpdate, 0, ASN1GeneralizedTime.class)) {
                nextUpdate.setRfcFieldName("nextUpdate");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode singleExtensions = node.getChildAt(i);
            if (isExplicitContextSpecificType(singleExtensions, 1)) {
                ExtensionsCorrector.INSTANCE.correct(
                        singleExtensions,
                        getBaseObjectUnchecked(singleExtensions),
                        "singleExtensions"
                );
            }
        }
    }

    /**
     * <pre>
     * CertID ::= SEQUENCE {
     *   hashAlgorithm      AlgorithmIdentifier {DIGEST-ALGORITHM, {...}},
     *   issuerNameHash     OCTET STRING, -- Hash of issuer's DN
     *   issuerKeyHash      OCTET STRING, -- Hash of issuer's public key
     *   serialNumber       CertificateSerialNumber
     * }
     * </pre>
     */
    private static void correctCertID(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("certID");
        if (node.getChildCount() > 0) {
            AlgorithmIdentifierCorrector.INSTANCE.correct(node.getChildAt(0), "hashAlgorithm");
        }
        if (node.getChildCount() > 1) {
            correctPrimitiveUniversalType(node.getChildAt(1), ASN1OctetString.class, "issuerNameHash");
        }
        if (node.getChildCount() > 2) {
            correctPrimitiveUniversalType(node.getChildAt(2), ASN1OctetString.class, "issuerKeyHash");
        }
        if (node.getChildCount() > 3) {
            CertificateSerialNumberCorrector.INSTANCE.correct(node.getChildAt(3), "serialNumber");
        }
    }

    /**
     * <pre>
     * CertStatus ::= CHOICE {
     *   good       [0] IMPLICIT NULL,
     *   revoked    [1] IMPLICIT RevokedInfo,
     *   unknown    [2] IMPLICIT UnknownInfo
     * }
     *
     * UnknownInfo ::= NULL
     * </pre>
     */
    private static void correctCertStatus(AbstractAsn1TreeNode node) {
        // We will use value explanation for choice, since there are 2 nulls
        if (isImplicitContextSpecificType(node, 0)) {
            correctNullCertStatus((Asn1TaggedObjectTreeNode) node, "good");
        } else if (isImplicitContextSpecificType(node, 1)) {
            correctRevokedInfo((Asn1TaggedObjectTreeNode) node);
        } else if (isImplicitContextSpecificType(node, 2)) {
            correctNullCertStatus((Asn1TaggedObjectTreeNode) node, "unknown");
        }
    }

    private static void correctNullCertStatus(Asn1TaggedObjectTreeNode node, String valueExplanation) {
        final ASN1TaggedObject obj = fixImplicitContextSpecificObject(node, ASN1Null::getInstance);
        if (!isUniversalType(getBaseObject(obj), ASN1Null.class)) {
            return;
        }
        node.setRfcFieldName("certStatus");
        node.setValueExplanation(valueExplanation);
    }

    /**
     * <pre>
     * RevokedInfo ::= SEQUENCE {
     *   revocationTime     GeneralizedTime,
     *   revocationReason   [0] EXPLICIT CRLReason OPTIONAL
     * }
     * </pre>
     */
    private static void correctRevokedInfo(Asn1TaggedObjectTreeNode node) {
        final ASN1TaggedObject obj =
                fixImplicitContextSpecificObject(node, ASN1Sequence::getInstance);
        if (!isUniversalType(getBaseObject(obj), ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("certStatus");
        if (node.getChildCount() > 0) {
            correctPrimitiveUniversalType(node.getChildAt(0), ASN1GeneralizedTime.class, "revocationTime");
        }
        if (node.getChildCount() > 1) {
            final AbstractAsn1TreeNode revocationReason = node.getChildAt(1);
            if (isExplicitContextSpecificType(revocationReason, 0)) {
                CrlReasonCorrector.INSTANCE.correct(
                        revocationReason,
                        getBaseObjectUnchecked(revocationReason),
                        "revocationReason"
                );
            }
        }
    }

    /**
     * <pre>
     * Version ::= INTEGER { v1(0) }
     * </pre>
     */
    private static void correctVersion(Asn1TaggedObjectTreeNode node) {
        node.setRfcFieldName("version");
        final ASN1Integer nodeValue = (ASN1Integer) getBaseObject(node);
        if (nodeValue.hasValue(0)) {
            node.setValueExplanation("v1");
        }
    }

    private static void correctCerts(AbstractAsn1TreeNode node) {
        if (!isExplicitContextSpecificType(node, 0, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("certs");
        for (final AbstractAsn1TreeNode cert : node) {
            CertificateCorrector.INSTANCE.correct(cert);
        }
    }
}
