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

import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class Asn1Utf8StringTreeNodeTest {
    @Test
    void toString_Empty() {
        final ASN1UTF8String obj = new DERUTF8String("");
        final Asn1Utf8StringTreeNode node = new Asn1Utf8StringTreeNode(obj);
        Asn1TestUtil.assertNodeMatches(0, "UTF8String: ", node);
    }

    @Test
    void toString_Regular() {
        final ASN1UTF8String obj = new DERUTF8String("Юзер");
        final Asn1Utf8StringTreeNode node = new Asn1Utf8StringTreeNode(obj);
        Asn1TestUtil.assertNodeMatches(0, "UTF8String: Юзер", node);
    }

    @Test
    void toString_Descriptive() {
        final ASN1UTF8String obj = new DERUTF8String("Юзер");
        final Asn1Utf8StringTreeNode node = new Asn1Utf8StringTreeNode(obj);
        node.setRfcFieldName("name");
        node.setValueExplanation("RFC");
        Asn1TestUtil.assertNodeMatches(0, "name: Юзер (RFC)", node);
    }
}
