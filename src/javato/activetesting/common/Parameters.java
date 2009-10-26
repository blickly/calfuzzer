package javato.activetesting.common;

import java.io.*;

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
public class Parameters {
    public static final long stallCheckerInterval = Long.getLong("javato.activeChecker.stallCheckerInterval", 10);
    public static final long livelockCheckerInterval = Long.getLong("javato.activeChecker.livelockCheckerInterval", 200);
    public static final String iidToLineMapFile = "iidToLine.map";
    public static final String usedIdFile = "javato.usedids";
    public static final int N_VECTOR_CLOCKS_WINDOW = 5;
    public static final int deadlockCycleLength = Integer.getInteger("javato.deadlock.cycle.length", 2);

    public static final String ERROR_STAT_FILE = System.getProperty("javato.activetesting.errorstat.file", "error.stat");
    public static final String ERROR_LOG_FILE = System.getProperty("javato.activetesting.errorlog.file", "error.log");
    public static final String ERROR_LIST_FILE = System.getProperty("javato.activetesting.errorlist.file", "error.list");
    public static final String analysisClass = System.getProperty("javato.activetesting.analysis.class");
    public static final int errorId = Integer.getInteger("javato.activetesting.errorid", -1);

    static public int readInteger(String filename, int defaultVal) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            int ret = Integer.parseInt(in.readLine());
            in.close();
            return ret;
        } catch (Exception e) {
        }
        return 0;
    }

    public static void writeIntegerList(String file, int val) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            for (int i = 1; i < val; i++) {
                pw.print(i + ",");
            }
            if (val > 0)
                pw.println(val);
            else
                pw.println();
            pw.close();
        } catch (IOException e) {
            System.err.println("Error while writing to " + file);
            System.exit(1);
        }

    }


}
