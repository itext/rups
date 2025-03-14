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
package com.itextpdf.rups.model.oid;

import java.util.Map;

/**
 * Tree node for the "OID -> Display Name" mapping tree.
 */
public final class OidTreeNode {
    /**
     * Display name for the specific node. It should include only the part
     * of the name, exclusive to this specific node, not the whole name.
     */
    private final String name;
    /**
     * Children for this specific OID tree node.
     */
    private final Map<String, OidTreeNode> children;

    /**
     * Creates a tree node without children.
     *
     * @param name Display name for the node.
     */
    public OidTreeNode(String name) {
        this.name = name;
        this.children = Map.of();
    }

    /**
     * Creates a tree node with children.
     *
     * @param name     Display name for the node.
     * @param children Children for this OID tree node.
     */
    public OidTreeNode(String name, Map<String, OidTreeNode> children) {
        this.name = name;
        this.children = children;
    }

    /**
     * Returns the node from the subtree by id.
     *
     * @param id ID to retrieve.
     *
     * @return Tree node, if found, otherwise null.
     */
    public OidTreeNode get(String id) {
        return children.getOrDefault(id, null);
    }

    /**
     * Returns the display name of the node.
     *
     * @return The display name of the node.
     */
    public String getName() {
        return name;
    }

    /**
     * Shorthand function to create an OID mapping tree entry from a subtree.
     *
     * @param id    Number ID of the tree node.
     * @param value Subtree, which corresponds to the provided ID.
     *
     * @return The created map entry.
     */
    public static Map.Entry<String, OidTreeNode> entry(String id, OidTreeNode value) {
        return Map.entry(id, value);
    }

    /**
     * Shorthand function to create an OID mapping tree entry from a display
     * name and children.
     *
     * @param id        Number ID of the tree node.
     * @param valueName Display name of the subtree root.
     * @param values    Children nodes of the subtree root.
     *
     * @return The created map entry.
     */
    public static Map.Entry<String, OidTreeNode> entry(String id, String valueName,
            Map<String, OidTreeNode> values) {
        return Map.entry(id, new OidTreeNode(valueName, values));
    }

    /**
     * Shorthand function to create an OID mapping tree entry from a display
     * name.
     *
     * @param id        Number ID of the tree node.
     * @param valueName Display name of the subtree root.
     *
     * @return The created map entry.
     */
    public static Map.Entry<String, OidTreeNode> entry(String id, String valueName) {
        return Map.entry(id, new OidTreeNode(valueName));
    }

    /**
     * Shorthand function to create an OID mapping tree entry, when there is no
     * special display name (ex. node represent a version OID, which is just
     * the same number).
     *
     * @param id Number ID of the tree node.
     *
     * @return The created map entry.
     */
    public static Map.Entry<String, OidTreeNode> entry(String id) {
        return Map.entry(id, new OidTreeNode(id));
    }
}
