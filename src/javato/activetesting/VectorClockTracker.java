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
}

class VectorClockTracker {
  private Map<Integer, VectorClock> threadClocks = new HashMap<Integer, VectorClock>();
  private Map<Integer, VectorClock> lockClocks = new HashMap<Integer, VectorClock>();

  public void startBefore(Integer parent, Integer child) {
    VectorClock parClock = threadClocks.get(parent);
    if (parClock == null) parClock = new VectorClock(parent);
    VectorClock childClock = threadClocks.get(child);
    if (childClock == null) childClock = new VectorClock(child);

    childClock.maximumUpdate(parClock);
    childClock.increment(child);
    parClock.increment(parent);

    threadClocks.put(child, childClock);
    threadClocks.put(parent, parClock);
  }
  public void joinAfter(Integer parent, Integer child) {
    VectorClock parClock = threadClocks.get(parent);
    if (parClock == null) parClock = new VectorClock(parent);
    VectorClock childClock = threadClocks.get(child);
    if (childClock == null) childClock = new VectorClock(child);

    parClock.maximumUpdate(childClock);
    childClock.increment(child);
    parClock.increment(parent);

    threadClocks.put(child, childClock);
    threadClocks.put(parent, parClock);
  }

  public void notifyBefore(Integer thread, Integer lock) {
    VectorClock lc = lockClocks.get(lock);
    lockClocks.remove(lock);
    if (lc != null) {
      VectorClock clock = threadClocks.get(thread);
      if (clock == null) clock = new VectorClock(thread);

      clock.maximumUpdate(lc);
      clock.increment(thread);
    }
  }
  public void waitAfter(Integer thread, Integer lock) {
    VectorClock lc = lockClocks.get(lock);
    VectorClock clock = threadClocks.get(thread);
    if (clock == null) clock = new VectorClock(thread);
    if (lc != null) {
      lc.maximumUpdate(clock);
    } else {
      lc = clock;
    }
    clock.increment(thread);
    lockClocks.put(lock, lc);
  }

  public VectorClock getVectorClock(Integer thread) {
    VectorClock clock = threadClocks.get(thread);
    if (clock == null) return new VectorClock(thread);
    else return clock;
  }

}
