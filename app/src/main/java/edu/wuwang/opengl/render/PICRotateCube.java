package edu.wuwang.opengl.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.wuwang.opengl.R;

public class PICRotateCube extends Shape {

//    private Handler handler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//
//        }
//    };
//
//    private Runnable updateRunnable = new Runnable() {
//        @Override
//        public void run() {
//            //设置透视投影
//            Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1, 1, 3, 90);
//            //设置相机位置
//            Matrix.setLookAtM(viewMatrix, 0, (float) (30.0f * Math.cos(count)), (float) (30.0f * Math.sin(count)), 10.0f, 0f, 0f, 0f, 0f, 0.0f, 1.0f);
//            //计算变换矩阵
//            Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, viewMatrix, 0);
//            count += 0.05;
//            handler.postDelayed(updateRunnable, 100);
//            Log.e("handlerTest", "run: " + this + "  " + SystemClock.currentThreadTimeMillis());
//        }
//    };

    private Context context;

    // 顶点坐标
    private final float[] cubePositions = {
            // Front face
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            // Right face
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            // Back face
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            // Left face
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            // Top face
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            // Bottom face
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f
    };

    // 纹理坐标
    private final float[] cubeTextureCoordinates = {
            // Front face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            // Right face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            // Back face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            // Left face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            // Top face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            // Bottom face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    // 顶点索引
    private final short[] cubeIndices = {
            0, 1, 2, 0, 2, 3,       // Front face
            4, 5, 6, 4, 6, 7,       // Right face
            8, 9, 10, 8, 10, 11,    // Back face
            12, 13, 14, 12, 14, 15, // Left face
            16, 17, 18, 16, 18, 19, // Top face
            20, 21, 22, 20, 22, 23  // Bottom face
    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private ShortBuffer indexBuffer;

    private int[] textures = new int[6]; // 存储6个面的纹理ID

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] mvpMatrix = new float[16];

    private int shaderProgram;
    private int positionHandle;
    private int textureCoordinateHandle;
    private int mvpMatrixHandle;
    private int textureUniformHandle;


    public PICRotateCube(View mView) {
        super(mView);
        this.context = mView.getContext();

        // 初始化缓冲区
        ByteBuffer bb = ByteBuffer.allocateDirect(cubePositions.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubePositions);
        vertexBuffer.position(0);

        ByteBuffer tb = ByteBuffer.allocateDirect(cubeTextureCoordinates.length * 4);
        tb.order(ByteOrder.nativeOrder());
        textureBuffer = tb.asFloatBuffer();
        textureBuffer.put(cubeTextureCoordinates);
        textureBuffer.position(0);

        ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect(cubeIndices.length * 2);
        indexByteBuffer.order(ByteOrder.nativeOrder());
        indexBuffer = indexByteBuffer.asShortBuffer();
        indexBuffer.put(cubeIndices);

        indexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 设置背景颜色和深度缓冲
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // 编译着色器和链接程序
        int vertexShader = loadMShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadMShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);

        // 获取属性位置
        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "a_Position");
        textureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoordinate");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "u_MVPMatrix");
        textureUniformHandle = GLES20.glGetUniformLocation(shaderProgram, "u_Texture");

        // 加载纹理
        loadTextures();
    }



    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // 计算宽高比
        float ratio = (float) width / height;

        // 设置透视投影矩阵
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // 设置相机位置
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // 计算MVP矩阵
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);

        // 使用着色器程序
        GLES20.glUseProgram(shaderProgram);

        // 传递顶点数据
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        // 传递纹理坐标数据
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);

        // 传递MVP矩阵
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        // 绘制立方体的每个面
        for (int i = 0; i < 6; i++) {
            // 绑定纹理
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
            GLES20.glUniform1i(textureUniformHandle, 0);

            // 绘制
            indexBuffer.position(6 * i);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_BYTE, indexBuffer);
        }

        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
    }

    private void loadTextures() {
        // 为每个面加载纹理
        int[] textureIds = new int[]{R.drawable.ic_launcher_web, R.drawable.ic_launcher_web, R.drawable.ic_launcher_web,
                R.drawable.ic_launcher_web, R.drawable.ic_launcher_web, R.drawable.ic_launcher_web};
        GLES20.glGenTextures(6, textures, 0);

        for (int i = 0; i < 6; i++) {
            // 创建纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);

            // 设置过滤器
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // 加载位图
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), textureIds[i]);

            // 绑定纹理到OpenGL
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // 回收位图资源
            bitmap.recycle();
        }
    }

    private int loadMShader(int type, String shaderCode) {
        // 创建一个新的着色器
        int shader = GLES20.glCreateShader(type);

        // 将源码添加到着色器并编译它
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    // 顶点着色器代码
    private final String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "attribute vec2 a_TexCoordinate;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main() {" +
                    "  v_TexCoordinate = a_TexCoordinate;" +
                    "  gl_Position = u_MVPMatrix * a_Position;" +
                    "}";

    // 片段着色器代码
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D u_Texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main() {" +
//                    "  gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +
                    "  gl_FragColor = vec3(0,1,0);" +
                    "}";

    @Override
    public void onDestroy() {
//        handler.removeCallbacks(updateRunnable);
        Log.e("handlerTest", "onDestroy:" + this);
    }
}
