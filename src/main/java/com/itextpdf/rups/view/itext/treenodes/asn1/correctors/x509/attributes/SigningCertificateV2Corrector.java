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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.AlgorithmIdentifierCorrector;

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * Corrector for the signingCertificateV2 attribute, as it is defined in RFC 5035.
 *
 * <pre>
 * SigningCertificateV2 ::= SEQUENCE {
 *   certs      SEQUENCE OF ESSCertIDv2,
 *   policies   SEQUENCE OF PolicyInformation OPTIONAL
 * }
 * </pre>
 */
public final class SigningCertificateV2Corrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final SigningCertificateV2Corrector INSTANCE = new SigningCertificateV2Corrector();

    private SigningCertificateV2Corrector() {
        // singleton class
    }

    /**
     * OBJECT IDENTIFIER for the type, which is handled by the corrector.
     */
    public static final String OID = "1.2.840.113549.1.9.16.2.47";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "signingCertificateV2";
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
            final AbstractAsn1TreeNode certs = node.getChildAt(0);
            if (isUniversalType(certs, ASN1Sequence.class)) {
                certs.setRfcFieldName("certs");
                for (final AbstractAsn1TreeNode cert : certs) {
                    correctEssCertIdv2(cert);
                }
            }
        }
        if (node.getChildCount() > 1) {
            SigningCertificateCorrector.correctPolicies(node.getChildAt(1));
        }
    }

    /**
     * <pre>
     * ESSCertIDv2 ::= SEQUENCE {
     *   hashAlgorithm  AlgorithmIdentifier DEFAULT {algorithm id-sha256},
     *   certHash       Hash,
     *   issuerSerial   IssuerSerial OPTIONAL
     * }
     * </pre>
     */
    private static void correctEssCertIdv2(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("essCertIDv2");
        int i = 0;
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode hashAlgorithm = node.getChildAt(i);
            if (AlgorithmIdentifierCorrector.isRootTypeValid(hashAlgorithm.getAsn1Primitive())) {
                AlgorithmIdentifierCorrector.INSTANCE.correct(hashAlgorithm, "hashAlgorithm");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            SigningCertificateCorrector.correctHash(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            SigningCertificateCorrector.correctIssuerSerial(node.getChildAt(i));
        }
    }
}
