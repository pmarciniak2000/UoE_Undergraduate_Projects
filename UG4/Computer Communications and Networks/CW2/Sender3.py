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
last_expected_ack = 0

#similar method in sender1 used to split file int 1024 byte parts
def split_packets(filename):
    global last_expected_ack

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

    # get the ack from the last packet to know when to end connection
    last_expected_ack = last_packet[0]*256 + last_packet[1]
    return pkt

def send(sock,packets,timeout,host,port,window_size):
    global current_packet, next_packet, lock, timer #these variables will be shared between the threads

    retransmissions = 0
    timer = Timer(timeout) # create a timer
    window = window_size
    
    # thread to allow sender to receive ack from receiver
    recv = Thread(target=recv_ack,args=(sock,)) 
    recv.start()
    
    # keep sending until all of the packets are sent
    while current_packet < len(packets):
        lock.acquire() # acquire lock for thread safety
        
        # while window not full send packets to receiver
        while next_packet < current_packet + window:
            sock.sendto(packets[next_packet],(host,port)) 
            if current_packet == next_packet:
                timer.get_time() # start timer for the packet
            next_packet += 1

        # wait until received ack to avoid overwhelming receiver - without this no. of retransmits will be higher as more packets dropped
        while timer.isrunning() and not timer.istimeout():
            lock.release()
            time.sleep(0.000005) 
            lock.acquire()

        # timer timed out so increment retransmission counter and resend packet
        if timer.istimeout():
            next_packet = current_packet #set the next packets seq.no to the current one so this one which was not acked is resent
            retransmissions += 1 
            timer.stop() # stop the timer for the new transmission

        #extra bit for stop-wait protocal to go back N
        #adjust window size if there are less packets left than current window size
        packets_left = len(packets) - current_packet
        if packets_left < window_size:
            window = packets_left
        lock.release()
    return retransmissions # return number of retransmissions

# receive ack
def recv_ack(sock):
    global last_expected_ack, current_packet, next_packet, lock, timer

    receiving = True # true by default when method called

    while receiving:
        data,_ = sock.recvfrom(1024) #receive data
        lock.acquire() 
        ack = int.from_bytes(data,'big') # convert data received into integer format
        current_packet = ack + 1 # increment to the next packets sequence number
        if current_packet == next_packet: # if all packet in the current window is sent, stop the timer
            timer.stop()
        if current_packet != next_packet: # if ack received but not all packets have been acked then restart timer.
            timer.stop()
            timer.get_time()
        if ack == last_expected_ack:# if the ack received is the last expected ack then end the thread
            receiving = False
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
    s.close()
