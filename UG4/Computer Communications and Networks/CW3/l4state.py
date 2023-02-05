from ryu.base import app_manager
from ryu.controller import ofp_event
from ryu.controller.handler import CONFIG_DISPATCHER, MAIN_DISPATCHER
from ryu.controller.handler import set_ev_cls
from ryu.ofproto import ofproto_v1_4
from ryu.lib.packet import packet
from ryu.lib.packet import ethernet
from ryu.lib.packet import in_proto
from ryu.lib.packet import ipv4
from ryu.lib.packet import tcp
from ryu.lib.packet.ether_types import ETH_TYPE_IP

class L4State14(app_manager.RyuApp):
    OFP_VERSIONS = [ofproto_v1_4.OFP_VERSION]

    def __init__(self, *args, **kwargs):
        super(L4State14, self).__init__(*args, **kwargs)
        self.ht = set()

    @set_ev_cls(ofp_event.EventOFPSwitchFeatures, CONFIG_DISPATCHER)
    def features_handler(self, ev):
        dp = ev.msg.datapath
        ofp, psr = (dp.ofproto, dp.ofproto_parser)
        acts = [psr.OFPActionOutput(ofp.OFPP_CONTROLLER, ofp.OFPCML_NO_BUFFER)]
        self.add_flow(dp, 0, psr.OFPMatch(), acts)

    def add_flow(self, dp, prio, match, acts, buffer_id=None):
        ofp, psr = (dp.ofproto, dp.ofproto_parser)
        bid = buffer_id if buffer_id is not None else ofp.OFP_NO_BUFFER
        ins = [psr.OFPInstructionActions(ofp.OFPIT_APPLY_ACTIONS, acts)]
        mod = psr.OFPFlowMod(datapath=dp, buffer_id=bid, priority=prio,
                                match=match, instructions=ins)
        dp.send_msg(mod)

    @set_ev_cls(ofp_event.EventOFPPacketIn, MAIN_DISPATCHER)
    def _packet_in_handler(self, ev):
        msg = ev.msg
        in_port, pkt = (msg.match['in_port'], packet.Packet(msg.data))
        dp = msg.datapath
        ofp, psr, did = (dp.ofproto, dp.ofproto_parser, format(dp.id, '016d'))
        eth = pkt.get_protocols(ethernet.ethernet)[0]
        iph = pkt.get_protocols(ipv4.ipv4) # IP header
        tcph = pkt.get_protocols(tcp.tcp) # TCP header
        out_port = 2 if in_port == 1 else 1 
        acts = [psr.OFPActionOutput(out_port)]
        if len(iph) != 0 and len(tcph) != 0: # consider only TCP over IPv4 packets
            (srcip, dstip, srcport, dstport) = (iph[0].src, iph[0].dst, tcph[0].src_port, tcph[0].dst_port) # find src & dst ip address and ports
            if in_port == 1:
                #check if legal flag combination
                if not tcph[0].has_flags(tcp.TCP_SYN, tcp.TCP_FIN) and not tcph[0].has_flags(tcp.TCP_SYN, tcp.TCP_RST) and tcph[0].bits != 0:
                    if (srcip, dstip, srcport, dstport) not in self.ht: # if key not in hashtable
                        self.ht.add((srcip, dstip, srcport, dstport)) # add key to hashtable
                        mtc = psr.OFPMatch(in_port=in_port, eth_type=ETH_TYPE_IP, ipv4_src=srcip, ipv4_dst=dstip, ip_proto=in_proto.IPPROTO_TCP, tcp_src=srcport, tcp_dst=dstport) # match
                        acts = [psr.OFPActionOutput(out_port)]
                        self.add_flow(dp, 1, mtc, acts, msg.buffer_id) # add the flow
                        if msg.buffer_id != ofp.OFP_NO_BUFFER:
                            return
                else: #if illegal flag combination drop the packet
                    acts = [psr.OFPActionOutput(ofp.OFPPC_NO_FWD)] # drop the packet
            elif in_port == 2: 
                if (dstip, srcip, dstport, srcport) not in self.ht: #if key not in hashtable
                    acts = [psr.OFPActionOutput(ofp.OFPPC_NO_FWD)] # drop the packet
                else:
                    mtc = psr.OFPMatch(in_port=in_port, eth_type=ETH_TYPE_IP, ipv4_src=srcip, ipv4_dst=dstip, ip_proto=in_proto.IPPROTO_TCP, tcp_src=srcport, tcp_dst=dstport) # match
                    acts = [psr.OFPActionOutput(out_port)]
                    self.add_flow(dp, 1, mtc, acts, msg.buffer_id) # add the flow
                    if msg.buffer_id != ofp.OFP_NO_BUFFER:
                        return
        data = msg.data if msg.buffer_id == ofp.OFP_NO_BUFFER else None
        out = psr.OFPPacketOut(datapath=dp, buffer_id=msg.buffer_id,
                               in_port=in_port, actions=acts, data=data)
        dp.send_msg(out)