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

from java.util import ResourceBundle, PropertyResourceBundle, Properties, MissingResourceException, Locale
from org.python.core import imp
from java.io import File
from bookshelf.builder import PlatformPackage, ReaderPackage

_config = {}
_config_name = ''

MAX_INTERLINE_SPACING = 6
MAX_PARA_INDENT = 11
SUPPORTED_ENCODINGS = [('windows-1251','CP1251'), ('KOI8-R','KOI8-R'), ('CP866', 'DOS Russian'), ('ISO-8859-1', 'Latin 1'), ('ISO-8859-2', 'Latin2'), ('ISO-8859-3', 'Latin3')]

def load(name):
    global _config_name
    _config_name = name
    exec open(name) in _config
    # sort some keys
    _config['SUPPORTED_LOCALES'].sort()

def plugins():
    plugins = {}
    platform = PlatformPackage(_config['PLATFORM_DIR'] + _config['PLATFORM_JAR'])
    platform.setBrandModel(_config['PLATFORM_BRAND'], _config['PLATFORM_MODEL'])
    readerPackage = ReaderPackage(_config['READER_JAR'], platform)
    for plugin in readerPackage.getPlugins():
        if not plugin.name == 'pager':
            plugins[plugin.name] = (plugin.description, plugin.compressedSize)
    return plugins

def supported():
    supported = {}
    for f in File(_config['PLATFORM_DIR']).list():
        platform = PlatformPackage(_config['PLATFORM_DIR'] + f)
        platformSupported = platform.getSupported()
        for brand in platformSupported.keySet():
            if not supported.has_key(brand):
                supported[brand] = {}
            for model in platformSupported[brand]:
                if not supported[brand].has_key(model):
                    supported[brand][model] = {}
                supported[brand][model][f] = (f, platform.getDescription())
    return supported    
    
def save():
    keys = _config.keys()
    keys.sort()
    config_file = open(_config_name, 'w')
    for key in keys:
        config_file.write(key + ' = ' + repr(_config[key]) + '\n')
    config_file.close()
    
def config(key):
    if _config.has_key(key):
        return _config[key]

def set_default(key, value):
    if _config.has_key(key):
        _config[key] = value
    else:
        raise Exception, "Unknown key " + key + " for default config value"        
        
def resource(name):
        result = {}
        try:
            r = ResourceBundle.getBundle("properties/" + name, Locale.getDefault(), imp.getSyspathJavaLoader())
            for key in r.getKeys():
                result[key] = r.getString(key)
        except MissingResourceException, e:
            pass
        return result
        
