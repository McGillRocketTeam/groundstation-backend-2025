import socket
import threading
HOST = '192.168.8.248'
PORT = 9000
def handle_input(conn):
    try:
        while True:
            user_input = input()
            if user_input.strip() and conn:
                try:
                    conn.sendall(user_input.encode() + b'\n')
                except (BrokenPipeError, OSError):
                    break
    except EOFError:
        pass  # For Ctrl+D / terminal close
try:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        server_socket.bind((HOST, PORT))
        server_socket.listen()
        print(f"Listening on {HOST}:{PORT}... (Press Ctrl+C to stop)")
        while True:
            conn, addr = server_socket.accept()
            print(f"Connected by {addr}")
            # Start a thread to read from terminal
            input_thread = threading.Thread(target=handle_input, args=(conn,), daemon=True)
            input_thread.start()
            try:
                while True:
                    data = conn.recv(1024)
                    if not data:
                        print("Client disconnected.")
                        break
                    print("Received:", data)
            except (ConnectionResetError, BrokenPipeError):
                print("Connection closed unexpectedly.")
except KeyboardInterrupt:
    print("\nServer interrupted by user. Shutting down gracefully.")