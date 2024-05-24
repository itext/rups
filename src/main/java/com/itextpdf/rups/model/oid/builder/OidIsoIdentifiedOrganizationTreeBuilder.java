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
 * <tt>1.3.*</tt> ISO Identified Organization root.
 */
final class OidIsoIdentifiedOrganizationTreeBuilder {
    private OidIsoIdentifiedOrganizationTreeBuilder() {
        // static class
    }

    /**
     * Tree: 1.3.*
     */
    public static OidTreeNode build() {
        // @formatter:off
        return new OidTreeNode("identified-organization", Map.ofEntries(
          entry("6", "dod", Map.ofEntries(
            entry("1", "internet", Map.ofEntries(
              entry("1", "directory"),
              entry("2", "mgmt"),
              entry("3", "experimental"),
              entry("4", "private", Map.ofEntries(
                entry("0", "reserved"),
                entry("1", "enterprise")
              )),
              entry("5", "security", Map.ofEntries(
                entry("5", "mechanisms", Map.ofEntries(
                  entry("7", createPkixTree())
                ))
              )),
              entry("6", "snmpV2"),
              entry("7", "mail"),
              entry("8", "features")
            ))
          )),
          entry("14", createOIWTree()),
          entry("36", createTeleTrustTree()),
          entry("101", createThawteTree())
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.3.6.1.5.5.7.*
     */
    private static OidTreeNode createPkixTree() {
        // @formatter:off
        return new OidTreeNode("pkix", Map.ofEntries(
          entry("0", "mod"),
          entry("1", createPkixPeTree()),
          entry("2", "qt", Map.ofEntries(
            entry("1", "cps"),
            entry("2", "unotice"),
            entry("3", "id-qt-textNotice"),
            entry("4", "id-qt-acps"),
            entry("5", "id-qt-acunotice")
          )),
          entry("3", createPkixKpTree()),
          entry("4", createPkixItTree()),
          entry("5", "pkip"),
          entry("6", createPkixAlgorithmsTree()),
          entry("7", "cmc"),
          entry("8", "on"),
          entry("9", "pda"),
          entry("10", "attributeCertificate"),
          entry("11", "qcs"),
          entry("12", "cct"),
          entry("13", "test"),
          entry("14", "cp"),
          entry("15", "cet"),
          entry("16", "ri"),
          entry("17", "id-stc"),
          entry("18", "id-swb"),
          entry("19", "id-svp"),
          entry("20", "id-logo"),
          entry("21", "id-ppl"),
          entry("22", "id-mr"),
          entry("23", "id-skis"),
          entry("24", "scep"),
          entry("48", createPkixAdTree())
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.3.6.1.5.5.7.1.*
     */
    private static OidTreeNode createPkixPeTree() {
        // @formatter:off
        return new OidTreeNode("pe", Map.ofEntries(
          entry("1", "authorityInfoAccess"),
          entry("2", "biometricInfo"),
          entry("3", "qcStatements"),
          entry("4", "auditIdentity"),
          entry("5", "id-pe-acTargeting"),
          entry("6", "aaControls"),
          entry("7", "id-pe-ipAddrBlocks"),
          entry("8", "id-pe-autonomousSysIds"),
          entry("9", "id-pe-sbgp-routerIdentifier"),
          entry("10", "proxying"),
          entry("11", "subjectInfoAccess"),
          entry("12", "id-pe-logotype"),
          entry("13", "id-pe-wlanSSID"),
          entry("14", "id-pe-proxyCertInfo"),
          entry("15", "id-pe-acPolicies"),
          entry("16", "id-pe-warranty-extn"),
          entry("17", "id-pe-sim"),
          entry("18", "id-pe-cmsContentConstraints"),
          entry("19", "id-pe-otherCerts"),
          entry("20", "id-pe-wrappedApexContinKey"),
          entry("21", "id-pe-clearanceConstraints"),
          entry("22", "id-pe-skiSemantics"),
          entry("23", "id-pe-nsa"),
          entry("24", "ext-TLSFeatures"),
          entry("25", "id-pe-mud-url"),
          entry("26", "id-pe-TNAuthList"),
          entry("27", "id-pe-JWTClaimConstraints"),
          entry("28", "id-pe-ipAddrBlocks-v2"),
          entry("29", "id-pe-autonomousSysIds-v2"),
          entry("30", "id-pe-mudsigner"),
          entry("31", "id-pe-acmeIdentifier"),
          entry("32", "id-pe-masa-url"),
          entry("33", "id-pe-eJWTClaimConstraints"),
          entry("34", "id-pe-nftype")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.3.6.1.5.5.7.3.*
     */
    private static OidTreeNode createPkixKpTree() {
        // @formatter:off
        return new OidTreeNode("kp", Map.ofEntries(
          entry("1", "id-kp-serverAuth"),
          entry("2", "id-kp-clientAuth"),
          entry("3", "id-kp-codeSigning"),
          entry("4", "id-kp-emailProtection"),
          entry("5", "id-kp-ipsecEndSystem"),
          entry("6", "id-kp-ipsecTunnel"),
          entry("7", "id-kp-ipsecUser"),
          entry("8", "id-kp-timeStamping"),
          entry("9", "id-kp-OCSPSigning"),
          entry("10", "id-kp-dvcs"),
          entry("11", "id-kp-sbgpCertAAServerAuth"),
          entry("12", "id-kp-scvp-responder"),
          entry("13", "id-kp-eapOverPPP"),
          entry("14", "id-kp-eapOverLAN"),
          entry("15", "id-kp-scvpServer"),
          entry("16", "id-kp-scvpClient"),
          entry("17", "id-kp-ipsecIKE"),
          entry("18", "id-kp-capwapAC"),
          entry("19", "id-kp-capwapWTP"),
          entry("20", "id-kp-sipDomain"),
          entry("21", "id-kp-secureShellClient"),
          entry("22", "id-kp-secureShellServer"),
          entry("23", "id-kp-sendRouter"),
          entry("24", "id-kp-sendProxiedRouter"),
          entry("25", "id-kp-sendOwner"),
          entry("26", "id-kp-sendProxiedOwner"),
          entry("27", "id-kp-cmcCA"),
          entry("28", "id-kp-cmcRA"),
          entry("29", "id-kp-cmcArchive"),
          entry("30", "id-kp-bgpsec-router"),
          entry("31", "id-kp-BrandIndicatorforMessageIdentification"),
          entry("32", "id-kp-cmKGA"),
          entry("33", "id-kp-rpcTLSClient"),
          entry("34", "id-kp-rpcTLSServer"),
          entry("35", "id-kp-bundleSecurity"),
          entry("36", "id-kp-documentSigning"),
          entry("37", "id-kp-jwt"),
          entry("38", "id-kp-httpContentEncrypt"),
          entry("39", "id-kp-oauthAccessTokenSigning")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.3.6.1.5.5.7.4.*
     */
    private static OidTreeNode createPkixItTree() {
        // @formatter:off
        return new OidTreeNode("it", Map.ofEntries(
          entry("1", "id-it-caProtEncCert"),
          entry("2", "id-it-signKeyPairTypes"),
          entry("3", "id-it-encKeyPairTypes"),
          entry("4", "id-it-preferredSymmAlg"),
          entry("5", "id-it-caKeyUpdateInfo"),
          entry("6", "id-it-currentCRL"),
          entry("7", "id-it-unsupportedOIDs"),
          entry("8", "id-it-subscriptionRequest"),
          entry("9", "id-it-subscriptionResponse"),
          entry("10", "id-it-keyPairParamReq"),
          entry("11", "id-it-keyPairParamRep"),
          entry("12", "id-it-revPassphrase"),
          entry("13", "id-it-implicitConfirm"),
          entry("14", "id-it-confirmWaitTime"),
          entry("15", "id-it-origPKIMessage"),
          entry("16", "id-it-suppLangTags"),
          entry("17", "id-it-caCerts"),
          entry("18", "id-it-rootCaKeyUpdate"),
          entry("19", "id-id-it-certReqTemplate-rootCaKeyUpdate"),
          entry("20", "id-it-rootCaCert"),
          entry("21", "id-it-certProfile"),
          entry("22", "id-it-crlStatusList"),
          entry("23", "id-it-crls")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.3.6.1.5.5.7.48.*
     */
    private static OidTreeNode createPkixAdTree() {
        // @formatter:off
        return new OidTreeNode("ad", Map.ofEntries(
          entry("1", "id-ad-ocsp", Map.ofEntries(
            entry("1", "id-pkix-ocsp-basic"),
            entry("2", "id-pkix-ocsp-nonce"),
            entry("3", "id-pkix-ocsp-crl"),
            entry("4", "id-pkix-ocsp-response"),
            entry("5", "id-pkix-ocsp-nocheck"),
            entry("6", "id-pkix-ocsp-archive-cutoff"),
            entry("7", "id-pkix-ocsp-service-locator"),
            entry("8", "id-pkix-ocsp-pref-sig-algs"),
            entry("9", "id-pkix-ocsp-extended-revoke"),
            entry("10", "id-pkix-ocsp-stir-tn")
          )),
          entry("2", "caIssuers"),
          entry("3", "timestamping"),
          entry("4", "id-ad-dvcs"),
          entry("5", "id-ad-caRepository"),
          entry("6", "id-ad-http-certs"),
          entry("7", "id-ad-http-crls"),
          entry("8", "id-ad-xkms"),
          entry("9", "id-ad-signedObjectRepository"),
          entry("10", "id-ad-rpkiManifest"),
          entry("11", "id-ad-signedObject"),
          entry("12", "id-ad-cmc"),
          entry("13", "id-ad-rpkiNotify"),
          entry("14", "id-ad-stirTNList")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.3.6.1.5.5.7.6.*
     */
    private static OidTreeNode createPkixAlgorithmsTree() {
        // @formatter:off
        return new OidTreeNode("algorithms", Map.ofEntries(
                entry("1", "ig-alg-des40"),
                entry("2", "id-alg-noSignature"),
                entry("3", "id-alg-dh-sig-hmac-sha1"),
                entry("4", "id-alg-dh-pop"),
                entry("5", "id-alg-dhPop-sha224"),
                entry("6", "id-alg-dhPop-sha256"),
                entry("7", "id-alg-dhPop-sha384"),
                entry("8", "id-alg-dhPop-sha512"),
                entry("15", "id-alg-dhPop-static-sha224-hmac-sha224"),
                entry("16", "id-alg-dhPop-static-sha256-hmac-sha256"),
                entry("17", "id-alg-dhPop-static-sha384-hmac-sha384"),
                entry("18", "id-alg-dhPop-static-sha512-hmac-sha512"),
                entry("25", "id-alg-ecdhPop-static-sha224-hmac-sha224"),
                entry("26", "id-alg-ecdhPop-static-sha256-hmac-sha256"),
                entry("27", "id-alg-ecdhPop-static-sha384-hmac-sha384"),
                entry("28", "id-alg-ecdhPop-static-sha512-hmac-sha512"),
                entry("29", "id-alg-eccsi-with-sha256"),
                entry("30", "id-RSASSA-PSS-SHAKE128"),
                entry("31", "id-RSASSA-PSS-SHAKE256"),
                entry("32", "id-ecdsa-with-shake128"),
                entry("33", "id-ecdsa-with-shake256")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.3.14.*
     */
    private static OidTreeNode createOIWTree() {
        // @formatter:off
        return new OidTreeNode("oiw", Map.ofEntries(
          entry("3", "secsig", Map.ofEntries(
            entry("1", "oIWSECSIGAlgorithmObjectIdentifiers"),
            entry("2", "algorithms", Map.ofEntries(
              entry("1", "rsa"),
              entry("2", "md4WitRSA", Map.ofEntries(
                entry("1", "sqmod-N")
              )),
              entry("3", "md5WithRSA", Map.ofEntries(
                entry("1", "sqmod-NwithRSA")
              )),
              entry("4", "md4WithRSAEncryption"),
              entry("6", "desECB"),
              entry("7", "desCBC"),
              entry("8", "desOFB"),
              entry("9", "desCFB"),
              entry("10", "desMAC"),
              entry("11", "rsaSignature"),
              entry("12", "dsa"),
              entry("13", "dsaWithSHA"),
              entry("14", "mdc2WithRSASignature"),
              entry("15", "shaWithRSASignature"),
              entry("16", "dhWithCommonModulus"),
              entry("17", "desEDE"),
              entry("18", "sha"),
              entry("19", "mdc-2"),
              entry("20", "dsaCommon"),
              entry("21", "dsaCommonWithSHA"),
              entry("22", "rsa-key-transport"),
              entry("23", "keyed-hash-seal"),
              entry("24", "md2WithRSASignature"),
              entry("25", "md5WithRSASignature"),
              entry("26", "sha1"),
              entry("27", "dsaWithSHA1"),
              entry("28", "dsaWithCommonSHA1"),
              entry("29", "sha1WithRSAEncryption")
            )),
            entry("3", "authentication-mechanism"),
            entry("4", "attribute"),
            entry("5", "oiwsecsigassrobjectidentifier", Map.ofEntries(
              entry("5", "rule")
            ))
          )),
          entry("7", "dssig")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.3.36.*
     */
    private static OidTreeNode createTeleTrustTree() {
        // @formatter:off
        return new OidTreeNode("teletrust", Map.ofEntries(
          entry("1", "document"),
          entry("2", "sio"),
          entry("3", "algorithm", Map.ofEntries(
            entry("1", "encryptionAlgorithm"),
            entry("2", "hashAlgorithm", Map.ofEntries(
              entry("1", "ripemd160"),
              entry("2", "ripemd128"),
              entry("3", "ripemd256"),
              entry("4", "mdc2slh"),
              entry("5", "mdc2dlh")
            )),
            entry("3", "signatureAlgorithm", Map.ofEntries(
              entry("1", "rsaSignature", Map.ofEntries(
                entry("1", "rsaSignatureWithsha1"),
                entry("2", "rsaSignatureWithripemd160"),
                entry("3", "rsaSignatureWithrimpemd128"),
                entry("4", "rsaSignatureWithrimpemd256")
              )),
              entry("2", createEcSignTree())
            )),
            entry("4", "signatureScheme")
          )),
          entry("4", "attribute"),
          entry("5", "policy"),
          entry("6", "api"),
          entry("7", "key-management"),
          entry("8", "common-pki")
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.3.36.3.3.2*
     */
    private static OidTreeNode createEcSignTree() {
        // @formatter:off
        return new OidTreeNode("ecSign", Map.ofEntries(
          entry("1", "ecSignWithsha1"),
          entry("2", "ecSignWithripemd160"),
          entry("3", "ecSignWithmd2"),
          entry("4", "ecSignWithmd5"),
          entry("5", "ttt-ecg", Map.ofEntries(
            entry("1", "fieldType", Map.ofEntries(
              entry("1", "characteristictwoField", Map.ofEntries(
                entry("1", "basisType", Map.ofEntries(
                  entry("1", "ipBasis")
                ))
              ))
            )),
            entry("2", "keyType", Map.ofEntries(
              entry("1", "ecgPublicKey")
            )),
            entry("3", "curve"),
            entry("4", "signatures", Map.ofEntries(
              entry("1", "ecgdsa-with-RIPEMD160"),
              entry("2", "ecgdsa-with-SHA1"),
              entry("3", "ecgdsa-with-SHA224"),
              entry("4", "ecgdsa-with-SHA256"),
              entry("5", "ecgdsa-with-SHA384"),
              entry("6", "ecgdsa-with-SHA512")
            )),
            entry("5", "module", Map.ofEntries(
              entry("1", "ecgAsn1Module")
            ))
          )),
          entry("8", "ecStdCurvesAndGeneration", Map.ofEntries(
            entry("1", "ellipticCurve", Map.ofEntries(
              entry("1", "versionOne", Map.ofEntries(
                entry("1", "brainpoolP160r1"),
                entry("2", "brainpoolP160t1"),
                entry("3", "brainpoolP192r1"),
                entry("4", "brainpoolP192t1"),
                entry("5", "brainpoolP224r1"),
                entry("6", "brainpoolP224t1"),
                entry("7", "brainpoolP256r1"),
                entry("8", "brainpoolP256t1"),
                entry("9", "brainpoolP320r1"),
                entry("10", "brainpoolP320t1"),
                entry("11", "brainpoolP384r1"),
                entry("12", "brainpoolP384t1"),
                entry("13", "brainpoolP512r1"),
                entry("14", "brainpoolP512t1")
              ))
            ))
          ))
        ));
        // @formatter:on
    }

    /**
     * Tree: 1.3.101.*
     */
    private static OidTreeNode createThawteTree() {
        // @formatter:off
        return new OidTreeNode("thawte", Map.ofEntries(
          entry("100", "reserved"),
          entry("110", "id-X25519"),
          entry("111", "id-X448"),
          entry("112", "id-Ed25519"),
          entry("113", "id-Ed448"),
          entry("114", "id-EdDSA25519-ph"),
          entry("115", "id-EdDSA448-ph")
        ));
        // @formatter:on
    }
}
