package edgeocl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import bitwise.apps.focusscan.scan.EdgeFile;
import config.ConfigFilter;
import edgeocl.pipeline.OpenCLJob;
import edgeocl.pipeline.Pipeline;
import edgeocl.pipeline.SaveImageTask;

public class EdgeTransform implements Runnable {
	private final Pipeline pipeline;
	private final ArrayList<DWT> wavelets = new ArrayList<>();
	
	public EdgeTransform(Pipeline in_pipeline) throws IOException {
		pipeline = in_pipeline;
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
				Imgdim dim = new Imgdim(img);
				
				for (int i = 0; i < pipeline.config.getFilters().size(); i++) {
					ConfigFilter filter = pipeline.config.getFilters().get(i);
					EdgeFile edgeFile = pipeline.edgeManifests.get(i);
					Path outPath = filter.getPath().resolve(edgeFile.getDataByImageNumber(job.getImageNumber()).getPath());
					SaveImageTask sit = new SaveImageTask(pipeline, img.type, img.arr, dim, outPath);
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
			}
		} catch (InterruptedException e) {
		}
	}
}
