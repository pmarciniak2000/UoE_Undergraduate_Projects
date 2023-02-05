from scapy.utils import RawPcapReader
from scapy.layers.l2 import Ether
from scapy.layers.inet import IP
from ipaddress import ip_address, ip_network
import sys
import matplotlib.pyplot as plt

class Node(object):
    def __init__(self, ip, plen):
        self.bytes = plen
        self.left = None
        self.right = None
        self.ip = ip
    def add(self, ip, plen):
        if self.ip > ip:
            if self.left is None: #if branch empty then add new node
                self.left = Node(ip,plen)
            else:                 #if branch not empty call add method
                self.left.add(ip,plen)
        elif self.ip < ip:
            if self.right is None:
                self.right = Node(ip, plen)
            else:
                self.right.add(ip,plen)
        else:                     #if ip exists in tree then add the bytes
            self.bytes += plen
    def data(self, data):
        if self.left:
            self.left.data(data)
        if self.bytes > 0:
            data[ip_network(self.ip)] = self.bytes
        if self.right:
            self.right.data(data)
    @staticmethod 
    def supernet(ip1, ip2):
        # arguments are either IPv4Address or IPv4Network
        na1 = ip_network(ip1).network_address
        na2 = ip_network(ip2).network_address
        addr1 = '{0:32b}'.format(int(na1)) #convert net addresses to 32bit format
        addr2 = '{0:32b}'.format(int(na2))
        prefix = addr1
        #find common prefix between the two addresses
        i=0
        while i < len(addr1): #or just <32 since addr1 will always be 32bits
            if addr1[i] != addr2[i]:
                prefix = addr1[:i]
                break
            i+=1
        netmask = len(prefix)
        prefix = prefix + '0' * (32-netmask) #fill up remaining bits of prefix with 0s
        na1 = ip_address(int(prefix,2)) #convert prefix to base 2 int
        return ip_network('{}/{}'.format(na1, netmask), strict=False)
    
    def aggr(self, byte_thresh):
        # left branch exists 
        if self.left is not None:
            if self.left.left is not None or self.left.right is not None:
                self.left.aggr(byte_thresh)
            if self.left.left is None and self.left.right is None:
                if self.left.bytes < byte_thresh:
                    self.bytes += self.left.bytes
                    self.ip = self.supernet(self.ip,self.left.ip) #use supernet method to find IP
                    self.left = None
            else:
                if self.left.bytes < byte_thresh:
                    self.bytes += self.left.bytes
                    self.ip = self.supernet(self.ip, self.left.ip)
                    self.left.bytes = 0

        # right branch exists 
        if self.right is not None:
            if self.right.left is not None or self.right.right is not None:
                self.right.aggr(byte_thresh)
            if self.right.left is None and self.right.right is None:
                if self.right.bytes < byte_thresh:
                    self.bytes += self.right.bytes
                    self.ip = self.supernet(self.ip, self.right.ip)
                    self.right = None
            else:
                if self.right.bytes < byte_thresh:
                    self.bytes += self.right.bytes
                    self.ip = self.supernet(self.ip, self.right.ip)
                    self.right.bytes = 0
            
class Data(object):
    def __init__(self, data):
        self.tot_bytes = 0
        self.data = {}
        self.aggr_ratio = 0.05
        root = None
        cnt = 0
        for pkt, metadata in RawPcapReader(data):
            ether = Ether(pkt)
            if not 'type' in ether.fields:
                continue
            if ether.type != 0x0800:
                continue
            ip = ether[IP]
            self.tot_bytes += ip.len
            if root is None:
                root = Node(ip_address(ip.src), ip.len)
            else:
                root.add(ip_address(ip.src), ip.len)
            cnt += 1
        root.aggr(self.tot_bytes * self.aggr_ratio)
        root.data(self.data)
    def Plot(self):
        data = {k: v/1000 for k, v in self.data.items()}
        plt.rcParams['font.size'] = 8
        fig = plt.figure()
        ax = fig.add_subplot(1, 1, 1)
        ax.grid(which='major', axis='y')
        ax.tick_params(axis='both', which='major')
        ax.set_xticks(range(len(data)))
        ax.set_xticklabels([str(l) for l in data.keys()], rotation=45,
            rotation_mode='default', horizontalalignment='right')
        ax.set_ylabel('Total bytes [KB]')
        ax.bar(ax.get_xticks(), data.values(), zorder=2)
        ax.set_title('IPv4 sources sending {} % ({}KB) or more traffic.'.format(
            self.aggr_ratio * 100, self.tot_bytes * self.aggr_ratio / 1000))
        plt.savefig(sys.argv[1] + '.aggr.pdf', bbox_inches='tight')
        plt.close()
    def _Dump(self):
        with open(sys.argv[1] + '.aggr.data', 'w') as f:
            f.write('{}'.format({str(k): v for k, v in self.data.items()}))

if __name__ == '__main__':
    d = Data(sys.argv[1])
    d.Plot()
    d._Dump()