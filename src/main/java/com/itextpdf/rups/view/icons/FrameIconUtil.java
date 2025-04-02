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
package com.itextpdf.rups.view.icons;

import com.itextpdf.rups.Rups;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class FrameIconUtil {
    /**
     * Scale between two subsequent icon sizes.
     */
    private static final int DIM_SCALE = 2;
    /**
     * Minimum width/height of an icon.
     */
    private static final int MIN_DIM = 16;
    /**
     * Maximum width/height of an icon.
     */
    private static final int MAX_DIM = 1024;
    /**
     * Path in resources to the logo.
     */
    private static final String LOGO_ICON = "logo.png";

    private FrameIconUtil() {
        // static class
    }

    public static List<Image> loadFrameIcons() {
        return LazyHolder.SCALED_ICONS;
    }

    /**
     * Wrapper to lazily load the logo from resource and prepare all scaled
     * versions of it.
     */
    private static final class LazyHolder {
        private static final List<Image> SCALED_ICONS;

        static {
            final Image image = Toolkit.getDefaultToolkit()
                    .getImage(Rups.class.getResource(LOGO_ICON));
            SCALED_ICONS = IntStream.iterate(MIN_DIM, dim -> dim <= MAX_DIM, dim -> dim * DIM_SCALE)
                    .mapToObj(dim -> image.getScaledInstance(dim, dim, Image.SCALE_SMOOTH))
                    .collect(Collectors.toUnmodifiableList());
        }
    }
}
