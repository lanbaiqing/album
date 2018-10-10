package com.lbq.album.browser.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 1174607250 on 2018/7/19 0019.
 */

public class Img
{
    public static void save(String path, Bitmap source, boolean isRecycled) throws IOException
    {
        FileOutputStream output = new FileOutputStream(path);
        if (source.compress(Bitmap.CompressFormat.JPEG, 100, output))
        {
            if (isRecycled && !source.isRecycled())
                source.recycle();
        }
        output.close();
    }
    public static Bitmap scaling(String path, int width, int height)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        int w1 = options.outWidth;
        int h1 = options.outHeight;
        int inSampleSize = 1;
        if (width < w1 || height < h1)
        {
            int w2 = w1 / width;
            int h2 = h1 / height;
            inSampleSize = w2 < h2 ? w2 : h2;
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }
    public static Bitmap compressSize(Bitmap source, Bitmap.CompressFormat format, int quality, int maxKB)
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        source.compress(format, quality, output);
        while (output.toByteArray().length / 1024 > maxKB)
        {
            quality -= 10;
            output.reset();
            source.compress(format, quality, output);
        }
        return BitmapFactory.decodeByteArray(output.toByteArray(), 0, output.size());
    }
    public static Bitmap thumbnail(Bitmap source, int width, int height)
    {
        return ThumbnailUtils.extractThumbnail(source, width, height);
    }
    public static Bitmap circular(Bitmap source)
    {
        int width = source.getWidth();
        int height = source.getHeight();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float radius = (width > height ? height : width) / 2;
        canvas.drawCircle(radius, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);

        return bitmap;
    }
    public static Bitmap cornerRadius(Bitmap source , float radius)
    {
        int width = source.getWidth();
        int height = source.getHeight();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);

        return bitmap;
    }
    public static Bitmap clip(Bitmap source, int startX, int startY, int endX, int endY)
    {
        if (endX > source.getWidth() || endY > source.getHeight())
            return source;
        else
            return Bitmap.createBitmap(source, startX, startY, endX, endY);
    }
}
