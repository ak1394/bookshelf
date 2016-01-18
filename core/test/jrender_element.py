import unittest
import bookshelf.font
from bookshelf.jrender.element import *
import alltests

fontFabric = bookshelf.font.FontFabric(alltests.dist_dir + "/fonts")

class T01_ParagraphTestCase(unittest.TestCase):
    def setUp(self):
        # load fonts
        self.small = fontFabric.loadFont("Small")

    def test1(self):
        "Line width"
        l = Line()
        l.add(Word("a", self.small))