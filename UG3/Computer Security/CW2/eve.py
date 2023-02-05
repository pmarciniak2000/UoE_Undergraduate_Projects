import sys
import os
import getpass

from common import *
from const import *

flag = sys.argv[1]

if flag == '--relay':
    # Connect with alice as bob
    dialog = Dialog('print')
    socketEA, aesEA = setup("bob", BUFFER_DIR, BUFFER_FILE_NAME)
    
    # Rename buffer for next connection
    os.rename(BUFFER_DIR + BUFFER_FILE_NAME, BUFFER_DIR + "buffer2")
    
    # Connect with bob as alice
    dialog = Dialog('print')
    socketEB, aesEB = setup("alice", BUFFER_DIR, BUFFER_FILE_NAME)

    # Decrypt bobs message to alice and forward it to alice
    dialog.info("Waiting for Bobs message...")
    receivedB = receive_and_decrypt(aesEB,socketEB)
    dialog.chat('Bob originally said: "{}"'.format(receivedB))
    
    # Forward Bobs message to Alice
    encrypt_and_send(receivedB, aesEA, socketEA)

    # Decrypt alices message to bob and forward it to bob
    dialog.info("Waiting for Alices message...")
    receivedA = receive_and_decrypt(aesEA,socketEA)
    dialog.chat('Alice originally said: "{}"'.format(receivedA))
    
    # Forward Alice's message to Bob
    encrypt_and_send(receivedA, aesEB, socketEB)

    dialog.think("Pretend i'm not here, carry on with your conversations")


elif flag == '--break-heart':
    # Connect with alice as bob
    dialog = Dialog('print')
    socketEA, aesEA = setup("bob", BUFFER_DIR, BUFFER_FILE_NAME)
    
    # Rename buffer for next connection
    os.rename(BUFFER_DIR + BUFFER_FILE_NAME, BUFFER_DIR + "buffer2")

    # Connect with bob as alice
    dialog = Dialog('print')
    socketEB, aesEB = setup("alice", BUFFER_DIR, BUFFER_FILE_NAME)

    # Decrypt Bob's original message
    dialog.info("Waiting for Bobs message...")
    receivedB = receive_and_decrypt(aesEB,socketEB)
    dialog.chat('Bob originally said: "{}"'.format(receivedB))

    # Send break up message to Alice
    encrypt_and_send(BAD_MSG["bob"], aesEA, socketEA)

    # Decrypt Alice's original message
    dialog.info("Waiting for Alices message...")
    receivedA = receive_and_decrypt(aesEA,socketEA)
    dialog.chat('Alice originally said: "{}"'.format(receivedA))

    # Send break up message to Bob 
    # or just forward Alice's reply as it's also bad since she thinks bob broke up with her
    encrypt_and_send(BAD_MSG['alice'], aesEB, socketEB)

    dialog.think("Your relationship is over!")


    # encrypt break up messages and send to bob and alice 
elif flag == '--custom':
    # Connect with alice as bob
    dialog = Dialog('print')
    socketEA, aesEA = setup("bob", BUFFER_DIR, BUFFER_FILE_NAME)
    
    # Rename buffer for next connection
    os.rename(BUFFER_DIR + BUFFER_FILE_NAME, BUFFER_DIR + "buffer2")

    # Connect with bob as alice
    dialog = Dialog('print')
    socketEB, aesEB = setup("alice", BUFFER_DIR, BUFFER_FILE_NAME)

    # Decrypt Bob's original message
    dialog.info("Waiting for Bobs message...")
    receivedB = receive_and_decrypt(aesEB,socketEB)
    dialog.chat('Bob originally said: "{}"'.format(receivedB))

    #ask for input, encrypt message and send to Alice
    dialog.prompt('Please input message for Alice...')
    to_send = input()
    encrypt_and_send(to_send, aesEA, socketEA)

    # Decrypt Alice's original message
    dialog.info("Waiting for Alices message...")
    receivedA = receive_and_decrypt(aesEA,socketEA)
    dialog.chat('Alice originally said: "{}"'.format(receivedA))

    #ask for input, encrypt message and send to Bob
    dialog.prompt('Please input message for Bob...')
    to_send = input()
    encrypt_and_send(to_send, aesEB, socketEB)
else:
    print('input must be either: --relay, --break-heart, or --custom')

tear_down(socketEB, BUFFER_DIR, BUFFER_FILE_NAME)
tear_down(socketEA, BUFFER_DIR , "buffer2")



