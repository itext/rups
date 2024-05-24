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
package com.itextpdf.rups.view.itext.treenodes.asn1;

import java.util.Locale;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.util.encoders.Hex;

/**
 * Tree node for showing BIT STRING ASN.1 objects.
 */
public final class Asn1BitStringTreeNode extends AbstractAsn1TreeNode {
    /**
     * Icon to use for BIT STRING ASN.1 objects.
     */
    private static final String ICON = "string.png";
    /**
     * Up to this number of bytes we will use a binary display instead of hex.
     */
    private static final int BINARY_DISPLAY_LIMIT = 5;

    /**
     * Creates a new tree node for a BIT STRING ASN.1 object.
     *
     * @param string BIT STRING ASN.1 object.
     */
    public Asn1BitStringTreeNode(ASN1BitString string) {
        super(ICON, string);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsn1Type() {
        return "BIT STRING";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsn1DisplayValue() {
        final ASN1BitString bitString = getAsn1Primitive();
        final byte[] bytes = bitString.getBytes();
        final int padBits = bitString.getPadBits();
        // If it is short enough, we display in binary instead of hex
        if (bytes.length < BINARY_DISPLAY_LIMIT) {
            final StringBuilder sb = new StringBuilder(2 + Byte.SIZE * bytes.length - padBits);
            sb.append("0b");
            for (int i = 0; i < bytes.length - 1; ++i) {
                appendBinaryString(sb, bytes[i], 0);
            }
            appendBinaryString(sb, bytes[bytes.length - 1], padBits);
            return sb.toString();
        }
        // This is not entirely correct, as if there are pad bits, then the
        // actual value is shorter than what is shown, but it would at least
        // be readable compared to a binary representation...
        return "0x" + Hex.toHexString(bytes).toUpperCase(Locale.ROOT);
    }

    private static void appendBinaryString(StringBuilder sb, byte octet, int padBits) {
        byte value = octet;
        for (int i = 0; i < Byte.SIZE - padBits; ++i) {
            sb.append(((value & 0x80) == 0) ? '0' : '1');
            value <<= 1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ASN1BitString getAsn1Primitive() {
        return (ASN1BitString) super.getAsn1Primitive();
    }
}
