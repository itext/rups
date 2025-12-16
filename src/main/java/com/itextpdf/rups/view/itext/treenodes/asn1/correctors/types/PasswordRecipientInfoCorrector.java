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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.types;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TaggedObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractSequenceCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.SequenceFieldCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.AlgorithmIdentifierCorrector;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;

/**
 * Corrector for the PasswordRecipientInfo type, as it is defined in RFC 5652.
 *
 * <pre>
 * PasswordRecipientInfo ::= SEQUENCE {
 *   version                CMSVersion,
 *   keyDerivationAlgorithm [0] IMPLICIT KeyDerivationAlgorithmIdentifier OPTIONAL,
 *   keyEncryptionAlgorithm KeyEncryptionAlgorithmIdentifier,
 *   encryptedKey           EncryptedKey
 * }
 *
 * KeyDerivationAlgorithmIdentifier ::= AlgorithmIdentifier
 *
 * KeyEncryptionAlgorithmIdentifier ::= AlgorithmIdentifier
 *
 * EncryptedKey ::= OCTET STRING
 * </pre>
 */
public final class PasswordRecipientInfoCorrector extends AbstractSequenceCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final PasswordRecipientInfoCorrector INSTANCE =
            new PasswordRecipientInfoCorrector();

    private PasswordRecipientInfoCorrector() {
        // singleton class
    }

    /**
     * Static array of field correctors for the root sequence.
     */
    private static final SequenceFieldCorrector[] FIELD_CORRECTORS = {
            PasswordRecipientInfoCorrector::correctVersion,
            PasswordRecipientInfoCorrector::correctKeyDerivationAlgorithm,
            PasswordRecipientInfoCorrector::correctKeyEncryptionAlgorithm,
            PasswordRecipientInfoCorrector::correctEncryptedKey,
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "pwri";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SequenceFieldCorrector[] getFieldCorrectors() {
        return FIELD_CORRECTORS;
    }

    private static boolean correctVersion(AbstractAsn1TreeNode node) {
        CmsVersionCorrector.INSTANCE.correct(node, "version");
        return true;
    }

    private static boolean correctKeyDerivationAlgorithm(AbstractAsn1TreeNode node) {
        if (!isImplicitContextSpecificType(node, 0)) {
            return false;
        }
        final ASN1TaggedObject obj = fixImplicitContextSpecificObject(
                (Asn1TaggedObjectTreeNode) node, ASN1Sequence::getInstance
        );
        AlgorithmIdentifierCorrector.INSTANCE.correct(
                node, getBaseObject(obj), "keyDerivationAlgorithm"
        );
        return true;
    }

    private static boolean correctKeyEncryptionAlgorithm(AbstractAsn1TreeNode node) {
        AlgorithmIdentifierCorrector.INSTANCE.correct(node, "keyEncryptionAlgorithm");
        return true;
    }

    private static boolean correctEncryptedKey(AbstractAsn1TreeNode node) {
        correctPrimitiveUniversalType(node, ASN1OctetString.class, "encryptedKey");
        return true;
    }
}
