from behave import *


@given('we have a file "{filename}" with the content')
def write_file(context, filename):
    file = open(filename, 'w+')
    file.write(context.text)
    file.close()


@then('there should be a file "{filename}" with the content')
def read_file(context, filename):
    file = open(filename, 'r')
    content = file.read()
    file.close()
    assert content == context.text


@then('I expect the fragment "{fragment}" in the interface description')
def step_impl(context, fragment):
    ifaceFilename = context.tmpdir + '/output/' + 'inst.h'

    file = open(ifaceFilename, 'r')
    iface = file.read()
    file.close()

    assert fragment in iface, 'fragment not found in interface: ' + fragment

