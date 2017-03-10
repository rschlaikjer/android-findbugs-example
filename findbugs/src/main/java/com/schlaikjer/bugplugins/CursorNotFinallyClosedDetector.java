package com.schlaikjer.bugplugins;

import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import edu.umd.cs.findbugs.BugAccumulator;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.SourceLineAnnotation;

public class CursorNotFinallyClosedDetector extends BytecodeScanningDetector {

    private static final String ANDROID_CURSOR = "Landroid/database/Cursor;";
    private static final String ANDROID_CURSOR_CLASS_CONST_OPERAND = "android/database/Cursor";
    private static final String CLOSE = "close";

    protected final BugAccumulator bugAccumulator;

    private final Map<String, FinallyInfo> suspectFinallys = new HashMap<>();

    private boolean examineMethod = false;

    public CursorNotFinallyClosedDetector(BugReporter bugReporter) {
        this.bugAccumulator = new BugAccumulator(bugReporter);
    }

    @Override
    public void visitMethod(Method method) {
        super.visitMethod(method);
        LocalVariableTable localVariableTable = method.getLocalVariableTable();
        examineMethod = variableTableContainsType(localVariableTable, ANDROID_CURSOR);
        System.out.println("Examine method " + method.getName() + " ? " + examineMethod);
        if (examineMethod) {
            // System.out.println(getMethod().getCode());
        }
    }

    @Override
    public void sawOpcode(int seen) {
        super.sawOpcode(seen);
        if (!examineMethod) {
            return;
        }
        // printOpCode(seen);

        checkIfCursorMethodsCalledOutsideTry(seen);
        checkExceptionHandlersCloseCursors(seen);
    }

    private void checkExceptionHandlersCloseCursors(int seen) {
        // Check if we're in a finally block
        int blockIndex = getFinallyBlockIndex(getMethod(), getPC());
        if (blockIndex < 0) {
            return;
        }

        String finallyReference = getMethodName() + blockIndex;
        if (!suspectFinallys.containsKey(finallyReference)) {
            suspectFinallys.put(finallyReference, new FinallyInfo(
                    getClassName(),
                    getSourceFile(),
                    getCode().getLineNumberTable().getSourceLine(getPC()),
                    getPC()
            ));
        }

        // Not a method call, return
        if (!isMethodCall()) {
            return;
        }

        // If the method is not being called on a cursor, return
        if (!ANDROID_CURSOR_CLASS_CONST_OPERAND.equals(getClassConstantOperand())) {
            return;
        }

        // If the method isn't close, return
        if (!CLOSE.equals(getNameConstantOperand())) {
            return;
        }

        // Mark this finally block as OK
        suspectFinallys.get(finallyReference).callsCursorClose = true;
    }

    private void checkIfCursorMethodsCalledOutsideTry(int seen) {
        // Not a method call, return
        if (!isMethodCall()) {
            return;
        }

        // If the method is not being called on a cursor, return
        if (!ANDROID_CURSOR_CLASS_CONST_OPERAND.equals(getClassConstantOperand())) {
            return;
        }

        // If a method is called on a cursor outside a try block, and that method is not
        // close, then accumulate a bug
        if (!CLOSE.equals(getNameConstantOperand()) && !isInTryBlock(getMethod(), getPC())) {
            System.out.println("Cursor." + getNameConstantOperand() + " called outside of try block!");

            bugAccumulator.accumulateBug(
                    new BugInstance(
                            this,
                            "DB_CURSOR_METHODS_CALLED_OUTSIDE_TRY",
                            HIGH_PRIORITY
                    ).addClassAndMethod(this),
                    this
            );
        }
    }

    /**
     * Finally blocks are defined as the PCs between the handler PC and the next call to athrow,
     * goto or return.
     *
     * @param method
     * @param pc
     * @return
     */
    private int getFinallyBlockIndex(Method method, int pc) {
        CodeException[] exceptionTable = method.getCode().getExceptionTable();
        int blockIndex = 0;
        for (CodeException exception : exceptionTable) {
            if (exception.getHandlerPC() <= pc) {
                int pc2 = pc;
                int codeByte;
                while ((codeByte = getCodeByte(pc2)) != ATHROW && codeByte != ARETURN && codeByte != GOTO && pc2 < getMaxPC()) {
                    pc2++;
                }
                if ((codeByte == ATHROW || codeByte == ARETURN || codeByte == GOTO) && pc < pc2) {
                    return blockIndex;
                }
            }
            blockIndex++;
        }

        return -1;
    }

    private boolean isInTryBlock(Method method, int pc) {
        CodeException[] exceptionTable = method.getCode().getExceptionTable();
        for (CodeException exception : exceptionTable) {
            if (exception.getStartPC() <= pc && pc < exception.getEndPC()) {
                return true;
            }
        }

        return false;
    }

    private static boolean variableTableContainsType(LocalVariableTable table, String type) {
        for (LocalVariable variable : table.getLocalVariableTable()) {
            if (type.equals(variable.getSignature())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void report() {
        for (Map.Entry<String, FinallyInfo> finallyEntry : suspectFinallys.entrySet()) {
            // If the finally calls close, it's OK
            FinallyInfo finallyInfo = finallyEntry.getValue();
            if (finallyInfo.callsCursorClose) {
                continue;
            }

            SourceLineAnnotation bugSLA = new SourceLineAnnotation(
                    finallyInfo.sourceClass,
                    finallyInfo.sourceFileName,
                    finallyInfo.sourceLineNumber,
                    finallyInfo.sourceLineNumber,
                    finallyInfo.bytecodeOffset,
                    finallyInfo.bytecodeOffset
            );

            bugAccumulator.accumulateBug(
                    new BugInstance(
                            this,
                            "DB_CURSOR_NOT_FINALLY_CLOSED",
                            HIGH_PRIORITY
                    ).addClass(getClassDescriptor()).addSourceLine(bugSLA),
                    bugSLA
            );

            // Otherwise, accumulate a bug
            System.out.println(String.format(
                    Locale.ENGLISH,
                    "Finally doesn't close cursor at %s line %d",
                    finallyInfo.sourceFileName,
                    finallyInfo.sourceLineNumber
            ));
        }

        bugAccumulator.reportAccumulatedBugs();
    }

    private static class FinallyInfo {
        boolean callsCursorClose;
        int sourceLineNumber;
        int bytecodeOffset;
        String sourceClass;
        String sourceFileName;

        public FinallyInfo(String sourceClass, String sourceFileName, int sourceLineNumber, int bytecodeOffset) {
            this.callsCursorClose = false;
            this.sourceClass = sourceClass;
            this.sourceFileName = sourceFileName;
            this.sourceLineNumber = sourceLineNumber;
            this.bytecodeOffset = bytecodeOffset;
        }
    }

}
