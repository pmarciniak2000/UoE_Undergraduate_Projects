#/* Patryk Marciniak s1828233 */
import sys
import socket
import time
import os
from threading import Thread,Lock

# custom timer class to deal with timer functionality
class Timer(object):

    def  __init__(self,timeout):
        self.timeout = timeout
        self.time = 0

    # get current time and set it to time value of timer class
    def get_time(self):
        self.time = time.time()

    #stop current timer
    def stop(self):
        self.time = 0

    #check if timer is currently running
    def isrunning(self):
        return self.time != 0

    #check is timer has exceeded timeout threshold
    def istimeout(self):
        if not self.isrunning():
            return False
        else:
            return (time.time() - self.time) > (self.timeout/1000) #convert timeout to ms
    
    #print time for testing purposes
    def print_time(self):
        print(self.time)


# declare global variables here so can be shared amongst threads
lock = Lock()
current_packet = 0
next_packet = 0

# method used to split file int 1024 byte parts
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
    # now need to change EOF flag for the last packet so receiver knows when packet finishes
    last_packet = bytearray(pkt[len(pkt)-1])
    last_packet[2] = 1 #change EOF flag
    pkt[len(pkt)-1] = last_packet

    return pkt

# Thread for sending packets
def send_pkt(sock,packet,seq_no,timeout,host,port):
    global acks_status,lock
    timer = Timer(timeout)

    sending = True
    while sending:
        # If packet not sent yet(indicated by timer not running) or the packet timed out
        if not timer.isrunning() or timer.istimeout():
            timer.stop()                          
            sock.sendto(packet, (host, port))  # send packet, then restart timer
            timer.get_time()
        lock.acquire()
        if not acks_status[seq_no]: #if the current packet is not acked, wait
            lock.release()
            time.sleep(0.00001)
        else: #if the packet is acked, release the thread and end loop
            lock.release()
            sending = False



def send(sock,packets,timeout,host,port,window_size):
    global acks_status, current_packet, next_packet, lock

    window = window_size
    acks_status = [0]*len(packets) # initialise array to keep track of received acks 0 = False, 1 = True
    
    # thread to allow sender to receive ack from receiver
    recv = Thread(target=recv_ack,args=(sock,))
    recv.start()

    # keep sending until all of the packets are sent
    while current_packet < len(packets):
        lock.acquire()
        
        # while window not full, start thread which will send packets to receiver
        while next_packet < current_packet + window:
            sendp = Thread(target=send_pkt,args=(sock,packets[next_packet],next_packet,timeout,host,port))
            sendp.start()
            next_packet += 1

        #wait for the current packet to be acked
        while not acks_status[current_packet]:
            lock.release()
            time.sleep(0.00001)
            lock.acquire()

        current_packet += 1 # increment current packet seq no

        # adjust window size if there are less packets left than current window size
        pktleft = len(packets) - current_packet
        if pktleft < window_size: #if the number of left packets is smaller than the window
            window = pktleft #set the window size to the number of remaining packets to prevent indexOutOfRange
        lock.release()

#Thread to receive ACKs
def recv_ack(sock):
    global acks_status, lock

    # keep receiving acks until all have been received
    while not all(acks_status):
        data,_ = sock.recvfrom(1024)
        ack = int.from_bytes(data,'big')
        lock.acquire()
        acks_status[ack] = 1 # set ack status to true when ack received
        lock.release()



if __name__ == "__main__":
    s = socket.socket(type=socket.SOCK_DGRAM)
    packets = split_packets(sys.argv[3])
    remotehost = sys.argv[1]
    port = int(sys.argv[2])
    timeout = int(sys.argv[4])
    window_size = int(sys.argv[5])

    start_tmr = time.time() # get time at start of transmitting
    retransmits = send(s,packets,timeout,remotehost,port,window_size)
    end_tmr = time.time() # get time at end of transmitting
    file_size = os.path.getsize(sys.argv[3]) # get file size
    throughput = (file_size / 1024) / (end_tmr-start_tmr) # throughput = filesize/transfer time
    output = str(round((throughput),2)) # format output string as required
    print(output)
