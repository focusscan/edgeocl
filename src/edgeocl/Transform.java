package edgeocl;

import java.io.InputStream;

public abstract class Transform {
	protected InputStream getStreamFor(String filename) {
        return Transform.class.getResourceAsStream(filename);
    }

	protected int roundUp(int groupSize, int globalSize) {
		int r = globalSize % groupSize;
		if (r == 0) {
			return globalSize;
		} else {
			return globalSize + groupSize - r;
		}
	}
}
