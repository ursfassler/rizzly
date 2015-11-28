import os
import tempfile
import shutil
import sys

def before_scenario(context, scenario):
    context.tmpdir = tempfile.mkdtemp()
    sys.path.append(context.tmpdir + '/output')
    os.chdir(context.tmpdir)

def after_scenario(context, scenario):
    sys.path.remove(context.tmpdir + '/output')
    os.chdir('/')
    shutil.rmtree(context.tmpdir)

