package javato.activetesting;

import java.util.Map;
import java.util.HashMap;

class VectorClock {
  public Map<Integer, Integer> vc = new HashMap<Integer, Integer>();

  VectorClock(Integer thread) {
    this.vc.put(thread, 1);
  }

  public void maximumUpdate(VectorClock rhs) {
    java.util.Set<Integer> threads = new java.util.HashSet<Integer>();
    threads.addAll(this.vc.keySet());
    threads.addAll(rhs.vc.keySet());
    for (Integer i : threads) {
      Integer li = this.vc.get(i);
      Integer ri = rhs.vc.get(i);
      if (li == null) li = 0;
      if (ri == null) ri = 0;
      this.vc.put(i, java.lang.Math.max(li, ri));
    }
  }

  public boolean lessThanEqual(VectorClock rhs) {
    for (Integer i : this.vc.keySet()) {
      Integer ri = rhs.vc.get(i);
      if (ri == null) ri = new Integer(0);
      Integer li = this.vc.get(i);
      if (li > ri) return false;
    }
    return true;
  }

  public void increment(Integer thread) {
    Integer time = this.vc.get(thread);
    if (time == null) {
      time = new Integer(0);
    }
    this.vc.put(thread, time+1);
  }

  public String toString() {
    String s = "[";
    for (Integer i : this.vc.keySet()) {
      s += " t" + i + ": " + this.vc.get(i);
    }
    return s + "]";
  }

  public VectorClock clone() {
    VectorClock newVC = new VectorClock(0);
    newVC.vc.remove(0);
    for (Integer i : this.vc.keySet()) {
      newVC.vc.put(i, this.vc.get(i));
    }
    return newVC;
  }
}

class VectorClockTracker {
  private Map<Integer, VectorClock> threadClocks = new HashMap<Integer, VectorClock>();
  private Map<Integer, VectorClock> lockClocks = new HashMap<Integer, VectorClock>();

  private VectorClock getThread(Integer thread) {
      VectorClock clock = threadClocks.get(thread);
      if (clock == null) {
        clock = new VectorClock(thread);
        threadClocks.put(thread, clock);
      }
      return clock;
  }
  public void startBefore(Integer parent, Integer child) {
    VectorClock parClock = getThread(parent);
    VectorClock childClock = getThread(child);
    System.out.println("In startBefore, original clocks. Parent: " + parClock
        + " Child: " + childClock);

    parClock.increment(parent);
    childClock.maximumUpdate(parClock);
    childClock.increment(child);
    parClock.increment(parent);
    System.out.println("Final clocks. Parent: " + parClock
        + " Child: " + childClock);
  }
  public void joinAfter(Integer parent, Integer child) {
    VectorClock parClock = getThread(parent);
    VectorClock childClock = getThread(child);
    System.out.println("In joinAfter, original clocks. Parent: " + parClock
        + " Child: " + childClock);

    childClock.increment(child);
    parClock.increment(parent);
    parClock.maximumUpdate(childClock);
    parClock.increment(parent);
    System.out.println("Final clocks. Parent: " + parClock
        + " Child: " + childClock);
  }

  public void notifyBefore(Integer thread, Integer lock) {
    VectorClock lc = lockClocks.get(lock);
    if (lc == null) {
      lc = new VectorClock(thread);
      lockClocks.put(lock, lc);
    }
    VectorClock clock = getThread(thread);
    System.out.println("In notify before. initial clock: " + clock);
    clock.increment(thread);
    lc.maximumUpdate(clock);
    System.out.println("lc: " + lc);
    System.out.println("final clock: " + clock);
  }
  public void waitAfter(Integer thread, Integer lock) {
    VectorClock lc = lockClocks.get(lock);
    VectorClock clock = getThread(thread);
    System.out.println("In wait after.  Inital clock: " + clock);
    System.out.println("lc clock: " + lc);
    clock.increment(thread);
    if (lc != null) {
      clock.maximumUpdate(lc);
      System.out.println("final clock: " + clock);
    }
  }

  public VectorClock getVectorClock(Integer thread) {
    VectorClock clock = threadClocks.get(thread);
    if (clock == null) return new VectorClock(thread);
    else return clock;
  }

}
