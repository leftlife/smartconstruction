package kr.koogle.android.smartconstruction.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import kr.koogle.android.smartconstruction.R;

public class CircularImageView extends ImageView {

    /** The border width. */
    private int borderWidth;

    /** The canvas size. */
    private int canvasSize;

    /** The image. */
    private Bitmap image;

    /** The paint. */
    private Paint paint;

    /** The paint border. */
    private Paint paintBorder;

    /**
     * Instantiates a new circular image view.
     *
     * @param context
     *            the context
     */
    public CircularImageView(final Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new circular image view.
     *
     * @param context
     *            the context
     * @param attrs
     *            the attrs
     */
    public CircularImageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.circularImageViewStyle);
    }

    /**
     * Instantiates a new circular image view.
     *
     * @param context
     *            the context
     * @param attrs
     *            the attrs
     * @param defStyle
     *            the def style
     */
    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        paint = new Paint();
        paint.setAntiAlias(true);
        paintBorder = new Paint();
        paintBorder.setAntiAlias(true);

        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.CircularImageView, defStyle, 0);
        if (attributes.getBoolean(R.styleable.CircularImageView_border, true)) {
            setBorderWidth(attributes.getDimensionPixelOffset(
                    R.styleable.CircularImageView_border_width, 0));
            setBorderColor(attributes.getColor(
                    R.styleable.CircularImageView_border_color, Color.WHITE));
        }

    }

    /**
     * Sets the border width.
     *
     * @param borderWidth
     *            the new border width
     */
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        this.requestLayout();
        this.invalidate();
    }

    /**
     * Sets the border color.
     *
     * @param borderColor
     *            the new border color
     */
    public void setBorderColor(int borderColor) {
        if (paintBorder != null)
            paintBorder.setColor(borderColor);
        this.invalidate();
    }

    /**
     * Adds the shadow.
     */
    public void addShadow() {
        setLayerType(LAYER_TYPE_SOFTWARE, paintBorder);
        paintBorder.setShadowLayer(4.0f, 0.0f, 2.0f, Color.BLACK);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
     */
    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        // load the bitmap
        image = drawableToBitmap(getDrawable());
        // init shader
        if (image != null) {
            canvasSize = canvas.getWidth();
            if (canvas.getHeight() < canvasSize)
                canvasSize = canvas.getHeight();
            BitmapShader shader = new BitmapShader(Bitmap.createScaledBitmap(
                    image, canvasSize, canvasSize, false),
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paint.setShader(shader);

            int circleCenter = (canvasSize - (borderWidth * 2)) / 2;
            canvas.drawCircle(circleCenter + borderWidth, circleCenter
                    + borderWidth, ((canvasSize - (borderWidth * 2)) / 2)
                    + borderWidth - 4.0f, paintBorder);
            canvas.drawCircle(circleCenter + borderWidth, circleCenter
                            + borderWidth,
                    ((canvasSize - (borderWidth * 2)) / 2) - 4.0f, paint);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.ImageView#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    /**
     * Measure width.
     *
     * @param measureSpec
     *            the measure spec
     * @return the int
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = specSize;
        } else {
            result = canvasSize;
        }
        return result;
    }

    /**
     * Measure height.
     *
     * @param measureSpecHeight
     *            the measure spec height
     * @return the int
     */
    private int measureHeight(int measureSpecHeight) {
        int result = 0;
        int specMode = View.MeasureSpec.getMode(measureSpecHeight);
        int specSize = View.MeasureSpec.getSize(measureSpecHeight);
        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = specSize;
        } else {
            result = canvasSize;
        }
        return (result + 2);
    }

    /**
     * Drawable to bitmap.
     *
     * @param drawable
     *            the drawable
     * @return the bitmap
     */
    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        } else if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}