import os
import tempfile
import shutil

def before_scenario(context, scenario):
    context.tmpdir = tempfile.mkdtemp()
    os.chdir(context.tmpdir)

def after_scenario(context, scenario):
    os.chdir('/')
    shutil.rmtree(context.tmpdir)


