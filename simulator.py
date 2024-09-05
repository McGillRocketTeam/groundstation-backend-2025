#!/usr/bin/env python3

import binascii
import io
import socket
import sys
from struct import unpack_from
from threading import Thread
from time import sleep


def send_tm(simulator):
    tm_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    with io.open('testdata.ccsds', 'rb') as f:
        simulator.tm_counter = 1
        header = bytearray(6)
        while f.readinto(header) == 6:
            (len,) = unpack_from('>H', header, 4)

            packet = bytearray(len + 7)
            f.seek(-6, io.SEEK_CUR)
            f.readinto(packet)




            exampleBytes = bytearray(b'\xccc\x1e,_^"5\x00\xa8\xa8\xd6R.\xc0G[\x04\xe5\x84\xc2\x1ed\xb2{E\x87AG\x94\x94"\xfa>\xde\x12R\xf3\xb5\x10o\xe3\x85\xa8\xa9D\x96e`\xb40>\x02\x16\x91\xca4\xcd\x89\xd6\x1e\xe4:\x7fhf\xc5\xf3\xe9t\x98\xd2\xacc\xb5.H\xb6\xf4B\n\x97i<&\xf0]\xaf\x07&\xe7T.\xe7\xd1\x89\xe4\xf2\xc6\xe5\x7f\x0e\x92\xfd\xfe\xfa3\xf0.\x96\x86')


            tm_socket.sendto(exampleBytes, ('127.0.0.1', 10015))
            simulator.tm_counter += 1

            sleep(1)


def receive_tc(simulator):
    tc_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    tc_socket.bind(('127.0.0.1', 10025))
    while True:
        data, _ = tc_socket.recvfrom(4096)
        simulator.last_tc = data
        simulator.tc_counter += 1


class Simulator():

    def __init__(self):
        self.tm_counter = 0
        self.tc_counter = 0
        self.tm_thread = None
        self.tc_thread = None
        self.last_tc = None

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
        return 'Sent: {} packets. Received: {} commands. Last command: {}'.format(
                         self.tm_counter, self.tc_counter, cmdhex)


if __name__ == '__main__':
    simulator = Simulator()
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
