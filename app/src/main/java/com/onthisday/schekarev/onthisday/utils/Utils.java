package com.onthisday.schekarev.onthisday.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

import static com.onthisday.schekarev.onthisday.utils.Const.Tag.TAG_UTILS;

public class Utils {
    public static class ImageOut {
        public static void uploadImage(final StorageReference storage,
                                       final ImageView imageView, final String imageName,
                                       final int quality, final int div) {
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();

            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap = Bitmap.createScaledBitmap(bitmap, widthImage(bitmap.getWidth(), div),
                    heightImage(bitmap.getHeight(), div), false);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] data = baos.toByteArray();

            StorageReference ref = storage.child(imageName + ".jpg");
            ref.putBytes(data);
        }
    }

    public static class ImageIn {
        public static void showImageView(final StorageReference storage,
                                         final ImageView imageView, final String imageName) {
            StorageReference imageRef = storage.child(imageName + ".jpg");
            long MAX_BYTES = 1024 * 1024;
            imageRef.getBytes(MAX_BYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageView.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG_UTILS, "onFailure: " + e.getMessage());
                }
            });
        }
    }

    private static int widthImage(int width, int div) {
        return width / div;
    }

    private static int heightImage(int height, int div) {
        return height / div;
    }

}
