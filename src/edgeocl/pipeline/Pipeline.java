package edgeocl.pipeline;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLPlatform;

import bitwise.apps.focusscan.scan.EdgeFile;
import config.Config;

public class Pipeline {
	private static final int SAVE_POOL_SIZE = 6;
	private static final int LOAD_POOL_SIZE = 4;
	private static final int OPENCL_QUEUE_SIZE = 4;
	
	public final Config config;
	public final ArrayList<EdgeFile> edgeManifests;
	public final CLPlatform platform;
	public final CLContext context;
	public final CLCommandQueue queue;
	public final ExecutorService savePool = Executors.newFixedThreadPool(SAVE_POOL_SIZE);
	public final ExecutorService loadPool = Executors.newFixedThreadPool(LOAD_POOL_SIZE);
	public final ArrayBlockingQueue<OpenCLJob> openclQueue = new ArrayBlockingQueue<>(OPENCL_QUEUE_SIZE);
	public final ArrayBlockingQueue<SaveImageTask> saveQueue = new ArrayBlockingQueue<>(2*SAVE_POOL_SIZE);
	public final CountDownLatch latch;
	
	public Pipeline(Config in_config, ArrayList<EdgeFile> in_edgeManifests, CLPlatform in_platform, CLContext in_context, CLCommandQueue in_queue, int outputCount) {
		config = in_config;
		edgeManifests = in_edgeManifests;
		platform = in_platform;
		context = in_context;
		queue = in_queue;
		latch = new CountDownLatch(outputCount);
	}
}
