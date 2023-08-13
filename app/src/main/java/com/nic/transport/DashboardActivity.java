package com.nic.transport;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class DashboardActivity extends AppCompatActivity {
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    TextView total_offense_submitted,total_action_taken,tvUserName,tvUserEmail;
    FloatingActionButton btn_submitoffense;
    SharedPreferenceData sharedPreferenceData;

    ImageView image_prev1,image_prev2,image_prev3,image_prev4;
    Spinner sp_request_type;
    Button btnFront,btnEngineNo,btnChasisNo,btnBack,buttonSave;
    EditText ed_vehicleno;

    private final int CAMERA_CAPTURE = 1;
    private final int GALLERY_CAPTURE = 2;
    private final int WRITE_PERMISSION_CODE = 3;
    Uri image_uri;
    int whichButtonClicked = 0;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        btn_submitoffense = findViewById(R.id.btn_submitoffense);
        navigationView = findViewById(R.id.navview);
        drawerLayout = findViewById(R.id.drawer);
        //adding customised toolbar
        toolbar = findViewById(R.id.toolbar);
        total_offense_submitted=findViewById(R.id.total_offense_submitted);
        total_action_taken=findViewById(R.id.total_action_taken);
        //tvUserName = findViewById(R.id.tvUserName);
        tvUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.tvUserName);
        tvUserEmail=(TextView) navigationView.getHeaderView(0).findViewById(R.id.tvUserEmail);
        sharedPreferenceData = new SharedPreferenceData(this);
        //setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Loading");

        image_prev1 = findViewById(R.id.prev1);
        image_prev2 = findViewById(R.id.prev2);
        image_prev3 = findViewById(R.id.prev3);
        image_prev4 = findViewById(R.id.prev4);
        btnFront = findViewById(R.id.btnFront);
        btnEngineNo = findViewById(R.id.btnEngineNo);
        btnChasisNo = findViewById(R.id.btnChasisNo);
        btnBack = findViewById(R.id.btnBack);
        buttonSave= findViewById(R.id.btnSave);
        ed_vehicleno=findViewById(R.id.vehicleno);
        sp_request_type = findViewById(R.id.request_type);


        //toggle button
        actionBarDrawerToggle = new ActionBarDrawerToggle(this , drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        actionBarDrawerToggle.syncState();
        //OffenseSubmitCount();
        GetUserDetails();

        //when an item is selected from menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.logout:
                        logoutAlertDialog();
                        break;
                }
                return true;
            }
        });

        btnFront.setOnClickListener(view -> {
            whichButtonClicked = 1;
            showImageImportDialog();
        });

        btnEngineNo.setOnClickListener(view -> {
            whichButtonClicked = 2;
            showImageImportDialog();
        });

        btnChasisNo.setOnClickListener(view -> {
            whichButtonClicked = 3;
            showImageImportDialog();
        });

        btnBack.setOnClickListener(view -> {
            whichButtonClicked = 4;
            showImageImportDialog();
        });


        buttonSave.setOnClickListener(view -> {
            if (ed_vehicleno.getText().toString().trim().equals("")){
                Toast.makeText(this, "Enter vehicle number", Toast.LENGTH_SHORT).show();
            } else if (picUri1 == null && picUri2 == null && picUri3 == null && picUri4 == null) {
                Toast.makeText(this, "Please take all photos as mentioned above", Toast.LENGTH_SHORT).show();
            }else {
                submitService();
            }
        });

    }

    public static boolean isGrantedPermissionWRITE_EXTERNAL_STORAGE(Activity activity) {
        int version = Build.VERSION.SDK_INT;
        if( version <= 32 ) {
            boolean isAllowPermissionApi28 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ;
            Log.i("general_em","isGrantedPermissionWRITE_EXTERNAL_STORAGE() - isAllowPermissionApi28: " + isAllowPermissionApi28);
            return  isAllowPermissionApi28;
        } else {
            boolean isAllowPermissionApi33 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ;
            Log.i("general_em","isGrantedPermissionWRITE_EXTERNAL_STORAGE() - isAllowPermissionApi33: " + isAllowPermissionApi33);
            return isAllowPermissionApi33;
        }
    }

    public static boolean isGrantedPermissionCAMERA(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ;
    }

    private void submitService() {
        progressDialog.show();
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.POST,ServiceManager.SaveImageData, response -> {
                progressDialog.dismiss();
                Log.d("response",response);
                String responseStr = new String(response);
                // Toast.makeText(OTPVerification.this, ""+responseStr, Toast.LENGTH_SHORT).show();
                String Authentication = StringUtils.substringBetween(responseStr, ">\"", "\"<");
                if(Authentication.equals("Uploaded Successfully")){
                    Toast.makeText(DashboardActivity.this, ""+Authentication, Toast.LENGTH_SHORT).show();
                }

                picUri1 = null;
                picUri2 = null;
                picUri3 = null;
                picUri4 = null;
                image_prev1.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                image_prev2.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                image_prev3.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                image_prev4.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                ed_vehicleno.setText("");

            },error -> {
                progressDialog.dismiss();
                Log.d("error",error.toString());
            }){
                @Nullable
                @Override
                protected Map getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date today = Calendar.getInstance().getTime();
                    String currentDate = df.format(today);
                    params.put("Mobile",sharedPreferenceData.getMobile("mobile"));
                    params.put("ServiceId",currentDate);
                    params.put("VehicleNumber",ed_vehicleno.getText().toString().trim());
                    params.put("ServiceTaken",sp_request_type.getSelectedItem().toString());
                    params.put("FtImage",ConvertImageToBase64(picUri1));
                    params.put("EnImage",ConvertImageToBase64(picUri2));
                    params.put("ChImage",ConvertImageToBase64(picUri3));
                    params.put("BkImage",ConvertImageToBase64(picUri4));

                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(DashboardActivity.this);
            requestQueue.add(stringRequest);
        }catch (Exception e){
            progressDialog.dismiss();
            e.printStackTrace();
        }

    }

    private String ConvertImageToBase64(Uri picUri) {
        String encoded = "";
        String ImagePath = RealPathUtil.getRealPath(this, picUri);
        File image = new File(ImagePath);
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        Log.i("info", image.getAbsolutePath());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        if (byteArray.length > 512000){
            byteArray = compressImage(ImagePath);
        }
        encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    public byte[] compressImage(String imageUri)
    {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;

        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;

        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    private String getRealPathFromURI(String contentURI)
    {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private Uri picUri1, picUri2, picUri3, picUri4;
    private void showImageImportDialog() {
        CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this);

        dialog.setTitle("Select Image");
        dialog.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals("Take Photo"))
                {
                    if (isGrantedPermissionWRITE_EXTERNAL_STORAGE(DashboardActivity.this)){
                        if (isGrantedPermissionCAMERA(DashboardActivity.this)){
                            switch (whichButtonClicked){
                                case 1:{
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        ContentValues values = new ContentValues(1);
                                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                        picUri1 = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri1);
                                        someActivityLauncher.launch(captureIntent);
                                    } else {
                                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
                                        picUri1 = Uri.fromFile(file);
                                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri1);
                                        someActivityLauncher.launch(captureIntent);
                                    }
                                    break;
                                }

                                case 2:{
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        ContentValues values = new ContentValues(1);
                                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                        picUri2 = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri2);
                                        someActivityLauncher.launch(captureIntent);
                                    } else {
                                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
                                        picUri2 = Uri.fromFile(file);
                                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri2);
                                        someActivityLauncher.launch(captureIntent);
                                    }
                                    break;
                                }

                                case 3:{
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        ContentValues values = new ContentValues(1);
                                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                        picUri3 = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri3);
                                        someActivityLauncher.launch(captureIntent);
                                    } else {
                                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
                                        picUri3 = Uri.fromFile(file);
                                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri3);
                                        someActivityLauncher.launch(captureIntent);
                                    }
                                    break;
                                }
                                case 4:{
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        ContentValues values = new ContentValues(1);
                                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                        picUri4 = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri4);
                                        someActivityLauncher.launch(captureIntent);
                                    } else {
                                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
                                        picUri4 = Uri.fromFile(file);
                                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri4);
                                        someActivityLauncher.launch(captureIntent);
                                    }
                                    break;
                                }
                            }
                        }else {
                            requestRuntimeCameraPermission();
                        }
                    }else {
                        requestRuntimeExternalStoragePermission();
                    }

                }
                else if (options[which].equals("Choose from Gallery"))
                {
                    if (isGrantedPermissionWRITE_EXTERNAL_STORAGE(DashboardActivity.this)){
                        if (isGrantedPermissionCAMERA(DashboardActivity.this)){
                            galleryImageActivityLauncher.launch("image/*");
                        }else {
                            requestRuntimeCameraPermission();
                        }
                    }else {
                        requestRuntimeExternalStoragePermission();
                    }


                }
                else if (options[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        dialog.create().show();
    }

    private static final int EXTERNAL_STORAGE = 101;
    private static final int CAMERA = 102;
    private void requestRuntimeExternalStoragePermission(){
        ActivityCompat.requestPermissions(this,permissions(),EXTERNAL_STORAGE);
    }

    private void requestRuntimeCameraPermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else {
                requestRuntimeCameraPermission();
            }
        }else if (requestCode == EXTERNAL_STORAGE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else {
                requestRuntimeExternalStoragePermission();
            }
        }
    }

    public static String[] storge_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storge_permissions_33 = {
            Manifest.permission.READ_MEDIA_IMAGES
    };

    public static String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p = storge_permissions_33;
        } else {
            p = storge_permissions;
        }
        return p;
    }

    boolean isGalleryOpen = false;


    ActivityResultLauncher<Intent> someActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        switch (whichButtonClicked){
                            case 1:
                                image_prev1.setImageURI(picUri1);
                                break;
                            case 2:
                                image_prev2.setImageURI(picUri2);
                                break;
                            case 3:
                                image_prev3.setImageURI(picUri3);
                                break;
                            case 4:
                                image_prev4.setImageURI(picUri4);
                                break;
                        }
                    }

                }
            });

    ActivityResultLauncher<String> galleryImageActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null){
                        switch (whichButtonClicked){
                            case 1:
                                picUri1 = result;
                                image_prev1.setImageURI(picUri1);
                                break;
                            case 2:
                                picUri2 = result;
                                image_prev2.setImageURI(picUri2);
                                break;
                            case 3:
                                picUri3 = result;
                                image_prev3.setImageURI(picUri3);
                                break;
                            case 4:
                                picUri4 = result;
                                image_prev4.setImageURI(picUri4);
                                break;
                        }
                    }
                }
            });



    private void OffenseSubmitCount() {
        RequestParams params = new RequestParams();
        params.put("Mobile",sharedPreferenceData.getMobile("mobile"));

        //  params.put("RegId", mResultEt.getText().toString().replaceAll(" ","").trim());
        new AsyncHttpClient().get(ServiceManager.DashBoardOffenseCount, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String responseStr = new String(responseBody);
                String responseString2 = "";
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    xpp.setInput(new StringReader(responseStr));
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_DOCUMENT) {
                            System.out.println("Start document");
                        } else if (eventType == XmlPullParser.START_TAG) {
                            System.out.println("Start tag " + xpp.getName());
                        } else if (eventType == XmlPullParser.END_TAG) {
                            System.out.println("End tag " + xpp.getName());
                        } else if (eventType == XmlPullParser.TEXT) {
                            responseString2 = xpp.getText();
                            System.out.println("Text " + xpp.getText());
                        }
                        eventType = xpp.next();
                    }
                    JSONArray result_data = new JSONArray(responseString2);
                    JSONObject output = result_data.getJSONObject(0);
                    String Total_Offense = output.getString("TotalSubmission");
                    String ActionTaken = output.getString("ActionTaken");
                    total_offense_submitted.setText(Total_Offense);
                    total_action_taken.setText(ActionTaken);
                }  catch (XmlPullParserException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(DashboardActivity.this, "Connection Failed Try Again Later", Toast.LENGTH_LONG).show();
            }
    });
    }

    private void logoutAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Warning");
        alertDialog.setMessage("Do you really want to logout ?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sharedPreferenceData.clearData();
                startActivity(new Intent(DashboardActivity.this,GetOTP.class));
                finishAffinity();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void GetUserDetails() {

        RequestParams params = new RequestParams();
        params.put("Mobile",sharedPreferenceData.getMobile("mobile"));
        //  params.put("RegId", mResultEt.getText().toString().replaceAll(" ","").trim());
        new AsyncHttpClient().get(ServiceManager.GetRegistrationDetails, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String responseStr = new String(responseBody);
                String responseString2 = "";
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    xpp.setInput(new StringReader(responseStr));
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_DOCUMENT) {
                            System.out.println("Start document");
                        } else if (eventType == XmlPullParser.START_TAG) {
                            System.out.println("Start tag " + xpp.getName());
                        } else if (eventType == XmlPullParser.END_TAG) {
                            System.out.println("End tag " + xpp.getName());
                        } else if (eventType == XmlPullParser.TEXT) {
                            responseString2 = xpp.getText();
                            System.out.println("Text " + xpp.getText());
                        }
                        eventType = xpp.next();
                    }
                    JSONArray result_data = new JSONArray(responseString2);
                    JSONObject output = result_data.getJSONObject(0);
                    String UserName = output.getString("User_Name");
                    String UserAddress = output.getString("User_Address");
                    String UserMobile = output.getString("User_Mobile");
                    String UserEmail = output.getString("User_Email");
                    tvUserName.setText(UserName);
                    tvUserEmail.setText(UserEmail);
                    
                    getDropDownFromServer();
                    
                    //String UserAddress = output.getString("User_Address");
                  //  String UserMobile = output.getString("User_Mobile");
                  //  String UserEmail = output.getString("User_Email");
//                    tvUserName.setText(UserName);
                }  catch (XmlPullParserException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(DashboardActivity.this, "Connection Failed Try Again Later", Toast.LENGTH_LONG).show();
            }
        });
    }

    List<String> serviceType = new ArrayList<>();
    private void getDropDownFromServer() {
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.GET,ServiceManager.ListServiceAvailable, response -> {
                progressDialog.dismiss();
                Log.d("response",response);
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        serviceType.add(jsonObject.getString("ServiceName"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,
                            serviceType);

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_request_type.setAdapter(adapter);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            },error -> {
                progressDialog.dismiss();
                Log.d("error",error.toString());
            });
            RequestQueue requestQueue = Volley.newRequestQueue(DashboardActivity.this);
            requestQueue.add(stringRequest);
        }catch (Exception e){
            progressDialog.dismiss();
            e.printStackTrace();
        }
    }


}