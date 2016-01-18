# @@DESCRIPTION@@. 
# Copyright (C) @@COPYRIGHT@@
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

from javax.swing import *
from java.awt import FlowLayout, GridLayout, BorderLayout, GridBagLayout, GridBagConstraints, Color
from java.awt.event import ActionListener, FocusListener
from java.awt.image import BufferedImage
from javax.swing.event import ListSelectionListener
from java.io import FileInputStream, File
from javax.imageio import ImageIO
from java.lang import ClassLoader
from java.util import ResourceBundle, PropertyResourceBundle, Properties, MissingResourceException, Locale

class PropertyContainer:
    def __getattr__(self, name):
        if self.__class__.__dict__.has_key('__get_' + name):
            return self.__class__.__dict__['__get_' + name](self)
        
    def __setattr__(self, name, value):
        if self.__class__.__dict__.has_key('__set_' + name):
            self.__class__.__dict__['__set_' + name](self, value)
        
class PyComboBox(JComboBox):
    def __init__(self, items):
        JComboBox.__init__(self)
        self.items = items
        for key, value in items:
            self.addItem(value)
        
    def get_selection(self):
        if self.getSelectedIndex() != -1:
            (key, value) = self.items[self.getSelectedIndex()]
            return key
        else:
            return None

    def set_selection(self, selection):
        index = 0
        for key, value in self.items:
            if key == selection:
                self.setSelectedIndex(index)
                return
            index = index + 1
            
    def replace_items(self, items):
        selection = self.get_selection()
        self.items = items
        self.removeAllItems()
        for key, value in items:
            self.addItem(value)
        self.set_selection(selection)

class PyLanguageComboBox(PyComboBox):
    def __init__(self, language_list, current_locale=Locale.getDefault()):
        items = [] 
        for lang in language_list:
            locale_language = Locale(lang).getDisplayLanguage(current_locale)
            items.append((lang, locale_language.capitalize()))
        PyComboBox.__init__(self, items)
                                        
class PyLabel(JLabel):
    def __init__(self, text, image_file=None):
        JLabel.__init__(self, text)
        if image_file is not None:
            self.icon = ImageIcon(ImageIO.read(File(image_file)))

    def set_image(self, image):
        self.setIcon(ImageIcon(image))

class PyPanel(JPanel):
    def __init__(self, components, title=None, layout=None):
        if title is not None:
            self.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title))
        if layout is not None:
            self.setLayout(layout)
        for c in components:
            self.add(c)

class PyBorderPanel(JPanel):
    def __init__(self, title=None, north=None, south=None, east=None, west=None, center=None):
        self.setLayout(BorderLayout())
        if title is not None:
            self.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title))
        if north is not None:
            self.add(north, BorderLayout.NORTH)
        if south is not None:
            self.add(south, BorderLayout.SOUTH)
        if east is not None:
            self.add(east, BorderLayout.EAST)
        if west is not None:
            self.add(west, BorderLayout.WEST)
        if center is not None:
            self.add(center, BorderLayout.CENTER)

class PyGridBagPanel(JPanel):
    def __init__(self, component=None, title=None, **named):
        self.setLayout(GridBagLayout())
        if title is not None:
            self.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title))
        if component is not None:
            self._add(component, named)                

    def add(self, component, **named):
        self._add(component, named)
        
    def _add(self, component, named):        
        c = GridBagConstraints()
        for argument in named:
            if argument == 'anchor' or argument == 'fill':
                exec("c." + argument + " = " + "GridBagConstraints." + named[argument])
            else:
                exec("c." + argument + "=" + str(named[argument]))
        JPanel.add(self, component, c)

class PyMenuItem(JMenuItem):
    def __init__(self, text, key=None, enabled=None, action=None):
        JMenuItem.__init__(self, text)
        if key is not None:
            self.setAccelerator(KeyStroke.getKeyStroke(key))
        if enabled is not None:
            self.setEnabled(enabled)
        if action is not None:
            self.actionPerformed = action
            
class PyColorButton(JButton):
        def __init__(self, color, action=None):
            JButton.__init__(self)
            self._image = BufferedImage(70, 12, BufferedImage.TYPE_INT_ARGB)
            g = self._image.createGraphics()
            g.setColor(color)
            g.fillRect(0, 0, self._image.width, self._image.height)
            self._icon = ImageIcon(self._image)
            self.icon = self._icon
            if action is not None:
                self.actionPerformed = action
    
        def set_color(self, color):
            g = self._image.createGraphics()
            g.setColor(color)
            g.fillRect(0, 0, self._image.width, self._image.height)
            self._icon = ImageIcon(self._image)
            self.icon = self._icon
                        
class ActionListenerProxy(ActionListener):
    def __init__(self, dest):
        self.dest = dest

    def actionPerformed(self, event):
        self.dest(event)        
        
class ListSelectionListenerProxy(ListSelectionListener):
    def __init__(self, dest):
        self.dest = dest

    def valueChanged(self, event):
        self.dest(event)
        
class FocusListenerProxy(FocusListener):
    def __init__(self, gained=None, lost=None):
        self.gained = gained
        self.lost = lost

    def focusGained(self, event):
        if self.gained is not None: self.gained(event)
        
    def focusLost(self, event):
        if self.lost is not None: self.lost(event)        