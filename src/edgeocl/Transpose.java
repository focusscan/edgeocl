package edgeocl;

import java.io.IOException;
import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLProgram.CompilerOptions;

public class Transpose extends Transform {
	private static final int BLOCK_DIM = 16;
	private static final int FLOAT_SIZE = 4;
	
	private final CLContext context;
	private final CLCommandQueue queue;
	private final CLProgram program;
	
	public Transpose(CLContext in_context, CLCommandQueue in_queue) throws IOException {
		context = in_context;
		queue = in_queue;
		program = context.createProgram(getStreamFor("/transpose.cl"));
        program.build(CompilerOptions.FAST_RELAXED_MATH);
        assert program.isExecutable();
	}
	
	public void transpose(CLBuffer<FloatBuffer> out, CLBuffer<FloatBuffer> in, Imgdim dim) {
		CLKernel kernel = program.createCLKernel("transpose");
		kernel.putArg(out)
			  .putArg(in)
			  .putArg(0)
			  .putArg(dim.width)
			  .putArg(dim.height)
			  .putNullArg((BLOCK_DIM + 1) * BLOCK_DIM * FLOAT_SIZE);
		queue.put2DRangeKernel(kernel, 0, 0, roundUp(BLOCK_DIM, dim.width), roundUp(BLOCK_DIM, dim.height), BLOCK_DIM, BLOCK_DIM);
		dim.transpose();
	}
}
