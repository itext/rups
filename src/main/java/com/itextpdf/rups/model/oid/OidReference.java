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
package com.itextpdf.rups.model.oid;

import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.oid.builder.OidMainTreeBuilder;
import com.itextpdf.rups.view.Language;

import java.util.StringTokenizer;

/**
 * <p>Static class for converting OBJECT IDENTIFIER number strings into
 * descriptive display strings.</p>
 *
 * <p>Ex. "1.2.840.113549.1.7.2" -> "/iso/member-body/us/rsadsi/pkcs/pkcs-7/signedData"</p>
 */
public final class OidReference {
    private OidReference() {
        // This is a "static" class
    }

    /**
     * Returns the display string for the provided OBJECT IDENTIFIER.
     *
     * @param oid OBJECT IDENTIFIER to get the display string for.
     *
     * @return The display string.
     */
    public static String getDisplayString(String oid) {
        final StringBuilder result = new StringBuilder();
        OidTreeNode node = LazyHolder.ROOT;
        final StringTokenizer tokenizer = new StringTokenizer(oid, ".");
        while (tokenizer.hasMoreTokens()) {
            final String id = tokenizer.nextToken();
            result.append('/');
            node = node.get(id);
            if (node == null) {
                LoggerHelper.warnf(Language.WARNING_OID_NAME_NOT_FOUND, OidReference.class, oid);
                result.append(id);
                break;
            }
            result.append(node.getName());
        }
        // Doing the other non-recognized parts separately, so that we report
        // it only once
        while (tokenizer.hasMoreTokens()) {
            result.append('/');
            result.append(tokenizer.nextToken());
        }
        return result.toString();
    }

    private static final class LazyHolder {
        /**
         * "OID -> Display String" mapping tree root.
         */
        public static final OidTreeNode ROOT = OidMainTreeBuilder.build();
    }
}
