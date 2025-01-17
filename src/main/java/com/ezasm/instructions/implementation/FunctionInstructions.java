package com.ezasm.instructions.implementation;

import com.ezasm.instructions.Instruction;
import com.ezasm.instructions.targets.input.IAbstractInput;
import com.ezasm.instructions.targets.input.LabelReferenceInput;
import com.ezasm.instructions.targets.inputoutput.RegisterInputOutput;
import com.ezasm.simulation.*;
import com.ezasm.simulation.exception.SimulationException;
import com.ezasm.simulation.transform.Transformation;
import com.ezasm.simulation.transform.TransformationSequence;
import com.ezasm.simulation.transform.transformable.InputOutputTransformable;
import com.ezasm.util.RawData;

/**
 * An implementation of standard function call instructions for the simulation.
 */
public class FunctionInstructions {

    private final Simulator simulator;
    private final MemoryInstructions memoryInstructions;

    /**
     * Some instructions require access to the Simulator directly, so that is provided.
     *
     * @param simulator the provided Simulator.
     */
    public FunctionInstructions(Simulator simulator) {
        this.simulator = simulator;
        this.memoryInstructions = new MemoryInstructions(simulator);
    }

    /**
     * The standard jump operation: sets the PC to the given line number.
     *
     * @param input the line to jump to.
     * @throws SimulationException if there is an error in accessing the simulation.
     */
    @Instruction
    public TransformationSequence jump(IAbstractInput input) throws SimulationException {
        InputOutputTransformable pc = new InputOutputTransformable(simulator, new RegisterInputOutput(Registers.PC));
        return new TransformationSequence(pc.transformation(input.get(simulator)));
    }

    /**
     * The standard jump operation: sets the PC to the given line number.
     *
     * @param input the line to jump to.
     * @throws SimulationException if there is an error in accessing the simulation.
     */
    @Instruction
    public TransformationSequence j(IAbstractInput input) throws SimulationException {
        return jump(input);
    }

    /**
     * The jump and link operation: sets the PC to the given line number and stores the return address. Stores the
     * previous return address onto the stack.
     *
     * @param input the line to jump to.
     * @throws SimulationException if there is an error in accessing the simulation.
     */
    @Instruction
    public TransformationSequence call(IAbstractInput input) throws SimulationException {
        RegisterInputOutput ra = new RegisterInputOutput(Registers.RA);
        InputOutputTransformable raio = new InputOutputTransformable(simulator, ra);
        RegisterInputOutput fi = new RegisterInputOutput(Registers.FID);
        InputOutputTransformable fiio = new InputOutputTransformable(simulator, fi);
        Register pc = simulator.getRegisters().getRegister(Registers.PC);
        TransformationSequence t = new TransformationSequence();

        t = t.concatenate(memoryInstructions.push(ra));
        t = t.concatenate(new TransformationSequence(raio.transformation(pc.getData())));

        // If we are jumping via a label, push the potentially new file id to the stack
        t = t.concatenate(memoryInstructions.consecutivePush(fi, 1));
        if (input instanceof LabelReferenceInput l) {
            long nextFileId = l.getLabelFileId(simulator).intValue();
            t = t.concatenate(new TransformationSequence(fiio.transformation(new RawData(nextFileId))));
        }
        t = t.concatenate(jump(input));

        return t;
    }

    /**
     * The jump and link operation: sets the PC to the given line number and stores the return address. Stores the
     * previous return address onto the stack.
     *
     * @param input the line to jump to.
     * @throws SimulationException if there is an error in accessing the simulation.
     */
    @Instruction
    public TransformationSequence jal(IAbstractInput input) throws SimulationException {
        return call(input);
    }

    /**
     * Return to the current return address and then pops the next return address off of the stack.
     *
     * @throws SimulationException if there is an error in accessing the simulation.
     */
    @Instruction
    public TransformationSequence _return() throws SimulationException {
        TransformationSequence t = new TransformationSequence();

        t = t.concatenate(jump(new RegisterInputOutput(Registers.RA)));
        t = t.concatenate(memoryInstructions.pop(new RegisterInputOutput(Registers.FID)));
        t = t.concatenate(memoryInstructions.consecutivePop(new RegisterInputOutput(Registers.RA), 1));
        return t;
    }

    /**
     * Exit the program naturally and sets the exit code to the given value.
     *
     * @param input the status code.
     *
     * @throws SimulationException if there is an error in accessing the simulation.
     */
    @Instruction
    public TransformationSequence exit(IAbstractInput input) throws SimulationException {
        InputOutputTransformable r0 = new InputOutputTransformable(simulator, new RegisterInputOutput(Registers.R0));
        InputOutputTransformable pc = new InputOutputTransformable(simulator, new RegisterInputOutput(Registers.PC));
        Transformation t1 = r0.transformation(input.get(simulator));
        Transformation t2 = pc.transformation(new RawData(simulator.endPC()));
        return new TransformationSequence(t1, t2);
    }

}
