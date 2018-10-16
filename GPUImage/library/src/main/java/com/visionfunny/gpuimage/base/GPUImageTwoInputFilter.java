/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.visionfunny.gpuimage.base;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.visionfunny.gpuimage.util.TextureRotationUtil;

public class GPUImageTwoInputFilter extends GPUImageFilter {
    private static final String VERTEX_SHADER =
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "attribute vec4 inputTextureCoordinate2;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 textureCoordinate2;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "    textureCoordinate2 = inputTextureCoordinate2.xy;\n" +
            "}";

    public int mGLUniformTexture2;
    public int mGLAttribTextureCoordinate2;
    public int mGLTextureId2 = OpenGlUtils.NO_TEXTURE;

    private ByteBuffer mTexture2CoordinatesBuffer;
    private Bitmap mBitmap;

    public GPUImageTwoInputFilter(String fragmentShader) {
        this(VERTEX_SHADER, fragmentShader);
    }

    public GPUImageTwoInputFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        initTexture2Buffer();
    }

    @Override
    public void onInit() {
        super.onInit();

        mGLAttribTextureCoordinate2 = GLES20.glGetAttribLocation(getProgram(), "inputTextureCoordinate2");
        mGLUniformTexture2 = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture2"); // This does assume a name of "inputImageTexture2" for second input texture in the fragment shader
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate2);

        if (mBitmap != null&&!mBitmap.isRecycled()) {
            setBitmap(mBitmap);
        }
    }
    
    public void setBitmap(final Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled()) {
            return;
        }
        mBitmap = bitmap;
        if (mBitmap == null) {
            return;
        }
        runOnDraw(new Runnable() {
            public void run() {
                if (mGLTextureId2 == OpenGlUtils.NO_TEXTURE) {
                    if (bitmap == null || bitmap.isRecycled()) {
                        return;
                    }
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
                    mGLTextureId2 = OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, false);
                }
            }
        });
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void recycleBitmap() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteTextures(1, new int[]{
                mGLTextureId2
        }, 0);
        mGLTextureId2 = OpenGlUtils.NO_TEXTURE;
    }

    @Override
    protected void onDrawArraysPre() {
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate2);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGLTextureId2);
        GLES20.glUniform1i(mGLUniformTexture2, 3);

        mTexture2CoordinatesBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate2, 2, GLES20.GL_FLOAT, false, 0, mTexture2CoordinatesBuffer);
    }

    @Override
    protected void onDrawArraysAfter() {
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate2);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    }

    public void initTexture2Buffer() {
        float[] buffer = TextureRotationUtil.TEXTURE_NO_ROTATION_FLOAT_ARRAY;
        ByteBuffer bBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder());
        FloatBuffer fBuffer = bBuffer.asFloatBuffer();
        fBuffer.put(buffer);
        fBuffer.flip();
        mTexture2CoordinatesBuffer = bBuffer;
    }
}
