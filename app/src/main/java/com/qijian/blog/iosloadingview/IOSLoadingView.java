package com.qijian.blog.iosloadingview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;


/**
 * Created by jaelyn on 2018/1/11.
 */

public class IOSLoadingView extends android.support.v7.widget.AppCompatImageView implements View.OnClickListener{
    public static final int STATE_LOADING = 0;
    private final int STATE_LOADING_STOP = 1;
    private final int STATE_NORMAL = 2;
    private final int STATE_ANIM = 3;
    private final int STATE_SUCCESS = 4;
    private int state = STATE_NORMAL;

    private Path xFerPath = new Path();
    private Path roundRectPath = new Path();
    private Path circlePath = new Path();
    private Path stopPath = new Path();
    private Path stopCirclePath = new Path();

    private Paint xFerPaint = new Paint();
    private Paint paint = new Paint();

    private float outRaduis = 130;
    private float outRaduisCurr = outRaduis;
    private float inRaduis = 100;
    private float stopRaduis = 80;
    private float stopRaduisCurr = stopRaduis * 0.7f;
    private float roundRaduis = 40;

    private int halfW;
    private int halfH;

    private RectF inRect = new RectF();
    private RectF stopRect = new RectF();
    private RectF stopLineRectLeft = new RectF();
    private RectF stopLineRectRight = new RectF();

    private float degress = 0;

    private OnClickListener onClickListener;
    private Path outCirclePath = new Path();
    private RectF rectF = new RectF();

    public IOSLoadingView(Context context) {
        this(context, null);
    }

    public IOSLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IOSLoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        intiView();
    }

    private void intiView() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(200, 0, 0, 0));
        paint.setStyle(Paint.Style.FILL);

        xFerPaint = new Paint();
        xFerPaint.setAntiAlias(true);
        xFerPaint.setColor(Color.WHITE);
        xFerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        xFerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        super.setOnClickListener(this);
    }

    /**
     * 绘制掏空了 圆形的圆角矩形
     */
    private void resetOutRoundRect() {
        outCirclePath.reset();
        outCirclePath.addCircle(getWidth() / 2, getHeight() / 2, outRaduisCurr, Path.Direction.CW);

        roundRectPath.reset();
        roundRectPath.addRoundRect(rectF, roundRaduis, roundRaduis, Path.Direction.CW);
        roundRectPath.op(outCirclePath, Path.Op.DIFFERENCE);
    }

    private void initXFerPath(){
        xFerPath.reset();
        xFerPath.addRoundRect(rectF, roundRaduis, roundRaduis, Path.Direction.CW);
    }

    private void
    resetStopPath() {
        stopPath.reset();
        stopPath.addRect(stopLineRectLeft, Path.Direction.CW);
        stopPath.addRect(stopLineRectRight, Path.Direction.CW);
    }

    private void initRect() {
        inRect = getRectByCenterWH(halfW, halfH, inRaduis, inRaduis);
        stopRect = getRectByCenterWH(halfW, halfH, stopRaduis, stopRaduis);
        rectF.set(0, 0, getWidth(), getHeight());

        float centerDX = inRaduis / 4;
        float h = inRaduis / 3;
        float w = h / 3f;
        stopLineRectLeft = getRectByCenterWH(halfW - centerDX, halfH, w, h);
        stopLineRectRight = getRectByCenterWH(halfW + centerDX, halfH, w, h);
    }

    private RectF getRectByCenterWH(float centerX, float centerY, float halfW, float halfH) {
        RectF rectF = new RectF(centerX - halfW, centerY - halfH, centerX + halfW, centerY + halfH);
        return rectF;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        halfW = w / 2;
        halfH = h / 2;
        outRaduis = halfW / 3f * 2;
        outRaduisCurr = outRaduis;
        inRaduis = outRaduis / 6 * 5;
        stopRaduis = inRaduis / 6 * 5;
        stopRaduisCurr = stopRaduis * 0.7f;
        roundRaduis = w / 2 / 4f;

        initRect();
        initXFerPath();
        resetOutRoundRect();
        resetStopPath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (state) {
            case STATE_NORMAL:
                //新建图层
                int count1 = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
                //绘制imageview的内容
                super.onDraw(canvas);
                //裁切成圆角图层
                canvas.drawPath(xFerPath, xFerPaint);
                canvas.restoreToCount(count1);
                break;
            case STATE_LOADING_STOP:
            case STATE_ANIM:
            case STATE_LOADING:
                int count2 = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
                super.onDraw(canvas);
                paint.setColor(Color.argb(100, 255, 255, 255));
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
                paint.setColor(Color.argb(200, 0, 0, 0));
                canvas.drawPath(xFerPath, xFerPaint);
                canvas.restoreToCount(count2);
                //绘制灰色掏空圆心的圆角矩形
                canvas.drawPath(roundRectPath, paint);
                //绘制中间的扇形，以及停止符号
                resetCircleStopPath();
                canvas.drawPath(circlePath, paint);
                break;
            case STATE_SUCCESS:
                int count3 = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
                super.onDraw(canvas);
                paint.setColor(Color.argb(100, 255, 255, 255));
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
                paint.setColor(Color.argb(200, 0, 0, 0));
                canvas.drawPath(xFerPath, xFerPaint);
                canvas.restoreToCount(count3);

                resetOutRoundRect();
                canvas.drawPath(roundRectPath, paint);
                break;
        }
    }

    private void resetCircleStopPath() {
        circlePath.reset();
        circlePath.moveTo(halfW, halfH);
        circlePath.addArc(inRect, degress - 90, 360 - degress);
        circlePath.lineTo(halfW, halfH);
        if (state == STATE_LOADING_STOP | state == STATE_ANIM) {
            resetStopPath();
            /*if (state == STATE_ANIM) {
                stopCirclePath.reset();
                stopCirclePath.addCircle(halfW, halfH, stopRaduisCurr, Path.Direction.CW);
                stopPath.op(stopCirclePath, Path.Op.INTERSECT);
            }*/
            stopRect.set(halfW - stopRaduisCurr, halfH - stopRaduisCurr, halfW + stopRaduisCurr, halfH + stopRaduisCurr);
            circlePath.addArc(stopRect, -90, degress);
            circlePath.lineTo(halfW, halfH);
            circlePath.op(stopPath, Path.Op.DIFFERENCE);
        }
    }

    public void setProgress(float percent, boolean anim){
        if (state == STATE_LOADING){
            degress =  360 * percent;
            if (degress >= 360){
                if (anim){
                    state = STATE_SUCCESS;
                    starLoadSuccessAnim();
                }else {
                    state = STATE_NORMAL;
                }
            }
            postInvalidate();
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.onClickListener = l;
    }

    @Override
    public void onClick(View view) {
        if (state == STATE_LOADING){
            state = STATE_ANIM;
            starAnim(true);
        }else if (state == STATE_LOADING_STOP){
            state = STATE_ANIM;
            starAnim(false);
        }else if (state == STATE_NORMAL) {
            onClickListener.onClick(view);
        }
    }

    public void setState(int state){
        if (this.state == state){
            return;
        }
        if (this.state == STATE_NORMAL && state == STATE_LOADING){
            //开始下载
            this.state = state;
            postInvalidate();
        }
    }

    private float currPercent = 0;
    private ValueAnimator anim;

    private void starAnim(boolean isShow){
        if (anim != null && anim.isRunning()){
            anim.cancel();
        }
        float from = currPercent;
        float to = 1;
        if (!isShow){
            from = currPercent;
            to = 0;
        }
        anim = ValueAnimator.ofFloat(from, to);
        anim.setDuration(400);
        anim.setInterpolator(new LinearInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Log.d("onAnimationUpdate--?", "----");
                currPercent = (float) valueAnimator.getAnimatedValue();
                stopRaduisCurr = stopRaduis * currPercent;
                postInvalidate();
            }
        });
        final float finalTo = to;
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                stopRaduisCurr = stopRaduis;
                if (finalTo == 1){
                    state = STATE_LOADING_STOP;
                }else {
                    state = STATE_LOADING;
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        anim.start();
    }

    private ValueAnimator succAnim;
    private void starLoadSuccessAnim(){
        if (succAnim != null && succAnim.isRunning()){
            return;
        }
        succAnim = ValueAnimator.ofFloat(0, 1.5f);
        succAnim.setInterpolator(new LinearInterpolator());
        succAnim.setDuration(400);
        succAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float percent = (float) valueAnimator.getAnimatedValue();
                outRaduisCurr = percent * (halfW - outRaduis) + outRaduis;
                postInvalidate();
            }
        });
        succAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                state = STATE_NORMAL;
                outRaduisCurr = outRaduis;
                resetOutRoundRect();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        succAnim.start();
    }
}
