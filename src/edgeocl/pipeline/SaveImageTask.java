package edgeocl.pipeline;

import java.nio.file.Path;

import edgeocl.Imgdim;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;

public class SaveImageTask implements Runnable {
	private final Pipeline pipeline;
	private final float[] img;
	private final long[] dim;
	private final Path imagePath;
	
/*    static
    {
        try {
            System.loadLibrary("jhdf5");
            H5.H5open();
            System.out.println("H5 loaded");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. Unsatisfied Link Error\n" + e);
        } catch (Exception e) {
            System.err.println("Native code library failed to load. \n" + e);
        }
    }*/
	
	public SaveImageTask(Pipeline in_pipeline, int in_imgType, float[] in_img, Imgdim in_dim, Path in_imagePath) {
		pipeline = in_pipeline;
		img = in_img;
		dim = new long[]{ in_dim.width, in_dim.height };
		imagePath = in_imagePath;
	}
	
	@Override
	public void run() {
		try {
			
			int file_id = H5.H5Fcreate(imagePath.toString(), HDF5Constants.H5F_ACC_TRUNC, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
			int dataspace_id = H5.H5Screate_simple(2, dim, null);
			int dataset_id = H5.H5Dcreate(file_id, "/img_data", HDF5Constants.H5T_NATIVE_FLOAT, dataspace_id, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
			H5.H5Dwrite(dataset_id, HDF5Constants.H5T_NATIVE_FLOAT, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, img);
			H5.H5Dclose(dataset_id);
			H5.H5Fclose(file_id);
			
			System.out.format("Saved (%s)\n", imagePath);
			
			pipeline.latch.countDown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
