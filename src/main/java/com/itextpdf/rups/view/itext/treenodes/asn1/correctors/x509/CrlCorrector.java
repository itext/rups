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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector;

import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * Corrector for the CertificateList type, as it is defined in RFC 5280.
 *
 * <pre>
 * CertificateList ::= SEQUENCE {
 *   tbsCertList            TBSCertList,
 *   signatureAlgorithm     AlgorithmIdentifier,
 *   signatureValue         BIT STRING
 * }
 * </pre>
 */
public final class CrlCorrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final CrlCorrector INSTANCE = new CrlCorrector();

    private CrlCorrector() {
        // singleton class
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "crl";
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
            correctTbsCertList(node.getChildAt(0));
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
     * TBSCertList ::= SEQUENCE {
     *   version                Version OPTIONAL,
     *                          -- if present, MUST be v2
     *   signature              AlgorithmIdentifier,
     *   issuer                 Name,
     *   thisUpdate             Time,
     *   nextUpdate             Time OPTIONAL,
     *   revokedCertificates    SEQUENCE OF RevokedCertificate OPTIONAL,
     *   crlExtensions          [0] EXPLICIT Extensions OPTIONAL
     *                          -- if present, version MUST be v2
     * }
     * </pre>
     */
    private static void correctTbsCertList(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("tbsCertList");
        int i = 0;
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode version = node.getChildAt(i);
            if (isUniversalType(version, ASN1Integer.class)) {
                VersionCorrector.INSTANCE.correct(version, "version");
                ++i;
            }
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
            TimeCorrector.INSTANCE.correct(node.getChildAt(i), "thisUpdate");
            ++i;
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode nextUpdate = node.getChildAt(i);
            if (TimeCorrector.isRootTypeValid(nextUpdate.getAsn1Primitive())) {
                TimeCorrector.INSTANCE.correct(nextUpdate, "nextUpdate");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode revokedCertificates = node.getChildAt(i);
            if (isUniversalType(revokedCertificates, ASN1Sequence.class)) {
                revokedCertificates.setRfcFieldName("revokedCertificates");
                for (final AbstractAsn1TreeNode revokedCertificate : revokedCertificates) {
                    correctRevokedCertificate(revokedCertificate);
                }
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode crlExtensions = node.getChildAt(i);
            if (isExplicitContextSpecificType(crlExtensions, 0)) {
                ExtensionsCorrector.INSTANCE.correct(
                        crlExtensions,
                        getBaseObjectUnchecked(crlExtensions),
                        "crlExtensions"
                );
            }
        }
    }

    /**
     * <pre>
     * RevokedCertificate ::= SEQUENCE {
     *   userCertificate        CertificateSerialNumber,
     *   revocationDate         Time,
     *   crlEntryExtensions     Extensions OPTIONAL
     *                          -- if present, version MUST be v2
     * }
     * </pre>
     */
    private static void correctRevokedCertificate(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("revokedCertificate");
        if (node.getChildCount() > 0) {
            CertificateSerialNumberCorrector.INSTANCE.correct(node.getChildAt(0), "userCertificate");
        }
        if (node.getChildCount() > 1) {
            TimeCorrector.INSTANCE.correct(node.getChildAt(1), "revocationDate");
        }
        if (node.getChildCount() > 2) {
            ExtensionsCorrector.INSTANCE.correct(node.getChildAt(2), "crlEntryExtensions");
        }
    }
}
