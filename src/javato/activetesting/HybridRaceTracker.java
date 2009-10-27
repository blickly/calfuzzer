package javato.activetesting;

import javato.activetesting.lockset.LockSet;
import javato.activetesting.HybridAnalysis;

import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;

class CommutativePair implements java.io.Serializable {
  Integer x, y;

  CommutativePair(Integer x, Integer y) {
    if (x < y) {
      this.x = x; this.y = y;
    } else {
      this.y = x; this.x = y;
    }
  }

  public boolean contains(Integer z) {
    return x.equals(z) || y.equals(z);
  }

  public String toString() {
    return "(" + x + "," + y + ")";
  }

  public int hashCode() {
    return x.hashCode() + y.hashCode();
  }

  public boolean equals(Object o) {
    CommutativePair rhs = (CommutativePair) o;
    return this.x.equals(rhs.x) && this.y.equals(rhs.y);
  }
}

class Event {
  public Integer iid;
  public Integer thread;
  public Long memory;
  public boolean isWrite;
  public VectorClock vClock;
  public LockSet ls;

  Event(Integer iid, Integer thread, Long memory, boolean isWrite, VectorClock vClock, LockSet ls) {
    this.iid = iid;
    this.thread = thread;
    this.memory = memory;
    this.isWrite = isWrite;
    this.vClock = vClock;
    this.ls = ls;
  }

  boolean isRace(Event rhs) {
    boolean result = !this.thread.equals(rhs.thread) &&
      this.memory.equals(rhs.memory)
      && (this.isWrite || rhs.isWrite)
      && !this.vClock.lessThanEqual(rhs.vClock)
      && !rhs.vClock.lessThanEqual(this.vClock)
      && !this.ls.intersects(rhs.ls);
    System.out.println("Checking race between " + this + " and " + rhs);
    System.out.println("(result = " + result + ")");
    System.out.println("Clock incomparable: " +
      (!this.vClock.lessThanEqual(rhs.vClock)
      && !rhs.vClock.lessThanEqual(this.vClock)));
    return result;
  }

  public String toString() {
    String s = "";
    //s += "Instruction: " + this.iid;
    s += " Thread: " + this.thread;
    s += " MemLoc: " + this.memory;
    s += " isWrite: " + this.isWrite;
    s += " VectorClock: " + this.vClock;
    s += " LockSet: " + this.ls;
    return s;
  }
}

class DatabaseQuery {
  Long memory;
  Integer thread;
  Boolean needWrite;

  DatabaseQuery(Long mem, Integer t, Boolean wr) {
    this.memory = mem;
    this.thread = t;
    this.needWrite = wr;
  }


  public int hashCode() {
    return this.memory.hashCode() + this.thread.hashCode()
      + this.needWrite.hashCode();
  }

  public boolean equals(Object o) {
    DatabaseQuery rhs = (DatabaseQuery) o;
    return this.memory.equals(rhs.memory) && this.thread.equals(rhs.thread)
      && this.needWrite.equals(rhs.needWrite);
  }
}

class HybridRaceTracker {
  private Map<Long, LinkedList<Event>> eventsAtMem = new HashMap<Long, LinkedList<Event>>();
  //private Map<DatabaseQuery, Event> eventDB = new HashMap<DatabaseQuery, Event>();
  private LinkedHashSet<CommutativePair> races = new LinkedHashSet<CommutativePair>();

  public void checkRace(Integer iid, Integer thread, Long memory, boolean isWrite, VectorClock vClock, LockSet ls) {
    Event thisEvent = new Event(iid, thread, memory, isWrite, vClock, ls);
    /*
    for (int t = 0; t < 100; ++t) {
      if (thread.equals(t))
        continue;
      Event otherEvent = eventDB.get(new DatabaseQuery(memory, t, !isWrite));
      if (otherEvent == null)
        continue;
      if (thisEvent.isRace(otherEvent)) {
        races.add(new CommutativePair(thisEvent.iid, otherEvent.iid));
      }
    }*/
    
    LinkedList<Event> eventStack = eventsAtMem.get(memory);
    if (eventStack == null) {
      eventStack = new LinkedList<Event>();
      eventsAtMem.put(memory, eventStack);
    }
    for (Event e : eventStack) {
      if ( thisEvent.isRace(e) ) {
        races.add(new CommutativePair(thisEvent.iid, e.iid));
      }
    }
    
  }

  public void addEvent(Integer iid, Integer thread, Long memory, boolean isWrite, VectorClock vClock, LockSet ls) {
    Event e = new Event(iid, thread, memory, isWrite, vClock, ls);
    /*
    System.out.println("Adding event to db: " + e);
    eventDB.put(new DatabaseQuery(memory, thread, false), e);
    if (isWrite) {
      eventDB.put(new DatabaseQuery(memory, thread, isWrite), e);
    }
    // */
    
    LinkedList<Event> eventStack = eventsAtMem.get(memory);
    if (eventStack == null) {
      eventStack = new LinkedList<Event>();
      eventsAtMem.put(memory, eventStack);
    }
    eventStack.addFirst(e);
    // */
  }

//    The following method call creates a file "error.list" containing the list of numbers "1,2,3,...,nRaces"
//    This file is used by run.xml to initialize Parameters.errorId with a number from from the list.
//    Parameters.errorId tells RaceFuzzer the id of the race that RaceFuzzer should try to create
  public int dumpRaces() {
    System.out.println("Potential races:");
    for (CommutativePair cp : races) {
      System.out.println(cp.toString());
    }
    try {
      java.io.FileOutputStream fos = new java.io.FileOutputStream("error.log");
      java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos);
      oos.writeObject(races);
    } catch (java.io.IOException e) {
      System.out.println("Failed to write to file: " + e);
    }
    return races.size();
  }

  public static LinkedHashSet<CommutativePair> getRacesFromFile() {
    LinkedHashSet<CommutativePair> savedRaces = null;
    try {
      java.io.FileInputStream fis = new java.io.FileInputStream("error.log");
      java.io.ObjectInputStream ois = new java.io.ObjectInputStream(fis);
      savedRaces = (LinkedHashSet<CommutativePair>) ois.readObject();
    } catch (java.io.IOException e) {
      System.out.println("Failed to read object from file: " + e);
    } catch (java.lang.ClassNotFoundException e) {
      System.out.println("Data in 'error.log' seems invalid: " + e);
    }
    return savedRaces;
  }

}
