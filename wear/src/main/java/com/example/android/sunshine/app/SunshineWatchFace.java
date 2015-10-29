/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.app;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.example.sunhine.lib.DataLayer;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class SunshineWatchFace extends BaseWatchFaceService
{
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }



    private class Engine extends BaseWatchFaceService.BaseEngine
    {

        private final String TAG = /*SunshineWatchFace.class.getSimpleName() + "." +*/ Engine.class.getSimpleName();

        float mScalingRatio;

        Paint mBackgroundPaint;
        Paint mTimeTextPaint;
        Paint mDateTextPaint;
        Paint mHighTempTextPaint;
        Paint mLowTempTextPaint;
        Paint mSeparatorPaint;

        float mXOffset;
        float mTimeYOffset;
        float mDateYOffset;
        float mSeparatorYOffset;
        float mTemperatureYOffset;
        float mImageYOffset;
        private float mCenterX;
        private float mCenterY;

        private int mBackgroundAmbiantColor;
        private int mBackgroundActiveColor;
        private String mTempHigh = "-";
        private String mTempLow = "-";
        private int mPrimaryTextColor;
        private int mSecondaryTextColor;
        private int mAmbiantTextColor;
        private float mTextSpacing;

        private Bitmap mWeatherBitmap;
        private float mWeatherImageWidth;
        private float mWeatherImageHeight;
        private float mTemperatureHighXOffset;
        private float mTemperatureLowXOffset;
        private float mImageXOffet;
        private int mSeparatorColor;

        java.text.DateFormat mDateFormat;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());
            Resources resources = SunshineWatchFace.this.getResources();
            /*mTimeYOffset = resources.getDimension(R.dimen.time_y_offset);
            mDateYOffset = resources.getDimension(R.dimen.date_y_offset);
            mSeparatorYOffset = resources.getDimension(R.dimen.separator_y_offset);
            mTemperatureYOffset = resources.getDimension(R.dimen.temperature_y_offset);
            mImageYOffset = resources.getDimension(R.dimen.image_y_offset);
            mWeatherImageWidth = resources.getDimension(R.dimen.weather_image_size);
            mWeatherImageHeight = mWeatherImageWidth;*/

            mDateFormat = DateFormat.getLongDateFormat(SunshineWatchFace.this);

            mBackgroundPaint = new Paint();

            mSeparatorPaint = new Paint();


            mBackgroundAmbiantColor = resources.getColor(R.color.ambiant_background);
            mBackgroundActiveColor = resources.getColor(R.color.active_background);
            mSeparatorColor = resources.getColor(R.color.separator);

            mBackgroundPaint.setColor(mBackgroundActiveColor);
            mSeparatorPaint.setColor(mSeparatorColor);

            mPrimaryTextColor = resources.getColor(R.color.primary_text);
            mSecondaryTextColor = resources.getColor(R.color.secondary_text);
            mAmbiantTextColor = resources.getColor(R.color.ambiant_text);

            mTimeTextPaint = createTextPaint(mPrimaryTextColor);
            mDateTextPaint = createTextPaint(mSecondaryTextColor);
            mHighTempTextPaint = createTextPaint(mPrimaryTextColor);
            mLowTempTextPaint = createTextPaint(mSecondaryTextColor);
            //mLowTempTextPaint.setTypeface()

            setInteractiveUpdateRateMs(INTERACTIVE_UPDATE_RATE_MS);


        }

        // GoogleApliClient onConnected
        @Override
        public void onConnected(Bundle bundle) {
            super.onConnected(bundle);

            // Request a weather updated when we are created
            Utils.sendSimpleMessage(mGoogleApiClient,DataLayer.PATH_REQUEST_WEATHER);
            updateFromDataLayer();
        }

        /**
         * Check if we have weather info from a previous DataItem
         */
        private void updateFromDataLayer() {


            /*Utils.getDataItemForLocalNode(mGoogleApiClient, DataLayer.PATH_WEATHER, new Utils.GetDataItemCallback() {
                @Override
                public void onResult(DataMap data) {
                    updateFromDataMap(data);
                    invalidate();
                }
            });*/

        }

        private void updateFromDataMap(DataMap config) {

            mTempHigh = config.getString(DataLayer.KEY_MAX_TEMP);
            mTempLow = config.getString(DataLayer.KEY_MIN_TEMP);
            int weatherId = config.getInt(DataLayer.KEY_WEATHER_ID);
            int resId = Utils.getIconResourceForWeatherCondition(weatherId);
            if(resId!=-1)
            {
                mWeatherBitmap = BitmapFactory.decodeResource(getResources(), resId);

                mWeatherBitmap = Bitmap.createScaledBitmap(mWeatherBitmap,
                        (int)mWeatherImageWidth,(int)mWeatherImageHeight, true);
            }
            else if(mWeatherBitmap!=null)
            {
                mWeatherBitmap.recycle();
                mWeatherBitmap=null;
            }
        }


        // DataApi callbacks

        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {
            super.onDataChanged(dataEventBuffer);

            Log.d(TAG,"onDataChanged");

            boolean needRefresh=false;

            for (DataEvent dataEvent : dataEventBuffer) {

                if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                    continue;
                }


                DataItem dataItem = dataEvent.getDataItem();

                Log.d(TAG,"Received dataItem: uri = " + dataItem.getUri() );

                if (!dataItem.getUri().getPath().equals( DataLayer.PATH_WEATHER)) {
                    continue;
                }

                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                DataMap config = dataMapItem.getDataMap();
                Log.d(TAG,"DataMap = " + config );
                updateFromDataMap(config);
                needRefresh=true;

            }

            if(needRefresh)
            {
                invalidate();
            }

        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.CENTER);
            return paint;
        }


        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);

            adjustPaintColorToCurrentMode(mBackgroundPaint, mBackgroundActiveColor, mBackgroundAmbiantColor);
            adjustPaintColorToCurrentMode(mTimeTextPaint,mPrimaryTextColor,mAmbiantTextColor);
            adjustPaintColorToCurrentMode(mDateTextPaint, mSecondaryTextColor, mAmbiantTextColor);
            adjustPaintColorToCurrentMode(mHighTempTextPaint, mPrimaryTextColor, mAmbiantTextColor);
            adjustPaintColorToCurrentMode(mLowTempTextPaint, mSecondaryTextColor, mAmbiantTextColor);
            adjustPaintColorToCurrentMode(mSeparatorPaint, mSeparatorColor, mAmbiantTextColor);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            Log.d(TAG,"onApplyWindowInsets");

            // Load resources that have alternate values for round watches.
            Resources resources = SunshineWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.x_offset_round : R.dimen.x_offset);
            float timeTextSize = resources.getInteger(isRound
                    ? R.integer.time_text_size_round : R.integer.time_text_size);
            float dateTextSize = resources.getInteger(isRound
                    ? R.integer.date_text_size_round : R.integer.date_text_size);
            float temperatureTextSize = resources.getInteger(isRound
                    ? R.integer.temperature_text_size_round : R.integer.temperature_text_size);
            mTextSpacing = resources.getInteger(isRound
                    ? R.integer.text_spacing_round : R.integer.text_spacing);

            mTextSpacing *= mScalingRatio;


            mTimeTextPaint.setTextSize(timeTextSize * mScalingRatio);
            mDateTextPaint.setTextSize(dateTextSize* mScalingRatio);
            mHighTempTextPaint.setTextSize(temperatureTextSize * mScalingRatio);
            mLowTempTextPaint.setTextSize(temperatureTextSize * mScalingRatio);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            Log.d(TAG, "onSurfaceChanged");

            Resources resources = SunshineWatchFace.this.getResources();

            float baseSize =  resources.getInteger(R.integer.base_size);

            mTimeYOffset = resources.getInteger(R.integer.time_y_offset);
            mDateYOffset = resources.getInteger(R.integer.date_y_offset);
            mSeparatorYOffset = resources.getInteger(R.integer.separator_y_offset);
            mTemperatureYOffset = resources.getInteger(R.integer.temperature_y_offset);
            mTemperatureHighXOffset = resources.getInteger(R.integer.temperature_high_x_offset);
            mTemperatureLowXOffset = resources.getInteger(R.integer.temperature_low_x_offset);
            mImageXOffet = resources.getInteger(R.integer.image_x_offset);
            mImageYOffset = resources.getInteger(R.integer.image_y_offset);
            mWeatherImageWidth = resources.getInteger(R.integer.weather_image_size);
            mWeatherImageHeight = mWeatherImageWidth;


            mCenterX = width / 2f;
            mCenterY = height / 2f;
            mScalingRatio = width/baseSize;

            mTimeYOffset *= mScalingRatio;
            mDateYOffset *=  mScalingRatio;
            mSeparatorYOffset *=  mScalingRatio;
            mTemperatureYOffset *=  mScalingRatio;
            mImageYOffset *=  mScalingRatio;
            mWeatherImageWidth *=  mScalingRatio;
            mWeatherImageHeight *=  mScalingRatio;
            mTemperatureHighXOffset *=  mScalingRatio;
            mTemperatureLowXOffset *=  mScalingRatio;
            mImageXOffet *=  mScalingRatio;


        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            mTime.setToNow();


            String timeText = mAmbient
                    ? String.format("%d:%02d", mTime.hour, mTime.minute)
                    : String.format("%d:%02d:%02d", mTime.hour, mTime.minute, mTime.second);

            canvas.drawText(timeText, mCenterX, mTimeYOffset, mTimeTextPaint);

            //.format(Date date)

            String dateText = mDateFormat.format(new Date(System.currentTimeMillis()));

            //String dateText = "TODO TODO";
            canvas.drawText(dateText, mCenterX, mDateYOffset, mDateTextPaint);

            canvas.drawLine(mCenterX-50,mSeparatorYOffset,mCenterX+50,mSeparatorYOffset,mSeparatorPaint);

            //float x = mXOffset;
            if(mWeatherBitmap!=null) {
                canvas.drawBitmap(mWeatherBitmap, mImageXOffet, mImageYOffset, mBackgroundPaint);
            }

            //x = x + mWeatherImageWidth + padding;
            canvas.drawText(mTempHigh, mTemperatureHighXOffset, mTemperatureYOffset, mHighTempTextPaint);
            //x+=highSize;
            //x+=padding;
            canvas.drawText(mTempLow, mTemperatureLowXOffset,mTemperatureYOffset, mLowTempTextPaint);
        }

    }
}
