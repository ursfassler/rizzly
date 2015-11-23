from behave import *

import subprocess


@when('I start rizzly with the file "{filename}"')
def argument(context, filename):
    context.proc = subprocess.Popen(["rizzly", "-i", filename], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    context.proc.wait()


@then('I expect the exit code {code:d}')
def exit_code(context, code):
    assert context.proc.returncode == code


@then('I expect an error code')
def exit_code(context):
    assert context.proc.returncode != 0


@then('I expect no error')
def exit_code(context):
    assert context.proc.returncode == 0


@then('stderr should contain "{text}"')
def stderr_not_empty(context, text):
    output = context.proc.stderr.read()
    assert output.find(text) != -1, 'expected to see "' + text + '", got: \n' + output


@then('some output on stderr')
def stderr_not_empty(context):
    output = context.proc.stderr.read()
    assert output != ''

