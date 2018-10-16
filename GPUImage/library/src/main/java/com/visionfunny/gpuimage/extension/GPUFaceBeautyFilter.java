package com.visionfunny.gpuimage.extension;

import android.opengl.GLES20;

import com.visionfunny.gpuimage.base.GPUImageFilter;
import com.visionfunny.gpuimage.base.GPUImageFilterGroup;

import java.nio.FloatBuffer;
import java.util.List;

/**
 * Created by chenfeifei on 2017/6/21.
 */

public class GPUFaceBeautyFilter extends GPUImageFilterGroup {

    public GPUFaceBeautyFilter(List<GPUImageFilter> filters) {
        super(filters);
    }

    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);

        if (mMergedFilters != null && mMergedFilters.size() > 0) {
            for(GPUImageFilter filter: mMergedFilters) {
                if(filter instanceof GPUImageCombinationFilter) {
                    ((GPUImageCombinationFilter)filter).setGLTextureId2(mFrameBufferTextures[0]);
                }
            }
        }
    }

    @Override
    public void onDraw(final int textureId, final FloatBuffer cubeBuffer,
                       final FloatBuffer textureBuffer) {
        runPendingOnDrawTasks();
        android.util.Log.d("cff", "onDraw running");

        if (!isInitialized() || mFrameBuffers == null || mFrameBufferTextures == null) {
            android.util.Log.d("cff", "onDraw return");
            return;
        }
        if (mMergedFilters != null) {
            int size = mMergedFilters.size();
            int previousTexture = textureId;
            for (int i = 0; i < size; i++) {
                GPUImageFilter filter = mMergedFilters.get(i);
                boolean isNotLast = i < size - 1;
                if (isNotLast) {
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[i]);
                    GLES20.glClearColor(1, 0, 0, 0);
                }

                if (i == 0) {
                    filter.onDraw(previousTexture, cubeBuffer, textureBuffer);
                } else if (i == size - 1) {
                    filter.onDraw(previousTexture, mGLCubeBuffer, (size % 2 == 0) ? mGLTextureBuffer : mGLTextureBuffer);
                } else {
                    filter.onDraw(previousTexture, mGLCubeBuffer, mGLTextureBuffer);
                }

                if (isNotLast) {
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                    previousTexture = mFrameBufferTextures[i];
                }
            }
        }
    }
}
