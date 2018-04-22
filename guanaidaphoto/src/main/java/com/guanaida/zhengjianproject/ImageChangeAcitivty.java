package com.guanaida.zhengjianproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.guanaida.zhengjianproject.imageloader.ImagePicker;
import com.guanaida.zhengjianproject.imageloader.bean.ImageItem;
import com.guanaida.zhengjianproject.imageloader.ui.ImageGridActivity;
import com.guanaida.zhengjianproject.imageloader.view.CropImageView;
import com.guanaida.zhengjianproject.util.MyHttpHelper;
import com.guanaida.zhengjianproject.util.PhotoUtils;
import com.guanaida.zhengjianproject.util.PicassoImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ImageChangeAcitivty extends AppCompatActivity implements View.OnClickListener {

    private ImageView showImg;
    private SeekBar seekBar1;
    private TextView textView;
    private SeekBar seekBar2;
    private TextView textView2;
    public static final int RESULTIMAGE_CODE = 0x123 ;
    public static final String RESULTIMAGE_BASE64 = "IMAGE_BASE64" ;
    private String photo;
    private String deepenNum;
    private String dimNum;
    private Bitmap bitmap;
    private Bitmap NewBitmap;
    ArrayList<ImageItem> images = null;
    private boolean isResult = false;
    private Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 001:
                    isResult = false;
                    showImg.setImageBitmap(bitmap);
                    postImg(deepenNum, dimNum);
                    break;
                case 002:
                    isResult = false;
                    showImg.setImageBitmap(NewBitmap);
                    progressBar.setVisibility(View.GONE);
                    break;

                case 003:
                    isResult = false;
                    Toast.makeText(ImageChangeAcitivty.this, "程序异常", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    break;
            }
        }
    };
    private ProgressBar progressBar;
    private Button button;
    private Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        initView();
        TranslucentFlag();
        initImage();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraIntent();
            }
        });


    }

    private void cameraIntent() {
        Intent intent = new Intent(ImageChangeAcitivty.this, ImageGridActivity.class);
        intent.putExtra(ImageGridActivity.EXTRAS_IMAGES, images);
        startActivityForResult(intent, 100);
    }

    /**
     * 沉浸式适配
     */
    private void TranslucentFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4 全透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0 全透明实现
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);//calculateStatusColor(Color.WHITE, (int) alphaValue)

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0以上 全透明实现
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

    }

    private void initImage() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new PicassoImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setMultiMode(false);
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(1);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素
    }

    private void initView() {
        showImg = (ImageView) findViewById(R.id.showImg);
        seekBar1 = (SeekBar) findViewById(R.id.seekBar1);
        textView = (TextView) findViewById(R.id.textView);
        seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        textView2 = (TextView) findViewById(R.id.textView2);
        deepenNum = "0";
        dimNum = "50";
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                deepenNum = progress + "";
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                postImg(deepenNum, dimNum);
            }
        });
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dimNum = progress + "";
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                postImg(deepenNum, dimNum);
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(this);
    }

    private void postImg(final String deepenNum, final String dimNum) {
        if (!isResult) {
            isResult = true;
            progressBar.setVisibility(View.VISIBLE);
            if (MyHttpHelper.isConllection(this)) {
                Observable.create(new Observable.OnSubscribe<Integer>() {

                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        if (MyHttpHelper.isConllection(ImageChangeAcitivty.this)) {
                            String[] key = new String[]{"photo", "deepenNum", "dimNum"};
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("photo", PhotoUtils.bitmapToBase64(bitmap));
                            map.put("deepenNum", deepenNum);
                            map.put("dimNum", dimNum);
                            String result = MyHttpHelper.GetMessage(ImageChangeAcitivty.this, "http://183.230.180.239:58084/api/PhotoManage/SetPhotoBG", key, map);
                            if (!TextUtils.isEmpty(result)) {
                                JSONObject jsonObject;
                                try {
                                    jsonObject = new JSONObject(result);
                                    String code = jsonObject.getString("code");
                                    if (code.equals("0")) {
                                        photo=jsonObject.getString("result");
                                        NewBitmap = PhotoUtils.base64ToBitmap(photo);
                                        subscriber.onNext(0);


                                    } else if (code.equals("1")) {
                                        subscriber.onNext(1);
                                    }

                                } catch (JSONException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                            }
                        } else {
                            subscriber.onNext(1);
                        }
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        switch (integer) {
                            case 0:
                                handler.sendEmptyMessage(002);
                                break;
                            case 1:
                                handler.sendEmptyMessage(003);
                                break;

                        }
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ImageChangeAcitivty.this, "网络异常,请检查网络", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == 100) {
                images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                bitmap = BitmapFactory.decodeFile(images.get(0).path, options);
                int degree = PhotoUtils.readPictureDegree(images.get(0).path);
                bitmap = PhotoUtils.rotateToDegrees(bitmap, degree);
                handler.sendEmptyMessage(001);
            } else {
                Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button) {
            cameraIntent();

        } else if (i == R.id.button2) {
            Intent intent = new Intent();
            intent.putExtra(ImageChangeAcitivty.RESULTIMAGE_BASE64, photo);
            setResult(RESULT_OK, intent);  //多选不允许裁剪裁剪，返回数据
            finish();

        }
    }
}
