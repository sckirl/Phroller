from re import I
import socket
import ipaddress
import pyvjoy
from threading import Thread
import time

class Phroller:
    def __init__(self):
        self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        
        # HOST is from user's IP address
        # PORT is from available port in the server's network
        self.HOST = socket.gethostbyname(socket.gethostname())
        self.PORT = str(ipaddress.ip_network(self.HOST)).split("/")[1]
        
        # vJoy driver
        self.vJoy = pyvjoy.VJoyDevice(1) 
        print("HOST %s | PORT %s" % (self.HOST, self.PORT))
        
    def connect(self):
        # Open the socket server, listen for any connection(s)
        self.s.bind((self.HOST, int(self.PORT)))
        self.s.listen()
    
    def readSocket(self): # Start a new thread for this function
        conn, addr = self.s.accept()
        print("Connected by", addr)
        
        # Accept connection, read the client's massages
        while True:
            data = conn.recv(1024)
            if data != b"" and len(data) <= 8:
                val = data.decode()[:-1]
                self.toJoy(val);
                
    def toJoy(self, val: str):
        buttonDict = {
            # dictionary for botton input. {"Button" : Button Value}
            "A" : 1,
            "B" : 2,
            "X" : 3,
            "Y" : 4,
            
            "TOP"  :   13,  
            "LFT"  :   15,
            "BOT"  :   14,
            "RGT"  :   16,
        }
        print(val)
        if val.isalpha():
            self.vJoy.set_button(buttonDict[val], 1)
            time.sleep(.1)
            self.vJoy.set_button(buttonDict[val], 0)
            
        elif val.isnumeric():
            # "Translate" the Y accelerometer value into controller movement
            # With vJoy as the driver
        
            # Convert val from range(-9, 9) into (0, 3200) for vjoy input
            _val = float(val) + 10
            _val = _val / 18
            _val = int(_val * 32000)
            self.vJoy.set_axis(pyvjoy.HID_USAGE_X, _val)
        
                    
if __name__ == "__main__":
    phroller = Phroller()
    
    phroller.connect()
    Thread(target=phroller.readSocket).run()