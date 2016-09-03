package bharatd.example.com.drum;

/**
 * Created by bharatd on 16/8/2016.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by hvtran.2014 on 27/5/2015.
 */
public class MainImage extends ImageView {
    private final int BUFFER_SIZE = 512;
    private final int DEFAULT_STROKE_WIDTH = 3;
    private final int DEFAULT_COLOR = 0xFFFF0000;

    private float leftX[];
    private float leftY[];
    private float leftZ[];
    private int leftIndex = 0;

    private float rightX[];
    private float rightY[];
    private float rightZ[];
    private int rightIndex = 0;

    private Paint paint;
    public MainImage(Context context){
        super(context);
        leftX = new float[BUFFER_SIZE];
        leftY = new float[BUFFER_SIZE];
        leftZ = new float[BUFFER_SIZE];
        leftIndex = 0;

        rightX = new float[BUFFER_SIZE];
        rightY = new float[BUFFER_SIZE];
        rightZ = new float[BUFFER_SIZE];
        rightIndex = 0;

        paint = new Paint();
        paint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        paint.setColor(DEFAULT_COLOR);
    }

    public MainImage(Context context, AttributeSet attrs){
        super(context, attrs);
        leftX = new float[BUFFER_SIZE];
        leftY = new float[BUFFER_SIZE];
        leftZ = new float[BUFFER_SIZE];
        leftIndex = 0;

        rightX = new float[BUFFER_SIZE];
        rightY = new float[BUFFER_SIZE];
        rightZ = new float[BUFFER_SIZE];
        rightIndex = 0;

        paint = new Paint();
        paint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        paint.setColor(DEFAULT_COLOR);
    }

    public MainImage(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        leftX = new float[BUFFER_SIZE];
        leftY = new float[BUFFER_SIZE];
        leftZ = new float[BUFFER_SIZE];
        leftIndex = 0;

        rightX = new float[BUFFER_SIZE];
        rightY = new float[BUFFER_SIZE];
        rightZ = new float[BUFFER_SIZE];
        rightIndex = 0;

        paint = new Paint();
        paint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        paint.setColor(DEFAULT_COLOR);
    }

    public MainImage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
        leftX = new float[BUFFER_SIZE];
        leftY = new float[BUFFER_SIZE];
        leftZ = new float[BUFFER_SIZE];
        leftIndex = 0;

        rightX = new float[BUFFER_SIZE];
        rightY = new float[BUFFER_SIZE];
        rightZ = new float[BUFFER_SIZE];
        rightIndex = 0;

        paint = new Paint();
        paint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        paint.setColor(DEFAULT_COLOR);
    }

    public void appendAccelerometer(int wrist, float values[]){
        if(wrist == 0) {
            leftX[leftIndex] = values[0];
            leftY[leftIndex] = values[1];
            leftZ[leftIndex] = values[2];
            leftIndex = (leftIndex + 1) % BUFFER_SIZE;
        }else {
            rightX[rightIndex] = values[0];
            rightY[rightIndex] = values[1];
            rightZ[rightIndex] = values[2];
            rightIndex = (rightIndex + 1) % BUFFER_SIZE;
        }
    }

    public void appendAccelerometer(int wrist, float x, float y, float z){
        if(wrist == 0) {
            leftX[leftIndex] = x;
            leftY[leftIndex] = y;
            leftZ[leftIndex] = z;
            leftIndex = (leftIndex + 1) % BUFFER_SIZE;
        }else {
            rightX[rightIndex] = x;
            rightY[rightIndex] = y;
            rightZ[rightIndex] = z;
            rightIndex = (rightIndex + 1) % BUFFER_SIZE;
        }
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        int left = leftIndex;
        int right = rightIndex;
        float firstX = leftX[left];
        float firstY = leftY[left];
        float firstZ = leftZ[left];
        float secondX;
        float secondY;
        float secondZ;
        for(int i = 1; i < BUFFER_SIZE; i++){
            left = (left + 1) % BUFFER_SIZE;
            secondX = leftX[left];
            secondY = leftY[left];
            secondZ = leftZ[left];
            paint.setColor(0xFFFF0000);
            canvas.drawLine((i-1)*5, firstX*20 + getHeight()/2, i*5, secondX*20 + getHeight()/2, paint);
            paint.setColor(0xFF00FF00);
            canvas.drawLine((i-1)*5, firstY*20 + getHeight()/2, i*5, secondY*20 + getHeight()/2, paint);
            paint.setColor(0xFF0000FF);
            canvas.drawLine((i-1)*5, firstZ*20 + getHeight()/2, i*5, secondZ*20 + getHeight()/2, paint);
            firstX = secondX;
            firstY = secondY;
            firstZ = secondZ;
        }
//        firstX = rightX[right];
//        firstY = rightY[right];
//        firstZ = rightZ[right];
//        for(int i = 1; i < BUFFER_SIZE; i++){
//            right = (right + 1) % BUFFER_SIZE;
//            secondX = rightX[right];
//            secondY = rightY[right];
//            secondZ = rightZ[right];
//            paint.setColor(0xFFFF0000);
//            canvas.drawLine((i-1)*5 + screenWidth/2, firstX*20 + getHeight()/2, i*5 + screenWidth/2, secondX*20 + getHeight()/2, paint);
//            paint.setColor(0xFF00FF00);
//            canvas.drawLine((i-1)*5 + screenWidth/2, firstY*20 + getHeight()/2, i*5 + screenWidth/2, secondY*20 + getHeight()/2, paint);
//            paint.setColor(0xFF0000FF);
//            canvas.drawLine((i-1)*5 + screenWidth/2, firstZ*20 + getHeight()/2, i*5 + screenWidth/2, secondZ*20 + getHeight()/2, paint);
//            firstX = secondX;
//            firstY = secondY;
//            firstZ = secondZ;
//        }
    }
}

