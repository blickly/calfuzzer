package javato.activetesting;

import javato.activetesting.analysis.CheckerAnalysisImpl;
import javato.activetesting.common.Parameters;

import java.util.LinkedHashSet;

/**
 * Copyright (c) 2007-2008,
 * Koushik Sen    <ksen@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class RaceFuzzerAnalysis extends CheckerAnalysisImpl {
//    private CommutativePair racePair;

    public void initialize() {
        if (Parameters.errorId >= 0) {
//    Your code goes here.
//    In my implementation I had the following code:
//            LinkedHashSet<CommutativePair> seenRaces = HybridRaceTracker.getRacesFromFile();
//            racePair = (CommutativePair) (seenRaces.toArray())[Parameters.errorId - 1];
        }
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked) {
//  ignore this
    }

    public void methodEnterBefore(Integer iid) {
//  ignore this
    }

    public void methodExitAfter(Integer iid) {
//  ignore this
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {
//  ignore this
    }

    public void waitAfter(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {
//  ignore this
    }

    public void readBefore(Integer iid, Integer thread, Long memory) {
//    Your code goes here.
//    In my implementation I had the following code:
//        if (racePair != null && racePair.contains(iid)) {
//            synchronized (ActiveChecker.lock) {
//                (new RaceChecker(memory, false, iid)).check();
//            }
//            ActiveChecker.blockIfRequired();
//        }
    }

    public void writeBefore(Integer iid, Integer thread, Long memory) {
//    Your code goes here.
//    In my implementation I had the following code:
//        if (racePair != null && racePair.contains(iid)) {
//            synchronized (ActiveChecker.lock) {
//                (new RaceChecker(memory, true, iid)).check();
//            }
//            ActiveChecker.blockIfRequired();
//        }
    }

    public void finish() {
//  ignore this
    }
}
