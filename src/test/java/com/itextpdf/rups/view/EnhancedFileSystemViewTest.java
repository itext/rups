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

import com.itextpdf.rups.mock.StrictFileSystemViewMock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
class EnhancedFileSystemViewTest {
    /*
     * We will use temp files as inputs, so that we don't affect actual files
     * accidentally...
     */

    private static volatile Path tempFile;
    private static volatile Path tempDir;

    @BeforeAll
    static void beforeAll() throws IOException {
        tempDir = Files.createTempDirectory("rups-test-");
        tempFile = Files.createTempFile(tempDir, null, null);
    }

    @AfterAll
    static void afterAll() throws IOException {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
            tempFile = null;
        }
        if (tempDir != null) {
            Files.deleteIfExists(tempDir);
            tempDir = null;
        }
    }

    /*
     * Methods tested below should have FilePathPreProcessor mappings. Just
     * testing, that it is called here.
     */

    @Test
    void createFileObject() {
        final String testPath = tempFile.toUri().toString();
        final String expectedPath = tempFile.toString().replace(File.separatorChar, '/');
        final File expectedResult = tempFile.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public File createFileObject(String path) {
                Assertions.assertEquals(expectedPath, path);
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.createFileObject(testPath));
    }

    @Test
    void createFileObjectWithDir() {
        final File testDir = tempDir.toFile();
        final String testFilename = tempFile.toUri().toString();
        final String expectedFilename = tempFile.toString().replace(File.separatorChar, '/');
        final File expectedResult = tempFile.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public File createFileObject(File dir, String filename) {
                Assertions.assertSame(testDir, dir);
                Assertions.assertEquals(expectedFilename, filename);
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.createFileObject(testDir, testFilename));
    }

    @Test
    void getChild() {
        final File testParent = tempDir.toFile();
        final String testFilename = tempFile.toUri().toString();
        final String expectedFilename = tempFile.toString().replace(File.separatorChar, '/');
        final File expectedResult = tempFile.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public File getChild(File parent, String filename) {
                Assertions.assertSame(testParent, parent);
                Assertions.assertEquals(expectedFilename, filename);
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.getChild(testParent, testFilename));
    }

    /*
     * All methods tested below are just proxies, so just a basic call test.
     */

    @Test
    void isRoot() {
        final File testFile = tempFile.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public boolean isRoot(File f) {
                Assertions.assertSame(testFile, f);
                return false;
            }
        });
        Assertions.assertFalse(fs.isRoot(testFile));
    }

    @Test
    void isTraversable() {
        final File testFile = tempFile.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public Boolean isTraversable(File f) {
                Assertions.assertSame(testFile, f);
                return Boolean.FALSE;
            }
        });
        Assertions.assertFalse(fs.isTraversable(testFile));
    }

    @Test
    void getSystemDisplayName() {
        final File testFile = tempFile.toFile();
        final String expectedResult = "result-string";
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public String getSystemDisplayName(File f) {
                Assertions.assertSame(testFile, f);
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.getSystemDisplayName(testFile));
    }

    @Test
    void getSystemTypeDescription() {
        final File testFile = tempFile.toFile();
        final String expectedResult = "result-string";
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public String getSystemTypeDescription(File f) {
                Assertions.assertSame(testFile, f);
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.getSystemTypeDescription(testFile));
    }

    @Test
    void getSystemIcon() {
        final File testFile = tempFile.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public Icon getSystemIcon(File f) {
                Assertions.assertSame(testFile, f);
                return null;
            }
        });
        Assertions.assertNull(fs.getSystemIcon(testFile));
    }

    @Test
    void isParent() {
        final File testFolder = tempDir.toFile();
        final File testFile = tempFile.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public boolean isParent(File folder, File file) {
                Assertions.assertSame(testFolder, folder);
                Assertions.assertSame(testFile, file);
                return false;
            }
        });
        Assertions.assertFalse(fs.isParent(testFolder, testFile));
    }

    @Test
    void isFileSystem() {
        final File testFile = tempFile.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public boolean isFileSystem(File f) {
                Assertions.assertSame(testFile, f);
                return false;
            }
        });
        Assertions.assertFalse(fs.isFileSystem(testFile));
    }

    @Test
    void createNewFolder() throws IOException {
        final File testContainingDir = tempDir.toFile();
        final File expectedResult = tempFile.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public File createNewFolder(File containingDir) {
                Assertions.assertSame(testContainingDir, containingDir);
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.createNewFolder(testContainingDir));
    }

    @Test
    void isHiddenFile() {
        final File testFile = tempFile.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public boolean isHiddenFile(File f) {
                Assertions.assertSame(testFile, f);
                return false;
            }
        });
        Assertions.assertFalse(fs.isHiddenFile(testFile));
    }

    @Test
    void isFileSystemRoot() {
        final File testDir = tempDir.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public boolean isFileSystemRoot(File dir) {
                Assertions.assertSame(testDir, dir);
                return false;
            }
        });
        Assertions.assertFalse(fs.isFileSystemRoot(testDir));
    }

    @Test
    void isDrive() {
        final File testDir = tempDir.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public boolean isDrive(File dir) {
                Assertions.assertSame(testDir, dir);
                return false;
            }
        });
        Assertions.assertFalse(fs.isDrive(testDir));
    }

    @Test
    void isFloppyDrive() {
        final File testDir = tempDir.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public boolean isFloppyDrive(File dir) {
                Assertions.assertSame(testDir, dir);
                return false;
            }
        });
        Assertions.assertFalse(fs.isFloppyDrive(testDir));
    }

    @Test
    void isComputerNode() {
        final File testDir = tempDir.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public boolean isComputerNode(File dir) {
                Assertions.assertSame(testDir, dir);
                return false;
            }
        });
        Assertions.assertFalse(fs.isComputerNode(testDir));
    }

    @Test
    void getRoots() {
        final File[] expectedResult = new File[0];
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public File[] getRoots() {
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.getRoots());
    }

    @Test
    void getHomeDirectory() {
        final File expectedResult = tempDir.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public File getHomeDirectory() {
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.getHomeDirectory());
    }

    @Test
    void getDefaultDirectory() {
        final File expectedResult = tempDir.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public File getDefaultDirectory() {
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.getDefaultDirectory());
    }

    @Test
    void getFiles() {
        final File testDir = tempDir.toFile();
        final File[] expectedResult = new File[0];
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public File[] getFiles(File dir, boolean useFileHiding) {
                Assertions.assertSame(testDir, dir);
                Assertions.assertFalse(useFileHiding);
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.getFiles(testDir, false));
    }

    @Test
    void getParentDirectory() {
        final File testDir = tempFile.toFile();
        final File expectedResult = tempDir.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public File getParentDirectory(File dir) {
                Assertions.assertSame(testDir, dir);
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.getParentDirectory(testDir));
    }

    @Test
    void getChooserComboBoxFiles() {
        final File[] expectedResult = new File[0];
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public File[] getChooserComboBoxFiles() {
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.getChooserComboBoxFiles());
    }

    @Test
    void isLink() {
        final File testFile = tempFile.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public boolean isLink(File file) {
                Assertions.assertSame(testFile, file);
                return false;
            }
        });
        Assertions.assertFalse(fs.isLink(testFile));
    }

    @Test
    void getLinkLocation() throws FileNotFoundException {
        final File testFile = tempFile.toFile();
        final File expectedResult = tempDir.toFile();
        final FileSystemView fs = new EnhancedFileSystemView(new StrictFileSystemViewMock() {
            @Override
            public File getLinkLocation(File file) {
                Assertions.assertSame(testFile, file);
                return expectedResult;
            }
        });
        Assertions.assertSame(expectedResult, fs.getLinkLocation(testFile));
    }
}