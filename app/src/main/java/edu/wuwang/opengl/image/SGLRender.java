/*
 *
 * SGLRender.java
 *
 * Created by Wuwang on 2016/10/15
 */
package edu.wuwang.opengl.image;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.view.View;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.wuwang.opengl.image.filter.AFilterRenderer;
import edu.wuwang.opengl.image.filter.ColorFilterRenderer;
import edu.wuwang.opengl.image.filter.ContrastColorFilterRenderer;

/**
 * Description:
 */
public class SGLRender implements GLSurfaceView.Renderer {

    private AFilterRenderer mFilter;
    private Bitmap bitmap;
    private int width, height;
    private boolean refreshFlag = false;
    private EGLConfig config;

    public SGLRender(View mView) {
        mFilter = new ContrastColorFilterRenderer(mView.getContext(), ColorFilterRenderer.Filter.NONE);
    }

    public void setFilter(AFilterRenderer filter) {
        refreshFlag = true;
        mFilter = filter;
        if (bitmap != null) {
            mFilter.setBitmap(bitmap);
        }
    }

    public void setImageBuffer(int[] buffer, int width, int height) {
        bitmap = Bitmap.createBitmap(buffer, width, height, Bitmap.Config.RGB_565);
        mFilter.setBitmap(bitmap);
    }

    public void refresh() {
        refreshFlag = true;
    }

    public AFilterRenderer getFilter() {
        return mFilter;
    }

    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
        mFilter.setBitmap(bitmap);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        this.config = config;
        mFilter.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        mFilter.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (refreshFlag && width != 0 && height != 0) {
            mFilter.onSurfaceCreated(gl, config);
            mFilter.onSurfaceChanged(gl, width, height);
            refreshFlag = false;
        }
        mFilter.onDrawFrame(gl);
    }
}
