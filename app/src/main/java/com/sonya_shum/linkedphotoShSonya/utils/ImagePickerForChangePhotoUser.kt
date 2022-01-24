package com.sonya_shum.linkedphotoShSonya.utils

import android.content.ContentUris
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import com.sonya_shum.linkedphotoShSonya.R
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Flash
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio

object ImagePickerForChangePhotoUser {
    fun getImage(listener: Listener, act:AppCompatActivity){
        act.addPixToActivity(R.id.placeHolderForChange, getOptions()){
            when (it.status) {
                PixEventCallback.Status.SUCCESS ->{
                    listener.onImageSelected(it.data[0])
                   // Log.d("MyLog","ImageSelected");


                } //use results as it.data

            }
        }

    }
    private fun getOptions(): Options{
        return Options().apply{
            ratio = Ratio.RATIO_AUTO                                    //Image/video capture ratio
            count = 1                                                   //Number of images to restrict selection count
            spanCount = 4                                               //Number for columns in grid
            path = "Pix/Camera"                                         //Custom Path For media Storage
            isFrontFacing = false                                       //Front Facing camera on start
            videoDurationLimitInSeconds = 10                            //Duration for video recording
            mode = Mode.Picture                                            //Option to select only pictures or videos or both
            flash = Flash.Auto                                          //Option to select flash type

        }

    }
    interface  Listener{
        fun onImageSelected(uri:  Uri)// если я хочу что бы была возможность вбрыть несколько картинок сразу- нужно передавать list
    }
}