# Codex High Frequency Data Framework
## What it does, and why
 - The Codex is an enumeration-backed auto-normalized data structure with design considerations for an IoT-centric comms protocol.
 - The library handles threading & socket comms internally, so robot code doesn't have to.  This includes batching of multiple tiny 'messages' in order to stay within the limitations of packet sizes and network rates.
 - The following protocols are available now or are inteded to become available:
     - **UDP**
     - **TCP** (with connection retry logic)
     - **NetworkTables Raw** (WIP-late Dec)
         - Encodes Codexes like UDP & TCP, then sends the `byte[]` array over NT
     - **NetworkTables By-Element**
         - Creates 1 table per registered Codex enumeration
         - Writes the codex metadata (time, index, & composite key) to their respective fields
         - Loops through the enumeration's values and writes each to the field corresponding to enueration.name()
         - Should be compatible with any NetworkTables "viewer" (testing WIP)
         - *NOTE* - of all available protocols, this is the only one that cannot handle a `null` element value
             - That simply means NetworkTables will show the 'last' good value when something goes wrong
             - The protocol will eventually have a configuration to set a default NT value of (e.g.) 0 when the codex data is `null`
     - **Passthrough** (WIP-late Dec)
         - Uses a basic Java listener/update interface
         - Codex data stays within the same process, and does not go remote
         - Useful for writing codex data to CSV from within the same process, or using Codexes to update a display
 - Automatically compresses `null` data prior to network transmission.  This means we no longer need to fear bandwidth limitations when sending a `null` element across a network.
 - (WIP-late Dec) Includes a 1-line command to log to CSV on the Robot.
 - Includes auto-incrementing metadata that keeps track of cycles for Codexes, which allows normalization of the data structure the Codex represents.
 - Includes benchmark & data integrity tests to ensure encode/decode/transit processing times are minimized.

## Get the Artifact
The latest stable release is available at **Maven Central**:
```
groupId: com.flybotix
artifactId: HighFrequencyRobots
version: 2017.11.25
Dev version: 0.0.25 (11/26)
```

## Get Coding
1. Create an enumeration that describes your data while implementing the CodexOf interface.  Both sides of the comms link will need this enumeration at compile time.  Since enumerations are lists of static objects, you can also do anything else you want with your enumeration (descriptions, short descriptions, inheritance, etc).
```java
import com.flybotix.hfr.codex.CodexOf;
public enum RobotData implements CodexOf<Double>{
  pdb0,
  pdb1,
  pdb2,
  pdb3,
  vrm0,
  totalcurrent,
  gyro
}
```
2. On your robot, create a "sender".
```java
// 172.22.11.1 is the default IP of the driver's station when connected over USB
// For a live field, it is recommended to set the driver's station to a static IP, such as '10.18.85.10'.
// See https://wpilib.screenstepslive.com/s/4485/m/24193/l/319135-ip-networking-at-the-event
ISendProtocol protocol = Protocols.createSender(EProtocol.UDP, 7778, 7777, "172.22.11.1");
CodexSender sender = new CodexSender(protocol);
```
3. On the laptop/client side, create a 'receiver'.  Then register for updates with that receiver.
```java
// Re-use this protocol for all of the receivers
IReceiveProtocol protocol = Protocols.createReceiver(EProtocol.UDP, 7778, null);
// A CodexReceiver registers with the protocol and parses ETestData messages received over that protocol
CodexReceiver<Double, ETestData> receiver = new CodexReceiver<>(ETestData.class, protocol);
//When a new ETestData message is received, this prints it to the console.
receiver.addListener(codex -> System.out.println(codex));
```
4.  During robot initialization, create a Codex and pass its reference where it's needed.  Try not to create a 'new' Codex for the same enumeration, as that may cause Java's garbage collection to pause the robot.
```java
// During robot init
Codex<Double, RobotData> data = new Codex(RobotData.class);

// Reset the codex at the beginning of each cycle.  This effectively sets each value to 'null'.  Fill out data throughout each cycle.
data.reset(); // beginning of the cycle
data.put(RobotData.pdb2, pdp.getCurrentForChannel(2));
data.put(RobotData.gyro, mxp.getGyroRelative());

// Use the data throughout the robot cycle after it's gathered
double degrees = data.get(RobotData.gyro);

// Send the data back to the laptop at the end of each cycle
sender.send(data);
```

## The Codex: an enumerated array
(WIP)
This project is based upon two simple principles:
1. A piece of data in an array actually has two pieces of information: the data value, and the position of the data in the array. If a piece of data does not exist at a particular position in an array, it is `null` - which also tells us something about that data.
2. We'd rather see compiler errors instead of weird data when the robot is running.  Compiler errors are easy to debug and fix.  Weird data is not.  Therefore a Codex is based upon a static data structure defined at compile time: the Java enumeration.

Enumerations in Java are just that: a compile-time reference of objects in a static array.  To get the position of the enumeration in the array, we call enum.ordinal().  To get the static information of the enumeration, we can call name(), toString(), or any other implemented method.  Enumerations can even implement interfaces, making them (effectively) static lambdas.

Think of this array as the 'columns' of a database or spreadsheet.  Every time `reset()` is called, the codex has effectively become a new row.  This is the power of the data structure itself, and it doesn't require much effort from someone using the library to make it happen.  Inserting new columns? No problem! Just add the column to the enumeration, set its value from your code, and any existing logging or display work that was based upon the loop of the enumeration will automatically handle the new column.

The fact that a Codex must represent data of the same type is simultaneously this project's biggest advantage and disadvantage.  It means this project will never represent complex types (e.g. like what JSON can do), but it also means that communicating the data can be extremely efficient (unlike JSON) - and therefore be executed at a higher frequency.  In FRC robots (and many IoT scenarios), the data is all of the same type.  If the data isn't sent over a comms protocol, then the type of the codex _can_ be a String, array of arrays, complex POJOs, etc, without worry of data corruption.

## Future Work (in order)
1. (WIP) Test threadsafe codex class, end-to-end.  Also add a thread executor to CodexSender.  Generally test multiple threads writing to codexes, and then a different thread sending the codex.
1. (WIP) Export data to CSV with just 1 extra line of setup code
1. Basic display using Gerrit Grunwald's horizon chart implementation
1. Bandwidth monitoring on the client side
1. Further characterization of NT with a live robot
1. End-to-end test TCP protocol with a live robot
1. Heuristics & warnings about limting quantities of fields, compression vs non-compression, etc.
