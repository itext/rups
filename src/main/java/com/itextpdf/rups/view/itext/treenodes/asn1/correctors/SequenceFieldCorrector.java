/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;

/**
 * Represents corrector function for fields in an ASN.1 sequence. Designed to
 * be used in {@link AbstractSequenceCorrector}.
 *
 * <p>
 * This is a functional interface whose functional method is
 * {@link #correct(AbstractAsn1TreeNode)}.
 *
 * <p>
 * Practically speaking, this is equivalent to
 * {@link java.util.function.Predicate<AbstractAsn1TreeNode>}. But it has very
 * different semantics, as the main result of the function call is the side
 * effect of modifying the input node argument. So a different name seems
 * appropriate.
 */
@FunctionalInterface
public interface SequenceFieldCorrector {
    /**
     * Corrects the provided ASN.1 object tree, which corresponds to a field
     * within an ASN.1 sequence. See the {@link AbstractCorrector} class
     * documentation for more information.
     *
     * <p>
     * This interface was designed with OPTIONAL and DEFAULT fields in mind.
     * Handling is done via the boolean return value:
     * <ul>
     *     <li>If the field is <i>not</i> optional, then the method should
     *     always return {@code true} and correct the tree, if possible. This
     *     will be treated as the field was processed and the next corrector
     *     in line will get a different field.</li>
     *
     *     <li>If the field is optional, but the type does <i>not</i> match,
     *     then the method should return {@code false} and do no correction.
     *     This will be treated as the field should be processed by the next
     *     corrector in line.</li>
     *
     *     <li>If the field is optional, and the type does match, then the
     *     method should return {@code true} and correct the tree, if
     *     possible. This will be treated as the field was processed and the
     *     next corrector in line will get a different field.</li>
     * </ul>
     *
     * @param node ASN.1 object tree to correct.
     *
     * @return {@code false} if the ASN.1 object should be passed to the next
     * field corrector for processing, {@code true} otherwise.
     */
    boolean correct(AbstractAsn1TreeNode node);
}
