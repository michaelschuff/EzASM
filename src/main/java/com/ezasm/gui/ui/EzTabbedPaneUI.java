package com.ezasm.gui.ui;

import com.ezasm.gui.util.EditorTheme;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;

public class EzTabbedPaneUI extends BasicTabbedPaneUI {

    private final EditorTheme editorTheme;
    private Color foreground;
    private Color unselectedBackground;
    private Color selectedBackground;

    public EzTabbedPaneUI(EditorTheme editorTheme) {
        super();
        this.editorTheme = editorTheme;
    }

    @Override
    public void installUI(JComponent component) {
        super.installUI(component);
        selectedBackground = editorTheme.background();
        unselectedBackground = editorTheme.currentLine();
        foreground = editorTheme.foreground();
    }

    @Override
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        return new Insets(3, 10, 10, 3);
    }

    @Override
    protected void paintTabBorder(Graphics graphics, int tabPlacement, int tabIndex, int x, int y, int w, int h,
            boolean isSelected) {
    }

    @Override
    protected void paintContentBorder(Graphics graphics, int tabPlacement, int selectedIndex) {
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title,
            Rectangle textRect, boolean isSelected) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setFont(font);

        View v = getTextViewForTab(tabIndex);
        if (v != null) { // html
            v.paint(graphics2D, textRect);
        } else { // plain text
            int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);
            if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex)) {
                if (isSelected) {
                    graphics2D.setColor(foreground);
                } else {
                    graphics2D.setColor(editorTheme.modifyAwayFromBackground(unselectedBackground, 3));
                }
                BasicGraphicsUtils.drawStringUnderlineCharAt(tabPane, graphics2D, title, mnemIndex, textRect.x,
                        textRect.y + metrics.getAscent());
            } else { // tab disabled
                graphics2D.setColor(unselectedBackground.brighter());
                BasicGraphicsUtils.drawStringUnderlineCharAt(tabPane, graphics2D, title, mnemIndex, textRect.x,
                        textRect.y + metrics.getAscent());
                graphics2D.setColor(unselectedBackground.darker());
                BasicGraphicsUtils.drawStringUnderlineCharAt(tabPane, graphics2D, title, mnemIndex, textRect.x - 1,
                        textRect.y + metrics.getAscent() - 1);
            }
        }
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
            Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
            boolean isSelected) {
        if (isSelected) {
            g.setColor(selectedBackground);
        } else {
            g.setColor(unselectedBackground);
        }
        g.fillRect(x, y, w, h);
    }
}
