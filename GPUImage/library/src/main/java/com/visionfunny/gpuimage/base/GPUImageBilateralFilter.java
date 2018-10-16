package com.visionfunny.gpuimage.base;

import android.opengl.GLES20;

/**
 * Created by chenfeifei on 2017/6/22.
 */

public class GPUImageBilateralFilter extends GPUImageFilterGroup {

    public static final String BILATERAL_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +

            "const int GAUSSIAN_SAMPLES = 9;\n" +

            "uniform vec2 singleStepOffset;\n" +

            "varying vec2 textureCoordinate;\n" +
            "varying vec2 blurCoordinates[GAUSSIAN_SAMPLES];\n" +

            "void main()\n" +
            "{\n" +
            "   gl_Position = position;\n" +
            "   textureCoordinate = inputTextureCoordinate.xy;\n" +

            "   int multiplier = 0;\n" +
            "   vec2 blurStep;\n" +

            "   for (int i = 0; i < GAUSSIAN_SAMPLES; i++)\n" +
            "   {\n" +
            "       multiplier = (i - ((GAUSSIAN_SAMPLES - 1) / 2));\n" +

            "       blurStep = float(multiplier) * singleStepOffset;\n" +
            "       blurCoordinates[i] = inputTextureCoordinate.xy + blurStep;\n" +
            "   }\n" +
            "}";

    public static final String BILATERAL_FRAGMENT_SHADER = "" +
            "uniform sampler2D inputImageTexture;\n" +

            " const lowp int GAUSSIAN_SAMPLES = 9;\n" +

            " varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];\n" +

            " uniform mediump float distanceNormalizationFactor;\n" +

            " void main()\n" +
            " {\n" +
            "     lowp vec4 centralColor;\n" +
            "     lowp float gaussianWeightTotal;\n" +
            "     lowp vec4 sum;\n" +
            "     lowp vec4 sampleColor;\n" +
            "     lowp float distanceFromCentralColor;\n" +
            "     lowp float gaussianWeight;\n" +
            "     \n" +
            "     centralColor = texture2D(inputImageTexture, blurCoordinates[4]);\n" +
            "     gaussianWeightTotal = 0.18;\n" +
            "     sum = centralColor * 0.18;\n" +
            "     \n" +
            "     sampleColor = texture2D(inputImageTexture, blurCoordinates[0]);\n" +
            "     distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
            "     gaussianWeight = 0.05 * (1.0 - distanceFromCentralColor);\n" +
            "     gaussianWeightTotal += gaussianWeight;\n" +
            "     sum += sampleColor * gaussianWeight;\n" +

            "     sampleColor = texture2D(inputImageTexture, blurCoordinates[1]);\n" +
            "     distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
            "     gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "     gaussianWeightTotal += gaussianWeight;\n" +
            "     sum += sampleColor * gaussianWeight;\n" +

            "     sampleColor = texture2D(inputImageTexture, blurCoordinates[2]);\n" +
            "     distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
            "     gaussianWeight = 0.12 * (1.0 - distanceFromCentralColor);\n" +
            "     gaussianWeightTotal += gaussianWeight;\n" +
            "     sum += sampleColor * gaussianWeight;\n" +

            "     sampleColor = texture2D(inputImageTexture, blurCoordinates[3]);\n" +
            "     distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
            "     gaussianWeight = 0.15 * (1.0 - distanceFromCentralColor);\n" +
            "     gaussianWeightTotal += gaussianWeight;\n" +
            "     sum += sampleColor * gaussianWeight;\n" +

            "     sampleColor = texture2D(inputImageTexture, blurCoordinates[5]);\n" +
            "     distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
            "     gaussianWeight = 0.15 * (1.0 - distanceFromCentralColor);\n" +
            "     gaussianWeightTotal += gaussianWeight;\n" +
            "     sum += sampleColor * gaussianWeight;\n" +

            "     sampleColor = texture2D(inputImageTexture, blurCoordinates[6]);\n" +
            "     distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
            "     gaussianWeight = 0.12 * (1.0 - distanceFromCentralColor);\n" +
            "     gaussianWeightTotal += gaussianWeight;\n" +
            "     sum += sampleColor * gaussianWeight;\n" +

            "     sampleColor = texture2D(inputImageTexture, blurCoordinates[7]);\n" +
            "     distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
            "     gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "     gaussianWeightTotal += gaussianWeight;\n" +
            "     sum += sampleColor * gaussianWeight;\n" +

            "     sampleColor = texture2D(inputImageTexture, blurCoordinates[8]);\n" +
            "     distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);\n" +
            "     gaussianWeight = 0.05 * (1.0 - distanceFromCentralColor);\n" +
            "     gaussianWeightTotal += gaussianWeight;\n" +
            "     sum += sampleColor * gaussianWeight;\n" +
            "     gl_FragColor = sum / gaussianWeightTotal;\n" +
            " }";

    private int mDisFactorLocation0;
    private int mDisFactorLocation1;
    private int mSingleStepOffsetLocation0;
    private int mSingleStepOffsetLocation1;
    private float mDistanceNormalizationFactor;

    public GPUImageBilateralFilter() {
        this(4.0f);
    }

    public GPUImageBilateralFilter(final float distanceNormalizationFactor) {
        super(null);
        addFilter(new GPUImageFilter(BILATERAL_VERTEX_SHADER, BILATERAL_FRAGMENT_SHADER));
        addFilter(new GPUImageFilter(BILATERAL_VERTEX_SHADER, BILATERAL_FRAGMENT_SHADER));
        mDistanceNormalizationFactor = distanceNormalizationFactor;
    }

    @Override
    public void onInit() {
        super.onInit();
        initHandles();
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setDistanceNormalizationFactor(mDistanceNormalizationFactor);
    }

    protected void initHandles() {
        GPUImageFilter filter = mFilters.get(0);
        mDisFactorLocation0 = GLES20.glGetUniformLocation(filter.getProgram(), "distanceNormalizationFactor");
        mSingleStepOffsetLocation0 = GLES20.glGetUniformLocation(filter.getProgram(), "singleStepOffset");

        filter = mFilters.get(1);
        mDisFactorLocation1 = GLES20.glGetUniformLocation(filter.getProgram(), "distanceNormalizationFactor");
        mSingleStepOffsetLocation1 = GLES20.glGetUniformLocation(filter.getProgram(), "singleStepOffset");
    }

    public void setDistanceNormalizationFactor(final float newValue) {
        mDistanceNormalizationFactor = newValue;
        GPUImageFilter filter = mFilters.get(0);
        filter.setFloat(mDisFactorLocation0, newValue);
        filter = mFilters.get(1);
        filter.setFloat(mDisFactorLocation1, newValue);
    }

    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);
        setTexelSize(width, height);
    }

    protected void setTexelSize(final float w, final float h) {
        GPUImageFilter filter = mFilters.get(0);
        filter.setFloatVec2(mSingleStepOffsetLocation0, new float[] {2.0f / w, 0.0f});
        filter = mFilters.get(1);
        filter.setFloatVec2(mSingleStepOffsetLocation1, new float[] {0.0f, 2.0f / h});
    }
}
