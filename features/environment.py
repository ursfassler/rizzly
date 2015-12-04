import os
import tempfile
import shutil
import sys

from parse_type import TypeBuilder
from behave import register_type

parse_bool = TypeBuilder.make_enum({"False": False, "True": True})
register_type(Bool=parse_bool)


def before_scenario(context, scenario):
    context.tmpdir = tempfile.mkdtemp()
    sys.path.append(context.tmpdir + '/output')
    os.chdir(context.tmpdir)


def after_scenario(context, scenario):
    sys.path.remove(context.tmpdir + '/output')
    os.chdir('/')
    shutil.rmtree(context.tmpdir)

