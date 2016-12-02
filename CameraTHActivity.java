package org.pervacio.pvathapplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.pervacio.pvathapplication.utils.ImageConstants;

//import com.sun.media.jai.*;


public class CameraTHActivity extends Activity {
	public static String testResult="";
	private static String TAG="AndroidCamEx";
	private Camera mCamera;
	private CameraPreview mPreview;
	private PictureCallback mPicture;
	private Button capture, switchCamera;
	private Context myContext;
	private LinearLayout cameraPreview;
	private boolean cameraFront = false;
	private String fileName;
	private String commandName;
	private ProgressDialog progressDialog;
	int i = 0;
	int counter = 0;
	int counter1 = 0;
	private static  final int CAM_PERMISSION_CODE=1001;
	private static  final int WRITESTORAGE_PERMISSION_CODE=1002;
	BoxDraw box;
	static String [] filePaths = new String[6];
	//	private ImageProcess imageProcessRef = new ImageProcess();
	private static  boolean isWhiteCaptured=false;
	private static  boolean isBlackCaptured=false;
	private static boolean isGrayCaptured=false;
	private static  boolean isRedCaptured=false;
	private static  boolean isGreenCaptured=false;
	boolean touchStatus=false;	private static  boolean isBlueCaptured=false;
	private static Bitmap bmpWhite;
	private static Bitmap bmpBlack;
	private static Bitmap bmpGray;
	private static Bitmap bmpRed;
	private static Bitmap bmpGreen;
	private static Bitmap bmpBlue;
	private ImageProcess imageProcessRef = new ImageProcess();

	static  final int WHITE_IMAGE_PROCESSED =1;
	static  final int BLACK_IMAGE_PROCESSED =2;
	static  final int GRAY_IMAGE_PROCESSED =3;
	static  final int RED_IMAGE_PROCESSED =4;
	static  final int GREEN_IMAGE_PROCESSED =5;
	static  final int BLUE_IMAGE_PROCESSED =6;
	static  final int IMAGE_BORDER_DETECTED =7;



	 public static Handler mHanlder;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_layout);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		myContext = this;
		box=new BoxDraw(this);	if (ContextCompat.checkSelfPermission(CameraTHActivity.this, Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(CameraTHActivity.this,
					Manifest.permission.CAMERA)) {
				Log.d(getString(R.string.app_name), "Show Permission rationale: Camera");
			}
			//else
			{
				Log.d(getString(R.string.app_name), "Ask For Permission: Camera");
				ActivityCompat.requestPermissions(CameraTHActivity.this,
						new String[]{Manifest.permission.CAMERA},
						CAM_PERMISSION_CODE);
			}
			mHasCameraPermission = false;
		} else {
			Log.d(getString(R.string.app_name), "Already has Permission: Camera");
			mHasCameraPermission = true;
			initialize();
		}

		if (ContextCompat.checkSelfPermission(CameraTHActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(CameraTHActivity.this,
					Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				Log.d(getString(R.string.app_name), "Show Permission rationale: Write");
			}
			//else
			{
				Log.d(getString(R.string.app_name), "Ask For Permission: Write");
				ActivityCompat.requestPermissions(CameraTHActivity.this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						WRITESTORAGE_PERMISSION_CODE);
			}
			mHasWritePermission = false;
		} else {
			Log.d(getString(R.string.app_name), "Already has Permission: Write");
			mHasWritePermission = true;
		}

		//gestureDetector.setIsLongpressEnabled(true);

		mHanlder = new Handler(){

			boolean isWhiteImageProcessed=false;
			boolean isBlackImageProcessed=false;

			boolean isGrayImageProcessed=false;
			boolean isRedImageProcessed=false;

			boolean isGreenImageProcessed=false;
			boolean isBlueImageProcessed=false;

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				switch (msg.what){

					case IMAGE_BORDER_DETECTED:
						Log.i("SatyaTest", "handler IMAGE_BORDER_DETECTED");
						ImageProcessDetectDeadPixelAsyncTask refWhite = new ImageProcessDetectDeadPixelAsyncTask(ImageConstants.WHITE);
						startAsyncTasksinParallel(refWhite,ImageConstants.WHITE);
						ImageProcessDetectDeadPixelAsyncTask refBlack = new ImageProcessDetectDeadPixelAsyncTask(ImageConstants.BLACK);
						startAsyncTasksinParallel(refBlack, ImageConstants.BLACK);
						//hideSimpleProgressDialog();
						break;
					case WHITE_IMAGE_PROCESSED:
						Log.i("SatyaTest","handler WHITE_IMAGE_PROCESSED");
						isWhiteImageProcessed=true;
						if(bmpWhite!=null){
							bmpWhite.recycle();
							Log.i("SatyaTest", " bmpWhite is recycled " + bmpWhite.isRecycled());
							bmpWhite=null;
							System.gc();
						}
						break;
					case BLACK_IMAGE_PROCESSED:
						Log.i("SatyaTest","handler BLACK_IMAGE_PROCESSED");
						isBlackImageProcessed=true;
						if(bmpBlack!=null){
							bmpBlack.recycle();
							Log.i("SatyaTest", " bmpBlack is recycled " + bmpBlack.isRecycled());
							bmpBlack=null;
							System.gc();
						}
						break;
					case GRAY_IMAGE_PROCESSED:
						Log.i("SatyaTest","handler GRAY_IMAGE_PROCESSED");
						isGrayImageProcessed=true;
						if(bmpGray!=null){
							bmpGray.recycle();
							Log.i("SatyaTest", " bmpGray is recycled " + bmpGray.isRecycled());
							bmpGray=null;
System.gc();
						}
						break;
					case RED_IMAGE_PROCESSED:
						Log.i("SatyaTest","handler RED_IMAGE_PROCESSED");
						isRedImageProcessed=true;
						if(bmpRed!=null){
							bmpRed.recycle();
							Log.i("SatyaTest", " bmpRed is recycled " + bmpRed.isRecycled());
							bmpRed=null;

							System.gc();						}
						break;
					case GREEN_IMAGE_PROCESSED:
						Log.i("SatyaTest","handler GREEN_IMAGE_PROCESSED");
						isGreenImageProcessed=true;
						if(bmpGreen!=null){
							bmpGreen.recycle();
							Log.i("SatyaTest", " bmpGreen is recycled " + bmpGreen.isRecycled());
							bmpGreen=null;
							System.gc();
						}
						break;
					case BLUE_IMAGE_PROCESSED:
						Log.i("SatyaTest","handler BLUE_IMAGE_PROCESSED");
						isBlueImageProcessed=true;
						if(bmpBlue!=null){
							bmpBlue.recycle();
							Log.i("SatyaTest", " bmpBlue is recycled " + bmpBlue.isRecycled());
							bmpBlue=null;
							System.gc();
						}
						break;
					default:
						break;
				}

				if(isWhiteImageProcessed && isBlackImageProcessed){
					StateMachine.getInstance(getApplicationContext()).postPDCommand(PDCommandName.BLACK_CAPTURED, PDConstants.TRUE);
					hideSimpleProgressDialog();
					isWhiteImageProcessed=false;
					isBlackImageProcessed=false;
				}

				if(isGrayImageProcessed && isRedImageProcessed){
					StateMachine.getInstance(getApplicationContext()).postPDCommand(PDCommandName.RED_CAPTURED, PDConstants.TRUE);
					hideSimpleProgressDialog();
					isGrayImageProcessed=false;
					isRedImageProcessed=false;
				}
				if(isGreenImageProcessed && isBlueImageProcessed){
					StateMachine.getInstance(getApplicationContext()).postPDCommand(PDCommandName.DISPLAYTESTRESULT, PDConstants.PASS);
					Log.i("SatyaTest","Sending DISPLAYTESTRESULT");

					hideSimpleProgressDialog();
					isGreenImageProcessed=false;
					isBlueImageProcessed=false;
				}
			}
		};
	}


	boolean mHasWritePermission=false;
	boolean mHasCameraPermission=false;

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[],
										   int[] grantResults) {
		switch (requestCode) {
			case CAM_PERMISSION_CODE: {
				if (grantResults.length > 0) {
					if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
						mHasCameraPermission = true;
						initialize();
					} else {
						mHasCameraPermission = false;
					}
				} else {
					mHasCameraPermission = false;
				}
				break;
			}
			case WRITESTORAGE_PERMISSION_CODE: {
				if (grantResults.length > 0) {
					if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
						mHasWritePermission = true;
					} else {
						mHasWritePermission = false;
					}
				} else {
					mHasWritePermission = false;
				}
				break;
			}
		}
	}
	private int findFrontFacingCamera() {
		int cameraId = -1;
		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				cameraId = i;
				cameraFront = true;
				break;
			}
		}
		return cameraId;
	}

	private int findBackFacingCamera() {
		int cameraId = -1;
		// Search for the back facing camera
		// get the number of cameras
		int numberOfCameras = Camera.getNumberOfCameras();
		// for every camera check
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				cameraId = i;
				cameraFront = false;
				break;
			}
		}
		return cameraId;
	}

	public void onResume() {
		super.onResume();
		Log.v("SatyaTest","onResume");
		if (!hasCamera(myContext)) {
			Toast toast = Toast.makeText(myContext,
					"Sorry, your phone does not have a camera!",
					Toast.LENGTH_LONG);
			toast.show();
			finish();
		}
		if (mCamera == null) {
			// if the front facing camera does not exist
//			if (findFrontFacingCamera() < 0) {
//				Toast.makeText(this, "No front facing camera found.",
//						Toast.LENGTH_LONG).show();
//				switchCamera.setVisibility(View.GONE);
//			}

			mCamera = Camera.open(findBackFacingCamera());
			mPicture = getPictureCallback();
			mPreview.refreshCamera(mCamera);

		}
	}
	private void focusOnTouch(MotionEvent event) {
		if (mCamera != null ) {

			Camera.Parameters parameters = mCamera.getParameters();
			if (parameters.getMaxNumMeteringAreas() > 0){
				Log.i(TAG, "fancy !");
				Rect rect = calculateFocusArea(event.getX(), event.getY());
				//addContentView(box, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				//RelativeLayout.LayoutParams.WRAP_CONTENT));
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
				meteringAreas.add(new Camera.Area(rect, 800));
				parameters.setFocusAreas(meteringAreas);
				mCamera.setParameters(parameters);
				//box.drawRectArea(true, rect);
				//box.onDraw(new Canvas());
				//box.invalidate();

				mCamera.autoFocus(new AutoFocusCallBackImpl());
			}else {
				mCamera.autoFocus(new AutoFocusCallBackImpl());
			}
		}
	}
	private static  final int FOCUS_AREA_SIZE= 50;
	private Rect calculateFocusArea(float x, float y) {
		int left = clamp(Float.valueOf((x / mPreview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
		int top = clamp(Float.valueOf((y / mPreview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
		Log.e("Kumar", "left"+left);
		Log.e("Kumar", "top="+top);
		Log.e("Kumar", "right="+left + FOCUS_AREA_SIZE);
		Log.e("Kumar", "bottom="+ top + FOCUS_AREA_SIZE);
		return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
	}

	private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
		int result;
		if (Math.abs(touchCoordinateInCameraReper)+focusAreaSize/2>1000){
			if (touchCoordinateInCameraReper>0){
				result = 1000 - focusAreaSize/2;
			} else {
				result = -1000 + focusAreaSize/2;
			}
		} else{
			result = touchCoordinateInCameraReper - focusAreaSize/2;
		}
		return result;
	}

	@Override
	public void setFinishOnTouchOutside(boolean finish) {
		super.setFinishOnTouchOutside(finish);
	}
	public void initialize() {
		cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
		mPreview = new CameraPreview(myContext, mCamera);
		cameraPreview.addView(mPreview);
		releaseCamera();
		chooseCamera(findBackFacingCamera()); // Take picture using the back camera.
		setBackCamera();
		mPreview.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.v("Kumar in", "OnTouch event");
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (touchStatus)
						focusOnTouch(event);
				}
				return true;
			}
		});
		// Start Countdown timer to create the surface
		createAutoFocusCall();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		/*if(progressDialog!=null)
			progressDialog.dismiss();*/
		this.createAutoFocusCall();
	}

	private void createAutoFocusCall() {

		if(!touchStatus) {
			new CountDownTimer(5000, 1000) {
				@Override
				public void onFinish() {
					// count finished
					// get the number of cameras
					int camerasNumber = Camera.getNumberOfCameras();
					if (camerasNumber > 1) {
						// release the old camera instance
						// switch camera, from the front and the back and vice versa

						//releaseCamera();
						//chooseCamera(findBackFacingCamera()); // Take picture using the back camera.
						//setBackCamera();
						setAutoFocusArea();
//					mCamera.takePicture(null, null, mPicture)
						if(mCamera!=null)
							mCamera.cancelAutoFocus();

						mCamera.autoFocus(new AutoFocusCallBackImpl());


						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//						mCamera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
//                            @Override
//                            public void onAutoFocusMoving(boolean start, Camera camera) {
//                                if (start) {
//                                    camera.takePicture(null, null, mPicture);
//                                }else {
//                                    Log.i("Kumar","else block remove ");
//                                    camera.takePicture(null, null, mPicture);
//                                }
//                            }
//                        });
						}
//					new CountDownTimer(10000, 1000) {
//						@Override
//						public void onFinish() {
//							// TODO Auto-generated method stub
//							// count finished
//							// get the number of cameras
//							int camerasNumber = Camera.getNumberOfCameras();
//							if (camerasNumber > 1) {
//								// release the old camera instance
//								// switch camera, from the front and the back
//								// and vice versa
////								mCamera.takePicture(null, null, mPicture);
//								mCamera.autoFocus(new AutoFocusCallBackImpl());
//							} else {
//								Toast toast = Toast
//										.makeText(
//												myContext,
//												"Sorry, your phone has only one camera!",
//												Toast.LENGTH_LONG);
//								toast.show();
//							}
//						}
//
//						@Override
//						public void onTick(long millisUntilFinished) {
//							// TODO Auto-generated method stub
//							// every time 1 second passes
//							Toast toast = Toast.makeText(myContext,
//									"Seconds Left front: " + millisUntilFinished
//											/ 1000, Toast.LENGTH_LONG);
//							}
//
//						}.start();
					} else {
						Toast toast = Toast.makeText(myContext,
								"Sorry, your phone has only one camera!",
								Toast.LENGTH_LONG);
						toast.show();
					}
				}

				@Override
				public void onTick(long millisUntilFinished) {
					// every time 1 second passes
					Toast toast = Toast.makeText(myContext, "Seconds Left back: "
							+ millisUntilFinished / 1000, Toast.LENGTH_LONG);
				}
			}.start();
		}
	}

	//Set the camera properties according to the phone camera resolution
	private void setBackCamera() {
		// TODO Auto-generated method stub
		Camera.Parameters param;
		param = mCamera.getParameters();
		List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
		boolean hasAutoFocus = supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO);
		if(hasAutoFocus)
			param.setFocusMode(Camera.Parameters.
					FOCUS_MODE_AUTO);
		//param.set("aperture", "28");
		//param.setExposureCompensation(8);
		//mCamera.cancelAutoFocus();
		Camera.Size bestSize = null;
		List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPictureSizes();

		Log.i("SatyaTest"," sizeList "+sizeList.size());
int index = sizeList.size();
		index = index*(3/4);
		switch (Build.MODEL){
			case "XT1052":
				bestSize = sizeList.get(0);
				break;
			case "XT1033":
				bestSize = sizeList.get(1);
				break;
			case "C6602":
				bestSize = sizeList.get(2); //3920*2204
				break;
			case "HTC Desire 820 dual sim":
				bestSize = sizeList.get(1);
				break;
		/*	case "HTC One M9PLUS":
				bestSize = sizeList.get(index);
				break;
			case "HTC Desire EYE":
				bestSize =sizeList.get(index);
				break;*/
		/*	case "SM-G920I":
				bestSize =sizeList.get(index);
				break;*/
			default:
				bestSize = sizeList.get(0);
				for (int i = 0; i < sizeList.size(); i++) {
					if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
						bestSize = sizeList.get(i);
					}
				}
		}

		Log.i("SatyaTest"," bestSize.width : "+bestSize.width+" bestSize.height "+bestSize.height);
		param.setPictureSize(bestSize.width, bestSize.height);
		//mCamera.cancelAutoFocus();
		mCamera.setParameters(param);
	}

	private void setFrontCamera() {
		// TODO Auto-generated method stub
		Camera.Parameters param;
		param = mCamera.getParameters();
		List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
		boolean hasAutoFocus = supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO);
		if(hasAutoFocus)
			param.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);

		Camera.Size bestSize = null;
		List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPictureSizes();

		switch (Build.MODEL){
			case "XT1033":
				bestSize = sizeList.get(1);
				break;
			case "C6602":
				bestSize = sizeList.get(0); //1920*1080
				break;
			case "HTC Desire 820 dual sim":
				if(sizeList.size() != 0)
					bestSize = sizeList.get(1);
				break;
			default:
				bestSize = sizeList.get(0);
				for (int i = 0; i < sizeList.size(); i++) {
					if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
						bestSize = sizeList.get(i);
					}
				}
		}
		param.setPictureSize(bestSize.width, bestSize.height);
		mCamera.setParameters(param);
	}


	public void chooseCamera(int id) {
		// if the camera preview is the front
		if (cameraFront) {
			// int cameraId = findBackFacingCamera();
			if (id >= 0) {
				// open the backFacingCamera
				// set a picture callback
				// refresh the preview

				mCamera = Camera.open(id);
				mPicture = getPictureCallback();
				mPreview.refreshCamera(mCamera);
			}
			try {
				Thread.sleep(1000);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		} else {
			// int cameraId = findFrontFacingCamera();
			if (id >= 0) {
				// open the backFacingCamera
				// set a picture callback
				// refresh the preview

				mCamera = Camera.open(id);
				mPicture = getPictureCallback();
				mPreview.refreshCamera(mCamera);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// when on Pause, release camera in order to be used from other
		// applications
		releaseCamera();
	}

	private boolean hasCamera(Context context) {
		// check if the device has camera
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	private PictureCallback getPictureCallback() {
		Log.v("Kumar","getPictureCallback===>");
		PictureCallback picture = new PictureCallback() {
			//Bitmap image;
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				// make a new picture file
				Log.v("SatyaTest","onPictureTaken called===>");
				File pictureFile = null;
				if(pdcmd!=null) {
					if (pdcmd.getDcmd().equals(PDCommandName.LCDTEST_WHITE)) {
						//fileName = "WHITE";
						commandName = PDCommandName.WHITE_CAPTURED;
						long startTime = System.currentTimeMillis();
						bmpWhite = BitmapFactory.decodeByteArray(data , 0, data.length);

						long endTime = System.currentTimeMillis();
						Log.i("SatyaTest", "PDCommandName.LCDTEST_WHITE decodefile " + (endTime - startTime));
						Log.i("SatyaTest"," bmpWhite width"+bmpWhite.getWidth()+" bmpWhite ht"+bmpWhite.getHeight());
						StateMachine.getInstance(getApplicationContext()).postPDCommand(commandName, PDConstants.TRUE);

						ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
						int largeAmount = manager.getLargeMemoryClass();
						int normalAmount = manager.getMemoryClass();
System.gc();

						Log.i("SatyaTest","largeAmount "+largeAmount+" normalAmount "+normalAmount);

					} else if (pdcmd.getDcmd().equals(PDCommandName.LCDTEST_BLACK)) {
						//	fileName = "BLACK";
						commandName = PDCommandName.BLACK_CAPTURED;
						long startTime = System.currentTimeMillis();
						bmpBlack = BitmapFactory.decodeByteArray(data , 0, data.length);
						long endTime = System.currentTimeMillis();
						Log.i("SatyaTest", "PDCommandName.LCDTEST_BLACK decodefile " + (endTime - startTime));

						new ImageProcessDetectBorderAsyncTask().execute(bmpWhite,bmpBlack);
						ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
						int largeAmount = manager.getLargeMemoryClass();
						int normalAmount = manager.getMemoryClass();


						Log.i("SatyaTest", "largeAmount " + largeAmount + " normalAmount " + normalAmount);


					} else if (pdcmd.getDcmd().equals(PDCommandName.LCDTEST_GRAY)) {
						//	fileName = "GRAY";
						commandName = PDCommandName.GRAY_CAPTURED;
						long startTime = System.currentTimeMillis();
						bmpGray = BitmapFactory.decodeByteArray(data , 0, data.length);
						long endTime = System.currentTimeMillis();
						Log.i("SatyaTest", "PDCommandName.LCDTEST_GRAY decodefile " + (endTime - startTime));
						StateMachine.getInstance(getApplicationContext()).postPDCommand(commandName, PDConstants.TRUE);
						ImageProcessDetectDeadPixelAsyncTask refBlack = new ImageProcessDetectDeadPixelAsyncTask(ImageConstants.GRAY);
						startAsyncTasksinParallel(refBlack, ImageConstants.GRAY);
						ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
						int largeAmount = manager.getLargeMemoryClass();
						int normalAmount = manager.getMemoryClass();


						Log.i("SatyaTest", "largeAmount " + largeAmount + " normalAmount " + normalAmount);

					}else if (pdcmd.getDcmd().equals(PDCommandName.LCDTEST_RED)) {
						//fileName = "RED";
						commandName = PDCommandName.RED_CAPTURED;
						long startTime = System.currentTimeMillis();
						bmpRed = BitmapFactory.decodeByteArray(data , 0, data.length);
						long endTime = System.currentTimeMillis();
						Log.i("SatyaTest", "PDCommandName.LCDTEST_RED decodefile " + (endTime - startTime));
						StateMachine.getInstance(getApplicationContext()).postPDCommand(commandName, PDConstants.TRUE);
						ImageProcessDetectDeadPixelAsyncTask refRed = new ImageProcessDetectDeadPixelAsyncTask(ImageConstants.RED);
						startAsyncTasksinParallel(refRed, ImageConstants.RED);
					/*	bmpBlack=null;
						bmpWhite=null;*/
					}else if (pdcmd.getDcmd().equals(PDCommandName.LCDTEST_GREEN)) {
						//fileName = "GREEN";
						commandName = PDCommandName.GREEN_CAPTURED;
						long startTime = System.currentTimeMillis();
						bmpGreen = BitmapFactory.decodeByteArray(data , 0, data.length);
						long endTime = System.currentTimeMillis();
						Log.i("SatyaTest", "PDCommandName.LCDTEST_GREEN decodefile " + (endTime - startTime));
						StateMachine.getInstance(getApplicationContext()).postPDCommand(commandName, PDConstants.TRUE);
						ImageProcessDetectDeadPixelAsyncTask refGreen = new ImageProcessDetectDeadPixelAsyncTask(ImageConstants.GREEN);
						startAsyncTasksinParallel(refGreen, ImageConstants.GREEN);
					} else if (pdcmd.getDcmd().equals(PDCommandName.LCDTEST_BLUE)) {
						//	fileName = "BLUE";
						commandName = PDCommandName.BLUE_CAPTURED;
						long startTime = System.currentTimeMillis();
						bmpBlue = BitmapFactory.decodeByteArray(data , 0, data.length);
						long endTime = System.currentTimeMillis();
						Log.i("SatyaTest", "PDCommandName.LCDTEST_BLUE decodefile " + (endTime - startTime));
						StateMachine.getInstance(getApplicationContext()).postPDCommand(commandName, PDConstants.TRUE);
						ImageProcessDetectDeadPixelAsyncTask refBlue = new ImageProcessDetectDeadPixelAsyncTask(ImageConstants.BLUE);
						startAsyncTasksinParallel(refBlue, ImageConstants.BLUE);
					}
				}
				else{

				}
				if(counter1 < 1){
					//pictureFile = getOutputMediaFile("IMAGE");
					counter1++;
				}
//				else {
//					pictureFile = getOutputMediaFile("Front");
//				}



					/*if(counter < 1){
						Toast toast = Toast.makeText(myContext, "Picture saved using back camera: "
								+ pictureFile.getName(), Toast.LENGTH_LONG);
						toast.show();
					}*/
//					counter++;
//					if(counter > 1){
//						Toast toast1 = Toast.makeText(myContext, "Picture saved using front camera: "
//								+ pictureFile.getName(), Toast.LENGTH_LONG);
//							toast1.show();
//						System.exit(0); // Close application
//					}
//					if(i == 0){ // Switch Camera
//						releaseCamera();
//						chooseCamera(findFrontFacingCamera());// Take picture using the front camera.
//						setFrontCamera();
//					}
//					i++;





				Log.i("SatyaTest","bmpWhite "+bmpWhite);
				Log.i("SatyaTest","bmpBlack "+bmpBlack);

				/*StateMachine.getInstance(getApplicationContext()).postPDCommand(commandName, PDConstants.TRUE);*/
				//	finish();

				Log.i("SatyaTest","isBlackCaptured "+isBlackCaptured+" isWhiteCaptured "+isWhiteCaptured+" isGrayCaptured "+isGrayCaptured);
				Log.i("SatyaTest","isRedCaptured "+isRedCaptured+" isGreenCaptured "+isGreenCaptured+" isBlueCaptured "+isBlueCaptured);
				if(isBlackCaptured && isWhiteCaptured && isGrayCaptured && isRedCaptured && isGreenCaptured && isBlueCaptured)
				{
					if(mPreview!=null){
						mPreview.stopCameraPriview();
					}



					Intent intent =new Intent(getApplicationContext(),ResultActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

					intent.putExtra("filePathArray", filePaths);
					startActivity(intent);

					finish();



				}
				else{
					mPreview.refreshCamera(mCamera);
				}
			}/* catch (FileNotFoundException e) {
				} catch (IOException e) {
				}
*/

			// refresh camera to continue preview
		};
		return picture;
	}

	private class AutoFocusCallBackImpl implements Camera.AutoFocusCallback {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			Log.i(TAG, "Inside autofocus callback. autofocused="+success);
			if (success) {
				mCamera.cancelAutoFocus();
				camera.takePicture(null, null, mPicture);
			}else {
				camera.takePicture(null, null, mPicture);
			}
			//play the autofocus sound
			//MediaPlayer.create(CameraTHActivity.this, R.raw.auto_focus).start();
		}
	}

	// make picture and save to a folder
	private File getOutputMediaFile(String type) {
		Log.v("Kumar","getPictureCallback===>");
		// make a new file directory inside the "sdcard" folder
		// Image will be saved to storage/emulated/0/CameraTest folder
//		File mediaStorageDir = new File("/sdcard/", "CameraTest");
		File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath(), "/CameraTest");
		Log.i("AndroidCamPath", Environment.getExternalStorageDirectory().getPath());
//		File mediaStorageDir = new File(CameraTHActivity.this.getFilesDir().getAbsolutePath(), "/CameraTest");
//        Log.i("AndroidCamPath", CameraTHActivity.this.getFilesDir().getAbsolutePath()+"/CameraTest");
		// if this "JCGCamera folder does not exist
		if (!mediaStorageDir.exists()) {
			// if you cannot make this folder return
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}

		// take the current timeStamp
		/*String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());*/
		File mediaFile;
		// and make a media file:
		/*mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ fileName + type + ".jpg");*/
		mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ fileName + type + ".bmp");

		return mediaFile;
	}

	private void releaseCamera() {
		// stop and release camera
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}
	private static PDCommand pdcmd;
	public static void setCommand(PDCommand cmd){
		pdcmd=cmd;
	}




	public void setAutoFocusArea(){
		if (mCamera != null ) {

			Camera.Parameters parameters = mCamera.getParameters();
			if (parameters.getMaxNumMeteringAreas() > 0){
				Log.i(TAG, "fancy !");
				DisplayMetrics displaymetrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
				int height = displaymetrics.heightPixels;
				int width = displaymetrics.widthPixels;
				Rect rect = calculateFocusArea(65,850);
				Log.e("Kumar", "X0"+rect.left);
				Log.e("Kumar", "XTOP"+rect.top);
				Log.e("Kumar", "XRight"+rect.right);
				Log.e("Kumar", "XBottom"+rect.bottom);


				//addContentView(box, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				//RelativeLayout.LayoutParams.WRAP_CONTENT));
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
				meteringAreas.add(new Camera.Area(rect, 800));
				parameters.setFocusAreas(meteringAreas);
				mCamera.setParameters(parameters);

				//box.drawRectArea(true, rect);
				//box.onDraw(new Canvas());
				//box.invalidate();

				mCamera.autoFocus(new AutoFocusCallBackImpl());
			}else {
				mCamera.autoFocus(new AutoFocusCallBackImpl());
			}
		}
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		//super.onBackPressed();
	}



	private class ImageProcessDetectBorderAsyncTask extends AsyncTask<Bitmap , Void, Boolean>
	{

		protected void onPreExecute() {
			// showProgressDialog("Saving Captured Image...");
			// checkFileSystem();
			// stopCam();// added for flash issue // commented for
			// flash_mode_auto

			showSimpleProgressDialog();

		};

		@Override
		protected Boolean doInBackground(Bitmap... params) {

			//Satya added wait for test
			Log.i("SatyaTest", "ImageProcessDetectBorderAsyncTask params : " + params.length);

			/*Bitmap whiteBMP = params[0];
			Bitmap blackBMP= params[1];*/

			long startTime = System.currentTimeMillis();
			//imageProcessRef.detectBorder(params[0], params[1]);

			imageProcessRef.detectBorder();

			long endTime = System.currentTimeMillis();

			Log.i("SatyaTest","detect border time taken "+(endTime-startTime));
			return true;
		}

		/*@Override
		protected void onPostExecute(Boolean result) {
		//	hideSimpleProgressDialog();

			//new ImageProcessDetectDeadPixelAsyncTask().execute(filePathArrays);


		}*/
	}


	void startAsyncTasksinParallel(ImageProcessDetectDeadPixelAsyncTask ref,String color) {

		if (color.equals(ImageConstants.WHITE)) {
			ref.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bmpWhite);
		}else if (color.equals(ImageConstants.BLACK)){
			ref.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bmpBlack);
		}else if (color.equals(ImageConstants.GRAY)){
			ref.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bmpGray);
		}else if (color.equals(ImageConstants.RED)){
			showSimpleProgressDialog();
			ref.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bmpRed);
		}else if (color.equals(ImageConstants.GREEN)){
			ref.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bmpGreen);
		}else if (color.equals(ImageConstants.BLUE)){
			showSimpleProgressDialog();
			ref.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bmpBlue);
		}

	}

	private class ImageProcessDetectDeadPixelAsyncTask extends AsyncTask<Bitmap , Void, Integer>
	{

		String bmpColor;
		ImageProcessDetectDeadPixelAsyncTask(String bmpColor){
			this.bmpColor = bmpColor;
		}
	/*	protected void onPreExecute() {
			// showProgressDialog("Saving Captured Image...");
			// checkFileSystem();
			// stopCam();// added for flash issue // commented for
			// flash_mode_auto
			showSimpleProgressDialog();

		};*/



		@Override
		protected Integer doInBackground(Bitmap... params) {

			int blackDeadPixels=0;
			int whiteDeadPixels=0;
			int grayDeadPixels=0;
			int redDeadPixels=0;
			int greenDeadPixels=0;
			int blueDeadPixels=0;
			//Satya added wait for test
			Log.i("SatyaTest", "ImageProcessDetectDeadPixelAsyncTask params : " + params);

			/*for(String s: params){
				Log.i("SatyaTest","s color "+s);


				if(s.contains(ImageConstants.BLACKIMAGE)){
					blackDeadPixels=imageProcessRef.processImageForDeadPixel(s,ImageConstants.BLACK);
				}else if(s.contains(ImageConstants.WHITEIMAGE)){
					whiteDeadPixels=imageProcessRef.processImageForDeadPixel(s,ImageConstants.WHITE);
				}else if(s.contains(ImageConstants.GRAYIMAGE)){
					grayDeadPixels=imageProcessRef.processImageForDeadPixel(s,ImageConstants.GRAY);
				}else if(s.contains(ImageConstants.REDIMAGE)){
					redDeadPixels=imageProcessRef.processImageForDeadPixel(s,ImageConstants.RED);
				}else if(s.contains(ImageConstants.GREENIMAGE)){
					greenDeadPixels=imageProcessRef.processImageForDeadPixel(s,ImageConstants.GREEN);
				}else if(s.contains(ImageConstants.BLUEIMAGE)){
					blueDeadPixels=imageProcessRef.processImageForDeadPixel(s,ImageConstants.BLUE);
				}
			}*//*

			Log.i("SatyaTest", "Dead Pixels Detected : blackDeadPixels" + blackDeadPixels+" whiteDeadPixels "+whiteDeadPixels+" grayDeadPixels "+grayDeadPixels+" redDeadPixels "+redDeadPixels+" greenDeadPixels "+greenDeadPixels+" blueDeadPixels "+blueDeadPixels);

			if(blackDeadPixels == 0&& whiteDeadPixels ==0 && grayDeadPixels ==0 && redDeadPixels ==0  && greenDeadPixels ==0 && blueDeadPixels ==0  ){
				return  true;
			}else
			{
				return false;
			}*/


		//	imageProcessRef.processImageForDeadPixel(params[0],bmpColor);

			imageProcessRef.processImageForDeadPixel(bmpColor);

			return 0;

		}

		@Override
		protected void onPostExecute(Integer result) {
			hideSimpleProgressDialog();
			//resultMsg.setVisibility(View.VISIBLE);
			//resultMsg.setText();
			String testResult="";
			/*if(result) {
				CameraTHActivity.testResult=PDConstants.PASS;
				Toast.makeText(CameraTHActivity.this, "Test is PASS ", Toast.LENGTH_SHORT).show();
				//testResult="Test Pass";

			}
			else
			{
				CameraTHActivity.testResult=PDConstants.FAIL;
				Toast.makeText(CameraTHActivity.this,"Test is FAIL ",Toast.LENGTH_SHORT).show();
				//testResult="Test Fail";
			}
*/
		/*	StateMachine.getInstance(getApplicationContext()).postPDCommand(PDCommandName.DISPLAYTESTRESULT, CameraTHActivity.testResult);

			//finish();

			startActivity(new Intent(getApplicationContext(), ResultActivity.class));*/
			//finish();


			/*Util.finishtest(result, getApplicationContext());
			finish();*/
		}
	}





	public void showSimpleProgressDialog() {
		//LogUtil.e(getClass().getSimpleName() + " showSimpleProgressDialog() ");
		progressDialog = ProgressDialog.show(CameraTHActivity.this, null,
				null);
		progressDialog.setContentView(R.layout.loader);
		progressDialog.setCancelable(false);
	}

	public void hideSimpleProgressDialog() {
		//LogUtil.e(getClass().getSimpleName() + " hideSimpleProgressDialog() ");
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}




	static class ImageProcess {

		static int minLeftX;
		static int minLeftY;
		static int maxRightX;
		static int maxRightY;
		static short[] leftBorderX;
		static short[] leftBorderY;
		static short[] rightBorderX;
		static short[] rightBorderY;
		static int verticalEnd;

		private static final int THRESHOLD_DIFF_BW = 200;
		private static final int THRESHOLD_WHITE = 400;
		private static final int THRESHOLD_BLACK=550;
		private static final int THRESHOLD_GREEN=500;
		private static final int THRESHOLD_RED=500;
		private static final int THRESHOLD_GRAY=500;
		private static final int THRESHOLD_BLUE=500;

		static int widthWhite;
		static int heightWhite;


		//static void detectBorder(Bitmap bitmapWhite, Bitmap bitmapBlack) {

		static void detectBorder() {


			/*widthWhite = bitmapWhite.getWidth();
			heightWhite = bitmapWhite.getHeight();*/

			widthWhite = bmpWhite.getWidth();
			heightWhite = bmpWhite.getHeight();

			Log.i("SatyaTest","detect Borer called");

			//Creating Luminance difference array

			short[][] LCDWhiteBlackDiff = new short[heightWhite][widthWhite];

			int pixelBlackColor =0;// (int)bitmap.getPixel(y, x);
			int pixelWhiteColor=0;
			byte pixelBlackColorRed=0;
			byte pixelBlackColorGreen=0;
			byte pixelBlackColorBlue=0;
			byte pixelWhiteColorRed=0;
			byte pixelWhiteColorGreen=0;
			byte pixelWhiteColorBlue=0;

			for (int x = 0; x < heightWhite; ++x) {
				for (int y = 0; y < widthWhite; ++y) {

				//	pixelBlackColor = bitmapBlack.getPixel(y, x);

					pixelBlackColor = bmpBlack.getPixel(y, x);

					pixelBlackColorRed = (byte) Color.red(pixelBlackColor);
					pixelBlackColorGreen = (byte)Color.green(pixelBlackColor);
					pixelBlackColorBlue = (byte)Color.blue(pixelBlackColor);

				//	pixelWhiteColor = bitmapWhite.getPixel(y, x);

					pixelWhiteColor = bmpWhite.getPixel(y, x);

					pixelWhiteColorRed = (byte)Color.red(pixelWhiteColor);
					pixelWhiteColorGreen = (byte)Color.green(pixelWhiteColor);
					pixelWhiteColorBlue = (byte)Color.blue(pixelWhiteColor);


				//	LCDWhiteBlackDiff[x][y]= (pixelWhiteColorRed & 0xff- pixelBlackColorRed & 0xff) + (pixelWhiteColorGreen & 0xff -pixelBlackColorGreen & 0xff) + (pixelWhiteColorBlue & 0xff -pixelBlackColorBlue & 0xff);

					//LCDWhiteBlackDiff[x][y]= (short)((pixelWhiteColorRed - pixelBlackColorRed) + (pixelWhiteColorGreen -pixelBlackColorGreen ) + (pixelWhiteColorBlue -pixelBlackColorBlue ));

					LCDWhiteBlackDiff[x][y]= (short) (((pixelWhiteColorRed & 0xff) - (pixelBlackColorRed & 0xff)) + ((pixelWhiteColorGreen & 0xff) -(pixelBlackColorGreen & 0xff)) + ((pixelWhiteColorBlue & 0xff -pixelBlackColorBlue & 0xff)));

					//LCDWhiteBlackDiff[x][y]= Math.abs(pixelWhiteColorRed-pixelBlackColorRed) + Math.abs(pixelWhiteColorGreen-pixelBlackColorGreen) + Math.abs(pixelWhiteColorBlue-pixelBlackColorBlue);

				}
			}



			leftBorderX = new short[heightWhite];
			leftBorderY = new short[heightWhite];
			rightBorderX = new short[heightWhite];
			rightBorderY = new short[heightWhite];

			for (int i=0; i<heightWhite; i++){
				leftBorderX[i]=0;
				leftBorderY[i]=0;
				rightBorderX[i]=0;
				rightBorderY[i]=0;
			}


			verticalEnd=0;



			for (int i = 1; i < heightWhite; i++)  //i,j=0 has been excluded for safety
			{
				for (int j = 1; j < widthWhite - 5; j++)
				{
					if ( ((LCDWhiteBlackDiff[i][j])  > THRESHOLD_DIFF_BW ) && ((LCDWhiteBlackDiff[i][j + 1] ) > THRESHOLD_DIFF_BW ) && ( (LCDWhiteBlackDiff[i][j + 2] ) > THRESHOLD_DIFF_BW )&& ( (LCDWhiteBlackDiff[i][j + 3]  )> THRESHOLD_DIFF_BW )&& ((LCDWhiteBlackDiff[i][j + 4] ) > THRESHOLD_DIFF_BW))
					{
						leftBorderX[verticalEnd] = (short)j;
						leftBorderY[verticalEnd] = (short)i;

						for (j = widthWhite - 1; j > 0; j--)
						{
							if (( (LCDWhiteBlackDiff[i][j] ) > THRESHOLD_DIFF_BW) &&( (LCDWhiteBlackDiff[i][j - 1] ) > THRESHOLD_DIFF_BW )&&( ( LCDWhiteBlackDiff[i][j - 2] ) > THRESHOLD_DIFF_BW ) && ((LCDWhiteBlackDiff[i][j - 3]  )> THRESHOLD_DIFF_BW ) && ( (LCDWhiteBlackDiff[i][j - 4] ) > THRESHOLD_DIFF_BW))
							{
								rightBorderX[verticalEnd] = (short)j;
								rightBorderY[verticalEnd] = (short)i;

								//	L2Rdistance[verticalEnd] = rightBorderX[verticalEnd] - leftBorderX[verticalEnd];
								verticalEnd++;
								break;
							}
						}
						break;
					}
				}
			}


			minLeftX = widthWhite;
			minLeftY=heightWhite;
			maxRightX=0;
			maxRightY=0;

			for (int i = 0; i < verticalEnd; i++)
			{
				if (minLeftX > (leftBorderX[i]))
					minLeftX = (leftBorderX[i]);

				if (minLeftY > (leftBorderY[i]))
					minLeftY = (leftBorderY[i]);

				if (maxRightX < (rightBorderX[i]))
					maxRightX = (rightBorderX[i]);

				if (maxRightY < (rightBorderY[i]) )
					maxRightY = (rightBorderY[i] );
			}

			Log.i("SatyaTest"," minLeftX "+minLeftX+"  minLeftY "+minLeftY+" maxRightX "+maxRightX+" maxRightY "+maxRightY);



			LCDWhiteBlackDiff=null;

			System.gc();
			Log.i("SatyaTest", "gc call made in detectBorder");
			Log.i("SatyaTest","End of detectBorder");


			mHanlder.sendEmptyMessage(IMAGE_BORDER_DETECTED);



		}

	//	static int processImageForDeadPixel(Bitmap bitmap, String displayColor){
			static int processImageForDeadPixel( String displayColor){

			int result=0;
			// filling white in areas other than LCD


			//an integer array that will store ARGB pixel values
			int[][] rgbValuesBlack;

			int pixelColor=0;





			 int width = 0;
			 int height = 0;


				switch (displayColor){

					case ImageConstants.WHITE:
						width = bmpWhite.getWidth();
						height=bmpWhite.getHeight();
						break;
					case ImageConstants.BLACK:
						width = bmpBlack.getWidth();
						height=bmpBlack.getHeight();
						break;
					case ImageConstants.GRAY:
						width = bmpGray.getWidth();
						height=bmpGray.getHeight();
						break;
					case ImageConstants.RED:
						width = bmpRed.getWidth();
						height=bmpRed.getHeight();
						break;
					case ImageConstants.GREEN:
						width = bmpGreen.getWidth();
						height=bmpGreen.getHeight();
						break;
					case ImageConstants.BLUE:
						width = bmpBlue.getWidth();
						height=bmpBlue.getHeight();
						break;
					default:
						break;


				}


			byte[][] LCD_Red = new byte[height][width];
			byte[][] LCD_Green = new byte[height][width];
			byte[][] LCD_Blue = new byte[height][width];


			Log.i("SatyaTest","processImageForDeadPixel "+displayColor);

			for (int x = 0; x < height; ++x) {
				for (int y = 0; y < width; ++y) {



				//	pixelColor =  bitmap.getPixel(y, x);



					switch (displayColor){

						case ImageConstants.WHITE:
							pixelColor = bmpWhite.getPixel(y,x);
							break;
						case ImageConstants.BLACK:
							pixelColor = bmpBlack.getPixel(y,x);
							break;
						case ImageConstants.GRAY:
							pixelColor = bmpGray.getPixel(y, x);
							break;
						case ImageConstants.RED:
							pixelColor = bmpRed.getPixel(y, x);
							break;
						case ImageConstants.GREEN:
							pixelColor = bmpGreen.getPixel(y, x);
							break;
						case ImageConstants.BLUE:
							pixelColor = bmpBlue.getPixel(y,x);
							break;
						default:
							break;


					}



					LCD_Red[x][y] = (byte) Color.red(pixelColor);
					LCD_Green[x][y] = (byte) Color.green(pixelColor);
					LCD_Blue[x][y] = (byte) Color.blue(pixelColor);
				}
			}

			int red=0,green=0,blue=0;
			int threshold=0;

			switch (displayColor){

				case ImageConstants.WHITE:
					red=255;
					green=255;
					blue=255;
					threshold=THRESHOLD_WHITE;

					break;
				case ImageConstants.BLACK:
					red=0;
					green=0;
					blue=0;
					threshold=THRESHOLD_BLACK;
					break;

				case  ImageConstants.RED:
					red=255;
					green=0;
					blue=0;
					threshold=THRESHOLD_RED;

					break;
				case  ImageConstants.GREEN:
					red=0;
					green=255;
					blue=0;
					threshold=THRESHOLD_GREEN;

					break;
				case  ImageConstants.BLUE:
					red=0;
					green=0;
					blue=255;
					threshold=THRESHOLD_BLUE;
					break;
				case  ImageConstants.GRAY:
					red=127;
					green=127;
					blue=127;
					threshold=THRESHOLD_GRAY;
					break;
				default:
					break;
			}



			for (int k = 0; k < verticalEnd; k++)
			{
				int i = leftBorderY[k];
				for (int j = (leftBorderX[k] )+50; j >= minLeftX; j--)
				{
					LCD_Red[i][j] = (byte)red;
					LCD_Green[i][j] =(byte) green;
					LCD_Blue[i][j] =(byte) blue;
				}

				for (int j = (rightBorderX[k])-50; j <= maxRightX; j++)
				{
					//Log.i("SatyaTest","rightBorderX[k] "+rightBorderX[k]);
					LCD_Red[i][j] = (byte)red;
					LCD_Green[i][j] = (byte) green;
					LCD_Blue[i][j] = (byte) blue;
				}
			}



			double localDeviation;
			int dangerPixels = 0;
			for (int i = minLeftY; i <= maxRightY-2; i++)
			{
				for (int j = minLeftX; j <= maxRightX-2; j++)
				{
					localDeviation = Math.abs(red - (LCD_Red[i][j]& 0xff)) + Math.abs(green - (LCD_Green[i][j] & 0xff)) + Math.abs(blue - (LCD_Blue[i][j] & 0xff));
					localDeviation += Math.abs(red - (LCD_Red[i][j + 1]& 0xff)) + Math.abs(green - (LCD_Green[i][j + 1] & 0xff)) +  Math.abs(blue- (LCD_Blue[i][j + 1] & 0xff));
					localDeviation += Math.abs(red - (LCD_Red[i][j + 2]& 0xff)) +Math.abs(green - (LCD_Green[i][j + 2] & 0xff)) +  Math.abs(blue- (LCD_Blue[i][j + 2] & 0xff));

					localDeviation += Math.abs(red - (LCD_Red[i + 1][j] & 0xff)) + Math.abs(green - (LCD_Green[i + 1][j] & 0xff)) +  Math.abs(blue - (LCD_Blue[i + 1][j] & 0xff));
					localDeviation += Math.abs(red - (LCD_Red[i + 1][j + 1] & 0xff)) + Math.abs(green - (LCD_Green[i + 1][j + 1] & 0xff)) +  Math.abs(blue - (LCD_Blue[i + 1][j + 1] & 0xff));
					localDeviation += Math.abs(red - (LCD_Red[i + 1][j + 2] & 0xff)) + Math.abs(green - (LCD_Green[i + 1][j + 2] & 0xff)) +  Math.abs(blue - (LCD_Blue[i + 1][j + 2] & 0xff));

					localDeviation += Math.abs(red - (LCD_Red[i + 2][j] & 0xff)) + Math.abs(green - (LCD_Green[i + 2][j] & 0xff)) +  Math.abs(blue - (LCD_Blue[i + 2][j] & 0xff));
					localDeviation += Math.abs(red - (LCD_Red[i + 2][j + 1] & 0xff)) + Math.abs(green - (LCD_Green[i + 2][j + 1]& 0xff)) +  Math.abs(blue- (LCD_Blue[i + 2][j + 1] & 0xff));
					localDeviation += Math.abs(red - (LCD_Red[i + 2][j + 2] & 0xff)) + Math.abs(green - (LCD_Green[i + 2][j + 2] & 0xff)) +  Math.abs(blue- (LCD_Blue[i + 2][j + 2] & 0xff));

					localDeviation = localDeviation / 9;

					if (localDeviation > threshold)
						dangerPixels++;
				}
			}

			result=dangerPixels;

			Log.i("SatyaTest", "detect dead pixels dangerPixels " + dangerPixels + " displayColor " + displayColor);


			if(displayColor.equals(ImageConstants.WHITE)){
				mHanlder.sendEmptyMessage(WHITE_IMAGE_PROCESSED);

			}else if (displayColor.equals(ImageConstants.BLACK)){
				mHanlder.sendEmptyMessage(BLACK_IMAGE_PROCESSED);
			}else if (displayColor.equals(ImageConstants.GRAY)){
				mHanlder.sendEmptyMessage(GRAY_IMAGE_PROCESSED);
			}else if (displayColor.equals(ImageConstants.RED)){
				mHanlder.sendEmptyMessage(RED_IMAGE_PROCESSED);
			}else if (displayColor.equals(ImageConstants.GREEN)){
				mHanlder.sendEmptyMessage(GREEN_IMAGE_PROCESSED);
			}else if (displayColor.equals(ImageConstants.BLUE)){
				mHanlder.sendEmptyMessage(BLUE_IMAGE_PROCESSED);
			}

			LCD_Red=null;
			LCD_Green=null;
			LCD_Blue=null;

			System.gc();


			return  result;

		}
	}

}