package com.ooftf.patternlock

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.ooftf.pattern.OnSlideListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        patternLock.onSlideListener = object : OnSlideListener {
            override fun onStart() {

            }

            override fun onCompleted(list: List<Int>) {
                if (list.size < 4) {
                    patternLock.error()
                    Handler().postDelayed({ patternLock.reset() }, 1000)
                }
            }

        }
    }
}
