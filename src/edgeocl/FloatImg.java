package edgeocl;

import java.awt.image.BufferedImage;

public class FloatImg {
	public final int height;
	public final int width;
	public final int type;
	public final float[] arr;
	
	public FloatImg(BufferedImage in) {
		height = in.getHeight();
		width = in.getWidth();
		type = in.getType();
		arr = new float[width * height];
		
		float[] rgb = in.getRaster().getPixels(0, 0, in.getWidth(), in.getHeight(), (float[])null);
		for (int i = 0; i < width * height; i++)
			arr[i] = (rgb[3*i] + rgb[3*i + 1] + rgb[3*i + 2])/3;
	}
	
	float max = -1;
	
	public void stats() {
		for (int i = 0; i < height * width; i++) {
			max = Math.max(max, Math.abs(arr[i]));
		}
		
		float histx[] = new float[10];
		int histy[] = new int[histx.length];
		for (int i = 0; i < 10; i++)
			histx[i] = (float)(max / (1 << (i+1)));
		for (int i = 0; i < arr.length; i++) {
			float v = Math.abs(arr[i]);
			histo: for (int j = 0; j < histx.length; j++) {
				if (v > histx[j]) {
					histy[j]++;
					continue histo;
				}
			}
		}
		
		System.out.println(String.format("%d pixels", arr.length));
		System.out.println(String.format("++ Max: %f", max));
		for (int i = 0; i < histx.length; i++) {
			System.out.println(String.format("++ %f %% (%d) > %f", (float)histy[i] / (float)arr.length, histy[i], histx[i]));
		}
	}
	
	public void rescale(boolean threashold) {
		if (max < 0)
			return;
		
		for (int i = 0; i < arr.length; i++) {
			if (threashold && arr[i] < 5)
				arr[i] = 0;
			else
				arr[i] = Math.min(255f, arr[i] * 255f/max);
		}
	}
	
	public BufferedImage toBufferedImage() {
		BufferedImage ret = new BufferedImage(width, height, type);
		float[] rgb = new float[3 * height * width];
		for (int i = 0; i < height * width; i++) {
			float v = (float) Math.min(255f, Math.abs(arr[i]));
			rgb[3*i]     = v;
			rgb[3*i + 1] = v;
			rgb[3*i + 2] = v;
		}
		ret.getRaster().setPixels(0, 0, width, height, rgb);
		
		return ret;
	}
}
