package com.example.user.test6;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class MyAdapter extends ArrayAdapter<String> {

    private Context context;

    ListView listView;

    private LruCache<String, Bitmap> lruCache;

    private FTPUtil ftpUtil;

    public MyAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        this.context = context;
        //设置缓冲
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        ftpUtil = new FTPUtil();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (listView == null) {
            listView = (ListView) parent;
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.image_item, null);
        }
        String fileName = getItem(position);
        TextView textView = (TextView) convertView.findViewById(R.id.textview);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
        textView.setText(fileName);
        //将文件名转化为图片文件名
        fileName = fileName.replace(".avi", ".jpg");
        imageView.setTag(fileName);
        Bitmap bitmap = getBitmapFromMemoryCache(fileName);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageBitmap(null);

            ImageTask imageTask = (ImageTask) convertView.getTag();
            if (imageTask != null) {
                imageTask.cancel(true);
            }
            imageTask = new ImageTask();
            convertView.setTag(imageTask);
            imageTask.execute(fileName);
        }
        return convertView;
    }

    class ImageTask extends AsyncTask<String, Void, Bitmap> {
        private String fileName;

        @Override
        protected Bitmap doInBackground(String... params) {
//            Log.e("imageTask", Thread.currentThread().toString());
            fileName = params[0];
            //从二级缓存中加载
            Bitmap bitmap = getFileCache(fileName);
            if (bitmap != null)
                return bitmap;
            try {
                ftpUtil.connectServer();
                bitmap = ftpUtil.downloadBitmap(fileName);
                if (fileName != null && bitmap != null) {
                    addBitmapToMemoryCache(fileName, bitmap);
                    addFileCache(fileName, bitmap);
                }
//                Log.e("imageTask","bitmap名字:"+fileName);
//                Log.e("imageTask","bitmap大小为:"+bitmap.getByteCount());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    ftpUtil.closeServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
//            Log.e("onPostExecute", "bitmap大小:" + bitmap.getByteCount());
            ImageView imageView = (ImageView) listView.findViewWithTag(fileName);

            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }


    /**
     * 将图片放入内存中缓存
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null)
            lruCache.put(key, bitmap);
    }

    /**
     * 从缓存中读取图片
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromMemoryCache(String key) {
        return lruCache.get(key);
    }

    /**
     * 二级缓存,从本地中读取数据
     *
     * @param key
     * @return
     */
    public Bitmap getFileCache(String key) {
        FileInputStream readfileCache = null;
        Bitmap bitmap = null;
        try {
            //将URL解码作为文件名
            String fileName = URLEncoder.encode(key, "UTF-8");
            readfileCache = context.openFileInput(fileName);
            bitmap = BitmapFactory.decodeStream(readfileCache);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (readfileCache != null) {
                try {
                    readfileCache.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * 将图片放入二级缓存中
     *
     * @param key
     * @param bitmap
     */
    public void addFileCache(String key, Bitmap bitmap) {
        FileOutputStream writefileCache = null;
        try {
            String fileName = URLEncoder.encode(key, "UTF-8");
//            Log.e("fileName", fileName);
            writefileCache = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, writefileCache);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (writefileCache != null) {
                try {
                    writefileCache.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

