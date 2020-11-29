package com.potato.timetable.ui.config;

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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.GridView;
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
import androidx.core.app.ActivityCompat;

import com.potato.timetable.R;
import com.potato.timetable.util.CalendarReminderUtils;
import com.potato.timetable.util.Config;
import com.potato.timetable.util.FileUtils;
import com.potato.timetable.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;

public class ConfigActivity extends AppCompatActivity {

    private TextView mAlphaTextView;
    private CardView mCardView;
    private ImageView mBgImageView;

    private static final int REQUEST_CODE_SYSTEM_PIC = 1;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;// 动态申请存储权限标识
    private static final int REQUEST_CODE_PHOTO_CUT = 3;

    private static final int REQUEST_WRITE_CALENDAR = 4;

    private static final String[] PERMISSIONS = {
            "android.permission.WRITE_CALENDAR"
    };

    public static final String EXTRA_UPDATE_BG = "update_bg";
    public static final String BG_NAME = "bg.jpg";
    public static final int CUSTOM_BG_ID = 0;//用户选择自定义背景图片

    public static String sPath;

    private float mAlpha = Config.getCardViewAlpha();
    private int mBgId = Config.getBgId();

    // 背景图片的资源id
    public static final int[] bgIds = new int[]{
            ConfigActivity.CUSTOM_BG_ID,//用户自己从相册选择
            R.color.background_color_white,
            R.drawable.bg_gradient,
            R.drawable.bg_2,
            R.drawable.bg_3,
            R.drawable.bg_4
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        SeekBar seekBar = findViewById(R.id.alpha_seekBar);

        mAlphaTextView = findViewById(R.id.tv_alpha);
        mCardView = findViewById(R.id.cv_config_alpha);

        mBgImageView = findViewById(R.id.iv_bg_config);


        initGridView();


        setCardViewAlpha();

        sPath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "pictures";
        initActionBar();

        int value = (int) (Config.getCardViewAlpha() * 100);
        String s = value + "%";
        mAlphaTextView.setText(s);

        seekBar.setProgress(value - 10);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int value = i + 10;
                String str = value + "%";
                mAlphaTextView.setText(str);
                mAlpha = value / 100.0f;
                mCardView.setAlpha(mAlpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Button cleanBtn = findViewById(R.id.btn_delete_calendar_event);
        cleanBtn.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(this, PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED) {
                CalendarReminderUtils.deleteCalendarEvent(ConfigActivity.this
                        , CalendarReminderUtils.DESCRIPTION);
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_WRITE_CALENDAR);
            }

        });

        Utils.setBackGround(this, mBgImageView);
    }


    private void initGridView() {
        GridView gridView = findViewById(R.id.gv_bg_select);
        final BgBtnAdapter bgBtnAdapter = new BgBtnAdapter(this);
        Collections.addAll(bgBtnAdapter.bgIdList,
                R.drawable.camera_logo,
                R.drawable.bg_x,
                R.drawable.bg_gradient,
                R.drawable.bg_2,
                R.drawable.bg_3,
                R.drawable.bg_4
        );

        gridView.setAdapter(bgBtnAdapter);
        gridView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (bgIds[i] == CUSTOM_BG_ID) {
                userSelectBg();
            } else {
                showBgConfirmDialog(bgIds[i]);
            }
        });
    }

    /**
     * 用户自己选择图片
     */
    private void userSelectBg() {
        //申请权限
        if (hasStoragePermission()) {
            //打开图库选取图片
            pickImgFromSystemPic();
        } else {
            requestStoragePermission();
        }
    }

    /**
     * 从系统相册选择图片
     */
    private void pickImgFromSystemPic() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SYSTEM_PIC);
    }


    /**
     * 显示背景确认对话框
     *
     * @param id
     */
    private void showBgConfirmDialog(final int id) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("是否将其设为背景图片")
                .create();
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", (dialogInterface, i) -> {
            if (mBgId != id) {
                showUserSelectBg(id);
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", (dialogInterface, i) -> alertDialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_config, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 设置CardView 透明度
     */
    private void setCardViewAlpha() {
        float alpha = Config.getCardViewAlpha();
        mCardView.setAlpha(alpha);
    }

    /**
     * 菜单栏
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (isConfigChange()) {
                final AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("是否保存设置?").create();
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", (dialogInterface, i) -> {
                    saveConfig();
                    alertDialog.dismiss();
                    finish();
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", (dialogInterface, i) -> {
                    alertDialog.dismiss();
                    finish();
                });
                alertDialog.show();
            } else {
                finish();
            }

        } else if (id == R.id.menu_apply) {
            if (isConfigChange()) {
                saveConfig();
                Toast.makeText(this, "应用成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "设置未发生改变", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @return 设置是否改变
     */
    private boolean isConfigChange() {
        return (mBgId != Config.getBgId() || mAlpha != Config.getCardViewAlpha());
    }

    /**
     * 保存设置
     */
    private void saveConfig() {
        Config.setCardViewAlpha(mAlpha);
        Config.setBgId(mBgId);
        Config.saveSharedPreferences(this);

        setUpdateResult();
    }

    /**
     * 通知MainActivity更新背景图片
     */
    private void setUpdateResult() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_UPDATE_BG, true);
        setResult(RESULT_OK, intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SYSTEM_PIC) {
                if (data != null) {
                    Uri imgUri = data.getData();
                    String path = FileUtils.getPathFromUri(this, imgUri);
                    File file = new File(path);
                    if (!file.exists()) {
                        Utils.showToast("获取图片失败");
                        return;
                    }
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        String name = file.getName();
                        String copyPath = sPath + File.separator + name;
                        try {
                            if (!FileUtils.fileCopy(getContentResolver().openInputStream(imgUri), copyPath)) {
                                Utils.showToast("复制图片失败");
                                return;
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        BitmapFactory.decodeFile(copyPath, options);
                    } else {
                        BitmapFactory.decodeFile(path, options);
                    }
                    int height = options.outHeight;
                    int width = options.outWidth;
                    if (height <= 0 || width <= 0) {
                        Utils.showToast("获取图片尺寸失败！");
                        return;
                    }
                    DisplayMetrics dm = getResources().getDisplayMetrics();

                    int contentHeight = dm.heightPixels - Utils.getStatusBarAndActionBarHeight(this);
                    float ratio = (float) contentHeight / dm.widthPixels;

                    if (height > contentHeight && width > dm.widthPixels) {
                        startPhotoCrop(imgUri, contentHeight, dm.widthPixels);
                    } else if ((float) height / width == ratio) {//如果图片比例与屏幕比例相同，直接复制图片
                        FileUtils.fileCopy(path, sPath + File.separator + BG_NAME);
                        showUserSelectBg(CUSTOM_BG_ID);
                    } else {
                        if ((float) height / width < ratio) {
                            startPhotoCrop(imgUri, height, Math.round(height / ratio));
                        } else {
                            startPhotoCrop(imgUri, Math.round(width * ratio), width);
                        }

                    }

                }
            } else if (requestCode == REQUEST_CODE_PHOTO_CUT) {
                //预览图片改变效果,设置不会保存到本地
                showUserSelectBg(CUSTOM_BG_ID);
            }
        }
    }

    public void showUserSelectBg(int id) {
        mBgId = id;//当为0时,读取自定义背景
        Utils.setBackGround(this, mBgImageView, mBgId);
        if (id == CUSTOM_BG_ID) {
            setUpdateResult();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Utils.showToast("需要访问本地图片才能完成背景图片的设置");
                }
            }
            pickImgFromSystemPic();
        } else if (requestCode == REQUEST_WRITE_CALENDAR) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Utils.showToast("没有权限！请授权");
                    return;
                }
            }
            CalendarReminderUtils.deleteCalendarEvent(ConfigActivity.this
                    , CalendarReminderUtils.DESCRIPTION);

        }
    }

    private boolean hasStoragePermission() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_STORAGE_PERMISSION);

    }

    /**
     * 初始化ActionBar
     */
    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.menu_config);
    }

    /**
     * 以屏幕分辨率比例裁剪图片
     *
     * @param uri    图片uri
     * @param height 图片高度,用于设置裁剪后的高度
     */
    public void startPhotoCrop(Uri uri, int height, int width) {
        Intent intent = new Intent("com.android.camera.action.CROP");

        intent.setDataAndType(uri, "image/*");

        // crop为true是设置在开启的intent中设置显示的view可以剪裁

        intent.putExtra("crop", "true");

        intent.putExtra("scale", true);

        // aspectX aspectY 是宽高的比例

        intent.putExtra("aspectX", width);

        intent.putExtra("aspectY", height);


        // outputX,outputY 是剪裁图片的宽高

        intent.putExtra("outputX", width);

        intent.putExtra("outputY", height);

        //设置了true的话直接返回bitmap，可能会很占内存

        intent.putExtra("return-data", false);

        //设置输出的格式

        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        File file = new File(sPath, BG_NAME);
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs())
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
