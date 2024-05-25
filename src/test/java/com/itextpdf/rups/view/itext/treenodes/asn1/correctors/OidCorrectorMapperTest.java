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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class OidCorrectorMapperTest {
    @Test
    void get_Known() throws IOException {
        /*
         * Here we make sure, that all corrector classes, which have a mappable
         * OID, are processed correctly by OidCorrectorMapper. Correctors,
         * which should be included, are extracted via reflection.
         */
        final Set<Class<?>> correctors = new HashSet<>();
        addCorrectorClassesFromPackage(correctors, getClass().getPackageName());
        for (final Class<?> corrector : correctors) {
            final String oid = getCorrectorOid(corrector);
            if (oid != null) {
                Assertions.assertInstanceOf(corrector, OidCorrectorMapper.get(oid));
            }
        }
    }

    @Test
    void get_Null() {
        // Should return a default corrector
        Assertions.assertInstanceOf(DefaultCorrector.class, OidCorrectorMapper.get(null));
    }

    @Test
    void get_Unknown() {
        // Should return a default corrector
        Assertions.assertInstanceOf(DefaultCorrector.class, OidCorrectorMapper.get("7.7.7"));
    }

    private static void addCorrectorClassesFromPackage(Set<Class<?>> output, String pkg) throws IOException {
        final Enumeration<URL> urls = ClassLoader.getSystemResources(pkg.replace('.', '/'));
        while (urls.hasMoreElements()) {
            final URL url = urls.nextElement();
            if (url == null) {
                continue;
            }
            try (final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(url.openStream(), Charset.defaultCharset()))) {
                while (true) {
                    final String entry = reader.readLine();
                    if (entry == null) {
                        break;
                    }
                    // In this case we assume a sub-package
                    if (!entry.contains(".")) {
                        addCorrectorClassesFromPackage(output, pkg + '.' + entry);
                    } else if (entry.endsWith("Corrector.class")) {
                        addClassIfExists(output, pkg + '.' + entry.substring(0, entry.length() - 6));
                    }
                }
            }
        }
    }

    private static void addClassIfExists(Set<Class<?>> output, String clsName) {
        try {
            output.add(Class.forName(clsName));
        } catch (Exception ignored) {
            // noop
        }
    }

    private String getCorrectorOid(Class<?> cls) {
        try {
            return (String) cls.getField("OID").get(null);
        } catch (Exception ignored) {
            return null;
        }
    }
}