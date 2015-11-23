from behave import *


@given('we have a file "{filename}" with the content')
def write_file(context, filename):
    file = open(filename, 'w+')
    file.write(context.text)
    file.close()
    pass


@then('there should be a file "{filename}" with the content')
def read_file(context, filename):
    file = open(filename, 'r')
    content = file.read()
    file.close()
    assert content == context.text

