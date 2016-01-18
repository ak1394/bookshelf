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

from java.io import File
from bookshelf.builder import MidletBuilder, ReaderPackage, PlatformPackage
from bookshelf.font import *
from config import load, config
from bookshelf.book import BookWriter
from bookshelf.jrender import *
from bookshelf.foprender import FopFormatter

class Bookshelf:
    def __init__(self):
        self.books = []
        self.configure()

    def configure(self):       
        self.font_fabric = FontFabric(config('FONT_DIRECTORY'))
        self.platform = PlatformPackage(config('PLATFORM_DIR') + config('PLATFORM_JAR'))
        self.platform.setBrandModel(config('PLATFORM_BRAND'), config('PLATFORM_MODEL'))
        self.platform.setRotation(config('SCREEN_ROTATION'))
        self.reader_package = ReaderPackage(config('READER_JAR'), self.platform)

    def add_book(self, f):
        book = Book(f, self)
        self.books.append(book)            
        return book

    def add_fo_book(self, f):
        book = FoBook(f, self)
        self.books.append(book)            
        return book

    def del_book(self, book):
        self.books.remove(book)
        
    def save(self, f):
        # TODO check if previews been generated / ie book rendered
        builder = MidletBuilder(self.reader_package, self.platform)
        builder.language = config('MIDLET_LOCALE')
        for book in self.books:
            builder.add(book.book)
        # add plugins
        for plugin in config('PLUGIN_LIST'):
            builder.addPlugin(plugin)
        # always add pager
        builder.addPlugin('pager')
        builder.write(f, config('MAKE_FOLDER'))
        
class Book:
    def __init__(self, text_file, bookshelf):
        self.text_file = text_file
        self.bookshelf = bookshelf

    def format_fop(self, config):
        fontWriter = self.bookshelf.platform.fontWriter
        font = self.bookshelf.font_fabric.loadFont(config['font_name'])
        font.color = config['color']
        font.background = config['background']

        bookWriter = BookWriter(dimension)
        bookWriter.setFont(font)

    def format(self, config):
        fontWriter = self.bookshelf.platform.fontWriter
        font = self.bookshelf.font_fabric.loadFont(config['font_name'])
        font.color = config['color']
        font.background = config['background']
        
        dimension = self.bookshelf.platform.canvasDimension
        line_height = (font.getHeight() + config['interline'])
        dimension.height = (dimension.height / line_height) * line_height

        bookWriter = BookWriter(dimension)
        bookWriter.setFont(font)
        
        paragraphSource = ParagraphSource(self.text_file, config['encoding'], font, config['para_start'], config['para_indent'])

        if config['hyphenate']:
            formatter = HyphenatingFormatter()
            formatter.setHyphenator(Hyphenator(config['language'], "-"))
        else:
            formatter = SimpleFormatter()
        
        formatter.setWidth(bookWriter.getPageSize().width)
        formatter.setSpace(2)
        if config['screen_border']: formatter.setMargin(1)

        paginator = Paginator()
        paginator.setFormatter(formatter)
        paginator.setParagraphSource(paragraphSource)
        paginator.setInterline(config['interline'])

        if config['justify']:
            lineJustifier = LineJustifier();
            lineJustifier.setWidth(bookWriter.pageSize.width);
            paginator.decorator = lineJustifier
        
        paginator.write(bookWriter)
        self.book = bookWriter.getBook()
        self.book.setTitle(config['title'])
        return self.book

class FoBook:
    def __init__(self, fo_file, bookshelf):
        self.fo_file = fo_file
        self.bookshelf = bookshelf

    def format(self, config):
        fontWriter = self.bookshelf.platform.fontWriter
        font = self.bookshelf.font_fabric.loadFont(config['font_name'])
        font.color = config['color']
        font.background = config['background']

        dimension = self.bookshelf.platform.canvasDimension
        bookWriter = BookWriter(dimension)
        bookWriter.setFont(font)

        fopFormatter = FopFormatter()
        fopFormatter.setFont(font)
        fopFormatter.format(self.fo_file, bookWriter)

        self.book = bookWriter.getBook()
        self.book.setTitle(config['title'])
        return self.book