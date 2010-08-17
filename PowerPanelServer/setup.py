from distutils.core import setup
import sets
import py2exe

setup(
    windows=[
        {
            'script': 'server.py',
            'icon_resources': [(1, 'icons.ico')]
        },
    ],
)
