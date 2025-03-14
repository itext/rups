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
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Static file path processor, which allows handling additional use cases:
 * <ul>
 *     <li>URIs with the {@code file} schema.</li>
 *     <li>URL-encoded paths.</li>
 * </ul>
 * <p>
 * All these additional use cases are only triggered, when it wouldn't prevent
 * opening an existing file with the original path name.
 * </p>
 */
public final class FilePathPreProcessor {
    private FilePathPreProcessor() {
        // Empty constructor
    }

    /**
     * Process the file name within the context of the specified directory.
     * Directory will not be included in the output file name string.
     *
     * @param currentDir current working dir for the test
     * @param filename   file name to process
     *
     * @return the processed file name
     */
    public static String process(File currentDir, String filename) {
        // Just pass empty values through
        if (filename == null || filename.isEmpty()) {
            return filename;
        }
        // If a file exists at the specified path, just go with it
        if (isFile(currentDir, filename)) {
            return filename;
        }
        // If path is, actually, a file URI, process that as well
        final Optional<String> uriFilePath = fromFileUri(filename);
        if (uriFilePath.isPresent()) {
            return uriFilePath.get();
        }
        // Another special case, if string is URL-encoded for some reason and
        // is not a URI, then test that as well
        final Optional<String> urlEncodedPath = fromUrlEncoded(currentDir, filename);
        if (urlEncodedPath.isPresent()) {
            return urlEncodedPath.get();
        }
        // If not special cases triggered, just return as-is.
        return filename;
    }

    /**
     * Process the path within the context of the current working directory.
     *
     * @param path path to process
     *
     * @return the processed path
     */
    public static String process(String path) {
        return process(null, path);
    }

    /**
     * Decodes a URL-encoded string and returns the decoded string, if is a
     * path to an existing file. Otherwise, returns an empty optional.
     *
     * @param currentDir       current working dir for the test
     * @param urlEncodedString URL-encoded string
     *
     * @return the decoded path to the file
     */
    private static Optional<String> fromUrlEncoded(File currentDir, String urlEncodedString) {
        try {
            final String decoded = URLDecoder.decode(urlEncodedString, StandardCharsets.UTF_8);
            if (isFile(currentDir, decoded)) {
                return Optional.of(decoded);
            }
        } catch (RuntimeException ignored) {
            // Ignored
        }
        return Optional.empty();
    }

    /**
     * Takes a {@code file} schema URI and returns the absolute path to the
     * file. If this is not a file URI, returns an empty optional.
     *
     * @param fileUriString {@code file} schema URI string
     *
     * @return the absolute path to the file
     */
    private static Optional<String> fromFileUri(String fileUriString) {
        try {
            URI uri = URI.create(fileUriString);
            final String scheme = uri.getScheme();
            if (!"file".equalsIgnoreCase(scheme)) {
                return Optional.empty();
            }
            /*
             * This is some handling for invalid URIs. For example, if you
             * want to have URI to, for example, "C:\note.pdf", then these
             * cases are valid:
             * - file:C:/node.pdf       (i.e. host part is omitted)
             * - file:/C:/node.pdf      (i.e. host part is omitted and path starts with a slash)
             * - file:///C:/node.pdf    (i.e. host part is empty)
             * But this case is not:
             * - file://C:/node.pdf     (i.e. "C:" is the host part)
             * This block should handle this, as this is a common mistake.
             */
            if (uri.getRawAuthority() != null || uri.getPath() == null) {
                //                          Skip "file:" at start
                uri = URI.create("file:/" + fileUriString.substring(5));
            }
            String path = new File(uri).getPath();
            // Returning the original URI separators for consistency
            if (File.separatorChar != '/') {
                path = path.replace(File.separatorChar, '/');
            }
            return Optional.of(path);
        } catch (RuntimeException ignored) {
            // Ignored
        }
        return Optional.empty();
    }

    /**
     * Returns whether a regular file exists at the specified path. If there is
     * an exception during the check, {@code false} is returned.
     *
     * @param currentDir current working dir for the test
     * @param path       path to check
     *
     * @return whether a regular file exists at the specified path
     */
    private static boolean isFile(File currentDir, String path) {
        try {
            return new File(currentDir, path).isFile();
        } catch (RuntimeException ignored) {
            // Ignored
        }
        return false;
    }
}
