package org.appwork.swing.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.appwork.app.gui.BasicGui;
import org.appwork.app.gui.MigPanel;

public class ExtPasswordField extends MigPanel implements FocusListener, DocumentListener {

    public static void main(final String[] args) {
        new BasicGui("ExtPasswordField") {

            @Override
            protected void layoutPanel() {
                final ExtPasswordField pw = new ExtPasswordField();
                final ExtPasswordField pwtext = new ExtPasswordField();
                pwtext.setPassword("thomas".toCharArray());
                final ExtPasswordField pwhelp = new ExtPasswordField();
                pwhelp.setName("pwhelp");
                final ExtPasswordField pwhelptext = new ExtPasswordField();
                pwhelptext.setPassword("thomas".toCharArray());
                pwhelp.setHelpText("Please give me a password");
                pwhelptext.setHelpText("BLABLA gimme a pw");
                final MigPanel p = new MigPanel("ins 0,wrap 2", "[][grow,fill]", "[]");
                this.getFrame().setContentPane(p);
                p.add(new JLabel("PW field"));
                p.add(pw);
                p.add(new JLabel("PW width help text"));
                p.add(pwhelp);
                p.add(new JLabel("PW field setpw"));
                p.add(pwtext);
                p.add(new JLabel("PW field setpw &helptext"));
                p.add(pwhelptext);
                p.add(new JButton(new AbstractAction() {
                    {
                        this.putValue(Action.NAME, "Print");
                    }

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        System.out.println(new String(pw.getPassword()));
                        System.out.println(new String(pwhelp.getPassword()));
                        System.out.println(new String(pwtext.getPassword()));
                        System.out.println(new String(pwhelptext.getPassword()));
                    }
                }));

            }

            @Override
            protected void requestExit() {
                // TODO Auto-generated method stub

            }
        };
    }

    private final ExtTextField   renderer;

    private final JPasswordField editor;

    private boolean              rendererMode;

    private char[]               password = new char[] {};
    private static String        MASK     = "••••••••••";

    /**
     * @param constraints
     * @param columns
     * @param rows
     */
    public ExtPasswordField() {
        super("ins 0", "[grow,fill]", "[grow,fill]");
        this.renderer = new ExtTextField();
        this.editor = new JPasswordField();

        this.renderer.addFocusListener(this);
        this.editor.addFocusListener(this);
        this.add(this.renderer, "hidemode 3");
        this.add(this.editor, "hidemode 3");
        this.editor.setText(ExtPasswordField.MASK);
        // this.renderer.setBackground(Color.RED);
        this.renderer.setText(ExtPasswordField.MASK);
        this.editor.getDocument().addDocumentListener(this);
        this.setRendererMode(true);

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        if (!this.equal(this.editor.getPassword(), ExtPasswordField.MASK.toCharArray())) {
            this.onChanged();
        }

    }

    /**
     * @param pw
     * @param ca
     * @return
     */
    private boolean equal(final char[] pw, final char[] ca) {
        boolean equals = pw.length == ca.length;
        if (equals) {
            for (int i = 0; i < pw.length; i++) {
                if (pw[i] != ca[i]) {
                    equals = false;
                    break;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    @Override
    public void focusGained(final FocusEvent e) {

        if (e.getSource() == this.renderer) {
            this.setRendererMode(false);
            this.editor.requestFocus();
        } else {
            this.editor.setText(ExtPasswordField.MASK);
            this.editor.selectAll();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
    @Override
    public void focusLost(final FocusEvent e) {

        if (e.getSource() == this.editor) {
            final char[] pw = this.editor.getPassword();
            final char[] ca = ExtPasswordField.MASK.toCharArray();

            if (!this.equal(pw, ca)) {
                this.password = pw;
                this.editor.setText(ExtPasswordField.MASK);
            }
            this.setRendererMode(true);
            this.setHelpText(this.getHelpText());
        }
    }

    public javax.swing.text.Document getDocument() {
        return this.editor.getDocument();
    }

    public Color getHelpColor() {
        return this.renderer.getHelpColor();
    }

    public String getHelpText() {
        return this.renderer.getHelpText();
    }

    public char[] getPassword() {
        return this.password;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        if (!this.equal(this.editor.getPassword(), ExtPasswordField.MASK.toCharArray())) {
            this.onChanged();
        }

    }

    protected void onChanged() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        if (!this.equal(this.editor.getPassword(), ExtPasswordField.MASK.toCharArray())) {
            this.onChanged();
        }

    }

    @Override
    public void setEnabled(final boolean b) {
        this.editor.setEnabled(b);
        this.renderer.setEnabled(b);
        super.setEnabled(b);
    }

    public void setHelpColor(final Color helpColor) {
        this.renderer.setHelpColor(helpColor);
    }

    /**
     * @param addLinksDialog_layoutDialogContent_input_help
     */
    public void setHelpText(final String helpText) {
        this.renderer.setHelpText(helpText);

        if (this.getHelpText() != null && (this.getPassword() == null || this.getPassword().length == 0 || ExtPasswordField.MASK.equals(new String(this.getPassword())))) {
            this.renderer.setText(this.getHelpText());
            this.renderer.setForeground(this.getHelpColor());
        } else {
            this.renderer.setText(ExtPasswordField.MASK);
            this.renderer.setForeground(this.renderer.getDefaultColor());
        }
        this.setRendererMode(this.rendererMode);

    }

    public void setPassword(final char[] password) {
        this.password = password;

        this.setHelpText(this.getHelpText());
    }

    /**
     * @param b
     */
    private void setRendererMode(boolean b) {
        this.rendererMode = b;
        b &= this.getHelpText() != null;
        this.renderer.setVisible(b);
        this.editor.setVisible(!b);
        this.revalidate();
    }

}