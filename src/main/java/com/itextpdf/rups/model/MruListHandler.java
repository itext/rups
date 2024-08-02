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
package com.itextpdf.rups.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

/**
 * Handler for a persistent "Most Recently Used" list.
 *
 * <p>
 * Indexing is done in such a way, that {@code 0} is the oldest element, while
 * {@code size() - 1} is the newest one. I.e. iteration is from oldest to
 * newest.
 * </p>
 *
 * <p>
 * Files are not stored as-is, instead "absolute files" are used for
 * consistency. So a file you want to put into the MRU list might not be equal
 * to the one, that was added.
 * </p>
 */
public final class MruListHandler {
    /**
     * Maximum size of the MRU list.
     */
    private static final int MRU_LIST_CAPACITY = 20;

    /**
     * Persistent storage for the MRU list.
     */
    private final Preferences persistentStorage;
    /**
     * Transient storage for the MRU list.
     */
    private final MruList<File> transientStorage;
    /**
     * A list of listeners for any change in the MRU list.
     */
    private final List<Consumer<MruListHandler>> changeListeners;

    /**
     * Creates a new MRU list handler, backed by the specified
     * {@link Preferences} node.
     *
     * @param persistentStorage {@link Preferences} node, which is used for
     *                          persistent storage.
     */
    public MruListHandler(Preferences persistentStorage) {
        this.persistentStorage = persistentStorage;
        this.transientStorage = new MruList<>(MRU_LIST_CAPACITY);
        this.changeListeners = new ArrayList<>();
        sync();
    }

    /**
     * Adds a new change listener to the MRU list handler.
     *
     * @param listener Listener to add.
     */
    public void addChangeListener(Consumer<MruListHandler> listener) {
        changeListeners.add(Objects.requireNonNull(listener));
    }

    /**
     * Returns the number of elements in the MRU list.
     *
     * @return The number of elements in the MRU list.
     */
    public int size() {
        return transientStorage.size();
    }

    /**
     * Returns {@code true} if the MRU list contains no elements.
     *
     * @return {@code true} if the MRU list contains no elements
     */
    public boolean isEmpty() {
        return transientStorage.isEmpty();
    }

    /**
     * Gets the most recent element within the MRU list.
     *
     * @return The most recent element or {@code null}, if list is empty.
     */
    public File peekMostRecent() {
        if (isEmpty()) {
            return null;
        }
        return transientStorage.get(transientStorage.size() - 1);
    }

    /**
     * Returns the element at the specified position within the MRU list.
     *
     * @param index Index of the element to return.
     *
     * @return The element at the specified position within the MRU list.
     *
     * @throws IndexOutOfBoundsException If the index is out of range
     *         ({@code index < 0 || index >= size()})
     */
    public File peek(int index) {
        return transientStorage.get(index);
    }

    /**
     * Returns the index of the first occurrence of the specified file in the
     * MRU list, or -1 if this list does not contain the file.
     *
     * @param file File to search for.
     *
     * @return The index of the first occurrence of the specified file in the
     *         MRU list, or -1 if this list does not contain the file.
     */
    public int indexOf(File file) {
        Objects.requireNonNull(file);

        return transientStorage.indexOf(file.getAbsoluteFile());
    }

    /**
     * Pushed the specified file to the end of the list or, if it is not
     * present, adds it as the latest new one.
     *
     * @param file File element to "use".
     */
    public void use(File file) {
        Objects.requireNonNull(file);

        // Do nothing, if it is already latest
        final File absoluteFile = file.getAbsoluteFile();
        if (absoluteFile.equals(peekMostRecent())) {
            return;
        }

        final int i = indexOf(absoluteFile);
        if (i >= 0) {
            transientStorage.remove(i);
        }
        transientStorage.add(absoluteFile);
        flush();
        fireChange();
    }

    public void remove(File file) {
        Objects.requireNonNull(file);

        final int i = indexOf(file.getAbsoluteFile());
        if (i >= 0) {
            transientStorage.remove(i);
            flush();
            fireChange();
        }
    }

    /**
     * Removes all the elements from the MRU list.
     */
    public void clear() {
        transientStorage.clear();
        flush();
        fireChange();
    }

    private void flush() {
        int i = 0;
        // Store current state
        for (final File element : transientStorage) {
            persistentStorage.put(Integer.toString(i), element.getAbsolutePath());
            ++i;
        }
        // Clear any possible leftover paths, so that we don't have hanging
        // data in the registry
        while (i < transientStorage.capacity()) {
            persistentStorage.remove(Integer.toString(i));
            ++i;
        }
    }

    private void sync() {
        transientStorage.clear();

        for (int i = 0; i < transientStorage.capacity(); ++i) {
            final String path = persistentStorage.get(Integer.toString(i), null);
            if (path == null) {
                break;
            }
            transientStorage.add(new File(path).getAbsoluteFile());
        }
    }

    private void fireChange() {
        for (final Consumer<MruListHandler> listener: changeListeners) {
            listener.accept(this);
        }
    }
}
