package javato.activetesting;

import javato.activetesting.lockset.LockSet;
import javato.activetesting.HybridAnalysis;

import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

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
    return x == z || y == z;
  }
}

class Event {
  public Integer iid;
  public Integer thread;
  public Long memory;
  public boolean isRead;
  public VectorClock vClock;
  public LockSet ls;

  Event(Integer iid, Integer thread, Long memory, boolean isRead, VectorClock vClock, LockSet ls) {
    this.iid = iid;
    this.thread = thread;
    this.memory = memory;
    this.isRead = isRead;
    this.vClock = vClock;
    this.ls = ls;
  }

  boolean isRace(Event rhs) {
    return this.thread != rhs.thread && this.memory == rhs.memory
      && !(this.isRead && rhs.isRead) && !this.vClock.lessThanEqual(rhs.vClock)
      && !rhs.vClock.lessThanEqual(this.vClock) && !this.ls.intersects(rhs.ls);
  }
}

class HybridRaceTracker {
  private Map<Long, LinkedList<Event>> eventsAtMem = new TreeMap<Long, LinkedList<Event>>();
  private LinkedHashSet<CommutativePair> races = new LinkedHashSet<CommutativePair>();
  HybridRaceTracker() { }

  public void checkRace(Integer iid, Integer thread, Long memory, boolean isRead, VectorClock vClock, LockSet ls) {
    Event thisEvent = new Event(iid, thread, memory, isRead, vClock, ls);
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

  public void addEvent(Integer iid, Integer thread, Long memory, boolean isRead, VectorClock vClock, LockSet ls) {
    Event e = new Event(iid, thread, memory, isRead, vClock, ls);
    LinkedList<Event> eventStack = eventsAtMem.get(memory);
    if (eventStack == null) {
      eventStack = new LinkedList<Event>();
      eventsAtMem.put(memory, eventStack);
    }
    eventStack.addFirst(e);
  }

//    The following method call creates a file "error.list" containing the list of numbers "1,2,3,...,nRaces"
//    This file is used by run.xml to initialize Parameters.errorId with a number from from the list.
//    Parameters.errorId tells RaceFuzzer the id of the race that RaceFuzzer should try to create
  public int dumpRaces() {
    try {
      java.io.FileOutputStream fos = new java.io.FileOutputStream("error.log");
      java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos);
      oos.writeObject(races);
    } catch (java.io.IOException e) {
      System.out.println("Failed to write to file: " + e);
    }
    return races.size();
  }

}
