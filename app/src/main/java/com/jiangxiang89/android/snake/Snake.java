/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.jiangxiang89.android.snake;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import cn.waps.extend.SlideWall;

import cn.waps.AppConnect;

/**
 * Snake: a simple game that everyone can enjoy.
 * 
 * This is an implementation of the classic Game "Snake", in which you control a serpent roaming
 * around the garden looking for apples. Be careful, though, because when you catch one, not only
 * will you become longer, but you'll move faster. Running into yourself or the walls will end the
 * game.
 * 
 */
public class Snake extends Activity implements View.OnClickListener {

    /**
     * Constants for desired direction of moving the snake
     */
    public static int MOVE_LEFT = 0;
    public static int MOVE_UP = 1;
    public static int MOVE_DOWN = 2;
    public static int MOVE_RIGHT = 3;

    private static String ICICLE_KEY = "snake-view";

    private SnakeView mSnakeView;
    // 抽屉广告布局
    private View slidingDrawerView;

    /**
     * Called when Activity is first created. Turns off the title bar, sets up the content views,
     * and fires up the SnakeView.
     * 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snake_layout);
        AppConnect.getInstance(this);
        AppConnect.getInstance(this).initAdInfo();
        AppConnect.getInstance(this).initPopAd(this);

// 互动广告调用方式
        LinearLayout layout = (LinearLayout) this.findViewById(R.id.AdLinearLayout);
        AppConnect.getInstance(this).showBannerAd(this, layout);

        // 迷你广告调用方式
        // AppConnect.getInstance(this).setAdBackColor(Color.argb(50, 120, 240,
        // 120));//设置迷你广告背景颜色
        // AppConnect.getInstance(this).setAdForeColor(Color.YELLOW);//设置迷你广告文字颜色
        LinearLayout miniLayout = (LinearLayout) findViewById(R.id.miniAdLinearLayout);
        AppConnect.getInstance(this).showMiniAd(this, miniLayout, 10);// 10秒刷新一次

        findViewById(R.id.imageUp).setOnClickListener(this);
        findViewById(R.id.imageDown).setOnClickListener(this);
        findViewById(R.id.imageLeft).setOnClickListener(this);
        findViewById(R.id.imageRight).setOnClickListener(this);
        findViewById(R.id.speedUp).setOnClickListener(this);
        findViewById(R.id.speedDown).setOnClickListener(this);
        // 抽屉式应用墙
        // 1,将drawable-hdpi文件夹中的图片全部拷贝到新工程的drawable-hdpi文件夹中
        // 2,将layout文件夹中的detail.xml和slidewall.xml两个文件，拷贝到新工程的layout文件夹中
        // 获取抽屉样式的自定义广告
        slidingDrawerView = SlideWall.getInstance().getView(this);
        if (slidingDrawerView != null) {
            this.addContentView(slidingDrawerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        }
        mSnakeView = (SnakeView) findViewById(R.id.snake);
        mSnakeView.setDependentViews((TextView) findViewById(R.id.text),
                findViewById(R.id.arrowContainer), findViewById(R.id.background));

        if (savedInstanceState == null) {
            // We were just launched -- set up a new game
            mSnakeView.setMode(SnakeView.READY);
        } else {
            // We are being restored
            Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
            if (map != null) {
                mSnakeView.restoreState(map);
            } else {
                mSnakeView.setMode(SnakeView.PAUSE);
            }
        }
        mSnakeView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mSnakeView.getGameState() == SnakeView.RUNNING) {
                    // Normalize x,y between 0 and 1
                    float x = event.getX() / v.getWidth();
                    float y = event.getY() / v.getHeight();

                    // Direction will be [0,1,2,3] depending on quadrant
                    int direction = 0;
                    direction = (x > y) ? 1 : 0;
                    direction |= (x > 1 - y) ? 2 : 0;

                    // Direction is same as the quadrant which was clicked
                    mSnakeView.moveSnake(direction);

                } else {
                    // If the game is not running then on touching any part of the screen
                    // we start the game by sending MOVE_UP signal to SnakeView
                    mSnakeView.moveSnake(MOVE_UP);
                }
                return false;
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppConnect.getInstance(this).close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game along with the activity
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Store the game state
        outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
    }

    /**
     * Handles key events in the game. Update the direction our snake is traveling based on the
     * DPAD.
     *
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                mSnakeView.moveSnake(MOVE_UP);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mSnakeView.moveSnake(MOVE_RIGHT);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mSnakeView.moveSnake(MOVE_DOWN);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mSnakeView.moveSnake(MOVE_LEFT);
                break;
        }

        return super.onKeyDown(keyCode, msg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageUp:
                mSnakeView.moveSnake(MOVE_UP);
                break;
            case R.id.imageRight:
                mSnakeView.moveSnake(MOVE_RIGHT);
                break;
            case R.id.imageDown:
                mSnakeView.moveSnake(MOVE_DOWN);
                break;
            case R.id.imageLeft:
                mSnakeView.moveSnake(MOVE_LEFT);
                break;
            case R.id.speedDown:
                mSnakeView.slowSpeed();
                break;
            case R.id.speedUp:
                mSnakeView.growSpeed();
                break;
        }
    }
}
