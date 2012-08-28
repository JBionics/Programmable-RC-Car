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

import java.io.*;
import java.util.*;

// This class is responsible for reading and writing to text files.
// port.txt contains the last used serial port.
// sequence.txt contains a series of commands that can be used to program the car.
public class TextFileParser {
    private FileInputStream m_fStream;
    private DataInputStream m_dataInStream;
    private BufferedReader m_reader;
    
    private String m_portName;
    private boolean m_portRecentlySet = false;
    
    private final String PORT_FILENAME = "port.txt";
    private final String SEQUENCE_FILENAME = "sequence.txt";
    
    // Set the port name and write it to port.txt
    public void setPortName(String port)
    {
        if (port == null) return;
        try {
            Writer out = new OutputStreamWriter(new FileOutputStream(PORT_FILENAME));
            out.write(port);
            out.close();
            m_portRecentlySet = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Return the port name
    // This will read it from the file if necessary or return the cached value.
    public String getPortName()
    {
        if (m_portName == null || m_portRecentlySet) {
            m_portName = getPortNameFromFile();
        }
        return m_portName;
    }
    
    // Returns an array of strings that contain the commands to program the car
    public String[] getSequence()
    {
        List<String> sequenceCmds = new ArrayList<String>();
        
        try{
              m_fStream = new FileInputStream(SEQUENCE_FILENAME);
              m_dataInStream = new DataInputStream(m_fStream);
              m_reader = new BufferedReader(new InputStreamReader(m_dataInStream));
              
              String strLine;
              //Read File Line By Line
              while ((strLine = m_reader.readLine()) != null)   {
                  // Commands are comma-separated, so split the string on a comma
                  String[] strings = strLine.split(",");

                  // If the right number of parameters are found and it's not a comment line ...
                  if (strings.length == 3 && !strLine.contains("#")) {
                      for (int i = 0; i < strings.length; i++) {
                          // Trim the string of white space and add it to the ArrayList
                          strings[i] = strings[i].trim();
                          sequenceCmds.add(strings[i]);
                      }
                  }
              }
              //Close the input stream
              m_dataInStream.close();
            } catch (Exception e) {//Catch exception if any
              System.err.println("Error: " + e.getMessage());
            }
        return sequenceCmds.toArray(new String[sequenceCmds.size()]);
    }
    
    // Return the port name stored in port.txt
    private String getPortNameFromFile()
    {
          String strLine = null;
          try{
              m_fStream = new FileInputStream(PORT_FILENAME);
              m_dataInStream = new DataInputStream(m_fStream);
              m_reader = new BufferedReader(new InputStreamReader(m_dataInStream));
              
              // Read the first line of the file
              strLine = m_reader.readLine();
              
              //Close the input stream
              m_dataInStream.close();
              m_portRecentlySet = false; 
            } catch (Exception e) {
              System.out.println("Error Reading File: " + e.getMessage());
              try {
                  File portFile = new File("port.txt");
                  portFile.createNewFile();
                  System.out.println("port.txt created");                 
              } catch (Exception ex) {
                  System.out.println("Could not create port.txt: " + ex.getMessage());                
              }
            }
            return strLine != null ? strLine : "port.txt not found";
    }   
}