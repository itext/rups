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

/**
 * Static class, which stores all PDF content stream operators as
 * {@code char[]}.
 */
@SuppressWarnings({"java:S1845", "java:S2386"})
public final class PdfOperators {
    /*
     * General graphics state
     */
    public static final char[] w = new char[] {'w'};
    public static final char[] J = new char[] {'J'};
    public static final char[] j = new char[] {'j'};
    public static final char[] M = new char[] {'M'};
    public static final char[] d = new char[] {'d'};
    public static final char[] ri = new char[] {'r', 'i'};
    public static final char[] i = new char[] {'i'};
    public static final char[] gs = new char[] {'g', 's'};
    public static final char[] Q = new char[] {'Q'};
    public static final char[] q = new char[] {'q'};
    /*
     * Special graphics state
     */
    public static final char[] cm = new char[] {'c', 'm'};
    /*
     * Path construction
     */
    public static final char[] m = new char[] {'m'};
    public static final char[] l = new char[] {'l'};
    public static final char[] c = new char[] {'c'};
    public static final char[] v = new char[] {'v'};
    public static final char[] y = new char[] {'y'};
    public static final char[] h = new char[] {'h'};
    public static final char[] re = new char[] {'r', 'e'};
    /*
     * Path painting
     */
    public static final char[] S = new char[] {'S'};
    public static final char[] s = new char[] {'s'};
    public static final char[] F = new char[] {'F'};
    public static final char[] f = new char[] {'f'};
    public static final char[] f_STAR = new char[] {'f', '*'};
    public static final char[] B = new char[] {'B'};
    public static final char[] B_STAR = new char[] {'B', '*'};
    public static final char[] b = new char[] {'b'};
    public static final char[] b_STAR = new char[] {'b', '*'};
    public static final char[] n = new char[] {'n'};
    /*
     * Clipping paths
     */
    public static final char[] W = new char[] {'W'};
    public static final char[] W_STAR = new char[] {'W', '*'};
    /*
     * Text objects
     */
    public static final char[] BT = new char[] {'B', 'T'};
    public static final char[] ET = new char[] {'E', 'T'};
    /*
     * Text state
     */
    public static final char[] Tc = new char[] {'T', 'c'};
    public static final char[] Tw = new char[] {'T', 'w'};
    public static final char[] Tz = new char[] {'T', 'z'};
    public static final char[] TL = new char[] {'T', 'L'};
    public static final char[] Tf = new char[] {'T', 'f'};
    public static final char[] Tr = new char[] {'T', 'r'};
    public static final char[] Ts = new char[] {'T', 's'};
    /*
     * Text positioning
     */
    public static final char[] Td = new char[] {'T', 'd'};
    public static final char[] TD = new char[] {'T', 'D'};
    public static final char[] Tm = new char[] {'T', 'm'};
    public static final char[] T_STAR = new char[] {'T', '*'};
    /*
     * Text showing
     */
    public static final char[] Tj = new char[] {'T', 'j'};
    public static final char[] TJ = new char[] {'T', 'J'};
    public static final char[] SINGLE_QUOTE = new char[] {'\''};
    public static final char[] DOUBLE_QUOTE = new char[] {'"'};
    /*
     * Type 3 fonts
     */
    public static final char[] d0 = new char[] {'d', '0'};
    public static final char[] d1 = new char[] {'d', '1'};
    /*
     * Colour
     */
    public static final char[] CS = new char[] {'C', 'S'};
    public static final char[] cs = new char[] {'c', 's'};
    public static final char[] SC = new char[] {'S', 'C'};
    public static final char[] sc = new char[] {'s', 'c'};
    public static final char[] SCN = new char[] {'S', 'C', 'N'};
    public static final char[] scn = new char[] {'s', 'c', 'n'};
    public static final char[] G = new char[] {'G'};
    public static final char[] g = new char[] {'g'};
    public static final char[] RG = new char[] {'R', 'G'};
    public static final char[] rg = new char[] {'r', 'g'};
    public static final char[] K = new char[] {'K'};
    public static final char[] k = new char[] {'k'};
    /*
     * Shading patterns
     */
    public static final char[] Sh = new char[] {'S', 'h'};
    /*
     * Inline images
     */
    public static final char[] BI = new char[] {'B', 'I'};
    public static final char[] ID = new char[] {'I', 'D'};
    public static final char[] EI = new char[] {'E', 'I'};
    /*
     * XObjects
     */
    public static final char[] Do = new char[] {'D', 'o'};
    /*
     * Marked-content
     */
    public static final char[] MP = new char[] {'M', 'P'};
    public static final char[] DP = new char[] {'D', 'P'};
    public static final char[] BMC = new char[] {'B', 'M', 'C'};
    public static final char[] BDC = new char[] {'B', 'D', 'C'};
    public static final char[] EMC = new char[] {'E', 'M', 'C'};
    /*
     * Compatibility
     */
    public static final char[] BX = new char[] {'B', 'X'};
    public static final char[] EX = new char[] {'E', 'X'};

    private PdfOperators() {
        // Static class
    }
}
