package navas.com.slidingpiewidget.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import navas.com.slidingpiewidget.R;


public class SplitPieView extends View {

    private Paint mPaintArc;
    private Paint mPaintPointerCircle;
    private Paint mPaintSelectedArc;
    private Paint mPaintOuterCircle;
    private Paint mPaintText;
    private Paint centreTextPaint;
    private Paint mPaintInsideCircle;
    private Paint mPaintSubscriptText;
    private RectF mOval, mOval2;
    private float centerX, centerY;
    private float left, top, right, bottom;
    private Paint mPaintLine;
    private float radius;

    private final int[] arcsStatus = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};// This is the status of arcs, 0 for unselected(or not created may be, if number of arcs is less than 10), 1 for selected.
    private final String[] subsciptText = {"Section 1", "Section 2", "Section 3", "Section 4", "Section 5", "Section 6", "Section 7", "Section 8", "Section 9", "Section 10"};
    private final int[] pieColors = {R.color.orange, R.color.red, R.color.orange, R.color.red, R.color.orange, R.color.red, R.color.orange, R.color.red, R.color.orange, R.color.red};

    private float pAngle = 0;
    private final float[] to = new float[9];
    private float tempTo = 0;

    private static final int ARC_SEPARATION_VALUE = 17; //This is the minimum angle needed between adjacent arcs

    private double totalValue;

    int activeLine = 0;
    int activeArc = 0;

    private int numberOfLines = 1;

    private final float[] arcsValue = new float[10];
    private final float[] pointLeft = new float[9];
    private final float[] pointTop = new float[9];
    private final double[] amountValueList = new double[10];


    private int touchedLine = -1;
    private boolean isTouched = false;
    private boolean isMovementStopped = false;// variable to stop drawing if line has been stuck in isInMovingArea method

    private Bitmap bitmapNonSelectedIcon;
    private Bitmap bitmapSelectedIcon;
    private float bitmapWidth;
    private float bitmapHeight;

    private int scaledSize;
    private int scaledSize12;
    private int scaledSize10;

    private double totalSelectedAmount = 0;
    private NumberFormat nf;

    final float scale = getContext().getResources().getDisplayMetrics().density;
    float radiusForSlider;

    //configurable values
    private boolean isIconNeededForPie;
    private boolean isPercentageNeeded = true; // this is set to true by default
    private boolean showTotalSelectedValue;

    private float pointLeftStart, pointTopStart;

    public SplitPieView(Context context) {
        super(context);
        init(null);
    }

    public SplitPieView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SplitPieView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    public void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SplitPieView);
        int totalValueSet = ta.getInt(R.styleable.SplitPieView_total_value, 100);
        ta.recycle();
        totalValue = totalValueSet;

        nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(false);

        radiusForSlider = (10 * scale + 0.5f);


        to[0] = 180;
        to[1] = 180;
        calculateValues();
        calculateSelectedAmount();

        bitmapNonSelectedIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icn_add_person);
        bitmapSelectedIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icn_add_person_blk);

        mPaintArc = new Paint();
        mPaintArc.setAntiAlias(true);
        mPaintArc.setStyle(Paint.Style.FILL);
        mPaintArc.setDither(true);
        mPaintArc.setColor(Color.parseColor("#565656"));
        int color = ContextCompat.getColor(getContext(), pieColors[0]);
        mPaintArc.setColor(color);

        mPaintPointerCircle = new Paint();
        mPaintPointerCircle.setAntiAlias(true);
        mPaintPointerCircle.setColor(Color.parseColor("#ffffff"));

        mPaintSelectedArc = new Paint();
        mPaintSelectedArc.setAntiAlias(true);
        mPaintSelectedArc.setStyle(Paint.Style.FILL);
        mPaintSelectedArc.setDither(true);
        mPaintSelectedArc.setColor(Color.parseColor("#dbdddc"));

        mPaintInsideCircle = new Paint();
        mPaintInsideCircle.setAntiAlias(true);
        mPaintInsideCircle.setStyle(Paint.Style.FILL);
        mPaintInsideCircle.setDither(true);
        mPaintInsideCircle.setColor(Color.parseColor("#565656"));

        mPaintOuterCircle = new Paint();
        mPaintOuterCircle.setAntiAlias(true);
        mPaintOuterCircle.setColor(Color.parseColor("#ffffff"));

        mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintLine.setAntiAlias(true);
        mPaintLine.setColor(Color.parseColor("#ffffff"));
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setStrokeJoin(Join.ROUND);
        mPaintLine.setStrokeCap(Cap.ROUND);
        mPaintLine.setStrokeWidth(5);

        scaledSize12 = getResources().getDimensionPixelSize(R.dimen.canvasText12);
        scaledSize10 = getResources().getDimensionPixelSize(R.dimen.canvasText10);

        mPaintText = new Paint();
        mPaintText.setAntiAlias(true);
        mPaintText.setColor(Color.parseColor("#ffffff"));
        scaledSize = getResources().getDimensionPixelSize(R.dimen.canvasText);
        mPaintText.setTextSize(scaledSize);

        mPaintSubscriptText = new Paint();
        mPaintSubscriptText.setAntiAlias(true);
        mPaintSubscriptText.setColor(Color.parseColor("#ffffff"));
        mPaintSubscriptText.setTextSize(scaledSize12);


        centreTextPaint = new Paint();
        centreTextPaint.setColor(Color.parseColor("#ffffff"));
        centreTextPaint.setTextSize(scaledSize12);

        bitmapWidth = bitmapNonSelectedIcon.getWidth();
        bitmapHeight = bitmapNonSelectedIcon.getHeight();

    }

    public double getTotalSelectedAmount() {
        return totalSelectedAmount;
    }

    public void setActiveArcs(int activeArc) {
        this.activeArc = activeArc;
    }

    public void setIconNeededForPie(boolean iconNeededForPie) {
        isIconNeededForPie = iconNeededForPie;
    }

    public void setShowTotalSelectedValue(boolean showTotalSelectedValue) {
        this.showTotalSelectedValue = showTotalSelectedValue;
    }

    public void setPercentageNeeded(boolean percentageNeeded) {
        isPercentageNeeded = percentageNeeded;
    }

    /**
     * @param value - Should be between 1 and 10 inclusive.
     */
    public void setNumberOfPortions(int value) {
        numberOfLines = value - 1;
        resetPies();
    }

    public void add() {
        if (numberOfLines < 9) {
            numberOfLines++;
        } else {
            return;
        }
        resetPies();
    }

    public void remove() {
        if (numberOfLines > 1) {
            numberOfLines--;
        } else {
            return;
        }
        resetPies();
    }

    public void resetPies() {
        for (int i = 0; i < numberOfLines; i++) {
            to[i] = (360 / (numberOfLines + 1)) * (i + 1);
        }

        for (int i = 0; i <= numberOfLines; i++) {
            arcsStatus[i] = 0;
        }

        calculateValues();
        calculateSelectedAmount();
        activeArc = 0;
        activeLine = 0;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = MeasureSpec.getSize(heightMeasureSpec);

        left = 40;
        right = desiredWidth - 40;
        top = 40;
        bottom = (right - left) + top;

        radius = (right - left) / 2;
        centerX = ((right - left) / 2) + left;
        centerY = ((bottom - top) / 2) + top;
        mOval = new RectF(left, top, right, bottom);
        mOval2 = new RectF(left - 5, top - 5, right + 5, bottom + 5);

        pointLeftStart = (float) (centerX + ((right - left) / 2) * Math.cos(-90 * Math.PI / 180F));
        pointTopStart = (float) (centerY + ((right - left) / 2) * Math.sin(-90 * Math.PI / 180F));

        //MUST CALL THIS
        setMeasuredDimension(measureDimension(desiredWidth, widthMeasureSpec),
                measureDimension(desiredHeight, heightMeasureSpec));

    }

    private int measureDimension(int desiredSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = desiredSize;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }


    public double[] getAmountValueList() {
        return amountValueList;
    }

    public int[] getSelectedStatus() {
        return arcsStatus;
    }

    public int getNumberOfPortions() {
        return numberOfLines + 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(mOval2, 0, 360, true, mPaintOuterCircle);                // draw the outline border white circle


        //**************************DRAW ARCS****************************
        for (int i = numberOfLines; i >= 0; i--) {
            // for first arc
            if (i == 0) {
                if (arcsStatus[i] == 1) {
                    canvas.drawArc(mOval, 0 - 90, arcsValue[i], true, mPaintSelectedArc);
                    drawInCenter(canvas, formatDisplayAmountString("" + amountValueList[i]), 0, 0 + arcsValue[i], true, i);
                } else if (arcsStatus[i] == 0) {
                    int color = ContextCompat.getColor(getContext(), pieColors[i]);
                    mPaintArc.setColor(color);
                    canvas.drawArc(mOval, 0 - 90, arcsValue[i], true, mPaintArc);
                    drawInCenter(canvas, formatDisplayAmountString("" + amountValueList[i]), 0, 0 + arcsValue[i], false, i);
                }

            }

            // for other arcs
            else {
                if (arcsStatus[i] == 1) {
                    canvas.drawArc(mOval, to[i - 1] - 90, arcsValue[i], true, mPaintSelectedArc);
                    drawInCenter(canvas, formatDisplayAmountString("" + amountValueList[i]), to[i - 1], to[i - 1] + arcsValue[i], true, i);
                } else if (arcsStatus[i] == 0) {
                    int color = ContextCompat.getColor(getContext(), pieColors[i]);
                    mPaintArc.setColor(color);
                    canvas.drawArc(mOval, to[i - 1] - 90, arcsValue[i], true, mPaintArc);
                    drawInCenter(canvas, formatDisplayAmountString("" + amountValueList[i]), to[i - 1], to[i - 1] + arcsValue[i], false, i);
                }
            }
        }


        //**************************DRAW LINES****************************

        // draw the line on zero which will never change
        canvas.drawLine(centerX, centerY, pointLeftStart, pointTopStart, mPaintLine);

        // draw the line to separate the arcs, active one will have a circle at the end
        for (int i = 0; i < numberOfLines; i++) {
            pointLeft[i] = (float) (centerX + ((right - left) / 2) * Math.cos((to[i] - 90) * Math.PI / 180F));
            pointTop[i] = (float) (centerY + ((right - left) / 2) * Math.sin((to[i] - 90) * Math.PI / 180F));
            canvas.drawLine(centerX, centerY, pointLeft[i], pointTop[i], mPaintLine);
            canvas.drawCircle(pointLeft[i], pointTop[i], radiusForSlider, mPaintPointerCircle);
        }


        ///***********************DRAW THE INNER CIRCLES AND VALUES*******************************
        if (totalSelectedAmount > 0 && showTotalSelectedValue) {
            // draw the border central white
            canvas.drawCircle(centerX, centerY, (float) (radius * 0.3) + 5, mPaintOuterCircle);
            // draw the central circle
            canvas.drawCircle(centerX, centerY, (float) (radius * 0.3), mPaintInsideCircle);

            int lenghtOfCenterText = formatDisplayAmountString(totalSelectedAmount + "").length();
            // draw the amount total of selected
            if (lenghtOfCenterText > 8) {
                centreTextPaint.setTextSize(scaledSize12);
            } else if (lenghtOfCenterText > 12) {
                centreTextPaint.setTextSize(scaledSize10);
            } else {
                centreTextPaint.setTextSize(scaledSize);
            }
            float textWidth = centreTextPaint.measureText(formatDisplayAmountString(totalSelectedAmount + ""));
            canvas.drawText(formatDisplayAmountString(totalSelectedAmount + ""), centerX - (textWidth / 2) - 5, centerY + 4, centreTextPaint);
        }
    }

    boolean isMoved = false;
    int moveTemp = 0; // varaible to avoid some random moves while tapping down for arc selection

    //@Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        float distance = (float) Math.sqrt(Math.pow((event.getX() - centerX), 2) + Math.pow((event.getY() - centerY), 2));
        if (distance > radius + 50) {
            return false;
        }

        float degrees = (float) ((float) ((Math.toDegrees(Math.atan2(event.getX() - centerX, centerY - event.getY())) + 360.0)) % 360.0);
        // and to make it count 0-360
        if (degrees < 0) {
            degrees += 2 * Math.PI;
        }

        pAngle = degrees;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isMovementStopped = false;
            isMoved = false;
            moveTemp = 0;

            touchedLine = findWhichArc(pAngle);
            isTouched = true;
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            moveTemp++;
            if (moveTemp < 5) {
                return false;
            }
            isMoved = true;

            if (isTouched) {
                tempTo = Math.round(degrees);

                if (isMovingInArea(touchedLine) && !isMovementStopped) {
                    if (touchedLine < 0) {
                        return false;
                    }
                    to[touchedLine] = tempTo;
                    calculateValues();
                    invalidate();
                    return true;
                } else {
                    if ((pAngle > 355)) {
                        isMovementStopped = true;
                    }
                    return false;
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            isTouched = false;
            touchedLine = -1;

            int touchedArc = getTouchedArc(pAngle);
            if (!isMoved) {
                if (arcsStatus[touchedArc] == 2) {
                    return true;
                } else if (arcsStatus[touchedArc] == 0) {
                    System.out.println("tocuhed a zero value, tocuheedarc: " + touchedArc);
                    arcsStatus[touchedArc] = 1;
                    calculateSelectedAmount();
                    invalidate();
                } else if (arcsStatus[touchedArc] == 1) {
                    arcsStatus[touchedArc] = 0;
                    calculateSelectedAmount();
                    invalidate();
                } else {
                    return true;
                }
                calculateValues();
                invalidate();
            } else {

            }
            return true;
        }

        return true;


    }


    // method to check whether the current line moved does not cross into adjacent pies.
    private boolean isMovingInArea(int arcInTouch) {

        // if there is only one line...means 2 arcs
        if (numberOfLines == 1) {
            return tempTo >= ARC_SEPARATION_VALUE && tempTo <= 360 - ARC_SEPARATION_VALUE;
        } else {

            //first line
            if (arcInTouch == 0) {
                return tempTo < to[arcInTouch + 1] - ARC_SEPARATION_VALUE && tempTo > ARC_SEPARATION_VALUE;
            }

            //last line
            else if (arcInTouch == numberOfLines - 1) {
                return tempTo < 360 - ARC_SEPARATION_VALUE && tempTo > to[arcInTouch - 1] + ARC_SEPARATION_VALUE;
            }

            //middle lines
            else {
                return tempTo < to[arcInTouch + 1] - ARC_SEPARATION_VALUE && tempTo > to[arcInTouch - 1] + ARC_SEPARATION_VALUE;
            }
        }
    }

    private void calculateValues() {

        for (int i = 0; i <= numberOfLines; i++) {
            if (i == 0) {
                arcsValue[i] = to[i];
            } else if (i == numberOfLines) {
                arcsValue[i] = 360 - to[i - 1];
            } else {
                arcsValue[i] = to[i] - to[i - 1];
            }
            System.out.println("arcsValue" + i + ": " + arcsValue[i]);
        }

        double tempTotal = 0;
        int count = 0;

        //*********** This is a level 1 calcualtion before adjusting
        for (int i = 0; i <= numberOfLines; i++) {
            System.out.println("number of lines: " + numberOfLines + "i" + i);
            String tempString = nf.format(totalValue * arcsValue[i] / 360);// this temp is for rounding the value to two digits)
            Double temp = Double.parseDouble(tempString);
            amountValueList[i] = temp;
        }


        //************ Adjusting the values to integer
        for (int i = 0; i <= numberOfLines; i++) {
            if (count == numberOfLines) {
                amountValueList[i] = totalValue - tempTotal;
            } else {
                amountValueList[i] = Math.round(amountValueList[i]);
                tempTotal += amountValueList[i];
                count++;
            }
        }

        calculateSelectedAmount();
    }

    private void calculateSelectedAmount() {
        totalSelectedAmount = 0;

        for (int i = 0; i <= numberOfLines; i++) {
            if (arcsStatus[i] == 1) {
                totalSelectedAmount += amountValueList[i];
            }
        }
    }

    private int findWhichArc(float pAngle) {
        for (int i = 0; i < numberOfLines; i++) {
            if (to[i] >= pAngle - 8 && to[i] <= pAngle + 8) {
                return i;
            }
        }
        return -1;
    }

    private int getTouchedArc(float touchedAngle) {
        for (int i = 0; i < numberOfLines; i++) {
            if (touchedAngle < to[i]) {
                return i;
            }
        }
        //this numberOflines is returned as special logic as to[i] of last arc is zero
        return numberOfLines;
    }


    private void drawInCenter(Canvas canvas, String amountValue, float startAngle, float endAngle, boolean selected, int position) {
        float centreAngle = (startAngle + endAngle) / 2;
        float radialDistance = 0;
        float pointLeft;
        float pointTop;
        float pointLeftBitmap;
        float pointTopBitmap;

        float pointLeftPosition;
        float pointTopPosition;

        if (centreAngle > 0 && centreAngle < 45) { // pie 1
            radialDistance = radius * (0.7f);

        } else if (centreAngle >= 45 && centreAngle < 90) { // pie 2
            radialDistance = radius * 1 / 2;
        } else if (centreAngle >= 90 && centreAngle < 135) {// pie 3
            radialDistance = radius * 1 / 2;
        } else if (centreAngle >= 135 && centreAngle <= 180) {// pie 4
            radialDistance = radius * (0.7f);
        } else if (centreAngle > 180 && centreAngle <= 225) {// pie 5
            radialDistance = radius * (0.7f);
        } else if (centreAngle > 225 && centreAngle <= 270) {// pie 6
            radialDistance = radius * 1 / 2;
        } else if (centreAngle > 270 && centreAngle <= 315) {// pie 7
            radialDistance = radius * 1 / 2;
        } else if (centreAngle > 315 && centreAngle < 360) {// pie 8
            radialDistance = radius * (0.7f);
        }

        pointLeftBitmap = (float) (centerX + (radialDistance) * Math.cos((centreAngle - 90) * Math.PI / 180F));
        pointTopBitmap = (float) (centerY + (radialDistance) * Math.sin((centreAngle - 90) * Math.PI / 180F));


        if (centreAngle > 0 && centreAngle < 45) {    // pie 1
            pointLeftBitmap -= 30;
            pointTopBitmap -= 30;

        } else if (centreAngle >= 45 && centreAngle < 90) {// pie 2
            pointLeftBitmap += 30;
            pointTopBitmap -= 50;

        } else if (centreAngle >= 90 && centreAngle < 135) {// pie 3
            pointLeftBitmap += 30;
            pointTopBitmap -= 30;

        } else if (centreAngle >= 135 && centreAngle <= 180) {// pie 4
            pointLeftBitmap -= 30;
            pointTopBitmap -= 30;

        } else if (centreAngle > 180 && centreAngle <= 225) {// pie 5
            pointLeftBitmap -= 30;
            pointTopBitmap -= 30;

        } else if (centreAngle > 225 && centreAngle <= 270) {// pie 6
            pointLeftBitmap -= 80;
            pointTopBitmap -= 30;


        } else if (centreAngle > 270 && centreAngle <= 315) {// pie 7
            pointLeftBitmap -= 80;
            pointTopBitmap -= 50;

        } else if (centreAngle > 315 && centreAngle < 360) {// pie 8
            pointLeftBitmap -= 30;
            pointTopBitmap -= 30;

        }

        if (selected) {
            mPaintText.setColor(Color.parseColor("#000000"));
            mPaintSubscriptText.setColor(Color.parseColor("#000000"));
        } else {
            mPaintText.setColor(Color.parseColor("#ffffff"));
            mPaintSubscriptText.setColor(Color.parseColor("#ffffff"));
        }

        if (String.valueOf(amountValue).length() > 7) {
            mPaintText.setTextSize(scaledSize12);
        } else if (String.valueOf(amountValue).length() > 9) {
            mPaintText.setTextSize(scaledSize10);
        } else {
            mPaintText.setTextSize(scaledSize);
        }

        pointLeft = pointLeftBitmap - ((mPaintText.measureText(amountValue) - bitmapWidth) / 2);
        pointTop = pointTopBitmap + bitmapHeight + (12 * scale);

        pointLeftPosition = pointLeftBitmap + (20 * scale);
        pointTopPosition = pointTopBitmap + (7 * scale);

        if (isIconNeededForPie) {
            if (selected) {
                canvas.drawBitmap(bitmapSelectedIcon, pointLeftBitmap, pointTopBitmap, mPaintArc);
            } else {
                canvas.drawBitmap(bitmapNonSelectedIcon, pointLeftBitmap, pointTopBitmap, mPaintArc);
            }
        }
        canvas.drawText(amountValue, pointLeft, pointTop, mPaintText);

        canvas.drawText(subsciptText[position] + "", pointLeft, pointTopPosition, mPaintSubscriptText);

    }

    private int getScreeWidth(Activity a) {
        Display display = a.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    private String formatDisplayAmountString(String value) {
        String finalValue = "";
        if (value != null && !value.equals("")) {
            DecimalFormat formatter = new DecimalFormat();
            formatter.setMaximumFractionDigits(0);
            formatter.setMinimumFractionDigits(0);
            formatter.setDecimalSeparatorAlwaysShown(false);
            finalValue = formatter.format(Double.parseDouble(value.replace(",", "")));
        } else {
            finalValue = "0";
        }

        return isPercentageNeeded ? finalValue + "%" : finalValue;
    }

}
