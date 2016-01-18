import unittest
import bookshelf.jrender
import bookshelf.font
import alltests

class FontTestCase(unittest.TestCase):
    def setUp(self):
        "FontFabric font loading"
        self.fontFabric = bookshelf.font.FontFabric(alltests.dist_dir + "/fonts")
        self.fonts = []
        for name in ["Small", "Verdana", "Narrow", "Tahoma"]:
            self.fonts.append(self.fontFabric.loadFont(name))

    def test1(self):
        "FontFabric font list"
        list = [name for name in self.fontFabric.list()]
        list.sort()
        assert list == ['Narrow', 'Small', 'Tahoma', 'Times', 'Verdana']

    def test2(self):
        "Font maxWidth"
        v = [11, 11, 11, 11]
        for i in range(len(v)):
            assert self.fonts[i].maxWidth == v[i]

    def test3(self):
        "Font height"
        v = [8, 12, 9, 9]
        for i in range(len(v)):
            assert self.fonts[i].height == v[i]

    def test4(self):
        "Font firstChar"
        v = [9, 9, 9, 9]
        for i in range(len(v)):
            assert self.fonts[i].firstChar == v[i]

    def test5(self):
        "Font lastChar"
        v = [256, 256, 256, 256]
        for i in range(len(v)):
            assert self.fonts[i].lastChar == v[i]
            
    def test6(self):
        "Font stringWidth"
        v = [15, 18, 15, 14]
        for i in range(len(v)):
            assert self.fonts[i].stringWidth("abc") == v[i]
        assert self.fonts[0].stringWidth("a") == 5
        assert self.fonts[0].stringWidth("b") == 5
        assert self.fonts[1].stringWidth("a") == 6
        assert self.fonts[1].stringWidth("b") == 6        