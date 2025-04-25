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
import com.google.samples.slideup.SlideUp
import com.google.samples.slideup.SlideUpBuilder

class SlideUpDismissActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slide_up_dismiss)

        val slideView = findViewById<View>(R.id.v_slide)

        slideView.setOnClickListener {
            Toast.makeText(this@SlideUpDismissActivity, "Click SlideView", Toast.LENGTH_SHORT).show()
        }


        val subView = findViewById<View>(R.id.v_sub)
        subView.setOnClickListener {
            Toast.makeText(this@SlideUpDismissActivity, "Click SubView", Toast.LENGTH_SHORT).show()
        }

        val slideUp = SlideUpBuilder(slideView)
            .slideToParent()
            .SlideDirection(SlideUp.SlideDirection.UP)
            .StartState(SlideUp.State.SHOWED)
            .listeners(object : SlideUp.Listener.Events {
                override fun onVisibilityChanged(visibility: Int) {
                    Log.d("zhangfei", "onVisibilityChanged, visibility:$visibility")
                }

                override fun onSlide(percent: Float) {
                    Log.d("zhangfei", "onSlide, percent:$percent")
                }
            })
            .build()
    }

}
