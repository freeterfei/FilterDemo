package com.visionfunny.gpuimage.extension;

import android.opengl.GLES20;

import com.visionfunny.gpuimage.base.GPUImageTwoInputFilter;

/**
 * Created by chenfeifei on 2017/6/21.
 */

public class GPUImageCombinationFilter extends GPUImageTwoInputFilter {

    public static final String IMAGE_COMBINATION_FRAGMENT_SHADER =
            " varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            " \n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " uniform mediump float smoothDegree;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "   highp vec4 bilateral = texture2D(inputImageTexture, textureCoordinate);\n" +
            "   highp vec4 origin = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "   \n" +
            "   highp vec4 smooth;\n" +
            "   lowp float r = origin.r;\n" +
            "   lowp float g = origin.g;\n" +
            "   lowp float b = origin.b;\n" +
            "   if(r > 0.3725 && g > 0.1568 && b > 0.0784 && r > b && (max(max(r, g), b) - min(min(r, g), b)) > 0.0588 && abs(r-g) > 0.0588) {\n" +
            "       smooth = vec4(1.0, 0.0, 0.0, 1.0);\n" +
            "   } else {\n" +
            "       smooth = origin;\n" +
            "   }\n" +
            "   gl_FragColor = bilateral;\n" +
            " }";

    //            "       smooth = (1.0 - smoothDegree) * (origin - bilateral) + bilateral;\n" +


    private float mSmoothDegree;
    private int mSmoothDegreeLocation;

    public GPUImageCombinationFilter() {
        this(0.8f);
    }

    public GPUImageCombinationFilter(float smoothDegree) {
        super(IMAGE_COMBINATION_FRAGMENT_SHADER);
        mSmoothDegree = smoothDegree;
    }

    @Override
    public void onInit() {
        super.onInit();
        mSmoothDegreeLocation = GLES20.glGetUniformLocation(getProgram(), "smoothDegree");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setSmoothDegree(mSmoothDegree);
    }

    public void setSmoothDegree(final float newValue) {
        mSmoothDegree = newValue;
        setFloat(mSmoothDegreeLocation, newValue);
    }


    public void setGLTextureId2(final int textureID) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                mGLTextureId2 = textureID;
            }
        });
    }
}
