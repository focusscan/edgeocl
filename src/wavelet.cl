#pragma OPENCL EXTENSION cl_khr_fp64 : enable

#define DISABLE_FORWARD 0
#define DISABLE_REVERSE 0
#define DISABLE_ZERO 0

__kernel void forward(
  __global float *out,
  __global float *in,
  unsigned int width,
  unsigned int height,
  unsigned int lim_width,
  unsigned int lim_height,
  __global float *scalingDecomp,
  __global float *waveletDecomp,
  unsigned int motherWavelength)
 {
	unsigned int c = get_global_id(0);
	
	if (c >= width)
		return;
	
#if (DISABLE_FORWARD)
		for (unsigned int i = 0; i < height; i++) {
			out[c + width * i] = in[c + width * i];
		}
		return;
#endif
	
	if (c >= lim_width) {
		for (unsigned int i = 0; i < height; i++) {
			out[c + width * i] = in[c + width * i];
		}
		return;
	}
	
	unsigned int h = lim_height >> 1;
	for (unsigned int i = 0; i < h; i++) {
		float outl = 0;
		float outh = 0;
		
		for (unsigned int j = 0; j < motherWavelength; j++) {
			unsigned int k = ( i << 1 ) + j;
			while (k >= lim_height)
				k -= lim_height;
			outl += in[c + width * k] * scalingDecomp[j];
			outh += in[c + width * k] * waveletDecomp[j]; 
		}
		
		out[c + width * i]       = outl;
		out[c + width * (i + h)] = outh;
	}
	
	for (unsigned int i = lim_height; i < height; i++) {
		out[c + width * i] = in[c + width * i];
	}
}

__kernel void reverse(
  __global float *out,
  __global float *in,
  unsigned int width,
  unsigned int height,
  unsigned int lim_width,
  unsigned int lim_height,
  __global float *scalingRecomp,
  __global float *waveletRecomp,
  unsigned int motherWavelength)
{
	unsigned int c = get_global_id(0);
	
	if (c >= width)
		return;
	
#if (DISABLE_REVERSE)
		for (unsigned int i = 0; i < height; i++) {
			out[c + width * i] = in[c + width * i];
		}
		return;
#endif
	
	if (c >= lim_width) {
		for (unsigned int i = 0; i < height; i++) {
			out[c + width * i] = in[c + width * i];
		}
		return;
	}
	
	for (unsigned int i = 0; i < lim_height; i++)
		out[c + width * i] = 0;
	
	unsigned int h = lim_height >> 1;
	for (unsigned int i = 0; i < h; i++) {
		float inl = in[c + width * i];
		float inh = in[c + width * (i + h)];
		for (unsigned int j = 0; j < motherWavelength; j++) {
			unsigned int k = ( i << 1 ) + j;
			while (k >= lim_height)
				k -= lim_height;
			out[c + width * k] += inl * scalingRecomp[j] + inh * waveletRecomp[j]; 
		}
	}
	
	for (unsigned int i = lim_height; i < height; i++) {
		out[c + width * i] = in[c + width * i];
	}
}

__kernel void zero(
  __global float *inout,
  unsigned int width,
  unsigned int height,
  unsigned int lim_width,
  unsigned int lim_height)
{
#if (DISABLE_ZERO)
	return;
#endif
	
  	unsigned int c = get_global_id(0);
  	
  	if (c >= lim_width)
  		return;
	
	for (int i = 0; i < lim_height; i++) {
		inout[c + width * i] = 0;
	}
}
