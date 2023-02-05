#/* Patryk Marciniak s1828233 */
import sys
import socket

# method to receive the bytes of the file
def receive(port):
    s = socket.socket(type=socket.SOCK_DGRAM) #creates a socket of type DGRAM for UDP connection
    s.bind(('localhost',port))
    recv_data = []
    last_mes = False
    #keep receiving until last message received
    while not last_mes:
        data = list(s.recvfrom(1027)[0])
        file_bytes = bytes(data[3:]) #ignore first 3 header bytes
        recv_data.append(file_bytes)
        if data[2] == 1:
            last_mes = True
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


