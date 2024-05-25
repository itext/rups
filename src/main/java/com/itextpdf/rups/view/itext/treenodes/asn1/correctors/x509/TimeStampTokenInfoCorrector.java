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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TaggedObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;

/**
 * Corrector for the TSTInfo type, as it is defined in RFC 3161.
 *
 * <pre>
 * TSTInfo ::= SEQUENCE {
 *   version            INTEGER { v1(1) },
 *   policy             TSAPolicyId,
 *   messageImprint     MessageImprint,
 *   serialNumber       INTEGER,
 *   genTime            GeneralizedTime,
 *   accuracy           Accuracy OPTIONAL,
 *   ordering           BOOLEAN DEFAULT FALSE,
 *   nonce              INTEGER OPTIONAL,
 *   tsa                [0] EXPLICIT GeneralName OPTIONAL,
 *   extensions         [1] IMPLICIT Extensions OPTIONAL
 * }
 *
 * TSAPolicyId ::= OBJECT IDENTIFIER
 *
 * MessageImprint ::= SEQUENCE {
 *   hashAlgorithm      AlgorithmIdentifier,
 *   hashedMessage      OCTET STRING
 * }
 *
 * Accuracy ::= SEQUENCE {
 *   seconds    INTEGER OPTIONAL,
 *   millis     [0] IMPLICIT INTEGER (1..999) OPTIONAL,
 *   micros     [1] IMPLICIT INTEGER (1..999) OPTIONAL
 * }
 * </pre>
 */
public final class TimeStampTokenInfoCorrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final TimeStampTokenInfoCorrector INSTANCE = new TimeStampTokenInfoCorrector();

    private TimeStampTokenInfoCorrector() {
        // singleton class
    }

    /**
     * OBJECT IDENTIFIER for the type, which is handled by the corrector.
     */
    public static final String OID = "1.2.840.113549.1.9.16.1.4";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "tstInfo";
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
        int i = 0;
        if (node.getChildCount() > i) {
            correctVersion(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            correctUniversalObjectIdentifier(node.getChildAt(i), "policy");
            ++i;
        }
        if (node.getChildCount() > i) {
            correctMessageImprint(node.getChildAt(i));
            ++i;
        }
        if (node.getChildCount() > i) {
            correctPrimitiveUniversalType(node.getChildAt(i), ASN1Integer.class, "serialNumber");
            ++i;
        }
        if (node.getChildCount() > i) {
            correctPrimitiveUniversalType(node.getChildAt(i), ASN1GeneralizedTime.class, "genTime");
            ++i;
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode accuracy = node.getChildAt(i);
            if (isUniversalType(accuracy, ASN1Sequence.class)) {
                correctAccuracy(accuracy);
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode ordering = node.getChildAt(i);
            if (isUniversalType(ordering, ASN1Boolean.class)) {
                ordering.setRfcFieldName("ordering");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode nonce = node.getChildAt(i);
            if (isUniversalType(nonce, ASN1Integer.class)) {
                nonce.setRfcFieldName("nonce");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode tsa = node.getChildAt(i);
            if (isExplicitContextSpecificType(tsa, 0)) {
                GeneralNameCorrector.INSTANCE.correct(tsa, getBaseObjectUnchecked(tsa), "tsa");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode extensions = node.getChildAt(i);
            if (isImplicitContextSpecificType(extensions, 1)) {
                final ASN1TaggedObject extensionsObj = fixImplicitContextSpecificObject(
                        (Asn1TaggedObjectTreeNode) extensions,
                        ASN1Sequence::getInstance
                );
                ExtensionsCorrector.INSTANCE.correct(
                        extensions,
                        getBaseObject(extensionsObj),
                        "extensions"
                );
            }
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

    /**
     * <pre>
     * MessageImprint ::= SEQUENCE {
     *   hashAlgorithm      AlgorithmIdentifier,
     *   hashedMessage      OCTET STRING
     * }
     * </pre>
     */
    private static void correctMessageImprint(AbstractAsn1TreeNode node) {
        if (!isUniversalType(node, ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName("messageImprint");
        if (node.getChildCount() > 0) {
            AlgorithmIdentifierCorrector.INSTANCE.correct(node.getChildAt(0), "hashAlgorithm");
        }
        if (node.getChildCount() > 1) {
            correctPrimitiveUniversalType(node.getChildAt(1), ASN1OctetString.class, "hashedMessage");
        }
    }

    /**
     * <pre>
     * Accuracy ::= SEQUENCE {
     *   seconds    INTEGER OPTIONAL,
     *   millis     [0] IMPLICIT INTEGER (1..999) OPTIONAL,
     *   micros     [1] IMPLICIT INTEGER (1..999) OPTIONAL
     * }
     * </pre>
     */
    private static void correctAccuracy(AbstractAsn1TreeNode node) {
        node.setRfcFieldName("accuracy");
        int i = 0;
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode seconds = node.getChildAt(i);
            if (isUniversalType(seconds, ASN1Integer.class)) {
                seconds.setRfcFieldName("seconds");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode millis = node.getChildAt(i);
            if (isImplicitContextSpecificType(millis, 0)) {
                correctImplicitInteger((Asn1TaggedObjectTreeNode) millis, "millis");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode micros = node.getChildAt(i);
            if (isImplicitContextSpecificType(micros, 1)) {
                correctImplicitInteger((Asn1TaggedObjectTreeNode) micros, "micros");
            }
        }
    }

    private static void correctImplicitInteger(Asn1TaggedObjectTreeNode node, String variableName) {
        final ASN1TaggedObject obj = fixImplicitContextSpecificObject(node, ASN1Integer::getInstance);
        if (!isUniversalType(getBaseObject(obj), ASN1Integer.class)) {
            return;
        }
        node.setRfcFieldName(variableName);
    }
}
