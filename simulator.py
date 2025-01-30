#!/usr/bin/env python3

import binascii
import socket
import sys
import random
from threading import Thread
from time import sleep

random.seed(42)

def get_mode_from_user():
    print("Select the mode for telemetry data transmission:")
    print("1. Constant - Fixed data packets at fixed intervals")
    print("2. Variable-Value - Data packets with random value adjustments at fixed intervals")
    print("3. Variable-Time - Data packets with random value adjustments at random intervals")
    choice = input("Enter 1, 2, or 3 to select a mode: ")
    if choice == '1':
        return 'constant'
    elif choice == '2':
        return 'variable-value'
    elif choice == '3':
        return 'variable-time'
    else:
        print("Invalid choice. Defaulting to Constant mode.")
        return 'constant'

def send_tm(simulator):
    tm_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    exampleBytes = bytearray([
        0xA3, 0x5F, 0xC2, 0x7D, 0x48, 0x9E, 0x34, 0xAB,
        0x7E, 0xD9, 0xF1, 0x63, 0x2C, 0x8A, 0x4F, 0x92,
        0x37, 0xE4, 0x5D, 0x86, 0x3A, 0xBE, 0x99, 0xCD,
        0x22, 0xF8, 0x13, 0xA9, 0x76, 0xE1, 0x44, 0x5F,
        0xD3, 0x8C, 0x21, 0x6E, 0xA7, 0x94, 0xB8, 0x35,
        0xDF, 0x09, 0xAC, 0x52, 0x71, 0x6B, 0xE3, 0x14,
        0xC6, 0x8F, 0x5A, 0xD2, 0x47, 0x1B, 0xE8, 0x92,
        0x39, 0xAF, 0x6D, 0xC4, 0x28, 0xF7, 0x13, 0x9B,
        0x6E, 0x84, 0xD5, 0x72, 0x4A, 0x3E, 0x9C, 0x81,
        0x6F, 0x57, 0xAC, 0x39, 0xE4, 0x98, 0x52, 0x7F,
        0xB3, 0x4D, 0xC8, 0x25, 0x6A, 0xF1, 0x93, 0x7E,
        0x42, 0xDF, 0x18, 0xB6, 0x3C, 0xF0, 0xA0, 0xF8,
        0x42, 0xDF, 0x18, 0xB6, 0x3C, 0xF0, 0xA0, 0xF8,
        0x42, 0xDF, 0x18, 0xB6, 0x3C, 0xF0, 0xA0, 0xF8,
        0x42, 0xDF, 0x18, 0xB6, 0x3C, 0xF0, 0xA0, 0xF8
    ])

    while True:
        if simulator.mode == 'constant':
            data_to_send = exampleBytes
        elif simulator.mode == 'variable-value':
            data_to_send = bytearray((byte + random.randint(-5, 5)) % 256 for byte in exampleBytes)
        elif simulator.mode == 'variable-time':
            data_to_send = bytearray((byte + random.randint(-5, 5)) % 256 for byte in exampleBytes)
            sleep(random.uniform(0.2, 2))
        else:
            data_to_send = exampleBytes

        tm_socket.sendto(data_to_send, ('127.0.0.1', 10015))
        sleep(1)
        simulator.tm_counter += 1

        if simulator.mode != 'variable-time':
            sleep(1)

def receive_tc(simulator):
    tc_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    tc_socket.bind(('127.0.0.1', 10025))
    while True:
        data, _ = tc_socket.recvfrom(4096)
        simulator.last_tc = data
        simulator.tc_counter += 1

class Simulator():
    def __init__(self, mode='constant'):
        self.tm_counter = 0
        self.tc_counter = 0
        self.tm_thread = None
        self.tc_thread = None
        self.last_tc = None
        self.mode = mode

    def start(self):
        self.tm_thread = Thread(target=send_tm, args=(self,))
        self.tm_thread.daemon = True
        self.tm_thread.start()
        self.tc_thread = Thread(target=receive_tc, args=(self,))
        self.tc_thread.daemon = True
        self.tc_thread.start()

    def print_status(self):
        cmdhex = None
        if self.last_tc:
            cmdhex = binascii.hexlify(self.last_tc).decode('ascii')
        return 'Mode: {} | Sent: {} packets | Received: {} commands | Last command: {}'.format(
            self.mode, self.tm_counter, self.tc_counter, cmdhex)

if __name__ == '__main__':
    selected_mode = get_mode_from_user()
    simulator = Simulator(mode=selected_mode)
    simulator.start()

    try:
        prev_status = None
        while True:
            status = simulator.print_status()
            if status != prev_status:
                sys.stdout.write('\r')
                sys.stdout.write(status)
                sys.stdout.flush()
                prev_status = status
            sleep(0.5)
    except KeyboardInterrupt:
        sys.stdout.write('\n')
        sys.stdout.flush()