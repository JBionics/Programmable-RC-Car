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

// This class is run in it's own thread.
// It is responsible for receiving a list of direction commands, writing them to the serial port
// and updating the gui as the commands are sent.
public class SequenceThread implements Runnable {
    private RCController m_controller;
    private RCControllerGUI m_gui;
    private String[] m_cmds;
    
    SequenceThread(RCController controller, RCControllerGUI gui, String[] cmds)
    {
        m_controller = controller;
        m_gui = gui;
        m_cmds = cmds;
    }

    @Override
    // Note: this code is run when thread.start is called from RCController.java
    public void run() {
        if (m_cmds.length % 3 > 0) {
            m_gui.showWarning("Parsing Error: sequence.txt");
            return; // Not a multiple of 3?  Return.
        }
        
        for (int i = 0; i < m_cmds.length; i+=3) {
            // A command sequence is made up of a direction, speed, and duration.
            String direction = m_cmds[i];
            String speedStr = m_cmds[i+1];
            String durationMsStr = m_cmds[i+2];
            
            // Convert Direction to keyState
            byte keyState = 0;
            if (direction.equalsIgnoreCase("FF")) {
                keyState = m_controller.FORWARD_BIT;
            } else if (direction.equalsIgnoreCase("FR")) {
                keyState = (byte) (m_controller.FORWARD_BIT + m_controller.RIGHT_BIT);
            } else if (direction.equalsIgnoreCase("FL")) {
                keyState = (byte) (m_controller.FORWARD_BIT + m_controller.LEFT_BIT);
            } else if (direction.equalsIgnoreCase("BB")) {
                keyState = (byte) (m_controller.BACKWARD_BIT);
            } else if (direction.equalsIgnoreCase("BL")) {
                keyState = (byte) (m_controller.BACKWARD_BIT + m_controller.LEFT_BIT);
            } else if (direction.equalsIgnoreCase("BR")) {
                keyState = (byte) (m_controller.BACKWARD_BIT + m_controller.RIGHT_BIT);
            } else if (direction.equalsIgnoreCase("LL")) {
                keyState = (byte) (m_controller.LEFT_BIT);
            } else if (direction.equalsIgnoreCase("RR")) {
                keyState = (byte) (m_controller.RIGHT_BIT);             
            } else if (direction.equalsIgnoreCase("SS")) {
                keyState = (byte) (m_controller.STOP);
            }
            
            // Convert speed and duration to ints
            byte speed = (byte) Integer.parseInt(speedStr);
            int durationMs = Integer.parseInt(durationMsStr);
            
            // Send command to serial port
            m_controller.sendDirectionCommand(keyState,speed);
            
            // Update the GUI
            m_gui.remoteUpdateGUI(keyState);
            
            // Delay for durationMs milliseconds until running the next command
            try {
                Thread.sleep(durationMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Sequence is finished, send the stop command and clear the GUI
        m_controller.sendDirectionCommand(m_controller.STOP);
        m_gui.remoteUpdateGUI(m_controller.STOP);
    }
}
