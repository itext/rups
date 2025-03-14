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

import com.itextpdf.test.ExtendedITextTest;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// TODO: add dir test
@Tag("UnitTest")
class FilePathPreProcessorTest extends ExtendedITextTest {
    private static final BiFunction<String, String, String> platformChoice;
    static {
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Windows")) {
            platformChoice = (String windows, String other) -> windows;
        } else {
            platformChoice = (String windows, String other) -> other;
        }
    }

    @Test
    void processUrlEncodedInput() throws IOException {
        // File exists on disk, so URL encoded input should also work
        final Path tmp = Files.createTempFile("r u p s ", ".pdf");
        final String tmpPath;
        final String encodedPath;
        try {
            tmpPath = tmp.toString();
            Assertions.assertEquals(tmpPath, FilePathPreProcessor.process(tmpPath));
            encodedPath = URLEncoder.encode(tmpPath, StandardCharsets.UTF_8);
            Assertions.assertEquals(tmpPath, FilePathPreProcessor.process(encodedPath));
        } finally {
            Files.deleteIfExists(tmp);
        }
        // File no longer exists, so no conversion should happen
        Assertions.assertEquals(tmpPath, FilePathPreProcessor.process(tmpPath));
        Assertions.assertEquals(encodedPath, FilePathPreProcessor.process(encodedPath));
    }

    @Test
    void processUrlEncodedName() throws IOException {
        // File exists on disk with a URL-looking name, should still work fine
        // without transformations
        final Path tmp = Files.createTempFile("r%20u%20p%20s%20", ".pdf");
        final String tmpPath;
        try {
            tmpPath = tmp.toString();
            Assertions.assertEquals(tmpPath, FilePathPreProcessor.process(tmpPath));
        } finally {
            Files.deleteIfExists(tmp);
        }
        // Should be the same, even if file no longer exists
        Assertions.assertEquals(tmpPath, FilePathPreProcessor.process(tmpPath));
    }

    @Test
    void processUrlEncodedDirectory() throws IOException {
        // Directory exists on disk, but URL encoded input should not be
        // transformed, as it handles only files
        final Path tmp = Files.createTempDirectory("rups ");
        final String tmpPath;
        final String encodedPath;
        try {
            tmpPath = tmp.toString();
            Assertions.assertEquals(tmpPath, FilePathPreProcessor.process(tmpPath));
            encodedPath = URLEncoder.encode(tmpPath, StandardCharsets.UTF_8);
            Assertions.assertEquals(encodedPath, FilePathPreProcessor.process(encodedPath));
        } finally {
            Files.deleteIfExists(tmp);
        }
        // Same when directory no longer exists
        Assertions.assertEquals(tmpPath, FilePathPreProcessor.process(tmpPath));
        Assertions.assertEquals(encodedPath, FilePathPreProcessor.process(encodedPath));
    }

    @Test
    void processContextDirRelativePath() throws IOException {
        /*
         * We will create a temporary file and check URL-encoding processing
         * with it. When we just take the file name without a directory, we
         * should get the encoded path as-is, since file doesn't exist. But if
         * we specify the temporary directory in the context, then it should
         * work.
         */
        final Path tmp = Files.createTempFile("r u p s ", ".pdf");
        try {
            final String name = tmp.getFileName().toString();
            Assertions.assertEquals(name, FilePathPreProcessor.process(name));
            final String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
            Assertions.assertEquals(encodedName, FilePathPreProcessor.process(encodedName));
            final File dir = tmp.getParent().toFile();
            Assertions.assertEquals(name, FilePathPreProcessor.process(dir, encodedName));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @MethodSource("processBasicArguments")
    @ParameterizedTest(name = "[{index}] {0}")
    void processBasic(String path, String expected) {
        Assertions.assertEquals(expected, FilePathPreProcessor.process(path));
    }

    private static Stream<Arguments> processBasicArguments() {
        return Stream.of(
                // Empty values are passed as-is
                identity(null),
                identity(""),
                // Basic file names are passed as-is
                identity(" "),
                identity("file"),
                identity("note.pdf"),
                // Windows paths are passed as-is
                identity("a\\b\\c\\file.txt"),
                identity("C:\\a\\b\\c\\file.txt"),
                identity("\\?\\C:\\a\\b\\c\\file.txt"),
                identity("\\.\\C:\\a\\b\\c\\file.txt"),
                // *nix paths are passed as-is
                identity("a/b/c/file.txt"),
                identity("/usr/bin/a/b/c/file.txt"),
                /* URI with file schema should be transformed, even with
                 * incorrect amount of slashes. But since those paths are
                 * absolute, there is some different processing for Windows.
                 * I.e. for Windows drives the leading slash is omitted in the
                 * File API, as drive letter is the beginning of the path.
                 */
                Arguments.of("file:/", "/"),
                Arguments.of("file:///", "/"),
                Arguments.of("file:/test.pdf", "/test.pdf"),
                Arguments.of("file://test.pdf", "/test.pdf"),
                Arguments.of("file:///test.pdf", "/test.pdf"),
                Arguments.of(
                        "file:C:/",
                        platformChoice.apply("C:/", "/C:")
                ),
                Arguments.of(
                        "file:/C:/",
                        platformChoice.apply("C:/", "/C:")
                ),
                Arguments.of(
                        "file://C:/",
                        platformChoice.apply("C:/", "/C:")
                ),
                Arguments.of(
                        "file:///C:/",
                        platformChoice.apply("C:/", "/C:")
                ),
                Arguments.of(
                        "file:C:/test.pdf",
                        platformChoice.apply("C:/test.pdf", "/C:/test.pdf")
                ),
                Arguments.of(
                        "file:/C:/test.pdf",
                        platformChoice.apply("C:/test.pdf", "/C:/test.pdf")
                ),
                Arguments.of(
                        "file://C:/test.pdf",
                        platformChoice.apply("C:/test.pdf", "/C:/test.pdf")
                ),
                Arguments.of(
                        "file:///C:/test.pdf",
                        platformChoice.apply("C:/test.pdf", "/C:/test.pdf")
                ),
                // Including URIs with URL-encoded parts
                Arguments.of(
                        "file:///C:/random%C3%A4test.pdf",
                        platformChoice.apply("C:/randomätest.pdf", "/C:/randomätest.pdf")
                ),
                Arguments.of("file:///random%C3%A4test.pdf", "/randomätest.pdf"),
                // These URL-encoded paths should remain as-is, as the files
                // don't exist on the disk (hopefully)
                identity("3142b600%208fa2%204686%2083b0%20241acaa984c7"),
                // % should not break anything
                identity("file%"),
                // "file:" is a valid *nix file name, so should remain as such
                identity("/home/file:/home/3142b600.pdf"),
                // Though "file:" is not valid on Windows, we won't touch it
                identity("C:/Users/file:/Users/3142b600.pdf")
        );
    }

    private static Arguments identity(String path) {
        return Arguments.of(path, path);
    }
}
