package edgeocl.pipeline;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import edgeocl.FloatImg;
import edgeocl.Imgdim;

public class SaveImageTask implements Runnable {
	private final Pipeline pipeline;
	private final int imgType;
	private final float[] img;
	private final Imgdim dim;
	private final Path imagePath;
	
	public SaveImageTask(Pipeline in_pipeline, int in_imgType, float[] in_img, Imgdim in_dim, Path in_imagePath) {
		pipeline = in_pipeline;
		imgType = in_imgType;
		img = in_img;
		dim = in_dim;
		imagePath = in_imagePath;
	}
	
	@Override
	public void run() {
		try {
			FloatImg fi = new FloatImg(imgType, img, dim);
			BufferedImage bi = fi.toBufferedImage();
			ImageIO.write(bi, "BMP", imagePath.toFile());
			
			System.out.format("Saved (%s)\n", imagePath);
			
			pipeline.latch.countDown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
