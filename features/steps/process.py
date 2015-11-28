from behave import *

import subprocess


@when('I start rizzly with the file "{filename}"')
def do_compile(context, filename):
    context.proc = subprocess.Popen(["rizzly", "-i", filename], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    context.proc.wait()

@when('I succesfully compile "{filename}" with rizzly')
def compile_ok(context, filename):
    do_compile(context, filename)
    no_error_code(context)

@then('I expect the exit code {code:d}')
def exit_code(context, code):
    assert context.proc.returncode == code


@then('I expect an error code')
def exit_code(context):
    assert context.proc.returncode != 0


@then('I expect no error')
def no_error_code(context):
    assert context.proc.returncode == 0, 'unexpected error ' + str(context.proc.returncode) + '\n' + context.proc.stderr.read()


@then('stderr should contain "{text}"')
def stderr_not_empty(context, text):
    output = context.proc.stderr.read()
    assert output.find(text) != -1, 'expected to see "' + text + '", got: \n' + output


@then('stdout should contain "{text}"')
def stdout_not_empty(context, text):
    output = context.proc.stdout.read()
    assert output.find(text) != -1, 'expected to see "' + text + '", got: \n' + output


@then('some output on stderr')
def stderr_not_empty(context):
    output = context.proc.stderr.read()
    assert output != ''

