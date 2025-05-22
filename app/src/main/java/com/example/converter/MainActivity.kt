package com.example.converter

import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.style.MetricAffectingSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.DecimalFormat
import kotlin.math.roundToInt
import kotlin.math.pow

data class SeekBarRange(val min: Double, val max: Double)

data class ConverterConfig(
    val inputEditText: EditText,
    val inputUnitTextView: TextView,
    val inputSeekBar: SeekBar,
    val resultEditText: EditText,
    val resultUnitTextView: TextView,
    val titleTextView: TextView,
    val euUnitName: String,
    val usUnitName: String,
    val euToUsConversionFactor: Double,
    val euMinInput: Double,
    val euMaxInput: Double,
    val usMinInput: Double,
    val usMaxInput: Double,
    val seekBarInternalMax: Int = 100
) {
    fun getFromUnitName(isUSMode: Boolean): String = if (isUSMode) usUnitName else euUnitName
    fun getToUnitName(isUSMode: Boolean): String = if (isUSMode) euUnitName else usUnitName
    fun getConversionFactor(isUSMode: Boolean): Double = if (isUSMode) 1.0 / euToUsConversionFactor else euToUsConversionFactor
    fun getMinInput(isUSMode: Boolean): Double = if (isUSMode) usMinInput else euMinInput
    fun getMaxInput(isUSMode: Boolean): Double = if (isUSMode) usMaxInput else euMaxInput
}

class MainActivity : AppCompatActivity() {

    private val decimalFormat = DecimalFormat("#")
    
    // Extension function for safe text selection
    private fun EditText.setSelectionSafe(position: Int) {
        val safePosition = position.coerceIn(0, text.length)
        setSelection(safePosition)
    }
    private var isUpdatingSeekBarFromEditTextByCode = false
    private var isUpdatingEditTextFromSeekBarByCode = false
    private var isUpdatingResultFromInputByCode = false
    private var isUpdatingInputFromResultByCode = false


    private lateinit var mainContentLayout: View
    private lateinit var regionToggle: SwitchMaterial
    private lateinit var flagEU: ImageView
    private lateinit var flagUSA: ImageView
    private var isUSMode = false // false = EU mode, true = US mode
    private val converters = mutableListOf<ConverterConfig>()
    
    // Category views
    private lateinit var weightCategory: View
    private lateinit var lengthCategory: View
    private lateinit var volumeCategory: View
    
    // Category buttons
    private lateinit var btnWeightCategory: View
    private lateinit var btnLengthCategory: View
    private lateinit var btnVolumeCategory: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        mainContentLayout = findViewById(R.id.mainContentLayout)
        regionToggle = findViewById(R.id.regionToggle)
        flagEU = findViewById(R.id.flagEU)
        flagUSA = findViewById(R.id.flagUSA)
        
        // Set responsive max width for content
        // Note: LinearLayout doesn't support maxWidth directly, would need ConstraintLayout for this feature
        
        // Initialize category views
        weightCategory = findViewById(R.id.weightCategory)
        lengthCategory = findViewById(R.id.lengthCategory)
        volumeCategory = findViewById(R.id.volumeCategory)
        
        // Initialize category buttons
        btnWeightCategory = findViewById(R.id.btnWeightCategory)
        btnLengthCategory = findViewById(R.id.btnLengthCategory)
        btnVolumeCategory = findViewById(R.id.btnVolumeCategory)
        
        // Set up category button click listeners
        btnWeightCategory.setOnClickListener { showCategory("weight") }
        btnLengthCategory.setOnClickListener { showCategory("length") }
        btnVolumeCategory.setOnClickListener { showCategory("volume") }
        
        // Show length category by default
        showCategory("length")
        
        regionToggle.setOnCheckedChangeListener { _, isChecked ->
            isUSMode = isChecked
            updateAllConverters()
        }

        // Weight converter
        val weightConfig = ConverterConfig(
            findViewById(R.id.editTextKilos), 
            findViewById(R.id.textViewKilosUnit),
            findViewById(R.id.seekBarKilos), 
            findViewById(R.id.textViewResultPounds),
            findViewById(R.id.textViewPoundsUnit),
            findViewById(R.id.textViewKgLbTitle),
            getString(R.string.unit_kg), getString(R.string.unit_lb), 2.20462, 0.0, 50000.0, 0.0, 110000.0
        )
        converters.add(weightConfig)
        setupConverter(weightConfig)
        
        // Length converter
        val lengthConfig = ConverterConfig(
            findViewById(R.id.editTextCm), 
            findViewById(R.id.textViewCmUnit),
            findViewById(R.id.seekBarCm), 
            findViewById(R.id.textViewResultInches),
            findViewById(R.id.textViewInchesUnit),
            findViewById(R.id.textViewCmInTitle),
            getString(R.string.unit_cm), getString(R.string.unit_in), 0.393701, 0.0, 100000.0, 0.0, 40000.0
        )
        converters.add(lengthConfig)
        setupConverter(lengthConfig)
        
        // Volume converter
        val volumeConfig = ConverterConfig(
            findViewById(R.id.editTextLiters), 
            findViewById(R.id.textViewLitersUnit),
            findViewById(R.id.seekBarLiters), 
            findViewById(R.id.textViewResultGallons),
            findViewById(R.id.textViewGallonsUnit),
            findViewById(R.id.textViewLGalTitle),
            getString(R.string.unit_l), getString(R.string.unit_gal), 0.264172, 0.0, 10000.0, 0.0, 37500.0
        )
        converters.add(volumeConfig)
        setupConverter(volumeConfig)
        
        // Distance converter
        val distanceConfig = ConverterConfig(
            findViewById(R.id.editTextMeters), 
            findViewById(R.id.textViewMetersUnit),
            findViewById(R.id.seekBarMeters), 
            findViewById(R.id.textViewResultFeet),
            findViewById(R.id.textViewFeetUnit),
            findViewById(R.id.textViewMFtTitle),
            getString(R.string.unit_m), getString(R.string.unit_ft), 3.28084, 0.0, 50000.0, 0.0, 164000.0
        )
        converters.add(distanceConfig)
        setupConverter(distanceConfig)
        
        // Grams/Ounces converter
        val gramsOuncesConfig = ConverterConfig(
            findViewById(R.id.editTextGrams), 
            findViewById(R.id.textViewGramsUnit),
            findViewById(R.id.seekBarGrams), 
            findViewById(R.id.textViewResultOunces),
            findViewById(R.id.textViewOuncesUnit),
            findViewById(R.id.textViewGOzTitle),
            getString(R.string.unit_g), getString(R.string.unit_oz), 0.035274, 0.0, 100000.0, 0.0, 3500.0
        )
        converters.add(gramsOuncesConfig)
        setupConverter(gramsOuncesConfig)
        
        // Meters/Yards converter
        val metersYardsConfig = ConverterConfig(
            findViewById(R.id.editTextMetersYd), 
            findViewById(R.id.textViewMetersYdUnit),
            findViewById(R.id.seekBarMetersYd), 
            findViewById(R.id.textViewResultYards),
            findViewById(R.id.textViewYardsUnit),
            findViewById(R.id.textViewYdMTitle),
            getString(R.string.unit_m), getString(R.string.unit_yd), 1.09361, 0.0, 50000.0, 0.0, 55000.0
        )
        converters.add(metersYardsConfig)
        setupConverter(metersYardsConfig)
        
        // MPG/L per 100km converter
        val mpgL100kmConfig = ConverterConfig(
            findViewById(R.id.editTextMpg), 
            findViewById(R.id.textViewMpgUnit),
            findViewById(R.id.seekBarMpg), 
            findViewById(R.id.textViewResultL100km),
            findViewById(R.id.textViewL100kmUnit),
            findViewById(R.id.textViewMpgL100kmTitle),
            getString(R.string.unit_mpg), getString(R.string.unit_l100km), 1.0, 0.0, 200.0, 0.0, 80.0
        )
        converters.add(mpgL100kmConfig)
        setupConverter(mpgL100kmConfig)
        
        // Apply styled titles to all converters
        applyStyledTitles()
    }
    
    private fun applyStyledTitles() {
        // Style the titles with raised arrows
        findViewById<TextView>(R.id.textViewKgLbTitle)?.text = createStyledTitle(
            getString(R.string.unit_name_kilograms), 
            getString(R.string.unit_name_pounds)
        )
        findViewById<TextView>(R.id.textViewGOzTitle)?.text = createStyledTitle(
            getString(R.string.unit_name_grams), 
            getString(R.string.unit_name_ounces)
        )
        findViewById<TextView>(R.id.textViewCmInTitle)?.text = createStyledTitle(
            getString(R.string.unit_name_centimeters), 
            getString(R.string.unit_name_inches)
        )
        findViewById<TextView>(R.id.textViewYdMTitle)?.text = createStyledTitle(
            getString(R.string.unit_name_meters), 
            getString(R.string.unit_name_yards)
        )
        findViewById<TextView>(R.id.textViewMFtTitle)?.text = createStyledTitle(
            getString(R.string.unit_name_meters), 
            getString(R.string.unit_name_feet)
        )
        findViewById<TextView>(R.id.textViewLGalTitle)?.text = createStyledTitle(
            getString(R.string.unit_name_liters), 
            getString(R.string.unit_name_gallons)
        )
        findViewById<TextView>(R.id.textViewMpgL100kmTitle)?.text = createStyledTitle(
            getString(R.string.unit_name_mpg), 
            getString(R.string.unit_name_l100km)
        )
    }
    
    private fun createStyledTitle(unit1: String, unit2: String): SpannableString {
        val arrow = " ↔ "
        val fullText = "$unit1$arrow$unit2"
        val spannable = SpannableString(fullText)
        
        // Find the arrow position
        val arrowStart = unit1.length
        val arrowEnd = arrowStart + arrow.length
        
        // Apply custom baseline shift to move arrow up slightly
        spannable.setSpan(
            BaselineShiftSpan(-0.156f), // Negative value moves up, about 2.8px for 18sp text
            arrowStart,
            arrowEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        return spannable
    }
    
    // Custom span to shift baseline
    private class BaselineShiftSpan(private val shiftPercentage: Float) : MetricAffectingSpan() {
        override fun updateMeasureState(textPaint: TextPaint) {
            textPaint.baselineShift = (textPaint.textSize * shiftPercentage).toInt()
        }
        
        override fun updateDrawState(textPaint: TextPaint) {
            textPaint.baselineShift = (textPaint.textSize * shiftPercentage).toInt()
        }
    }
    
    private fun updateAllConverters() {
        converters.forEach { config ->
            // Get current value in the previous unit system
            val currentText = config.inputEditText.text.toString()
            val currentValue = currentText.toDoubleOrNull() ?: 0.0
            
            // Convert the value to the new unit system
            val convertedValue = if (config.euUnitName == getString(R.string.unit_mpg) || config.usUnitName == getString(R.string.unit_mpg)) {
                // MPG/L per 100km doesn't need conversion when switching regions, just swap the units
                currentValue
            } else {
                if (isUSMode) {
                    // Converting from EU to US
                    currentValue * config.euToUsConversionFactor
                } else {
                    // Converting from US to EU
                    currentValue / config.euToUsConversionFactor
                }
            }
            
            updateConverterUI(config)
            
            // Set the converted value
            val newText = decimalFormat.format(convertedValue)
            config.inputEditText.setText(newText)
            config.inputEditText.setSelectionSafe(newText.length)
            
            // Update SeekBar range and position
            config.inputSeekBar.tag = SeekBarRange(config.getMinInput(isUSMode), config.getMaxInput(isUSMode))
            val range = config.inputSeekBar.tag as SeekBarRange
            val (absMin, absMax) = range.min to range.max
            val progress = if (absMax - absMin != 0.0) {
                // Reverse the exponential scaling to get the correct slider position
                val normalizedValue = (convertedValue - absMin) / (absMax - absMin)
                val scalingPower = 2.5 // Must match the power used in onProgressChanged
                val normalizedProgress = normalizedValue.pow(1.0 / scalingPower)
                (normalizedProgress * config.inputSeekBar.max).roundToInt()
            } else 0
            config.inputSeekBar.progress = progress.coerceIn(0, config.inputSeekBar.max)
            
            updateConversionResult(config, convertedValue)
        }
    }
    
    private fun updateConverterUI(config: ConverterConfig) {
        // Update unit labels
        config.inputUnitTextView.text = config.getFromUnitName(isUSMode)
        config.resultUnitTextView.text = config.getToUnitName(isUSMode)
        
        // Update hints to just show "0"
        config.inputEditText.hint = getString(R.string.hint_zero)
    }

    private fun setupConverter(config: ConverterConfig) {
        // Initial setup
        updateConverterUI(config)
        
        val initialValue = config.getMinInput(isUSMode)
        val initialText = decimalFormat.format(initialValue)
        config.inputEditText.setText(initialText)
        config.inputEditText.setSelectionSafe(initialText.length)

        config.inputSeekBar.max = config.seekBarInternalMax
        config.inputSeekBar.progress = 0
        config.inputSeekBar.tag = SeekBarRange(config.getMinInput(isUSMode), config.getMaxInput(isUSMode))

        config.inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingEditTextFromSeekBarByCode) return
                isUpdatingSeekBarFromEditTextByCode = true

                val rawValue = s.toString().toDoubleOrNull() ?: config.getMinInput(isUSMode)
                // Round to nearest integer
                val value = rawValue.roundToInt().toDouble()

                val range = config.inputSeekBar.tag as SeekBarRange
                val (absMin, absMax) = range.min to range.max
                var clampedValue = value.coerceIn(absMin, absMax)

                val originalInputValue = s.toString().toDoubleOrNull()
                var textNeedsUpdate = false

                if (originalInputValue != null && originalInputValue != clampedValue) {
                    textNeedsUpdate = true
                } else if (s.toString().isNotEmpty() && originalInputValue == null) {
                    clampedValue = absMin
                    textNeedsUpdate = true
                }

                if (textNeedsUpdate) {
                    val newText = decimalFormat.format(clampedValue)
                    config.inputEditText.setText(newText)
                    config.inputEditText.setSelectionSafe(newText.length)
                }

                val progress = if (absMax - absMin != 0.0) {
                    // Reverse the exponential scaling to get the correct slider position
                    val normalizedValue = (clampedValue - absMin) / (absMax - absMin)
                    val scalingPower = 4.0 // Must match the power used in onProgressChanged
                    val normalizedProgress = normalizedValue.pow(1.0 / scalingPower)
                    (normalizedProgress * config.inputSeekBar.max).roundToInt()
                } else 0
                config.inputSeekBar.progress = progress.coerceIn(0, config.inputSeekBar.max)
                updateConversionResult(config, clampedValue)

                // Screen shake removed for cleaner UI
                isUpdatingSeekBarFromEditTextByCode = false
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val currentSeekBar = config.inputSeekBar
        currentSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (isUpdatingSeekBarFromEditTextByCode || !fromUser) return
                isUpdatingEditTextFromSeekBarByCode = true

                val range = seekBar.tag as SeekBarRange
                val (absMin, absMax) = range.min to range.max
                val valueRange = absMax - absMin
                val actualValue = if (seekBar.max > 0) {
                    // Use exponential scaling for progressive value increase
                    val normalizedProgress = progress.toDouble() / seekBar.max
                    val scalingPower = 4.0 // Adjust this value to control the curve (higher = more exponential)
                    val scaledProgress = normalizedProgress.pow(scalingPower)
                    val rawValue = absMin + scaledProgress * valueRange
                    // Round to nearest integer
                    rawValue.roundToInt().toDouble()
                } else {
                    absMin.roundToInt().toDouble()
                }

                val textForEditText = decimalFormat.format(actualValue)
                config.inputEditText.setText(textForEditText)
                config.inputEditText.setSelectionSafe(textForEditText.length)

                updateConversionResult(config, actualValue)

                // Visual effects removed for cleaner UI
                isUpdatingEditTextFromSeekBarByCode = false
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Visual effects removed for cleaner UI
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Animation removed for cleaner UI
            }
        })
        
        // Add TextWatcher to result EditText for reverse conversion
        config.resultEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingResultFromInputByCode || isUpdatingEditTextFromSeekBarByCode) return
                isUpdatingInputFromResultByCode = true
                
                val rawResultValue = s.toString().toDoubleOrNull() ?: 0.0
                // Round to nearest integer
                val resultValue = rawResultValue.roundToInt().toDouble()
                
                // Convert back to input value (reverse conversion)
                val rawInputValue = if (config.euUnitName == getString(R.string.unit_mpg) || config.usUnitName == getString(R.string.unit_mpg)) {
                    // Special handling for MPG/L per 100km conversion (inverse relationship)
                    if (resultValue != 0.0) {
                        235.214583 / resultValue
                    } else {
                        0.0
                    }
                } else {
                    resultValue / config.getConversionFactor(isUSMode)
                }
                // Round to nearest integer
                val inputValue = rawInputValue.roundToInt().toDouble()
                
                // Update input EditText
                val inputText = decimalFormat.format(inputValue)
                config.inputEditText.setText(inputText)
                config.inputEditText.setSelectionSafe(inputText.length)
                
                // Update SeekBar
                val range = config.inputSeekBar.tag as SeekBarRange
                val (absMin, absMax) = range.min to range.max
                val clampedValue = inputValue.coerceIn(absMin, absMax)
                val progress = if (absMax - absMin != 0.0) {
                    // Reverse the exponential scaling to get the correct slider position
                    val normalizedValue = (clampedValue - absMin) / (absMax - absMin)
                    val scalingPower = 4.0 // Must match the power used in onProgressChanged
                    val normalizedProgress = normalizedValue.pow(1.0 / scalingPower)
                    (normalizedProgress * config.inputSeekBar.max).roundToInt()
                } else 0
                config.inputSeekBar.progress = progress.coerceIn(0, config.inputSeekBar.max)
                
                isUpdatingInputFromResultByCode = false
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        updateConversionResult(config, config.getMinInput(isUSMode))
    }

    private fun updateConversionResult(config: ConverterConfig, inputValue: Double) {
        isUpdatingResultFromInputByCode = true
        val rawResultValue = if (config.euUnitName == getString(R.string.unit_mpg) || config.usUnitName == getString(R.string.unit_mpg)) {
            // Special handling for MPG/L per 100km conversion (inverse relationship)
            if (inputValue != 0.0) {
                if (isUSMode) {
                    // US mode: L/100km to MPG
                    235.214583 / inputValue
                } else {
                    // EU mode: MPG to L/100km
                    235.214583 / inputValue
                }
            } else {
                0.0
            }
        } else {
            inputValue * config.getConversionFactor(isUSMode)
        }
        // Round to nearest integer
        val resultValue = rawResultValue.roundToInt().toDouble()
        val resultText = decimalFormat.format(resultValue)
        config.resultEditText.setText(resultText)
        config.resultEditText.setSelectionSafe(resultText.length)
        isUpdatingResultFromInputByCode = false
    }

    
    private fun showCategory(category: String) {
        // Hide all categories
        weightCategory.visibility = View.GONE
        lengthCategory.visibility = View.GONE
        volumeCategory.visibility = View.GONE
        
        // Update button colors to show inactive state
        btnWeightCategory.alpha = 0.5f
        btnLengthCategory.alpha = 0.5f
        btnVolumeCategory.alpha = 0.5f
        
        // Show selected category and highlight button
        when (category) {
            "weight" -> {
                weightCategory.visibility = View.VISIBLE
                btnWeightCategory.alpha = 1.0f
            }
            "length" -> {
                lengthCategory.visibility = View.VISIBLE
                btnLengthCategory.alpha = 1.0f
            }
            "volume" -> {
                volumeCategory.visibility = View.VISIBLE
                btnVolumeCategory.alpha = 1.0f
            }
        }
    }


}