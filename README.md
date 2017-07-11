# IRCTypeClientServer
Author: Richard Holgate
This is a small custom IRC-like Client/Server setup that I wrote for my Internet Protocols class. 
It only works locally, though it could work across networks with some small changes.

Copyright (c) 2017 Richard Holgate

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

# Compiling:
Just a regular 'javac *.java' should work to compile.

# Running:
This program is run with 1 server and any number of clients. 
Start up the server with 'java Server', and start up any number of clients by running 'java Client' any number of times.

# Use:
The server will run on it's own, displaying appropriate messages along the way, but requires no further interaction.
Each client represents a user in the IRC server. After providing a username at the prompt, type 'help' to display the following prompt:

    Type one of the following commands to get the listed result.
        list [room] - list all available rooms. If a [room] was provided, list all users in that room instead.
          
        join [room1] [room2] ... [room n] - join all listed rooms. If a room doesn't exist, it'll be created and the user will join it.
          
        leave [room] - leave room [room]. If [room] doesn't exist nothing will happen.
          
        (msg (or) message) room:[room1] [message1] room:[room2] [message2] ... room:[room n] [message n]
          
        send [message] to [room]. If [room] doesn't exist, nothing will happen
                
        quit - close the program
        
        
After you're finished with all the clients, simply close the Server. There is no exit command for it.
