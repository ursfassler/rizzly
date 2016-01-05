from behave import *

import subprocess
import os

from ctypes import *

@when('I send an event click()')
def send_event(context):
    context.testee.inst_click()

@when('I send an event tick()')
def send_event(context):
    context.testee.inst_tick()

@when('I send an event inp({value:d})')
def send_event(context, value):
    context.testee.inst_inp(value)

@when('I send an event inp({value:Bool})')
def send_event(context, value):
    context.testee.inst_inp(value)

@when('I send an event inp({value1:Bool}, {value2:Bool})')
def send_event(context, value1, value2):
    context.testee.inst_inp(value1, value2)

@when('I send an event inp({value1:d}, {value2:d})')
def send_event(context, value1, value2):
    context.testee.inst_inp(value1, value2)

@when('I send an event inp({value1:Bool}, {value2:Bool}, {value3:Bool})')
def send_event(context, value1, value2, value3):
    context.testee.inst_inp(value1, value2, value3)

@when(u'I send an event inp([{arr0:d}, {arr1:d}, {arr2:d}, {arr3:d}])')
def step_impl(context, arr0, arr1, arr2, arr3):
    context.testee.inst_inp([arr0, arr1, arr2, arr3])

@when('I send an event set({value1:d}, {value2:d})')
def set_event(context, value1, value2):
    context.testee.inst_set(value1, value2)

@then('I expect an event {expectedEvent}')
def expect_event_0(context, expectedEvent):
    assert context.testee._canRead()
    expectedEvent = 'inst_' + expectedEvent
    event = context.testee._next()
    assert event == expectedEvent, 'expected: ' + expectedEvent + '; got: ' + event

@then('I expect the request get() = {result:d}')
def step_impl(context, result):
    assert context.testee.inst_get() == result

@then('I expect the request get() = {result:Bool}')
def step_impl(context, result):
    assert context.testee.inst_get() == result

@then('I expect the request get({value1:d}) = {result:d}')
def step_impl(context, value1, result):
    assert context.testee.inst_get(value1) == result

@then('I expect the request op({value:d}) = {result:d}')
def step_impl(context, value, result):
    assert context.testee.inst_op(value) == result

@then('I expect the request op({value:Bool}) = {result:Bool}')
def step_impl(context, value, result):
    calculatedValue = context.testee.inst_op(value)
    assert calculatedValue == result, 'expected ' + str(result) + ', got ' + str(calculatedValue)

@then('I expect the request op({left:d}, {right:d}) = {result:d}')
def step_impl(context, left, right, result):
    calculatedValue = context.testee.inst_op(left, right)
    assert calculatedValue == result, 'expected ' + str(result) + ', got ' + str(calculatedValue)

@then('I expect the request op({value1:d}, {value2:d}, {value3:d}) = {result:d}')
def step_impl(context, value1, value2, value3, result):
    calculatedValue = context.testee.inst_op(value1, value2, value3)
    assert calculatedValue == result, 'expected ' + str(result) + ', got ' + str(calculatedValue)

@then('I expect the request op({value1:Bool}, {value2:Bool}, {value3:Bool}) = {result:Bool}')
def step_impl(context, value1, value2, value3, result):
    calculatedValue = context.testee.inst_op(value1, value2, value3)
    assert calculatedValue == result, 'expected ' + str(result) + ', got ' + str(calculatedValue)

@then('I expect the request op({value1:d}, {value2:d}, {value3:Bool}) = {result:Bool}')
def step_impl(context, value1, value2, value3, result):
    calculatedValue = context.testee.inst_op(value1, value2, value3)
    assert calculatedValue == result, 'expected ' + str(result) + ', got ' + str(calculatedValue)

@then('I expect the request op({value1:d}, {value2:d}, {value3:d}, {value4:d}) = {result:Bool}')
def step_impl(context, value1, value2, value3, value4, result):
    calculatedValue = context.testee.inst_op(value1, value2, value3, value4)
    assert calculatedValue == result, 'expected ' + str(result) + ', got ' + str(calculatedValue)

@then('I expect the request op({left:d}, {right:d}) = {result:Bool}')
def step_impl(context, left, right, result):
    calculatedValue = context.testee.inst_op(left, right)
    assert calculatedValue == result, 'expected ' + str(result) + ', got ' + str(calculatedValue)

@then('I expect the request op({left:Bool}, {right:Bool}) = {result:Bool}')
def step_impl(context, left, right, result):
    calculatedValue = context.testee.inst_op(left, right)
    assert calculatedValue == result, 'expected ' + str(result) + ', got ' + str(calculatedValue)

@then('I expect the request read() = {result:d}')
def request(context, result):
    assert context.testee.inst_read() == result

@then('I expect the request read() = {result:Bool}')
def request(context, result):
    assert context.testee.inst_read() == result

@then(u'I expect the request read() = \'{text}\'')
def step_impl(context, text):
    raise NotImplementedError(u'STEP: Then I expect the request read() = \'{text}\'')


