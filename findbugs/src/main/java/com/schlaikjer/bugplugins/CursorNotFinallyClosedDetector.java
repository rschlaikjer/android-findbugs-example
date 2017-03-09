package com.schlaikjer.bugplugins;

import edu.umd.cs.findbugs.BugAccumulator;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class CursorNotFinallyClosedDetector extends OpcodeStackDetector {

    protected final BugAccumulator bugAccumulator;

    public CursorNotFinallyClosedDetector(BugReporter bugReporter) {
        this.bugAccumulator = new BugAccumulator(bugReporter);
    }

    @Override
    public void sawOpcode(int seen) {

    }

}
