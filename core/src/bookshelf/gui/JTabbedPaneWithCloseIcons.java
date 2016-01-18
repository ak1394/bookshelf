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

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class JTabbedPaneWithCloseIcons extends JTabbedPane
{
    private ActionListener actionListener;

    public JTabbedPaneWithCloseIcons()
    {
        this.setUI(new JTabbedPaneWithCloseIconsUI(SwingUtilities.LEFT));
        addCloseableIconListener();
    }

    public void setActionListener(ActionListener l)
    {
        this.actionListener = l;
    }

    private Rectangle getSelectedIconRect()
    {
        return ((JTabbedPaneWithCloseIconsUI) this.getUI()).getSelectedIconRect();
    }

    private void addCloseableIconListener()
    {
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e)
            {
                Rectangle r = getSelectedIconRect();
                if (r.contains(e.getPoint()))
                {
                    if (actionListener != null)
                    {
                        actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "close"));
                    }
                }
            }
        };

        this.addMouseListener(mouseListener);
    }

    public void addTab(String title, Component component)
    {
        addTab(title, new CloseTabIcon(null), component);
    }

    public void addTab(String title, Component component, String tip)
    {
        addTab(title, new CloseTabIcon(null), component, tip);
    }

}