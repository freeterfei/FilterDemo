package com.visionfunny.gpuimage;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;

import com.visionfunny.gpuimage.base.GPUImageFilterGroup;
import com.visionfunny.gpuimage.base.OpenGlUtils;
import com.visionfunny.gpuimage.base.GPUImageFilter;
import com.visionfunny.gpuimage.extension.CameraBaseFilter;
import com.visionfunny.gpuimage.extension.GPUFaceBeautyFilter;
import com.visionfunny.gpuimage.extension.GPUImageCombinationFilter;
import com.visionfunny.gpuimage.ref.GPUImageView;
import com.visionfunny.gpuimage.base.GPUImageBilateralFilter;
import com.visionfunny.gpuimage.util.TextureRotationUtil;
import com.visionfunny.gpuimage.util.VertexUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.visionfunny.gpuimage.util.TextureRotationUtil.TEXTURE_NO_ROTATION;

/**
 * Created by chenfeifei on 2017/6/15.
 */

public class GPUImage {
    private final Context mContext;
    private final GPUImageRenderer mRenderer;
    private GLSurfaceView mGlSurfaceView;
    private GPUImageFilter mFilter;//filter for preview
    private GPUImageFilter mImageFilter;//filter for picture

    private Bitmap mCurrentBitmap;
    private ScaleType mScaleType = ScaleType.CENTER_CROP;

    /**
     * Instantiates a new GPUImage object.
     *
     * @param context the context
     */
    public GPUImage(final Context context) {
        if (!supportsOpenGLES2(context)) {
            throw new IllegalStateException("OpenGL ES 2.0 is not supported on this phone.");
        }

        mContext = context;
        //mFilter = new CameraBaseFilter();

        //init mFilter for preview
        List<GPUImageFilter> filters = new ArrayList<>();
        filters.add(new CameraBaseFilter());
        filters.add(new GPUImageBilateralFilter());
        filters.add(new GPUImageCombinationFilter());
        mFilter = new GPUFaceBeautyFilter(filters);

        //GPUImageCombinationFilter filter = new GPUImageCombinationFilter();
        //filter.setBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));


        //init mImageFilter for picture save
        List<GPUImageFilter> filters1 = new ArrayList<>();
        filters1.add(new GPUImageFilter());
        filters1.add(new GPUImageBilateralFilter());
        filters1.add(new GPUImageCombinationFilter());
        mImageFilter = new GPUFaceBeautyFilter(filters1);

        mRenderer = new GPUImageRenderer(mFilter);
    }

    /**
     * Checks if OpenGL ES 2.0 is supported on the current device.
     *
     * @param context the context
     * @return true, if successful
     */
    private boolean supportsOpenGLES2(final Context context) {
        final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    /**
     * Sets the GLSurfaceView which will display the preview.
     *
     * @param view the GLSurfaceView
     */
    public void setGLSurfaceView(final GLSurfaceView view) {
        mGlSurfaceView = view;
        mGlSurfaceView.setEGLContextClientVersion(2);
        mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGlSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        mGlSurfaceView.setRenderer(mRenderer);
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGlSurfaceView.requestRender();
    }

    /**
     * Sets the background color
     *
     * @param red red color value
     * @param green green color value
     * @param blue red color value
     */
    public void setBackgroundColor(float red, float green, float blue) {
        mRenderer.setBackgroundColor(red, green, blue);
    }

    /**
     * Request the preview to be rendered again.
     */
    public void requestRender() {
        if (mGlSurfaceView != null) {
            mGlSurfaceView.requestRender();
        }
    }

    /**
     * Sets the up camera to be connected to GPUImage to get a filtered preview.
     *
     * @param camera the camera
     */
    public void setUpCamera(final Camera camera) {
        setUpCamera(camera, 0, false, false);
    }

    /**
     * Sets the up camera to be connected to GPUImage to get a filtered preview.
     *
     * @param camera the camera
     * @param degrees by how many degrees the image should be rotated
     * @param flipHorizontal if the image should be flipped horizontally
     * @param flipVertical if the image should be flipped vertically
     */
    public void setUpCamera(final Camera camera, final int degrees, final boolean flipHorizontal,
                            final boolean flipVertical) {

        final Camera.Size previewSize = camera.getParameters().getPreviewSize();
        mRenderer.setImageSize(previewSize.width, previewSize.height);

        setUpCameraICS(camera);

        Rotation rotation = Rotation.NORMAL;
        switch (degrees) {
            case 90:
                rotation = Rotation.ROTATION_90;
                break;
            case 180:
                rotation = Rotation.ROTATION_180;
                break;
            case 270:
                rotation = Rotation.ROTATION_270;
                break;
        }
        mRenderer.setRotationCamera(rotation, flipHorizontal, flipVertical);

    }

    @TargetApi(14)
    private void setUpCameraICS(final Camera camera) {
        mRenderer.setUpSurfaceTexture(camera);
    }

    /**
     * Sets the filter which should be applied to the image which was (or will
     * be) set by setImage(...).
     *
     * @param filter the new filter
     */
    public void setFilter(final GPUImageFilter filter) {
        mFilter = filter;
        mRenderer.setFilter(mFilter);
        requestRender();
    }

    public interface ResponseListener<T> {
        void response(T item);
    }

    public enum ScaleType { CENTER_INSIDE, CENTER_CROP }



    //*****************Picture Save*****************//


    public interface OnPictureSavedListener {
        void onPictureSaved(Uri uri);
    }

    //*****************Picture Save*****************//



    /**
     * Gets the given bitmap with current filter applied as a Bitmap.
     *
     * @param bitmap the bitmap on which the current filter should be applied
     * @return the bitmap with filter applied
     */
    public Bitmap getBitmapWithFilterApplied(final Bitmap bitmap) {
        if (mGlSurfaceView != null) {
            mRenderer.deleteImage();
            mRenderer.runOnDraw(new Runnable() {

                @Override
                public void run() {
                    synchronized(mImageFilter) {
                        mImageFilter.destroy();
                        mImageFilter.notify();
                    }
                }
            });
            synchronized(mImageFilter) {
                requestRender();
                try {
                    mImageFilter.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        GPUImageRenderer renderer = new GPUImageRenderer(mImageFilter);
        renderer.setRotation(Rotation.NORMAL,
                mRenderer.isFlippedHorizontally(), mRenderer.isFlippedVertically());
        renderer.setScaleType(mScaleType);
        PixelBuffer buffer = new PixelBuffer(bitmap.getWidth(), bitmap.getHeight());
        buffer.setRenderer(renderer);
        renderer.setImageBitmap(bitmap, false);
        Bitmap result = buffer.getBitmap();
        mImageFilter.destroy();
        renderer.deleteImage();
        buffer.destroy();

        //恢复预览使用的Filter
        /*
        mRenderer.setFilter(mFilter);
        if (mCurrentBitmap != null) {
            mRenderer.setImageBitmap(mCurrentBitmap, false);
        }
        requestRender();
        */

        return result;
    }



    /**
     * Gets the images for multiple filters on a image. This can be used to
     * quickly get thumbnail images for filters. <br>
     * Whenever a new Bitmap is ready, the listener will be called with the
     * bitmap. The order of the calls to the listener will be the same as the
     * filter order.
     *
     * @param bitmap the bitmap on which the filters will be applied
     * @param filters the filters which will be applied on the bitmap
     * @param listener the listener on which the results will be notified
     */
    /*
    public static void getBitmapForMultipleFilters(final Bitmap bitmap,
                                                   final List<GPUImageFilter> filters, final GPUImage.ResponseListener<Bitmap> listener) {
        if (filters.isEmpty()) {
            return;
        }
        GPUImageRenderer renderer = new GPUImageRenderer(filters.get(0));
        renderer.setImageBitmap(bitmap, false);
        PixelBuffer buffer = new PixelBuffer(bitmap.getWidth(), bitmap.getHeight());
        buffer.setRenderer(renderer);

        for (GPUImageFilter filter : filters) {
            renderer.setFilter(filter);
            listener.response(buffer.getBitmap());
            filter.destroy();
        }
        renderer.deleteImage();
        buffer.destroy();
    }

    */

    /**
     * Deprecated: Please use
     * {@link GPUImageView#saveToPictures(String, String, com.visionfunny.gpuimage.ref.GPUImageView.OnPictureSavedListener)}
     *
     * Save current image with applied filter to Pictures. It will be stored on
     * the default Picture folder on the phone below the given folderName and
     * fileName. <br>
     * This method is async and will notify when the image was saved through the
     * listener.
     *
     * @param folderName the folder name
     * @param fileName the file name
     * @param listener the listener
     */
    @Deprecated
    public void saveToPictures(final String folderName, final String fileName,
                               final GPUImage.OnPictureSavedListener listener) {
        saveToPictures(mCurrentBitmap, folderName, fileName, listener);
    }

    /**
     * Deprecated: Please use
     * {@link GPUImageView#saveToPictures(String, String, com.visionfunny.gpuimage.ref.GPUImageView.OnPictureSavedListener)}
     *
     * Apply and save the given bitmap with applied filter to Pictures. It will
     * be stored on the default Picture folder on the phone below the given
     * folerName and fileName. <br>
     * This method is async and will notify when the image was saved through the
     * listener.
     *
     * @param bitmap the bitmap
     * @param folderName the folder name
     * @param fileName the file name
     * @param listener the listener
     */
    @Deprecated
    public void saveToPictures(final Bitmap bitmap, final String folderName, final String fileName,
                               final OnPictureSavedListener listener) {
        new SaveTask(bitmap, folderName, fileName, listener).execute();
    }

    @Deprecated
    private class SaveTask extends AsyncTask<Void, Void, Void> {

        private final Bitmap mBitmap;
        private final String mFolderName;
        private final String mFileName;
        private final OnPictureSavedListener mListener;
        private final Handler mHandler;

        public SaveTask(final Bitmap bitmap, final String folderName, final String fileName,
                        final OnPictureSavedListener listener) {
            mBitmap = bitmap;
            mFolderName = folderName;
            mFileName = fileName;
            mListener = listener;
            mHandler = new Handler();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            Bitmap result = getBitmapWithFilterApplied(mBitmap);
            saveImage(mFolderName, mFileName, result);
            return null;
        }

        private void saveImage(final String folderName, final String fileName, final Bitmap image) {
            File path = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(path, folderName + "/" + fileName);
            try {
                file.getParentFile().mkdirs();
                image.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(file));
                MediaScannerConnection.scanFile(mContext,
                        new String[] {
                                file.toString()
                        }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(final String path, final Uri uri) {
                                if (mListener != null) {
                                    mHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            mListener.onPictureSaved(uri);
                                        }
                                    });
                                }
                            }
                        });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }












    private class GPUImageRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
        public static final int NO_IMAGE = -1;


        private GPUImageFilter mFilter;

        public final Object mSurfaceChangedWaiter = new Object();

        private int mGlOESTextureId = NO_IMAGE;
        private SurfaceTexture mSTexture = null;

        private int mGLTextureId = NO_IMAGE;//used for picture save

        private final FloatBuffer mGLCubeBuffer;
        private final FloatBuffer mGLTextureBuffer;
        private IntBuffer mGLRgbBuffer;

        //the width and height of GLSurfaceView
        private int mOutputWidth;
        private int mOutputHeight;

        //the width and height of preview size or imageview
        private int mImageWidth;
        private int mImageHeight;
        private int mAddedPadding;

        private final Queue<Runnable> mRunOnDraw;
        private final Queue<Runnable> mRunOnDrawEnd;
        private Rotation mRotation;
        private boolean mFlipHorizontal;
        private boolean mFlipVertical;
        private GPUImage.ScaleType mScaleType = GPUImage.ScaleType.CENTER_CROP;

        private float mBackgroundRed = 0;
        private float mBackgroundGreen = 0;
        private float mBackgroundBlue = 0;

        private boolean mUpdateST = false;

        public GPUImageRenderer(final GPUImageFilter filter) {
            mFilter = filter;
            mRunOnDraw = new LinkedList<Runnable>();
            mRunOnDrawEnd = new LinkedList<Runnable>();

            mGLCubeBuffer = ByteBuffer.allocateDirect(VertexUtil.CUBE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            mGLCubeBuffer.put(VertexUtil.CUBE).position(0);

            mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            mGLTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);

            //setRotation(Rotation.ROTATION_90, true, true);
        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            requestRender();
            mUpdateST = true;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(mBackgroundRed, mBackgroundGreen, mBackgroundBlue, 1);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            mFilter.init();
            initTex();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mOutputWidth = width;
            mOutputHeight = height;

            GLES20.glViewport(0, 0, width, height);
            GLES20.glUseProgram(mFilter.getProgram());
            mFilter.onOutputSizeChanged(width, height);
            adjustImageScaling();
            synchronized (mSurfaceChangedWaiter) {
                mSurfaceChangedWaiter.notifyAll();
            }
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            runAll(mRunOnDraw);

            if (mSTexture != null) {

                if(mUpdateST) {
                    mSTexture.updateTexImage();

                    mUpdateST = false;
                }
            }

            if(((GPUImageFilterGroup)mFilter).getFilters().get(0) instanceof CameraBaseFilter) {
                android.util.Log.d("test", "CameraBaseFilter");
                //used for preview
                mFilter.onDraw(mGlOESTextureId, mGLCubeBuffer, mGLTextureBuffer);
            } else {
                //used for picture save
                android.util.Log.d("test", "not CameraBaseFilter");
                //mFilter.onDraw(mGlOESTextureId, mGLCubeBuffer, mGLTextureBuffer);
                mFilter.onDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer);
            }


            runAll(mRunOnDrawEnd);


        }

        /**
         * setup the camera preview texture and startPreview
         * @param camera
         */

        public void setUpSurfaceTexture(final Camera camera) {
            runOnDraw(new Runnable() {
                @Override
                public void run() {

                    try{
                        if(camera != null) {
                            camera.setPreviewTexture(mSTexture);
                            camera.startPreview();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        /**
         * Sets the background color
         *
         * @param red red color value
         * @param green green color value
         * @param blue red color value
         */
        public void setBackgroundColor(float red, float green, float blue) {
            mBackgroundRed = red;
            mBackgroundGreen = green;
            mBackgroundBlue = blue;
        }

        public void setFilter(final GPUImageFilter filter) {
            runOnDraw(new Runnable() {

                @Override
                public void run() {
                    final GPUImageFilter oldFilter = mFilter;
                    mFilter = filter;
                    if (oldFilter != null) {
                        oldFilter.destroy();
                    }
                    mFilter.init();
                    GLES20.glUseProgram(mFilter.getProgram());
                    mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight);
                }
            });
        }

        private void runAll(Queue<Runnable> queue) {
            synchronized (queue) {
                while (!queue.isEmpty()) {
                    queue.poll().run();
                }
            }
        }

        private void initTex() {
            mGlOESTextureId = OpenGlUtils.getExternalOESTextureID();
            mSTexture = new SurfaceTexture(mGlOESTextureId);
            mSTexture.setOnFrameAvailableListener(this);
        }

        public void setRotationCamera(final Rotation rotation, final boolean flipHorizontal, final boolean flipVertical) {
            setRotation(rotation, flipVertical, flipHorizontal);
        }

        public void setRotation(final Rotation rotation, final boolean flipHorizontal, final boolean flipVertical) {
            mFlipHorizontal = flipHorizontal;
            mFlipVertical = flipVertical;
            setRotation(rotation);
        }

        public void setRotation(final Rotation rotation) {
            mRotation = rotation;
            mRunOnDrawEnd.add(new Runnable() {
                @Override
                public void run() {
                    adjustImageScaling();
                }
            });
        }

        public void setImageSize(int width, int height) {
            mImageWidth = width;
            mImageHeight = height;
        }

        private void adjustImageScaling() {

            if(!isReadyForAdjust()){
                return;
            }

            float outputWidth = mOutputWidth;
            float outputHeight = mOutputHeight;
            if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
                outputWidth = mOutputHeight;
                outputHeight = mOutputWidth;
            }

            android.util.Log.d("test", " mOutputWidth =" + mOutputWidth);
            android.util.Log.d("test", " mOutputHeight =" + mOutputHeight);

            android.util.Log.d("test", " mImageWidth =" + mImageWidth);
            android.util.Log.d("test", " mImageHeight =" + mImageHeight);

            float ratio1 = outputWidth / mImageWidth;
            float ratio2 = outputHeight / mImageHeight;
            float ratioMax = Math.max(ratio1, ratio2);
            int imageWidthNew = Math.round(mImageWidth * ratioMax);
            int imageHeightNew = Math.round(mImageHeight * ratioMax);

            float ratioWidth = imageWidthNew / outputWidth;
            float ratioHeight = imageHeightNew / outputHeight;

            float[] cube = VertexUtil.CUBE;
            float[] textureCords = TextureRotationUtil.getRotation(mRotation, mFlipHorizontal, mFlipVertical);

            for(int i = 0; i < textureCords.length; i++) {
                android.util.Log.d("test", " textureCords =" + textureCords[i]);
            }
            if (mScaleType == GPUImage.ScaleType.CENTER_CROP) {
                float distHorizontal = (1 - 1 / ratioWidth) / 2;
                float distVertical = (1 - 1 / ratioHeight) / 2;
                textureCords = new float[]{
                        addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
                        addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
                        addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
                        addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
                };
            } else {
                cube = new float[]{
                        VertexUtil.CUBE[0] / ratioHeight, VertexUtil.CUBE[1] / ratioWidth,
                        VertexUtil.CUBE[2] / ratioHeight, VertexUtil.CUBE[3] / ratioWidth,
                        VertexUtil.CUBE[4] / ratioHeight, VertexUtil.CUBE[5] / ratioWidth,
                        VertexUtil.CUBE[6] / ratioHeight, VertexUtil.CUBE[7] / ratioWidth,
                };
            }

            mGLCubeBuffer.clear();
            mGLCubeBuffer.put(cube).position(0);
            mGLTextureBuffer.clear();
            mGLTextureBuffer.put(textureCords).position(0);

        }

        private float addDistance(float coordinate, float distance) {
            return coordinate == 0.0f ? distance : 1 - distance;
        }

        private boolean isReadyForAdjust() {
            if(mImageWidth == 0 || mImageHeight == 0 || mOutputWidth == 0 || mOutputHeight == 0) {
                return false;
            }
            return true;
        }


        protected void runOnDraw(final Runnable runnable) {
            synchronized (mRunOnDraw) {
                mRunOnDraw.add(runnable);
            }
        }


        public void deleteImage() {
            runOnDraw(new Runnable() {

                @Override
                public void run() {
                    GLES20.glDeleteTextures(1, new int[]{
                            mGLTextureId
                    }, 0);
                    mGLTextureId = NO_IMAGE;
                }
            });
        }

        public void setImageBitmap(final Bitmap bitmap, final boolean recycle) {
            if (bitmap == null) {
                return;
            }

            runOnDraw(new Runnable() {

                @Override
                public void run() {
                    Bitmap resizedBitmap = null;
                    if (bitmap.getWidth() % 2 == 1) {
                        resizedBitmap = Bitmap.createBitmap(bitmap.getWidth() + 1, bitmap.getHeight(),
                                Bitmap.Config.ARGB_8888);
                        Canvas can = new Canvas(resizedBitmap);
                        can.drawARGB(0xff, 0xff, 0x00, 0x00);
                        can.drawBitmap(bitmap, 0, 0, null);
                        mAddedPadding = 1;
                    } else {
                        mAddedPadding = 0;
                    }

                    mGLTextureId = OpenGlUtils.loadTexture(
                            resizedBitmap != null ? resizedBitmap : bitmap, mGLTextureId, recycle);
                    if (resizedBitmap != null) {
                        resizedBitmap.recycle();
                    }
                    mImageWidth = bitmap.getWidth();
                    mImageHeight = bitmap.getHeight();
                    adjustImageScaling();
                }
            });
        }

        public void setScaleType(GPUImage.ScaleType scaleType) {
            mScaleType = scaleType;
        }

        public boolean isFlippedHorizontally() {
            return mFlipHorizontal;
        }

        public boolean isFlippedVertically() {
            return mFlipVertical;
        }

    }
}
