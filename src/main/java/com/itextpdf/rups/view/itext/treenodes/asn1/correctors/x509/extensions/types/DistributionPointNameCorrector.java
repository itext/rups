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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions.types;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TaggedObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.GeneralNamesCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.types.RelativeDistinguishedNameCorrector;

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;

/**
 * Corrector for the DistributionPointName type, as it is defined in RFC 5280.
 *
 * <pre>
 * DistributionPointName ::= CHOICE {
 *   fullName                   [0] IMPLICIT GeneralNames,
 *   nameRelativeToCRLIssuer    [1] IMPLICIT RelativeDistinguishedName
 * }
 * </pre>
 */
public final class DistributionPointNameCorrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final DistributionPointNameCorrector INSTANCE = new DistributionPointNameCorrector();

    private DistributionPointNameCorrector() {
        // singleton class
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "distributionPointName";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void correct(AbstractAsn1TreeNode node) {
        // Because this is a CHOICE, we have special handling for default name
        correct(node, (String) null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void correct(AbstractAsn1TreeNode node, ASN1Primitive obj) {
        // Because this is a CHOICE, we have special handling for default name
        correct(node, obj, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void correct(AbstractAsn1TreeNode node, ASN1Primitive obj, String variableName) {
        if (isImplicitContextSpecificType(obj, 0)) {
            final ASN1TaggedObject newObj = fixImplicitContextSpecificObject(
                    (Asn1TaggedObjectTreeNode) node,
                    (ASN1TaggedObject) obj,
                    ASN1Sequence::getInstance
            );
            GeneralNamesCorrector.INSTANCE.correct(
                    node,
                    getBaseObject(newObj),
                    getName(0, variableName)
            );
        } else if (isImplicitContextSpecificType(obj, 1)) {
            final ASN1TaggedObject newObj = fixImplicitContextSpecificObject(
                    (Asn1TaggedObjectTreeNode) node,
                    (ASN1TaggedObject) obj,
                    ASN1Set::getInstance
            );
            RelativeDistinguishedNameCorrector.INSTANCE.correct(
                    node,
                    getBaseObject(newObj),
                    getName(1, variableName)
            );
        }
    }

    private static final String[] DEFAULT_VARIABLE_NAMES = {
            "fullName",
            "nameRelativeToCRLIssuer",
    };

    private static String getName(int tagNo, String variableName) {
        assert (0 <= tagNo) && (tagNo < DEFAULT_VARIABLE_NAMES.length);
        if (variableName != null) {
            return variableName;
        }
        return DEFAULT_VARIABLE_NAMES[tagNo];
    }
}
