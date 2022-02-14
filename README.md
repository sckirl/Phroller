# Phroller

A Java-python program to (presumably) control your computer with controller-like interface, using your phone.
This program uses socket connection from your computer (as the server) to your android device.

By controlling your phone like a steering wheel, you can control your controller's Y axis, and by pressing the buttons, you will be able to control them like a xbox controller.
(Not sure if the controller is 1:1 with xbox, I dont have a controller, I just test them with https://devicetests.com/controller-tester, turns out the program is VERY laggy).

## To use 
- Make sure USB debugging is on for your android device, plug USB to your computer
- Run the java Program
- *IMPORTANT* download vjoy (https://sourceforge.net/projects/vjoystick/) for the python program to function.
- From your computer, run the python code (Phroller.py) and you will open a new server there
- Enter the server's IP and port into your android app. Try to test it (recommendation: https://devicetests.com/controller-tester)
- Get disappointed by how laggy it is
- Leave
- Profit(?)

## Bugs
- Lag from phone to controller connection
- Constantly opening a new thread at OnSensorChanged() (cause of lag)
- Some received string values might get stuck together, creating unwanted values. E.g "7.50\n5.00\n" instead of "7.50\n"
- Lag
- LAG

## Notes
- This is my first android app
- This is my first time learning socket connection
- This is my first time connecting two languages together
- I rarely do java projects, no experience in java's Threads
- Most of the things here were copy-pasted, no surprise there
- I regret making this stuff.
