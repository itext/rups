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
package com.itextpdf.rups.view;

import com.itextpdf.rups.model.FilePathPreProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

/**
 * An enhanced file system view, with which the file chooser can handle more
 * path use cases:
 * <ul>
 *     <li>URIs with the {@code file} schema.</li>
 *     <li>URL-encoded paths.</li>
 * </ul>
 * <p>
 * All these additional use cases are only triggered, when it wouldn't prevent
 * opening an existing file with the original path name.
 * </p>
 */
public final class EnhancedFileSystemView extends FileSystemView {
    /**
     * Singleton instance of the file system view.
     */
    public static final EnhancedFileSystemView INSTANCE = new EnhancedFileSystemView();

    /**
     * Base {@link FileSystemView} object to forward requests to.
     */
    private final FileSystemView baseFileSystemView;

    /**
     * Creates a new enhanced file system view
     */
    public EnhancedFileSystemView() {
        this.baseFileSystemView = FileSystemView.getFileSystemView();
    }

    /**
     * Creates a new enhanced file system view as a wrapper over an existing
     * one.
     *
     * @param baseFileSystemView base file system view to wrap
     */
    public EnhancedFileSystemView(FileSystemView baseFileSystemView) {
        this.baseFileSystemView = Objects.requireNonNull(baseFileSystemView);
    }

    /*
     * These three methods below are just delegated to baseFilesSystemView,
     * but with the file name going through our path pre-processor.
     */

    @Override
    public File createFileObject(File dir, String filename) {
        return baseFileSystemView.createFileObject(dir, FilePathPreProcessor.process(dir, filename));
    }

    @Override
    public File createFileObject(String path) {
        return baseFileSystemView.createFileObject(FilePathPreProcessor.process(path));
    }

    @Override
    public File getChild(File parent, String fileName) {
        return baseFileSystemView.getChild(parent, FilePathPreProcessor.process(parent, fileName));
    }

    /*
     * All methods below are just delegated to baseFilesSystemView without any
     * modifications.
     */

    @Override
    public boolean isRoot(File f) {
        return baseFileSystemView.isRoot(f);
    }

    @Override
    public Boolean isTraversable(File f) {
        return baseFileSystemView.isTraversable(f);
    }

    @Override
    public String getSystemDisplayName(File f) {
        return baseFileSystemView.getSystemDisplayName(f);
    }

    @Override
    public String getSystemTypeDescription(File f) {
        return baseFileSystemView.getSystemTypeDescription(f);
    }

    @Override
    public Icon getSystemIcon(File f) {
        return baseFileSystemView.getSystemIcon(f);
    }

    @Override
    public boolean isParent(File folder, File file) {
        return baseFileSystemView.isParent(folder, file);
    }

    @Override
    public boolean isFileSystem(File f) {
        return baseFileSystemView.isFileSystem(f);
    }

    @Override
    public File createNewFolder(File containingDir) throws IOException {
        return baseFileSystemView.createNewFolder(containingDir);
    }

    @Override
    public boolean isHiddenFile(File f) {
        return baseFileSystemView.isHiddenFile(f);
    }

    @Override
    public boolean isFileSystemRoot(File dir) {
        return baseFileSystemView.isFileSystemRoot(dir);
    }

    @Override
    public boolean isDrive(File dir) {
        return baseFileSystemView.isDrive(dir);
    }

    @Override
    public boolean isFloppyDrive(File dir) {
        return baseFileSystemView.isFloppyDrive(dir);
    }

    @Override
    public boolean isComputerNode(File dir) {
        return baseFileSystemView.isComputerNode(dir);
    }

    @Override
    public File[] getRoots() {
        return baseFileSystemView.getRoots();
    }

    @Override
    public File getHomeDirectory() {
        return baseFileSystemView.getHomeDirectory();
    }

    @Override
    public File getDefaultDirectory() {
        return baseFileSystemView.getDefaultDirectory();
    }

    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        return baseFileSystemView.getFiles(dir, useFileHiding);
    }

    @Override
    public File getParentDirectory(File dir) {
        return baseFileSystemView.getParentDirectory(dir);
    }

    @Override
    public File[] getChooserComboBoxFiles() {
        return baseFileSystemView.getChooserComboBoxFiles();
    }

    @Override
    public boolean isLink(File file) {
        return baseFileSystemView.isLink(file);
    }

    @Override
    public File getLinkLocation(File file) throws FileNotFoundException {
        return baseFileSystemView.getLinkLocation(file);
    }
}
