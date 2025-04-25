import network
import socket
import machine
import time

# Setup Wi-Fi
ssid = '<<Your_WiFi_SSID>>'  # enter actual Wi-Fi SSID
password = '<<Your_WiFi_Password>>'  # enter actual Wi-Fi password

# GPIO setup
led_pin = machine.Pin(2, machine.Pin.OUT)  # change the GPIO pin

# customize default state
led_pin.value(1)
state = 'ON'
# led_pin.value(0)
# state = 'OFF'


# Connect to Wi-Fi
wlan = network.WLAN(network.STA_IF)
wlan.active(True)
wlan.connect(ssid, password)

print("Connecting to Wi-Fi...")
while not wlan.isconnected():
    time.sleep(0.5)
print("Connected:", wlan.ifconfig())

# Start the socket server
addr = socket.getaddrinfo('0.0.0.0', 80)[0][-1]
s = socket.socket()
s.bind(addr)
s.listen(1)

print('Listening on', addr)

# Main server loop
while True:
    try:
        cl, addr = s.accept()
        print('Client connected from', addr)
        request = cl.recv(1024)
        request_str = request.decode('utf-8')
        print('Request:', request_str)

        # Basic parsing
        path_line = request_str.split('\r\n')[0]
        path = path_line.split(' ')[1]
        print('Path:', path)

        # Handle commands
        if path == '/on':
            led_pin.value(1)
            state = 'ON'
            response = 'GPIO2 is ON\n'
        elif path == '/off':
            led_pin.value(0)
            state = 'OFF'
            response = 'GPIO2 is OFF\n'
        elif path == '/status':
            response = f'GPIO2 is {state}\n'
        else:
            response = 'Unknown command\n'

        cl.send('HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n')
        cl.send(response)
        cl.close()

    except Exception as e:
        print('Error:', e)

