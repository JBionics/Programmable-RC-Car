////////////////////////////////////////////////////////////////////////////////////////////////
//    Copyright (C) 2012 Jon Bennett
//
//    This file is part of Programmable RC Car Controller.
//
//    http://www.jbprojects.net/articles/programmable-rc/
//
//    Programmable RC Car Controller is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Programmable RC Car Controller is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Programmable RC Car Controller.  If not, see <http://www.gnu.org/licenses/>.
////////////////////////////////////////////////////////////////////////////////////////////////

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 

// This class is responsible for handling the serial port communication
public class RCCom implements SerialPortEventListener {
    
    SerialPort serialPort;
    
    // Buffered input stream from the port
    private InputStream input;
    
    // The output stream to the port
    private OutputStream output;
    
    // Milliseconds to block while waiting for port open
    private static final int TIME_OUT = 2000;
    
    // Default bits per second for COM port.
    private static final int DATA_RATE = 9600;
    
    // Returns a list of available serial port names
    public String[] getPortNames()
    {
        Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
        
        int numPorts = 0;
        while (portEnum.hasMoreElements()) {
            portEnum.nextElement();
            // LAME.  Don't like having to iterate through twice, though that's probably
            // more efficient than using an ArrayList.
            numPorts++;
        }
        
        String[] ports = new String[numPorts];
        portEnum = CommPortIdentifier.getPortIdentifiers();
        
        for (int i = 0; i < numPorts; i++) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            ports[i] = currPortId.getName();
            System.out.println("Found Port: " + ports[i]);
        }       
        
        return ports;
    }

    // Opens a serial port
    // Returns an error message on failure, null on successful connection
    public String connect(String portName)
    {
        CommPortIdentifier portId = null;
        Enumeration<?> portEnum;
        
        try {
            portEnum = CommPortIdentifier.getPortIdentifiers();
        } catch (Exception e) {
            return e.getMessage();                          
        }

        // Iterate through list of serial ports.  Only continue if there is a match.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if (currPortId.getName().equals(portName)) {
                portId = currPortId;
                break;
            }
        }

        if (portId == null) {
            return "Could not find COM port.";
        }

        try {
            // Close port if it was previously open
            close(); 
            
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            // open the streams
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            // Return an error message if port can't be opened
            return e.toString();
        }
        return null; // No errors
    }
    
    // Close the port
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }
    
    // Handle serial port event
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            // Write data to System.out
            try {
                int available = input.available();
                byte chunk[] = new byte[available];
                input.read(chunk, 0, available);
                System.out.print(new String(chunk));
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you could consider the other ones.
    }

    // Send data to the opened serial port
    public synchronized void sendData(byte[] array)
    {
        if (array != null && output != null) {
            try {
                output.write(array);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
