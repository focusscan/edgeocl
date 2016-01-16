package edgeocl;

import java.io.IOException;
import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLProgram.CompilerOptions;

import jwave.transforms.wavelets.Wavelet;

public class DWT extends Transform {
	private static CLProgram program = null;
	
	private final CLContext context;
	private final CLCommandQueue queue;
	private final Wavelet wavelet;
	
	private final CLBuffer<FloatBuffer> scalingDecomp;
	private final CLBuffer<FloatBuffer> waveletDecomp;
	private final CLBuffer<FloatBuffer> scalingRecomp;
	private final CLBuffer<FloatBuffer> waveletRecomp;
	
	private static float[] toFloatArr(double[] in) {
		float[] ret = new float[in.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = (float)in[i];
		return ret;
	}
	
	public DWT(CLContext in_context, CLCommandQueue in_queue, Wavelet in_wavelet) throws IOException {
		context = in_context;
		queue = in_queue;
		if (null == program) {
			program = context.createProgram(getStreamFor("/wavelet.cl"));
	        program.build(CompilerOptions.FAST_RELAXED_MATH);
	        assert program.isExecutable();
		}
		wavelet = in_wavelet;
		
		float _scalingDecomp[] = toFloatArr(wavelet.getScalingDeComposition());
		float _waveletDecomp[] = toFloatArr(wavelet.getWaveletDeComposition());
		float _scalingRecomp[] = toFloatArr(wavelet.getScalingReConstruction());
		float _waveletRecomp[] = toFloatArr(wavelet.getWaveletReConstruction());
		
		scalingDecomp = context.createBuffer(Buffers.newDirectFloatBuffer(_scalingDecomp), CLBuffer.Mem.READ_ONLY);
		waveletDecomp = context.createBuffer(Buffers.newDirectFloatBuffer(_waveletDecomp), CLBuffer.Mem.READ_ONLY);
		scalingRecomp = context.createBuffer(Buffers.newDirectFloatBuffer(_scalingRecomp), CLBuffer.Mem.READ_ONLY);
		waveletRecomp = context.createBuffer(Buffers.newDirectFloatBuffer(_waveletRecomp), CLBuffer.Mem.READ_ONLY);
		
		queue.putWriteBuffer(scalingDecomp, true);
		queue.putWriteBuffer(waveletDecomp, true);
		queue.putWriteBuffer(scalingRecomp, true);
		queue.putWriteBuffer(waveletRecomp, true);
		
		queue.finish();
	}
	
	public void forward(CLBuffer<FloatBuffer> out, CLBuffer<FloatBuffer> in, Imgdim dim, Imgdim lim_dim) {
		assert (0 == lim_dim.width % 2);
		assert (0 == lim_dim.height % 2);
		
		CLKernel kernel = program.createCLKernel("forward");
		kernel.putArg(out)
			  .putArg(in)
			  .putArg(dim.width)
			  .putArg(dim.height)
			  .putArg(lim_dim.width)
			  .putArg(lim_dim.height)
			  .putArg(scalingDecomp)
			  .putArg(waveletDecomp)
			  .putArg(wavelet.getMotherWavelength());
		int localWorkSize = queue.getDevice().getMaxWorkGroupSize();
        int globalWorkSize = roundUp(localWorkSize, dim.width);
		queue.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize);
		queue.finish();
	}
	
	public void reverse(CLBuffer<FloatBuffer> out, CLBuffer<FloatBuffer> in, Imgdim dim, Imgdim lim_dim) {
		assert (0 == lim_dim.width % 2);
		assert (0 == lim_dim.height % 2);
		
		CLKernel kernel = program.createCLKernel("reverse");
		kernel.putArg(out)
			  .putArg(in)
			  .putArg(dim.width)
			  .putArg(dim.height)
			  .putArg(lim_dim.width)
			  .putArg(lim_dim.height)
			  .putArg(scalingRecomp)
			  .putArg(waveletRecomp)
			  .putArg(wavelet.getMotherWavelength());
		int localWorkSize = queue.getDevice().getMaxWorkGroupSize();
        int globalWorkSize = roundUp(localWorkSize, dim.width);
		queue.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize);
		queue.finish();
	}
	
	public void zero(CLBuffer<FloatBuffer> inout, Imgdim dim, Imgdim lim_dim) {
		CLKernel kernel = program.createCLKernel("zero");
		kernel.putArg(inout)
			  .putArg(dim.width)
			  .putArg(dim.height)
			  .putArg(lim_dim.width)
			  .putArg(lim_dim.height);
		int localWorkSize = queue.getDevice().getMaxWorkGroupSize();
        int globalWorkSize = roundUp(localWorkSize, dim.width);
		queue.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize);
		queue.finish();
	}
}
