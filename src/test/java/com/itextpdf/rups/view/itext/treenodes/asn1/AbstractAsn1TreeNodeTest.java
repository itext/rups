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
package com.itextpdf.rups.view.itext.treenodes.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class AbstractAsn1TreeNodeTest {
    @Test
    void iterator_Primitive() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERPrintableString("AZ")
        );
        Assertions.assertNotNull(node);
        final Iterator<AbstractAsn1TreeNode> it = node.iterator();
        Assertions.assertNotNull(it);

        Assertions.assertFalse(it.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void iterator_Constructed() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        DERNull.INSTANCE,
                        new DERPrintableString("AZ"),
                })
        );
        Assertions.assertNotNull(node);
        final Iterator<AbstractAsn1TreeNode> it = node.iterator();
        Assertions.assertNotNull(it);

        Assertions.assertTrue(it.hasNext());
        final AbstractAsn1TreeNode firstChild = it.next();
        Assertions.assertNotNull(firstChild);
        Asn1TestUtil.assertNodeMatches(0, "NULL: NULL", firstChild);

        Assertions.assertTrue(it.hasNext());
        final AbstractAsn1TreeNode secondChild = it.next();
        Assertions.assertNotNull(secondChild);
        Asn1TestUtil.assertNodeMatches(0, "PrintableString: AZ", secondChild);

        Assertions.assertFalse(it.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void iPdfContextMenuTargetImplementation() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(DERNull.INSTANCE);
        Assertions.assertFalse(node.supportsInspectObject());
        Assertions.assertTrue(node.supportsSave());
    }

    @Test
    void toDisplayJson() throws IOException {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("1.2"),
                                new ASN1Integer(1),
                        }),
                        new DERTaggedObject(true, 1, ASN1Boolean.getInstance(false))
                })
        );
        node.getChildAt(0).setRfcFieldName("first");
        node.getChildAt(1).setRfcFieldName("second");
        node.getChildAt(1).setValueExplanation("NOT-CRITICAL");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        node.toDisplayJson(baos);
        Assertions.assertEquals(
                String.format("{%n" +
                        "  \"type\": \"SEQUENCE\",%n" +
                        "  \"children\": [%n" +
                        "    {%n" +
                        "      \"name\": \"first\",%n" +
                        "      \"type\": \"SEQUENCE\",%n" +
                        "      \"children\": [%n" +
                        "        {%n" +
                        "          \"type\": \"OBJECT IDENTIFIER\",%n" +
                        "          \"value\": \"1.2\",%n" +
                        "          \"explanation\": \"/iso/member-body\"%n" +
                        "        },%n" +
                        "        {%n" +
                        "          \"type\": \"INTEGER\",%n" +
                        "          \"value\": \"1\"%n" +
                        "        }%n" +
                        "      ]%n" +
                        "    },%n" +
                        "    {%n" +
                        "      \"name\": \"second\",%n" +
                        "      \"type\": \"[1] EXPLICIT BOOLEAN\",%n" +
                        "      \"value\": \"FALSE\",%n" +
                        "      \"explanation\": \"NOT-CRITICAL\"%n" +
                        "    }%n" +
                        "  ]%n" +
                        "}"),
                baos.toString(StandardCharsets.UTF_8)
        );
    }
}
