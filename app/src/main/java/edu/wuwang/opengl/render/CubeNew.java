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

public class CubeNew extends Shape {

    private float[] viewMatrix = new float[16];
    private float[] projectMatrix = new float[16];
    private float[] mvpMatrix = new float[16];

    private float ratio;

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private FloatBuffer textureBuffer;
    private int[] textureIds = new int[6];
    private final int program;

    private int matrixHandler;

    private static final float[] vertices = {
//            // Front face
//            -1, 1, 1,
//            -1, -1, 1,
//            1, -1, 1,
//            1, 1, 1,
//            // Back face
//            -1, 1, -1,
//            -1, -1, -1,
//            1, -1, -1,
//            1, 1, -1,

            // 前面
            -1.0f, -1.0f, 1.0f,  // v0
            1.0f, -1.0f, 1.0f,  // v1
            1.0f, 1.0f, 1.0f,  // v2
            -1.0f, 1.0f, 1.0f,  // v3

            // 后面
            -1.0f, -1.0f, -1.0f,  // v4
            1.0f, -1.0f, -1.0f,  // v5
            1.0f, 1.0f, -1.0f,  // v6
            -1.0f, 1.0f, -1.0f,  // v7

            // 左侧面
            -1.0f, -1.0f, -1.0f,  // v8
            -1.0f, -1.0f, 1.0f,  // v9
            -1.0f, 1.0f, 1.0f,  // v10
            -1.0f, 1.0f, -1.0f,  // v11

            // 右侧面
            1.0f, -1.0f, -1.0f,  // v12
            1.0f, -1.0f, 1.0f,  // v13
            1.0f, 1.0f, 1.0f,  // v14
            1.0f, 1.0f, -1.0f,  // v15

            // 顶面
            -1.0f, 1.0f, -1.0f,  // v16
            1.0f, 1.0f, -1.0f,  // v17
            1.0f, 1.0f, 1.0f,  // v18
            -1.0f, 1.0f, 1.0f,  // v19

            // 底面
            -1.0f, -1.0f, -1.0f,  // v20
            1.0f, -1.0f, -1.0f,  // v21
            1.0f, -1.0f, 1.0f,  // v22
            -1.0f, -1.0f, 1.0f   // v23
    };

    private static final short[] indices = {
//            0, 1, 2, 0, 2, 3,   // Front face
//            4, 5, 6, 4, 6, 7,   // Back face
//            0, 4, 5, 0, 5, 1,   // Left face
//            3, 2, 6, 3, 6, 7,   // Right face
//            0, 3, 7, 0, 7, 4,   // Top face
//            1, 2, 6, 1, 6, 5    // Bottom face

            // 前面
            0, 1, 2, 0, 2, 3,

            // 后面
            4, 5, 6, 4, 6, 7,

            // 左侧面
            8, 9, 10, 8, 10, 11,

            // 右侧面
            12, 13, 14, 12, 14, 15,

            // 顶面
            16, 17, 18, 16, 18, 19,

            // 底面
            20, 21, 22, 20, 22, 23
    };

    private static final float[] textureCoords = {
/*            // Front face

//            0.0f, 1.0f,
//            1.0f, 1.0f,
//            1.0f, 0.0f,
//            0.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Back face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            // Left face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

//            // Right face
//            0.0f, 0.0f,
//            0.0f, 1.0f,
//            1.0f, 1.0f,
//            1.0f, 0.0f,
//            // Top face
//            0.0f, 0.0f,
//            0.0f, 1.0f,
//            1.0f, 1.0f,
//            1.0f, 0.0f,
//            // Bottom face
//            0.0f, 0.0f,
//            0.0f, 1.0f,
//            1.0f, 1.0f,
//            1.0f, 0.0f,*/
            // 前面
            0.0f, 0.0f,  // t0
            0.0f, 1.0f,  // t3
            1.0f, 1.0f,  // t2
            1.0f, 0.0f,  // t1

            // 后面
            0.0f, 0.0f,  // t4
            1.0f, 0.0f,  // t5
            1.0f, 1.0f,  // t6
            0.0f, 1.0f,  // t7

            // 左侧面
            1.0f, 1.0f,  // t10
            1.0f, 0.0f,  // t9
            0.0f, 0.0f,  // t8
            0.0f, 1.0f,  // t11

            // 右侧面
            1.0f, 1.0f,  // t14
            1.0f, 0.0f,  // t13
            0.0f, 0.0f,  // t12
            0.0f, 1.0f,  // t15

            // 顶面
            0.0f, 0.0f,  // t16
            1.0f, 0.0f,  // t17
            1.0f, 1.0f,  // t18
            0.0f, 1.0f,  // t19

            // 底面
            1.0f, 1.0f,  // t22
            0.0f, 1.0f,  // t23
            0.0f, 0.0f,  // t20
            1.0f, 0.0f,  // t21
    };

    public CubeNew(View view) {
        super(view);
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer ib = ByteBuffer.allocateDirect(indices.length * 2);
        ib.order(ByteOrder.nativeOrder());
        indexBuffer = ib.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);

        ByteBuffer tb = ByteBuffer.allocateDirect(textureCoords.length * 4);
        tb.order(ByteOrder.nativeOrder());
        textureBuffer = tb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);

        loadTextures(view.getContext());

        String vertexShaderCode = "attribute vec4 aPosition;\n" +
                "attribute vec2 aTexCoord;\n" +
                "varying vec2 vTexCoord;\n" +
                "uniform mat4 vMatrix;" +
                "\n" +
                "void main() {\n" +
                "    gl_Position = vMatrix * aPosition;\n" +
                "    vTexCoord = aTexCoord;\n" +
                "}";
        String fragmentShaderCode = "precision mediump float;\n" +
                "varying vec2 vTexCoord;\n" +
                "uniform sampler2D uTexture;\n" +
                "\n" +
                "void main() {\n" +
                "    gl_FragColor = texture2D(uTexture, vTexCoord);\n" +
                "}";
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        //获取变换矩阵vMatrix成员句柄
        matrixHandler = GLES20.glGetUniformLocation(program, "vMatrix");
    }

    private void loadTextures(Context context) {
        // Load six different textures for each face of the cube
        int[] resourceIds = {
                R.drawable.ic_launcher_web,
                R.drawable.icon_duoyun,
                R.drawable.icon_tianqi,
                R.drawable.icon_xiaoxue,
                R.drawable.icon_qingtian,
                R.drawable.icon_tianqi_2
        };

        GLES20.glGenTextures(6, textureIds, 0);
        for (int i = 0; i < textureIds.length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[i]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceIds[i]);
            if (bitmap == null) {
                throw new RuntimeException("Error loading texture " + resourceIds[i]);
            }

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }
    }

    public void draw() {
        // Clear the color and depth buffer
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(program);

        int positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        int textureHandle = GLES20.glGetAttribLocation(program, "aTexCoord");
        int textureUniformHandle = GLES20.glGetUniformLocation(program, "uTexture");

        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, mvpMatrix, 0);

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(textureHandle);


        for (int i = 0; i < 6; i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0); // Activate texture unit 0
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[i]);
            GLES20.glUniform1i(textureUniformHandle, 0);

            // Log the current texture ID and face index
//            Log.i("CubeNew", "Drawing face " + i + " with texture ID: " + textureIds[i]);

            // Reset the position of the index buffer for each face
//            indexBuffer.position(i * 6);
//            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
//            GLES20.glDrawArrays();
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, i * 4, 4);
        }

        // Reset the index buffer position after drawing
//        indexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 计算宽高比
        ratio = (float) width / height;
        //设置透视投影
        Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1, 1, 3, 50);
        //设置相机位置
        Matrix.setLookAtM(viewMatrix, 0, -30.0f, -20.0f, -30.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, viewMatrix, 0);
        handler.postDelayed(updateRunnable, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        draw();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

        }
    };

    private double degrees = 45;
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            //设置透视投影
            Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1, 1, 3, 90);

            //设置相机位置
            Matrix.setLookAtM(viewMatrix, 0, (float) (30.0f * Math.cos(Math.toRadians(degrees))), (float) (30.0f * Math.sin(Math.toRadians(degrees))), 10.0f, 0f, 0f, 0f, 0f, 0.0f, 1.0f);
            //计算变换矩阵
            Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, viewMatrix, 0);
//            count += 0.05;
            degrees += 3;
            handler.postDelayed(updateRunnable, 10);
            Log.e("handlerTest", "run: " + this + "  " + SystemClock.currentThreadTimeMillis());
        }
    };
}