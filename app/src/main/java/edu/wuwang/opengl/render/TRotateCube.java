package edu.wuwang.opengl.render;

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

public class TRotateCube extends Shape {

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

        }
    };

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            //设置透视投影
            Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1, 1, 3, 90);
            //设置相机位置
            Matrix.setLookAtM(viewMatrix, 0, (float) (30.0f * Math.cos(count)), (float) (30.0f * Math.sin(count)), 10.0f, 0f, 0f, 0f, 0f, 0.0f, 1.0f);
            //计算变换矩阵
            Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, viewMatrix, 0);
            count += 0.05;
            handler.postDelayed(updateRunnable, 100);
            Log.e("handlerTest", "run: " + this + "  " + SystemClock.currentThreadTimeMillis());
        }
    };

    private FloatBuffer vertexBuffer, colorBuffer;
    private FloatBuffer textureBuffer;

    private ShortBuffer indexBuffer;

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 vMatrix;" +
                    "varying  vec4 vColor;" +
                    "attribute vec4 aColor;" +
                    "void main() {" +
                    "  gl_Position = vMatrix*vPosition;" +
                    "  vColor = aColor;" +
                    "}";

    private final String fragmentShaderCode = "precision mediump float;" +
            "varying vec4 vColor;" +
            "uniform sampler2D u_Texture;" +
            "varying vec2 v_TexCoordinate;" +
            "void main() {" +
            "  gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +
            "  gl_FragColor = vColor;" +
            "}";

    private int program;

    private int matrixHandler;

    private int COORDS_VERTEX_COUNT = 3;

    private float ratio;

    private double count = 0.0;

    private final float[] cubePositions = {
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f
    };

    private final short[] cubePositionsIndex = {
            0, 1, 3, 3, 2, 1, // 远离自己的一面
            0, 4, 6, 6, 3, 0, // 左侧面
            1, 5, 7, 7, 2, 1, // 右侧面
            0, 1, 5, 5, 4, 0, // 底面
            3, 6, 7, 7, 2, 3, // 顶面
            4, 5, 6, 6, 7, 5 //前面
    };

    float color[] = {
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
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

    private final short[] cubeIndices = {
            0, 1, 2, 0, 2, 3,       // Front face
            4, 5, 6, 4, 6, 7,       // Right face
            8, 9, 10, 8, 10, 11,    // Back face
            12, 13, 14, 12, 14, 15, // Left face
            16, 17, 18, 16, 18, 19, // Top face
            20, 21, 22, 20, 22, 23  // Bottom face
    };



    private int positionHandle = 0;
    private int colorHandle = 0;
    private int textureUniformHandle;
    private int textureCoordinateHandle;

    private int[] textures = new int[6]; // 存储6个面的纹理ID

    private float[] viewMatrix = new float[16];
    private float[] projectMatrix = new float[16];
    private float[] mvpMatrix = new float[16];




    public TRotateCube(View mView) {
        super(mView);
        ByteBuffer buffer = ByteBuffer.allocateDirect(cubePositions.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        vertexBuffer = buffer.asFloatBuffer();
        vertexBuffer.put(cubePositions);
        vertexBuffer.position(0);

        ByteBuffer dd = ByteBuffer.allocateDirect(
                color.length * 4);
        dd.order(ByteOrder.nativeOrder());
        colorBuffer = dd.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

//        ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect(cubePositionsIndex.length * 2);
//        indexByteBuffer.order(ByteOrder.nativeOrder());
//        indexBuffer = indexByteBuffer.asShortBuffer();
//        indexBuffer.put(cubePositionsIndex);
        ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect(cubeIndices.length * 2);
        indexByteBuffer.order(ByteOrder.nativeOrder());
        indexBuffer = indexByteBuffer.asShortBuffer();
        indexBuffer.put(cubeIndices);
        indexBuffer.position(0);

        ByteBuffer tb = ByteBuffer.allocateDirect(cubeTextureCoordinates.length * 4);
        tb.order(ByteOrder.nativeOrder());
        textureBuffer = tb.asFloatBuffer();
        textureBuffer.put(cubeTextureCoordinates);
        textureBuffer.position(0);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // 加载纹理
        loadTextures();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //计算宽高比
        ratio = (float) width / height;
        //设置透视投影
        Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        //设置相机位置
        Matrix.setLookAtM(viewMatrix, 0, 5.0f, 5.0f, 10.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, viewMatrix, 0);
        handler.postDelayed(updateRunnable, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(program);
        //获取变换矩阵vMatrix成员句柄
        matrixHandler = GLES20.glGetUniformLocation(program, "vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, mvpMatrix, 0);
        //获取顶点着色器的vPosition成员句柄
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(positionHandle);
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);
        //获取片元着色器的vColor成员的句柄
        colorHandle = GLES20.glGetAttribLocation(program, "aColor");
        //设置绘制三角形的颜色
//        GLES20.glUniform4fv(mColorHandle, 2, color, 0);
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, 4,
                GLES20.GL_FLOAT, false,
                0, colorBuffer);

        textureCoordinateHandle = GLES20.glGetAttribLocation(program, "a_TexCoordinate");
        textureUniformHandle = GLES20.glGetUniformLocation(program, "u_Texture");

        // 传递纹理坐标数据
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);

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

        //索引法绘制正方体
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cubePositionsIndex.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(positionHandle);
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
            Bitmap bitmap = BitmapFactory.decodeResource(mView.getContext().getResources(), textureIds[i]);

            // 绑定纹理到OpenGL
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // 回收位图资源
            bitmap.recycle();
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(updateRunnable);
        Log.e("handlerTest", "onDestroy:" + this);
    }
}
