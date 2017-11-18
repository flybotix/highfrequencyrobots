# High Frequency Data framework, meant for WiFi-based robots

## What it does, and why
 - HFR is a Java-native IoT comms protocol that also acts alternative to Network Tables for FRC robots not on a live field.
 - Provides helpers that deal with the entire communications protocol in as few as 6 lines of code!
 - (WIP) Seamlessly integrates with Network Tables for live FRC field environments (including NT's protections & reliability)
 - Deals with threading & socket comms internally, so robot code doesn't have to.  This includes batching of multiple tiny 'messages' in order to stay within the limitations of packet sizes and rates.
 - Is available for UDP, TCP, NetworkTables (WIP), and 'passthrough' (WIP) protocols
 - Automatically compresses NULL data.  This means we no longer need to fear bandwidth limitations when sending a NULL element across a network.
 - Includes auto-incrementing metadata that keeps track of cycles for Codexes, which allows normalization of the data structure the Codex represents.
 - Includes benchmark & data integrity tests to ensure encode/decode/transit processing times are minimized

## Getting started
Get the artifact! Latest stable release:
```
groupId: com.flybotix
artifactId: HighFrequencyRobots
version: 2017.11.15

Dev version: 0.0.16
```

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
ISendProtocol protocol = Protocols.createSender(EProtocol.UDP, 7778, 7777, "localhost");
CodexSender sender = new CodexSender(protocol);
```
3. On the laptop/client side, create a 'receiver'.  Then register for updates with that receiver.
```java
// Re-use this protocol for all of the receivers
IReceiveProtocol protocol = Protocols.createReceiver(EProtocol.UDP, 7778, "localhost");
CodexReceiver<Double, ETestData> receiver = new CodexReceiver<>(ETestData.class, protocol);
receiver.addListener(codex -> System.out.println(codex));
```
4.  During robot initialization, create a Codex and pass its reference where it's needed.  Try not to create a 'new' Codex for the same enumeration, as that may cause Java's garbage collection to pause the robot.
```java
// During robot init
Codex<Double, RobotData> data = Codex.of.thisEnum(RobotData.class);

// Reset the codex at the beginning of each cycle.  This effectively sets each value to 'null'.  Fill out data throughout each cycle.
data.reset(); // beginning of the cycle
data.put(RobotData.pdb2, -23.3d);
data.put(RobotData.gyro, mxp.getGyroRelative());

// Use the data throughout the robot cycle after it's gathered
double degrees = data.get(RobotData.gyro);

// Send the data back to the laptop at the end of each cycle
sender.send(data);
```

## The Codex: an enumerated array
(WIP)
This project is based upon two simple principles:
1. A piece of data in an array actually has two pieces of information: the data value, and the position of the data in the array. If a piece of data does not exist at a particular position in an array, it is NULL - which also tells us something about that data.
2. We'd rather see compiler errors instead of weird data when the robot is running.  Compiler errors are easy to debug and fix.  Weird data is not.

Enumerations in Java are just that: a compile-time reference of objects in a static array.  To get the position of the enumeration in the array, we call enum.ordinal().  To get the static information of the enumeration, we can call name(), toString(), or any other implemented method.  Enumerations can even implement interfaces, making them (effectively) static lambdas.  The HFR project expects a Codex's enumeration to be available at compile time on all "sides" of a comms link.

The Codex in this project is an array that uses an enumeration to define what the values at any given position MEAN to a programmer & user. A programmer simply needs to specify the type in the array, and then the enumeration which backs the codex.  The class has a few utility and metadata methods.

An enumerated array was chosen over a EnumMap because indexing an array is a faster O(1) operation, whereas indexing into a map is a O(n) operation.  Since an enumeration's length never changes after compile-time, we know that the length of the data array will never change - and thus most operations into getting data from the array are safe.

The fact that a Codex must represent data of the same type is simultaneously this project's biggest advantage and disadvantage.  It means this project will never represent complex types (e.g. like what JSON can do), but it also means that communicating the data can be extremely efficient (unlike JSON) - and therefore be executed at a higher frequency.  In FRC robots (and many IoT scenarios), the data is all of the same type.  If the data isn't sent over a comms protocol, then the type of the codex _can_ be a String, array of arrays, complex POJOs, etc, without worry of data corruption.

## Future Work (in order)
1. Implement & end-to-end test NT protocol with a live robot
1. Create a class for linked instances of the same Codex (e.g. linking multiple TalonSRX Codexes together)
1. Export data to CSV with just 1 extra line of setup code
1. Bandwidth monitoring on the client side
1. End-to-end test TCP protocol with a live robot
1. Manage workload of the default client to keep resource usage low during export
1. Heuristics & warnings about limting quantities of fields, compression vs non-compression, etc.
1. Manage workload of the default client to keep resource usage low during export
