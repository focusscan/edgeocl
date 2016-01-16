package edgeocl.pipeline;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import edgeocl.FloatImg;

public class LoadImageTask implements Runnable {
	private final Pipeline pipeline;
	private final int imageNumber;
	private final Path imagePath;
	
	public LoadImageTask(Pipeline in_pipeline, int in_imageNumber, Path in_imagePath) {
		pipeline = in_pipeline;
		imageNumber = in_imageNumber;
		imagePath = in_imagePath;
	}
	
	@Override
	public void run() {
		try {
			BufferedImage bi = ImageIO.read(imagePath.toFile());
			FloatImg img = new FloatImg(bi);
			
			OpenCLJob job = new OpenCLJob(imageNumber, img);
			
			System.out.format("Loaded image %d (%s)\n", imageNumber, imagePath);
			
			pipeline.openclQueue.put(job);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
