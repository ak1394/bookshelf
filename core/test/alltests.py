import unittest
import sys

dist_dir = sys.argv[2]

def suite():
    modules_to_test = ('font', 'jrender_element', 'jrender') # and so on
    alltests = unittest.TestSuite()
    for module in map(__import__, modules_to_test):
        alltests.addTest(unittest.findTestCases(module))
    return alltests

if __name__ == '__main__':
    unittest.main(defaultTest='suite', argv=sys.argv[0:2])