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
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERT61String;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.DERUniversalString;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class GeneralNameCorrectorTest {
    @Test
    void correct_OtherNameWithDefaultName() {
        final AbstractAsn1TreeNode node = createOtherNameNode();
        GeneralNameCorrector.INSTANCE.correct(node);
        validateOtherNameNode(node, "otherName");
    }

    @Test
    void correct_OtherNameWithoutDefaultName() {
        final AbstractAsn1TreeNode node = createOtherNameNode();
        GeneralNameCorrector.INSTANCE.correct(node, "name");
        validateOtherNameNode(node, "name");
    }

    @Test
    void correct_OtherNameBehindTag() {
        final ASN1Primitive obj = createOtherNameNode().getAsn1Primitive();
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 17, obj)
        );
        GeneralNameCorrector.INSTANCE.correct(node, obj);
        validateOtherNameNode(node, "otherName");
    }

    @Test
    void correct_OtherNameInvalidBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 0, new ASN1Integer(1))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "[0] IMPLICIT INTEGER: 1", node);
    }

    @Test
    void correct_OtherNameEmptySequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 0, new DERSequence())
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "otherName", node);
    }

    @Test
    void correct_OtherNameInvalidSequenceFieldTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 0, new DERSequence(new ASN1Encodable[] {
                        new ASN1Integer(0),
                        new ASN1Integer(1),
                }))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "otherName", node);
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 0", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "INTEGER: 1", node.getChildAt(1));
    }

    @Test
    void correct_Rfc822NameWithDefaultName() {
        final AbstractAsn1TreeNode node = createRfc822NameNode();
        GeneralNameCorrector.INSTANCE.correct(node);
        validateRfc822NameNode(node, "rfc822Name");
    }

    @Test
    void correct_Rfc822NameWithoutDefaultName() {
        final AbstractAsn1TreeNode node = createRfc822NameNode();
        GeneralNameCorrector.INSTANCE.correct(node, "name");
        validateRfc822NameNode(node, "name");
    }

    @Test
    void correct_Rfc822NameBehindTag() {
        final ASN1Primitive obj = createRfc822NameNode().getAsn1Primitive();
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 17, obj)
        );
        GeneralNameCorrector.INSTANCE.correct(node, obj);
        validateRfc822NameNode(node, "rfc822Name");
    }

    @Test
    void correct_Rfc822NameInvalidBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 1, new ASN1Integer(1))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "[1] IMPLICIT INTEGER: 1", node);
    }

    @Test
    void correct_DnsNameWithDefaultName() {
        final AbstractAsn1TreeNode node = createDnsNameNode();
        GeneralNameCorrector.INSTANCE.correct(node);
        validateDnsNameNode(node, "dnsName");
    }

    @Test
    void correct_DnsNameWithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDnsNameNode();
        GeneralNameCorrector.INSTANCE.correct(node, "name");
        validateDnsNameNode(node, "name");
    }

    @Test
    void correct_DnsNameBehindTag() {
        final ASN1Primitive obj = createDnsNameNode().getAsn1Primitive();
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 17, obj)
        );
        GeneralNameCorrector.INSTANCE.correct(node, obj);
        validateDnsNameNode(node, "dnsName");
    }

    @Test
    void correct_DnsNameInvalidBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 2, new ASN1Integer(1))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "[2] IMPLICIT INTEGER: 1", node);
    }

    @Test
    void correct_X400AddressWithDefaultName() {
        final AbstractAsn1TreeNode node = createX400AddressNode();
        GeneralNameCorrector.INSTANCE.correct(node);
        validateX400AddressNode(node, "x400Address");
    }

    @Test
    void correct_X400AddressWithoutDefaultName() {
        final AbstractAsn1TreeNode node = createX400AddressNode();
        GeneralNameCorrector.INSTANCE.correct(node, "name");
        validateX400AddressNode(node, "name");
    }

    @Test
    void correct_X400AddressBehindTag() {
        final ASN1Primitive obj = createX400AddressNode().getAsn1Primitive();
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 17, obj)
        );
        GeneralNameCorrector.INSTANCE.correct(node, obj);
        validateX400AddressNode(node, "x400Address");
    }

    @Test
    void correct_X400AddressInvalidBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 3, new ASN1Integer(1))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "[3] IMPLICIT INTEGER: 1", node);
    }

    @Test
    void correct_DirectoryNameWithDefaultName() {
        final AbstractAsn1TreeNode node = createDirectoryNameNode();
        GeneralNameCorrector.INSTANCE.correct(node);
        validateDirectoryNameNode(node, "directoryName");
    }

    @Test
    void correct_DirectoryNameWithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDirectoryNameNode();
        GeneralNameCorrector.INSTANCE.correct(node, "name");
        validateDirectoryNameNode(node, "name");
    }

    @Test
    void correct_DirectoryNameBehindTag() {
        final ASN1Primitive obj = createDirectoryNameNode().getAsn1Primitive();
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 17, obj)
        );
        GeneralNameCorrector.INSTANCE.correct(node, obj);
        validateDirectoryNameNode(node, "directoryName");
    }

    @Test
    void correct_DirectoryNameInvalidBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 4, new ASN1Integer(1))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "[4] EXPLICIT INTEGER: 1", node);
    }

    @Test
    void correct_EdiPartyNameWithDefaultName() {
        final AbstractAsn1TreeNode node = createEdiPartyNameNode();
        GeneralNameCorrector.INSTANCE.correct(node);
        validateEdiPartyNameNode(node, "ediPartyName");
    }

    @Test
    void correct_EdiPartyNameWithoutDefaultName() {
        final AbstractAsn1TreeNode node = createEdiPartyNameNode();
        GeneralNameCorrector.INSTANCE.correct(node, "name");
        validateEdiPartyNameNode(node, "name");
    }

    @Test
    void correct_EdiPartyNameBehindTag() {
        final ASN1Primitive obj = createEdiPartyNameNode().getAsn1Primitive();
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 17, obj)
        );
        GeneralNameCorrector.INSTANCE.correct(node, obj);
        validateEdiPartyNameNode(node, "ediPartyName");
    }

    @Test
    void correct_EdiPartyNameInvalidBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 5, new ASN1Integer(1))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "[5] IMPLICIT INTEGER: 1", node);
    }

    @Test
    void correct_EdiPartyNameEmptySequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 5, new DERSequence())
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "ediPartyName", node);
    }

    @Test
    void correct_EdiPartyNameUniversalSequenceFieldTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 5, new DERSequence(new ASN1Encodable[] {
                        new DERPrintableString("A"),
                        new DERPrintableString("B"),
                }))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "ediPartyName", node);
        Asn1TestUtil.assertNodeMatches(0, "PrintableString: A", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "PrintableString: B", node.getChildAt(1));
    }

    @Test
    void correct_EdiPartyNameInvalidSequenceFieldBaseTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 5, new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0, new ASN1Integer(0)),
                        new DERTaggedObject(true, 1, new ASN1Integer(1)),
                }))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "ediPartyName", node);
        Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT INTEGER: 0", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT INTEGER: 1", node.getChildAt(1));
    }

    @Test
    void correct_EdiPartyNameTeletexDirectoryString() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 5, new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0, new DERT61String("A")),
                        new DERTaggedObject(true, 1, new DERT61String("B")),
                }))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "ediPartyName", node);
        Asn1TestUtil.assertNodeMatches(0, "nameAssigner: A", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "partyName: B", node.getChildAt(1));
    }

    @Test
    void correct_EdiPartyNamePrintableDirectoryString() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 5, new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0, new DERPrintableString("A")),
                        new DERTaggedObject(true, 1, new DERPrintableString("B")),
                }))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "ediPartyName", node);
        Asn1TestUtil.assertNodeMatches(0, "nameAssigner: A", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "partyName: B", node.getChildAt(1));
    }

    @Test
    void correct_EdiPartyNameUniversalDirectoryString() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 5, new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0, new DERUniversalString(new byte[0])),
                        new DERTaggedObject(true, 1, new DERUniversalString(new byte[0])),
                }))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(2, "ediPartyName", node);
        Asn1TestUtil.assertNodeMatches(0, "nameAssigner: #1C00", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "partyName: #1C00", node.getChildAt(1));
    }

    @Test
    void correct_UriWithDefaultName() {
        final AbstractAsn1TreeNode node = createUriNode();
        GeneralNameCorrector.INSTANCE.correct(node);
        validateUriNode(node, "uniformResourceIdentifier");
    }

    @Test
    void correct_UriWithoutDefaultName() {
        final AbstractAsn1TreeNode node = createUriNode();
        GeneralNameCorrector.INSTANCE.correct(node, "name");
        validateUriNode(node, "name");
    }

    @Test
    void correct_UriBehindTag() {
        final ASN1Primitive obj = createUriNode().getAsn1Primitive();
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 17, obj)
        );
        GeneralNameCorrector.INSTANCE.correct(node, obj);
        validateUriNode(node, "uniformResourceIdentifier");
    }

    @Test
    void correct_UriInvalidBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 6, new ASN1Integer(1))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "[6] IMPLICIT INTEGER: 1", node);
    }

    @Test
    void correct_IpAddressWithDefaultName() {
        final AbstractAsn1TreeNode node = createIpAddressNode();
        GeneralNameCorrector.INSTANCE.correct(node);
        validateIpAddressNode(node, "ipAddress");
    }

    @Test
    void correct_IpAddressWithoutDefaultName() {
        final AbstractAsn1TreeNode node = createIpAddressNode();
        GeneralNameCorrector.INSTANCE.correct(node, "name");
        validateIpAddressNode(node, "name");
    }

    @Test
    void correct_IpAddressBehindTag() {
        final ASN1Primitive obj = createIpAddressNode().getAsn1Primitive();
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 17, obj)
        );
        GeneralNameCorrector.INSTANCE.correct(node, obj);
        validateIpAddressNode(node, "ipAddress");
    }

    @Test
    void correct_IpAddressInvalidBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 7, new ASN1Integer(1))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "[7] IMPLICIT INTEGER: 1", node);
    }

    @Test
    void correct_RegisteredIdWithDefaultName() {
        final AbstractAsn1TreeNode node = createRegisteredIdNode();
        GeneralNameCorrector.INSTANCE.correct(node);
        validateRegisteredIdNode(node, "registeredID");
    }

    @Test
    void correct_RegisteredIdWithoutDefaultName() {
        final AbstractAsn1TreeNode node = createRegisteredIdNode();
        GeneralNameCorrector.INSTANCE.correct(node, "name");
        validateRegisteredIdNode(node, "name");
    }

    @Test
    void correct_RegisteredIdBehindTag() {
        final ASN1Primitive obj = createRegisteredIdNode().getAsn1Primitive();
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 17, obj)
        );
        GeneralNameCorrector.INSTANCE.correct(node, obj);
        validateRegisteredIdNode(node, "registeredID");
    }

    @Test
    void correct_RegisteredIdInvalidBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 8, new ASN1Integer(1))
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "[8] IMPLICIT INTEGER: 1", node);
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERIA5String("example.com")
        );
        GeneralNameCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(0, "IA5String: example.com", node);
    }

    private static void validateOtherNameNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(2, expectedVariableName, node);
        Asn1TestUtil.assertNodeMatches(
                0,
                "type-id: 2.5.4.3 (/joint-iso-itu-t/ds/attributeType/commonName)",
                node.getChildAt(0)
        );
        Asn1TestUtil.assertNodeMatches(0, "value: Test", node.getChildAt(1));
    }

    private static AbstractAsn1TreeNode createOtherNameNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 0, new DERSequence(new ASN1Encodable[] {
                        new ASN1ObjectIdentifier("2.5.4.3"),    // commonName
                        new DERTaggedObject(true, 0, new DERPrintableString("Test")),
                }))
        );
    }

    private static void validateRfc822NameNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(0, expectedVariableName + ": example@example.com", node);
    }

    private static AbstractAsn1TreeNode createRfc822NameNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 1, new DERIA5String("example@example.com"))
        );
    }

    private static void validateDnsNameNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(0, expectedVariableName + ": example.com", node);
    }

    private static AbstractAsn1TreeNode createDnsNameNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 2, new DERIA5String("example.com"))
        );
    }

    private static void validateX400AddressNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(0, expectedVariableName, node);
    }

    private static AbstractAsn1TreeNode createX400AddressNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 3, new DERSequence())
        );
    }

    private static void validateDirectoryNameNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(0, expectedVariableName, node);
    }

    private static AbstractAsn1TreeNode createDirectoryNameNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(true, 4, new DERSequence())
        );
    }

    private static void validateEdiPartyNameNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(2, expectedVariableName, node);
        Asn1TestUtil.assertNodeMatches(0, "nameAssigner: A", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "partyName: B", node.getChildAt(1));
    }

    private static AbstractAsn1TreeNode createEdiPartyNameNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 5, new DERSequence(new ASN1Encodable[] {
                        new DERTaggedObject(true, 0, new DERUTF8String("A")),
                        new DERTaggedObject(true, 1, new DERBMPString("B")),
                }))
        );
    }

    private static void validateUriNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(0, expectedVariableName + ": https://example.com/", node);
    }

    private static AbstractAsn1TreeNode createUriNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 6, new DERIA5String("https://example.com/"))
        );
    }

    private static void validateIpAddressNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(0, expectedVariableName + ": 0x7F000001", node);
    }

    private static AbstractAsn1TreeNode createIpAddressNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 7, new DEROctetString(new byte[] {127, 0, 0, 1}))
        );
    }

    private static void validateRegisteredIdNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(0, expectedVariableName + ": 1.2 (/iso/member-body)", node);
    }

    private static AbstractAsn1TreeNode createRegisteredIdNode() {
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERTaggedObject(false, 8, new ASN1ObjectIdentifier("1.2"))
        );
    }
}