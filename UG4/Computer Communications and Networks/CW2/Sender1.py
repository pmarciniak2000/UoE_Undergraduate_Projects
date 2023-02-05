#/* Patryk Marciniak s1828233 */
import sys
import socket
import time

def split_packets(filename):
    i = 0
    pkt = []
    # read file in 1024 byte parts
    with open(filename,'rb') as f:
        img = f.read(1024)
        while img:
            # add 3 header bytes to the start of each packet 
            # 2 bits are seq no and 1 is EOF flag
            seq_no = i.to_bytes(2,'big')
            hdr = seq_no + b'\x00'
            hdr = hdr + img
            pkt.append(hdr)
            img = f.read(1024)
            i+=1
    f.close()
    # now need to chenge EOF flag for the last packet so receiver knows when packet finishes
    last_packet = bytearray(pkt[len(pkt)-1])
    last_packet[2] = 1 #change EOF flag
    pkt[len(pkt)-1] = last_packet
    return pkt

if __name__ == "__main__":
    s = socket.socket(type=socket.SOCK_DGRAM)
    packet = split_packets(sys.argv[3])
    remotehost = sys.argv[1]
    port = int(sys.argv[2])
    for pkt in packet:
        s.sendto(pkt,(remotehost,port))
        time.sleep(0.01) #small delay between sending packets
    s.close()

