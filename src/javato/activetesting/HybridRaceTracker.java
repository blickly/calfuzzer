package javato.activetesting;

import javato.activetesting.lockset.LockSet;
import javato.activetesting.analysis.Observer;

import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;

class CommutativePair implements java.io.Serializable {
  Integer x, y;

  CommutativePair(Integer x, Integer y) {
    if (x < y) {
      this.x = new Integer(x); this.y = new Integer(y);
    } else {
      this.y = new Integer(x); this.x = new Integer(y);
    }
  }

  public boolean contains(Integer z) {
    return x.equals(z) || y.equals(z);
  }

  public String toString() {
    return "(" + Observer.getIidToLine(x) + "[" + x + "],"
      + Observer.getIidToLine(y) + "[" + y + "])";
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
    this.iid = new Integer(iid);
    this.thread = new Integer(thread);
    this.memory = new Long(memory);
    this.isWrite = isWrite;
    this.vClock = vClock.clone();
    this.ls = new LockSet(ls);
  }

  boolean isRace(Event rhs) {
    boolean result = !this.thread.equals(rhs.thread) &&
      this.memory.equals(rhs.memory)
      && (this.isWrite || rhs.isWrite)
      && !this.vClock.lessThanEqual(rhs.vClock)
      && !rhs.vClock.lessThanEqual(this.vClock)
      && !this.ls.intersects(rhs.ls);
    if (result) {
      System.out.println("Potential race between " + this + " and " + rhs);
    }
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

class HybridRaceTracker {
  private Map<Long, Map<Integer, LinkedList<Event>>> eventDB = new HashMap<Long, Map<Integer, LinkedList<Event>>>();
  private LinkedHashSet<CommutativePair> races = new LinkedHashSet<CommutativePair>();

  public void checkRace(Integer iid, Integer thread, Long memory, boolean isWrite, VectorClock vClock, LockSet ls) {
    Event thisEvent = new Event(iid, thread, memory, isWrite, vClock, ls);
    Map<Integer, LinkedList<Event>> eventsForThread = eventDB.get(memory);
    if (eventsForThread == null) return;
    for (LinkedList<Event> eventList : eventsForThread.values()) {
      for (Event e : eventList) {
        if (e.vClock.lessThanEqual(vClock)) break;
        else if (thisEvent.isRace(e)) {
          CommutativePair thisRace = new CommutativePair(thisEvent.iid, e.iid);
          races.add(thisRace);
          System.out.println("Potential race between " + thisRace);
        }
      }
    }
  }

  public void addEvent(Integer iid, Integer thread, Long memory, boolean isWrite, VectorClock vClock, LockSet ls) {
    Event e = new Event(iid, thread, memory, isWrite, vClock, ls);
    Map<Integer, LinkedList<Event>> eventsForThread = eventDB.get(memory);
    if (eventsForThread == null) {
      eventsForThread = new HashMap<Integer, LinkedList<Event>>();
      eventDB.put(memory, eventsForThread);
    }
    LinkedList<Event> eventList = eventsForThread.get(thread);
    if (eventList == null) {
      eventList = new LinkedList<Event>();
      eventsForThread.put(thread, eventList);
    }
    eventList.addFirst(e);
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
