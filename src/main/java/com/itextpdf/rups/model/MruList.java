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

import java.util.AbstractList;
import java.util.Objects;

/**
 * An implementation of a list for "Most Recently Used" items.
 *
 * <p>
 * This is a fixed capacity circular buffer, which will overwrite first items,
 * if there is overflow on addition.
 * </p>
 */
public final class MruList<T> extends AbstractList<T> {
    private final Object[] storage;
    private int begin;
    private int size;

    public MruList(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity should positive");
        }
        this.storage = new Object[capacity];
        this.begin = 0;
        this.size = 0;
    }

    public int capacity() {
        return storage.length;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean add(T t) {
        if (size == storage.length) {
            storage[begin] = t;
            begin = (begin + 1) % storage.length;
        } else {
            storage[toStorageIndex(size)] = t;
            ++size;
        }
        return true;
    }

    @Override
    public T get(int index) {
        Objects.checkIndex(index, size);
        return storage(toStorageIndex(index));
    }

    @Override
    public T set(int index, T element) {
        Objects.checkIndex(index, size);
        final int storageIndex = toStorageIndex(index);
        final T prevElement = storage(storageIndex);
        storage[storageIndex] = element;
        return prevElement;
    }

    @Override
    public T remove(int index) {
        Objects.checkIndex(index, size);
        final int storageIndex = toStorageIndex(index);
        final int storageEnd = toStorageIndex(size);
        final T prevElement = storage(storageIndex);
        if (storageIndex < storageEnd) {
            // Move end part to the left
            System.arraycopy(
                    storage, storageIndex + 1,
                    storage, storageIndex,
                    storageEnd - storageIndex - 1
            );
            storage[storageEnd - 1] = null;
        } else {
            // Move start part to the right
            System.arraycopy(
                    storage, begin,
                    storage, begin + 1,
                    storageIndex - begin
            );
            storage[begin] = null;
            begin = (begin + 1) % storage.length;
        }
        --size;
        return prevElement;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; ++i) {
            storage[toStorageIndex(i)] = null;
        }
        begin = 0;
        size = 0;
    }

    @SuppressWarnings("unchecked")
    private T storage(int storageIndex) {
        return (T) storage[storageIndex];
    }

    private int toStorageIndex(int index) {
        return (begin + index) % storage.length;
    }
}
