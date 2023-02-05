#/* Patryk Marciniak s1828233 */
import sys
import socket

def receive(port):
    s = socket.socket(type=socket.SOCK_DGRAM) #creates a socket of type DGRAM for UDP connection
    s.bind(('localhost',port))
    recv_data = [] # received packages
    expected_ack = 0 # expected ack(seq no.)
    last_mes = False
    while not last_mes:
        full_recv = s.recvfrom(1027)
        data = bytearray(full_recv[0]) # current received package
        file_bytes = data[3:] # data in file - 3 header bits
        ack = data[0]*256 + data[1] # received sequence number from sender

        if data[2] == 1 and ack == expected_ack: # if last packet indicated by EOF == 1
            recv_data.append(file_bytes)
            for i in range(10):
                s.sendto(data[0:2], full_recv[1]) # send ack of last packet multiple times in case lost in transmission
            last_mes = True
            break
        
        if ack == expected_ack: # if received ack is the one expected
            recv_data.append(file_bytes)
            s.sendto(expected_ack.to_bytes(2, 'big'), full_recv[1]) # send ack to sender
            expected_ack += 1 # increment next ack so we can check if following packet arriving is correct
        else: # unexpected ack received so send the previous ack number to sender
            if expected_ack == 0:
                s.sendto(expected_ack.to_bytes(2, 'big'), full_recv[1])
            else:
                s.sendto((expected_ack - 1).to_bytes(2, 'big'), full_recv[1])
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



