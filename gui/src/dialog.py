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
from java.awt import BorderLayout, GridLayout, GridBagLayout, GridBagConstraints, Dimension, Color
from java.awt.event import ActionListener, KeyEvent
from javax.swing.event import ListSelectionListener
from bookshelf.gui import JTabbedPaneWithCloseIcons
from bookshelf.builder import MidletBuilder, ReaderPackage, PlatformPackage
from bookshelf.preview import *
from bookshelf.makefont import JFontChooser, FontFabric
from java.io import ObjectOutputStream, FileOutputStream
from java.util import Locale
from java.lang import String
import config as c
from  gui import *
import sys

_d = c.resource('dialogs')

def confirm(dialog, title, message):
    if JOptionPane.showConfirmDialog(dialog.contentPane, message,
            title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION: return 1
    return 0            

def confirm_dialog(dialog, title=_d['settings_save_title'], message=_d['settings_save_message']):
    return confirm(dialog, title, message)

def confirm_dialog_no_restart(dialog, title=_d['settings_save_title'], message=_d['settings_save_message_no_restart']):
    return confirm(dialog, title, message)
                
class About(JDialog):
    def __init__(self, parent):
        JDialog.__init__(self, parent)
        r = c.resource('about')
        b_panel=PyPanel([JButton("Ok", actionPerformed=self.action_ok)])
        t_panel=PyPanel([
            JLabel("@@PRODUCT@@ @@VERSION@@"), 
            JLabel("Copyright (c) @@COPYRIGHT@@"),
            JLabel("@@URL@@"),  
            ], layout=GridLayout(3, 1), title="")
        panel=PyBorderPanel(south=b_panel, center=t_panel)

        self.contentPane.setPreferredSize(Dimension(400, 100))
        self.title = r['title']
        self.contentPane.add(panel)

    def action_ok(self, event):
        self.dispose()

class Phone(JDialog):
    def __init__(self, parent):
        JDialog.__init__(self, parent)
        self.supported = c.supported()

        brand = c.config('PLATFORM_BRAND')
        model = c.config('PLATFORM_MODEL')
        platform = c.config('PLATFORM_JAR')

        self.allow_events = 1
        
        supported_brands = self.supported.keys()
        supported_brands.sort()
        self.brand_list = brand_list = JList(supported_brands, selectionMode=ListSelectionModel.SINGLE_SELECTION)
        brand_list.addListSelectionListener(ListSelectionListenerProxy(self.action_brand))
        brand_list_pane=JScrollPane(brand_list)
        brand_list_pane.setPreferredSize(Dimension(75, 150))
        brand_panel = PyPanel([brand_list_pane], title=_d['brand'])

        self.model_list_model = DefaultListModel()
        self.model_list = model_list = JList(self.model_list_model, selectionMode=ListSelectionModel.SINGLE_SELECTION)
        model_list.addListSelectionListener(ListSelectionListenerProxy(self.action_model))      
        model_list_pane=JScrollPane(model_list)
        model_list_pane.setPreferredSize(Dimension(75, 150))
        model_panel = PyPanel([model_list_pane], title=_d['model'])

        self.platform_list_model = DefaultListModel()
        self.platform_list = platform_list = JList(self.platform_list_model, selectionMode=ListSelectionModel.SINGLE_SELECTION)
        platform_list.addListSelectionListener(ListSelectionListenerProxy(self.action_platform))      
        platform_list_pane=JScrollPane(platform_list)
        platform_list_pane.setPreferredSize(Dimension(75, 150))
        platform_panel = PyPanel([platform_list_pane], title=_d['platform'])

        self.text = text = JTextArea();
        text.editable = 0
        text.enabled = 0
        text.lineWrap = 1
        text.wrapStyleWord = 1
        text.setPreferredSize(Dimension(200, 150))
        text_panel = PyPanel([text], title=_d['description'])

        all_panel = PyPanel([brand_panel, model_panel, platform_panel, text_panel], title="")

        button_panel=PyPanel([JButton(_d['save'], actionPerformed=self.action_ok), JButton(_d['discard'], actionPerformed=self.action_cancel)])
        panel=PyBorderPanel(south=button_panel, west=all_panel)
        self.title = _d['phone_model_title']
        self.contentPane.add(panel)
        
        brand_list.setSelectedValue(brand, 1)
        model_list.setSelectedValue(model, 1)
        platform_list.setSelectedValue(platform, 1)
        
        
    def action_ok(self, event):
        brand = self.brand_list.selectedValue
        model = self.model_list.selectedValue
        platform = self.platform_list.selectedValue
        if confirm_dialog_no_restart(self):
            c.set_default('PLATFORM_BRAND', brand)
            c.set_default('PLATFORM_MODEL', model)
            c.set_default('PLATFORM_JAR', platform)
            c.save()
            self.parent.reconfigure()
        self.dispose()            

    def action_cancel(self, event):
        self.dispose()

    def action_brand(self, event):
        self.allow_events = 0
        brand = self.brand_list.selectedValue
        self.model_list_model.clear()
        models = self.supported[brand].keys()
        models.sort()
        for model in models:
            self.model_list_model.addElement(model)
        self.model_list.selectedIndex = 0
        self.allow_events = 1
        self.action_model(None)

    def action_model(self, event):
        if self.allow_events:
            self.allow_events = 0
            brand = self.brand_list.selectedValue
            model = self.model_list.selectedValue
            self.platform_list_model.clear()
            for platform in self.supported[brand][model]:
                self.platform_list_model.addElement(platform)
            self.platform_list.selectedIndex = 0
            self.allow_events = 1
            self.action_platform(None)

    def action_platform(self, event):
        if self.allow_events:
            brand = self.brand_list.selectedValue
            model = self.model_list.selectedValue
            platform = self.platform_list.selectedValue
            (platform_jar, platform_description) = self.supported[brand][model][platform]
            self.text.setText(platform_description)

class InterfaceSettings(JDialog):
    def __init__(self, parent):
        JDialog.__init__(self, parent)
        self._parent = parent
        e = c.resource('entry')

        self.open_file_chooser = JFileChooser(c.config('DEFAULT_OPEN_DIRECTORY'))
        self.open_file_chooser.multiSelectionEnabled = 0
        self.open_file_chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        self.open_file_dir = c.config('DEFAULT_OPEN_DIRECTORY')
        self.open_file_label = JLabel(self.open_file_dir)
        
        self.save_file_chooser = JFileChooser(c.config('DEFAULT_SAVE_DIRECTORY'))
        self.save_file_chooser.multiSelectionEnabled = 0
        self.save_file_chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        self.save_file_dir = c.config('DEFAULT_SAVE_DIRECTORY')
        self.save_file_label = JLabel(self.save_file_dir)

        self.language = PyLanguageComboBox(c.config('SUPPORTED_LOCALES'), Locale.getDefault())
        self.language.set_selection(Locale.getDefault().language)

        if c.config('MIDLET_LOCALE') is not None and c.config('MIDLET_LOCALE') != '':
            midlet_locale = Locale(c.config('MIDLET_LOCALE'))
        else:
            midlet_locale = Locale.getDefault()
        self.midlet_language = PyLanguageComboBox(c.config('SUPPORTED_LOCALES'))
        self.midlet_language.set_selection(midlet_locale.language)

        main = PyGridBagPanel(title="")
        main.setPreferredSize(Dimension(400, 200))

        lang_panel = PyBorderPanel(west=JLabel(e['language']), east=self.language, title="")
        main.add(lang_panel, gridx=0, gridy=0, fill="HORIZONTAL", anchor="LINE_START", weightx=0.5, weighty=0.5)

        midlet_lang_panel = PyBorderPanel(west=JLabel(_d['midlet_language']), east=self.midlet_language, title="")
        main.add(midlet_lang_panel, gridx=0, gridy=1, fill="HORIZONTAL", anchor="LINE_START", weightx=0.5, weighty=0.5)

        open_panel = PyBorderPanel(west=JLabel(_d['open_dir']), east=JButton(_d['select'], actionPerformed=self.action_set_open), south=self.open_file_label, title="")
        main.add(open_panel, gridx=0, gridy=2, fill="HORIZONTAL", anchor="LINE_START", weightx=0.5, weighty=0.5)

        save_panel = PyBorderPanel(west=JLabel(_d['save_dir']), east=JButton(_d['select'], actionPerformed=self.action_set_save), south=self.save_file_label, title="")
        main.add(save_panel, gridx=0, gridy=3, fill="HORIZONTAL", anchor="LINE_START", weightx=0.5, weighty=0.5)

        self.make_folder = JCheckBox()
        makefolder_panel = PyBorderPanel(west=JLabel(_d['make_dir']), east=self.make_folder, title="")
        main.add(makefolder_panel, gridx=0, gridy=4, fill="HORIZONTAL", anchor="LINE_START", weightx=0.5, weighty=0.5)
        self.make_folder.selected = c.config('MAKE_FOLDER')
        
        button_panel=PyPanel([JButton(_d['save'], actionPerformed=self.action_ok), JButton(_d['discard'], actionPerformed=self.action_cancel)])
        panel=PyBorderPanel(south=button_panel, west=main)

        self.title = _d['interface_title']
        self.contentPane.add(panel)

    def action_ok(self, event):
        make_folder = self.make_folder.selected
        if confirm_dialog(self):
            c.set_default('MAKE_FOLDER', make_folder)
            c.set_default('DEFAULT_OPEN_DIRECTORY', self.open_file_dir)
            c.set_default('DEFAULT_SAVE_DIRECTORY', self.save_file_dir)
            c.set_default('LOCALE', self.language.get_selection())
            c.set_default('MIDLET_LOCALE', self.midlet_language.get_selection())
            c.save()
        self.dispose()            
        
    def action_cancel(self, event):
        self.dispose()

    def action_set_open(self, event):
        if not self.open_file_chooser.showOpenDialog(self._parent) == JFileChooser.APPROVE_OPTION: return
        self.open_file_dir = self.open_file_chooser.getSelectedFile().toString()
        self.open_file_label.text = self.open_file_dir 

    def action_set_save(self, event):
        if not self.save_file_chooser.showOpenDialog(self._parent) == JFileChooser.APPROVE_OPTION: return
        self.save_file_dir = self.save_file_chooser.getSelectedFile().toString()
        self.save_file_label.text = self.save_file_dir 
        
class TextInputSettings(JDialog):
    def __init__(self, parent):
        JDialog.__init__(self, parent)
        e = c.resource('entry')

        self.language = PyLanguageComboBox(c.config('SUPPORTED_LOCALES'), Locale.getDefault())
        self.language.set_selection(c.config('DEFAULT_TEXT_LANGUAGE'))
        
        self.encoding = PyComboBox(c.SUPPORTED_ENCODINGS)

        self.para_start = PyComboBox([(x, x) for x in range(11)])
        self.para_start.set_selection(c.config('DEFAULT_PARAGRAPH_START'))

        main_panel = PyGridBagPanel(title='')
        main_panel.add(JLabel(e['language']), gridx=0, gridy=0, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.language, gridx=1, gridy=0, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
        main_panel.add(JLabel(e['encoding']), gridx=0, gridy=1, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.encoding, gridx=1, gridy=1, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
        main_panel.add(JLabel(e['source_para_start']), gridx=0, gridy=2, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.para_start, gridx=1, gridy=2, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
        main_panel.setPreferredSize(Dimension(300, 70))

        button_panel=PyPanel([JButton(_d['save'], actionPerformed=self.action_ok), JButton(_d['discard'], actionPerformed=self.action_cancel)])
        
        panel=PyGridBagPanel(title='')
        panel.add(main_panel)
        panel.add(button_panel, gridy=1)        

        self.title = _d['text_input_title']
        self.contentPane.add(panel)

    def action_ok(self, event):
        language = self.language.get_selection()
        encoding = self.encoding.get_selection()
        para_start = self.para_start.get_selection()
        if confirm_dialog_no_restart(self):
            c.set_default('DEFAULT_TEXT_LANGUAGE', language)
            c.set_default('DEFAULT_TEXT_ENCODING', encoding)
            c.set_default('DEFAULT_PARAGRAPH_START', para_start)
            c.save()
        self.dispose()            

    def action_cancel(self, event):
        self.dispose()

class OutputSettings(JDialog):
    def __init__(self, parent, font_fabric):
        JDialog.__init__(self, parent)
        self.e = e = c.resource('entry')

        self.font_name = PyComboBox([(n, n) for n in font_fabric.list()])
        self.font_name.set_selection(c.config('DEFAULT_FONT_NAME'))
        
        self.interline = PyComboBox([(x, x) for x in range(c.MAX_INTERLINE_SPACING)])
        self.interline.set_selection(c.config('DEFAULT_INTERLINE_SPACING'))
        
        self.para_indent = PyComboBox([(x, x) for x in range(c.MAX_PARA_INDENT)])
        self.para_indent.set_selection(c.config('DEFAULT_PARAGRAPH_INDENT'))
        
        self.hyphenate = JCheckBox()
        self.hyphenate.selected = c.config('DEFAULT_HYPHENATE_TEXT')
        
        self.justify = JCheckBox()
        self.justify.selected = c.config('DEFAULT_JUSTIFY_TEXT')

        self.screen_border = JCheckBox()
        self.screen_border.selected = c.config('DEFAULT_SCREEN_BORDER')
        
        self.color_foreground = Color(c.config('DEFAULT_COLOR'))
        self.color_background = Color(c.config('DEFAULT_BACKGROUND_COLOR'))
        
        self.button_color_foreground = PyColorButton(self.color_foreground, action=self.action_set_foreground)
        self.button_color_background = PyColorButton(self.color_background, action=self.action_set_background)

        main_panel = PyGridBagPanel(title='')
        
        main_panel.add(JLabel(e['font']), gridx=0, gridy=0, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.font_name, gridx=1, gridy=0, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')

        main_panel.add(JLabel(e['interline_spacing']), gridx=0, gridy=1, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.interline, gridx=1, gridy=1, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
 
        main_panel.add(JLabel(e['result_para_start']), gridx=0, gridy=2, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.para_indent, gridx=1, gridy=2, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')

        main_panel.add(JLabel(e['justify']), gridx=0, gridy=3, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.justify, gridx=1, gridy=3, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
                
        main_panel.add(JLabel(e['hyphenate']), gridx=0, gridy=4, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.hyphenate, gridx=1, gridy=4, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')

        main_panel.add(JLabel(e['screen_border']), gridx=0, gridy=5, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.screen_border, gridx=1, gridy=5, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')

        main_panel.add(JLabel(e['color_foreground']), gridx=0, gridy=6, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.button_color_foreground, gridx=1, gridy=6, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')

        main_panel.add(JLabel(e['color_background']), gridx=0, gridy=7, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.button_color_background, gridx=1, gridy=7, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
                
        main_panel.setPreferredSize(Dimension(300, 170))

        button_panel=PyPanel([JButton(_d['save'], actionPerformed=self.action_ok), JButton(_d['discard'], actionPerformed=self.action_cancel)])

        panel=PyGridBagPanel(title='')
        panel.add(main_panel)
        panel.add(button_panel, gridy=1)        

        self.title = _d['output_title']
        self.contentPane.add(panel)

    def action_ok(self, event):
        font = self.font_name.get_selection()
        interline = self.interline.get_selection()
        para_indent = self.para_indent.get_selection()
        hyphenate = self.hyphenate.selected
        justify = self.justify.selected
        screen_border = self.screen_border.selected
        color_foreground = self.color_foreground.RGB
        color_background = self.color_background.RGB
        
        if confirm_dialog_no_restart(self):
            c.set_default('DEFAULT_FONT_NAME', font)
            c.set_default('DEFAULT_INTERLINE_SPACING', interline)
            c.set_default('DEFAULT_PARAGRAPH_INDENT', para_indent)
            c.set_default('DEFAULT_HYPHENATE_TEXT', hyphenate)
            c.set_default('DEFAULT_JUSTIFY_TEXT', justify)
            c.set_default('DEFAULT_SCREEN_BORDER', screen_border)
            c.set_default('DEFAULT_COLOR', color_foreground)
            c.set_default('DEFAULT_BACKGROUND_COLOR', color_background)
            c.save()
        self.dispose()            

    def action_cancel(self, event):
        self.dispose()

    def action_set_foreground(self, event):
        color = JColorChooser.showDialog(self.parent, self.e['select_foreground_color'], self.color_foreground)
        if color is not None:
            self.button_color_foreground.set_color(color)
            self.color_foreground = color
                    
    def action_set_background(self, event):
        color = JColorChooser.showDialog(self.parent, self.e['select_background_color'], self.color_background)
        if color is not None:
            self.button_color_background.set_color(color)
            self.color_background = color

class PluginSettings(JDialog):
    def __init__(self, parent):
        JDialog.__init__(self, parent)

        self.available_list_model = DefaultListModel()
        self.selected_list_model = DefaultListModel()
        self.plugins = c.plugins()
        (available, selected) = self.get_plugins()
        for plugin in available: self.available_list_model.addElement(plugin)
        for plugin in selected: self.selected_list_model.addElement(plugin)

        self.available_list = available_list = JList(self.available_list_model, selectionMode=ListSelectionModel.SINGLE_SELECTION)
        available_list.addFocusListener(FocusListenerProxy(gained=self.action_available))
        available_list.addListSelectionListener(ListSelectionListenerProxy(self.action_available))
        available_list_pane=JScrollPane(available_list)
        available_panel = PyPanel([available_list_pane], title=_d['plugin_available'], layout=GridLayout(1,0))
        available_panel.setPreferredSize(Dimension(75, 150))

        self.selected_list = selected_list = JList(self.selected_list_model, selectionMode=ListSelectionModel.SINGLE_SELECTION)
        selected_list.addListSelectionListener(ListSelectionListenerProxy(self.action_selected))
        selected_list.addFocusListener(FocusListenerProxy(gained=self.action_selected))
        selected_list_pane=JScrollPane(selected_list)
        selected_panel = PyPanel([selected_list_pane], title=_d['plugin_selected'], layout=GridLayout(1,0))
        selected_panel.setPreferredSize(Dimension(75, 150))

        self.text = text = JTextArea()
        text.editable = 0
        text.enabled = 0
        text.lineWrap = 1
        text.wrapStyleWord = 1
        text_pane=JScrollPane(text)
        text_pane.setPreferredSize(Dimension(200, 100))

        self.status_line = status_line = JLabel()

        text_panel = PyGridBagPanel(title=_d['plugin_description'])
        text_panel.add(text_pane, fill='BOTH', weightx=0.5, weighty=0.5)
        text_panel.add(status_line, gridy=1, anchor='LAST_LINE_START')

        self.add_button = add_button = JButton(_d['plugin_add'], actionPerformed=self.action_add)
        self.remove_button = remove_button = JButton(_d['plugin_remove'], actionPerformed=self.action_remove)

        list_panel = PyGridBagPanel(title='')
        list_panel.add(available_panel, gridx=0, fill='BOTH', weightx=0.5, weighty=0.5)
        list_panel.add(text_panel, gridx=1, gridwidth=2, fill='BOTH', weightx=0.5, weighty=0.5)
        list_panel.add(selected_panel, gridx=3, fill='BOTH', weightx=0.5, weighty=0.5)
        list_panel.add(PyPanel([add_button, remove_button], layout=GridLayout(1,2)), gridy=1, gridwidth=4, fill='HORIZONTAL')

        button_panel=PyPanel([JButton(_d['save'], actionPerformed=self.action_ok), JButton(_d['discard'], actionPerformed=self.action_cancel)])

        panel = PyGridBagPanel()
        panel.add(list_panel, fill='BOTH', weightx=0.5, weighty=0.5)
        panel.add(button_panel, gridy=1)
        panel.setPreferredSize(Dimension(500, 250))

        self.title = _d['plugin_title']
        self.contentPane.add(panel)
        
        if not self.available_list_model.empty:
            self.available_list.selectedIndex = 0
            self.available_list.grabFocus()
        else:
            self.selected_list.selectedIndex = 0
            self.selected_list.grabFocus()
        
    def action_ok(self, event):
        if confirm_dialog(self):
            selected = []
            for element in self.selected_list_model.elements():
                selected.append(element)
            c.set_default('PLUGIN_LIST', selected)
            c.save()
        self.dispose()            

    def action_cancel(self, event):
        self.dispose()
        
    def action_available(self, event):
        if not self.available_list.selectionEmpty:
            self.update_plugin_info(self.available_list.selectedValue)
            self.add_button.enabled = 1
            self.remove_button.enabled = 0
            self.selected_list.clearSelection()
        
    def action_selected(self, event):
        if not self.selected_list.selectionEmpty:
            self.update_plugin_info(self.selected_list.selectedValue)
            self.remove_button.enabled = 1
            self.add_button.enabled = 0
            self.available_list.clearSelection()
        
    def action_add(self, event):
        if not self.available_list.selectionEmpty:
            plugin = self.available_list.selectedValue
            self.available_list_model.removeElement(plugin)
            self.available_list.clearSelection()
            self.selected_list_model.addElement(plugin)
            self.remove_button.enabled = 1
            self.add_button.enabled = 0
            if not self.available_list_model.empty:
                self.available_list.selectedIndex = 0
                self.available_list.grabFocus()
            else:
                self.selected_list.selectedIndex = 0
                self.selected_list.grabFocus()
            
                
    def action_remove(self, event):
        if not self.selected_list.selectionEmpty:
            plugin = self.selected_list.selectedValue
            self.selected_list_model.removeElement(plugin)
            self.selected_list.clearSelection()
            self.available_list_model.addElement(plugin)
            self.add_button.enabled = 1
            self.remove_button.enabled = 0
            if not self.selected_list_model.empty:
                self.selected_list.selectedIndex = 0
                self.selected_list.grabFocus()
            else:
                self.available_list.selectedIndex = 0
                self.available_list.grabFocus()
                
    def get_plugins(self):
        available = self.plugins.keys()
        selected = c.config('PLUGIN_LIST')
        for plugin in selected:
            if plugin in available:
                available.remove(plugin)
        return (available, selected)
        
    def update_plugin_info(self, plugin):
        (plugin_description, plugin_size) = self.plugins[plugin]
        total_size = 0
        for plugin in self.selected_list_model.elements():
            (description, size) = self.plugins[plugin]
            total_size = total_size + size
        self.text.text = plugin_description
        self.status_line.text ="%s %d %s, %s %d %s" % (_d['plugin_size'], plugin_size, _d['bytes'], _d['total'], total_size, _d['bytes'])

class SystemFontImport(JDialog):
    def __init__(self, parent):
        JDialog.__init__(self, parent)

        e = c.resource('entry')
        self.encoding = PyComboBox(c.SUPPORTED_ENCODINGS)
        self.select_button = JButton(_d['select'], actionPerformed=self.action_select)
        self.font_label = JLabel(_d['select_font'] + ' : ' + _d['no_font_selected'])
        self.font = None
        self.font_name = JTextField(12)        
        self.chooser = JFontChooser(parent)
        self.chooser.locationRelativeTo = self

        self.rb_normal = JRadioButton()
        self.rb_antialiased = JRadioButton()
        self.rb_subpixel = JRadioButton()
        self.rb_subpixel_aa = JRadioButton()
        rb_group = ButtonGroup()
        rb_group.add(self.rb_normal)
        rb_group.add(self.rb_antialiased)
        rb_group.add(self.rb_subpixel)
        rb_group.add(self.rb_subpixel_aa)
        self.rb_normal.selected = 1

        main_panel = PyGridBagPanel(title='')
        main_panel.add(self.font_label, gridx=0, gridy=0, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.select_button, gridx=1, gridy=0, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
        main_panel.add(JLabel(_d['normal']), gridx=0, gridy=1, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.rb_normal, gridx=1, gridy=1, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
        main_panel.add(JLabel(_d['antialiased']), gridx=0, gridy=2, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.rb_antialiased, gridx=1, gridy=2, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
        main_panel.add(JLabel(_d['subpixel']), gridx=0, gridy=3, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.rb_subpixel, gridx=1, gridy=3, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
        main_panel.add(JLabel(_d['subpixel_aa']), gridx=0, gridy=4, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.rb_subpixel_aa, gridx=1, gridy=4, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
        main_panel.add(JLabel(e['encoding']), gridx=0, gridy=5, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.encoding, gridx=1, gridy=5, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
        main_panel.add(JLabel(_d['font_name']), gridx=0, gridy=6, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.font_name, gridx=1, gridy=6, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')

        main_panel.setPreferredSize(Dimension(300, 150))
        self.button_import = JButton(_d['import'], actionPerformed=self.action_ok, enabled=0)
        button_panel=PyPanel([self.button_import, JButton(_d['discard'], actionPerformed=self.action_cancel)])
        
        panel=PyGridBagPanel(title='')
        panel.add(main_panel)
        panel.add(button_panel, gridy=1)        

        self.title = _d['font_import_title']
        self.contentPane.add(panel)

    def action_ok(self, event):
        fabric = FontFabric()
        if self.rb_normal.selected:
            font = fabric.loadNormal(self.font, self.encoding.get_selection())
        elif self.rb_antialiased.selected:
            font = fabric.loadAntialiased(self.font, self.encoding.get_selection())
        elif self.rb_subpixel.selected:
            font = fabric.loadSubpixel(self.font, self.encoding.get_selection())
        elif self.rb_subpixel_aa.selected:
            font = fabric.loadSubpixelAntialiased(self.font, self.encoding.get_selection())

        font_name = String(self.font_name.text).replaceAll(' ', '')
        font.name = font_name
        font_file = c.config('FONT_DIRECTORY') + '/' + font_name
        o = ObjectOutputStream(FileOutputStream(font_file))
        o.writeObject(font)
        o.close()
        self.dispose()
        self.parent.refresh_fonts()

    def action_select(self, event):
        if self.font is not None:
            result = self.chooser.showDialog(self.font)
        else:
            result = self.chooser.showDialog()
        if result == JFontChooser.OK_OPTION:
            self.font = self.chooser.font
            self.font_label.text = _d['select_font'] + ' : ' + self.font.name
            self.font_name.text = self.font.name + '-' + str(self.font.size)
            self.button_import.enabled = 1

    def action_cancel(self, event):
        self.dispose()
        
class DeleteFont(JDialog):
    def __init__(self, parent):
        JDialog.__init__(self, parent)
        e = c.resource('entry')
 
        self.font_list = font_list = JList(parent.bookshelf.font_fabric.list(), selectionMode=ListSelectionModel.SINGLE_SELECTION)
        font_list_pane=JScrollPane(font_list)
        font_list_pane.setPreferredSize(Dimension(150, 150))
        font_panel = PyPanel([font_list_pane], title=e['font'])

        button_panel=PyPanel([JButton(_d['delete'], actionPerformed=self.action_ok), JButton(_d['cancel'], actionPerformed=self.action_cancel)])
        
        panel=PyGridBagPanel(title='')
        panel.add(font_panel)
        panel.add(button_panel, gridy=1)        

        self.title = _d['font_delete_title']
        self.contentPane.add(panel)

    def action_ok(self, event):
        if confirm_dialog(self, _d['font_delete_title'], _d['font_delete_confirmation']):
            font = self.font_list.selectedValue
            self.parent.bookshelf.font_fabric.delete(font)
            self.parent.refresh_fonts()
            self.dispose()            
        
    def action_cancel(self, event):
        self.dispose()
        
class PalmFontImport(JDialog):
    def __init__(self, parent):
        JDialog.__init__(self, parent)

        e = c.resource('entry')
        self.encoding = PyComboBox(c.SUPPORTED_ENCODINGS)
        self.select_button = JButton(_d['select'], actionPerformed=self.action_select)
        self.font_label = JLabel(_d['select_font'] + ' : ' + _d['no_font_selected'])
        self.font = None
        self.font_name = JTextField(12)
        self.file_chooser = JFileChooser(c.config('DEFAULT_OPEN_DIRECTORY'))
        #self.file_chooser.locationRelativeTo = self

        main_panel = PyGridBagPanel(title='')
        main_panel.add(self.font_label, gridx=0, gridy=0, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.select_button, gridx=1, gridy=0, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
        main_panel.add(JLabel(e['encoding']), gridx=0, gridy=1, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.encoding, gridx=1, gridy=1, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')
        main_panel.add(JLabel(_d['font_name']), gridx=0, gridy=2, anchor='LINE_START', weightx=0.5)
        main_panel.add(self.font_name, gridx=1, gridy=2, anchor='LINE_END', weightx=0.1, fill='HORIZONTAL')

        main_panel.setPreferredSize(Dimension(300, 100))

        button_panel=PyPanel([JButton(_d['import'], actionPerformed=self.action_ok), JButton(_d['discard'], actionPerformed=self.action_cancel)])
        
        panel=PyGridBagPanel(title='')
        panel.add(main_panel)
        panel.add(button_panel, gridy=1)        

        self.title = _d['font_import_title']
        self.contentPane.add(panel)

    def action_ok(self, event):
        fabric = FontFabric()
        if self.font_file.name[-3:].lower() == 'pdb':
            font = fabric.loadPdb(self.font_file, self.encoding.get_selection())
        if self.font_file.name[-3:].lower() == 'pft':
            font = fabric.loadPft(self.font_file, self.encoding.get_selection())

        font_name = String(self.font_name.text).replaceAll(' ', '')
        font.name = font_name
        font_file = c.config('FONT_DIRECTORY') + '/' + font_name
        o = ObjectOutputStream(FileOutputStream(font_file))
        o.writeObject(font)
        o.close()
        self.dispose()
        self.parent.refresh_fonts()

    def action_select(self, event):
        if not self.file_chooser.showOpenDialog(self) == JFileChooser.APPROVE_OPTION: return
        self.font_file = self.file_chooser.selectedFile
        self.font_label.text = _d['select_font'] + ' : ' + self.font_file.name
        self.font_name.text = self.font_file.name[:-4]

    def action_cancel(self, event):
        self.dispose()
        
