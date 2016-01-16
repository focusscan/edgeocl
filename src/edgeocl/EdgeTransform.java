package edgeocl;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.ArrayList;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;

import bitwise.apps.focusscan.scan.EdgeFile;
import config.ConfigFilter;
import edgeocl.pipeline.OpenCLJob;
import edgeocl.pipeline.Pipeline;
import edgeocl.pipeline.SaveImageTask;

public class EdgeTransform implements Runnable {
	private final Pipeline pipeline;
	private final Transpose trans;
	private final ArrayList<DWT> wavelets = new ArrayList<>();
	
	public EdgeTransform(Pipeline in_pipeline) throws IOException {
		pipeline = in_pipeline;
		trans = new Transpose(pipeline.context, pipeline.queue);
		for (ConfigFilter filter : pipeline.config.getFilters()) {
			wavelets.add(new DWT(pipeline.context, pipeline.queue, filter.getWavelet()));
		}
	}
	
	private volatile boolean abort = false;
	
	public void stop() {
		abort = true;
	}

	@Override
	public void run() {
		try {
			while (!abort) {
				OpenCLJob job = pipeline.openclQueue.take();
				FloatImg img = job.getImg();
				
				CLBuffer<FloatBuffer> bimg = pipeline.context.createBuffer(Buffers.newDirectFloatBuffer(img.arr), CLBuffer.Mem.READ_ONLY);
				pipeline.queue.putWriteBuffer(bimg, true);
				
				for (int i = 0; i < pipeline.config.getFilters().size(); i++) {
					ConfigFilter filter = pipeline.config.getFilters().get(i);
					EdgeFile edgeFile = pipeline.edgeManifests.get(i);
					DWT dwt = wavelets.get(i);
					
					CLBuffer<FloatBuffer> b1 = pipeline.context.createFloatBuffer(img.arr.length, CLBuffer.Mem.READ_WRITE);
					CLBuffer<FloatBuffer> b2 = pipeline.context.createFloatBuffer(img.arr.length, CLBuffer.Mem.READ_WRITE);
					
					int steps = filter.getSteps();
					Imgdim dim = new Imgdim(img);
					Imgdim lim_dim = new Imgdim(img);
					
					trans.copy(b1, bimg, dim);
					forward(pipeline.context, pipeline.queue, trans, dwt, b1, b2, dim, lim_dim, steps);
					lim_dim.half();
					dwt.zero(b2, dim, lim_dim);
					lim_dim.twice();
					reverse(pipeline.context, pipeline.queue, trans, dwt, b1, b2, dim, lim_dim, steps);
					
					float[] edgeArr = new float[img.arr.length];
					pipeline.queue.putReadBuffer(b1, true);
					b1.getBuffer().get(edgeArr);
					
					b1.release();
					b2.release();
					
					Path outPath = filter.getPath().resolve(edgeFile.getDataByImageNumber(job.getImageNumber()).getPath());
					SaveImageTask sit = new SaveImageTask(pipeline, img.type, edgeArr, dim, outPath);
					pipeline.saveQueue.put(sit);
					pipeline.savePool.submit(new Runnable() {
						@Override
						public void run() {
							try {
								pipeline.saveQueue.take().run();
							} catch (InterruptedException e) {
							}
						}
					});
				}
				
				bimg.release();
			}
		} catch (InterruptedException e) {
		}
	}
	
	public static void forward(CLContext context, CLCommandQueue queue, Transpose trans, DWT dwt, CLBuffer<FloatBuffer> b1, CLBuffer<FloatBuffer> b2, Imgdim dim, Imgdim lim_dim, int steps) {
		for (int i = 0; i < steps; i++) {
			dwt.forward(b2, b1, dim, lim_dim);
			
			trans.transpose(b1, b2, dim);
			lim_dim.transpose();
			
			dwt.forward(b2, b1, dim, lim_dim);
			
			if (i+1 < steps) {
				trans.transpose(b1, b2, dim);
				lim_dim.transpose();
				lim_dim.half();
			}
		}
	}
	
	public static void reverse(CLContext context, CLCommandQueue queue, Transpose trans, DWT dwt, CLBuffer<FloatBuffer> b1, CLBuffer<FloatBuffer> b2, Imgdim dim, Imgdim lim_dim, int steps) {
		for (int i = steps; i > 0; i--) {
			if (i < steps) {
				trans.transpose(b2, b1, dim);
				lim_dim.transpose();
				lim_dim.twice();
			}
			
			dwt.reverse(b1, b2, dim, lim_dim);
			
			trans.transpose(b2, b1, dim);
			lim_dim.transpose();
			
			dwt.reverse(b1, b2, dim, lim_dim);
		}
	}
}
