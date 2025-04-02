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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors;

import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.algorithms.Mgf1Corrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.algorithms.RsassaPssCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.BasicOcspResponseCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.TimeStampTokenInfoCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.CmsAlgorithmProtectionCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.ContentTypeCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.MessageDigestCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.RandomNonceCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.RevocationInfoArchivalCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.SequenceNumberCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.SigningCertificateCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.SigningCertificateV2Corrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.SigningTimeCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.TimeStampTokenCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.ocsp.OcspAcceptableResponsesCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.ocsp.OcspArchiveCutoffCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.ocsp.OcspCrlCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.ocsp.OcspExtendedRevokeCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.ocsp.OcspNocheckCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.ocsp.OcspNonceCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.ocsp.OcspPreferredSignatureAlgorithmsCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.attributes.ocsp.OcspServiceLocatorCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions.ArchiveRevInfoCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions.AuthorityInfoAccessCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions.AuthorityKeyIdentifierCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions.BasicConstraintsCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions.CertificatePoliciesCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions.CrlDistributionPointsCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions.ExtKeyUsageCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions.KeyUsageCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions.SubjectKeyIdentifierCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions.TimeStampCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions.UbiquityRightsCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.policies.CpsPointerCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.policies.UserNoticeCorrector;

import java.util.Map;

/**
 * Static class for retrieving correctors based on the object identifiers of
 * the types they handle.
 */
public final class OidCorrectorMapper {
    /**
     * OID -> Corrector map.
     */
    private static final Map<String, AbstractCorrector> CORRECTOR_MAP = Map.ofEntries(
            // Well this is ugly... Need code generation...
            Map.entry(
                    ContentInfoCorrector.OID,
                    ContentInfoCorrector.INSTANCE
            ),
            Map.entry(
                    SignedDataCorrector.OID,
                    SignedDataCorrector.INSTANCE
            ),
            // Cryptographic Algorithms
            Map.entry(
                    Mgf1Corrector.OID,
                    Mgf1Corrector.INSTANCE
            ),
            Map.entry(
                    RsassaPssCorrector.OID,
                    RsassaPssCorrector.INSTANCE
            ),
            // X.509 General
            Map.entry(
                    BasicOcspResponseCorrector.OID,
                    BasicOcspResponseCorrector.INSTANCE
            ),
            Map.entry(
                    TimeStampTokenInfoCorrector.OID,
                    TimeStampTokenInfoCorrector.INSTANCE
            ),
            // X.509 Attributes
            Map.entry(
                    OcspArchiveCutoffCorrector.OID,
                    OcspArchiveCutoffCorrector.INSTANCE
            ),
            Map.entry(
                    OcspCrlCorrector.OID,
                    OcspCrlCorrector.INSTANCE
            ),
            Map.entry(
                    OcspExtendedRevokeCorrector.OID,
                    OcspExtendedRevokeCorrector.INSTANCE
            ),
            Map.entry(
                    OcspNocheckCorrector.OID,
                    OcspNocheckCorrector.INSTANCE
            ),
            Map.entry(
                    OcspNonceCorrector.OID,
                    OcspNonceCorrector.INSTANCE
            ),
            Map.entry(
                    OcspPreferredSignatureAlgorithmsCorrector.OID,
                    OcspPreferredSignatureAlgorithmsCorrector.INSTANCE
            ),
            Map.entry(
                    OcspAcceptableResponsesCorrector.OID,
                    OcspAcceptableResponsesCorrector.INSTANCE
            ),
            Map.entry(
                    OcspServiceLocatorCorrector.OID,
                    OcspServiceLocatorCorrector.INSTANCE
            ),
            Map.entry(
                    CmsAlgorithmProtectionCorrector.OID,
                    CmsAlgorithmProtectionCorrector.INSTANCE
            ),
            Map.entry(
                    ContentTypeCorrector.OID,
                    ContentTypeCorrector.INSTANCE
            ),
            Map.entry(
                    MessageDigestCorrector.OID,
                    MessageDigestCorrector.INSTANCE
            ),
            Map.entry(
                    RandomNonceCorrector.OID,
                    RandomNonceCorrector.INSTANCE
            ),
            Map.entry(
                    RevocationInfoArchivalCorrector.OID,
                    RevocationInfoArchivalCorrector.INSTANCE
            ),
            Map.entry(
                    SequenceNumberCorrector.OID,
                    SequenceNumberCorrector.INSTANCE
            ),
            Map.entry(
                    SigningCertificateCorrector.OID,
                    SigningCertificateCorrector.INSTANCE
            ),
            Map.entry(
                    SigningCertificateV2Corrector.OID,
                    SigningCertificateV2Corrector.INSTANCE
            ),
            Map.entry(
                    SigningTimeCorrector.OID,
                    SigningTimeCorrector.INSTANCE
            ),
            Map.entry(
                    TimeStampTokenCorrector.OID,
                    TimeStampTokenCorrector.INSTANCE
            ),
            // X.509 Extensions
            Map.entry(
                    ArchiveRevInfoCorrector.OID,
                    ArchiveRevInfoCorrector.INSTANCE
            ),
            Map.entry(
                    AuthorityInfoAccessCorrector.OID,
                    AuthorityInfoAccessCorrector.INSTANCE
            ),
            Map.entry(
                    AuthorityKeyIdentifierCorrector.OID,
                    AuthorityKeyIdentifierCorrector.INSTANCE
            ),
            Map.entry(
                    BasicConstraintsCorrector.OID,
                    BasicConstraintsCorrector.INSTANCE
            ),
            Map.entry(
                    CertificatePoliciesCorrector.OID,
                    CertificatePoliciesCorrector.INSTANCE
            ),
            Map.entry(
                    CrlDistributionPointsCorrector.OID,
                    CrlDistributionPointsCorrector.INSTANCE
            ),
            Map.entry(
                    ExtKeyUsageCorrector.OID,
                    ExtKeyUsageCorrector.INSTANCE
            ),
            Map.entry(
                    KeyUsageCorrector.OID,
                    KeyUsageCorrector.INSTANCE
            ),
            Map.entry(
                    SubjectKeyIdentifierCorrector.OID,
                    SubjectKeyIdentifierCorrector.INSTANCE
            ),
            Map.entry(
                    TimeStampCorrector.OID,
                    TimeStampCorrector.INSTANCE
            ),
            Map.entry(
                    UbiquityRightsCorrector.OID,
                    UbiquityRightsCorrector.INSTANCE
            ),
            // X.509 Certificate Policies
            Map.entry(
                    CpsPointerCorrector.OID,
                    CpsPointerCorrector.INSTANCE
            ),
            Map.entry(
                    UserNoticeCorrector.OID,
                    UserNoticeCorrector.INSTANCE
            )
    );

    private OidCorrectorMapper() {
        // This is a "static" class
    }

    /**
     * Returns the corrector for the provided OBJECT IDENTIFIER. If the is no
     * match, then a {@link DefaultCorrector} is returned.
     *
     * @param oid OBJECT IDENTIFIER to get the corrector for.
     *
     * @return The matching corrector, or {@link DefaultCorrector}, if there
     * is no match.
     */
    public static AbstractCorrector get(String oid) {
        if (oid == null) {
            return DefaultCorrector.INSTANCE;
        }
        return CORRECTOR_MAP.getOrDefault(oid, DefaultCorrector.INSTANCE);
    }
}
