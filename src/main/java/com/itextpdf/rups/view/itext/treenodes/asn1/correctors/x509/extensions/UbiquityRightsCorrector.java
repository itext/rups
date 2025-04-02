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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.extensions;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1BooleanTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * Corrector for the proprietary TimeStamp extension from Adobe.
 *
 * <pre>
 * UbiquityRights ::= SEQUENCE {
 *   version        INTEGER { v1(1) }, -- extension version
 *   ubSubRights    UBSubRights,
 *   mode           DeploymentMode
 * }
 *
 * UBSubRights ::= BIT STRING {
 *   FormFillInAndSave(0),
 *   FormImportExport(1),
 *   FormAddDelete(2),
 *   SubmitStandalone(3),
 *   SpawnTemplate(4),
 *   Signing(5),
 *   AnnotModify(6),
 *   AnnotImportExport(7),
 *   BarcodePlaintext(8),
 *   AnnotOnline(9),
 *   FormOnline(10),
 *   EFModify(11)
 * }
 *
 * DeploymentMode ::= ENUMERATED {
 *   evaluation (0), -- Eval cert. Docs are disabled when certificate is invalid.
 *   production (1)  -- Production cert. Docs remain valid for eternity.
 * }
 * </pre>
 */
public final class UbiquityRightsCorrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final UbiquityRightsCorrector INSTANCE = new UbiquityRightsCorrector();

    private UbiquityRightsCorrector() {
        // singleton class
    }

    /**
     * OBJECT IDENTIFIER for the type, which is handled by the corrector.
     */
    public static final String OID = "1.2.840.113583.1.1.7.1";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "ubiquityRights";
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
            correctVersion(node.getChildAt(0));
        }
        if (node.getChildCount() > 1) {
            correctSubRights(node.getChildAt(1));
        }
        if (node.getChildCount() > 2) {
            correctDeploymentMode(node.getChildAt(2));
        }
    }

    /**
     * <pre>
     * Version ::= INTEGER { v1(1) }
     * </pre>
     */
    private static void correctVersion(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Integer.class)) {
            return;
        }
        node.setRfcFieldName("version");
        final ASN1Integer nodeValue = (ASN1Integer) node.getAsn1Primitive();
        if (nodeValue.hasValue(1)) {
            node.setValueExplanation("v1");
        }
    }

    private static final String[] SUB_RIGHTS_NAMES = {
            "FormFillInAndSave",
            "FormImportExport",
            "FormAddDelete",
            "SubmitStandalone",
            "SpawnTemplate",
            "Signing",
            "AnnotModify",
            "AnnotImportExport",
            "BarcodePlaintext",
            "AnnotOnline",
            "FormOnline",
            "EFModify",
    };

    /**
     * <pre>
     * UBSubRights ::= BIT STRING {
     *   FormFillInAndSave(0),
     *   FormImportExport(1),
     *   FormAddDelete(2),
     *   SubmitStandalone(3),
     *   SpawnTemplate(4),
     *   Signing(5),
     *   AnnotModify(6),
     *   AnnotImportExport(7),
     *   BarcodePlaintext(8),
     *   AnnotOnline(9),
     *   FormOnline(10),
     *   EFModify(11)
     * }
     * </pre>
     */
    private static void correctSubRights(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1BitString.class)) {
            return;
        }
        node.setRfcFieldName("ubSubRights");
        // Will add nodes to "decipher" the bit string
        final byte[] flags = ((ASN1BitString) node.getAsn1Primitive()).getBytes();
        for (int i = 0; i < SUB_RIGHTS_NAMES.length; ++i) {
            node.add(new Asn1BooleanTreeNode(SUB_RIGHTS_NAMES[i], hasFlag(flags, i)));
        }
    }

    private static final String[] DEPLOYMENT_MODE_LABELS = {
            "evaluation",
            "production",
    };

    /**
     * <pre>
     * DeploymentMode ::= ENUMERATED {
     *   evaluation (0), -- Eval cert. Docs are disabled when certificate is invalid.
     *   production (1)  -- Production cert. Docs remain valid for eternity.
     * }
     * </pre>
     */
    private static void correctDeploymentMode(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Enumerated.class)) {
            return;
        }
        node.setRfcFieldName("mode");
        final BigInteger nodeValue = ((ASN1Enumerated) node.getAsn1Primitive()).getValue();
        if (isNumberInRange(nodeValue, DEPLOYMENT_MODE_LABELS.length)) {
            node.setValueExplanation(DEPLOYMENT_MODE_LABELS[nodeValue.intValue()]);
        }
    }
}
