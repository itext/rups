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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.prefs.AbstractPreferences;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
class MruListHandlerTest {
    @Test
    void full() {
        // Initial sync state
        final MapPreferences preferences = new MapPreferences(new HashMap<>(Map.of(
                "0", toAbsolutePath("test/a"),
                "1", toAbsolutePath("test/b")
        )));
        final MruListHandler mru = new MruListHandler(preferences);
        Assertions.assertFalse(mru.isEmpty());
        Assertions.assertEquals(2, mru.size());
        Assertions.assertEquals(toAbsoluteFile("test/a"), mru.peek(0));
        Assertions.assertEquals(toAbsoluteFile("test/b"), mru.peek(1));
        Assertions.assertEquals(toAbsoluteFile("test/b"), mru.peekMostRecent());
        Assertions.assertEquals(0, mru.indexOf(toAbsoluteFile("test/a")));
        Assertions.assertEquals(1, mru.indexOf(toAbsoluteFile("test/b")));
        Assertions.assertEquals(-1, mru.indexOf(toAbsoluteFile("test/c")));

        final CounterChangeListener changeListener = new CounterChangeListener();
        mru.addChangeListener(changeListener);

        // Bumping latest file, there is no change
        mru.use(toAbsoluteFile("test/b"));
        Assertions.assertEquals(0, changeListener.counter);
        Assertions.assertFalse(mru.isEmpty());
        Assertions.assertEquals(2, mru.size());
        Assertions.assertEquals(toAbsoluteFile("test/a"), mru.peek(0));
        Assertions.assertEquals(toAbsoluteFile("test/b"), mru.peek(1));
        Assertions.assertEquals(toAbsoluteFile("test/b"), mru.peekMostRecent());
        Assertions.assertEquals(0, mru.indexOf(toAbsoluteFile("test/a")));
        Assertions.assertEquals(1, mru.indexOf(toAbsoluteFile("test/b")));
        Assertions.assertEquals(-1, mru.indexOf(toAbsoluteFile("test/c")));
        Assertions.assertEquals(
                Map.of("0", toAbsolutePath("test/a"), "1", toAbsolutePath("test/b")),
                preferences.getMap()
        );

        // Bumping existing file
        mru.use(toAbsoluteFile("test/a"));
        Assertions.assertEquals(1, changeListener.counter);
        Assertions.assertFalse(mru.isEmpty());
        Assertions.assertEquals(2, mru.size());
        Assertions.assertEquals(toAbsoluteFile("test/b"), mru.peek(0));
        Assertions.assertEquals(toAbsoluteFile("test/a"), mru.peek(1));
        Assertions.assertEquals(toAbsoluteFile("test/a"), mru.peekMostRecent());
        Assertions.assertEquals(0, mru.indexOf(toAbsoluteFile("test/b")));
        Assertions.assertEquals(1, mru.indexOf(toAbsoluteFile("test/a")));
        Assertions.assertEquals(-1, mru.indexOf(toAbsoluteFile("test/c")));
        Assertions.assertEquals(
                Map.of("0", toAbsolutePath("test/b"), "1", toAbsolutePath("test/a")),
                preferences.getMap()
        );

        // Adding new file
        mru.use(toAbsoluteFile("test/c"));
        Assertions.assertEquals(2, changeListener.counter);
        Assertions.assertFalse(mru.isEmpty());
        Assertions.assertEquals(3, mru.size());
        Assertions.assertEquals(toAbsoluteFile("test/b"), mru.peek(0));
        Assertions.assertEquals(toAbsoluteFile("test/a"), mru.peek(1));
        Assertions.assertEquals(toAbsoluteFile("test/c"), mru.peek(2));
        Assertions.assertEquals(toAbsoluteFile("test/c"), mru.peekMostRecent());
        Assertions.assertEquals(0, mru.indexOf(toAbsoluteFile("test/b")));
        Assertions.assertEquals(1, mru.indexOf(toAbsoluteFile("test/a")));
        Assertions.assertEquals(2, mru.indexOf(toAbsoluteFile("test/c")));
        Assertions.assertEquals(
                Map.of("0", toAbsolutePath("test/b"), "1", toAbsolutePath("test/a"), "2", toAbsolutePath("test/c")),
                preferences.getMap()
        );

        // Removing existing file
        mru.remove(toAbsoluteFile("test/a"));
        Assertions.assertEquals(3, changeListener.counter);
        Assertions.assertFalse(mru.isEmpty());
        Assertions.assertEquals(2, mru.size());
        Assertions.assertEquals(toAbsoluteFile("test/b"), mru.peek(0));
        Assertions.assertEquals(toAbsoluteFile("test/c"), mru.peek(1));
        Assertions.assertEquals(toAbsoluteFile("test/c"), mru.peekMostRecent());
        Assertions.assertEquals(0, mru.indexOf(toAbsoluteFile("test/b")));
        Assertions.assertEquals(1, mru.indexOf(toAbsoluteFile("test/c")));
        Assertions.assertEquals(-1, mru.indexOf(toAbsoluteFile("test/a")));
        Assertions.assertEquals(
                Map.of("0", toAbsolutePath("test/b"), "1", toAbsolutePath("test/c")),
                preferences.getMap()
        );

        // Removing non-existing file, there is no change
        mru.remove(toAbsoluteFile("test/a"));
        Assertions.assertEquals(3, changeListener.counter);
        Assertions.assertFalse(mru.isEmpty());
        Assertions.assertEquals(2, mru.size());
        Assertions.assertEquals(toAbsoluteFile("test/b"), mru.peek(0));
        Assertions.assertEquals(toAbsoluteFile("test/c"), mru.peek(1));
        Assertions.assertEquals(toAbsoluteFile("test/c"), mru.peekMostRecent());
        Assertions.assertEquals(0, mru.indexOf(toAbsoluteFile("test/b")));
        Assertions.assertEquals(1, mru.indexOf(toAbsoluteFile("test/c")));
        Assertions.assertEquals(-1, mru.indexOf(toAbsoluteFile("test/a")));
        Assertions.assertEquals(
                Map.of("0", toAbsolutePath("test/b"), "1", toAbsolutePath("test/c")),
                preferences.getMap()
        );

        // Clearing everything
        mru.clear();
        Assertions.assertEquals(4, changeListener.counter);
        Assertions.assertTrue(mru.isEmpty());
        Assertions.assertEquals(0, mru.size());
        Assertions.assertNull(mru.peekMostRecent());
        Assertions.assertEquals(-1, mru.indexOf(toAbsoluteFile("test/b")));
        Assertions.assertEquals(-1, mru.indexOf(toAbsoluteFile("test/c")));
        Assertions.assertEquals(-1, mru.indexOf(toAbsoluteFile("test/a")));
        Assertions.assertEquals(Map.of(), preferences.getMap());
    }

    @Test
    void absolutePathHandling() {
        // Initial sync state, paths from preferences should get converted to
        // absolute ones
        final MapPreferences preferences = new MapPreferences(new HashMap<>(Map.of(
                "0", "test/a",
                "1", "test/b"
        )));
        final MruListHandler mru = new MruListHandler(preferences);

        // Peek should return absolute files
        Assertions.assertNotEquals(new File("test/a"), mru.peek(0));
        Assertions.assertEquals(toAbsoluteFile("test/a"), mru.peek(0));
        Assertions.assertNotEquals(new File("test/b"), mru.peek(1));
        Assertions.assertEquals(toAbsoluteFile("test/b"), mru.peek(1));
        Assertions.assertNotEquals(new File("test/b"), mru.peekMostRecent());
        Assertions.assertEquals(toAbsoluteFile("test/b"), mru.peekMostRecent());

        // indexOf should work with any file
        Assertions.assertEquals(0, mru.indexOf(new File("test/a")));
        Assertions.assertEquals(0, mru.indexOf(toAbsoluteFile("test/a")));
        Assertions.assertEquals(1, mru.indexOf(new File("test/b")));
        Assertions.assertEquals(1, mru.indexOf(toAbsoluteFile("test/b")));
        Assertions.assertEquals(-1, mru.indexOf(new File("test/c")));
        Assertions.assertEquals(-1, mru.indexOf(toAbsoluteFile("test/c")));

        // use should work with any file
        mru.use(new File("test/a"));
        Assertions.assertEquals(2, mru.size());
        Assertions.assertEquals(toAbsoluteFile("test/b"), mru.peek(0));
        Assertions.assertEquals(toAbsoluteFile("test/a"), mru.peek(1));
        // At this point it will update paths in preference to absolute ones
        // because of flush
        Assertions.assertEquals(
                Map.of("0", toAbsolutePath("test/b"), "1", toAbsolutePath("test/a")),
                preferences.getMap()
        );
        mru.use(toAbsoluteFile("test/b"));
        Assertions.assertEquals(2, mru.size());
        Assertions.assertEquals(toAbsoluteFile("test/a"), mru.peek(0));
        Assertions.assertEquals(toAbsoluteFile("test/b"), mru.peek(1));
        Assertions.assertEquals(
                Map.of("0", toAbsolutePath("test/a"), "1", toAbsolutePath("test/b")),
                preferences.getMap()
        );

        // remove should work with any file
        mru.remove(new File("test/b"));
        Assertions.assertEquals(1, mru.size());
        Assertions.assertEquals(toAbsoluteFile("test/a"), mru.peek(0));
        Assertions.assertEquals(Map.of("0", toAbsolutePath("test/a")), preferences.getMap());
        mru.remove(toAbsoluteFile("test/a"));
        Assertions.assertEquals(0, mru.size());
        Assertions.assertEquals(Map.of(), preferences.getMap());
    }

    @Test
    void invalidIndex() {
        final MruListHandler mru = new MruListHandler(new MapPreferences(Map.of()));
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> mru.peek(0));
    }

    private static File toAbsoluteFile(String path) {
        return new File(path).getAbsoluteFile();
    }

    private static String toAbsolutePath(String path) {
        return new File(path).getAbsolutePath();
    }

    private static final class CounterChangeListener implements Consumer<MruListHandler> {
        public int counter = 0;

        @Override
        public void accept(MruListHandler mruListHandler) {
            ++counter;
        }
    }

    /**
     * One-level preferences implementation based on map.
     */
    private static final class MapPreferences extends AbstractPreferences {
        private final Map<String, String> map;

        public MapPreferences(Map<String, String> map) {
            super(null, "");
            this.map = map;
        }

        public Map<String, String> getMap() {
            return map;
        }

        @Override
        protected void putSpi(String key, String value) {
            map.put(key, value);
        }

        @Override
        protected String getSpi(String key) {
            return map.getOrDefault(key, null);
        }

        @Override
        protected void removeSpi(String key) {
            map.remove(key);
        }

        @Override
        protected void removeNodeSpi() {
            // noop
        }

        @Override
        protected String[] keysSpi() {
            final Set<String> keys = map.keySet();
            return keys.toArray(new String[keys.size()]);
        }

        @Override
        protected String[] childrenNamesSpi() {
            return new String[0];
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            return null;
        }

        @Override
        protected void syncSpi() {
            // noop
        }

        @Override
        protected void flushSpi() {
            // noop
        }
    }
}
