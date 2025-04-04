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

import com.itextpdf.rups.RupsConfiguration;
import com.itextpdf.rups.conf.LookAndFeelId;
import com.itextpdf.rups.view.icons.FrameIconUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * The window responsible for holding the UI to set the preferences.
 */
public final class PreferencesWindow {

    private JDialog jDialog;

    private GridBagLayout gridBagLayout;
    private GridBagConstraints left;
    private GridBagConstraints right;

    private JPanel visualPanel;
    private JScrollPane generalSettingsScrollPane;

    // Fields to reset
    private JCheckBox openDuplicateFiles;
    private JTextField pathField;
    private JLabel restartLabel;
    private JComboBox<String> localeBox;
    /**
     * Combo box for selecting Look & Feel.
     */
    private JComboBox<LookAndFeelId> lookAndFeelBox;

    public PreferencesWindow() {
        initializeJDialog();
        initializeLayout();

        createGeneralSettingsTab();
        createVisualSettingsTab();
        createTabbedPane();
        createSaveCancelResetSection();

        completeJDialogCreation();
    }

    private void initializeJDialog() {
        this.jDialog = new JDialog();

        this.jDialog.setTitle(Language.PREFERENCES.getString());
        this.jDialog.setIconImages(FrameIconUtil.loadFrameIcons());
        this.jDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.jDialog.setModal(true);
        this.jDialog.setLayout(new BorderLayout());
        this.jDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                RupsConfiguration.INSTANCE.cancelTemporaryChanges();
                resetView();
            }
        });
    }

    private void initializeLayout() {
        final Insets insets = new Insets(2, 2, 2, 2);

        this.gridBagLayout = new GridBagLayout();

        this.left = new GridBagConstraints();
        this.left.insets = insets;
        this.left.anchor = GridBagConstraints.EAST;

        this.right = new GridBagConstraints();
        this.right.insets = insets;
        this.right.weightx = 2.0;
        this.right.fill = GridBagConstraints.HORIZONTAL;
        this.right.gridwidth = GridBagConstraints.REMAINDER;
    }

    private void createGeneralSettingsTab() {
        this.pathField = new JTextField(RupsConfiguration.INSTANCE.getHomeFolder().getPath(), 30);
        JLabel pathLabel = new JLabel(Language.PREFERENCES_OPEN_FOLDER.getString());
        pathLabel.setLabelFor(this.pathField);

        JButton pathChooser = new JButton(Language.PREFERENCES_SELECT_NEW_DEFAULT_FOLDER.getString());
        pathChooser.addActionListener((ActionEvent e) -> {
            JFileChooser fileChooser = new JFileChooser(RupsConfiguration.INSTANCE.getHomeFolder());
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int choice = fileChooser.showOpenDialog(jDialog);

            if (choice == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getPath();
                this.pathField.setText(path);
                RupsConfiguration.INSTANCE.setHomeFolder(path);
            }
        });

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.add(this.pathField);
        fieldsPanel.add(pathChooser);

        this.openDuplicateFiles = new JCheckBox("", RupsConfiguration.INSTANCE.canOpenDuplicateFiles());
        this.openDuplicateFiles.addActionListener((ActionEvent e) ->
            RupsConfiguration.INSTANCE.setOpenDuplicateFiles(((JCheckBox) e.getSource()).isSelected())
        );
        JLabel openDuplicateFilesLabel = new JLabel(Language.PREFERENCES_ALLOW_DUPLICATE_FILES.getString());
        openDuplicateFilesLabel.setLabelFor(this.openDuplicateFiles);

        JPanel generalSettingsPanel = new JPanel();
        generalSettingsPanel.setLayout(this.gridBagLayout);

        generalSettingsPanel.add(pathLabel, this.left);
        generalSettingsPanel.add(fieldsPanel, this.right);

        generalSettingsPanel.add(openDuplicateFilesLabel, this.left);
        generalSettingsPanel.add(this.openDuplicateFiles, this.right);

        this.generalSettingsScrollPane = new JScrollPane(generalSettingsPanel);
    }

    private void createVisualSettingsTab() {
        /*
         * Kind of a hack. Instead of using label visibility, we will set it
         * to " ". This way it has a height, so the place for the label remains
         * in the window and, because of that, component do not jump. Would
         * assume there is a better way to do this...
         */
        this.restartLabel = new JLabel(" ");

        this.lookAndFeelBox = new JComboBox<>();
        for (final LookAndFeelId laf : RupsConfiguration.SUPPORTED_LOOK_AND_FEEL) {
            this.lookAndFeelBox.addItem(laf);
        }
        this.lookAndFeelBox.setSelectedItem(RupsConfiguration.INSTANCE.getLookAndFeel());
        this.lookAndFeelBox.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                RupsConfiguration.INSTANCE.setLookAndFeel((LookAndFeelId) e.getItem());
                this.restartLabel.setText(Language.PREFERENCES_NEED_RESTART.getString());
            }
        });

        final JLabel lookAndFeelLabel = new JLabel(Language.LOOK_AND_FEEL.getString());
        lookAndFeelLabel.setLabelFor(this.lookAndFeelBox);

        this.localeBox = new JComboBox<>();
        this.localeBox.addItem("nl-NL");
        this.localeBox.addItem("en-US");
        this.localeBox.setSelectedItem(RupsConfiguration.INSTANCE.getUserLocale().toLanguageTag());
        this.localeBox.addActionListener((ActionEvent e) -> {
            Object selectedItem = localeBox.getSelectedItem();
            String selectedString = (String) selectedItem;
            RupsConfiguration.INSTANCE.setUserLocale(Locale.forLanguageTag(selectedString));
            this.restartLabel.setText(Language.PREFERENCES_NEED_RESTART.getString());
        });

        final JLabel localeLabel = new JLabel(Language.LOCALE.getString());
        localeLabel.setLabelFor(localeBox);

        this.visualPanel = new JPanel();
        this.visualPanel.setLayout(this.gridBagLayout);

        this.visualPanel.add(lookAndFeelLabel, this.left);
        this.visualPanel.add(this.lookAndFeelBox, this.right);
        this.visualPanel.add(localeLabel, this.left);
        this.visualPanel.add(this.localeBox, this.right);
        this.visualPanel.add(this.restartLabel, this.right);
    }

    private void createTabbedPane() {
        final JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.add(Language.PREFERENCES_RUPS_SETTINGS.getString(), this.generalSettingsScrollPane);
        tabbedPane.add(Language.PREFERENCES_VISUAL_SETTINGS.getString(), this.visualPanel);

        this.jDialog.add(tabbedPane, BorderLayout.CENTER);
    }

    private void createSaveCancelResetSection() {
        JPanel buttons = new JPanel();

        JButton save = new JButton(Language.SAVE.getString());
        save.addActionListener(this::handleSave);
        buttons.add(save);

        JButton cancel = new JButton(Language.DIALOG_CANCEL.getString());
        cancel.addActionListener(this::handleCancel);
        buttons.add(cancel);

        JButton reset = new JButton(Language.PREFERENCES_RESET_TO_DEFAULTS.getString());

        reset.addActionListener((ActionEvent e) -> {
            int choice = JOptionPane.showConfirmDialog(jDialog,
                    Language.PREFERENCES_RESET_TO_DEFAULTS_CONFIRM.getString());
            if (choice == JOptionPane.OK_OPTION) {
                RupsConfiguration.INSTANCE.resetToDefaultProperties();
                resetView();
            }
        });
        buttons.add(reset);

        jDialog.add(buttons, BorderLayout.SOUTH);
    }

    private void completeJDialogCreation() {
        this.jDialog.pack();
        this.jDialog.setResizable(false);
    }

    private void resetView() {
        this.pathField.setText(RupsConfiguration.INSTANCE.getHomeFolder().getPath());
        this.openDuplicateFiles.setSelected(RupsConfiguration.INSTANCE.canOpenDuplicateFiles());
        this.lookAndFeelBox.setSelectedItem(RupsConfiguration.INSTANCE.getLookAndFeel());
        this.localeBox.setSelectedItem(RupsConfiguration.INSTANCE.getUserLocale().toLanguageTag());
        this.restartLabel.setText(" ");
    }

    public void show(Component component) {
        jDialog.setLocationRelativeTo(component);
        jDialog.setVisible(true);
    }

    private void handleSave(ActionEvent e) {
        RupsConfiguration.INSTANCE.saveConfiguration();
        resetView();
        jDialog.dispose();
    }

    private void handleCancel(ActionEvent e) {
        // Warn user of unsaved changes via dialog
        if (RupsConfiguration.INSTANCE.hasUnsavedChanges()) {
            final int choice = JOptionPane.showConfirmDialog(
                    jDialog, Language.SAVE_UNSAVED_CHANGES.getString()
            );
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
        }
        RupsConfiguration.INSTANCE.cancelTemporaryChanges();
        resetView();
        jDialog.dispose();
    }
}
