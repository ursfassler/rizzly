from behave import *

import subprocess
import os
import sys

from ctypes import *


def compileAll():
    for f in ['instCb.cc', 'Makefile', 'queue.cc', 'queue.h', 'queue.py', 'inst.py'] :
        subprocess.call(['cp', '/home/urs/projekte/rizzlygit/rizzly/features/full/' + f, '.'])
    subprocess.call(['make'])

def createInstance():
    sys.path.append(os.getcwd())
    module = __import__("inst")
    return module.Inst()


@when('fully compile everything')
def fully_compile(context):
    compileAll()
    context.testee = createInstance()


@when('I initialize it')
def initialize(context):
    context.testee._construct()

@when('I send an event click()')
def send_event(context):
    context.testee.click()

@then('I expect an event {expectedEvent}')
def expect_event_0(context, expectedEvent):
    assert context.testee._canRead()
    event = context.testee._next()
    assert event == expectedEvent, 'expected: ' + expectedEvent + '; got: ' + event

@then('I expect no more events')
def no_more_events(context):
    assert not context.testee._canRead()


