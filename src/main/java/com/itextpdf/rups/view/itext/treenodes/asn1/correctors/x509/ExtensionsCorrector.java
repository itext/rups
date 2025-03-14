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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.OidCorrectorMapper;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * Corrector for the Extensions type, as it is defined in RFC 5280.
 *
 * <pre>
 * Extensions ::= SEQUENCE SIZE (1..MAX) OF Extension
 *
 * Extension ::= SEQUENCE {
 *   extnID     OBJECT IDENTIFIER,
 *   critical   BOOLEAN DEFAULT FALSE,
 *   extnValue  OCTET STRING
 *              -- contains the DER encoding of an ASN.1 value
 *              -- corresponding to the extension type identified
 *              -- by extnID
 * }
 * </pre>
 */
public final class ExtensionsCorrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final ExtensionsCorrector INSTANCE = new ExtensionsCorrector();

    private ExtensionsCorrector() {
        // singleton class
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "extensions";
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
        for (final AbstractAsn1TreeNode extension : node) {
            correctExtension(extension);
        }
    }

    /**
     * <pre>
     * Extension ::= SEQUENCE {
     *   extnID     OBJECT IDENTIFIER,
     *   critical   BOOLEAN DEFAULT FALSE,
     *   extnValue  OCTET STRING
     *              -- contains the DER encoding of an ASN.1 value
     *              -- corresponding to the extension type identified
     *              -- by extnID
     * }
     * </pre>
     */
    private static void correctExtension(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("extension");
        String oid = null;
        int i = 0;
        if (node.getChildCount() > i) {
            oid = correctUniversalObjectIdentifier(node.getChildAt(i), "extnID");
            ++i;
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode critical = node.getChildAt(i);
            if (isUniversalType(critical, ASN1Boolean.class)) {
                critical.setRfcFieldName("critical");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode extnValue = node.getChildAt(i);
            if (isUniversalType(extnValue, ASN1OctetString.class)) {
                extnValue.setRfcFieldName("extnValue");
                final AbstractAsn1TreeNode child = Asn1TreeNodeFactory.fromPrimitive(
                        ((ASN1OctetString) extnValue.getAsn1Primitive()).getOctets()
                );
                if (child != null) {
                    OidCorrectorMapper.get(oid).correct(child);
                    extnValue.add(child);
                }
            }
        }
    }
}
