package com.cerobits.patron;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;

import androidx.core.content.ContextCompat;


import java.util.ArrayList;

public class Patron extends GridLayout {

    public static final String TAG = "Patron";

    public static final float DEFAULT_RADIUS_RATIO = 0.9f;
    public static final float DEFAULT_LINE_WIDTH = 4f; // unit: dp
    public static final float DEFAULT_SPACING = 24f; // unit: dp
    public static final int DEFAULT_ROW_COUNT = 3;
    public static final int DEFAULT_COLUMN_COUNT = 3;
    public static final int DEFAULT_ERROR_DURATION = 400; // unit: ms
    public static final float DEFAULT_HIT_AREA_PADDING_RATIO = 0.2f;
    public enum MODO {ESPERA, SIMPLE}

    private MODO Modo;

    private int lineWidth = 0;
    private int regularLineColor = 0, esperaLineColor = 0, errorLineColor = 0;

    private int spacing = 0;

    private int plvRowCount = 0;
    private int plvColumnCount = 0;

    private int errorDuration = 0;
    private float hitAreaPaddingRatio = 0f;

    private final ArrayList<Celda> cells, selectedCells;

    private final Paint linePaint = new Paint();
    private final Path linePath = new Path();

    private float lastX = 0f;
    private float lastY = 0f;

    private OnPatternListener onPatternListener = null;

    private boolean Activado = true;

    public Patron(Context context) {
        super(context);
        cells = new ArrayList<>();
        selectedCells = new ArrayList<>();
    }

    public Patron(Context context, AttributeSet attrs) {
        super(context, attrs);
        cells = new ArrayList<>();
        selectedCells = new ArrayList<>();
        CargarValores(context,attrs);

        setRowCount(plvRowCount);
        setColumnCount(plvColumnCount);

        setupCells();
        initPathPaint();
    }

    private void setupCells() {
        for (int i = 0; i < plvRowCount; i++) {
            for (int j = 0; j < plvColumnCount; j++) {
                Celda celda = new Celda(getContext(),i * plvColumnCount + j);
                int padding = spacing / 2;
                Log.d(TAG, "setupCells: El padding es " + padding);
                celda.setPadding(padding, padding, padding, padding);

                addView(celda);
                cells.add(celda);
            }
        }
    }

    private void initPathPaint() {
        linePaint.setAntiAlias(true);
        linePaint.setDither(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        linePaint.setStrokeWidth(lineWidth);
        linePaint.setColor(regularLineColor);
    }

    public interface OnPatternListener {
        void onStarted();
        void onProgress(ArrayList<Integer> ids);
        boolean onComplete(ArrayList<Integer> ids);
    }

    public void setOnPatternListener(OnPatternListener listener) {
        onPatternListener = listener;
    }

    private Celda getHitCell(int x, int y){
        for (Celda cell : cells) {
            if (isSelected(cell,x,y))
                return cell;
        }
        return null;
    }

    private boolean isSelected(View view, int x, int y){
        float innerPadding = view.getWidth() * hitAreaPaddingRatio;
        return x >= view.getLeft() + innerPadding &&
                x <= view.getRight() - innerPadding &&
                y >= view.getTop() + innerPadding &&
                y <= view.getBottom() - innerPadding;
    }


    private void handleActionMove(MotionEvent event)
    {
        Celda hitCell = getHitCell((int) event.getX(), (int) event.getY());
        if (hitCell != null) {
            if (!selectedCells.contains(hitCell)) {
                notifyCellSelected(hitCell);
            }
        }

        lastX = event.getX();
        lastY = event.getY();
        postInvalidate();
    }
    private void onFinish()
    {
        lastX = 0f;
        lastY = 0f;

        switch (Modo) {
            case ESPERA:
                for(Celda cell : selectedCells) {
                    cell.setESTADO(Celda.Estado.ESPERA);
                }
                linePaint.setColor(esperaLineColor);
                postInvalidate();
                for (Celda cell : cells) {
                    cell.postInvalidate();
                }
                Activado = false;
                if (onPatternListener != null)
                    onPatternListener.onComplete(generateSelectedIds());
                break;
            case SIMPLE:
                boolean isCorrect = true;
                if (onPatternListener != null)
                    isCorrect = onPatternListener.onComplete(generateSelectedIds());
                if (isCorrect) {
                    reset();
                } else {
                    onError();
                }
                break;
        }
    }
    public void reset()
    {
        Activado = true;
        for(Celda cell : selectedCells) {
            cell.Reset();
        }
        selectedCells.clear();
        linePaint.setColor(regularLineColor);
        linePath.reset();

        lastX = 0f;
        lastY = 0f;

        postInvalidate();
        for (Celda cell : cells) {
            cell.postInvalidate();
        }
    }

    private void onError() {
        for (Celda cell : selectedCells) {
            cell.setESTADO(Celda.Estado.ERROR);
            cell.postInvalidate();
        }
        linePaint.setColor(errorLineColor);
        postInvalidate();
        postDelayed(this::reset, errorDuration);


    }

    public void setModo(MODO modo) {
        Modo = modo;
    }

    public MODO getModo() {
        return Modo;
    }

    public ArrayList<Integer> generateSelectedIds(){
        ArrayList<Integer> result = new ArrayList<>();
        for (Celda selectedCell : selectedCells) {
            result.add(selectedCell.getIndex());
        }
        return result;
    }

    private void notifyCellSelected(Celda cell) {
        Log.d(TAG, "notifyCellSelected: Se capto a la celda " + cell.getIndex());
        selectedCells.add(cell);
        if (onPatternListener != null)
            onPatternListener.onProgress(generateSelectedIds());
        cell.setESTADO(Celda.Estado.SELECCIONADO);
        Point center = cell.getCenter();
        if (selectedCells.size() == 1)
            linePath.moveTo(center.x, center.y);
        else
             linePath.lineTo(center.x, center.y);
        //Le dice a la celda que se dibuje de nuevo
        cell.postInvalidate();
    }

    public boolean isActivado() {
        return Activado;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(Activado)
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Celda hitCell = getHitCell((int)event.getX(), (int)event.getY());
                if (hitCell == null) {
                    return false;
                }
                else
                {
                    if (onPatternListener != null)
                        onPatternListener.onStarted();
                    notifyCellSelected(hitCell);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                onFinish();
                break;
            case MotionEvent.ACTION_CANCEL:
                reset();
                break;
        }
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawPath(linePath, linePaint);
        if (selectedCells.size() > 0 && lastX > 0 && lastY > 0) {
            Point center = selectedCells.get(selectedCells.size() - 1).getCenter();
            canvas.drawLine(center.x, center.y, lastX, lastY, linePaint);
        }

    }

    private void CargarValores(Context context, AttributeSet attrs)
    {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Patron);
        int regularCellBackground = ta.getColor(R.styleable.Patron_plv_regularCellBackground, ContextCompat.getColor(context, R.color.BackgroundRegularColor));
        int regularDotColor = ta.getColor(R.styleable.Patron_plv_regularDotColor, ContextCompat.getColor(context, R.color.regularColor));
        float regularDotRadiusRatio = ta.getFloat(R.styleable.Patron_plv_regularDotRadiusRatio, DEFAULT_RADIUS_RATIO);

        int selectedCellBackground = ta.getColor(R.styleable.Patron_plv_selectedCellBackground, ContextCompat.getColor(context, R.color.BackgroundSelectedColor));
        int selectedDotColor = ta.getColor(R.styleable.Patron_plv_selectedDotColor, ContextCompat.getColor(context, R.color.selectedColor));
        float selectedDotRadiusRatio = ta.getFloat(R.styleable.Patron_plv_selectedDotRadiusRatio, DEFAULT_RADIUS_RATIO);

        int esperaCellBackground = ta.getColor(R.styleable.Patron_plv_esperaCellBackground, ContextCompat.getColor(context, R.color.BackgroundEsperaColor));
        int esperaDotColor = ta.getColor(R.styleable.Patron_plv_esperaDotColor, ContextCompat.getColor(context, R.color.esperaColor));
        float esperaDotRadiusRatio = ta.getFloat(R.styleable.Patron_plv_esperaDotRadiusRatio, DEFAULT_RADIUS_RATIO);

        int errorCellBackground = ta.getColor(R.styleable.Patron_plv_errorCellBackground, ContextCompat.getColor(context, R.color.BackgroundErrorColor));
        int errorDotColor = ta.getColor(R.styleable.Patron_plv_errorDotColor, ContextCompat.getColor(context, R.color.errorColor));
        float errorDotRadiusRatio = ta.getFloat(R.styleable.Patron_plv_errorDotRadiusRatio, DEFAULT_RADIUS_RATIO);

        Modo = MODO.values()[ta.getInt(R.styleable.Patron_plv_modo,0)];

        Celda.setDrawCirculo(new Celda.DrawCirculo(regularCellBackground, regularDotColor, regularDotRadiusRatio),
                new Celda.DrawCirculo(selectedCellBackground, selectedDotColor, selectedDotRadiusRatio),
                new Celda.DrawCirculo(esperaCellBackground, esperaDotColor, esperaDotRadiusRatio),
                new Celda.DrawCirculo(errorCellBackground, errorDotColor, errorDotRadiusRatio));



        lineWidth = ta.getDimensionPixelSize(R.styleable.Patron_plv_lineWidth,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_LINE_WIDTH, context.getResources().getDisplayMetrics()));
        regularLineColor = ta.getColor(R.styleable.Patron_plv_regularLineColor, ContextCompat.getColor(context, R.color.selectedColor));
        esperaLineColor = ta.getColor(R.styleable.Patron_plv_esperaLineColor, ContextCompat.getColor(context, R.color.esperaColor));
        errorLineColor = ta.getColor(R.styleable.Patron_plv_errorLineColor, ContextCompat.getColor(context, R.color.errorColor));

        spacing = ta.getDimensionPixelSize(R.styleable.Patron_plv_spacing,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SPACING, context.getResources().getDisplayMetrics()));

        plvRowCount = ta.getInteger(R.styleable.Patron_plv_rowCount, DEFAULT_ROW_COUNT);
        plvColumnCount = ta.getInteger(R.styleable.Patron_plv_columnCount, DEFAULT_COLUMN_COUNT);

        errorDuration = ta.getInteger(R.styleable.Patron_plv_errorDuration, DEFAULT_ERROR_DURATION);
        hitAreaPaddingRatio = ta.getFloat(R.styleable.Patron_plv_hitAreaPaddingRatio, DEFAULT_HIT_AREA_PADDING_RATIO);

        ta.recycle();
    }

}
