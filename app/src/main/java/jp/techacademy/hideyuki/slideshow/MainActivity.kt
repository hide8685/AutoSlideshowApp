package jp.techacademy.hideyuki.slideshow


import android.Manifest
import android.content.ContentResolver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Handler
import java.time.format.ResolverStyle
import java.util.*


class MainActivity :  AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                createCursol()
                getContentsInfo()
                start_stop.text="停止"
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            createCursol()
            getContentsInfo()
            start_stop.text="停止"
        }

        start_stop.setOnClickListener {
            if (mTimer == null) {
                getContentsInfo()
                start_stop.text="停止"
                step.isEnabled = false
                back.isEnabled = false
            } else {
                mTimer!!.cancel()
                mTimer = null
                start_stop.text="再生"
                step.isEnabled = true
                back.isEnabled = true
            }
        }


        step.setOnClickListener {
            if (mTimer == null) {
                moveCursol(true)
            }
        }

        back.setOnClickListener {
            if (mTimer == null) {
                moveCursol(false)
            }
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createCursol()
                    getContentsInfo()
                    start_stop.text="停止"
                }else{
                    start_stop.isEnabled = false
                    step.isEnabled = false
                    back.isEnabled = false
                }
        }
    }

    private var mTimer: Timer? = null
    private var mHandler = Handler()
    private var mCursor: Cursor? = null


    private fun createCursol(){
        val resolver = contentResolver
        mCursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目（null = 全項目）
                null, // フィルタ条件（null = フィルタなし）
                null, // フィルタ用パラメータ
                null // ソート (nullソートなし）
        )
    }

    private fun moveCursol(TF:Boolean){

        if (TF){
            if(!mCursor!!.moveToNext()){
                mCursor!!.moveToFirst()
            }
        }else{
            if(!mCursor!!.moveToPrevious()){
                mCursor!!.moveToLast()
            }
        }

        val fieldIndex = mCursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = mCursor!!.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        mHandler.post {
            imageView.setImageURI(imageUri)
            Log.d("Image","$imageUri")
        }

    }

    private fun getContentsInfo() {

        // タイマーの始動
        mTimer = Timer()
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                moveCursol(true)
            }

        }, 0, 2000)
        step.isEnabled = false
        back.isEnabled = false
    }



}