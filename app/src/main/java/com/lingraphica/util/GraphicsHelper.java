package com.lingraphica.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by punit on 2/14/17.
 */

public class GraphicsHelper {


    public Bitmap getThumbnailImage(File videoFile) {
        Bitmap bmp = ThumbnailUtils.createVideoThumbnail(videoFile.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
        return cropVideoBitmap(bmp);
    }

    private Bitmap outline(Bitmap _bmp, int strokeWidth) {
        Bitmap bmp = _bmp.copy(Bitmap.Config.ARGB_8888, true); // mutable always
        int bt = strokeWidth / 2;
        Paint wsPaint = new Paint();
        wsPaint.setColor(Color.BLACK);
        wsPaint.setStyle(Paint.Style.STROKE);
        wsPaint.setStrokeWidth(strokeWidth);
        Canvas c = new Canvas(bmp);
        Rect destRect = new Rect(bt, bt, bmp.getWidth() - bt, bmp.getHeight() - bt);
        c.drawRect(destRect, wsPaint);
        return bmp;
    }

    private Bitmap cropVideoBitmap(Bitmap bm) {
        float bwidth;
        float bheight;
        bwidth = bm.getWidth();
        bheight = bm.getHeight();
        int destHeight = (int) Math.min(bheight, bwidth);
        int destWidth = destHeight * 100 / 132;
        int indent = (int) ((bwidth - destWidth) / 2);
        Matrix mat = new Matrix();
        Bitmap newBMP = Bitmap.createBitmap(bm, indent, 0, destWidth, destHeight, mat, true);
        newBMP = outline(newBMP, 4);
        bm.recycle();
        return newBMP;
    }

}
