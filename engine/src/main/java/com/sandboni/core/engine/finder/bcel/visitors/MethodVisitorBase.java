package com.sandboni.core.engine.finder.bcel.visitors;

import com.sandboni.core.engine.sta.Context;
import com.sandboni.core.engine.sta.graph.Link;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

public abstract class MethodVisitorBase extends EmptyVisitor {

    static final int NO_LINE_NUMBER = -1;

    protected final Method method;
    protected final JavaClass javaClass;
    protected final Context context;

    int linksCount = 0;
    protected int currentLineNumber = NO_LINE_NUMBER;

    public MethodVisitorBase(Method m, JavaClass jc, Context c) {
        method = m;
        javaClass = jc;
        context = c;
    }

    private boolean shouldVisitInstruction(Instruction i) {
        short opcode = i.getOpcode();
        return ((InstructionConst.getInstruction(opcode) != null)
                && !(i instanceof ConstantPushInstruction)
                && !(i instanceof ReturnInstruction));
    }

    protected void visitInstructions(Method method) {
        // exact match (i.e. map) doesnt work here
        LineNumber[] lineNumbers = method.getLineNumberTable() != null ? method.getLineNumberTable().getLineNumberTable() : null;
        InstructionHandle[] instructionHandles = new InstructionList(method.getCode().getCode()).getInstructionHandles();
        handleMethodInstructions(lineNumbers, instructionHandles);
    }

    private void handleMethodInstructions(LineNumber[] lineNumbers, InstructionHandle[] instructionHandles) {
        int startLineNumber = NO_LINE_NUMBER;
        for (InstructionHandle ih : instructionHandles) {
            currentLineNumber = NO_LINE_NUMBER;
            if (lineNumbers != null && lineNumbers.length > 0) {
                currentLineNumber = getCurrentLineNumber(lineNumbers, ih);
                if (startLineNumber == NO_LINE_NUMBER) {
                    startLineNumber = currentLineNumber;
                }
            }
            Instruction i = ih.getInstruction();

            //the check 'currentLineNumber >= startLineNumber' is meant to avoid a case where the instruction happens before the constructor
            if (currentLineNumber >= startLineNumber && !shouldVisitInstruction(i)) {
                i.accept(this);
            }
        }
    }

    private int getCurrentLineNumber(LineNumber[] lineNumbers, InstructionHandle ih) {
        for (int i = 0; i < lineNumbers.length - 1; i++) {
            if (lineNumbers[i].getStartPC() <= ih.getPosition() && lineNumbers[i + 1].getStartPC() >= ih.getPosition()) {
                return lineNumbers[i].getLineNumber();
            }
        }
        return lineNumbers[0].getLineNumber();
    }

    protected void addLink(Link link) {
        linksCount = linksCount + context.addLink(link);
    }
}