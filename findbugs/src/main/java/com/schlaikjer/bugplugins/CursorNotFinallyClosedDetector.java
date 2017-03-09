package com.schlaikjer.bugplugins;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugAccumulator;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

public class CursorNotFinallyClosedDetector extends BytecodeScanningDetector {

    private static final String ANDROID_CURSOR = "Landroid/database/Cursor;";

    protected final BugAccumulator bugAccumulator;

    public CursorNotFinallyClosedDetector(BugReporter bugReporter) {
        this.bugAccumulator = new BugAccumulator(bugReporter);
    }

    @Override
    public void visitMethod(Method method) {
        super.visitMethod(method);
        LocalVariableTable localVariableTable = method.getLocalVariableTable();
        if (variableTableContainsType(localVariableTable, ANDROID_CURSOR)) {
            System.out.println(method.getCode());
        }
    }

    private static boolean variableTableContainsType(LocalVariableTable table, String type) {
        for (LocalVariable variable : table.getLocalVariableTable()) {
            if (type.equals(variable.getSignature())) {
                return true;
            }
        }
        return false;
    }

}
