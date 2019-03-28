import socket
import sys

localIP     = "0.0.0.0"
localPort   = 12777
bufferSize  = 4096

ipaddresses = [] 

msgFromServer       = "printOnScreen,128,128"
bytesToSend         = str.encode(msgFromServer)

# Create a datagram socket
UDPServerSocket = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)

# Bind to address and ip
UDPServerSocket.bind((localIP, localPort))
print("UDP server up and listening")

# Listen for incoming datagrams

while 1:
	bytesAddressPair = UDPServerSocket.recvfrom(bufferSize)
	message = bytesAddressPair[0]
	address = bytesAddressPair[1]
	clientMsg = "Message from Client:{}".format(message)
	clientIP  = "Client IP Address:{}".format(address)
	#print(clientMsg)
	#print(clientIP)
	#print(address[0])

	#STUFF FOR HOLE PUNCH 
	# let's try making a list of IPs, adding 
	# them if they're not in the list and
	# send them to the others connecting
	if address[0] in ipaddresses:
		#do nothing
		print("ip already recorded")
	else:
		ipaddresses.append(address[0])

	for x in address:
		print("x is " + x)

		#	try:
		#	UDPServerSocket.sendto(bytesToSend, (37.162.79.66, "12777"))
		#except Exception as e:
		#	print(Exception)




	# Sending a reply to client
	#UDPServerSocket.sendto(bytesToSend, address)

#except KeyboardInterrupt:
	#serverSock.close()
	#sys.exit(0)