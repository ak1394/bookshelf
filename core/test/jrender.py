import unittest
import bookshelf.font
from bookshelf.jrender import *
from bookshelf.jrender.element import *
from java.io import File, ByteArrayInputStream, InputStreamReader
from java.lang import String
import alltests

class T01_ParagraphReaderTestCase(unittest.TestCase):
    def _make_reader(self, str, start):
        r = InputStreamReader(ByteArrayInputStream(String(str).getBytes()))
        self.reader = bookshelf.jrender.ParagraphReader(r, start)
        
    def testPara1(self):
        "ParagraphReader one-line paragraph"
        self._make_reader("one two three", 0)
        assert self.reader.read() == "one two three"
       
    def testPara2(self):
        "ParagraphReader two-line paragraph"
        self._make_reader("   one two three\none two three", 3)
        assert self.reader.read() == "one two three one two three"
       
    def testPara3(self):
        "ParagraphReader two two-line paragraphs"
        self._make_reader("   one two three\none two three\n   four five\n four five", 3)
        assert self.reader.read() == "one two three one two three"
        assert self.reader.read() == "four five four five"
       
    def testPara4(self):
        "ParagraphReader two onle-line paragraphs separated by empty line"
        self._make_reader("one two three\n\none two three", 10)
        assert self.reader.read() == "one two three"
        assert self.reader.read() == "one two three"

    def testPara5(self):
        "ParagraphReader one tree-line paragraph with some indentation"
        self._make_reader("    one\n  two\n  three", 4)
        assert self.reader.read() == "one two three"

    def testPara6(self):
        "ParagraphReader one tree-line paragraph with indents and spaces at the end"
        self._make_reader("    one  \n  two          \n  three         ", 4)
        assert self.reader.read() == "one two three"

class T02_ParagraphTestCase(unittest.TestCase):
    def setUp(self):
        # load fonts
        fontFabric = bookshelf.font.FontFabric(alltests.dist_dir + "/fonts")
        self.small = fontFabric.loadFont("Small")

    def test1(self):
        "Paragraph iterator test"
        p = bookshelf.jrender.Paragraph("one two three", self.small, 0)
        i = p.iterator()
        assert i.next().getContent() == "one"
        assert i.next().getContent() == "two"
        assert i.next().getContent() == "three"
        assert i.hasNext() == 0

    def test2(self):
        "Paragraph pushElement test"
        p = bookshelf.jrender.Paragraph("one two", self.small, 0)
        p.pushElement(Word("three", self.small))
        i = p.iterator()
        assert i.next().getContent() == "three"
        assert i.next().getContent() == "one"
        assert i.next().getContent() == "two"
        assert i.hasNext() == 0

class T03_SplitterTestCase(unittest.TestCase):
    def setUp(self):
        # load fonts
        fontFabric = bookshelf.font.FontFabric(alltests.dist_dir + "/fonts")
        self.small = fontFabric.loadFont("Small")
        self.verdana = fontFabric.loadFont("Verdana")

    def test1(self):
        "Splitter test case"
        s = Splitter()
        # width is zero
        r = s.split(Word("abcdef", self.small), 0)
        assert r[0].toString() == "" 
        assert r[1].toString() == "abcdef"
        # width is too small
        r = s.split(Word("abcdef", self.small), 2)
        assert r[0].toString() == "" 
        assert r[1].toString() == "abcdef"
        # width is ok, on the border of letter
        r = s.split(Word("abcdef", self.small), 10)
        assert r[0].toString() == "ab" 
        assert r[1].toString() == "cdef"
        # width is ok, a bit bigger then boundary of a letter
        r = s.split(Word("abcdef", self.small), 11)
        assert r[0].toString() == "ab" 
        assert r[1].toString() == "cdef"
        # width is big
        r = s.split(Word("ab", self.small), 10)
        assert r[0].toString() == "ab" 
        assert r[1].toString() == ""
        # width is too big
        r = s.split(Word("abcdef", self.small), 100)
        assert r[0].toString() == "abcdef" 
        assert r[1].toString() == ""
        # different font
        r = s.split(Word("abcdef", self.verdana), 10)
        assert r[0].toString() == "a" 
        assert r[1].toString() == "bcdef"

class T04_HyphenatorTestCase(unittest.TestCase):
    def setUp(self):
        # load fonts
        fontFabric = bookshelf.font.FontFabric(alltests.dist_dir + "/fonts")
        self.small = fontFabric.loadFont("Small")
        self.verdana = fontFabric.loadFont("Verdana")
        self.hs = Hyphenator(", ", "en", "-")
        self.hv = Hyphenator(", ", "en", "-")

    def test1(self):
        "Hyphenate one word using font 'Small'"
        word = Word("architector", self.small)
        r = self.hs.hyphenate(word, 13)
        assert r[0].toString() == "" and r[1].toString() == "architector"
        r = self.hs.hyphenate(word, 14)
        assert r[0].toString() == "ar-" and r[1].toString() == "chitector"
        r = self.hs.hyphenate(word, 25)
        assert r[0].toString() == "ar-" and r[1].toString() == "chitector"
        r = self.hs.hyphenate(word, 26)
        assert r[0].toString() == "archi-" and r[1].toString() == "tector"

    def test2(self):
        "Hyphenate one word using font 'Verdana'"
        word = Word("architector", self.verdana)
        r = self.hv.hyphenate(word, 15)
        assert r[0].toString() == "" and r[1].toString() == "architector"
        r = self.hv.hyphenate(word, 16)
        assert r[0].toString() == "ar-" and r[1].toString() == "chitector"
        r = self.hv.hyphenate(word, 30)
        assert r[0].toString() == "ar-" and r[1].toString() == "chitector"
        r = self.hv.hyphenate(word, 31)
        assert r[0].toString() == "archi-" and r[1].toString() == "tector"

    def test3(self):
        "Hyphenate fragment using font 'Small'"
        word = Word("cat,architector", self.small)
        assert self.hs.hyphenate(word, 13)[0].toString() == ""
        assert self.hs.hyphenate(word, 14)[0].toString() == "cat"
        assert self.hs.hyphenate(word, 16)[0].toString() == "cat"
        assert self.hs.hyphenate(word, 17)[0].toString() == "cat,"
        assert self.hs.hyphenate(word, 31)[0].toString() == "cat,ar-"
     
    def test4(self):
        "Hyphenate fragment using font 'Verdana'"
        word = Word("cat,architector", self.verdana)
        assert self.hv.hyphenate(word, 16)[0].toString() == ""
        assert self.hv.hyphenate(word, 17)[0].toString() == "cat"
        assert self.hv.hyphenate(word, 19)[0].toString() == "cat"
        assert self.hv.hyphenate(word, 20)[0].toString() == "cat,"
        assert self.hv.hyphenate(word, 36)[0].toString() == "cat,ar-"                        
       
class T05_SimpleFormatterTestCase(unittest.TestCase):
    def setUp(self):
        # load fonts
        fontFabric = bookshelf.font.FontFabric(alltests.dist_dir + "/fonts")
        self.small = fontFabric.loadFont("Small")
        self.verdana = fontFabric.loadFont("Verdana")
       
    def test1(self):
        "SimpleFormatter indent test"
        f = SimpleFormatter()
        f.space = 2
        f.width = 20
        # no indent
        para = Paragraph("one two three", self.small, 0)
        space = f.format(para).childIterator().next()
        assert space.isResizable() == 0 and space.width == 0
        # indent of 5
        para = Paragraph("one two three", self.small, 0)
        space = f.format(para, 5).childIterator().next()
        assert space.isResizable() == 0 and space.width == 5

    def test2(self):
        "SimpleFormatter tree line test"
        f = SimpleFormatter()
        f.space = 2
        f.width = 30
        para = Paragraph("one two three", self.small, 0)
        # first line
        i = f.format(para).childIterator()
        assert i.next().isResizable() == 0
        assert i.next().toString() == 'one'
        # second line
        i = f.format(para).childIterator()
        assert i.next().isResizable() == 0
        assert i.next().toString() == 'two'
        # third line
        i = f.format(para).childIterator()
        assert i.next().isResizable() == 0
        assert i.next().toString() == 'three'
        # no more lines
        assert f.format(para) == None
      
    def test3(self):
        "SimpleFormatter wide line, number of words"
        f = SimpleFormatter()
        f.space = 2
        f.width = 200
        # font 'small'
        para = Paragraph("one two three", self.small, 0)
        assert len([e for e in f.format(para).childIterator()]) == 6
        # font 'verdana'
        para = Paragraph("one two three", self.verdana, 0)
        assert len([e for e in f.format(para).childIterator()]) == 6

    def test4(self):
        "SimpleFormatter different fonts and indent"
        f = SimpleFormatter()
        f.space = 2
        f.width = 32
        # font 'small'
        para = Paragraph("one two three", self.small, 0)
        assert len([e for e in f.format(para).childIterator()]) == 4
        # font 'verdana'
        para = Paragraph("one two three", self.verdana, 0)
        assert len([e for e in f.format(para).childIterator()]) == 2
        # font 'small' with indent
        f.width = 32
        para = Paragraph("one two three", self.small, 0)
        assert len([e for e in f.format(para, 10).childIterator()]) == 2
       
class T06_HyphenatingFormatterTestCase(unittest.TestCase):
    def setUp(self):
        # load fonts
        fontFabric = bookshelf.font.FontFabric(alltests.dist_dir + "/fonts")
        self.small = fontFabric.loadFont("Small")
        self.hyphenator = bookshelf.jrender.Hyphenator(",-", "en", "-")
        
    def test1(self):
        "HyphenatingFormatter simple test"
        f = HyphenatingFormatter()
        para = Paragraph("cat pennyroyal-architect", self.small, 0)
        f.space = 2
        f.width = 80
        f.hyphenator = self.hyphenator
        i = f.format(para).childIterator()
        i.next()
        i.next()
        i.next()
        assert i.next().toString() == 'pennyroyal-ar-'

class T07_ParagraphTestCase(unittest.TestCase):
    def setUp(self):
        # load fonts
        fontFabric = bookshelf.font.FontFabric(alltests.dist_dir + "/fonts")
        self.small = fontFabric.loadFont("Small")

    def test1(self):
        "Paragraph format, SimpleFormatter"
        f = SimpleFormatter()
        f.space = 2
        f.width = 48
        para = Paragraph("cat calculatingly architect", self.small, 0)
        para.indent = 4
        r = para.format(f)
        expected = [(4,'cat'), (0, 'calculatingl'), (0, 'y'), (2, 'architect')]
        for line in r:
            (s, w) = expected.pop(0)
            i = line.childIterator()
            assert i.next().width == s
            assert i.next().toString() == w

    def test2(self):
        "Paragraph format, HyphenatingFormatter"
        f = HyphenatingFormatter()
        f.space = 2
        f.width = 48
        f.hyphenator = Hyphenator(",-", "en", "-")
        para = Paragraph("cat calculatingly architect", self.small, 4)
        r = para.format(f)

        i = r[0].childIterator()
        assert i.next().width == 4
        assert i.next().toString() == 'cat'
        assert i.next().width == 2
        assert i.next().toString() == 'calcu-'
        i = r[1].childIterator()
        assert i.next().width == 0
        assert i.next().toString() == 'latingly'
        assert i.next().width == 2
        assert i.next().toString() == 'ar-'
        i = r[2].childIterator()
        assert i.next().width == 0
        assert i.next().toString() == 'chitect'

    def test3(self):
        "Paragraph format, HyphenatingFormatter regression test 1"
        f = HyphenatingFormatter()
        f.space = 2
        f.width = 101
        f.hyphenator = Hyphenator(",-", "en", "-")
        para = Paragraph("The Old Sea-dog at the Admiral Benbow", self.small, 0)
        para.indent = 0
        r = para.format(f)
        assert r[0].toString() == "['<s~0>','The','<s2>','Old','<s2>','Sea-dog','<s2>','at','<s2>','the',]"
        assert r[1].toString() == "['<s~0>','Admiral','<s~2>','Benbow',]"

    def test4(self):
        "Paragraph format, HyphenatingFormatter regression test 2"
        f = HyphenatingFormatter()
        f.space = 2
        f.width = 101
        f.hyphenator = Hyphenator(",-", "en", "-")
        para = Paragraph("SQUIRE TRELAWNEY, Dr.", self.small, 0)
        para.indent = 0
        r = para.format(f)
        assert r[0].toString() == "['<s~0>','SQUIRE','<s2>','TRELAWNEY,',]"
        assert r[1].toString() == "['<s~0>','Dr.',]"
                