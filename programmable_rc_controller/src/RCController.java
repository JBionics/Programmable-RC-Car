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

// This class is responsible for handling the business logic
// of the Programmable RC Car Controller.
public class RCController {
    // Bits to represent different directions.
    // Example, to indicate Forward & Right, adding
    // FORWARD_BIT + RIGHT_BIT = 9.
    // This can later be decoded to drive the car
    // forward and right.
    public final byte STOP = 0;
    public final byte FORWARD_BIT = 1;
    public final byte BACKWARD_BIT = 2;
    public final byte LEFT_BIT = 4;
    public final byte RIGHT_BIT = 8;

    // Enum value for drive command, see arduino code fore more info
    private final byte DRIVE_CMD = 10;

    private byte m_speed = (byte) 255;
    private TextFileParser m_txtParser;
    private RCCom m_rcCom;

    public RCController() {
        m_txtParser = new TextFileParser();
        ;
        m_rcCom = new RCCom();
    }

    // Send command to serial port
    private void sendCommand(byte commandId, byte data1, byte data2) {
        // Command is of format ID, DATA1, DATA2, CHECKSUM
        // See arduino code for more info
        byte[] byteArray = new byte[4];
        byteArray[0] = commandId;
        byteArray[1] = data1;
        byteArray[2] = data2;
        byteArray[3] = (byte) (commandId + data1 + data2);
        m_rcCom.sendData(byteArray);
    }

    // Send drive command to serial port, no speed specified
    public synchronized void sendDirectionCommand(byte keyState) {
        sendCommand(DRIVE_CMD, keyState, m_speed);
        System.out.println("Sending Command: " + keyState);
    }

    // Send drive command to serial port, speed specified
    public synchronized void sendDirectionCommand(byte keyState, byte speed) {
        sendCommand(DRIVE_CMD, keyState, speed);
        System.out.println("Sending Command: " + keyState);
    }

    // Change the driving speed
    // Also sends the command to the serial port
    public void setSpeed(byte keyState, byte speed) {
        // Range 0 - 255
        m_speed = speed;
        sendDirectionCommand(keyState, speed);
    }

    // Run through the programmed commands in sequence.txt
    // Creates a new thread every time it's called
    public synchronized void processProgrammedSequence(RCControllerGUI gui) {
        String[] sequenceCmds = m_txtParser.getSequence();
        if (sequenceCmds != null) {
	        SequenceThread s = new SequenceThread(this, gui, sequenceCmds);
	        new Thread(s).start();
        } else {
        	gui.showWarning("Could not process sequence.txt.\nMake sure that the file exists in the same directory as Programmable RC Controller.");
        }
    }

    // Returns port name of serial port in use
    public String getPortName() {
        return m_txtParser.getPortName();
    }

    // Sets the serial port name (updates port.txt too)
    public void setPortName(String port) {
        m_txtParser.setPortName(port);
    }

    // Returns a String array of all found serial ports
    public String[] getPortNames() {
        return m_rcCom.getPortNames();
    }

    // Connect to the serial ports
    public String connect() {
        return m_rcCom.connect(getPortName());
    }

    // Program entry point
    public static void main(String[] args) {
        try {
            // RC Controller (Business Logic)
            RCController controller = new RCController();

            // RC Controller GUI
            RCControllerGUI gui = new RCControllerGUI(controller);

            // Connect to the serial port and display error msg if necessary
            String errorMsg = controller.connect();
            gui.setPortName(controller.getPortName());
            if (errorMsg != null) {
                gui.showWarning("Could not connect ("
                        + controller.getPortName() + "):\n" + errorMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
