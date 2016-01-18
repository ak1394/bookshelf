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
from java.awt import BorderLayout, GridBagLayout, GridBagConstraints, Dimension, Color
from bookshelf.preview import Preview
from bookshelf.gui import JTabbedPaneWithCloseIcons
from bookshelf.builder import MidletBuilder, ReaderPackage, PlatformPackage
from bookshelf.preview import *
import config as c
import dialog
from  gui import *
import sys

class BookshelfView(JFrame):
    def __init__(self, bookshelf):
        JFrame.__init__(self, windowClosing=lambda event: sys.exit(0))
        self.size = (500, 420)
        self.bookshelf = bookshelf
        self.max_entries = 32
        self.formatters = {'.txt':BookView, '.fo':FopView}
        # resources
        self.r = r = c.resource('window')
        # title
        self.set_title()
        
        # menu File
        self.menu_open = PyMenuItem(r['item_open'], action=self.action_open, key='ctrl O')
        self.menu_save = PyMenuItem(r['item_save'], action=self.action_save, key='ctrl S', enabled=0)
        self.menu_close = PyMenuItem(r['item_close'], action=self.action_close, enabled=0)
        self.menu_exit = PyMenuItem(r['item_exit'], action=self.action_exit)
        menu_file = JMenu(r['menu_file'])
        for m in [self.menu_open, self.menu_save, self.menu_close, self.menu_exit]:
            menu_file.add(m)
        # submenu Rotation
        group_rotation = ButtonGroup()
        menu_rotation = JMenu(r['menu_rotation'])
        self.menu_item_rotation_0 = JCheckBoxMenuItem(r['item_rotation_0'], actionPerformed=self.action_rotation_0, selected=(c.config('SCREEN_ROTATION') == 0))
        for menu_item in [
                self.menu_item_rotation_0,
                JCheckBoxMenuItem(r['item_rotation_90'], actionPerformed=self.action_rotation_90, selected=(c.config('SCREEN_ROTATION') == 90)),
                JCheckBoxMenuItem(r['item_rotation_270'], actionPerformed=self.action_rotation_270, selected=(c.config('SCREEN_ROTATION') == 270))]:
            menu_rotation.add(menu_item)
            group_rotation.add(menu_item)
        
        # menu Settings
        menu_settings = JMenu(r['menu_settings'])
        for menu_item in [
                PyMenuItem(r['item_phone'], action=self.action_select_phone),
                PyMenuItem(r['item_interface'], action=self.action_interface_settings),
                PyMenuItem(r['item_text_input'], action=self.action_text_input_settings),
                PyMenuItem(r['item_output'], action=self.action_output_settings),
                menu_rotation,
                PyMenuItem(r['item_plugin'], action=self.action_plugin_settings)]:
            menu_settings.add(menu_item)
        # menu Fonts
        menu_fonts = JMenu(r['menu_fonts'])
        for menu_item in [
                PyMenuItem(r['item_import_system'], action=self.action_import_system_font),
                PyMenuItem(r['item_import_palm'], action=self.action_import_palm_font),
                PyMenuItem(r['item_delete'], action=self.action_delete_font),
                ]:
            menu_fonts.add(menu_item)
        # menu Help
        menu_help = JMenu(r['menu_help'])
        for menu_item in [
                PyMenuItem(r['item_about'], action=self.action_about)]:
            menu_help.add(menu_item)
        # menu
        menu = JMenuBar()
        menu.add(menu_file)
        menu.add(menu_settings)
        menu.add(menu_fonts)
        menu.add(Box.createHorizontalGlue())
        menu.add(menu_help)
        self.JMenuBar = menu

        # entry pane
        self.tabbed_pane = JTabbedPaneWithCloseIcons()
        self.tabbed_pane.actionListener = ActionListenerProxy(self.action_close)
        # file chooser
        self.open_file_chooser = JFileChooser(c.config('DEFAULT_OPEN_DIRECTORY'))
        self.open_file_chooser.multiSelectionEnabled = 1
        self.save_file_chooser = JFileChooser(c.config('DEFAULT_SAVE_DIRECTORY'))
        self.save_file_chooser.multiSelectionEnabled = 0

    def set_title(self):
        self.title =  self.r['window_title'] + ' - ' + c.config('PLATFORM_BRAND') + ' ' + c.config('PLATFORM_MODEL')

    def get_formatter(self, name):
        for key in self.formatters:
            if name[-len(key):].lower() == key:
                return self.formatters[key]
        # if nothing found return default formatter
        return self.formatters['.txt']
    
    def action_open(self, event):
        if not self.open_file_chooser.showOpenDialog(self) == JFileChooser.APPROVE_OPTION: return    
        # make sure that all files exist
        for f in self.open_file_chooser.getSelectedFiles():
            if not f.canRead():
                JOptionPane.showMessageDialog(self.contentPane, self.r['unable_to_open_message'] + 
                    '\n' + f.name, self.r['unable_to_open_title'], JOptionPane.WARNING_MESSAGE)
                return 
        # add views to tabbed pane
        for f in self.open_file_chooser.getSelectedFiles():
            formatterClass = self.get_formatter(f.getName())
            view = formatterClass(self.bookshelf, f)
            self.tabbed_pane.addTab(f.getName(), view, f.getAbsolutePath())
            self.tabbed_pane.setSelectedComponent(view)
        # update menu
        if self.tabbed_pane.getTabCount() > 0:
            self.menu_save.enabled = 1
            self.menu_close.enabled = 1
        if self.tabbed_pane.getTabCount() >= self.max_entries:
            self.menu_open.enabled = 0
        # add tabbed pane to window
        self.contentPane.add(self.tabbed_pane, BorderLayout.NORTH)
        self.contentPane.revalidate()

    def action_save(self, event):
        for i in range(self.tabbed_pane.getTabCount()):
            # call validate on each view
            error = self.tabbed_pane.getComponentAt(i).validate() 
            if error is not None:
                (message, title) = error
                self.tabbed_pane.setSelectedIndex(i)
                JOptionPane.showMessageDialog(None, message, title, JOptionPane.ERROR_MESSAGE)
                return
        if self.save_file_chooser.showSaveDialog(self) == JFileChooser.APPROVE_OPTION:
            for i in range(self.tabbed_pane.tabCount):
                bookView = self.tabbed_pane.getComponentAt(i)    
                if bookView.preview is None:
                    bookView.action_preview(None)
            self.bookshelf.save(self.save_file_chooser.selectedFile)

    def action_close(self, event):
        if JOptionPane.showConfirmDialog(self.contentPane, self.r['close_confirm_message'],
            self.r['close_confirm_title'], JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION:
            entry_view = self.tabbed_pane.getSelectedComponent()
            entry_view.action_close(event)
            self.tabbed_pane.remove(entry_view)
            self.bookshelf.del_book(entry_view.book)
            # check if no entries left
            if self.tabbed_pane.getTabCount() == 0:
                self.menu_save.enabled = 0
                self.menu_close.enabled = 0
                self.contentPane.remove(self.tabbed_pane)
            if self.tabbed_pane.getTabCount() < self.max_entries:
                self.menu_open.enabled = 1
            self.contentPane.repaint()

    def action_exit(self, event):
        sys.exit()

    def action_about(self, event):
        self.show_modal_dialog(dialog.About(self))

    def action_select_phone(self, event):
        self.show_modal_dialog(dialog.Phone(self))

    def action_interface_settings(self, event):
        self.show_modal_dialog(dialog.InterfaceSettings(self))

    def action_text_input_settings(self, event):
        self.show_modal_dialog(dialog.TextInputSettings(self))

    def action_output_settings(self, event):
        self.show_modal_dialog(dialog.OutputSettings(self, self.bookshelf.font_fabric))

    def action_plugin_settings(self, event):
        self.show_modal_dialog(dialog.PluginSettings(self))

    def action_import_system_font(self, event):
        self.show_modal_dialog(dialog.SystemFontImport(self))

    def action_import_palm_font(self, event):
        self.show_modal_dialog(dialog.PalmFontImport(self))

    def action_delete_font(self, event):
        self.show_modal_dialog(dialog.DeleteFont(self))

    def action_rotation_0(self, event):
        self.set_rotation(0)
        
    def action_rotation_90(self, event):
        self.set_rotation(90)

    def action_rotation_270(self, event):
        self.set_rotation(270)
        
    def set_rotation(self, rotation):
        platform = self.bookshelf.platform
        if platform.hasProperty('DISABLE_ROTATION') and platform.getProperty('DISABLE_ROTATION') == 'true':
            self.menu_item_rotation_0.selected = 1
        else:
            c.set_default('SCREEN_ROTATION', rotation)
            c.save()
            self.reconfigure()
        
    def show_modal_dialog(self, dialog):
        dialog.setLocation(self.getLocation().x + 50, self.getLocation().y + 50)
        dialog.modal = 1
        dialog.pack()
        dialog.show()

    def refresh_fonts(self):
        for i in range(self.tabbed_pane.tabCount):
            bookView = self.tabbed_pane.getComponentAt(i)
            bookView.refresh_fonts()

    def reconfigure(self):
        self.bookshelf.configure()
        if self.bookshelf.platform.hasProperty('DISABLE_ROTATION') and self.bookshelf.platform.getProperty('DISABLE_ROTATION') == 'true':
            c.set_default('SCREEN_ROTATION', 0)
            c.save()
            self.bookshelf.configure()
        self.set_title()
        for i in range(self.tabbed_pane.tabCount):
            bookView = self.tabbed_pane.getComponentAt(i)    
            # re-render every loaded book
            bookView.action_preview(None)

class BookView(JPanel):
    def __init__(self, bookshelf, text_file):
        self.bookshelf = bookshelf
        self.book = bookshelf.add_book(text_file)
        self.preview = None
        self.r = r = c.resource('entry')
        self.layout = BorderLayout()
        # source
        self.renderer = None
        self.language = PyLanguageComboBox(c.config('SUPPORTED_LOCALES'))
        self.encoding = PyComboBox(c.SUPPORTED_ENCODINGS)
        self.para_start = PyComboBox([(x, x) for x in range(11)])
        # result
        self.title = JTextField(text_file.getName().split('.')[0], 12)
        self.font_name = PyComboBox([(n, n) for n in bookshelf.font_fabric.list()])
        self.interline = PyComboBox([(x, x) for x in range(-c.MAX_INTERLINE_SPACING, c.MAX_INTERLINE_SPACING)])
        self.para_indent = PyComboBox([(x, x) for x in range(c.MAX_PARA_INDENT)])
        self.hyphenate = JCheckBox()
        self.justify = JCheckBox()
        self.screen_border = JCheckBox()

        # defaults
        self.color_foreground = Color(c.config('DEFAULT_COLOR'))
        self.color_background = Color(c.config('DEFAULT_BACKGROUND_COLOR'))

        self.button_color_foreground = PyColorButton(self.color_foreground, action=self.action_set_foreground)
        self.button_color_background = PyColorButton(self.color_background, action=self.action_set_background)

        if c.config('DEFAULT_TEXT_LANGUAGE') is not None:
            default_text_language = c.config('DEFAULT_TEXT_LANGUAGE')
        else:
            default_text_language = Locale.getDefault().language
            
        self.encoding.set_selection(c.config('DEFAULT_TEXT_ENCODING'))
        self.para_start.set_selection(c.config('DEFAULT_PARAGRAPH_START'))
        self.language.set_selection(default_text_language)
        self.para_indent.set_selection(c.config('DEFAULT_PARAGRAPH_INDENT'))
        self.font_name.set_selection(c.config('DEFAULT_FONT_NAME'))
        self.hyphenate.selected = c.config('DEFAULT_HYPHENATE_TEXT')
        self.justify.selected = c.config('DEFAULT_JUSTIFY_TEXT')
        self.screen_border.selected = c.config('DEFAULT_SCREEN_BORDER')
        self.interline.set_selection(c.config('DEFAULT_INTERLINE_SPACING'))
        
        self.ilabel = PyLabel(None)
        self.ilabel.setPreferredSize(bookshelf.platform.canvasDimension)
        
        buttons = PyPanel([JButton(r['preview'], actionPerformed=self.action_preview)])
        
        result_panel = PyPanel([
                PyLabel(r['name']), self.title,
                PyLabel(r['font']), self.font_name,
                PyLabel(r['interline_spacing']), self.interline,
                PyLabel(r['result_para_start']), self.para_indent,
                PyLabel(r['justify']), self.justify,
                PyLabel(r['hyphenate']), self.hyphenate,
                PyLabel(r['screen_border']), self.screen_border,
                PyLabel(r['color_foreground']), self.button_color_foreground,
                PyLabel(r['color_background']), self.button_color_background
                ], layout=GridLayout(9,2), title=r['result_title'])
        
        source_panel = PyPanel([
                PyLabel(r['language']), self.language,
                PyLabel(r['encoding']), self.encoding,
                PyLabel(r['source_para_start']), self.para_start
                ], layout=GridLayout(3,2), title=r['source_title'])
        
        self.preview_button_up = JButton(r['up'], actionPerformed=self.action_up, enabled=0)
        self.preview_button_down = JButton(r['down'], actionPerformed=self.action_down, enabled=0)
        
        preview_buttons = PyPanel([
            self.preview_button_up,
            self.preview_button_down])

        buttons_panel = PyPanel([JButton(r['preview'], actionPerformed=self.action_preview)])
        
        preview_panel = PyBorderPanel(south=preview_buttons, center=PyPanel([self.ilabel]), title=r['preview_title'])
         
        self.add(PyBorderPanel(north=result_panel, south=source_panel), BorderLayout.WEST)
        self.add(preview_panel, BorderLayout.EAST)
        self.add(buttons_panel, BorderLayout.SOUTH)
        
    def action_up(self, event):
        self.ilabel.set_image(self.preview.previous())

    def action_down(self, event):
        self.ilabel.set_image(self.preview.next())

    def action_preview(self, event):
        config = {}
        config['title'] = self.title.text
        config['font_name'] = self.font_name.get_selection()
        config['para_start'] = self.para_start.get_selection()
        config['interline'] = self.interline.get_selection()
        config['encoding'] = self.encoding.get_selection()
        config['language'] = self.language.get_selection()
        config['para_indent'] = self.para_indent.get_selection()
        config['hyphenate'] = self.hyphenate.selected
        config['justify'] = self.justify.selected        
        config['screen_border'] = self.screen_border.selected
        config['color'] = self.color_foreground
        config['background'] = self.color_background
        
        book = self.book.format(config)        
        self.preview = Preview(book)
        self.ilabel.setPreferredSize(self.bookshelf.platform.canvasDimension)
        self.ilabel.set_image(self.preview.current())
        self.preview_button_up.enabled = 1
        self.preview_button_down.enabled = 1

    def action_close(self, event):
        pass
        
    def action_set_foreground(self, event):
        color = JColorChooser.showDialog(self.parent, self.r['select_foreground_color'], self.color_foreground)
        if color is not None:
            self.button_color_foreground.set_color(color)
            self.color_foreground = color
                    
    def action_set_background(self, event):
        color = JColorChooser.showDialog(self.parent, self.r['select_background_color'], self.color_background)
        if color is not None:
            self.button_color_background.set_color(color)
            self.color_background = color
        
    def refresh_fonts(self):
        self.font_name.replace_items([(n, n) for n in self.bookshelf.font_fabric.list()])
        
    def validate(self):
        if self.title.text == "":
            return (self.r['save_noname_message'], self.r['save_noname_title'])

class FopView(JPanel):
    def __init__(self, bookshelf, fo_file):
        self.bookshelf = bookshelf
        self.book = bookshelf.add_fo_book(fo_file)
        self.preview = None
        self.r = r = c.resource('entry')
        self.layout = BorderLayout()
        # result
        self.title = JTextField(fo_file.getName().split('.')[0], 12)
        self.font_name = PyComboBox([(n, n) for n in bookshelf.font_fabric.list()])

        self.ilabel = PyLabel(None)
        self.ilabel.setPreferredSize(bookshelf.platform.canvasDimension)
        
        # defaults
        self.color_foreground = Color(c.config('DEFAULT_COLOR'))
        self.color_background = Color(c.config('DEFAULT_BACKGROUND_COLOR'))
        self.font_name.set_selection(c.config('DEFAULT_FONT_NAME'))

        self.button_color_foreground = PyColorButton(self.color_foreground, action=self.action_set_foreground)
        self.button_color_background = PyColorButton(self.color_background, action=self.action_set_background)
        
        buttons = PyPanel([JButton(r['preview'], actionPerformed=self.action_preview)])
        
        result_panel = PyPanel([
                PyLabel(r['name']), self.title,
                PyLabel(r['font']), self.font_name,
                PyLabel(r['color_foreground']), self.button_color_foreground,
                PyLabel(r['color_background']), self.button_color_background
                ], layout=GridLayout(4,2), title=r['result_title'])
        
        self.preview_button_up = JButton(r['up'], actionPerformed=self.action_up, enabled=0)
        self.preview_button_down = JButton(r['down'], actionPerformed=self.action_down, enabled=0)
        
        preview_buttons = PyPanel([
            self.preview_button_up,
            self.preview_button_down])

        buttons_panel = PyPanel([JButton(r['preview'], actionPerformed=self.action_preview)])
        
        preview_panel = PyBorderPanel(south=preview_buttons, center=PyPanel([self.ilabel]), title=r['preview_title'])
         
        self.add(PyBorderPanel(north=result_panel), BorderLayout.WEST)
        self.add(preview_panel, BorderLayout.EAST)
        self.add(buttons_panel, BorderLayout.SOUTH)        
        
    def action_up(self, event):
        self.ilabel.set_image(self.preview.previous())

    def action_down(self, event):
        self.ilabel.set_image(self.preview.next())
    
    def action_preview(self, event):
        config = {}
        config['title'] = self.title.text
        config['font_name'] = self.font_name.get_selection()
        config['color'] = self.color_foreground
        config['background'] = self.color_background

        book = self.book.format(config)
        self.preview = Preview(book)
        self.ilabel.setPreferredSize(self.bookshelf.platform.canvasDimension)
        self.ilabel.set_image(self.preview.current())
        self.preview_button_up.enabled = 1
        self.preview_button_down.enabled = 1
        
    def action_set_foreground(self, event):
        color = JColorChooser.showDialog(self.parent, self.r['select_foreground_color'], self.color_foreground)
        if color is not None:
            self.button_color_foreground.set_color(color)
            self.color_foreground = color
                    
    def action_set_background(self, event):
        color = JColorChooser.showDialog(self.parent, self.r['select_background_color'], self.color_background)
        if color is not None:
            self.button_color_background.set_color(color)
            self.color_background = color

    def action_close(self, event):
        pass

    def refresh_fonts(self):
        self.font_name.replace_items([(n, n) for n in self.bookshelf.font_fabric.list()])
        
    def validate(self):
        if self.title.text == "":
            return (self.r['save_noname_message'], self.r['save_noname_title'])