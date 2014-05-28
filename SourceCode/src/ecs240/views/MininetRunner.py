import os

from mininet.log import info, error, debug, output, setLogLevel
from mininet.net import Mininet, VERSION
from mininet.node import Controller, RemoteController, NOX, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, OVSSwitch, UserSwitch
from mininet.link import TCLink, Intf, Link

print 'running against MiniNet ' + VERSION

class MininetRunner(object):

    "A simple network editor for Mininet."

    def __init__(self, parent=None):
        self.defaultIpBase = '10.0.0.0/8'
        self.appPrefs = {
                       "ipBase": self.defaultIpBase
                       }
        
        self.appName = 'MiniEdit'
        self.nodes = {}
        self.net = None
        self.fd = None
        
    def buildNodes(self):
        self.fd = open('/home/yixin/info.txt', 'r')
        print("*****buiding nodes")
        for line in self.fd:    
            if "node" in line:
                name=line[5:-1]
                if "switch" in line:
                    print "adding " + name
                    h = self.net.addSwitch(name, cls=UserSwitch)
                    print "h.name:" + h.name
                elif "host" in line:
                    print "adding " + name
                    h = self.net.addHost(name, cls=Host)
                    print "h.name:" + h.name
                elif "controller" in line:
                    print "adding " + name
                    h = self.net.addController(name, cls=Controller)
                    print "h.name:" + h.name
                self.nodes[name] = h
        self.fd.close()
        print self.nodes.items()

    def buildLinks(self):
        self.fd = open('/home/yixin/info.txt', 'r')
        print("*****buiding links")
        for line in self.fd:
            if "link" in line:
                commapos = line.find(':')
                semipos = line.find(';')
                srckey=line[commapos + 1:semipos ]
                dstkey=line[semipos + 1:-1]
                src = self.nodes[srckey]
                print("src:" + src.name)
                dst = self.nodes[dstkey]
                print("dst:" + dst.name)
                self.net.addLink(node1=src, node2=dst, cls=TCLink)
        self.fd.close()

    def build(self):
        print "Build network based on topology."
        self.net = Mininet(topo=None,
                       listenPort=None,
                       build=False,
                       ipBase=self.appPrefs['ipBase'])                

        self.buildNodes()
        self.buildLinks()
        # Build network (we have to do this separately at the moment )
        self.net.build()
        self.net.start()
        return 
    
    def start(self):
        pass
            
            
if __name__ == '__main__':
    setLogLevel('info')
    app = MininetRunner()
    app.build()
            
            
            
