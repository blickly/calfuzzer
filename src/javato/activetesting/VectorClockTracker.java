package javato.activetesting;

import java.util.Map;
import java.util.HashMap;

class VectorClock {
  public static final int MAX_THREADS = 155;
  public Map<Integer, Integer> vc = new HashMap<Integer, Integer>();

  VectorClock() {}
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
      if (ri == null) continue;
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
  private VectorClock[] vectorClocks = new VectorClock[VectorClock.MAX_THREADS];
  private Map<Integer, VectorClock> threadClocks = new HashMap<Integer, VectorClock>();
  private Map<Integer, VectorClock> lockClocks = new HashMap<Integer, VectorClock>();

  VectorClockTracker() {
    for (int i = 0; i < VectorClock.MAX_THREADS; ++i) {
      vectorClocks[i] = new VectorClock();
      vectorClocks[i].increment(i);
    }
  }

  public void startBefore(Integer parent, Integer child) {
    vectorClocks[child].maximumUpdate(vectorClocks[parent]);
    vectorClocks[child].increment(child);
    vectorClocks[parent].increment(parent);
  }
  public void joinAfter(Integer parent, Integer child) {
    vectorClocks[parent].maximumUpdate(vectorClocks[child]);
    vectorClocks[parent].increment(parent);
    vectorClocks[child].increment(child);
  }

  public void notifyBefore(Integer thread, Integer lock) {
    VectorClock lc = lockClocks.get(lock);
    lockClocks.remove(lock);
    if (lc != null) {
      vectorClocks[thread].maximumUpdate(lc);
      vectorClocks[thread].increment(thread);
    }
  }
  public void waitAfter(Integer thread, Integer lock) {
    VectorClock lc = lockClocks.get(lock);
    if (lc != null) {
      lc.maximumUpdate(vectorClocks[thread]);
      vectorClocks[thread].increment(thread);
    } else {
      lc = vectorClocks[thread];
    }
    lockClocks.put(lock, lc);
  }

  public VectorClock getVectorClock(Integer thread) {
    return vectorClocks[thread];
  }

}
