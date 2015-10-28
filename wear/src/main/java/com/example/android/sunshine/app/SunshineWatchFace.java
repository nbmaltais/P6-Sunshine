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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.example.sunhine.lib.DataLayer;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

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

        Paint mBackgroundPaint;
        Paint mPrimaryTextPaint;
        Paint mSecondaryTextPaint;
        Paint mHighTempTextPaint;
        Paint mLowTempTextPaint;

        float mXOffset;
        float mYOffset;
        private float mLineHeight;

        private int mBackgroundAmbiantColor;
        private int mBackgroundActiveColor;
        private String mTempHigh = "-";
        private String mTempLow = "-";
        private int mPrimaryTextColor;
        private int mSecondaryTextColor;
        private int mAmbiantTextColor;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());
            Resources resources = SunshineWatchFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);
            mLineHeight = resources.getDimension(R.dimen.digital_line_height);

            mBackgroundPaint = new Paint();

            mBackgroundAmbiantColor = resources.getColor(R.color.ambiant_background);
            mBackgroundActiveColor = resources.getColor(R.color.active_background);
            mBackgroundPaint.setColor(mBackgroundActiveColor);

            mPrimaryTextColor = resources.getColor(R.color.primary_text);
            mSecondaryTextColor = resources.getColor(R.color.secondary_text);
            mAmbiantTextColor = 0xFFFFFF;

            mPrimaryTextPaint = new Paint();
            mPrimaryTextPaint = createTextPaint(mPrimaryTextColor);
            mSecondaryTextPaint = createTextPaint(mSecondaryTextColor);
            mHighTempTextPaint = createTextPaint(mPrimaryTextColor);
            mLowTempTextPaint = createTextPaint(mSecondaryTextColor);
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
            return paint;
        }


        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);

            adjustPaintColorToCurrentMode(mBackgroundPaint, mBackgroundActiveColor, mBackgroundAmbiantColor);
            adjustPaintColorToCurrentMode(mPrimaryTextPaint,mPrimaryTextColor,mAmbiantTextColor);
            adjustPaintColorToCurrentMode(mSecondaryTextPaint,mSecondaryTextColor,mAmbiantTextColor);
            adjustPaintColorToCurrentMode(mHighTempTextPaint,mPrimaryTextColor,mAmbiantTextColor);
            adjustPaintColorToCurrentMode(mLowTempTextPaint,mSecondaryTextColor,mAmbiantTextColor);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = SunshineWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.primary_text_size_round : R.dimen.primary_text_size);
            float secondaryTextSize = resources.getDimension(isRound
                    ? R.dimen.secondary_text_size_round : R.dimen.secondary_text_size);

            mPrimaryTextPaint.setTextSize(textSize);
            mSecondaryTextPaint.setTextSize(secondaryTextSize);
            mHighTempTextPaint.setTextSize(secondaryTextSize);
            mLowTempTextPaint.setTextSize(secondaryTextSize);
        }


        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            mTime.setToNow();
            String text = mAmbient
                    ? String.format("%d:%02d", mTime.hour, mTime.minute)
                    : String.format("%d:%02d:%02d", mTime.hour, mTime.minute, mTime.second);
            canvas.drawText(text, mXOffset, mYOffset, mPrimaryTextPaint);

            float highSize = mHighTempTextPaint.measureText(mTempHigh);
            float tampOffsetY = mYOffset+mLineHeight;
            float padding = mHighTempTextPaint.measureText(" ");

            canvas.drawText(mTempHigh, mXOffset, tampOffsetY, mHighTempTextPaint);
            canvas.drawText(mTempLow, mXOffset+ highSize + padding, tampOffsetY, mLowTempTextPaint);
        }

    }
}
