package com.example.user.test6;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private ProgressDialog progressDialog;
    private List<String> titleList = new ArrayList<String>();
    private MyAdapter myAdapter;

    
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
//        setContentView(R.layout.test_main);
//         imageView= (ImageView) findViewById(R.id.image);
        showProcessDialog();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//        downloadFormVidicon();
//            }
//        }).start();
        new DownloadTitleTask().execute(0);
        myAdapter = new MyAdapter(this, 0, titleList);
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(myAdapter);
    }

    class DownloadTitleTask extends AsyncTask {

        @Override
        protected Bitmap doInBackground(Object[] params) {
            FTPUtil ftpUtil=new FTPUtil();
            try {
                ftpUtil.connectServer();
                ftpUtil.downloadTitleList(titleList);
//                Bitmap bitmap=ftpUtil.downloadBitmap("N20160612171510.jpg");
//                return bitmap;
//                Log.e("Bitmap","文件大小"+bitmap.getByteCount());
            } catch (IOException e) {
                e.printStackTrace();
//                Toast.makeText(MainActivity.this,"网络连接失败",Toast.LENGTH_SHORT).show();
            }finally {
                try {
                    ftpUtil.closeServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
           return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (myAdapter != null) {
                myAdapter.notifyDataSetChanged();
                closeProcessDialog();
//            imageView.setImageBitmap(bitmap);
//            closeProcessDialog();
            }
            
        }
    }
private void showProcessDialog(){
    progressDialog = new ProgressDialog(MainActivity.this);
    progressDialog.setMessage("加载中,请稍后");
    progressDialog.setCanceledOnTouchOutside(false);
    progressDialog.show();
}
    private void closeProcessDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
