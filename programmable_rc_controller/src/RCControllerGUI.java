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

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

// This class is responsible for the Programmable RC Car Controller GUI
public class RCControllerGUI {
    private JFrame m_frame;
    private JLabel[] m_lblDirections;
    private ImageIcon[] m_iconImages;
    private JTextField m_portField;
    private JSlider m_speedSlider;
    
    // State of the buttons being pressed
    private byte m_keyState = 0;
    
    private RCController m_controller;
        
    public RCControllerGUI(RCController controller) {
        // Draws the GUI to the screen
        initialize();
        m_controller = controller;
        m_frame.setVisible(true);
    }
    
    // Show a warning to the user
    public void showWarning(String str)
    {
        JOptionPane.showMessageDialog(null, str);
    }
    
    // Update the port name on the GUI
    public void setPortName(String portName) 
    {
        m_portField.setText(portName);
    }
    
    // Update the GUI without user input
    // Occurs when the car has been programmed using sequence.txt
    public synchronized void remoteUpdateGUI(byte keyState)
    {
        if ((keyState & m_controller.FORWARD_BIT) > 0) {
            updateDirectionIcon(0, true);
        } else {
            updateDirectionIcon(0, false);
        }
        
        if ((keyState & m_controller.BACKWARD_BIT) > 0) {
            updateDirectionIcon(1, true);
        } else {
            updateDirectionIcon(1, false);
        }
        
        if ((keyState & m_controller.LEFT_BIT) > 0) {
            updateDirectionIcon(2, true);
        } else {
            updateDirectionIcon(2, false);
        }
        
        if ((keyState & m_controller.RIGHT_BIT) > 0) {
            updateDirectionIcon(3, true);
        } else {
            updateDirectionIcon(3, false);
        }       
    }
    
    // Update a GUI direction icon
    private void updateDirectionIcon(int direction, boolean on)
    {
        // TODO: Bad coding style, should use defined values instead of hard-coded 0 - 3 for direction values
        int icon_index = direction + (on ? 4 : 0);
        
        if (direction >= m_lblDirections.length || icon_index >= m_iconImages.length) return;

        m_lblDirections[direction].setIcon(m_iconImages[icon_index]);
    }   

    // Draws the GUI to the screen
    private void initialize() {
        m_frame = new JFrame("Programmable RC Car Controller");
        m_frame.addKeyListener(new KeyAdapter() {
            @Override
            // Catches all keyPressed events
            public void keyPressed(KeyEvent arg0) {             
                switch (arg0.getKeyCode())
                {
                case 38: // UP
                case 'W':
                    updateDirectionIcon(0, true);
                    if ((m_keyState & m_controller.FORWARD_BIT) == 0) {
                        m_keyState += m_controller.FORWARD_BIT;
                        // Send command to the serial port
                        m_controller.sendDirectionCommand(m_keyState);
                    }
                    break;
                case 40: // DOWN
                case 'S':
                    updateDirectionIcon(1, true);
                    if ((m_keyState & m_controller.BACKWARD_BIT) == 0) {
                        m_keyState += m_controller.BACKWARD_BIT;
                        m_controller.sendDirectionCommand(m_keyState);
                    }
                    break;
                case 37: // LEFT
                case 'A':
                    updateDirectionIcon(2, true);
                    if ((m_keyState & m_controller.LEFT_BIT) == 0) {
                        m_keyState += m_controller.LEFT_BIT;
                        m_controller.sendDirectionCommand(m_keyState);
                    }
                    break;
                case 39: // RIGHT
                case 'D':
                    updateDirectionIcon(3, true);
                    if ((m_keyState & m_controller.RIGHT_BIT) == 0) {
                        m_keyState += m_controller.RIGHT_BIT;
                        m_controller.sendDirectionCommand(m_keyState);
                    }
                    break;
                case 61: // +
                    m_speedSlider.setValue(m_speedSlider.getValue() + m_speedSlider.getMinorTickSpacing());
                    break;
                case 45: // -
                    m_speedSlider.setValue(m_speedSlider.getValue() - m_speedSlider.getMinorTickSpacing());
                    break;
                case 27: // ESC
                    System.exit(0);
                    break;
                }
                
            }
            @Override
            public void keyReleased(KeyEvent arg0) {
                // Catches all keyReleased events
                switch (arg0.getKeyCode())
                {
                case 38: // UP
                case 'W':
                    updateDirectionIcon(0, false);
                    if ((m_keyState & m_controller.FORWARD_BIT) > 0) {
                        m_keyState -= m_controller.FORWARD_BIT;
                        // Sends command to serial port
                        m_controller.sendDirectionCommand(m_keyState);
                    }
                    break;
                case 40: // DOWN
                case 'S':
                    updateDirectionIcon(1, false);
                    if ((m_keyState & m_controller.BACKWARD_BIT) > 0) {
                        m_keyState -= m_controller.BACKWARD_BIT;
                        m_controller.sendDirectionCommand(m_keyState);
                    }
                    break;
                case 37: // LEFT
                case 'A':
                    updateDirectionIcon(2, false);
                    if ((m_keyState & m_controller.LEFT_BIT) > 0) {
                        m_keyState -= m_controller.LEFT_BIT;
                        m_controller.sendDirectionCommand(m_keyState);
                    }
                    break;
                case 39: // RIGHT
                case 'D':
                    updateDirectionIcon(3, false);
                    if ((m_keyState & m_controller.RIGHT_BIT) > 0) {
                        m_keyState -= m_controller.RIGHT_BIT;
                        m_controller.sendDirectionCommand(m_keyState);
                    }
                    break;
                    
                case 27: // ESC
                    break;
                }
                
            }
        });
        
        // Setup main frame
        m_frame.setBounds(100, 100, 450, 300);
        m_frame.setResizable(false);
        m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        m_frame.getContentPane().setLayout(null);

        // Text field that displays the serial port in use
        m_portField = new JTextField();
        m_portField.setBounds(81, 244, 216, 28);
        m_portField.setFocusable(false);
        m_portField.setColumns(10);
        m_portField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                // If textField is clicked on, display a dialog box with a list of found serial ports to select
                String[] ports = m_controller.getPortNames();
                String str = (String)JOptionPane.showInputDialog(
                                    m_frame,
                                    null,
                                    "Select Serial Port",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    ports,
                                    ports.length > 0 ? m_controller.getPortName() : "No Ports Found");
                if (ports.length > 0 && str != null) {
                    // Update the GUI
                    setPortName(str);
                    
                    // Update the port.txt text file
                    m_controller.setPortName(str);                  
                    
                    // Try to connect to the new port
                    String errorMsg = m_controller.connect();
                    if (errorMsg != null) {
                        showWarning("Could not connect (" + m_controller.getPortName() +"):\n" + errorMsg);
                    }                   
                }
            }
        });
        m_frame.getContentPane().add(m_portField);      
        
        JLabel lblPort = new JLabel("Serial Port:");
        lblPort.setBounds(6, 250, 87, 16);
        m_frame.getContentPane().add(lblPort);
        
        // Speed Slider
        m_speedSlider = new JSlider();
        m_speedSlider.setMinorTickSpacing(10);
        m_speedSlider.setOrientation(SwingConstants.VERTICAL);
        m_speedSlider.setMaximum(255);
        m_speedSlider.setBounds(360, 40, 40, 172);
        m_speedSlider.setValue(400);
        m_speedSlider.setFocusable(false);
        m_speedSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                if (m_controller != null) {
                    // Send commands to serial port as slider is adjusted
                    m_controller.setSpeed(m_keyState, (byte) m_speedSlider.getValue());
                }
            }
        });     
        m_frame.getContentPane().add(m_speedSlider);
        
        // Slider labels
        JLabel lblFast = new JLabel("FAST");
        lblFast.setBounds(363, 12, 31, 16);
        m_frame.getContentPane().add(lblFast);
        
        JLabel label = new JLabel("SLOW");
        label.setBounds(363, 203, 40, 16);
        m_frame.getContentPane().add(label);
        
        JLabel label_1 = new JLabel("(+)");
        label_1.setBounds(369, 27, 31, 16);
        m_frame.getContentPane().add(label_1);
        
        JLabel label_2 = new JLabel("(-)");
        label_2.setBounds(372, 217, 40, 16);
        m_frame.getContentPane().add(label_2);
        
        // Run Sequence Button
        JButton btnRunSequencetxt = new JButton("Run Sequence.txt");
        btnRunSequencetxt.setFocusable(false);
        btnRunSequencetxt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                processSequenceTxt();
            }
        });
        btnRunSequencetxt.setBounds(297, 245, 153, 29);
        m_frame.getContentPane().add(btnRunSequencetxt);
        
        // Images used for directions
        m_iconImages = new ImageIcon[8];
        String[] imgPaths = new String[8];
        imgPaths[0] = "up_off.gif";
        imgPaths[1] = "down_off.gif";
        imgPaths[2] = "left_off.gif";
        imgPaths[3] = "right_off.gif";
        imgPaths[4] = "up_on.gif";
        imgPaths[5] = "down_on.gif";
        imgPaths[6] = "left_on.gif";
        imgPaths[7] = "right_on.gif";
        
        for (int i = 0; i < m_iconImages.length; i++) {
        	try {
            	m_iconImages[i] = new ImageIcon(getClass().getResource(imgPaths[i]));
        	} catch (Exception e) {
                System.out.println("Could not load image resource: " + e.getMessage());
                m_iconImages[i] = new ImageIcon();
        	}
        }
        
        // Labels that the images will go on
        m_lblDirections = new JLabel[4];
        
        // Positioning the direction arrow labels
        int up_x = 173;
        int up_y = 10;
        int up_size = 75;
        
        m_lblDirections[0] = new JLabel(m_iconImages[0]);       
        m_lblDirections[0].setBounds(up_x, up_y, up_size, up_size);
        m_frame.getContentPane().add(m_lblDirections[0]);
        
        m_lblDirections[1] = new JLabel(m_iconImages[1]);
        m_lblDirections[1].setBounds(up_x, up_y + 2*up_size - 20, up_size, up_size);
        m_frame.getContentPane().add(m_lblDirections[1]);
        
        m_lblDirections[2] = new JLabel(m_iconImages[2]);
        m_lblDirections[2].setBounds(up_x - up_size + 10, up_y + up_size - 10, up_size, up_size);
        m_frame.getContentPane().add(m_lblDirections[2]);
        
        m_lblDirections[3] = new JLabel(m_iconImages[3]);
        m_lblDirections[3].setBounds(up_x + up_size - 10, up_y + up_size - 10, up_size, up_size);
        m_frame.getContentPane().add(m_lblDirections[3]);
    }
    
    // Run the series of programmed commands in sequence.txt
    private void processSequenceTxt()
    {
        m_controller.processProgrammedSequence(this);
    }
}