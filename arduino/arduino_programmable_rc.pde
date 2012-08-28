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

#define DEBUG (1)

// Pin Functions
#define FORWARD_PIN   (9)
#define BACKWARD_PIN (10)
#define LEFT_PIN     (11)
#define RIGHT_PIN    (12)

// Bits to indicate FORWARD, BACKWARD, LEFT, and RIGHT
#define FORARD_BIT   (1) // b'0001' (binary)
#define BACKWARD_BIT (2) // b'0010'
#define LEFT_BIT     (4) // b'0100'
#define RIGHT_BIT    (8) // b'1000'

// Each command is 4 bytes in size
struct Command
{
    byte id;
    byte data1;
    byte data2;
    byte checksum;
};

// List of commands
enum COMMAND_IDS
{
    INVALID_CMD = 0,
    DRIVE = 10
};

void setup()
{
    // Setup Pin I/O Functions
    pinMode(FORWARD_PIN, OUTPUT);
    pinMode(BACKWARD_PIN, OUTPUT);
    pinMode(LEFT_PIN, OUTPUT);
    pinMode(RIGHT_PIN, OUTPUT);
    
    // Initialize Serial
    Serial.begin(9600);
}

// If DEBUG is enabled, this function writes a string to the serial port
void dbg_print(const char * s)
{
#if DEBUG
    Serial.write(s);
#endif
}

// Decodes a command struct, does some error checking, and controls the Arduino pins
void driveCar(struct Command &newCmd)
{
    // If forward and backward are both enabled, error, remove the backward bit set
    if ((newCmd.data1 & FORARD_BIT) && (newCmd.data1 & BACKWARD_BIT)) {
        newCmd.data1 -= BACKWARD_BIT;
    }
    
    // If left and right are both enabled, error, remove the right bit set
    if ((newCmd.data1 & LEFT_BIT) && (newCmd.data1 & RIGHT_BIT)) {
        newCmd.data1 -= RIGHT_BIT;
    }
    
    // Drive forward if enabled
    if (newCmd.data1 & FORARD_BIT) {
        // Note: newCmd.data2 is the speed, a PWM value specified in range 0 - 255, 255 = MAX
        analogWrite(FORWARD_PIN, newCmd.data2);
    } else {
        analogWrite(FORWARD_PIN, 0);
    }
    
    // Drive backward if enabled
    if (newCmd.data1 & BACKWARD_BIT) {
        analogWrite(BACKWARD_PIN, newCmd.data2);
    } else {
        analogWrite(BACKWARD_PIN, 0);
    }
    
    // Drive left if enabled
    if (newCmd.data1 & LEFT_BIT) {
        digitalWrite(LEFT_PIN, HIGH);
    } else {
        digitalWrite(LEFT_PIN, LOW);
    }
    
    // Drive right if enabled
    if (newCmd.data1 & RIGHT_BIT) {
        digitalWrite(RIGHT_PIN, HIGH);
    } else {
        digitalWrite(RIGHT_PIN, LOW);
    }
}

void processCommand(struct Command &newCmd)
{
    switch (newCmd.id)
    {
        case DRIVE:
            dbg_print("Drive...");
            driveCar(newCmd);
            break;
        default:
            // Unknown Command, do nothing
            dbg_print("Invalid cmd received...");
            break;
    }
}

// Main control loop
// Receives data from the serial port and sends it to be processed
void loop()
{
    Command incomingCmd;
    if (Serial.available() >= sizeof(Command)) {
        // read the incoming data:
        Command * mem = &incomingCmd;
        unsigned char * p = (unsigned char *)mem;
        for (int i = 0; i < sizeof(Command); i++) {
            unsigned int data = Serial.read();
            p[i] = data;
        }
        
        // Verify checksum
        byte received_sum = incomingCmd.id + incomingCmd.data1 + incomingCmd.data2;
        if (incomingCmd.id != INVALID_CMD && received_sum == incomingCmd.checksum) {
            processCommand(incomingCmd);
            dbg_print("Good Cmd - checksum matched");
        } else {
            //Checksum didn't match, don't process the command
            dbg_print("Bad Cmd - invalid cmd or checksum didn't match");
        }
    }
}
