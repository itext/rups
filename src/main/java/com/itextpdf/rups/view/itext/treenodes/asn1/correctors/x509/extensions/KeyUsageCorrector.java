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

import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Primitive;

/**
 * Corrector for the keyUsage extension attribute, as it is
 * defined in RFC 5280.
 *
 * <pre>
 * KeyUsage ::= BIT STRING {
 *   digitalSignature   (0),
 *   nonRepudiation     (1), -- recent editions of X.509 have
 *                           -- renamed this bit to contentCommitment
 *   keyEncipherment    (2),
 *   dataEncipherment   (3),
 *   keyAgreement       (4),
 *   keyCertSign        (5),
 *   cRLSign            (6),
 *   encipherOnly       (7),
 *   decipherOnly       (8)
 * }
 * </pre>
 */
public final class KeyUsageCorrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final KeyUsageCorrector INSTANCE = new KeyUsageCorrector();

    private KeyUsageCorrector() {
        // singleton class
    }

    /**
     * OBJECT IDENTIFIER for the type, which is handled by the corrector.
     */
    public static final String OID = "2.5.29.15";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "keyUsage";
    }

    private static final String[] FLAG_NAMES = {
            "digitalSignature",
            "contentCommitment",
            "keyEncipherment",
            "dataEncipherment",
            "keyAgreement",
            "keyCertSign",
            "crlSign",
            "encipherOnly",
            "decipherOnly",
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void correct(AbstractAsn1TreeNode node, ASN1Primitive obj, String variableName) {
        if (!isUniversalType(obj, ASN1BitString.class)) {
            return;
        }
        node.setRfcFieldName(variableName);
        // Will add nodes to "decipher" the bit string
        final byte[] flags = ((ASN1BitString) obj).getBytes();
        for (int i = 0; i < FLAG_NAMES.length; ++i) {
            node.add(new Asn1BooleanTreeNode(FLAG_NAMES[i], hasFlag(flags, i)));
        }
    }
}
