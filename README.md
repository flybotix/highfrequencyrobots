# Codex High Frequency Data Framework
Latest version: `2022.4.1`

## What it does, and why
 - The `Codex` is an enumeration-backed auto-normalized data structure with design considerations for an IoT-centric comms protocol.
 - The `RobotCodex` is a specialized version of `Codex` which has a (primitive) `double` array. 
 - The HFD project aims to streamline IoT data into normalized data structures for use in relational databases, with the end-goal of providing large datasets to ML.
 - The library handles network threading & socket comms internally, so robot code doesn't have to.  This includes batching of multiple tiny 'messages' in order to stay within the limitations of packet sizes and network rates.
 - The following protocols are available now or are inteded to become available:
     - **UDP**
     - **TCP** (with connection retry logic)
     - **Passthrough** 
         - Uses a basic Java listener/update interface
         - Codex data stays within the same process, and does not go remote
         - Useful for writing codex data to CSV from within the same process, or using Codexes to update a display
 - Automatically compresses `null` data prior to network transmission.  This means we no longer need to fear bandwidth limitations when sending a `null` element across a network. This does not apply to `RobotCodex`.
 - Includes a 1-line command to **log to CSV** on the Robot.
 - Includes auto-incrementing metadata that keeps track of cycles for Codexes, which allows normalization of the data structure the Codex represents.
 - Includes benchmark & data integrity tests to ensure encode/decode/transit processing times are minimized.

## Get the Artifact
The latest stable release is available **via jitpack.io**, and is also available via this repository's releases tab:
```
repositories {
     jcenter()
     maven { url "https://jitpack.io" }
}
dependencies {
      implementation 'com.github.flybotix:highfrequencyrobots:2022.4.1'
}
```

## Get Coding with RobotCodex, using a simple CSV logger
First, create an enumeration that describes your data while implementing the CodexOf interface.  Both sides of the comms link will need this enumeration at compile time.  Since enumerations are lists of static objects, you can also do anything else you want with your enumeration (descriptions, short descriptions, inheritance, etc).
```java
public enum ElevatorState{UP, DOWN}

public enum RobotData {
  pdb0,
  pdb1,
  pdb2,
  pdb3,
  vrm0,
  totalcurrent,
  gyro, 
    
  // This piece of data represents a Boolean flag, true/false.
  @RobotCodex.FlagCodex
  isAutonomous,
    
  // This represents a state machine. The logger will automtically convert this from a number to text in order to make it easier to read. However, if you want to graph this, then do not put this Annotation.  
  @RobotCodex.StateCodex(stateEnum = ElevatorState.class)
  elevatorState;
}
```
Second, during robot initialization, create a Codex and pass its reference where it's needed.  Try not to create a 'new' Codex for the same enumeration, as that may cause Java's garbage collection to pause the robot.
```java
// During robot init
RobotCodex<RobotData> data = new RobotCodex(RobotData.class);
data.createSimpleBooleanConverter(RobotData.isAutonomous);
data.createSimpleEnumConverter(RobotData.elevatorState, ElevatorState.class);

// Reset the codex at the beginning of each cycle.  This effectively sets each value to 'Double.NaN'.  Fill out data throughout each cycle.
data.reset(); // beginning of the cycle
data.put(RobotData.pdb2, pdp.getCurrentForChannel(2));
data.put(RobotData.gyro, mxp.getGyroRelative());
data.put(RobotData.isAutonomous, false);
data.put(RobotData.elevatorState, ElevatorState.DOWN);

// Use the data throughout the robot cycle after it's gathered
double degrees = data.get(RobotData.gyro);
ElevatorState state = data.get(RobotData.elvatorState, ElevatorState.class);
boolean isAutonomous = data.isSet(RobotData.isAutonomous);

// Log this to a file
String csvline = data.toFormattedCSV();
```

## The Codex: an enumerated array
(WIP)
This project is based upon two simple principles:
1. A piece of data in an array actually has two pieces of information: the data value, and the position of the data in the array. If a piece of data does not exist at a particular position in an array, it is `null` (or `Double.NaN`) - which also tells us something about that data.
2. We'd rather see compiler errors instead of weird data when the robot is running.  Compiler errors are easy to debug and fix.  Weird data is not.  Therefore a Codex is based upon a static data structure defined at compile time: the Java enumeration.

Enumerations in Java are just that: a compile-time reference of objects in a static array.  To get the position of the enumeration in the array, we call enum.ordinal().  To get the static information of the enumeration, we can call name(), toString(), or any other implemented method.  Enumerations can even implement interfaces, making them (effectively) static lambdas.

Think of this array as the 'columns' of a database or spreadsheet.  Every time `reset()` is called, the codex has effectively become a new row.  This is the power of the data structure itself, and it doesn't require much effort from someone using the library to make it happen.  Inserting new columns? No problem! Just add the column to the enumeration, set its value from your code, and any existing logging or display work that was based upon the loop of the enumeration will automatically handle the new column.

The fact that a Codex must represent data of the same type is simultaneously this project's biggest advantage and disadvantage.  It means this project will never represent complex types (e.g. like what JSON can do), but it also means that communicating the data can be extremely efficient (unlike JSON) - and therefore be executed at a higher frequency.  In FRC robots (and many IoT scenarios), the data is all of the same type.  If the data isn't sent over a comms protocol, then the type of the codex _can_ be a String, array of arrays, complex POJOs, etc, without worry of data corruption.

## Future Work (in order)
1. Bandwidth monitoring on the client side
1. Further characterization of NT with a live robot
1. End-to-end test TCP protocol with a live robot
1. Heuristics & warnings about limting quantities of fields, compression vs non-compression, etc.


## Change Log
2020.2.8
 - Made `RobotCodex::set(pEnum, boolean)` consistent with `RobotCodex.isSet(pEnum)` for the boolean element

2020.2.15a
 - Added new method to format the CSV values of a CSV string in `RobotCodex`. Override `RobotCodex.GLOBAL_CSV_FORMAT` to change it. Call `RobotCodex.toFormattedCSV()` to retrieve the formatted string

2020.2.19
 - Created `safeGet()` methods for certain scenarios that need extra safety in the value that is returned

2020.3.9
 - BREAKING CHANGE: `ICodexTimeProvider` now expects a DOUBLE value, representing SECONDS (instead of a long value representing nanoseconds). This should make it much easier to direcly read a CSV.

2022.4.20 (ish)
 - Created the annotations for CodexState and CodexFlag. This will auto-generate a text converter for pieces of data that are backed by a different enumeration, and use that text when logging to CSV. This is useful for NetworkTables as well. In the future, work will be done to provide both the ordinal and the text output to CSV, since it is easier to graph the state's ordinal.