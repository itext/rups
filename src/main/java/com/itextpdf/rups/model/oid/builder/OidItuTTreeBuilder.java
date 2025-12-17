/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
 * <tt>0.*</tt> ITU-T root.
 */
public final class OidItuTTreeBuilder {
    private OidItuTTreeBuilder() {
        // static class
    }

    /**
     * Tree: 0.*
     */
    public static OidTreeNode build() {
        // @formatter:off
        return new OidTreeNode("itu-t", Map.ofEntries(
          entry("4", "identified-organization", Map.ofEntries(
            entry("0", createEtsiTree())
          ))
        ));
        // @formatter:on
    }

    /**
     * Tree: 0.4.0.*
     *
     * @see <a href="https://www.etsi.org/standards">
     * ETSI - Standards
     * </a>
     */
    private static OidTreeNode createEtsiTree() {
        // @formatter:off
        return new OidTreeNode("etsi", Map.ofEntries(
          entry("1862", "id-qc-profile", Map.ofEntries(
            entry("1", createEtsiQcsTree())
          )),
          entry("2042", createOtherCertificatePoliciesTree()),
          entry("194112", createQualifiedCertificatePoliciesTree())
        ));
        // @formatter:on
    }

    /**
     * Tree: 0.4.0.1862.1.*
     *
     * @see <a href="https://www.etsi.org/deliver/etsi_en/319400_319499/31941205/02.05.01_60/en_31941205v020501p.pdf">
     * Electronic Signatures and Trust Infrastructures (ESI);
     * Certificate Profiles;
     * Part 5: QCStatements
     * </a>
     */
    private static OidTreeNode createEtsiQcsTree() {
        // @formatter:off
        return new OidTreeNode("id-etsi-qcs", Map.ofEntries(
          entry("1", "id-etsi-qcs-QcCompliance"),
          entry("2", "id-etsi-qcs-QcLimitValue"),
          entry("3", "id-etsi-qcs-QcRetentionPeriod"),
          entry("4", "id-etsi-qcs-QcSSCD"),
          entry("5", "id-etsi-qcs-QcPDS"),
          entry("6", "id-etsi-qcs-QcType", Map.ofEntries(
            entry("1", "id-etsi-qct-esign"),
            entry("2", "id-etsi-qct-eseal"),
            entry("3", "id-etsi-qct-web")
          )),
          entry("7", "id-etsi-qcs-QcCClegislation"),
          entry("8", "id-etsi-qcs-QcIdentMethod", Map.ofEntries(
            entry("1", "id-etsi-qct-eIDAS1-ab"),
            entry("2", "id-etsi-qct-eIDAS1-cd"),
            entry("3", "id-etsi-qct-eIDAS2-acd"),
            entry("4", "id-etsi-qct-eIDAS2-b")
          )),
          entry("9", "id-etsi-qcs-QcQSCDlegislation")
        ));
        // @formatter:on
    }

    /**
     * Tree: 0.4.0.2042.*
     *
     * @see <a href="https://www.etsi.org/deliver/etsi_en/319400_319499/31941101/01.05.01_60/en_31941101v010501p.pdf">
     * Electronic Signatures and Trust Infrastructures (ESI);
     * Policy and security requirements for
     * Trust Service Providers issuing certificates;
     * Part 1: General requirements
     * </a>
     */
    private static OidTreeNode createOtherCertificatePoliciesTree() {
        // @formatter:off
        return new OidTreeNode("other-certificate-policies", Map.ofEntries(
          entry("1", "policy-identifiers", Map.ofEntries(
            entry("1", "ncp"),
            entry("2", "ncpplus"),
            entry("3", "lcp"),
            entry("4", "evcp"),
            entry("6", "dvcp"),
            entry("7", "ovcp"),
            entry("8", "ivcp")
          ))
        ));
        // @formatter:on
    }

    /**
     * Tree: 0.4.0.194112.*
     *
     * @see <a href="https://www.etsi.org/deliver/etsi_en/319400_319499/31941102/02.06.01_60/en_31941102v020601p.pdf">
     * Electronic Signatures and Trust Infrastructures (ESI);
     * Policy and security requirements for
     * Trust Service Providers issuing certificates;
     * Part 2: Requirements for trust service providers issuing
     * EU qualified certificates
     * </a>
     */
    private static OidTreeNode createQualifiedCertificatePoliciesTree() {
        // @formatter:off
        return new OidTreeNode("qualified-certificate-policies", Map.ofEntries(
          entry("1", "policy-identifiers", Map.ofEntries(
            entry("0", "qcp-natural"),
            entry("1", "qcp-legal"),
            entry("2", "qcp-natural-qscd"),
            entry("3", "qcp-legal-qscd"),
            entry("4", "qcp-web"),
            entry("5", "qncp-web"),
            entry("6", "qncp-web-gen")
          ))
        ));
        // @formatter:on
    }
}
