from behave import *

import subprocess
import os

from ctypes import *


@when('I initialize it')
def initialize(context):
    context.testee.inst__construct()

@when('I deinitialize it')
def initialize(context):
    context.testee.inst__destruct()

@then('I expect no more events')
def no_more_events(context):
    assert not context.testee._canRead()

@then('I expect {count:d} events in the queue')
def step_impl(context, count):
    assert count == context.testee.inst__queue_count()

@when('I process one event')
def step_impl(context):
    context.testee.inst__queue_dispatch()

