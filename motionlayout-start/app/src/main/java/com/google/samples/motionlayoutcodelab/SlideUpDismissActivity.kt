/*
 *   Copyright (C) 2019 The Android Open Source Project
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.google.samples.motionlayoutcodelab

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity;
import com.google.samples.slide.Slide
import com.google.samples.slide.SlideBuilder

class SlideUpDismissActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slide_up_dismiss)

        makeSlide(findViewById<View>(R.id.v_slide))
        val subView = findViewById<View>(R.id.v_sub)
        subView.setOnClickListener {
            Log.d("zhangfei", "subView click")
        }

        makeSlide2(findViewById<View>(R.id.v_slide2))
        val subView2 = findViewById<View>(R.id.v_sub2)
        subView2.setOnClickListener {
            Log.d("zhangfei", "subView2 click")
        }
    }

    private fun makeSlide(slideView: View) {
        slideView.setOnClickListener {
            Log.d("zhangfei", "slideView click")
        }
        val slideUp = SlideBuilder(slideView)
            .slideToParent()
            //.slideDirection(Slide.SlideDirection.LEFT.dir or Slide.SlideDirection.RIGHT.dir)
            .slideDirection(Slide.SlideDirection.VERTICAL)
            .listeners(object : Slide.Listener {
                override fun onSlide(percent: Float) {
                    Log.d("zhangfei", "onSlide, percent:$percent")
                }

                override fun onSlideToEnd() {
                    Log.e("zhangfei", "onSlideToEnd")
                }
            })
            .build()
    }

    private fun makeSlide2(slideView2: View) {
        slideView2.setOnClickListener {
            Log.d("zhangfei", "slideView2 click")
        }
        val slideUp = SlideBuilder(slideView2)
            .slideToParent()
            //.slideDirection(Slide.SlideDirection.LEFT.dir or Slide.SlideDirection.RIGHT.dir)
            .slideDirection(Slide.SlideDirection.HORIZONTAL)
            .listeners(object : Slide.Listener {
                override fun onSlide(percent: Float) {
                    Log.d("zhangfei", "onSlide, percent:$percent")
                }

                override fun onSlideToEnd() {
                    Log.e("zhangfei", "onSlideToEnd")
                }
            })
            .build()
    }

}
