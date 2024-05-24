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
 * <tt>2.*</tt> Joint-ISO-ITU-T root.
 */
/*
 * "String literals should not be duplicated" is ignored here, since this is,
 * basically, a codified database of a tree of strings, so there will be some
 * duplication naturally.
 */
@SuppressWarnings("java:S1192")
public final class OidJointIsoItuTTreeBuilder {
    private OidJointIsoItuTTreeBuilder() {
        // static class
    }

    /**
     * Tree: 2.*
     */
    public static OidTreeNode build() {
        // @formatter:off
        return new OidTreeNode("joint-iso-itu-t", Map.ofEntries(
          entry("5", createDsTree()),
          entry("16", "country", Map.ofEntries(
            entry("724", "es", Map.ofEntries(
              entry("1", "adm", Map.ofEntries(
                entry("3", "mpr", Map.ofEntries(
                  entry("1", "e-Administration", Map.ofEntries(
                    entry("1", "eSignatures", Map.ofEntries(
                      entry("1", "documents"),
                      entry("2", "signature-policy", Map.ofEntries(
                        entry("1", "1", Map.ofEntries(
                          entry("8"),
                          entry("9")
                        ))
                      )),
                      entry("3", "aFirma"),
                      entry("4", "tsa"),
                      entry("5", "extensions"),
                      entry("6", "pades-commitment-type-identifier")
                    ))
                  ))
                ))
              ))
            )),
            entry("840", "us", Map.ofEntries(
              entry("1", "organization", Map.ofEntries(
                entry("101", "gov", Map.ofEntries(
                  entry("3", createCsorTree())
                ))
              ))
            ))
          )),
          entry("23", "international-organizations", Map.ofEntries(
            entry("140", "ca-browser-forum", Map.ofEntries(
              entry("1", "certificate-policies", Map.ofEntries(
                entry("1", "ev-guidelines"),
                entry("2", "baseline-requirements", Map.ofEntries(
                  entry("1", "domain-validated"),
                  entry("2", "organization-validated"),
                  entry("3", "individual-validated")
                )),
                entry("3", "extended-validation-codesigning"),
                entry("4", "code-signing-requirements", Map.ofEntries(
                  entry("1", "code-signing"),
                  entry("2", "timestamping")
                )),
                entry("5", "smime", Map.ofEntries(
                  entry("1", "mailbox-validated", Map.ofEntries(
                    entry("1", "legacy"),
                    entry("2", "multipurpose"),
                    entry("3", "strict")
                  )),
                  entry("2", "organization-validated", Map.ofEntries(
                    entry("1", "legacy"),
                    entry("2", "multipurpose"),
                    entry("3", "strict")
                  )),
                  entry("3", "sponsor-validated", Map.ofEntries(
                    entry("1", "legacy"),
                    entry("2", "multipurpose"),
                    entry("3", "strict")
                  )),
                  entry("4", "individual-validated", Map.ofEntries(
                    entry("1", "legacy"),
                    entry("2", "multipurpose"),
                    entry("3", "strict")
                  ))
                )),
                entry("31", "onion-EV")
              ))
            ))
          ))
        ));
        // @formatter:on
    }

    /**
     * Tree: 2.5.*
     */
    private static OidTreeNode createDsTree() {
        // @formatter:off
        return new OidTreeNode("ds", Map.ofEntries(
          entry("1", "module"),
          entry("2", "serviceElement"),
          entry("3", "applicationContext"),
          entry("4", createDsAttributeTypeTree()),
          entry("5", "attributeSyntaxVendor"),
          entry("6", "objectClass"),
          entry("7", "attribute-set"),
          entry("8", "algorithm"),
          entry("9", "abstractSyntax"),
          entry("10", "object"),
          entry("11", "port"),
          entry("12", "dsaOperationalAttribute"),
          entry("13", "matchingRule"),
          entry("14", "knowledgeMatchingRule"),
          entry("15", "nameForm"),
          entry("16", "group"),
          entry("17", "subentry"),
          entry("18", "operationalAttributeType"),
          entry("19", "operationalBinding"),
          entry("20", "schemaObjectClass"),
          entry("21", "schemaOperationalAttribute"),
          entry("23", "administrativeRoles"),
          entry("24", "accessControlAttribute"),
          entry("25", "rosObject"),
          entry("26", "contract"),
          entry("27", "package"),
          entry("28", "accessControlSchemes"),
          entry("29", createDsCertificateExtensionTree()),
          entry("30", "managementObject"),
          entry("31", "attributeValueContext"),
          entry("32", "securityExchange"),
          entry("33", "idmProtocol"),
          entry("34", "problem"),
          entry("35", "notification"),
          entry("36", "matchingRestriction"),
          entry("37", "controlAttributeType"),
          entry("38", "keyPurposes"),
          entry("39", "passwordQuality"),
          entry("40", "attributeSyntax"),
          entry("41", "avRestriction"),
          entry("42", "cmsContentType"),
          entry("43", "wrprot"),
          entry("44", "algo")
        ));
        // @formatter:on
    }

    /**
     * Tree: 2.5.4.*
     */
    private static OidTreeNode createDsAttributeTypeTree() {
        // @formatter:off
        return new OidTreeNode("attributeType", Map.ofEntries(
          entry("0", "objectClass"),
          entry("1", "aliasedEntryName"),
          entry("2", "knowledgeInformation"),
          entry("3", "commonName"),
          entry("4", "surname"),
          entry("5", "serialNumber"),
          entry("6", "countryName"),
          entry("7", "localityName"),
          entry("8", "stateOrProvinceName"),
          entry("9", "streetAddress"),
          entry("10", "organizationName"),
          entry("11", "organizationalUnitName"),
          entry("12", "title"),
          entry("13", "description"),
          entry("14", "searchGuide"),
          entry("15", "businessCategory"),
          entry("16", "postalAddress"),
          entry("17", "postalCode"),
          entry("18", "postOfficeBox"),
          entry("19", "physicalDeliveryOfficeName"),
          entry("20", "telephoneNumber"),
          entry("21", "telexNumber"),
          entry("22", "teletexTerminalIdentifier"),
          entry("23", "facsimileTelephoneNumber"),
          entry("24", "x121Address"),
          entry("25", "internationalISDNNumber"),
          entry("26", "registeredAddress"),
          entry("27", "destinationIndicator"),
          entry("28", "preferredDeliveryMethod"),
          entry("29", "presentationAddress"),
          entry("30", "supportedApplicationContext"),
          entry("31", "member"),
          entry("32", "owner"),
          entry("33", "roleOccupant"),
          entry("34", "seeAlso"),
          entry("35", "userPassword"),
          entry("36", "userCertificate"),
          entry("37", "cACertificate"),
          entry("38", "authorityRevocationList"),
          entry("39", "certificateRevocationList"),
          entry("40", "crossCertificatePair"),
          entry("41", "name"),
          entry("42", "givenName"),
          entry("43", "initials"),
          entry("44", "generationQualifier"),
          entry("45", "uniqueIdentifier"),
          entry("46", "dnQualifier"),
          entry("47", "enhancedSearchGuide"),
          entry("48", "protocolInformation"),
          entry("49", "distinguishedName"),
          entry("50", "uniqueMember"),
          entry("51", "houseIdentifier"),
          entry("52", "supportedAlgorithms"),
          entry("53", "deltaRevocationList"),
          entry("54", "dmdName"),
          entry("55", "clearance"),
          entry("56", "defaultDirQop"),
          entry("57", "attributeIntegrityInfo"),
          entry("58", "attributeCertificate"),
          entry("59", "attributeCertificateRevocationList"),
          entry("60", "confKeyInfo"),
          entry("61", "aACertificate"),
          entry("62", "attributeDescriptorCertificate"),
          entry("63", "attributeAuthorityRevocationList"),
          entry("64", "family-information"),
          entry("65", "pseudonym"),
          entry("66", "communicationsService"),
          entry("67", "communicationsNetwork"),
          entry("68", "certificationPracticeStmt"),
          entry("69", "certificatePolicy"),
          entry("70", "pkiPath"),
          entry("71", "privPolicy"),
          entry("72", "role"),
          entry("73", "delegationPath"),
          entry("74", "protPrivPolicy"),
          entry("75", "xMLPrivilegeInfo"),
          entry("76", "xmlPrivPolicy"),
          entry("77", "uuidpair"),
          entry("78", "tagOid"),
          entry("79", "uiiFormat"),
          entry("80", "uiiInUrh"),
          entry("81", "contentUrl"),
          entry("82", "permission"),
          entry("83", "uri"),
          entry("84", "pwdAttribute"),
          entry("85", "userPwd"),
          entry("86", "urn"),
          entry("87", "url"),
          entry("88", "utmCoordinates"),
          entry("89", "urnC"),
          entry("90", "uii"),
          entry("91", "epc"),
          entry("92", "tagAfi"),
          entry("93", "epcFormat"),
          entry("94", "epcInUrn"),
          entry("95", "ldapUrl"),
          entry("96", "id-at-tagLocation"),
          entry("97", "organizationIdentifier"),
          entry("98", "id-at-countryCode3c"),
          entry("99", "id-at-countryCode3n"),
          entry("100", "id-at-dnsName"),
          entry("101", "id-at-eepkCertificateRevocationList"),
          entry("102", "id-at-eeAttrCertificateRevocationList"),
          entry("103", "id-at-supportedPublicKeyAlgorithms"),
          entry("104", "id-at-intEmail"),
          entry("105", "id-at-jid"),
          entry("106", "id-at-objectIdentifier")
        ));
        // @formatter:on
    }

    /**
     * Tree: 2.5.29.*
     */
    private static OidTreeNode createDsCertificateExtensionTree() {
        // @formatter:off
        return new OidTreeNode("certificateExtension", Map.ofEntries(
          entry("2", "keyAttributes"),
          entry("4", "keyUsageRestriction"),
          entry("5", "policyMapping"),
          entry("6", "subtreesConstraint"),
          entry("9", "subjectDirectoryAttributes"),
          entry("14", "subjectKeyIdentifier"),
          entry("15", "keyUsage"),
          entry("16", "privateKeyUsagePeriod"),
          entry("17", "subjectAltName"),
          entry("18", "issuerAltName"),
          entry("19", "basicConstraints"),
          entry("20", "cRLNumber"),
          entry("21", "reasonCode"),
          entry("22", "expirationDate"),
          entry("23", "instructionCode"),
          entry("24", "invalidityDate"),
          entry("28", "issuingDistributionPoint"),
          entry("29", "certificateIssuer"),
          entry("30", "nameConstraints"),
          entry("31", "crlDistributionPoints"),
          entry("32", "certificatePolicies", Map.ofEntries(
            entry("0", "anyPolicy")
          )),
          entry("33", "policyMappings"),
          entry("35", "authorityKeyIdentifier"),
          entry("36", "policyConstraints"),
          entry("37", "extKeyUsage"),
          entry("38", "authorityAttributeIdentifier"),
          entry("39", "roleSpecCertIdentifier"),
          entry("40", "cRLStreamIdentifier"),
          entry("41", "basicAttConstraints"),
          entry("42", "delegatedNameConstraints"),
          entry("43", "timeSpecification"),
          entry("44", "cRLScope"),
          entry("45", "statusReferrals"),
          entry("46", "freshestCRL"),
          entry("47", "orderedList"),
          entry("48", "attributeDescriptor"),
          entry("49", "userNotice"),
          entry("50", "sOAIdentifier"),
          entry("51", "baseUpdateTime"),
          entry("52", "acceptableCertPolicies"),
          entry("53", "deltaInfo"),
          entry("54", "inhibitAnyPolicy"),
          entry("55", "targetInformation"),
          entry("56", "noRevAvail"),
          entry("57", "acceptablePrivilegePolicies"),
          entry("58", "id-ce-toBeRevoked"),
          entry("59", "id-ce-RevokedGroups"),
          entry("60", "id-ce-expiredCertsOnCRL"),
          entry("61", "indirectIssuer"),
          entry("62", "id-ce-noAssertion"),
          entry("63", "id-ce-aAissuingDistributionPoint"),
          entry("64", "id-ce-issuedOnBehaIFOF"),
          entry("65", "id-ce-singleUse"),
          entry("66", "id-ce-groupAC"),
          entry("67", "id-ce-allowedAttAss"),
          entry("68", "id-ce-attributeMappings"),
          entry("69", "id-ce-holderNameConstraints"),
          entry("70", "id-ce-a-authorizationValidation"),
          entry("71", "id-ce-protRestrict"),
          entry("72", "id-ce-subjectAltPublicKeyInfo"),
          entry("73", "id-ce-altSignatureAlgorithm"),
          entry("74", "id-ce-altSignatureValue"),
          entry("75", "id-ce-associatedInformation")
        ));
        // @formatter:on
    }

    /**
     * Tree: 2.16.840.1.101.3.*
     */
    private static OidTreeNode createCsorTree() {
        // @formatter:off
        return new OidTreeNode("csor", Map.ofEntries(
          entry("4", "nistAlgorithms", Map.ofEntries(
            entry("0", "modules", Map.ofEntries(
              entry("1", "aes")
            )),
            entry("1", createAesTree()),
            entry("2", createHashAlgsTree()),
            entry("3", createSigAlgsTree())
          ))
        ));
        // @formatter:on
    }

    /**
     * Tree: 2.16.840.1.101.3.4.1.*
     */
    private static OidTreeNode createAesTree() {
        // @formatter:off
        return new OidTreeNode("aes", Map.ofEntries(
          entry("1", "aes128-ECB"),
          entry("2", "aes128-CBC-PAD"),
          entry("3", "aes128-OFB"),
          entry("4", "aes128-CFB"),
          entry("5", "aes128-wrap"),
          entry("6", "aes128-GCM"),
          entry("7", "aes128-CCM"),
          entry("8", "aes128-wrap-pad"),
          entry("9", "aes128-GMAC"),
          entry("21", "aes192-ECB"),
          entry("22", "aes192-CBC-PAD"),
          entry("23", "aes192-OFB"),
          entry("24", "aes192-CFB"),
          entry("25", "aes192-wrap"),
          entry("26", "id-aes192-GCM"),
          entry("27", "id-aes192-CCM"),
          entry("28", "aes192-wrap-pad"),
          entry("29", "aes192-GMAC"),
          entry("41", "aes256-ECB"),
          entry("42", "aes256-CBC-PAD"),
          entry("43", "aes256-OFB"),
          entry("44", "aes256-CFB"),
          entry("45", "id-aes256-wrap"),
          entry("46", "aes256-GCM"),
          entry("47", "aes256-CCM"),
          entry("48", "aes256-wrap-pad"),
          entry("49", "aes256-GMAC")
        ));
        // @formatter:on
    }

    /**
     * Tree: 2.16.840.1.101.3.4.2.*
     */
    private static OidTreeNode createHashAlgsTree() {
        // @formatter:off
        return new OidTreeNode("hashAlgs", Map.ofEntries(
          entry("1", "sha256"),
          entry("2", "sha384"),
          entry("3", "sha512"),
          entry("4", "sha224"),
          entry("5", "sha512-224"),
          entry("6", "sha512-256"),
          entry("7", "sha3-224"),
          entry("8", "sha3-256"),
          entry("9", "sha3-384"),
          entry("10", "sha3-512"),
          entry("11", "shake128"),
          entry("12", "shake256"),
          entry("13", "hmacWithSHA3-224"),
          entry("14", "hmacWithSHA3-256"),
          entry("15", "hmacWithSHA3-384"),
          entry("16", "hmacWithSHA3-512"),
          entry("17", "shake128-len"),
          entry("18", "shake256-len"),
          entry("19", "id-KMAC128"),
          entry("20", "id-KMAC256"),
          entry("21", "id-KMACXOF128"),
          entry("22", "id-KACXOF256")
        ));
        // @formatter:on
    }

    /**
     * Tree: 2.16.840.1.101.3.4.3.*
     */
    private static OidTreeNode createSigAlgsTree() {
        // @formatter:off
        return new OidTreeNode("sigAlgs", Map.ofEntries(
          entry("1", "dsa-with-sha224"),
          entry("2", "dsa-with-sha256"),
          entry("3", "dsa-with-sha384"),
          entry("4", "dsa-with-sha512"),
          entry("5", "id-dsa-with-sha3-224"),
          entry("6", "id-dsa-with-sha3-256"),
          entry("7", "id-dsa-with-sha3-384"),
          entry("8", "id-dsa-with-sha3-512"),
          entry("9", "id-ecdsa-with-sha3-224"),
          entry("10", "id-ecdsa-with-sha3-256"),
          entry("11", "id-ecdsa-with-sha3-384"),
          entry("12", "id-ecdsa-with-sha3-512"),
          entry("13", "id-rsassa-pkcs1-v1-5-with-sha3-224"),
          entry("14", "id-rsassa-pkcs1-v1-5-with-sha3-256"),
          entry("15", "id-rsassa-pkcs1-v1-5-with-sha3-384"),
          entry("16", "id-rsassa-pkcs1-v1-5-with-sha3-512")
        ));
        // @formatter:on
    }
}
