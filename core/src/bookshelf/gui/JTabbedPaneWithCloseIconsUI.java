/*
 * @@DESCRIPTION@@. 
 * Copyright (C) @@COPYRIGHT@@
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package bookshelf.gui;

import java.awt.FontMetrics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class JTabbedPaneWithCloseIconsUI extends BasicTabbedPaneUI
{

    private Rectangle selIconRect;
    private int horizontalTextPosition = SwingUtilities.LEFT;

    public JTabbedPaneWithCloseIconsUI()
    {
    }

    public JTabbedPaneWithCloseIconsUI(int horTextPosition)
    {
        horizontalTextPosition = horTextPosition;
    }

    public Rectangle getSelectedIconRect()
    {
        return selIconRect;
    }

    protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon,
            Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected)
    {

        textRect.x = 0;
        textRect.y = 0;
        iconRect.x = 0;
        iconRect.y = 0;
        SwingUtilities.layoutCompoundLabel((JComponent) tabPane, metrics, title, icon, SwingUtilities.CENTER,
                SwingUtilities.CENTER, SwingUtilities.CENTER, horizontalTextPosition, tabRect, iconRect, textRect,
                textIconGap + 2);

        selIconRect = iconRect;
    }
}