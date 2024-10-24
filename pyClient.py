from pymycobot.mycobot import MyCobot
import queue
import threading
import socket
import time
import re


host='192.168.1.66' #Host Name
port = 9876    #Port Number
queue = queue.Queue(maxsize = 1)
currentState = ""
emotion = ""
delimeter = r'[,'']'
level = 0.0
def receiveData() :

	global speed
	global emotion
	global level

	while True :
		s=socket.socket()
		s.connect((host,port))
		msgrecv = s.recv(1024)
		msgrecv = str(msgrecv)
		if msgrecv != "b''" :
			print(msgrecv)
			if queue.full() :
				queue.get()
			split = msgrecv.split(",")
			level_lock.acquire()
			level_str = split[1][:len(split[1])-1]
			level = float(level_str)
			level_lock.release()
			print("Level: ", level)
			if msgrecv.__contains__('Frustration') :
				queue.put("Frustration")
				emotion = "Frustration"
			else : 
				queue.put("Focused")
				emotion = "Focused"

def LED() :

	global speed
	global speed_lock
	global emotion
	global level

	while True :
		
		if emotion == "Frustration" :
			mc.set_color(0,0,0)
			if level <= 0.3 :
				speed = 35
			elif level >= 0.4 and level < 0.7 :
				speed = 10
			else :
				mc.send_angles([0,0,0,0,0,0],5)

		else :
			speed_lock.acquire()
			mc.set_color(0,255,0)
			if level >= 0.5 :
				speed = 40
			else :
				speed = 30
		if (level < 0.7 ) :
			mc.send_angles([(-0.43),(-82.52),40,0.7,0.26,0.26],speed)
			time.sleep(1.0)
			mc.send_angles([(-0.43),(-82.52),(-40),0.7,0.26,0.26],speed)


if __name__ == '__main__' :
	mc = MyCobot('/dev/ttyAMA0', 1000000)   
    #Initialize global variables and locks
	speed_lock = threading.Lock()
	level_lock = threading.Lock()
	mc.send_angles([0,0,0,0,0,0],30)

   #Start Threads
	t1 = threading.Thread(target = receiveData, args=())
	t2 = threading.Thread(target = LED, args=())
	t1.start()
	t2.start()
