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
package com.itextpdf.rups.view.itext;

import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

/**
 * Interface for handling events from {@link PdfObjectPanel}.
 */
public interface IPdfObjectPanelEventListener {
    /**
     * Handler for an event, when a new child was added to a PDF array.
     *
     * @param value  Child object, that is being added.
     * @param parent Parent for the object in the tree.
     * @param index  Index in the parent's tree for the object.
     */
    void handleArrayChildAdded(PdfObject value, PdfObjectTreeNode parent, int index);

    /**
     * Handler for an event, when an object was deleted from a PDF array.
     *
     * @param parent Parent for the object in the tree.
     * @param index  Index in the parent's tree for the object.
     */
    void handleArrayChildDeleted(PdfObjectTreeNode parent, int index);

    /**
     * Handler for an event, when a new child was added to a PDF dictionary.
     *
     * @param value  Child object, that is being added.
     * @param parent Parent for the object in the tree.
     * @param key    Key, under which the object is added.
     * @param index  Index in the parent's tree for the object.
     */
    void handleDictChildAdded(PdfObject value, PdfObjectTreeNode parent, PdfName key, int index);


    /**
     * Handler for an event, when an object was deleted from a PDF dictionary.
     *
     * @param parent Parent for the object in the tree.
     * @param key    Key, under which the object was located.
     */
    void handleDictChildDeleted(PdfObjectTreeNode parent, PdfName key);
}
