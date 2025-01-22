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
package com.itextpdf.rups.model.contentstream;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Node of a parse tree of a PDF content stream.
 *
 * <p>
 * Each node is an element of a circular double-linked list, which allows you
 * to easily traverse between siblings. To distinguish, where the list ends,
 * a special marker node is inserted of a
 * {@link ParseTreeNodeType#CHILD_SENTINEL} type. These marker nodes are
 * internal and are not accessible to the class users. List manipulation
 * is handled by the class itself.
 * </p>
 *
 * <p>
 * Node can have multiple children, which can be traversed via the siblings
 * interface. Only the root node and composite type nodes are expected to have
 * children, while primitive type node should have just text instead. Each
 * node, with the exception of the root node, will have a parent set for
 * traversal.
 * </p>
 *
 * <p>
 * Parse tree starts with a root node, which has no siblings, no parent, and
 * is of a {@link ParseTreeNodeType#ROOT} type. Its first order children
 * should be a tokenized representation of a PDF stream. For the most part the
 * tree shouldn't be very tall at this moment, as there are very few composite
 * types (string literals, arrays and dictionaries), and those are rarely
 * encountered in a content stream in a deeply nested way.
 * </p>
 */
public final class ParseTreeNode {
    /**
     * Parent of the node. Should be null for root.
     */
    private final ParseTreeNode parent;
    /**
     * Type of the node.
     */
    private final ParseTreeNodeType type;
    /**
     * Text array, backing the tree node. Expected to be non-null for primitive
     * types. Part of the inlined text segment data.
     */
    private final char[] textArray;
    /**
     * Starting offset into the text array. Expected to be a valid value for
     * primitive types. Part of the inlined text segment data.
     */
    private final int textOffset;
    /**
     * Text segment length. Expected to be a valid value for primitive types.
     * Part of the inlined text segment data.
     */
    private final int textCount;
    /**
     * Circular double-linked list of children. Maintained manually by the
     * class. Should point to a sentinel node, with getNext being the first
     * element of the list and getPrev being the last element of the list.
     */
    private ParseTreeNode children = null;
    /**
     * Pointer to the previous sibling node in a circular double-linked list.
     * For a root node it will be set to {@code this}.
     */
    private ParseTreeNode prev = this;
    /**
     * Pointer to the next sibling node in a circular double-linked list.
     * For a root node it will be set to {@code this}.
     */
    private ParseTreeNode next = this;

    /**
     * Creates a root parse tree node.
     */
    public ParseTreeNode() {
        this.parent = null;
        this.type = ParseTreeNodeType.ROOT;
        this.textArray = null;
        this.textOffset = 0;
        this.textCount = 0;
    }

    /**
     * Creates a child parse tree node of a composite type.
     *
     * @param type   Type of the node. Should be a composite type.
     * @param parent Parent of the node. Should not be null.
     */
    private ParseTreeNode(ParseTreeNodeType type, ParseTreeNode parent) {
        this(type, null, 0, 0, parent);
    }

    /**
     * Creates a child parse tree node of a specified type, which is,
     * optionally, backed by text.
     *
     * @param type       Type of the node.
     * @param textArray  Backing text array of the node. Should not be null
     *                   for a primitive type.
     * @param textOffset Starting offset into the text array. Should be valid
     *                   for a primitive type.
     * @param textCount  Text segment length. Should be valid for a primitive
     *                   type.
     * @param parent     Parent of the node. Should not be null.
     */
    private ParseTreeNode(ParseTreeNodeType type, char[] textArray, int textOffset, int textCount,
            ParseTreeNode parent) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(parent);
        if (textArray == null && type.isPrimitive()) {
            throw new IllegalArgumentException("Primitive type should have text present");
        }
        this.parent = parent;
        this.type = type;
        this.textArray = textArray;
        this.textOffset = textOffset;
        this.textCount = textCount;
    }

    /**
     * Returns whether the node is a root node or not.
     *
     * @return Whether the node is a root node or not.
     */
    public boolean isRoot() {
        // Only checking the parent pointer, as you should not be able to
        // create a non-root node without a parent
        return parent == null;
    }

    /**
     * Returns whether the node is a leaf node. I.e. it is a leaf node, if it
     * has no children. Should be false only for root and primitive nodes.
     *
     * @return Whether the node is a leaf node.
     */
    public boolean isLeaf() {
        return children == null || (children.getNext() == children);
    }

    /**
     * Returns whether text of this node matches the specified text. This
     * operation is valid only for primitive nodes.
     *
     * @param text Expected text.
     *
     * @return Whether text of this node matches the specified text.
     */
    public boolean is(char[] text) {
        return Arrays.equals(text, 0, text.length, textArray, textOffset, textOffset + textCount);
    }

    /**
     * Returns whether this is an operator type node with the specified text.
     *
     * @param operator Operator text.
     *
     * @return Whether this is an operator type node with the specified text.
     */
    public boolean isOperator(char[] operator) {
        if (type != ParseTreeNodeType.OPERATOR) {
            return false;
        }
        return is(operator);
    }

    /**
     * Returns the parent of the node. Will return null for root.
     *
     * @return The parent of the node. Will return null for root.
     */
    public ParseTreeNode getParent() {
        return parent;
    }

    /**
     * Returns the type of the node.
     *
     * @return The type of the node.
     */
    public ParseTreeNodeType getType() {
        return type;
    }

    /**
     * Returns the backing text of a node as a char sequence. Only valid for
     * primitive type nodes.
     *
     * @return The backing text of a node as a char sequence.
     */
    public CharSequence getText() {
        return CharBuffer.wrap(textArray, textOffset, textCount);
    }

    /**
     * Returns the backing text array. Only valid for primitive type nodes.
     *
     * @return The backing text array.
     */
    public char[] getTextArray() {
        return textArray;
    }

    /**
     * Returns the starting offset into the text array. Only valid for
     * primitive type nodes.
     *
     * @return The starting offset into the text array.
     */
    public int getTextOffset() {
        return textOffset;
    }

    /**
     * Returns the text segment length. Only valid for primitive type nodes.
     *
     * @return The text segment length.
     */
    public int getTextCount() {
        return textCount;
    }

    /**
     * Returns the start offset for the node. If this is a primitive node,
     * then it is equivalent to calling {@link #getTextOffset()}. But if it is
     * a composite node, it returns the text offset of the leftmost
     * primitive descendant.
     *
     * @return The start offset for the node.
     */
    public int getStartOffset() {
        if (textArray != null) {
            return textOffset;
        }
        ParseTreeNode child = getFirstChild();
        while (child != null) {
            if (child.textArray != null) {
                return child.textOffset;
            }
            child = child.getFirstChild();
        }
        return 0;
    }

    /**
     * Returns the end offset for the node. If this is a primitive node, then
     * it is equivalent to summing {@link #getTextOffset()} and
     * {@link #getTextCount()}. But if it is a composite node, it returns the
     * end offset of the leftmost primitive descendant.
     *
     * @return The start offset for the node.
     */
    public int getEndOffset() {
        if (textArray != null) {
            return textOffset + textCount;
        }
        ParseTreeNode child = getLastChild();
        while (child != null) {
            if (child.textArray != null) {
                return child.textOffset + child.textCount;
            }
            child = child.getLastChild();
        }
        return 0;
    }

    /**
     * Returns the first child of a node, or null, if it is a leaf.
     *
     * @return The first child of a node, or null, if it is a leaf.
     */
    public ParseTreeNode getFirstChild() {
        if (children == null) {
            return null;
        }
        return children.getNext();
    }

    /**
     * Returns the last child of a node, or null, if it is a leaf.
     *
     * @return The last child of a node, or null, if it is a leaf.
     */
    public ParseTreeNode getLastChild() {
        if (children == null) {
            return null;
        }
        return children.getPrev();
    }

    /**
     * Creates a new tree node and adds it as the last child of the node.
     *
     * @param type       Type of the node.
     * @param textArray  Backing text array of the node. Should not be null
     *                   for a primitive type.
     * @param textOffset Starting offset into the text array. Should be valid
     *                   for a primitive type.
     * @param textCount  Text segment length. Should be valid for a primitive
     *                   type.
     *
     * @return The newly created child node.
     */
    public ParseTreeNode addChild(ParseTreeNodeType type, char[] textArray, int textOffset, int textCount) {
        return addChild(new ParseTreeNode(type, textArray, textOffset, textCount, this));
    }

    /**
     * Creates a new tree node of a composite type and adds it as the last
     * child of the node.
     *
     * @param type Type of the node. Should be a composite type.
     *
     * @return The newly created child node.
     */
    public ParseTreeNode addChild(ParseTreeNodeType type) {
        return addChild(new ParseTreeNode(type, this));
    }

    /**
     * Creates a new tree node and adds it as the next sibling of the node.
     *
     * @param type       Type of the node.
     * @param textArray  Backing text array of the node. Should not be null
     *                   for a primitive type.
     * @param textOffset Starting offset into the text array. Should be valid
     *                   for a primitive type.
     * @param textCount  Text segment length. Should be valid for a primitive
     *                   type.
     *
     * @return The newly created child node.
     */
    public ParseTreeNode addNext(ParseTreeNodeType type, char[] textArray, int textOffset, int textCount) {
        final ParseTreeNode node = new ParseTreeNode(type, textArray, textOffset, textCount, getParent());
        linkNext(node);
        return node;
    }

    /**
     * Returns the total length of text in this parse tree. This is calculated
     * by summing the text lengths of all underlying primitive nodes.
     *
     * @return The total length of text in this parse tree.
     */
    public int length() {
        int result = 0;
        final Iterator<ParseTreeNode> it = primitiveNodeIterator();
        while (it.hasNext()) {
            final ParseTreeNode node = it.next();
            result += node.getTextCount();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (type.isPrimitive()) {
            return type + ": " + getText();
        }
        return type.toString();
    }

    /**
     * Returns the backing text of this parse tree. This is constructed
     * by concatenating the text of all underlying primitive nodes.
     *
     * @return The backing text of this parse tree.
     */
    public String getFullText() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<ParseTreeNode> it = primitiveNodeIterator();
        while (it.hasNext()) {
            final ParseTreeNode node = it.next();
            sb.append(node.getTextArray(), node.getTextOffset(), node.getTextCount());
        }
        return sb.toString();
    }

    /**
     * Returns an iterator, which goes through all the primitive node from left
     * to right. I.e. it goes through the text-backed leaves.
     *
     * @return An iterator, which goes through all the primitive node from left
     * to right.
     */
    public Iterator<ParseTreeNode> primitiveNodeIterator() {
        return new PrimitiveNodeIterator(this);
    }

    /**
     * Returns whether the node has the next sibling.
     *
     * @return Whether the node has the next sibling.
     */
    public boolean hasNext() {
        return next != this && !next.type.isMarker();
    }

    /**
     * Returns the next sibling child of a node, or null, if it is the last
     * one.
     *
     * @return The next sibling child of a node, or null, if it is the last
     * one.
     */
    public ParseTreeNode getNext() {
        if (!hasNext()) {
            return null;
        }
        return next;
    }

    /**
     * Returns whether the node has the previous sibling.
     *
     * @return Whether the node has the previous sibling.
     */
    public boolean hasPrev() {
        return prev != this && !prev.type.isMarker();
    }


    /**
     * Returns the previous sibling child of a node, or null, if it is the
     * first one.
     *
     * @return The previous sibling child of a node, or null, if it is the
     * first one.
     */
    public ParseTreeNode getPrev() {
        if (!hasPrev()) {
            return null;
        }
        return prev;
    }

    /**
     * Removes the current node from the tree and returns its next sibling.
     *
     * @return Node's next sibling.
     */
    public ParseTreeNode remove() {
        final ParseTreeNode nextNode = getNext();
        unlink();
        return nextNode;
    }

    /**
     * Links the element to be the previous sibling.
     *
     * @param elem Element to link.
     */
    private void linkPrev(ParseTreeNode elem) {
        assert elem != null;

        prev.next = elem;
        elem.prev = this.prev;
        elem.next = this;
        this.prev = elem;
    }

    /**
     * Links the element to be the next sibling.
     *
     * @param elem Element to link.
     */
    private void linkNext(ParseTreeNode elem) {
        assert elem != null;

        next.prev = elem;
        elem.prev = this;
        elem.next = this.next;
        this.next = elem;
    }

    /**
     * Unlinks the node from the siblings list.
     */
    private void unlink() {
        prev.next = this.next;
        next.prev = this.prev;
        this.prev = this;
        this.next = this;
    }

    /**
     * Adds a new child at the end of the children list.
     *
     * @param child Child node to add.
     *
     * @return The added child node.
     */
    private ParseTreeNode addChild(ParseTreeNode child) {
        assert child != null;

        if (child.getType().isMarker()) {
            throw new IllegalArgumentException("Marker types are not allowed");
        }
        if (type.isPrimitive()) {
            throw new IllegalStateException("Primitive nodes cannot have children");
        }
        if (children == null) {
            children = new ParseTreeNode(ParseTreeNodeType.CHILD_SENTINEL, this);
        }
        children.linkPrev(child);
        return child;
    }

    /**
     * Iterator implementation, which goes through all primitive nodes in the
     * tree from left to right.
     */
    private static final class PrimitiveNodeIterator implements Iterator<ParseTreeNode> {
        private ParseTreeNode nextNode;

        private PrimitiveNodeIterator(ParseTreeNode start) {
            nextNode = start;
            if (nextNode.isRoot()) {
                moveOnce();
            }
            while (nextNode != null && !nextNode.type.isPrimitive()) {
                moveOnce();
            }
        }

        @Override
        public boolean hasNext() {
            return nextNode != null;
        }

        @Override
        public ParseTreeNode next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final ParseTreeNode result = nextNode;
            do {
                moveOnce();
            } while (nextNode != null && !nextNode.isLeaf());
            return result;
        }

        private void moveOnce() {
            assert nextNode != null;
            if (!nextNode.isLeaf()) {
                nextNode = nextNode.getFirstChild();
                return;
            }
            if (nextNode.hasNext()) {
                nextNode = nextNode.getNext();
                return;
            }
            do {
                nextNode = nextNode.getParent();
            } while (nextNode != null && !nextNode.hasNext());
            if (nextNode != null) {
                nextNode = nextNode.getNext();
            }
        }
    }
}
