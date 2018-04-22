package com.megvii.idcardproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.megvii.idcardlib.IDCardScanActivity;
import com.megvii.idcardlib.util.Util;
import com.megvii.idcardquality.IDCardQualityLicenseManager;
import com.megvii.licensemanager.Manager;
import com.megvii.livenessdetection.LivenessLicenseManager;
import com.megvii.livenesslib.LivenessActivity;
import com.qcloud.image.ImageClient;
import com.qcloud.image.request.FaceCompareRequest;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by binghezhouke on 15-8-12.
 */
public class LoadingActivity extends Activity implements View.OnClickListener {

	private Button selectBtn;
	boolean isVertical;
	private RelativeLayout contentRel;
	private LinearLayout barLinear;
	private TextView WarrantyText;
	private ProgressBar WarrantyBar;
	private Button againWarrantyBtn;
	private String uuid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState==null) {
			setContentView(R.layout.activity_loading);
			init();
			initData();
			network();
		}

	}

	TextView result;

	private void init() {
		contentRel = (RelativeLayout) findViewById(R.id.loading_layout_contentRel);
		barLinear = (LinearLayout) findViewById(R.id.loading_layout_barLinear);
		WarrantyText = (TextView) findViewById(R.id.loading_layout_WarrantyText);
		WarrantyBar = (ProgressBar) findViewById(R.id.loading_layout_WarrantyBar);
		againWarrantyBtn = (Button) findViewById(R.id.loading_layout_againWarrantyBtn);
		selectBtn = (Button) findViewById(R.id.loading_layout_isVerticalBtn);
		selectBtn.setOnClickListener(this);
		uuid = Util.getUUIDString(this);
		findViewById(R.id.loading_back).setOnClickListener(this);
		findViewById(R.id.loading_front).setOnClickListener(this);
		findViewById(R.id.loading_layout_livenessBtn).setOnClickListener(this);
		findViewById(R.id.tencentyun_comparison).setOnClickListener(this);
		result = (TextView) findViewById(R.id.tv_result);
	}

	private void initData() {
		if (isVertical)
			selectBtn.setText("vertical");
		else
			selectBtn.setText("horizontal");
	}

	/**
	 * 联网授权
	 */
	private void network() {
		contentRel.setVisibility(View.GONE);
		barLinear.setVisibility(View.VISIBLE);
		againWarrantyBtn.setVisibility(View.GONE);
		WarrantyText.setText("正在联网授权中...");
		WarrantyBar.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Manager manager = new Manager(LoadingActivity.this);
				IDCardQualityLicenseManager idCardLicenseManager = new IDCardQualityLicenseManager(
						LoadingActivity.this);
				LivenessLicenseManager licenseManager = new LivenessLicenseManager(LoadingActivity.this);
				manager.registerLicenseManager(idCardLicenseManager);
				manager.registerLicenseManager(licenseManager);
				manager.takeLicenseFromNetwork(uuid);
				if (idCardLicenseManager.checkCachedLicense() > 0 && licenseManager.checkCachedLicense() > 0) {
					//授权成功
					UIAuthState(true);
				}else {
					//授权失败
					UIAuthState(false);
				}
			}
		}).start();
	}

	private void UIAuthState(final boolean isSuccess) {
		runOnUiThread(new Runnable() {
			public void run() {
				authState(isSuccess);
			}
		});
	}

	private void authState(boolean isSuccess) {
		if (isSuccess) {
			barLinear.setVisibility(View.GONE);
			WarrantyBar.setVisibility(View.GONE);
			againWarrantyBtn.setVisibility(View.GONE);
			contentRel.setVisibility(View.VISIBLE);
		} else {
			barLinear.setVisibility(View.VISIBLE);
			WarrantyBar.setVisibility(View.GONE);
			againWarrantyBtn.setVisibility(View.VISIBLE);
			contentRel.setVisibility(View.GONE);
			WarrantyText.setText("联网授权失败！请检查网络或找服务商");
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.loading_layout_againWarrantyBtn:
				network();
				break;
			case R.id.loading_layout_isVerticalBtn:
				isVertical = !isVertical;
				initData();
				break;
			case R.id.loading_front:
				requestCameraPerm(0);
				break;
			case R.id.loading_back:
				requestCameraPerm(1);
				break;
			case R.id.loading_layout_livenessBtn:
				requestCameraPerm(3);
				break;
			case R.id.tencentyun_comparison:
				faceFaceCompare();
				break;
		}
	}

	/**
	 * 人证比对
	 */
	private void faceFaceCompare() {
		if(!mSide0Flag || !mLiveFlag) {	// 正面或者人脸没拍摄的话
			showToast("请先拍摄身份证正面和活体图片");
		} else {
			new Thread() {
				@Override
				public void run() {
					compareByChildThread();
				}
			}.start();
		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String message = (String) msg.obj;
			result.setText(message);
		}
	};

	private void compareByChildThread() {
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
			compareNameList[0] = "temp0.jpg";
			compareNameList[1] = "temp1.jpg";
			compareImageList[0] = ByteimgUtils.getFileFromBytes(mSide0PorImgs, "/sdcard/" + compareNameList[0]);
			compareImageList[1] = ByteimgUtils.getFileFromBytes(mLiveImgs, "/sdcard/" + compareNameList[1]);
		} catch (Exception ex) {
			Logger.getLogger(LoadingActivity.class.getName()).log(Level.SEVERE, null, ex);
		}
		FaceCompareRequest faceCompareReq = new FaceCompareRequest(bucketName, compareNameList, compareImageList);
		ret = imageClient.faceCompare(faceCompareReq);
		System.out.println("face compare ret:" + ret);
		Message msg = Message.obtain();
		msg.what = 0x1001;
		msg.obj = ret;
		mHandler.sendMessage(msg);
	}

	/**
	 * 吐司
	 * @param content
     */
	private void showToast(String content) {
		Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
	}

	int mSide = 0;
	private void requestCameraPerm(int side) {
		mSide = side;
		if (android.os.Build.VERSION.SDK_INT >= M) {
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.CAMERA)
					!= PackageManager.PERMISSION_GRANTED) {
				//进行权限请求
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.CAMERA},
						EXTERNAL_STORAGE_REQ_CAMERA_CODE);
			} else {
				enterNextPage(side);
			}
		} else {
			enterNextPage(side);
		}
	}

	private void enterNextPage(int side){
		switch (side) {
			case 0:
				if(mSide0Flag && null != mSide0Imgs && null != mSide0PorImgs) {	// 正面信息已经拿到了
					Intent intent = new Intent(this, IdcardResultActivity.class);
					intent.putExtra("side", 0);
					intent.putExtra("idcardImg", mSide0Imgs);
					intent.putExtra("portraitImg", mSide0PorImgs);
					startActivity(intent);
				} else {
					Intent intent = new Intent(this, IDCardScanActivity.class);
					intent.putExtra("side", side);
					intent.putExtra("isvertical", isVertical);
					startActivityForResult(intent, INTO_IDCARDSCAN_PAGE);
				}
				break;
			case 1:
				if(mSide1Flag && null != mSide1Imgs) {
					Intent intent = new Intent(this, IdcardResultActivity.class);
					intent.putExtra("side", 1);
					intent.putExtra("idcardImg", mSide1Imgs);
					startActivity(intent);
				} else {
					Intent intent = new Intent(this, IDCardScanActivity.class);
					intent.putExtra("side", side);
					intent.putExtra("isvertical", isVertical);
					startActivityForResult(intent, INTO_IDCARDSCAN_PAGE);
				}
				break;
			case 3:
				if(mLiveFlag && null != mLiveImgs && null != mBunlde) {	// 传上次的信息
					MegliveResultActivity.startActivity(this, mBunlde);
				} else {
					startActivityForResult(new Intent(this, LivenessActivity.class), PAGE_INTO_LIVENESS);
				}
				break;
		}
	}

	public static final int EXTERNAL_STORAGE_REQ_CAMERA_CODE = 10;

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		if (requestCode == EXTERNAL_STORAGE_REQ_CAMERA_CODE) {
			if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {// Permission Granted

				Util.showToast(this, "获取相机权限失败");
			} else
				enterNextPage(mSide);
		}
	}


	private static final int INTO_IDCARDSCAN_PAGE = 100;
	private static final int PAGE_INTO_LIVENESS = 101;

	static byte[] mSide0Imgs;	// 身份证正面图片
	static boolean mSide0Flag;
	static byte[] mSide0PorImgs;	// 身份证头像

	static byte[] mSide1Imgs;	// 身份证反面图片
	static boolean mSide1Flag;

	static byte[] mLiveImgs; 	// 活体图片
	static boolean mLiveFlag;
	Bundle mBunlde;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == INTO_IDCARDSCAN_PAGE && resultCode == RESULT_OK) {
			Intent intent = new Intent(this, IdcardResultActivity.class);
			intent.putExtra("side", data.getIntExtra("side", 0));
			intent.putExtra("idcardImg", data.getByteArrayExtra("idcardImg"));
			if (data.getIntExtra("side", 0) == 0) { // 身份证正面
				intent.putExtra("portraitImg", data.getByteArrayExtra("portraitImg"));

				mSide0Flag = true;
				mSide0Imgs = data.getByteArrayExtra("idcardImg");
				mSide0PorImgs = data.getByteArrayExtra("portraitImg");
			} else {	// 身份证反面
				mSide1Flag = true;
				mSide1Imgs = data.getByteArrayExtra("idcardImg");
			}
			startActivity(intent);
		} else if(requestCode == PAGE_INTO_LIVENESS && resultCode == RESULT_OK) {
//            String result = data.getStringExtra("result");
//            String delta = data.getStringExtra("delta");
//            Serializable images=data.getSerializableExtra("images");
			Bundle bundle=data.getExtras();
			MegliveResultActivity.startActivity(this, bundle);

			mBunlde = bundle;
		}
	}
}