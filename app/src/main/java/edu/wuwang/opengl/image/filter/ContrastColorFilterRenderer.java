package edu.wuwang.opengl.image.filter;

import android.content.Context;
import android.opengl.GLES20;

/**
 * Created by wuwang on 2016/10/22
 */
public class ContrastColorFilterRenderer extends AFilterRenderer {

    private ColorFilterRenderer.Filter filterType;

    private int hChangeType;
    private int hChangeColor;

    public ContrastColorFilterRenderer(Context context, ColorFilterRenderer.Filter filterType) {
        super(context, "filter/half_color_vertex.sh", "filter/half_color_fragment.sh");
        this.filterType = filterType;
    }

    @Override
    public void onDrawSet() {
        GLES20.glUniform1i(hChangeType, filterType.getType());
        GLES20.glUniform3fv(hChangeColor, 1, filterType.data(), 0);
    }

    @Override
    public void onDrawCreatedSet(int mProgram) {
        hChangeType = GLES20.glGetUniformLocation(mProgram, "vChangeType");
        hChangeColor = GLES20.glGetUniformLocation(mProgram, "vChangeColor");
    }

}
