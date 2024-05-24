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
package com.itextpdf.rups.model.oid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class OidReferenceTest {

    /*
     * Test data for this was taken from iText sign test files. I.e. these are
     * OIDs, that were present in them and are common enough to have a proper
     * display for them.
     */

    @Test
    void getDisplayString_Unknown() {
        // This is a clearly non-mapped OID
        Assertions.assertEquals(
                "/9/9/9/9",
                OidReference.getDisplayString("9.9.9.9")
        );
        // If only a part is not recognized, then only it should remain numbers
        Assertions.assertEquals(
                "/iso/member-body/us/ansi-x962/curves/prime/prime256v1/13",
                OidReference.getDisplayString("1.2.840.10045.3.1.7.13")
        );
    }

    @Test
    void getDisplayString_DsAttributeTypes() {
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/attributeType/commonName",
                OidReference.getDisplayString("2.5.4.3")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/attributeType/countryName",
                OidReference.getDisplayString("2.5.4.6")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/attributeType/localityName",
                OidReference.getDisplayString("2.5.4.7")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/attributeType/stateOrProvinceName",
                OidReference.getDisplayString("2.5.4.8")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/attributeType/organizationName",
                OidReference.getDisplayString("2.5.4.10")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/attributeType/organizationalUnitName",
                OidReference.getDisplayString("2.5.4.11")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/attributeType/title",
                OidReference.getDisplayString("2.5.4.12")
        );
    }

    @Test
    void getDisplayString_DsCertificateExtensions() {
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/certificateExtension/subjectKeyIdentifier",
                OidReference.getDisplayString("2.5.29.14")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/certificateExtension/keyUsage",
                OidReference.getDisplayString("2.5.29.15")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/certificateExtension/basicConstraints",
                OidReference.getDisplayString("2.5.29.19")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/certificateExtension/reasonCode",
                OidReference.getDisplayString("2.5.29.21")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/certificateExtension/crlDistributionPoints",
                OidReference.getDisplayString("2.5.29.31")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/certificateExtension/authorityKeyIdentifier",
                OidReference.getDisplayString("2.5.29.35")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/ds/certificateExtension/extKeyUsage",
                OidReference.getDisplayString("2.5.29.37")
        );
    }

    @Test
    void getDisplayString_NistAlgorithms() {
        Assertions.assertEquals(
                "/joint-iso-itu-t/country/us/organization/gov/csor/nistAlgorithms/hashAlgs/sha256",
                OidReference.getDisplayString("2.16.840.1.101.3.4.2.1")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/country/us/organization/gov/csor/nistAlgorithms/hashAlgs/sha384",
                OidReference.getDisplayString("2.16.840.1.101.3.4.2.2")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/country/us/organization/gov/csor/nistAlgorithms/hashAlgs/sha512",
                OidReference.getDisplayString("2.16.840.1.101.3.4.2.3")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/country/us/organization/gov/csor/nistAlgorithms/hashAlgs/sha3-256",
                OidReference.getDisplayString("2.16.840.1.101.3.4.2.8")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/country/us/organization/gov/csor/nistAlgorithms/hashAlgs/sha3-384",
                OidReference.getDisplayString("2.16.840.1.101.3.4.2.9")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/country/us/organization/gov/csor/nistAlgorithms/hashAlgs/sha3-512",
                OidReference.getDisplayString("2.16.840.1.101.3.4.2.10")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/country/us/organization/gov/csor/nistAlgorithms/hashAlgs/shake256",
                OidReference.getDisplayString("2.16.840.1.101.3.4.2.12")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/country/us/organization/gov/csor/nistAlgorithms/sigAlgs"
                        + "/id-ecdsa-with-sha3-256",
                OidReference.getDisplayString("2.16.840.1.101.3.4.3.10")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/country/us/organization/gov/csor/nistAlgorithms/sigAlgs"
                        + "/id-ecdsa-with-sha3-384",
                OidReference.getDisplayString("2.16.840.1.101.3.4.3.11")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/country/us/organization/gov/csor/nistAlgorithms/sigAlgs"
                        + "/id-ecdsa-with-sha3-512",
                OidReference.getDisplayString("2.16.840.1.101.3.4.3.12")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/country/us/organization/gov/csor/nistAlgorithms/sigAlgs"
                        + "/id-rsassa-pkcs1-v1-5-with-sha3-256",
                OidReference.getDisplayString("2.16.840.1.101.3.4.3.14")
        );
    }

    @Test
    void getDisplayString_Pkix() {
        Assertions.assertEquals(
                "/iso/identified-organization/dod/internet/security/mechanisms/pkix/pe/authorityInfoAccess",
                OidReference.getDisplayString("1.3.6.1.5.5.7.1.1")
        );
        Assertions.assertEquals(
                "/iso/identified-organization/dod/internet/security/mechanisms/pkix/kp/id-kp-timeStamping",
                OidReference.getDisplayString("1.3.6.1.5.5.7.3.8")
        );
        Assertions.assertEquals(
                "/iso/identified-organization/dod/internet/security/mechanisms/pkix/ad/id-ad-ocsp",
                OidReference.getDisplayString("1.3.6.1.5.5.7.48.1")
        );
        Assertions.assertEquals(
                "/iso/identified-organization/dod/internet/security/mechanisms/pkix/ad/id-ad-ocsp/id-pkix-ocsp-basic",
                OidReference.getDisplayString("1.3.6.1.5.5.7.48.1.1")
        );
        Assertions.assertEquals(
                "/iso/identified-organization/dod/internet/security/mechanisms/pkix/ad/id-ad-ocsp/id-pkix-ocsp-nonce",
                OidReference.getDisplayString("1.3.6.1.5.5.7.48.1.2")
        );
    }

    @Test
    void getDisplayString_RsaDataSecurity() {
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-1/rsaEncryption",
                OidReference.getDisplayString("1.2.840.113549.1.1.1")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-1/rsassa-pss",
                OidReference.getDisplayString("1.2.840.113549.1.1.10")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-1/sha256WithRSAEncryption",
                OidReference.getDisplayString("1.2.840.113549.1.1.11")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-1/sha512WithRSAEncryption",
                OidReference.getDisplayString("1.2.840.113549.1.1.13")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-1/id-mgf1",
                OidReference.getDisplayString("1.2.840.113549.1.1.8")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-7/data",
                OidReference.getDisplayString("1.2.840.113549.1.7.1")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-7/signedData",
                OidReference.getDisplayString("1.2.840.113549.1.7.2")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-9/emailAddress",
                OidReference.getDisplayString("1.2.840.113549.1.9.1")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-9/smime/ct/id-ct-TSTInfo",
                OidReference.getDisplayString("1.2.840.113549.1.9.16.1.4")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-9/smime/id-aa/signing-certificate",
                OidReference.getDisplayString("1.2.840.113549.1.9.16.2.12")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-9/smime/id-aa/id-aa-timeStampToken",
                OidReference.getDisplayString("1.2.840.113549.1.9.16.2.14")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-9/smime/id-aa/id-aa-ets-sigPolicyId",
                OidReference.getDisplayString("1.2.840.113549.1.9.16.2.15")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-9/smime/id-aa/id-aa-signingCertificateV2",
                OidReference.getDisplayString("1.2.840.113549.1.9.16.2.47")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-9/smime/spq/id-spq-ets-sqt-uri",
                OidReference.getDisplayString("1.2.840.113549.1.9.16.5.1")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-9/contentType",
                OidReference.getDisplayString("1.2.840.113549.1.9.3")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-9/messageDigest",
                OidReference.getDisplayString("1.2.840.113549.1.9.4")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-9/signing-time",
                OidReference.getDisplayString("1.2.840.113549.1.9.5")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/pkcs/pkcs-9/id-aa-CMSAlgorithmProtection",
                OidReference.getDisplayString("1.2.840.113549.1.9.52")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/rsadsi/digestAlgorithm/hmacWithSHA256",
                OidReference.getDisplayString("1.2.840.113549.2.9")
        );
    }

    @Test
    void getDisplayString_CurveRelated() {
        Assertions.assertEquals(
                "/iso/member-body/us/ansi-x962/keyType/ecPublicKey",
                OidReference.getDisplayString("1.2.840.10045.2.1")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/ansi-x962/curves/prime/prime256v1",
                OidReference.getDisplayString("1.2.840.10045.3.1.7")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/ansi-x962/signatures/ecdsa-with-SHA2/ecdsa-with-SHA256",
                OidReference.getDisplayString("1.2.840.10045.4.3.2")
        );
        Assertions.assertEquals(
                "/iso/member-body/us/ansi-x962/signatures/ecdsa-with-SHA2/ecdsa-with-SHA384",
                OidReference.getDisplayString("1.2.840.10045.4.3.3")
        );
        Assertions.assertEquals(
                "/iso/identified-organization/thawte/id-Ed25519",
                OidReference.getDisplayString("1.3.101.112")
        );
        Assertions.assertEquals(
                "/iso/identified-organization/thawte/id-Ed448",
                OidReference.getDisplayString("1.3.101.113")
        );
        Assertions.assertEquals(
                "/iso/identified-organization/teletrust/algorithm/signatureAlgorithm/ecSign"
                        + "/ecStdCurvesAndGeneration/ellipticCurve/versionOne/brainpoolP384r1",
                OidReference.getDisplayString("1.3.36.3.3.2.8.1.1.11")
        );
    }

    @Test
    void getDisplayString_Adobe() {
        Assertions.assertEquals(
                "/iso/member-body/us/adbe/acrobat/security/revocationInfoArchival",
                OidReference.getDisplayString("1.2.840.113583.1.1.8")
        );
    }

    @Test
    void getDisplayString_Misc() {
        Assertions.assertEquals(
                "/iso/member-body/us/x9-57/x9algorithm/dsa",
                OidReference.getDisplayString("1.2.840.10040.4.1")
        );
        Assertions.assertEquals(
                "/iso/identified-organization/oiw/secsig/algorithms/sha1",
                OidReference.getDisplayString("1.3.14.3.2.26")
        );
        Assertions.assertEquals(
                "/iso/identified-organization/teletrust/algorithm/hashAlgorithm/ripemd160",
                OidReference.getDisplayString("1.3.36.3.2.1")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/country/es/adm/mpr/e-Administration/eSignatures/signature-policy/1/9",
                OidReference.getDisplayString("2.16.724.1.3.1.1.2.1.9")
        );
        Assertions.assertEquals(
                "/joint-iso-itu-t/international-organizations/ca-browser-forum/certificate-policies"
                        + "/baseline-requirements/domain-validated",
                OidReference.getDisplayString("2.23.140.1.2.1")
        );
    }
}