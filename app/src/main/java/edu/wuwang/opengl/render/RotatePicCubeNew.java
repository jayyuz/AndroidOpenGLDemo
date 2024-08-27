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

public class RotatePicCubeNew extends Shape {

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

        }
    };

    private double degrees = 0;
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
            handler.postDelayed(updateRunnable, 100);
            Log.e("handlerTest", "run: " + this + "  " + SystemClock.currentThreadTimeMillis());
        }
    };

    private FloatBuffer vertexBuffer, colorBuffer, textureCoordinationBuffer, textureCoordinationOffsetIndexBuffer;

    private ShortBuffer cubePositionsIndexBuffer;

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 vMatrix;" +
                    "varying  vec4 vColor;" +
                    "attribute vec4 aColor;" +
                    "attribute vec2 vCoordinate;" +
                    "attribute vec2 vCoordinateOffset;" +
                    "varying vec2 aCoordinate;" +
                    "void main() {" +
                    "  gl_Position = vMatrix * vPosition;" +
                    "  vColor = aColor;" +
                    "  aCoordinate = vCoordinate;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "uniform sampler2D vTexture;" +
                    "varying vec2 aCoordinate;" +
                    "uniform sampler2D u_Textures[6]; // 假设最多有 6 个纹理\n" +
                    "varying vec2 v_TexCoord;\n" +
                    "uniform int u_TextureIndex;" +
                    "void main() {" +
                    "  vec2 a = vec2(aCoordinate.x, aCoordinate.y);" +
                    "  gl_FragColor = texture2D(vTexture, a);" +
                    "}";

    private int program;

    private int matrixHandler;

    private int textureUniformHandle;

    private int COORDS_VERTEX_COUNT = 3;

    private float ratio;

    private double count = 0.0;

    // 右手坐标系：食指指向x轴，中指指向y轴，大拇指指向z轴
    // 如果按照平面二维笛卡尔坐标系的画法，x轴向右，y轴向上，z轴则指向自己
    private final float[] cubePositions = {
            // top 4 points (顺时针)
            1.0f, -1.0f, 1.0f, // 0
            -1.0f, -1.0f, 1.0f, // 1
            -1.0f, 1.0f, 1.0f, // 2
            1.0f, 1.0f, 1.0f, // 3

            // bottom 4 points
            1.0f, -1.0f, -1.0f, // 4
            -1.0f, -1.0f, -1.0f, // 5
            -1.0f, 1.0f, -1.0f, // 6
            1.0f, 1.0f, -1.0f, // 7
    };

    private final short[] cubePositionsIndex = {
            4, 0, 3, 3, 7, 4, // Front face
            7, 6, 2, 2, 3, 7, // Right face
            6, 2, 1, 1, 5, 6, // Back face
            5, 1, 0, 0, 4, 5, // Left face
            0, 1, 2, 2, 3, 0, // Top face
            4, 5, 6, 6, 7, 4  // Bottom face
    };

    private final float[] textureCoordination = {
            // Front face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };

    private final float[] textureCoordinationOffset = {
            0.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 0.0f,
    };


    private int positionHandle = 0;
    private int colorHandle = 0;

    private int textureCoordinationHandle = 0;
    private int textureCoordinationOffsetHandle = 0;

    private float[] viewMatrix = new float[16];
    private float[] projectMatrix = new float[16];
    private float[] mvpMatrix = new float[16];

    private Bitmap bitmap;
    private int textureId;
    private int[] texture;

    private int[] imageIds;


    public RotatePicCubeNew(View mView) {
        super(mView);
        ByteBuffer buffer = ByteBuffer.allocateDirect(cubePositions.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        vertexBuffer = buffer.asFloatBuffer();
        vertexBuffer.put(cubePositions);
        vertexBuffer.position(0);

        ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect(cubePositionsIndex.length * 2);
        indexByteBuffer.order(ByteOrder.nativeOrder());
        cubePositionsIndexBuffer = indexByteBuffer.asShortBuffer();
        cubePositionsIndexBuffer.put(cubePositionsIndex);
        cubePositionsIndexBuffer.position(0);

        ByteBuffer coorBuffer = ByteBuffer.allocateDirect(textureCoordination.length * 4);
        coorBuffer.order(ByteOrder.nativeOrder());
        this.textureCoordinationBuffer = coorBuffer.asFloatBuffer();
        textureCoordinationBuffer.put(textureCoordination);
        textureCoordinationBuffer.position(0);

        ByteBuffer coorOffsetBuffer = ByteBuffer.allocateDirect(textureCoordinationOffset.length * 4);
        coorOffsetBuffer.order(ByteOrder.nativeOrder());
        this.textureCoordinationOffsetIndexBuffer = coorOffsetBuffer.asFloatBuffer();
        textureCoordinationOffsetIndexBuffer.put(textureCoordinationOffset);
        textureCoordinationOffsetIndexBuffer.position(0);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);


        imageIds = new int[]{R.drawable.icon_duoyun, R.drawable.icon_qingtian, R.drawable.icon_tianqi,
                R.drawable.icon_tianqi_2, R.drawable.ic_launcher_web, R.drawable.icon_xiaoxue};

        //获取变换矩阵vMatrix成员句柄
        matrixHandler = GLES20.glGetUniformLocation(program, "vMatrix");

        //获取顶点着色器的vPosition成员句柄
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");

        //获取片元着色器的vColor成员的句柄
        colorHandle = GLES20.glGetAttribLocation(program, "aColor");

        textureCoordinationHandle = GLES20.glGetAttribLocation(program, "vCoordinate");

        textureCoordinationOffsetHandle = GLES20.glGetAttribLocation(program, "vCoordinateOffset");
        textureUniformHandle = GLES20.glGetUniformLocation(program, "vTexture");
    }

    private int createTexture() {
        texture = new int[6];

        //生成纹理
        GLES20.glGenTextures(6, texture, 0);
        for (int i = 0; i < 6; i++) {
            bitmap = BitmapFactory.decodeResource(mView.getResources(), imageIds[i]);
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[i]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }


        return texture[0];
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //计算宽高比
        ratio = (float) width / height;
        //设置透视投影
        Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1, 1, 3, 50);
        //设置相机位置
        Matrix.setLookAtM(viewMatrix, 0, -30.0f, 0.0f, -30.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, viewMatrix, 0);
        handler.postDelayed(updateRunnable, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(program);

        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, mvpMatrix, 0);

        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(positionHandle);
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(textureCoordinationOffsetHandle);
        GLES20.glVertexAttribPointer(textureCoordinationOffsetHandle, 2, GLES20.GL_FLOAT, false, 0, textureCoordinationOffsetIndexBuffer);

        if (textureId == 0) {
            textureId = createTexture();
        }


        // 绘制立方体的每个面
        for (int i = 0; i < 6; i++) {
            GLES20.glEnableVertexAttribArray(textureCoordinationHandle);
            GLES20.glVertexAttribPointer(textureCoordinationHandle, 2, GLES20.GL_FLOAT, false, 0, textureCoordinationBuffer);
            // 激活纹理0
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glUniform1i(textureUniformHandle, 0);
            // 每个面绑定不同的纹理id
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[i]);

            // 绘制
            cubePositionsIndexBuffer.position(6 * i);
            // 索引法绘制正方体
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, cubePositionsIndexBuffer);
        }

        //禁止顶点数组的句柄
        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordinationHandle);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(updateRunnable);
    }
}
