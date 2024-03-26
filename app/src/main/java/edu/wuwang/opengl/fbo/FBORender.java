/*
 *
 * FBORender.java
 *
 * Created by Wuwang on 2016/12/24
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package edu.wuwang.opengl.fbo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.wuwang.opengl.filter.AFilter;
import edu.wuwang.opengl.filter.GrayFilter;
import edu.wuwang.opengl.utils.Gl2Utils;

/**
 * Description:
 */
public class FBORender implements GLSurfaceView.Renderer {

    private Resources res;
    private AFilter mFilter;
    private Bitmap mBitmap;
    private ByteBuffer mBuffer;

    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] fTexture = new int[2];

    private Callback mCallback;

    public FBORender(Resources res) {
        mFilter = new GrayFilter(res);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFilter.create();
        mFilter.setMatrix(Gl2Utils.flip(Gl2Utils.getOriginalMatrix(), false, true));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 需要理解OPENGL使用了状态机，调用函数后，带有上下文
        if (mBitmap != null && !mBitmap.isRecycled()) {
            createEnvi();
            // 绑定当前环境的FrameBuffer
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);
            // 将framebuffer绑定到纹理1
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, fTexture[1], 0);

            /**
             * 用于将一个渲染缓冲区对象（Renderbuffer Object，简称 RBO）附加到帧缓冲区对象（Framebuffer Object，简称 FBO）的指定附件点上。
             * 这个操作是在设置离屏渲染环境时常见的步骤，它允许你将渲染结果（如颜色、深度或模板信息）存储在一个非纹理格式的缓冲区中。
             * 参数解释：
             * target: 指定要操作的帧缓冲区对象的目标。在 OpenGL ES 2.0 中，这个参数应该是 GLES20.GL_FRAMEBUFFER。
             * attachment: 指定要附加渲染缓冲区对象的帧缓冲区附件点。常见的附件点包括 GLES20.GL_COLOR_ATTACHMENT0（颜色缓冲区）、GLES20.GL_DEPTH_ATTACHMENT（深度缓冲区）和 GLES20.GL_STENCIL_ATTACHMENT（模板缓冲区）。
             * renderbuffertarget: 指定渲染缓冲区对象的目标。在 OpenGL ES 2.0 中，这个参数应该是 GLES20.GL_RENDERBUFFER。
             * renderbuffer: 指定要附加的渲染缓冲区对象的名称（ID）。如果传递 0，则会从相应的附件点上分离任何已附加的渲染缓冲区对象。
             */
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER, fRender[0]);

            GLES20.glViewport(0, 0, mBitmap.getWidth(), mBitmap.getHeight());

            // 这里将会绘制到前面绑定的FBO中
            mFilter.setTextureId(fTexture[0]);
            mFilter.draw();

            // 从FBO中读取像素
            GLES20.glReadPixels(0, 0, mBitmap.getWidth(), mBitmap.getHeight(), GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, mBuffer);
            if (mCallback != null) {
                mCallback.onCall(mBuffer);
            }
            deleteEnvi();
            mBitmap.recycle();
        }
    }

    public void createEnvi() {
        GLES20.glGenFramebuffers(1, fFrame, 0); //  创建一个新的帧缓冲对象，并将其 ID 存储在 fFrame 数组的第一个元素中。

        GLES20.glGenRenderbuffers(1, fRender, 0); // 创建一个新的渲染缓冲对象，并将其 ID 存储在 fRender 数组的第一个元素中。
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, fRender[0]); // 绑定刚刚创建的渲染缓冲对象，使其成为当前活动的渲染缓冲对象。
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
                mBitmap.getWidth(), mBitmap.getHeight()); // 为当前绑定的渲染缓冲对象分配存储空间，用于存储深度信息。深度缓冲的格式为 GL_DEPTH_COMPONENT16，大小与 mBitmap 的宽度和高度相同。
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, fRender[0]); // 将渲染缓冲对象附加到帧缓冲对象的深度附件点。这意味着在渲染到这个帧缓冲对象时，深度信息将被存储在 fRender[0] 指定的渲染缓冲对象中。
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0); // 解绑当前的渲染缓冲对象

        GLES20.glGenTextures(2, fTexture, 0); // 创建两个新的纹理对象，并将它们的 ID 存储在 fTexture 数组中。
        for (int i = 0; i < 2; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[i]);
            if (i == 0) {
                /**
                 * GLUtils.texImage2D 是 Android OpenGL ES API 中的一个方法，它用于将 Android Bitmap 对象的像素数据上传到一个 OpenGL ES 纹理。这个方法是 GLUtils 类的一部分，它提供了一些实用功能，使得在 Android 平台上使用 OpenGL ES 更加方便。
                 * texImage2D 方法的典型用法是在创建新纹理或更新现有纹理的图像内容时使用。它封装了一些底层的 OpenGL ES 纹理设置调用，简化了从 Bitmap 到纹理的数据传输过程。
                 * 参数解释：
                 * target: 指定目标纹理。对于 2D 纹理，这个参数通常是 GL_TEXTURE_2D。
                 * level: 指定多级渐远纹理的级别。如果不使用多级渐远纹理，这个值应该设置为 0。
                 * internalformat: 指定纹理的颜色组件格式。在大多数情况下，这个参数会被忽略，因为 Bitmap 对象的格式会用来确定最终的纹理格式。
                 * bitmap: Bitmap 对象，包含要上传的像素数据。
                 * type: 指定 Bitmap 对象的像素数据类型。通常，这个参数会被设置为 GL_UNSIGNED_BYTE，因为 Android Bitmap 对象的像素通常是以无符号字节的形式存储的。
                 * border: 必须为 0，因为 OpenGL ES 不支持带边框的纹理。
                 */
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap, 0);
            } else {
                /**
                 * GLES20.glTexImage2D 是 OpenGL ES 2.0 API 中的一个函数，用于指定一个二维纹理图像。这个函数可以用来创建一个新的纹理图像，或者替换现有纹理图像的内容。它是设置纹理数据的核心函数之一。
                 * 参数解释：
                 * target: 纹理目标。对于二维纹理，这个参数通常是 GL_TEXTURE_2D。
                 * level: 多级渐远纹理的级别。如果不使用多级渐远纹理，这个值应该设置为 0。对于多级渐远纹理，0 表示基本图像级别，而更高的级别表示更小的、分辨率降低的纹理图像。
                 * internalformat: 纹理数据的颜色组件格式。常见的值有 GL_RGB、GL_RGBA、GL_LUMINANCE 和 GL_LUMINANCE_ALPHA 等。
                 * width: 纹理图像的宽度，以像素为单位。
                 * height: 纹理图像的高度，以像素为单位。
                 * border: 必须为 0。在 OpenGL ES 中，纹理边框不受支持。
                 * format: 像素数据的格式。这应该与纹理图像的 internalformat 匹配。例如，如果 internalformat 是 GL_RGBA，那么 format 也应该是 GL_RGBA。
                 * type: 像素数据的数据类型。例如，GL_UNSIGNED_BYTE 表示像素数据是无符号字节。
                 * pixels: 包含纹理图像数据的缓冲区。如果只是想要分配纹理空间而不上传数据，可以传递 null。
                 */
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap.getWidth(), mBitmap.getHeight(),
                        0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            }
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
        mBuffer = ByteBuffer.allocate(mBitmap.getWidth() * mBitmap.getHeight() * 4); // 分配一个 ByteBuffer，用于存储从帧缓冲对象读取的像素数据。这里假设每个像素占用 4 个字节（RGBA）。
    }

    private void deleteEnvi() {
        GLES20.glDeleteTextures(2, fTexture, 0);
        GLES20.glDeleteRenderbuffers(1, fRender, 0);
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
    }

    interface Callback {
        void onCall(ByteBuffer data);
    }

}
