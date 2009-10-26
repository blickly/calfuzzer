package javato.activetesting.instrumentor;

import javato.instrumentor.Visitor;
import javato.instrumentor.contexts.InvokeContext;
import javato.instrumentor.contexts.RHSContextImpl;
import javato.instrumentor.contexts.RefContext;
import soot.SootMethod;
import soot.Value;
import soot.jimple.*;
import soot.util.Chain;

/**
 * Copyright (c) 2007-2008
 * Pallavi Joshi	<pallavi@cs.berkeley.edu>
 * Koushik Sen <ksen@cs.berkeley.edu>
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
public class VisitorForActiveTesting extends Visitor {

    public VisitorForActiveTesting(Visitor visitor) {
        super(visitor);
    }


    public int getStSize() {
        return st.getSize();
    }

    private Stmt getStmtToBeInstrumented(Chain units, AssignStmt assignStmt, Value leftOp) {
        Stmt cur = assignStmt;
        Stmt succ;
        Stmt last = (Stmt) units.getLast();

        while (cur != last) {
            succ = (Stmt) units.getSuccOf(cur);
            if ((succ != null) && (succ instanceof InvokeStmt)) {
                InvokeExpr iExpr = succ.getInvokeExpr();
                if (iExpr.getMethod().getSubSignature().indexOf("<init>") != -1) {
                    if (((InstanceInvokeExpr) iExpr).getBase() == leftOp) {
                        return succ;
                    }
                }
            }
            cur = succ;
        }
        return assignStmt;
    }

    private Stmt getStmtToBeInstrumented2(SootMethod sm, Chain units, AssignStmt assignStmt,
                                          Value objectOnWhichMethodIsInvoked, Stmt currStmtToBeInstrumented) {
        Stmt cur = assignStmt;
        Stmt succ;
        Stmt last = (Stmt) units.getLast();

        if (sm.getSubSignature().indexOf("<init>") != -1 && objectOnWhichMethodIsInvoked != null) {
            while (cur != last) {
                succ = (Stmt) units.getSuccOf(cur);
                if ((succ != null) && (succ instanceof InvokeStmt)) {
                    InvokeExpr iExpr = succ.getInvokeExpr();
                    if (iExpr.getMethod().getSubSignature().indexOf("<init>") != -1) {
                        if (((InstanceInvokeExpr) iExpr).getBase() == objectOnWhichMethodIsInvoked) {
                            return succ;
                        }
                    }
                }
                cur = succ;
            }
        }
        return currStmtToBeInstrumented;
    }

    private Value getMethodsTargetObject(SootMethod sm, Chain units) {
        Value objectOnWhichMethodIsInvoked = null;
        if (!sm.isStatic()) {
            Stmt startStmt = (Stmt) units.getFirst();
            if (startStmt instanceof IdentityStmt) {
                objectOnWhichMethodIsInvoked = ((IdentityStmt) startStmt).getLeftOp();
            } else {
                System.err.println("First statement within a non-static method is not an IdentityStmt");
                System.exit(-1);
            }
        }
        return objectOnWhichMethodIsInvoked;
    }

    public void visitStmtAssign(SootMethod sm, Chain units, AssignStmt assignStmt) {
        Value leftOp = assignStmt.getLeftOp();
        Value rightOp = assignStmt.getRightOp();
        if ((rightOp instanceof NewExpr)
                || (rightOp instanceof NewArrayExpr)
                || (rightOp instanceof NewMultiArrayExpr)) {
            Stmt stmtToBeInstrumented = getStmtToBeInstrumented(units, assignStmt, leftOp);
            Value objectOnWhichMethodIsInvoked = getMethodsTargetObject(sm, units);

            stmtToBeInstrumented = getStmtToBeInstrumented2(sm, units, assignStmt,
                    objectOnWhichMethodIsInvoked, stmtToBeInstrumented);

            if (objectOnWhichMethodIsInvoked != null) {
                addCallWithObjectObject(units, stmtToBeInstrumented, "myNewExprInANonStaticMethodAfter",
                        leftOp, objectOnWhichMethodIsInvoked, false);
            } else {
                addCallWithObject(units, stmtToBeInstrumented, "myNewExprInAStaticMethodAfter", leftOp, false);
            }
        }
        nextVisitor.visitStmtAssign(sm, units, assignStmt);
    }

    public void visitStmtEnterMonitor(SootMethod sm, Chain units, EnterMonitorStmt enterMonitorStmt) {
        addCallWithObject(units, enterMonitorStmt, "myLockBefore", enterMonitorStmt.getOp(), true);
        nextVisitor.visitStmtEnterMonitor(sm, units, enterMonitorStmt);
    }

    public void visitStmtExitMonitor(SootMethod sm, Chain units, ExitMonitorStmt exitMonitorStmt) {
        addCallWithObject(units, exitMonitorStmt, "myUnlockAfter", exitMonitorStmt.getOp(), false);
        nextVisitor.visitStmtExitMonitor(sm, units, exitMonitorStmt);
    }

    public void visitInstanceInvokeExpr(SootMethod sm, Chain units, Stmt s, InstanceInvokeExpr invokeExpr, InvokeContext context) {
        Value base = invokeExpr.getBase();
        String sig = invokeExpr.getMethod().getSubSignature();
        if (sig.equals("void wait()")) {
            addCallWithObject(units, s, "myWaitAfter", base, false);
        } else if (sig.equals("void wait(long)") || sig.equals("void wait(long,int)")) {
            addCallWithObject(units, s, "myWaitAfter", base, false);
        } else if (sig.equals("void notify()")) {
            addCallWithObject(units, s, "myNotifyBefore", base, true);
        } else if (sig.equals("void notifyAll()")) {
            addCallWithObject(units, s, "myNotifyAllBefore", base, true);
        } else if (sig.equals("void start()") && isThreadSubType(invokeExpr.getMethod().getDeclaringClass())) {
            addCallWithObject(units, s, "myStartBefore", base, true);
        } else if (sig.equals("void join()") && isThreadSubType(invokeExpr.getMethod().getDeclaringClass())) {
            addCallWithObject(units, s, "myJoinAfter", base, false);
        } else if ((sig.equals("void join(long)") || sig.equals("void join(long,int)"))
                && isThreadSubType(invokeExpr.getMethod().getDeclaringClass())) {
            addCallWithObject(units, s, "myJoinAfter", base, false);
        }

        nextVisitor.visitInstanceInvokeExpr(sm, units, s, invokeExpr, context);

        addCall(units, s, "myMethodEnterBefore", true);
        addCall(units, s, "myMethodExitAfter", false);
        if (invokeExpr.getMethod().getSubSignature().indexOf("<init>") == -1) {
            String ssig = invokeExpr.getMethod().getSubSignature();
            ssig = ssig.substring(ssig.indexOf(' ') + 1);
            Value sig2 = StringConstant.v(ssig);
            addCallWithObjectString(units, s, "myLockBefore", base, sig2, true);
            // t = t.syncMethod() is problematic, so do not pass t
            addCall(units, s, "myUnlockAfter", false);
        }
    }

    public void visitStaticInvokeExpr(SootMethod sm, Chain units, Stmt s, StaticInvokeExpr invokeExpr, InvokeContext context) {
        nextVisitor.visitStaticInvokeExpr(sm, units, s, invokeExpr, context);
        addCall(units, s, "myMethodEnterBefore", true);
        addCall(units, s, "myMethodExitAfter", false);
        if (invokeExpr.getMethod().isSynchronized()) {
            addCallWithInt(units, s, "myLockBefore",
                    IntConstant.v(st.get(invokeExpr.getMethod().getDeclaringClass().getName())), true);
            addCallWithInt(units, s, "myUnlockAfter",
                    IntConstant.v(st.get(invokeExpr.getMethod().getDeclaringClass().getName())), false);
        }
    }


    public void visitArrayRef(SootMethod sm, Chain units, Stmt s, ArrayRef arrayRef, RefContext context) {
        if (context == RHSContextImpl.getInstance()) {
            addCallWithObjectInt(units, s, "myReadBefore", arrayRef.getBase(), arrayRef.getIndex(), true);
        } else {
            addCallWithObjectInt(units, s, "myWriteBefore", arrayRef.getBase(), arrayRef.getIndex(), true);
        }
        nextVisitor.visitArrayRef(sm, units, s, arrayRef, context);
    }

    public void visitInstanceFieldRef(SootMethod sm, Chain units, Stmt s, InstanceFieldRef instanceFieldRef, RefContext context) {
        if (!sm.getName().equals("<init>") || !instanceFieldRef.getField().getName().equals("this$0")) {
            Value v = IntConstant.v(st.get(instanceFieldRef.getField().getName()));
            if (context == RHSContextImpl.getInstance()) {
                addCallWithObjectInt(units, s, "myReadBefore", instanceFieldRef.getBase(), v, true);
            } else {
                addCallWithObjectInt(units, s, "myWriteBefore", instanceFieldRef.getBase(), v, true);
            }
        }
        nextVisitor.visitInstanceFieldRef(sm, units, s, instanceFieldRef, context);
    }


    public void visitStaticFieldRef(SootMethod sm, Chain units, Stmt s, StaticFieldRef staticFieldRef, RefContext context) {
        Value v1 = IntConstant.v(st.get(staticFieldRef.getField().getDeclaringClass().getName()));
        Value v2 = IntConstant.v(st.get(staticFieldRef.getField().getName()));
        if (context == RHSContextImpl.getInstance()) {
            addCallWithIntInt(units, s, "myReadBefore", v1, v2, true);
        } else {
            addCallWithIntInt(units, s, "myWriteBefore", v1, v2, true);
        }
        nextVisitor.visitStaticFieldRef(sm, units, s, staticFieldRef, context);
    }


}
