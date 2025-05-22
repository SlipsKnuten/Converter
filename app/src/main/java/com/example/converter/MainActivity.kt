package com.example.converter

import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.CycleInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.DecimalFormat
import kotlin.math.roundToInt
import kotlin.random.Random

data class ConverterConfig(
    val inputEditText: EditText,
    val inputSeekBar: SeekBar,
    val resultTextView: TextView,
    val fromUnitName: String,
    val toUnitName: String,
    val conversionFactor: Double,
    val absMinInput: Double,
    val absMaxInput: Double,
    val seekBarInternalMax: Int = 100
)

class MainActivity : AppCompatActivity() {

    private val decimalFormat = DecimalFormat("#.##")
    private var isUpdatingSeekBarFromEditTextByCode = false
    private var isUpdatingEditTextFromSeekBarByCode = false

    private lateinit var switchTheme: SwitchMaterial
    private lateinit var iconSun: ImageView
    private lateinit var iconMoon: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "ThemePrefs"
    private val KEY_IS_DARK_MODE = "isDarkMode"

    private lateinit var gifExplosionView: ImageView
    private lateinit var mainContentLayout: View
    private val explosionHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean(KEY_IS_DARK_MODE, false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_main)

        switchTheme = findViewById(R.id.switchTheme)
        iconSun = findViewById(R.id.iconSun)
        iconMoon = findViewById(R.id.iconMoon)
        gifExplosionView = findViewById(R.id.gifExplosionView)
        mainContentLayout = findViewById(R.id.mainContentLayout)

        switchTheme.isChecked = isDarkMode
        updateIconVisibility(isDarkMode)

        switchTheme.setOnCheckedChangeListener { _, newIsDarkMode ->
            if (newIsDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            saveThemePreference(newIsDarkMode)
        }

        setupConverter(
            ConverterConfig(
                findViewById(R.id.editTextKilos), findViewById(R.id.seekBarKilos), findViewById(R.id.textViewResultPounds),
                "Kilograms", "Pounds", 2.20462, 0.0, 150.0
            )
        )
        setupConverter(
            ConverterConfig(
                findViewById(R.id.editTextCm), findViewById(R.id.seekBarCm), findViewById(R.id.textViewResultInches),
                "Centimeters", "Inches", 0.393701, 0.0, 300.0
            )
        )
        setupConverter(
            ConverterConfig(
                findViewById(R.id.editTextLiters), findViewById(R.id.seekBarLiters), findViewById(R.id.textViewResultGallons),
                "Liters", "Gallons (US)", 0.264172, 0.0, 50.0
            )
        )
        setupConverter(
            ConverterConfig(
                findViewById(R.id.editTextMeters), findViewById(R.id.seekBarMeters), findViewById(R.id.textViewResultFeet),
                "Meters", "Feet", 3.28084, 0.0, 100.0
            )
        )
    }

    private fun saveThemePreference(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_DARK_MODE, isDarkMode).apply()
    }

    private fun updateIconVisibility(isDarkMode: Boolean) {
    }

    private fun setupConverter(config: ConverterConfig) {
        config.inputEditText.setText(decimalFormat.format(config.absMinInput))
        config.inputEditText.hint = "${decimalFormat.format(config.absMinInput)} ${config.fromUnitName}"
        config.inputSeekBar.max = config.seekBarInternalMax
        config.inputSeekBar.progress = 0
        config.inputSeekBar.tag = Pair(config.absMinInput, config.absMaxInput)

        config.inputEditText.addTextChangedListener(object : TextWatcher {
            var lastTextChangeShakeTime: Long = 0
            val textShakeThreshold = 100L

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingEditTextFromSeekBarByCode) return
                isUpdatingSeekBarFromEditTextByCode = true

                val value = s.toString().toDoubleOrNull() ?: config.absMinInput
                val (absMin, absMax) = config.inputSeekBar.tag as Pair<Double, Double>
                var clampedValue = value.coerceIn(absMin, absMax)

                val originalInputString = s?.toString()
                val originalInputValue = originalInputString?.toDoubleOrNull()
                var textChangedDueToCode = false

                if (originalInputValue != null && originalInputValue != clampedValue) {
                    config.inputEditText.setText(decimalFormat.format(clampedValue))
                    config.inputEditText.text?.let { currentText ->
                        config.inputEditText.setSelection(currentText.length)
                    }
                    textChangedDueToCode = true
                } else if (originalInputString != null && originalInputString.isNotEmpty() && originalInputValue == null) {
                    clampedValue = absMin
                    config.inputEditText.setText(decimalFormat.format(clampedValue))
                    config.inputEditText.text?.let { currentText ->
                        config.inputEditText.setSelection(currentText.length)
                    }
                    textChangedDueToCode = true
                }

                val progress = if (absMax - absMin != 0.0) {
                    ((clampedValue - absMin) / (absMax - absMin) * config.inputSeekBar.max).roundToInt()
                } else 0
                config.inputSeekBar.progress = progress.coerceIn(0, config.inputSeekBar.max)
                updateConversionResult(config, clampedValue)

                if (!textChangedDueToCode && s.toString().isNotEmpty()) {
                    val currentTime = System.currentTimeMillis()
                    if(currentTime - lastTextChangeShakeTime > textShakeThreshold) {
                        triggerInstantScreenShake(intensity = 2f, duration = 40)
                        lastTextChangeShakeTime = currentTime
                    }
                }
                isUpdatingSeekBarFromEditTextByCode = false
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val currentSeekBar = config.inputSeekBar
        currentSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            val rect = Rect()
            val xOffsetDp = -15
            val yOffsetDp = -25
            val xOffsetPx = (xOffsetDp * resources.displayMetrics.density).toInt()
            val yOffsetPx = (yOffsetDp * resources.displayMetrics.density).toInt()
            var listenerLastProgressTimeForShake: Long = 0
            var listenerLastProgressValueForShake: Int = 0

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (isUpdatingSeekBarFromEditTextByCode || !fromUser) return
                isUpdatingEditTextFromSeekBarByCode = true

                val (absMin, absMax) = seekBar.tag as Pair<Double, Double>
                val valueRange = absMax - absMin
                val actualValue = if (seekBar.max > 0) {
                    absMin + (progress.toDouble() / seekBar.max) * valueRange
                } else {
                    absMin
                }
                config.inputEditText.setText(decimalFormat.format(actualValue))
                updateConversionResult(config, actualValue)

                if (fromUser && seekBar.thumb != null) {
                    seekBar.thumb.copyBounds(rect)
                    val thumbAbsoluteCenterX = seekBar.paddingLeft + rect.left + rect.width() / 2f
                    val thumbAbsoluteCenterY = seekBar.paddingTop + rect.top + rect.height() / 2f
                    val seekBarLocation = IntArray(2)
                    seekBar.getLocationOnScreen(seekBarLocation)

                    val gifWidth = gifExplosionView.width.takeIf { it > 0 } ?: (100 * resources.displayMetrics.density).toInt()
                    val gifHeight = gifExplosionView.height.takeIf { it > 0 } ?: (100 * resources.displayMetrics.density).toInt()

                    val rootLayoutLocation = IntArray(2)
                    (gifExplosionView.parent as View).getLocationOnScreen(rootLayoutLocation)

                    var gifTargetX = seekBarLocation[0] + thumbAbsoluteCenterX - (gifWidth / 2f)
                    var gifTargetY = seekBarLocation[1] + thumbAbsoluteCenterY - (gifHeight / 2f)

                    gifTargetX += xOffsetPx
                    gifTargetY += yOffsetPx

                    val finalGifX = (gifTargetX - rootLayoutLocation[0]).toInt()
                    val finalGifY = (gifTargetY - rootLayoutLocation[1])

                    triggerGifExplosion(finalGifX, finalGifY)

                    val currentTime = System.currentTimeMillis()
                    if (listenerLastProgressTimeForShake == 0L) {
                        listenerLastProgressTimeForShake = currentTime
                        listenerLastProgressValueForShake = progress
                    }
                    val deltaTime = currentTime - listenerLastProgressTimeForShake
                    val deltaProgress = kotlin.math.abs(progress - listenerLastProgressValueForShake)

                    if (deltaTime > 15 && deltaProgress > 0) {
                        val speedFactor = (deltaProgress.toFloat() / deltaTime.toFloat()) * 100f
                        val baseIntensity = 2.5f
                        val maxIntensity = 7f
                        val shakeIntensity = (baseIntensity + speedFactor * 0.4f).coerceIn(baseIntensity, maxIntensity)
                        val baseDuration = 60L
                        val minDuration = 40L
                        val shakeDuration = (baseDuration - speedFactor * 3f).toLong().coerceIn(minDuration, baseDuration)
                        triggerInstantScreenShake(intensity = shakeIntensity, duration = shakeDuration)
                        listenerLastProgressValueForShake = progress
                        listenerLastProgressTimeForShake = currentTime
                    }
                }
                isUpdatingEditTextFromSeekBarByCode = false
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                listenerLastProgressTimeForShake = System.currentTimeMillis()
                listenerLastProgressValueForShake = seekBar.progress

                if (seekBar.thumb != null) {
                    seekBar.thumb.copyBounds(rect)
                    val thumbAbsoluteCenterX = seekBar.paddingLeft + rect.left + rect.width() / 2f
                    val thumbAbsoluteCenterY = seekBar.paddingTop + rect.top + rect.height() / 2f
                    val seekBarLocation = IntArray(2); seekBar.getLocationOnScreen(seekBarLocation)

                    val gifWidth = gifExplosionView.width.takeIf { it > 0 } ?: (100 * resources.displayMetrics.density).toInt()
                    val gifHeight = gifExplosionView.height.takeIf { it > 0 } ?: (100 * resources.displayMetrics.density).toInt()

                    val rootLayoutLocation = IntArray(2)
                    (gifExplosionView.parent as View).getLocationOnScreen(rootLayoutLocation)

                    var gifTargetX = seekBarLocation[0] + thumbAbsoluteCenterX - (gifWidth / 2f)
                    var gifTargetY = seekBarLocation[1] + thumbAbsoluteCenterY - (gifHeight / 2f)
                    gifTargetX += xOffsetPx
                    gifTargetY += yOffsetPx

                    val finalGifX = (gifTargetX - rootLayoutLocation[0]).toInt()
                    val finalGifY = (gifTargetY - rootLayoutLocation[1])

                    triggerGifExplosion(finalGifX, finalGifY, true)
                    triggerInstantScreenShake(intensity = 8f, duration = 80)
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                listenerLastProgressTimeForShake = 0L
                mainContentLayout.animate().translationX(0f).translationY(0f).setDuration(100).start()
            }
        })
        updateConversionResult(config, config.absMinInput)
    }

    private fun updateConversionResult(config: ConverterConfig, inputValue: Double) {
        val resultValue = inputValue * config.conversionFactor
        config.resultTextView.text = "${decimalFormat.format(resultValue)} ${config.toUnitName}"
    }

    private fun triggerGifExplosion(x: Int, y: Float, isInitialBurst: Boolean = false) {
        val params = gifExplosionView.layoutParams as FrameLayout.LayoutParams
        params.leftMargin = x
        params.topMargin = y.roundToInt()
        gifExplosionView.requestLayout()

        Glide.with(this)
            .asGif()
            .load(R.drawable.explosion)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>, isFirstResource: Boolean): Boolean {
                    gifExplosionView.visibility = View.GONE
                    return false
                }
                override fun onResourceReady(resource: GifDrawable, model: Any, target: Target<GifDrawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    resource.setLoopCount(1)
                    val gifDuration = getGifDuration(resource)
                    val displayDuration = if(isInitialBurst) (gifDuration * 1.2).toLong() else gifDuration
                    explosionHandler.postDelayed({
                        gifExplosionView.visibility = View.GONE
                    }, displayDuration)
                    return false
                }
            })
            .into(gifExplosionView)
        gifExplosionView.visibility = View.VISIBLE
    }

    private fun getGifDuration(drawable: GifDrawable): Long {
        var totalDuration = 0L
        try {
            val stateField = GifDrawable::class.java.getDeclaredField("state")
            stateField.isAccessible = true
            val gifState = stateField.get(drawable)

            val frameLoaderField = gifState::class.java.getDeclaredField("frameLoader")
            frameLoaderField.isAccessible = true
            val frameLoader = frameLoaderField.get(gifState)

            val gifDecoderField = frameLoader::class.java.getDeclaredField("gifDecoder")
            gifDecoderField.isAccessible = true
            val gifDecoder = gifDecoderField.get(frameLoader)

            val getDelayMethod = gifDecoder::class.java.getMethod("getDelay", Int::class.javaPrimitiveType)
            val getFrameCountMethod = gifDecoder::class.java.getMethod("getFrameCount")
            val frameCount = getFrameCountMethod.invoke(gifDecoder) as Int

            for (i in 0 until frameCount) {
                totalDuration += getDelayMethod.invoke(gifDecoder, i) as Int
            }
        } catch (e: Exception) {
            totalDuration = 800L
        }
        return if (totalDuration > 0 && totalDuration < 5000) totalDuration else 800L
    }

    private fun triggerInstantScreenShake(intensity: Float = 5f, duration: Long = 60L) {
        mainContentLayout.animate().cancel()
        mainContentLayout.translationX = 0f
        mainContentLayout.translationY = 0f

        ObjectAnimator.ofFloat(mainContentLayout, View.TRANSLATION_X, 0f, intensity, -intensity, intensity * 0.7f, -intensity * 0.7f, 0f).apply {
            this.duration = duration
            interpolator = CycleInterpolator(1.5f)
            start()
        }
        ObjectAnimator.ofFloat(mainContentLayout, View.TRANSLATION_Y, 0f, intensity * 0.6f, -intensity * 0.6f, 0f).apply {
            this.duration = duration
            interpolator = CycleInterpolator(1f)
            start()
        }
    }
}