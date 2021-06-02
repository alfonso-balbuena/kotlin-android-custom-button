package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.StaticLayout
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var _background = 0
    private var _textColor = 0
    private var _circleColor = 0
    private var _animationColor = 0
    private var _percentageWidthAnimation = 0.0f
    private var _sweepAngle = 0.0f
    private val boundText = Rect()
    private var centerHeight = 0.0f
    private var centerWidth = 0.0f


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
        typeface = Typeface.create("",Typeface.NORMAL)
    }

    private val valueAnimatorBackground = ValueAnimator.ofFloat(0.0f,1.0f).apply {
        duration = 2000
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        addUpdateListener { updateValue ->
            _percentageWidthAnimation = updateValue.animatedValue as Float
            invalidate()
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator?) {
                _percentageWidthAnimation = 0.0f;
                buttonState = ButtonState.Completed
                invalidate()
            }
        })
    }

    private val valueAnimatorCircle = ValueAnimator.ofFloat(0.0f,360.0f).apply {
        duration = 2000
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        addUpdateListener {
            _sweepAngle = it.animatedValue as Float
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator?) {
                _sweepAngle = 0.0f
            }
        })
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when(new) {
            ButtonState.Clicked -> {
                valueAnimatorCircle.start()
                valueAnimatorBackground.start()
                buttonState = ButtonState.Loading
            }
            ButtonState.Loading -> {
                this.isEnabled = false
            }
            ButtonState.Completed -> {
                this.isEnabled = true

            }
        }
    }


    init {
        isClickable = true
        context.withStyledAttributes(attrs,R.styleable.LoadingButton) {
            _background = getColor(R.styleable.LoadingButton_backgroundColor,0)
            _textColor = getColor(R.styleable.LoadingButton_textColor,0)
            _circleColor = getColor(R.styleable.LoadingButton_circleColor,0)
            _animationColor = getColor(R.styleable.LoadingButton_animationColor,0)
        }
    }

    override fun performClick(): Boolean {
        if(buttonState != ButtonState.Clicked)
            buttonState = ButtonState.Clicked
        super.performClick()
        return true
    }

    fun stop() {
        valueAnimatorCircle.cancel()
        valueAnimatorBackground.cancel()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = _background
        canvas?.drawRect(0.0f,0.0f, widthSize.toFloat(), heightSize.toFloat(),paint)
        paint.color = _animationColor
        canvas?.drawRect(0.0f,0.0f,widthSize.toFloat() * _percentageWidthAnimation,heightSize.toFloat(),paint)
        val text = if (buttonState == ButtonState.Loading) context.getString(R.string.loading_text) else context.getString(R.string.download)
        paint.getTextBounds(text,0,text.length,boundText)
        paint.color = _textColor
        canvas?.drawText(text, centerWidth, centerHeight + (boundText.height()/2),paint)
        paint.color = _circleColor
        canvas?.drawArc(getOval(boundText.width().toFloat(),centerWidth,centerHeight),180.0f,_sweepAngle,true,paint)

    }

    private fun getOval(widthText : Float,centerX : Float, centerY : Float) : RectF {
        val oval = RectF()
        val halfWidthText = widthText / 2
        val radio = boundText.height().toFloat() / 2
        oval.left = centerX + halfWidthText + 10
        oval.top = centerY - radio
        oval.bottom = centerY + radio
        oval.right = centerX + halfWidthText + radio * 2 + 10
        return oval
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        centerHeight = (heightSize/2).toFloat()
        centerWidth = (widthSize/2).toFloat()
        setMeasuredDimension(w, h)
    }

}