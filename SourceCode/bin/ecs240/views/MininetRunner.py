import os
import re

from mininet.log import info, error, debug, output, setLogLevel
from mininet.net import Mininet, VERSION
from mininet.node import Controller, RemoteController, NOX, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, OVSSwitch, UserSwitch
from mininet.link import TCLink, Intf, Link
from mininet.topo import Topo

from mininet.clean import cleanup
from mininet.cli import CLI
from mininet.log import lg, LEVELS, info
from mininet.net import Mininet, MininetWithControlNet, VERSION
from mininet.node import (Host, CPULimitedHost, Controller, OVSController,
                           NOX, RemoteController, UserSwitch, OVSKernelSwitch,
                           OVSLegacyKernelSwitch)
from mininet.link import Link, TCLink
from mininet.topo import SingleSwitchTopo, LinearTopo, SingleSwitchReversedTopo
from mininet.topolib import TreeTopo
from mininet.util import custom, customConstructor, dumpNodeConnections
from mininet.util import buildTopo, quietRun

from mininet.examples.multitest import ifconfigTest

from time import sleep

print 'running against MiniNet ' + VERSION

class MininetTopo(Topo):

    "A simple network topo for Mininet."

    def __init__(self, parent=None):
        super(MininetTopo, self).__init__()
        
        self.my_filename = os.getcwd() + '/info.topo'
        self.my_nodes = []
        self.my_links = {}
        self.fd = None
        
        self.buildNodes()
        self.buildLinks()

        # self.print_topo()
        
    def buildNodes(self):
        print self.my_filename
        self.fd = open(self.my_filename, 'r')
        print ("*****building nodes")
        for line in self.fd:
            if "node" in line:
                commapos = line.find(';')
                name = line[5:commapos]
                print name
                if "switch" in line:
                    switch = self.addSwitch(name)
                    self.my_nodes.append(switch)
                elif"host" in line:
                    host = self.addHost(name)
                    self.my_nodes.append(host)
        self.fd.close()

    def buildLinks(self):
        self.fd = open(self.my_filename, 'r')
        print("*****buiding links")
        for line in self.fd:
            if "link" in line:
                commapos = line.find(':')
                semipos = line.find(';')
                src = line[commapos + 1:semipos ]
                dst = line[semipos + 1:-1]
                print "(" + src + "," + dst + ")"
                self.addLink(node1=src, node2=dst)
                self.my_links[src] = dst
        self.fd.close()

    def print_topo(self):
        print("*****print topo")
        print(self.my_nodes)
        print(self.my_links.items())
    
#!/usr/bin/env python

"""
Mininet runner
author: Brandon Heller (brandonh@stanford.edu)
changes: Joshua Reich (jreich@cs.princeton.edu)
from mininet/master branch commit 6c22e057cc45e9bda5c110815aeee12bfc7e2be5

To see options:
  sudo mn -h

Example to pull custom params (topo, switch, etc.) from a file:
  sudo mn --custom ~/mininet/custom/custom_example.py
"""

from optparse import OptionParser
import os
import sys
import time

# Fix setuptools' evil madness, and open up (more?) security holes
if 'PYTHONPATH' in os.environ:
    sys.path = os.environ[ 'PYTHONPATH' ].split(':') + sys.path

# built in topologies, created only when run
TOPODEF = 'minimal'
TOPOS = { 'minimal': lambda: SingleSwitchTopo(k=2),
          'linear': LinearTopo,
          'reversed': SingleSwitchReversedTopo,
          'single': SingleSwitchTopo,
          'tree': TreeTopo,
          'my':  MininetTopo}

SWITCHDEF = 'ovsk'
SWITCHES = { 'user': UserSwitch,
             'ovsk': OVSKernelSwitch,
             'ovsl': OVSLegacyKernelSwitch }

HOSTDEF = 'proc'
HOSTS = { 'proc': Host,
          'rt': custom(CPULimitedHost, sched='rt'),
          'cfs': custom(CPULimitedHost, sched='cfs') }

CONTROLLERDEF = 'ovsc'
CONTROLLERS = { 'ref': Controller,
                'ovsc': OVSController,
                'nox': NOX,
                'remote': RemoteController,
                'none': lambda name: None }

LINKDEF = 'default'
LINKS = { 'default': Link,
          'tc': TCLink }


# optional tests to run
TESTS = [ 'pingall', 'iperfudp', 'iperftcp', 'ifconfig']

ALTSPELLING = { 'pingall': 'pingall',
                'iperfudp': 'iperfudp',
                'ifconfig': 'ifconfig',
                'iperftcp':'iperftcp'}


def addDictOption(opts, choicesDict, default, name, helpStr=None):
    """Convenience function to add choices dicts to OptionParser.
       opts: OptionParser instance
       choicesDict: dictionary of valid choices, must include default
       default: default choice key
       name: long option name
       help: string"""
    if default not in choicesDict:
        raise Exception('Invalid  default %s for choices dict: %s' % 
                         (default, name))
    if not helpStr:
        helpStr = ('|'.join(sorted(choicesDict.keys())) + 
                    '[,param=value...]')
    opts.add_option('--' + name,
                     type='string',
                     default=default,
                     help=helpStr)


def version(*_args):
    "Print Mininet version and exit"
    print "%s" % VERSION
    exit()

class MininetRunner(object):
    "Build, setup, and run Mininet."

    def __init__(self):
        "Init."
        self.options = None
        self.args = None  # May be used someday for more CLI scripts
        self.validate = None

        self.parseArgs()
        self.setup()
        self.begin()

    def setCustom(self, name, value):
        "Set custom parameters for MininetRunner."
        if name in ('topos', 'switches', 'hosts', 'controllers'):
            # Update dictionaries
            param = name.upper()
            globals()[ param ].update(value)
        elif name == 'validate':
            # Add custom validate function
            self.validate = value
        else:
            # Add or modify global variable or class
            globals()[ name ] = value

    def parseCustomFile(self, fileName):
        "Parse custom file and add params before parsing cmd-line options."
        customs = {}
        if os.path.isfile(fileName):
            execfile(fileName, customs, customs)
            for name, val in customs.iteritems():
                self.setCustom(name, val)
        else:
            raise Exception('could not find custom file: %s' % fileName)

    def parseArgs(self):
        """Parse command-line args and return options object.
           returns: opts parse options dict"""
        if '--custom' in sys.argv:
            index = sys.argv.index('--custom')
            if len(sys.argv) > index + 1:
                filename = sys.argv[ index + 1 ]
                self.parseCustomFile(filename)
            else:
                raise Exception('Custom file name not found')

        desc = ("The %prog utility creates Mininet network from the\n"
                 "command line. It can create parametrized topologies,\n"
                 "invoke the Mininet CLI, and run tests.")

        usage = ('%prog [options]\n'
                  '(type %prog -h for details)')

        opts = OptionParser(description=desc, usage=usage)
        addDictOption(opts, SWITCHES, SWITCHDEF, 'switch')
        addDictOption(opts, HOSTS, HOSTDEF, 'host')
        addDictOption(opts, CONTROLLERS, CONTROLLERDEF, 'controller')
        addDictOption(opts, LINKS, LINKDEF, 'link')
        addDictOption(opts, TOPOS, TOPODEF, 'topo')

        opts.add_option('--clean', '-c', action='store_true',
                         default=False, help='clean and exit')
        opts.add_option('--custom', type='string', default=None,
                         help='read custom topo and node params from .py' + 
                         'file')
        opts.add_option('--test', type='choice', choices=TESTS,
                         default=TESTS[ 0 ],
                         help='|'.join(TESTS))
        opts.add_option('--xterms', '-x', action='store_true',
                         default=False, help='spawn xterms for each node')
        opts.add_option('--ipbase', '-i', type='string', default='10.0.0.0/8',
                         help='base IP address for hosts')
        opts.add_option('--mac', action='store_true',
                         default=False, help='automatically set host MACs')
        opts.add_option('--arp', action='store_true',
                         default=False, help='set all-pairs ARP entries')
        opts.add_option('--verbosity', '-v', type='choice',
                         choices=LEVELS.keys(), default='info',
                         help='|'.join(LEVELS.keys()))
        opts.add_option('--innamespace', action='store_true',
                         default=False, help='sw and ctrl in namespace?')
        opts.add_option('--listenport', type='int', default=6634,
                         help='base port for passive switch listening')
        opts.add_option('--nolistenport', action='store_true',
                         default=False, help="don't use passive listening " + 
                         "port")
        opts.add_option('--pre', type='string', default=None,
                         help='CLI script to run before tests')
        opts.add_option('--post', type='string', default=None,
                         help='CLI script to run after tests')
        opts.add_option('--pin', action='store_true',
                         default=False, help="pin hosts to CPU cores "
                         "(requires --host cfs or --host rt)")
        opts.add_option('--version', action='callback', callback=version)

        self.options, self.args = opts.parse_args()

    def setup(self):
        "Setup and validate environment."

        # set logging verbosity
        if LEVELS[self.options.verbosity] > LEVELS['output']:
            print ('*** WARNING: selected verbosity level (%s) will hide CLI '
                    'output!\n'
                    'Please restart Mininet with -v [debug, info, output].'
                    % self.options.verbosity)
        lg.setLogLevel(self.options.verbosity)

    def begin(self):
        "Create and run mininet."

        if self.options.clean:
            cleanup()
            exit()

        start = time.time()

        topo = buildTopo(TOPOS, self.options.topo)
        switch = customConstructor(SWITCHES, self.options.switch)
        host = customConstructor(HOSTS, self.options.host)
        controller = customConstructor(CONTROLLERS, self.options.controller)
        link = customConstructor(LINKS, self.options.link)

        if self.validate:
            self.validate(self.options)

        inNamespace = self.options.innamespace
        Net = MininetWithControlNet if inNamespace else Mininet
        ipBase = self.options.ipbase
        xterms = self.options.xterms
        mac = self.options.mac
        arp = self.options.arp
        pin = self.options.pin
        listenPort = None
        if not self.options.nolistenport:
            listenPort = self.options.listenport
        mn = Net(topo=topo,
                  switch=switch, host=host, controller=controller,
                  link=link,
                  ipBase=ipBase,
                  inNamespace=inNamespace,
                  xterms=xterms, autoSetMacs=mac,
                  autoStaticArp=arp, autoPinCpus=pin,
                  listenPort=listenPort)

        # ## PYRETIC CHANGE - SETUP HOSTS BASED ON TOPOLOGY NODE_INFO
        for host in mn.hosts:
            try:
                gw = topo.node_info[host.name]['gw']
                intf = host.defaultIntf()
                host.cmd('route add default gw %s dev %s' % (gw, intf))        
            except KeyError:
                pass
        # ## END PYRETIC CHANGE - SETUP HOSTS BASED ON TOPOLOGY NODE_INFO

        if self.options.pre:
            CLI(mn, script=self.options.pre)

        test = self.options.test
        test = ALTSPELLING.get(test, test)

        mn.start()
        print "*** network built:"
        dumpNodeConnections(mn.values())

        if test == 'pingall':
            mn.ping()
        elif test == 'iperfudp':
            iperf(mn)
        elif test == 'iperftcp':
            iperf(mn, 'TCP')
        elif test == 'ifconfig':
            ifconfigTest(mn)
        else:
            pass
        
        if self.options.post:
            CLI(mn, script=self.options.post)

        mn.stop()

        elapsed = float(time.time() - start)
        info('completed in %0.3f seconds\n' % elapsed)

def iperf(net, l4Type='UDP', udpBw='10M'):
    for node in net.hosts:
        for dest in net.hosts:
            if node != dest:
                hosts = [ node, dest ]
                client, server = hosts
                output('*** Iperf: testing ' + l4Type + ' bandwidth between ')
                output("%s and %s\n" % (client.name, server.name))
                server.cmd('killall -9 iperf')
                iperfArgs = 'iperf '
                bwArgs = ''
                if l4Type == 'UDP':
                    iperfArgs += '-u '
                    bwArgs = '-b ' + udpBw + ' '
                elif l4Type != 'TCP':
                    raise Exception('Unexpected l4 type: %s' % l4Type)
                server.sendCmd(iperfArgs + '-s', printPid=True)
                servout = ''
                while server.lastPid is None:
                    servout += server.monitor()
                if l4Type == 'TCP':
                    counttried = 0
                    while 'Connected' not in client.cmd(
                            'sh -c "echo A | telnet -e A %s 5001"' % server.IP()):
                        print('waiting for iperf to start up...')
                        sleep(.5)
                        counttried = counttried + 1
                        if(counttried == 4):
                            break
                cliout = client.cmd(iperfArgs + '-t 5 -c ' + server.IP() + ' ' + 
                                     bwArgs)
                debug('Client output: %s\n' % cliout)
                server.sendInt()
                servout += server.waitOutput()
                debug('Server output: %s\n' % servout)
                result = [ parseIperf(servout), parseIperf(cliout) ]
                if l4Type == 'UDP':
                    result.insert(0, udpBw)
                output('*** Results: %s\n' % result)
        return result
    
def parseIperf(iperfOutput):

        r = r'([\d\.]+ \w+/sec)'
        m = re.findall(r, iperfOutput)
        if m:
            return m[-1]
        else:
            # was: raise Exception(...)
            output(iperfOutput)
            return ''

if __name__ == "__main__":
    MininetRunner()
