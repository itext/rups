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
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;

/**
 * Corrector for the Certificate type, as it is defined in RFC 5280.
 *
 * <pre>
 * Certificate ::= SEQUENCE {
 *   tbsCertificate         TBSCertificate,
 *   signatureAlgorithm     AlgorithmIdentifier,
 *   signatureValue         BIT STRING
 * }
 * </pre>
 */
public final class CertificateCorrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final CertificateCorrector INSTANCE = new CertificateCorrector();

    private CertificateCorrector() {
        // singleton class
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "certificate";
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
            correctTbsCertificate(node.getChildAt(0));
        }
        if (node.getChildCount() > 1) {
            AlgorithmIdentifierCorrector.INSTANCE.correct(node.getChildAt(1), "signatureAlgorithm");
        }
        if (node.getChildCount() > 2) {
            correctPrimitiveUniversalType(node.getChildAt(2), ASN1BitString.class, "signatureValue");
        }
    }

    /**
     * <pre>
     * TBSCertificate ::= SEQUENCE {
     *   version                [0] EXPLICIT Version DEFAULT v1,
     *   serialNumber           CertificateSerialNumber,
     *   signature              AlgorithmIdentifier,
     *   issuer                 Name,
     *   validity               Validity,
     *   subject                Name,
     *   subjectPublicKeyInfo   SubjectPublicKeyInfo,
     *   issuerUniqueID         [1] IMPLICIT UniqueIdentifier OPTIONAL,
     *   subjectUniqueID        [2] IMPLICIT UniqueIdentifier OPTIONAL,
     *   extensions             [3] EXPLICIT Extensions OPTIONAL
     * }
     * </pre>
     */
    private static void correctTbsCertificate(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("tbsCertificate");
        int i = 0;
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode version = node.getChildAt(i);
            if (isExplicitContextSpecificType(version, 0)) {
                VersionCorrector.INSTANCE.correct(version, getBaseObjectUnchecked(version), "version");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            CertificateSerialNumberCorrector.INSTANCE.correct(node.getChildAt(i), "serialNumber");
            ++i;
        }
        if (node.getChildCount() > i) {
            AlgorithmIdentifierCorrector.INSTANCE.correct(node.getChildAt(i), "signature");
            ++i;
        }
        if (node.getChildCount() > i) {
            NameCorrector.INSTANCE.correct(node.getChildAt(i), "issuer");
            ++i;
        }
        if (node.getChildCount() > i) {
            correctValidity(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            NameCorrector.INSTANCE.correct(node.getChildAt(i), "subject");
            ++i;
        }
        if (node.getChildCount() > i) {
            correctSubjectPublicKeyInfo(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode issuerUniqueID = node.getChildAt(i);
            if (isImplicitContextSpecificType(issuerUniqueID, 1)) {
                correctUniqueId((Asn1TaggedObjectTreeNode) issuerUniqueID, "issuerUniqueID");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode subjectUniqueID = node.getChildAt(i);
            if (isImplicitContextSpecificType(subjectUniqueID, 2)) {
                correctUniqueId((Asn1TaggedObjectTreeNode) subjectUniqueID, "subjectUniqueID");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode extensions = node.getChildAt(i);
            if (isExplicitContextSpecificType(extensions, 3)) {
                ExtensionsCorrector.INSTANCE.correct(
                        extensions,
                        getBaseObjectUnchecked(extensions),
                        "extensions"
                );
            }
        }
    }

    /**
     * <pre>
     * Validity ::= SEQUENCE {
     *   notBefore  Time,
     *   notAfter   Time
     * }
     * </pre>
     */
    private static void correctValidity(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("validity");
        if (node.getChildCount() > 0) {
            TimeCorrector.INSTANCE.correct(node.getChildAt(0), "notBefore");
        }
        if (node.getChildCount() > 1) {
            TimeCorrector.INSTANCE.correct(node.getChildAt(1), "notAfter");
        }
    }

    /**
     * <pre>
     * SubjectPublicKeyInfo ::= SEQUENCE {
     *   algorithm          AlgorithmIdentifier,
     *   subjectPublicKey   BIT STRING
     * }
     * </pre>
     */
    private static void correctSubjectPublicKeyInfo(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("subjectPublicKeyInfo");
        if (node.getChildCount() > 0) {
            AlgorithmIdentifierCorrector.INSTANCE.correct(node.getChildAt(0), "algorithm");
        }
        if (node.getChildCount() > 1) {
            correctPrimitiveUniversalType(node.getChildAt(1), ASN1BitString.class, "subjectPublicKey");
        }
    }

    private static void correctUniqueId(Asn1TaggedObjectTreeNode node, String variableName) {
        final ASN1TaggedObject obj =
                fixImplicitContextSpecificObject(node, ASN1BitString::getInstance);
        if (!isUniversalType(getBaseObject(obj), ASN1BitString.class)) {
            return;
        }
        node.setRfcFieldName(variableName);
    }
}
