from scapy.utils import RawPcapReader
from scapy.layers.l2 import Ether
from scapy.layers.inet import IP, TCP
from scapy.layers.inet6 import IPv6
from ipaddress import ip_address, IPv6Address
from socket import IPPROTO_TCP
import sys
import matplotlib.pyplot as plt

class Flow(object):
    def __init__(self, data):
        self.pkts = 0
        self.flows = 0
        self.ft = {}
        for pkt, metadata in RawPcapReader(data):
            self.pkts += 1
            ether = Ether(pkt)
            if ether.type == 0x86dd:
                ip = ether[IPv6]
                # ignore packets w/o TCP header or those that indicate TCP but dont have TCP layer
                if ip.nh != 6 or (ip.nh == 6 and not ip.haslayer(TCP)): 
                    continue
                src_ip = int(IPv6Address(ip.src))
                dst_ip = int(IPv6Address(ip.dst))
            elif ether.type == 0x0800:
                ip = ether[IP]
                if ip.proto != 6 or (ip.proto == 6 and not ip.haslayer(TCP)):
                    continue
                src_ip = int(ip_address(ip.src))
                dst_ip = int(ip_address(ip.dst))
            tcp = ip[TCP]
            sport = tcp.sport
            dport = tcp.dport
            
            # go through if each case if tuple not in dictionary then create an entry for it it, 
            # if it is then add the bytes transferred for each flow
            if (src_ip, dst_ip, sport, dport) not in self.ft.keys():
                if (dst_ip, src_ip, dport, sport) not in self.ft.keys():
                    if ip.version == 4:
                        self.ft[(src_ip, dst_ip, sport, dport)] = ip.len - (ip.ihl + ip.dataofs) * 4
                        continue
                    if ip.version == 6:
                        self.ft[(src_ip, dst_ip, sport, dport)] = ip.plen - ip.dataofs * 4
                        continue
                if (dst_ip, src_ip, dport, sport) in self.ft.keys():
                    if ip.version == 4:
                        self.ft[(dst_ip, src_ip, dport, sport)] += ip.len - (ip.ihl + ip.dataofs) * 4
                        continue
                    if ip.version == 6:
                        self.ft[(dst_ip, src_ip, dport, sport)] += ip.plen - ip.dataofs * 4
                        continue
            if (src_ip, dst_ip, sport, dport) in self.ft.keys():
                if ip.version == 4:
                    self.ft[(src_ip, dst_ip, sport, dport)] += ip.len - (ip.ihl + ip.dataofs) * 4
                    continue
                if ip.version == 6:
                    self.ft[(src_ip, dst_ip, sport, dport)] += ip.plen - ip.dataofs * 4
                    continue
    def Plot(self):
        topn = 100
        data = [i/1000 for i in list(self.ft.values())]
        data.sort()
        data = data[-topn:]
        fig = plt.figure()
        ax = fig.add_subplot(1,1,1)
        ax.hist(data, bins=50, log=True)
        ax.set_ylabel('# of flows')
        ax.set_xlabel('Data sent [KB]')
        ax.set_title('Top {} TCP flow size distribution.'.format(topn))
        plt.savefig(sys.argv[1] + '.flow.pdf', bbox_inches='tight')
        plt.close()
    def _Dump(self):
        with open(sys.argv[1] + '.flow.data', 'w') as f:
            f.write('{}'.format(self.ft))

if __name__ == '__main__':
    d = Flow(sys.argv[1])
    d.Plot()
    d._Dump()
