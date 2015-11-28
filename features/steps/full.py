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


@when('I initialize it')
def initialize(context):
    context.testee.inst__construct()

@when('I deinitialize it')
def initialize(context):
    context.testee.inst__destruct()

@when('I send an event click()')
def send_event(context):
    context.testee.inst_click()

@when('I send an event tick()')
def send_event(context):
    context.testee.inst_tick()

@when('I send an event inp({value:d})')
def send_event(context, value):
    context.testee.inst_inp(value)

@when('I send an event set({value1:d}, {value2:d})')
def set_event(context, value1, value2):
    context.testee.inst_set(value1, value2)

@then('I expect an event {expectedEvent}')
def expect_event_0(context, expectedEvent):
    assert context.testee._canRead()
    expectedEvent = 'inst_' + expectedEvent
    event = context.testee._next()
    assert event == expectedEvent, 'expected: ' + expectedEvent + '; got: ' + event

@then('I expect the request get({value1:d}) = {result:d}')
def request(context, value1, result):
    assert context.testee.inst_get(value1) == result

@then('I expect the request read() = {result:d}')
def request(context, result):
    assert context.testee.inst_read() == result

@then('I expect no more events')
def no_more_events(context):
    assert not context.testee._canRead()




