from behave import *

import subprocess
import os

from ctypes import *


def compileAll():
    subprocess.call(['make'])

def createInstance(library):
    import inst
    inst = reload(inst)
    return inst.Inst(library)


@when('fully compile everything')
def fully_compile(context):
    os.chdir(context.tmpdir + '/output')
    compileAll()
    context.testee = createInstance(context.tmpdir + '/output/' + 'libinst.so')

