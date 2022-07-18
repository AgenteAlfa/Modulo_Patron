package com.cerobits.patron;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.View;


public class Celda extends View {

    public static final String TAG = "Celda";
    private static DrawCirculo DCNormal, DCSeleccionado, DCEspera, DCError;


    public enum Estado{NORMAL, SELECCIONADO, ESPERA, ERROR}
    public static final int CeldaPorColumna = 3;

    private Estado ESTADO = Estado.NORMAL;
    private final Paint paint;
    private final int Index;

    protected static void setDrawCirculo(DrawCirculo DC_Normal, DrawCirculo DC_Seleccionado,
                                         DrawCirculo DC_Espera, DrawCirculo DC_Error)
    {
        DCNormal = DC_Normal;
        DCSeleccionado = DC_Seleccionado;
        DCEspera = DC_Espera;
        DCError = DC_Error;
    }

    public Celda(Context context) {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Index = -1;
    }

    public Celda(Context context, int index) {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Index = index;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int d = MeasureSpec.getSize(widthMeasureSpec) / CeldaPorColumna;
        setMeasuredDimension(d,d);
    }

    public float getRadius() {
        return (Math.min(getWidth(), getHeight()) - (getPaddingLeft() + getPaddingRight())) / 2f;
    }
    private void drawDot(Canvas canvas, DrawCirculo drawCirculo){
        float radio = getRadius();
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        paint.setColor(drawCirculo.Fondo);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX,centerY,radio,paint);

        paint.setColor(drawCirculo.Color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX,centerY,radio * drawCirculo.RadioRatio,paint);



    }

    public Point getCenter(){
        Point point = new Point();
        point.x = getLeft() + (getRight() - getLeft()) / 2;
        point.y = getTop() + (getBottom() - getTop()) / 2;
        return point;
    }


    public void setESTADO(Estado ESTADO) {
        this.ESTADO = ESTADO;
    }


    public void Reset(){
        ESTADO = Estado.NORMAL;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: Se dibujo una celda");
        switch (ESTADO) {
            case NORMAL:
                drawDot(canvas, DCNormal);
                break;
            case SELECCIONADO:
                drawDot(canvas, DCSeleccionado);
                break;
            case ESPERA:
                drawDot(canvas, DCEspera);
                break;
            case ERROR:
                drawDot(canvas, DCError);
                break;
        }
    }

    public int getIndex() {
        return Index;
    }



    protected static class DrawCirculo {
        final private int Fondo;
        final private int Color;
        final private float RadioRatio;


        public DrawCirculo(int fondo, int color, float radio) {
            Fondo = fondo;
            Color = color;
            RadioRatio = radio;
        }

    }

}
