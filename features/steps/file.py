from behave import *
import subprocess
import os


@given('we have a file "{filename}" with the content')
def write_file(context, filename):
    file = open(filename, 'w+')
    file.write(context.text)
    file.close()


@then('there should be a file "{filename}" with the content')
def read_file(context, filename):
    if not os.path.isfile(filename):
        assert False, 'file not found: ' + filename

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

@then(u'I expect an xml file "{filename}" with the content')
def step_impl(context, filename):
    if not os.path.isfile(filename):
        assert False, 'file not found: ' + filename

    file = open('__expected.xml', 'w+')
    file.write(context.text)
    file.close()

    proc = subprocess.Popen(['xmldiff', '__expected.xml', filename], stdout=subprocess.PIPE)
    proc.wait()
    
    assert proc.returncode == 0, 'returned error: ' + str(proc.returncode) + '\n' + proc.stdout.read()


