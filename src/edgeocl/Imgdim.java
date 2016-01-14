package edgeocl;

public class Imgdim {
	public int width;
	public int height;
	
	public Imgdim(Imgdim o) {
		width = o.width;
		height = o.height;
	}
	
	public Imgdim(int in_width, int in_height) {
		width = in_width;
		height = in_height;
	}
	
	public Imgdim(FloatImg in) {
		width = in.width;
		height = in.height;
	}
	
	public void transpose() {
		int w = height;
		int h = width;
		width = w;
		height = h;
	}
	
	public void half() {
		assert (0 == width % 2);
		assert (0 == height % 2);
		
		width = width / 2;
		height = height / 2;
	}
	
	public void twice() {
		width = width * 2;
		height = height * 2;
	}
}
