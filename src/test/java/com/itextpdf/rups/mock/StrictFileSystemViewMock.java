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
package com.itextpdf.rups.mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

/**
 * FileSystemView child, which reimplements all its methods to throw an
 * {@link UnsupportedOperationException}. This way you can mock single methods
 * in tests and catch unexpected calls to other methods.
 */
public class StrictFileSystemViewMock extends FileSystemView {
    @Override
    public boolean isRoot(File f) {
        throw new MethodNotMockedException();
    }

    @Override
    public Boolean isTraversable(File f) {
        throw new MethodNotMockedException();
    }

    @Override
    public String getSystemDisplayName(File f) {
        throw new MethodNotMockedException();
    }

    @Override
    public String getSystemTypeDescription(File f) {
        throw new MethodNotMockedException();
    }

    @Override
    public Icon getSystemIcon(File f) {
        throw new MethodNotMockedException();
    }

    @Override
    public boolean isParent(File folder, File file) {
        throw new MethodNotMockedException();
    }

    @Override
    public File getChild(File parent, String fileName) {
        throw new MethodNotMockedException();
    }

    @Override
    public boolean isFileSystem(File f) {
        throw new MethodNotMockedException();
    }

    @Override
    public File createNewFolder(File containingDir) throws IOException {
        throw new MethodNotMockedException();
    }

    @Override
    public boolean isHiddenFile(File f) {
        throw new MethodNotMockedException();
    }

    @Override
    public boolean isFileSystemRoot(File dir) {
        throw new MethodNotMockedException();
    }

    @Override
    public boolean isDrive(File dir) {
        throw new MethodNotMockedException();
    }

    @Override
    public boolean isFloppyDrive(File dir) {
        throw new MethodNotMockedException();
    }

    @Override
    public boolean isComputerNode(File dir) {
        throw new MethodNotMockedException();
    }

    @Override
    public File[] getRoots() {
        throw new MethodNotMockedException();
    }

    @Override
    public File getHomeDirectory() {
        throw new MethodNotMockedException();
    }

    @Override
    public File getDefaultDirectory() {
        throw new MethodNotMockedException();
    }

    @Override
    public File createFileObject(File dir, String filename) {
        throw new MethodNotMockedException();
    }

    @Override
    public File createFileObject(String path) {
        throw new MethodNotMockedException();
    }

    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        throw new MethodNotMockedException();
    }

    @Override
    public File getParentDirectory(File dir) {
        throw new MethodNotMockedException();
    }

    @Override
    public File[] getChooserComboBoxFiles() {
        throw new MethodNotMockedException();
    }

    @Override
    public boolean isLink(File file) {
        throw new MethodNotMockedException();
    }

    @Override
    public File getLinkLocation(File file) throws FileNotFoundException {
        throw new MethodNotMockedException();
    }

    @Override
    protected File createFileSystemRoot(File f) {
        throw new MethodNotMockedException();
    }
}
