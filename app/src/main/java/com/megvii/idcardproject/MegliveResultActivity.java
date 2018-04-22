package com.megvii.idcardproject;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.megvii.idcardproject.view.RotaterView;
import com.qcloud.image.ImageClient;
import com.qcloud.image.request.FaceCompareRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.megvii.idcardproject.LoadingActivity.mLiveFlag;
import static com.megvii.idcardproject.LoadingActivity.mLiveImgs;
import static com.megvii.idcardproject.LoadingActivity.mSide0PorImgs;

public class MegliveResultActivity extends Activity implements View.OnClickListener {
    private TextView textView;
    private ImageView mImageView;
    private LinearLayout ll_result_image;
    private ImageView bestImage, envImage;
    private String bestPath;
    private String envPath;
    private ImageView mAddImage;
    private int FLAG_REQUEST_SYSTEM_IMAGE = 0x1001;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        init();
        initData();
    }

    byte[] image_best;

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        String resultOBJ = bundle.getString("result");
        try {
            JSONObject result = new JSONObject(resultOBJ);
            textView.setText(result.getString("result"));
            int resID = result.getInt("resultcode");
            checkID(resID);
            boolean isSuccess = result.getString("result").equals(
                    getResources().getString(R.string.verify_success));
            mImageView.setImageResource(isSuccess ? R.drawable.result_success
                    : R.drawable.result_failded);
            if (isSuccess) {
                String delta = bundle.getString("delta");
                Map<String, byte[]> images = (Map<String, byte[]>) bundle.getSerializable("images");
                image_best = images.get("image_best");
                byte[] image_env = images.get("image_env");

                // 更换一下登录界面逻辑
                mLiveFlag = true;
                mLiveImgs = image_env;

                //N张动作图根据需求自取，对应字段action1、action2 ...
                // byte[] image_action1 = images.get("image_action1);
                ll_result_image.setVisibility(View.VISIBLE);
                bestImage.setImageBitmap(BitmapFactory.decodeByteArray(image_best, 0, image_best.length));
                envImage.setImageBitmap(BitmapFactory.decodeByteArray(image_env, 0, image_env.length));

                //保存图片
                //bestPath = ConUtil.saveJPGFile(this, image_best, "image_best");
                //envPath = ConUtil.saveJPGFile(this, image_env, "image_env");
                //调用活体比对API
//                imageVerify(images,delta);
            } else {
                ll_result_image.setVisibility(View.GONE);
            }
            doRotate(isSuccess);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkID(int resID) {
        if (resID == R.string.verify_success) {
            doPlay(R.raw.meglive_success);
        } else if (resID == R.string.liveness_detection_failed_not_video) {
            doPlay(R.raw.meglive_failed);
        } else if (resID == R.string.liveness_detection_failed_timeout) {
            doPlay(R.raw.meglive_failed);
        } else if (resID == R.string.liveness_detection_failed) {
            doPlay(R.raw.meglive_failed);
        } else {
            doPlay(R.raw.meglive_failed);
        }
    }

    Button mToHomeBtn;  // 返回首页按钮

    private void init() {
        mImageView = (ImageView) findViewById(R.id.result_status);
        textView = (TextView) findViewById(R.id.result_text_result);
        ll_result_image = (LinearLayout) findViewById(R.id.ll_result_image);
        bestImage = (ImageView) findViewById(R.id.iv_best);
        envImage = (ImageView) findViewById(R.id.iv_env);
        mToHomeBtn = (Button) findViewById(R.id.result_next);
        mToHomeBtn.setMovementMethod(ScrollingMovementMethod.getInstance());
        mToHomeBtn.setOnClickListener(this);
        mAddImage = (ImageView) findViewById(R.id.iv_pic);
        mAddImage.setOnClickListener(this);
    }

//    /**
//     * 如何调用Verify2.0方法  详细API字段请参考Verify文档描述
//     */
//    public void imageVerify(Map<String, byte[]> images, String delta) {
//        RequestParams requestParams = new RequestParams();
//        requestParams.put("api_key", "API_KEY");
//        requestParams.put("api_secret", "API_SECRET");
//        requestParams.put("comparison_type", "1");
//        requestParams.put("face_image_type", "meglive");
//        requestParams.put("idcard_name", "身份证姓名");
//        requestParams.put("idcard_number", "身份证号码");
//        requestParams.put("delta", delta);
//        for (Map.Entry<String, byte[]> entry : images.entrySet()) {
//            requestParams.put(entry.getKey(),
//                    new ByteArrayInputStream(entry.getValue()));
//        }
////        try {
////            requestParams.put("image_best", new File(bestPath));
////            requestParams.put("image_env", new File(envPath));
////        } catch (FileNotFoundException e) {
////            e.printStackTrace();
////        }
//
//        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
//        String url = "https://api.megvii.com/faceid/v2/verify";
//        asyncHttpClient.post(url, requestParams,
//                new AsyncHttpResponseHandler() {
//                    @Override
//                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
//                        //请求成功
//                        if (bytes != null) {
//                            String success = new String(bytes);
//                            Log.e("TAG", "成功信息：" + success);
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(int i, Header[] headers,
//                                          byte[] bytes, Throwable throwable) {
//                        // 请求失败
//                        if (bytes != null) {
//                            String error = new String(bytes);
//                            Log.e("TAG", "失败信息：" + error);
//                        } else {
//                            Log.e("TAG", "失败信息：" + throwable);
//                        }
//
//
//                    }
//                });
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Intent intent = new Intent(this, LoadingActivity.class);
//        startActivity(intent);
        finish();
    }

    public static void startActivity(Context context, Bundle bundle) {
        Intent intent = new Intent(context, MegliveResultActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.result_next) {
            finish();
        } else if(id == R.id.iv_pic) {
            doPickImageFromSystem();
        }
    }

    /**
     * 选择系统图片
     */
    private void doPickImageFromSystem() {
        /**
         * 参数一:打开系统相册的ACTION
         * 参数二:返回数据的方式(从系统相册的数据库获取)
         */
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, FLAG_REQUEST_SYSTEM_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();//使用getData方法获取要调用的接口
        Cursor c = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
        if (c != null) {
            c.moveToFirst();
            //通过游标来获取名为MediaStore.Images.Media.DATA字段的值
            String path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            mAddImage.setImageBitmap(bitmap);

            final byte[] refImg = bitmap2Bytes(bitmap);
            new Thread() {
                @Override
                public void run() {
                    compareByChildThread(refImg, image_best);
                }
            }.start();
        }
    }

    private byte[] bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private void compareByChildThread(byte[] img1, byte[] img2) {
        String appId = "1256464548";
        String secretId = "AKIDYH3NkKUCRX1NclIwbCPXUadZKMPJFw7Z";
        String secretKey = "SkWPUSPf0c6Par2EGjIjKRybWbZTGOrR";
        String bucketName = "tencentyun";
        ImageClient imageClient = new ImageClient(appId, secretId, secretKey);

        String ret;
        //2. 图片内容方式
        System.out.println("====================================================");
        String[] compareNameList = new String[2];
        File[] compareImageList = new File[2];
        try {
            compareNameList[0] = "temp10.jpg";
            compareNameList[1] = "temp20.jpg";
            compareImageList[0] = ByteimgUtils.getFileFromBytes(img1, "/sdcard/" + compareNameList[0]);
            compareImageList[1] = ByteimgUtils.getFileFromBytes(img2, "/sdcard/" + compareNameList[1]);
        } catch (Exception ex) {
            Logger.getLogger(LoadingActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
        FaceCompareRequest faceCompareReq = new FaceCompareRequest(bucketName, compareNameList, compareImageList);
        ret = imageClient.faceCompare(faceCompareReq);
        System.out.println("face compare ret:" + ret);
        Message msg = Message.obtain();
        msg.what = 0x1002;
        msg.obj = ret;
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x1002:
                    mToHomeBtn.setText((String)msg.obj);
                    break;
            }
        }
    };

    private void doRotate(boolean success) {
        RotaterView rotaterView = (RotaterView) findViewById(R.id.result_rotater);
        rotaterView.setColour(success ? 0xff4ae8ab : 0xfffe8c92);
        final ImageView statusView = (ImageView) findViewById(R.id.result_status);
        statusView.setVisibility(View.INVISIBLE);
        statusView.setImageResource(success ? R.drawable.result_success
                : R.drawable.result_failded);

        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(rotaterView,
                "progress", 0, 100);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.setDuration(600);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Animation scaleanimation = AnimationUtils.loadAnimation(
                        MegliveResultActivity.this, R.anim.scaleoutin);
                statusView.startAnimation(scaleanimation);
                statusView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();
    }

    private MediaPlayer mMediaPlayer = null;

    private void doPlay(int rawId) {
        if (mMediaPlayer == null)
            mMediaPlayer = new MediaPlayer();

        mMediaPlayer.reset();
        try {
            AssetFileDescriptor localAssetFileDescriptor = getResources()
                    .openRawResourceFd(rawId);
            mMediaPlayer.setDataSource(
                    localAssetFileDescriptor.getFileDescriptor(),
                    localAssetFileDescriptor.getStartOffset(),
                    localAssetFileDescriptor.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception localIOException) {
            localIOException.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }
    }
}