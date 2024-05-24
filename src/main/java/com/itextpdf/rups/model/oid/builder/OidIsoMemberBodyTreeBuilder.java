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
package com.itextpdf.rups.model.oid.builder;

import com.itextpdf.rups.model.oid.OidTreeNode;

import java.util.Map;
import static com.itextpdf.rups.model.oid.OidTreeNode.entry;

/**
 * Static class for building an "OID -> Display Name" mapping tree from the
 * <tt>1.2.*</tt> ISO Member Body root.
 */
/*
 * "String literals should not be duplicated" is ignored here, since this is,
 * basically, a codified database of a tree of strings, so there will be some
 * duplication naturally.
 */
@SuppressWarnings("java:S1192")
final class OidIsoMemberBodyTreeBuilder {
    private OidIsoMemberBodyTreeBuilder() {
        // static class
    }

    /**
     * Tree: 1.2.*
     */
    public static OidTreeNode build() {
        // @formatter:off
        return new OidTreeNode("member-body", Map.ofEntries(
          entry("840", "us", Map.ofEntries(
            entry("10040", createX957Tree()),
            entry("10045", createAnsiX962Tree()),
            entry("113549", createRsaDsiTree()),
            entry("113583", createAdobeTree())
          ))
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.2.840.10040.*
     */
    private static OidTreeNode createX957Tree() {
        // @formatter:off
        return new OidTreeNode("x9-57", Map.ofEntries(
          entry("2", "holdinstruction", Map.ofEntries(
            entry("1", "holdinstruction-none"),
            entry("2", "callissuer"),
            entry("3", "reject")
          )),
          entry("4", "x9algorithm", Map.ofEntries(
            entry("1", "dsa"),
            entry("3", "dsa-with-sha1")
          ))
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.2.840.10045.*
     */
    private static OidTreeNode createAnsiX962Tree() {
        // @formatter:off
        return new OidTreeNode("ansi-x962", Map.ofEntries(
          entry("0", "modules"),
          entry("1", "fieldType", Map.ofEntries(
            entry("1", "prime-field"),
            entry("2", "characteristic-two-field", Map.ofEntries(
              entry("3", "basisType", Map.ofEntries(
                entry("1", "gnBasis"),
                entry("2", "tpBasis"),
                entry("3", "ppBasis")
              ))
            ))
          )),
          entry("2", "keyType", Map.ofEntries(
            entry("1", "ecPublicKey")
          )),
          entry("3", "curves", Map.ofEntries(
            entry("0", "characteristicTwo", Map.ofEntries(
              entry("1", "c2pnb163v1"),
              entry("2", "c2pnb163v2"),
              entry("3", "c2pnb163v3"),
              entry("4", "c2pnb176w1"),
              entry("5", "c2tnb191v1"),
              entry("6", "c2tnb191v2"),
              entry("7", "c2tnb191v3"),
              entry("8", "c2onb191v4"),
              entry("9", "c2onb191v5"),
              entry("10", "c2pnb208w1"),
              entry("11", "c2tnb239v1"),
              entry("12", "c2tnb239v2"),
              entry("13", "c2tnb239v3"),
              entry("14", "c2onb239v4"),
              entry("15", "c2onb239v5"),
              entry("16", "c2pnb272W1"),
              entry("17", "c2pnb304W1"),
              entry("18", "c2tnb359v1"),
              entry("19", "c2pnb368w1"),
              entry("20", "c2tnb431r1")
            )),
            entry("1", "prime", Map.ofEntries(
              entry("1", "prime192v1"),
              entry("2", "prime192v2"),
              entry("3", "prime192v3"),
              entry("4", "prime239v1"),
              entry("5", "prime239v2"),
              entry("6", "prime239v3"),
              entry("7", "prime256v1")
            ))
          )),
          entry("4", "signatures", Map.ofEntries(
            entry("1", "ecdsa-with-SHA1"),
            entry("2", "ecdsa-with-Recommended"),
            entry("3", "ecdsa-with-SHA2", Map.ofEntries(
              entry("1", "ecdsa-with-SHA224"),
              entry("2", "ecdsa-with-SHA256"),
              entry("3", "ecdsa-with-SHA384"),
              entry("4", "ecdsa-with-SHA512")
            ))
          )),
          entry("5", "module")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.2.840.113549.*
     */
    private static OidTreeNode createRsaDsiTree() {
        // @formatter:off
        return new OidTreeNode("rsadsi", Map.ofEntries(
          entry("1", "pkcs", Map.ofEntries(
            entry("1", "pkcs-1", Map.ofEntries(
              entry("0", "modules", Map.ofEntries(
                entry("1", "pkcs-1")
              )),
              entry("1", "rsaEncryption"),
              entry("2", "md2WithRSAEncryption"),
              entry("3", "md4withRSAEncryption"),
              entry("4", "md5WithRSAEncryption"),
              entry("5", "sha1-with-rsa-signature"),
              entry("6", "rsaOAEPEncryptionSET"),
              entry("7", "id-RSAES-OAEP"),
              entry("8", "id-mgf1"),
              entry("9", "id-pSpecified"),
              entry("10", "rsassa-pss"),
              entry("11", "sha256WithRSAEncryption"),
              entry("12", "sha384WithRSAEncryption"),
              entry("13", "sha512WithRSAEncryption"),
              entry("14", "sha224WithRSAEncryption"),
              entry("15", "sha512-224WithRSAEncryption"),
              entry("16", "sha512-256WithRSAEncryption")
            )),
            entry("7", "pkcs-7", Map.ofEntries(
              entry("0", "module", Map.ofEntries(
                entry("1", "pkcs-7")
              )),
              entry("1", "data"),
              entry("2", "signedData"),
              entry("3", "envelopedData"),
              entry("4", "signedAndEnvelopedData"),
              entry("5", "digestedData"),
              entry("6", "encryptedData"),
              entry("7", "dataWithAttributes"),
              entry("8", "encryptedPrivateKeyInfo")
            )),
            entry("9", createPkcs9Tree())
          )),
          entry("2", "digestAlgorithm", Map.ofEntries(
            entry("2", "md2"),
            entry("4", "md4"),
            entry("5", "md5"),
            entry("6", "hmacWithMD5"),
            entry("7", "hmacWithSHA1"),
            entry("8", "hmacWithSHA224"),
            entry("9", "hmacWithSHA256"),
            entry("10", "hmacWithSHA384"),
            entry("11", "hmacWithSHA512"),
            entry("12", "hmacWithSHA512-224"),
            entry("13", "hmacWithSHA512-256")
          ))
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.2.840.113549.1.9.*
     */
    private static OidTreeNode createPkcs9Tree() {
        // @formatter:off
        return new OidTreeNode("pkcs-9", Map.ofEntries(
          entry("0", "module", Map.ofEntries(
            entry("1", "pkcs-9")
          )),
          entry("1", "emailAddress"),
          entry("2", "unstructuredName"),
          entry("3", "contentType"),
          entry("4", "messageDigest"),
          entry("5", "signing-time"),
          entry("6", "countersignature"),
          entry("7", "challengePassword"),
          entry("8", "unstructuredAddress"),
          entry("9", "extendedCertificateAttributes"),
          entry("13", "signingDescription"),
          entry("14", "extensionRequest"),
          entry("15", "smimeCapabilities", Map.ofEntries(
            entry("1", "preferSignedData"),
            entry("2", "canNotDecryptAny"),
            entry("3", "sMIMECapabilitiesVersions"),
            entry("4", "receipt"),
            entry("5", "contentHints"),
            entry("6", "mlExpansionHistory")
          )),
          entry("16", createSMimeTree()),
          entry("17", "pgpKeyID"),
          entry("20", "friendlyName"),
          entry("21", "localKeyID"),
          entry("22", "certTypes", Map.ofEntries(
            entry("1", "x509Certificate"),
            entry("2", "sdsiCertificate")
          )),
          entry("23", "crlTypes", Map.ofEntries(
            entry("1", "x509Crl")
          )),
          entry("24", "pkcs-9-oc", Map.ofEntries(
            entry("1", "pkcs-9-oc-pkcsEntity"),
            entry("2", "pkcs-9-oc-naturalPerson")
          )),
          entry("25", "pkcs-9-at", Map.ofEntries(
            entry("1", "pkcs15Token"),
            entry("2", "encryptedPrivateKeyInfo"),
            entry("3", "pkcs-9-at-randomNonce"),
            entry("4", "sequenceNumber"),
            entry("5", "pkcs7PDU")
          )),
          entry("26", "pkcs-9-sx", Map.ofEntries(
            entry("1", "pkcs-9-sx-pkcs9String"),
            entry("2", "pkcs-9-sx-signingTime")
          )),
          entry("27", "pkcs-9-mr", Map.ofEntries(
            entry("1", "pkcs-9-mr-caseIgnoreMatch"),
            entry("2", "pkcs-9-mr-signingTimeMatch"),
            entry("3", "signingTimeMatch")
          )),
          entry("52", "id-aa-CMSAlgorithmProtection")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.2.840.113549.1.9.16.*
     */
    private static OidTreeNode createSMimeTree() {
        // @formatter:off
        return new OidTreeNode("smime", Map.ofEntries(
          entry("0", createSMimeModulesTree()),
          entry("1", createSMimeCtTree()),
          entry("2", createSMimeAaTree()),
          entry("3", createSMimeAlgTree()),
          entry("4", "cd", Map.ofEntries(
            entry("1", "id-cd-ldap")
          )),
          entry("5", "spq", Map.ofEntries(
            entry("1", "id-spq-ets-sqt-uri"),
            entry("2", "id-spq-ets-sqt-unotice")
          )),
          entry("6", "cti", Map.ofEntries(
            entry("1", "id-cti-ets-proofOfOrigin"),
            entry("2", "id-cti-ets-proofOfReceipt"),
            entry("3", "id-cti-ets-proofOfDelivery"),
            entry("4", "id-cti-ets-proofOfSender"),
            entry("5", "id-cti-ets-proofOfApproval"),
            entry("6", "id-cti-ets-proofOfCreation")
          )),
          entry("7", "tsp", Map.ofEntries(
            entry("1", "id-tsp-TEST-Amoco"),
            entry("2", "id-tsp-TEST-Caterpillar"),
            entry("3", "id-tsp-TEST-Whirlpool"),
            entry("4", "id-tsp-TEST-Whirlpool-Categories")
          )),
          entry("8", "skd", Map.ofEntries(
            entry("1", "id-skd-glUseKEK"),
            entry("2", "id-skd-glDelete"),
            entry("3", "id-skd-glAddMember"),
            entry("4", "id-skd-glDeleteMember"),
            entry("5", "id-skd-glRekey"),
            entry("6", "id-skd-glAddOwner"),
            entry("7", "id-skd-glRemoveOwner"),
            entry("8", "id-skd-glkCompromise"),
            entry("9", "id-skd-glkRefresh"),
            entry("10", "id-skd-glFailInfo"),
            entry("11", "id-skd-glaQueryRequest"),
            entry("12", "id-skd-glaQueryResponse"),
            entry("13", "id-skd-glProvideCert"),
            entry("14", "id-skd-glUpdateCert"),
            entry("15", "id-skd-glKey")
          )),
          entry("9", "sti", Map.ofEntries(
            entry("1", "id-sti-originatorSig"),
            entry("2", "id-sti-domainSig"),
            entry("3", "id-sti-addAttribSig"),
            entry("4", "id-sti-reviewSig"),
            entry("5", "id-sti-delegatedOriginatorSig")
          )),
          entry("10", "eit", Map.ofEntries(
            entry("1", "id-eit-envelopedData"),
            entry("2", "id-eit-signedData"),
            entry("3", "id-eit-certsOnly"),
            entry("4", "id-eit-signedReceipt"),
            entry("5", "id-eit-envelopedX400"),
            entry("6", "id-eit-signedX400"),
            entry("7", "id-eit-compressedData")
          )),
          entry("11", "cap", Map.ofEntries(
            entry("1", "id-cap-preferBinaryInside")
          )),
          entry("12", createSMimePskcTree()),
          entry("13", "id-ori", Map.ofEntries(
            entry("1", "id-ori-keyTransPSK"),
            entry("2", "id-ori-keyAgreePSK"),
            entry("3", "id-ori-kem")
          )),
          entry("15", "authEnc-128"),
          entry("16", "authEnc-256")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.2.840.113549.1.9.16.0.*
     */
    private static OidTreeNode createSMimeModulesTree() {
        // @formatter:off
        return new OidTreeNode("modules", Map.ofEntries(
          entry("1", "cms"),
          entry("2", "ess"),
          entry("3", "oid"),
          entry("4", "msg-v3"),
          entry("9", "id-mod-certdist"),
          entry("10", "domsec"),
          entry("11", "compress"),
          entry("12", "symkeydist"),
          entry("13", "rcek"),
          entry("14", "cms-2001"),
          entry("15", "v1AttrCert"),
          entry("16", "cmsalg-2001"),
          entry("17", "pwri"),
          entry("18", "pwri"),
          entry("19", "cms-aes"),
          entry("20", "cms-rsaes-oaep"),
          entry("21", "msg-v3dot1"),
          entry("22", "cms-firmware-wrap"),
          entry("23", "id-mod-cms-camellia"),
          entry("24", "cms-2004"),
          entry("25", "id-mod-cms-seed"),
          entry("28", "eSignature-explicit88"),
          entry("29", "eSignature-explicit97"),
          entry("30", "id-mod-ess-2006"),
          entry("31", "cms-authEnvelopedData"),
          entry("32", "cms-aes-ccm-and-gcm"),
          entry("33", "id-mod-symmetricKeyPkgV1"),
          entry("34", "id-mod-multipleSig-2008"),
          entry("36", "id-mod-symkeydist-02"),
          entry("37", "id-mod-cmsalg-2001-02"),
          entry("38", "id-mod-cms-aes-02"),
          entry("39", "id-mod-msg-v3dot1-02"),
          entry("40", "id-mod-cms-firmware-wrap-02"),
          entry("41", "id-mod-cms-2004-02"),
          entry("42", "id-mod-ess-2006-02"),
          entry("43", "id-mod-cms-authEnvelopedData-02"),
          entry("44", "id-mod-cms-aes-ccm-gcm-02"),
          entry("45", "id-mod-cms-ecc-alg-2009-88"),
          entry("46", "id-mod-cms-ecc-alg-2009-02"),
          entry("49", "id-mod-MD5-XOR-EXPERIMENT"),
          entry("50", "mod-asymmetricKeyPkgV1"),
          entry("51", "id-mod-encryptedKeyPkgV1"),
          entry("52", "id-mod-cms-algorithmProtect"),
          entry("53", "id-mod-pskcAttributesModule"),
          entry("54", "id-mod-compressedDataContent"),
          entry("55", "id-mod-binSigningTime-2009"),
          entry("56", "id-mod-contentCollect-2009"),
          entry("57", "id-mod-cmsAuthEnvData-2009"),
          entry("58", "id-mod-cms-2009"),
          entry("59", "id-mod-multipleSign-2009"),
          entry("60", "id-mod-rpkiManifest"),
          entry("61", "id-mod-rpkiROA"),
          entry("62", "id-mod-setKeyAttributeV1"),
          entry("63", "id-mod-keyPkgReceiptAndErrV2"),
          entry("64", "id-mod-mts-hashsig-2013"),
          entry("65", "secure-headers-v1"),
          entry("66", "id-mod-CMS-AEADChaCha20Poly1305"),
          entry("67", "id-mod-cms-ecdh-alg-2017"),
          entry("68", "id-mod-hkdf-oid-2019"),
          entry("69", "id-mod-cms-ori-psk-2019"),
          entry("70", "id-mod-cms-shakes-2019"),
          entry("71", "id-mod-cbor-2019"),
          entry("72", "id-mod-aes-gmac-alg-2020"),
          entry("73", "id-mod-rpkiSignedChecklist-2022"),
          entry("75", "id-mod-rpkiROA-2023"),
          entry("76", "id-pkcs12-pbmac1-2023")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.2.840.113549.1.9.16.1.*
     */
    private static OidTreeNode createSMimeCtTree() {
        // @formatter:off
        return new OidTreeNode("ct", Map.ofEntries(
          entry("0", "id-ct-anyContentType"),
          entry("1", "id-ct-receipt"),
          entry("2", "id-ct-authData"),
          entry("3", "id-ct-publishCert"),
          entry("4", "id-ct-TSTInfo"),
          entry("5", "id-ct-tdtInfo"),
          entry("6", "id-ct-contentInfo"),
          entry("7", "id-ct-DVCSRequestData"),
          entry("8", "id-ct-DVCSResponseData"),
          entry("9", "id-ct-compressedData"),
          entry("10", "id-ct-scvp-certValRequest"),
          entry("11", "id-ct-scvp-certValResponse"),
          entry("12", "id-ct-scvp-valPolRequest"),
          entry("13", "id-ct-valPolResponse"),
          entry("14", "id-ct-attrCertEncAttrs"),
          entry("15", "id-ct-TSReq"),
          entry("16", "id-ct-firmwarePackage"),
          entry("17", "id-ct-firmwareLoadReceipt"),
          entry("18", "id-ct-firmwareLoadError"),
          entry("19", "id-ct-contentCollection"),
          entry("20", "id-ct-contentWithAttrs"),
          entry("21", "id-ct-encKeyWithID"),
          entry("22", "id-ct-encPEPSI"),
          entry("23", "id-ct-authEnvelopedData"),
          entry("24", "id-ct-routeOriginAuthz"),
          entry("25", "id-ct-KP-sKeyPackage"),
          entry("26", "id-ct-rpkiManifest"),
          entry("27", "id-ct-asciiTextWithCRLF"),
          entry("28", "id-ct-xml"),
          entry("29", "id-ct-pdf"),
          entry("30", "id-ct-postscript"),
          entry("31", "id-ct-timestampedData"),
          entry("32", "id-ct-ASAdjacencyAttest"),
          entry("33", "id-ct-rpkiTrustAnchor"),
          entry("34", "id-ct-trustAnchorList"),
          entry("35", "id-ct-rpkiGhostbusters"),
          entry("36", "id-ct-resourceTaggedAttest"),
          entry("37", "id-ct-utf8TextWithCRLF"),
          entry("38", "id-ct-htmlWithCRLF"),
          entry("39", "id-ct-epub"),
          entry("40", "id-ct-animaJSONVoucher"),
          entry("41", "id-ct-mudtype"),
          entry("42", "id-ct-sztpConveyedInfoXML"),
          entry("43", "id-ct-sztpConveyedInfoJSON"),
          entry("44", "id-ct-cbor"),
          entry("45", "id-ct-cborSequence"),
          entry("46", "id-ct-animaCBORVoucher"),
          entry("47", "id-ct-geofeedCSVwithCRLF"),
          entry("48", "id-ct-signedChecklist"),
          entry("49", "id-ct-ASPA"),
          entry("50", "id-ct-signedTAL"),
          entry("51", "id-ct-rpkiSignedPrefixList")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.2.840.113549.1.9.16.2.*
     */
    private static OidTreeNode createSMimeAaTree() {
        // @formatter:off
        return new OidTreeNode("id-aa", Map.ofEntries(
          entry("1", "id-aa-receiptRequest"),
          entry("2", "id-aa-securityLabel"),
          entry("3", "id-aa-mlExpandHistory"),
          entry("4", "id-aa-contentHint"),
          entry("5", "id-aa-msgSigDigest"),
          entry("6", "id-aa-encapContentType"),
          entry("7", "id-aa-contentIdentifier"),
          entry("8", "macValue"),
          entry("9", "id-aa-equivalentLabels"),
          entry("10", "id-aa-contentReference"),
          entry("11", "id-aa-encrypKeyPref"),
          entry("12", "signing-certificate"),
          entry("13", "id-aa-smimeEncryptCerts"),
          entry("14", "id-aa-timeStampToken"),
          entry("15", "id-aa-ets-sigPolicyId"),
          entry("16", "id-aa-ets-commitmentType"),
          entry("17", "id-aa-ets-signerLocation"),
          entry("18", "id-aa-ets-signerAttr"),
          entry("19", "id-aa-ets-otherSigCert"),
          entry("21", "id-aa-ets-CertificateRefs"),
          entry("22", "aa-ets-revocationRefs"),
          entry("23", "id-aa-ets-certValues"),
          entry("24", "id-aa-ets-revocationValues"),
          entry("25", "id-aa-ets-escTimeStamp"),
          entry("26", "id-aa-ets-certCRLTimestamp"),
          entry("27", "id-aa-ets-archiveTimeStamp"),
          entry("28", "id-aa-signatureType"),
          entry("29", "id-aa-dvcs-dvc"),
          entry("30", "id-aa-CEKReference"),
          entry("31", "id-aa-CEKMaxDecrypts"),
          entry("32", "id-aa-KEKDerivationAlg"),
          entry("33", "id-aa-intendedRecipients"),
          entry("34", "id-aa-cmc-unsignedData"),
          entry("35", "id-aa-firmwarePackageID"),
          entry("36", "id-aa-targetHardwareIDs"),
          entry("37", "id-aa-decryptKeyID"),
          entry("38", "id-aa-implCryptoAlgs"),
          entry("39", "id-aa-wrappedFirmwareKey"),
          entry("40", "id-aa-communityIdentifiers"),
          entry("41", "id-aa-fwPkgMessageDigest"),
          entry("42", "id-aa-firmwarePackageInfo"),
          entry("43", "id-aa-implCompressAlgs"),
          entry("44", "attrCertificateRefs"),
          entry("45", "attrRevocationRefs"),
          entry("46", "binarySigningTime"),
          entry("47", "id-aa-signingCertificateV2"),
          entry("48", "id-aa-ets-archiveTimestampV2"),
          entry("49", "id-aa-er-internal"),
          entry("50", "id-aa-er-external"),
          entry("52", "id-aa-cmsAlgorithmProtect"),
          entry("54", "id-aa-asymmDecryptKeyID"),
          entry("55", "id-aa-secureHeaderFieldsIdentifier"),
          entry("56", "id-aa-otpChallenge"),
          entry("57", "id-aa-revocationChallenge"),
          entry("58", "id-aa-estIdentityLinking")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.2.840.113549.1.9.16.3.*
     */
    private static OidTreeNode createSMimeAlgTree() {
        // @formatter:off
        return new OidTreeNode("alg", Map.ofEntries(
          entry("1", "id-alg-ESDHwith3DES"),
          entry("2", "id-alg-ESDHwithRC2"),
          entry("3", "id-alg-3DESwrap"),
          entry("4", "id-alg-RC2wrap"),
          entry("5", "alg-ESDH"),
          entry("6", "alg-CMS3DESwrap"),
          entry("7", "alg-CMSRC2wrap"),
          entry("8", "alg-zlibCompress"),
          entry("9", "pwri-kek"),
          entry("10", "alg-SSDH"),
          entry("11", "id-alg-HMACwith3DESwrap"),
          entry("12", "id-alg-HMACwithAESwrap"),
          entry("13", "id-alg-MD5-XOR-EXPERIMENT"),
          entry("14", "id-alg-rsa-kem"),
          entry("15", "id-alg-authEnc-128"),
          entry("16", "id-alg-authEnc-256"),
          entry("17", "id-alg-hss-lms-hashsig"),
          entry("18", "id-alg-AEADChaCha20Poly1305"),
          entry("19", "dhSinglePass-stdDH-hkdf-sha256-scheme"),
          entry("20", "dhSinglePass-stdDH-hkdf-sha384-scheme"),
          entry("21", "dhSinglePass-stdDH-hkdf-sha512-scheme"),
          entry("22", "id-alg-AES-SIV-CMAC-aead-256"),
          entry("23", "id-alg-AES-SIV-CMAC-aead-384"),
          entry("24", "id-alg-AES-SIV-CMAC-aead-512"),
          entry("25", "id-alg-AES-SIV-CMAC-wrap-256"),
          entry("26", "id-alg-AES-SIV-CMAC-wrap-384"),
          entry("27", "id-alg-AES-SIV-CMAC-wrap-512"),
          entry("28", "id-alg-hkdf-with-sha256"),
          entry("29", "id-alg-hkdf-with-sha384"),
          entry("30", "id-alg-hkdf-with-sha512")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.2.840.113549.1.9.16.12.*
     */
    private static OidTreeNode createSMimePskcTree() {
        // @formatter:off
        return new OidTreeNode("id-pskc", Map.ofEntries(
          entry("1", "id-pskc-manufacturer"),
          entry("2", "id-pskc-serialNo"),
          entry("3", "id-pskc-model"),
          entry("4", "id-pskc-issueNo"),
          entry("5", "id-pskc-deviceBinding"),
          entry("6", "id-pskc-deviceStartDate"),
          entry("7", "id-pskc-deviceExpiryDate"),
          entry("8", "id-pskc-moduleId"),
          entry("9", "id-pskc-keyId"),
          entry("10", "id-pskc-algorithm"),
          entry("11", "id-pskc-issuer"),
          entry("12", "id-pskc-keyProfileId"),
          entry("13", "id-pskc-keyReference"),
          entry("14", "id-pskc-friendlyName"),
          entry("15", "id-pskc-algorithmParameters"),
          entry("16", "id-pskc-counter"),
          entry("17", "id-pskc-time"),
          entry("18", "id-pskc-timeInterval"),
          entry("19", "id-pskc-timeDrift"),
          entry("20", "id-pskc-valueMAC"),
          entry("21", "id-pskc-keyStartDate"),
          entry("22", "id-pskc-keyExpiryDate"),
          entry("23", "id-pskc-noOfTransactions"),
          entry("24", "id-pskc-keyUsages"),
          entry("25", "id-pskc-pinPolicy"),
          entry("26", "id-pskc-deviceUserId"),
          entry("27", "id-pskc-keyUserId")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.2.840.113583.*
     */
    private static OidTreeNode createAdobeTree() {
        // @formatter:off
        return new OidTreeNode("adbe", Map.ofEntries(
          entry("1", "acrobat", Map.ofEntries(
            entry("1", "security", Map.ofEntries(
              entry("8", "revocationInfoArchival")
            )),
            entry("2", "cps", Map.ofEntries(
              entry("1", "authenticDocuments"),
              entry("2", "test"),
              entry("3", "ubiquity"),
              entry("4", "adhoc"),
              entry("5", "qtsa"),
              entry("6", "qesig"),
              entry("7", "qeseal")
            )),
            entry("7", "ubiquity", Map.ofEntries(
              entry("1", "ubiquitySubRights")
            )),
            entry("9", "x509-extensions", Map.ofEntries(
              entry("1", "time-stamp"),
              entry("2", "archiveRevInfo")
            ))
          ))
        ));
        // @formatter:on
    }
}
