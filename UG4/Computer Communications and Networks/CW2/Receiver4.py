#/* Patryk Marciniak s1828233 */
import sys
import socket

def receive(port, window_size):
    s = socket.socket(type=socket.SOCK_DGRAM)
    s.bind(('localhost',port))
    recv_data = [] # received packages
    expected_ack = 0 # expected ack(seq no.)
    buffer = [-1] * window_size # initalise receiver buffer
    lastack = 100000000000 # initalise last ack no. to an arbitrarily high number
    last_mes = False
    while not last_mes:
        full_recv = s.recvfrom(1027)
        data = bytearray(full_recv[0]) # current received package
        file_bytes = data[3:] # data in file - 3 header bits
        ack = data[0]*256 + data[1] # received sequence number from sender

        if data[2] == 1: #if EOF is true
            lastack = ack

        if ack == expected_ack: #if received seqnumber is the first packet's seqnumber in the window

            # send seq no as ack to the sender
            s.sendto(ack.to_bytes(2,'big'), full_recv[1])

            #if current window not full, indicated by the presence of -1s
            if -1 in buffer[1:]:
                buffer_idx = buffer[1:].index(-1)   #get index of the first unreceived packet
            else: #if current window full
                buffer_idx = window_size-1
            recv_data.append(file_bytes)
            recv_data += buffer[1:buffer_idx+1] # put messages from the start of the buffer till next unreceived packet into the recv[]
            
            # create a temporary buffer which will start from the next unreceived packet
            temp_buffer = buffer[buffer_idx+1:]
            temp_buffer += ([-1] * (window_size-len(temp_buffer))) #populate the rest of the buffer with -1s
            buffer = temp_buffer
            expected_ack = ack + 1 + buffer_idx # set the expexted ack to the seq no of the first packet in the window
            if expected_ack > lastack:

                # if last packet is received, send ack of last packet multiple times in case lost in transmission
                for i in range(lastack-window_size,expected_ack):
                    for j in range(10):
                        s.sendto(i.to_bytes(2, 'big'), full_recv[1])
                last_mes = True
                break
            #continue
            
        # if received seq no is not the seq no of the first packet in the window then send the ack
        if ack != expected_ack:
            s.sendto((ack).to_bytes(2, 'big'), full_recv[1])
            # if the seq no is greater than the seq no of the first packet in the window, add packet to buffer
            if ack > expected_ack: 
                buffer[ack - expected_ack] = file_bytes
    s.close()
    return recv_data

if __name__ == '__main__':
    port = int(sys.argv[1])
    filename = sys.argv[2]
    window_size = int(sys.argv[3])
    #call method to receive data
    received = receive(port,window_size)
    f = open(filename,'wb')
    #write each part in the received data to the file
    for p in received:
        f.write(p)
    f.close()


