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
from java.util import Locale
from java.awt import Color
from config import load, config
from logic import Bookshelf
import sys

def main():
    load(sys.argv[1])
    bookshelf = Bookshelf()
    book = bookshelf.add_fo_book(File(sys.argv[2]))
    c = {}
    c['title'] = sys.argv[2]
    c['font_name'] = config('DEFAULT_FONT_NAME')
    c['para_start'] = 4
    c['interline'] = 0
    c['encoding'] = 'windows-1251'
    c['language'] = 'ru'
    c['para_indent'] = 8
    c['hyphenate'] = 1
    c['justify'] = 1
    c['screen_border'] = 1
    c['color'] = Color.BLACK
    c['background'] = Color.WHITE
    book.format(c)
    bookshelf.save(File(sys.argv[3]))
    
main()
