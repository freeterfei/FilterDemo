package com.visionfunny.gpuimage.notused;

import com.visionfunny.gpuimage.notused.GPUImageBilateralFilter;

/**
 * Created by chenfeifei on 2017/6/21.
 */

public class GPUImageBilateralYFilter extends GPUImageBilateralFilter {

    public GPUImageBilateralYFilter(){
        super();
    }

    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);
    }

    @Override
    protected void setTexelSize(float w, float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[] {0.0f, 2.0f / h});
    }
}
