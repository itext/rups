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
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TaggedObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.OidCorrectorMapper;

import java.util.List;
import org.bouncycastle.asn1.ASN1BMPString;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1T61String;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.ASN1UniversalString;

/**
 * Corrector for the GeneralName type, as it is defined in RFC 5280.
 *
 * <pre>
 * GeneralName ::= CHOICE {
 *   otherName                  [0] IMPLICIT OtherName,
 *   rfc822Name                 [1] IMPLICIT IA5String,
 *   dNSName                    [2] IMPLICIT IA5String,
 *   x400Address                [3] IMPLICIT ORAddress,
 *   directoryName              [4] EXPLICIT Name,
 *   ediPartyName               [5] IMPLICIT EDIPartyName,
 *   uniformResourceIdentifier  [6] IMPLICIT IA5String,
 *   iPAddress                  [7] IMPLICIT OCTET STRING,
 *   registeredID               [8] IMPLICIT OBJECT IDENTIFIER
 * }
 *
 * OtherName ::= SEQUENCE {
 *   type-id    OBJECT IDENTIFIER,
 *   value      [0] EXPLICIT ANY DEFINED BY type-id
 * }
 *
 * EDIPartyName ::= SEQUENCE {
 *   nameAssigner   [0] EXPLICIT DirectoryString OPTIONAL,
 *   partyName      [1] EXPLICIT DirectoryString
 * }
 *
 * DirectoryString ::= CHOICE {
 *   teletexString      TeletexString (SIZE (1..MAX)),
 *   printableString    PrintableString (SIZE (1..MAX)),
 *   universalString    UniversalString (SIZE (1..MAX)),
 *   utf8String         UTF8String (SIZE (1..MAX)),
 *   bmpString          BMPString (SIZE (1..MAX))
 * }
 * </pre>
 *
 * <p><tt>ORAddress</tt> correction is not implemented and the subtree will
 * remain untouched (with the exception of the subtree root node name)</p>
 */
public final class GeneralNameCorrector extends AbstractCorrector {
    /**
     * Singleton instance of the corrector.
     */
    public static final GeneralNameCorrector INSTANCE = new GeneralNameCorrector();

    private GeneralNameCorrector() {
        // singleton class
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultVariableName() {
        return "generalName";
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
            correctOtherName(
                    (Asn1TaggedObjectTreeNode) node,
                    (ASN1TaggedObject) obj,
                    getName(0, variableName)
            );
        } else if (isImplicitContextSpecificType(obj, 1)) {
            correctIa5String(
                    (Asn1TaggedObjectTreeNode) node,
                    (ASN1TaggedObject) obj,
                    getName(1, variableName)
            );
        } else if (isImplicitContextSpecificType(obj, 2)) {
            correctIa5String(
                    (Asn1TaggedObjectTreeNode) node,
                    (ASN1TaggedObject) obj,
                    getName(2, variableName)
            );
        } else if (isImplicitContextSpecificType(obj, 3)) {
            /*
             * This branch is insane as it requires implementing correctors
             * for a ton of X.400 types, which are not used anywhere else.
             * Also, realistically, you wouldn't see this branch in the wild,
             * as X.400 has been dead for ages...
             *
             * So it will only set the name of the subtree node and that's it.
             */
            correctSequence(
                    (Asn1TaggedObjectTreeNode) node,
                    (ASN1TaggedObject) obj,
                    getName(3, variableName)
            );
        } else if (isExplicitContextSpecificType(obj, 4)) {
            NameCorrector.INSTANCE.correct(node, getBaseObjectUnchecked(obj), getName(4, variableName));
        } else if (isImplicitContextSpecificType(obj, 5)) {
            correctEdiPartyName(
                    (Asn1TaggedObjectTreeNode) node,
                    (ASN1TaggedObject) obj,
                    getName(5, variableName)
            );
        } else if (isImplicitContextSpecificType(obj, 6)) {
            correctIa5String(
                    (Asn1TaggedObjectTreeNode) node,
                    (ASN1TaggedObject) obj,
                    getName(6, variableName)
            );
        } else if (isImplicitContextSpecificType(obj, 7)) {
            correctOctetString(
                    (Asn1TaggedObjectTreeNode) node,
                    (ASN1TaggedObject) obj,
                    getName(7, variableName)
            );
        } else if (isImplicitContextSpecificType(obj, 8)) {
            correctObjectIdentifier(
                    (Asn1TaggedObjectTreeNode) node,
                    (ASN1TaggedObject) obj,
                    getName(8, variableName)
            );
        }
    }

    private static boolean correctSequence(
            Asn1TaggedObjectTreeNode node,
            ASN1TaggedObject obj,
            String variableName
    ) {
        final ASN1TaggedObject newObj =
                fixImplicitContextSpecificObject(node, obj, ASN1Sequence::getInstance);
        if (!isUniversalType(getBaseObject(newObj), ASN1Sequence.class)) {
            return false;
        }
        node.setRfcFieldName(variableName);
        return true;
    }

    private static void correctIa5String(
            Asn1TaggedObjectTreeNode node,
            ASN1TaggedObject obj,
            String variableName
    ) {
        final ASN1TaggedObject newObj =
                fixImplicitContextSpecificObject(node, obj, ASN1IA5String::getInstance);
        if (!isUniversalType(getBaseObject(newObj), ASN1IA5String.class)) {
            return;
        }
        node.setRfcFieldName(variableName);
    }

    /**
     * <pre>
     * OtherName ::= SEQUENCE {
     *   type-id    OBJECT IDENTIFIER,
     *   value      [0] EXPLICIT ANY DEFINED BY type-id
     * }
     * </pre>
     */
    private static void correctOtherName(
            Asn1TaggedObjectTreeNode node,
            ASN1TaggedObject obj,
            String variableName
    ) {
        final ASN1TaggedObject newObj =
                fixImplicitContextSpecificObject(node, obj, ASN1Sequence::getInstance);
        if (!isUniversalType(getBaseObject(newObj), ASN1Sequence.class)) {
            return;
        }
        node.setRfcFieldName(variableName);
        String oid = null;
        if (node.getChildCount() > 0) {
            oid = correctUniversalObjectIdentifier(node.getChildAt(0), "type-id");
        }
        if (node.getChildCount() > 1) {
            final AbstractAsn1TreeNode value = node.getChildAt(1);
            if (isExplicitContextSpecificType(value, 0)) {
                OidCorrectorMapper.get(oid).correct(value, getBaseObjectUnchecked(value), "value");
            }
        }
    }

    /**
     * <pre>
     * EDIPartyName ::= SEQUENCE {
     *   nameAssigner   [0] EXPLICIT DirectoryString OPTIONAL,
     *   partyName      [1] EXPLICIT DirectoryString
     * }
     * </pre>
     */
    private static void correctEdiPartyName(
            Asn1TaggedObjectTreeNode node,
            ASN1TaggedObject obj,
            String variableName
    ) {
        if (!correctSequence(node, obj, variableName)) {
            return;
        }
        int i = 0;
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode nameAssigner = node.getChildAt(i);
            if (isExplicitContextSpecificType(nameAssigner, 0)) {
                correctDirectoryString((Asn1TaggedObjectTreeNode) nameAssigner, "nameAssigner");
                ++i;
            }
        }
        if (node.getChildCount() > i) {
            final AbstractAsn1TreeNode partyName = node.getChildAt(i);
            if (isExplicitContextSpecificType(partyName, 1)) {
                correctDirectoryString((Asn1TaggedObjectTreeNode) partyName, "partyName");
            }
        }
    }

    private static final List<Class<? extends ASN1Primitive>> VALID_DIRECTORY_STRING_TYPES = List.of(
            ASN1T61String.class,
            ASN1PrintableString.class,
            ASN1UniversalString.class,
            ASN1UTF8String.class,
            ASN1BMPString.class
    );

    /**
     * <pre>
     * DirectoryString ::= CHOICE {
     *   teletexString      TeletexString (SIZE (1..MAX)),
     *   printableString    PrintableString (SIZE (1..MAX)),
     *   universalString    UniversalString (SIZE (1..MAX)),
     *   utf8String         UTF8String (SIZE (1..MAX)),
     *   bmpString          BMPString (SIZE (1..MAX))
     * }
     * </pre>
     */
    private static void correctDirectoryString(Asn1TaggedObjectTreeNode node, String variableName) {
        final ASN1Primitive baseObj = getBaseObject(node);
        for (final Class<? extends ASN1Primitive> type : VALID_DIRECTORY_STRING_TYPES) {
            if (isUniversalType(baseObj, type)) {
                node.setRfcFieldName(variableName);
                return;
            }
        }
    }

    private static void correctOctetString(
            Asn1TaggedObjectTreeNode node,
            ASN1TaggedObject obj,
            String variableName
    ) {
        final ASN1TaggedObject newObj =
                fixImplicitContextSpecificObject(node, obj, ASN1OctetString::getInstance);
        if (!isUniversalType(getBaseObject(newObj), ASN1OctetString.class)) {
            return;
        }
        node.setRfcFieldName(variableName);
    }

    private static void correctObjectIdentifier(
            Asn1TaggedObjectTreeNode node,
            ASN1TaggedObject obj,
            String variableName
    ) {
        final ASN1TaggedObject newObj =
                fixImplicitContextSpecificObject(node, obj, ASN1ObjectIdentifier::getInstance);
        if (!isUniversalType(getBaseObject(newObj), ASN1ObjectIdentifier.class)) {
            return;
        }
        node.setRfcFieldName(variableName);
    }

    private static final String[] DEFAULT_VARIABLE_NAMES = {
            "otherName",
            "rfc822Name",
            "dnsName",
            "x400Address",
            "directoryName",
            "ediPartyName",
            "uniformResourceIdentifier",
            "ipAddress",
            "registeredID",
    };

    private static String getName(int tagNo, String variableName) {
        assert (0 <= tagNo) && (tagNo < DEFAULT_VARIABLE_NAMES.length);
        if (variableName != null) {
            return variableName;
        }
        return DEFAULT_VARIABLE_NAMES[tagNo];
    }
}
