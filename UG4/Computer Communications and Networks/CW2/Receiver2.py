#/* Patryk Marciniak s1828233 */
import sys
import socket

# method to receive the bytes of the file
def receive(port):
    s = socket.socket(type=socket.SOCK_DGRAM) #creates a socket of type DGRAM for UDP connection
    s.bind(('localhost',port))
    recv_data = [] # received packages
    seqnos = [] # keep track of received seq no.s to avoid duplicate retransmissions
    last_mes = False
    while not last_mes:
        full_recv = s.recvfrom(1027)
        data = bytearray(full_recv[0]) # byte data of current package received
        file_bytes = data[3:] # data in file - 3 header bits
        
        #if seq no. has not been retransmitted then add to list of sequence no's
        if (data[0],data[1]) not in seqnos: 
            recv_data.append(file_bytes)
            seqnos.append((data[0],data[1]))

        if data[2] == 1: # if last packet indicated by EOF == 1
            for i in range(10):# send ack of last packet multiple times in case lost in transmission
                s.sendto(data[0:2], full_recv[1])
            last_mes = True
        s.sendto(data[0:2],full_recv[1])# send seq no. to sender as the ack
    s.close()
    return recv_data

if __name__ == '__main__':
    port = int(sys.argv[1])
    filename = sys.argv[2]
    #call method to receive data
    received = receive(port)
    f = open(filename,'wb')
    #write each part in the received data to the file
    for p in received:
        f.write(p)
    f.close()


