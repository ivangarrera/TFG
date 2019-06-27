package com.example.ivangarrera.example.Controller;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothConnectionThread extends Thread {
    private final InputStream input_bytes;
    private final OutputStream output_bytes;
    private android.os.Handler handler;

    public BluetoothConnectionThread(BluetoothSocket socket, Handler handler) {
        InputStream tmp_input_bytes = null;
        OutputStream tmp_output_bytes = null;
        this.handler = handler;

        try {
            tmp_input_bytes = socket.getInputStream();
            tmp_output_bytes = socket.getOutputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        input_bytes = tmp_input_bytes;
        output_bytes = tmp_output_bytes;
    }

    public void run() {
        byte[] buffer = new byte[256];  // Buffer to store the incoming bytes
        int number_of_incoming_bytes;   // Number of bytes that have been read

        // This thread is going to be always waiting for bluetooth data
        while (true) {
            try {
                number_of_incoming_bytes = input_bytes.read(buffer);
                // Notify ReceiveBTMessageHandler that a new message has arrived
                handler.obtainMessage(1, number_of_incoming_bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }

    // This function is used to write data to bluetooth socket
    public void write(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            output_bytes.write(msgBuffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
