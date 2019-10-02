package com.potato.timetable.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.potato.timetable.R;
import com.potato.timetable.util.Config;
import com.potato.timetable.util.FileUtils;
import com.potato.timetable.util.Utils;

import java.io.File;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mAlphaTextView;
    private CardView mCardView;
    private ImageView mBgImageView;

    private static final int REQUEST_CODE_SYSTEM_PIC=1;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;// 动态申请存储权限标识
    private static final int REQUEST_CODE_PHOTO_CUT=3;
    public static final String EXTRA_UPDATE_BG="update_bg";
    public static final String BG_NAME="bg.jpg";

    public static String sPath;

    private float mAlpha = Config.getCardViewAlpha();
    private int mBgId =Config.getBgId();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        SeekBar seekBar=findViewById(R.id.alpha_seekBar);

        mAlphaTextView=findViewById(R.id.tv_alpha);
        mCardView=findViewById(R.id.cv_config_alpha);

        mBgImageView =findViewById(R.id.iv_bg_config);

        ImageView imageView1,imageView2,imageView3;
        imageView1=findViewById(R.id.iv_select_bg_1);
        imageView2=findViewById(R.id.iv_select_bg_2);
        imageView3=findViewById(R.id.iv_select_bg_3);

        imageView1.setOnClickListener(this);
        imageView2.setOnClickListener(this);
        imageView3.setOnClickListener(this);

        setCardViewAlpha();

        sPath =getExternalFilesDir(null).getAbsolutePath()+File.separator+"pictures";
        initActionBar();
        Button button=findViewById(R.id.btn_select_img);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //申请权限
                requestStoragePermission();
                //打开图库选取图片
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_SYSTEM_PIC);//打开系统相册
            }
        });

        int value=(int)(Config.getCardViewAlpha()*100);
        String s=value+"%";
        mAlphaTextView.setText(s);

        seekBar.setProgress(value-10);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int value=i+10;
                String str=value+"%";
                mAlphaTextView.setText(str);
                mAlpha =value/100.0f;
                mCardView.setAlpha(mAlpha);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Utils.setBackGround(mBgImageView);
    }

    @Override
    public void onClick(final View view) {
        final int id=view.getId();
        final AlertDialog alertDialog=new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("是否将其设为背景图片")
                .create();
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (id)
                {
                    case R.id.iv_select_bg_2:
                        mBgId =R.drawable.bg_1;
                        break;
                    case R.id.iv_select_bg_3:
                        mBgId =R.drawable.bg_2;
                        break;
                        default:
                            mBgId =R.drawable.bg_rem;
                            break;
                }
                mBgImageView.setImageResource(mBgId);
            }
        });
        alertDialog.show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_config,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setCardViewAlpha()
    {
        float alpha=Config.getCardViewAlpha();
        mCardView.setAlpha(alpha);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==android.R.id.home)
        {
            finish();
        }
        else if(id==R.id.menu_apply)
        {
            if(saveConfig())
            {
                Toast.makeText(this,"应用成功",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,"设置未发生改变",Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean saveConfig()
    {
        if(mBgId !=Config.getBgId()|| mAlpha !=Config.getCardViewAlpha())
        {
            Config.setCardViewAlpha(mAlpha);
            Config.setBgId(mBgId);
            Config.saveSharedPreferences(this);
            //通知MainActivity更新背景图片
            Intent intent=new Intent();
            intent.putExtra(EXTRA_UPDATE_BG,true);
            setResult(RESULT_OK,intent);
            return true;
        }
        else
            return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            if(requestCode==REQUEST_CODE_SYSTEM_PIC)
            {
                if(data!=null)
                {
                    Uri imgUri=data.getData();
                    String path= FileUtils.getPath(this,imgUri);
                    Bitmap bitmap=BitmapFactory.decodeFile(path);
                    int height=bitmap.getHeight();
                    int width=bitmap.getWidth();
                    DisplayMetrics dm=getResources().getDisplayMetrics();
                    if((float)height/width==(float)dm.heightPixels/dm.widthPixels)
                    {//如果图片比例与屏幕比例相同，直接复制图片
                        FileUtils.fileCopy(path, sPath +File.separator+BG_NAME);
                        mBgId =0;//当为0时,读取自定义背景
                        Utils.setBackGround(mBgImageView, mBgId);
                    }
                    else
                    {
                        startPhotoCrop(imgUri,height);
                    }

                }

            }
            else if(requestCode==REQUEST_CODE_PHOTO_CUT)
            {
                //预览图片改变效果,设置不会保存到本地
                mBgId =0;
                Utils.setBackGround(mBgImageView, mBgId);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // 用户同意，执行相应操作
                Log.e("TAG","用户已经同意了存储权限");
            }else {
                Toast.makeText(this,"需要访问本地图片才能完成背景图片的设置",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void requestStoragePermission() {

        int hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        Log.e("TAG","开始" + hasCameraPermission);
        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED){
            // 拥有权限，可以执行涉及到存储权限的操作
            Log.e("TAG", "你已经授权了该组权限");
        }else {
            // 没有权限，向用户申请该权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e("TAG", "向用户申请该组权限");
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            }
        }

    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.menu_config);
    }
    public void startPhotoCrop(Uri uri,int height) {
        DisplayMetrics dm=getResources().getDisplayMetrics();

        Intent intent = new Intent("com.android.camera.action.CROP");

        intent.setDataAndType(uri, "image/*");

        // crop为true是设置在开启的intent中设置显示的view可以剪裁

        intent.putExtra("crop", "true");

        intent.putExtra("scale", true);

        // aspectX aspectY 是宽高的比例

        intent.putExtra("aspectX", 9);

        intent.putExtra("aspectY", 16);


        // outputX,outputY 是剪裁图片的宽高

        intent.putExtra("outputX", Math.round (height/16.0f*9.0f));

        intent.putExtra("outputY", height);

        //设置了true的话直接返回bitmap，可能会很占内存

        intent.putExtra("return-data", false);

        //设置输出的格式

        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        File file=new File(sPath,BG_NAME);
        if(!file.getParentFile().exists())
        {
            if(!file.getParentFile().mkdirs())
                return;
        }
        //Log.d("uri",Uri.fromFile(file).getPath());

        //设置输出的地址

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

        //不启用人脸识别

        intent.putExtra("noFaceDetection", true);

        startActivityForResult(intent, REQUEST_CODE_PHOTO_CUT);

    }
}
