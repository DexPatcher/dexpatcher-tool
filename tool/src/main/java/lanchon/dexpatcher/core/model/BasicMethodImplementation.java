package lanchon.dexpatcher.core.model;

import java.util.List;

import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.TryBlock;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.instruction.Instruction;

public class BasicMethodImplementation implements MethodImplementation {

	private final int registerCount;
	private final Iterable<? extends Instruction> instructions;
	private final List<? extends TryBlock<? extends ExceptionHandler>> tryBlocks;
	private final Iterable<? extends DebugItem> debugItems;

	public BasicMethodImplementation(
			int registerCount,
			Iterable<? extends Instruction> instructions,
			List<? extends TryBlock<? extends ExceptionHandler>> tryBlocks,
			Iterable<? extends DebugItem> debugItems
	) {
		this.registerCount = registerCount;
		this.instructions = instructions;
		this.tryBlocks = tryBlocks;
		this.debugItems = debugItems;
	}

	@Override
	public int getRegisterCount() {
		return registerCount;
	}

	@Override
	public Iterable<? extends Instruction> getInstructions() {
		return instructions;
	}

	@Override
	public List<? extends TryBlock<? extends ExceptionHandler>> getTryBlocks() {
		return tryBlocks;
	}

	@Override
	public Iterable<? extends DebugItem> getDebugItems() {
		return debugItems;
	}

}
