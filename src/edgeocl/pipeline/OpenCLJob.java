package edgeocl.pipeline;

import edgeocl.FloatImg;

public class OpenCLJob {
	private final int imageNumber;
	private final FloatImg img;
	
	public OpenCLJob(int in_imageNumber, FloatImg in_img) {
		imageNumber = in_imageNumber;
		img = in_img;
	}
	
	public int getImageNumber() {
		return imageNumber;
	}
	
	public FloatImg getImg() {
		return img;
	}
}
