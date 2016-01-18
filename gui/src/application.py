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

from javax.swing import UIManager
from java.awt import Toolkit
from java.util import Locale
from config import load, config
def main():
    load('bookshelf.conf')
    if config('LOCALE') is not None and config('LOCALE') != '':
        Locale.setDefault(Locale(config('LOCALE')))
    from window import BookshelfView
    from logic import Bookshelf
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    view = BookshelfView(Bookshelf())
    screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    view.setLocation(screenSize.width/5,  screenSize.height/5)
    view.setVisible(1)

main()
