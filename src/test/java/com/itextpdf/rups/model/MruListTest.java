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
package com.itextpdf.rups.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
class MruListTest {
    @Test
    void init() {
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new MruList<Integer>(-1));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new MruList<Integer>(0));
        final MruList<Integer> list = new MruList<>(2);
        Assertions.assertEquals(2, list.capacity());
        Assertions.assertEquals(0, list.size());
    }

    @Test
    void basic() {
        final MruList<Integer> list = new MruList<>(5);
        Assertions.assertEquals(5, list.capacity());
        Assertions.assertEquals(0, list.size());

        Assertions.assertTrue(list.add(0));
        Assertions.assertEquals(1, list.size());
        Assertions.assertArrayEquals(new Integer[]{0}, list.toArray());

        Assertions.assertTrue(list.add(1));
        Assertions.assertEquals(2, list.size());
        Assertions.assertArrayEquals(new Integer[]{0, 1}, list.toArray());

        Assertions.assertTrue(list.add(2));
        Assertions.assertEquals(3, list.size());
        Assertions.assertArrayEquals(new Integer[]{0, 1, 2}, list.toArray());

        Assertions.assertTrue(list.add(3));
        Assertions.assertEquals(4, list.size());
        Assertions.assertArrayEquals(new Integer[]{0, 1, 2, 3}, list.toArray());

        Assertions.assertTrue(list.add(4));
        Assertions.assertEquals(5, list.size());
        Assertions.assertArrayEquals(new Integer[]{0, 1, 2, 3, 4}, list.toArray());

        Assertions.assertTrue(list.add(5));
        Assertions.assertEquals(5, list.size());
        Assertions.assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, list.toArray());

        Assertions.assertTrue(list.add(6));
        Assertions.assertEquals(5, list.size());
        Assertions.assertArrayEquals(new Integer[]{2, 3, 4, 5, 6}, list.toArray());

        Assertions.assertEquals(2, list.set(0, 0));
        Assertions.assertEquals(5, list.size());
        Assertions.assertArrayEquals(new Integer[]{0, 3, 4, 5, 6}, list.toArray());

        Assertions.assertEquals(5, list.remove(3));
        Assertions.assertEquals(4, list.size());
        Assertions.assertArrayEquals(new Integer[]{0, 3, 4, 6}, list.toArray());

        Assertions.assertEquals(3, list.remove(1));
        Assertions.assertEquals(3, list.size());
        Assertions.assertArrayEquals(new Integer[]{0, 4, 6}, list.toArray());

        Assertions.assertTrue(list.add(7));
        Assertions.assertEquals(4, list.size());
        Assertions.assertArrayEquals(new Integer[]{0, 4, 6, 7}, list.toArray());

        list.clear();
        Assertions.assertEquals(0, list.size());
    }

    @Test
    void rotateBegin() {
        final MruList<Integer> list = new MruList<>(2);
        Assertions.assertEquals(2, list.capacity());
        Assertions.assertEquals(0, list.size());

        Assertions.assertTrue(list.add(0));
        Assertions.assertEquals(1, list.size());
        Assertions.assertArrayEquals(new Integer[]{0}, list.toArray());

        Assertions.assertTrue(list.add(1));
        Assertions.assertEquals(2, list.size());
        Assertions.assertArrayEquals(new Integer[]{0, 1}, list.toArray());

        Assertions.assertTrue(list.add(2));
        Assertions.assertEquals(2, list.size());
        Assertions.assertArrayEquals(new Integer[]{1, 2}, list.toArray());

        Assertions.assertTrue(list.add(3));
        Assertions.assertEquals(2, list.size());
        Assertions.assertArrayEquals(new Integer[]{2, 3}, list.toArray());

        Assertions.assertTrue(list.add(4));
        Assertions.assertEquals(2, list.size());
        Assertions.assertArrayEquals(new Integer[]{3, 4}, list.toArray());
    }

    @Test
    void invalidIndex() {
        final MruList<Integer> list = new MruList<>(5);
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> list.get(0));
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> list.set(0, 0));
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> list.remove(0));
    }
}
