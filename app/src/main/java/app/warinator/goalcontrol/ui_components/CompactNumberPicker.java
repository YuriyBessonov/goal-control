package app.warinator.goalcontrol.ui_components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import app.warinator.goalcontrol.R;

/**
 * Кастомный NumberPicker
 */
public class CompactNumberPicker extends RelativeLayout {
    private TextView tvValue;
    private ImageButton btnInc;
    private ImageButton btnDec;


    private int mMaxValue;
    private int mMinValue;
    private int mValue;
    private OnValueChangeListener mOnValueChangeListener;

    public CompactNumberPicker(Context context) {
        super(context);
        initView(context, null);
    }

    public CompactNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public CompactNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        mOnValueChangeListener = onValueChangeListener;
    }

    //Инициализация View
    private void initView(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.compact_number_picker, this, true);
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CompactNumberPicker,
                    0, 0);
            try {
                mMaxValue = a.getInteger(R.styleable.CompactNumberPicker_maxValue, Integer.MAX_VALUE - 1);
                mMinValue = a.getInteger(R.styleable.CompactNumberPicker_minValue, 0);
                mValue = a.getInteger(R.styleable.CompactNumberPicker_value, mMinValue);
            } finally {
                a.recycle();
            }
        } else {
            mMaxValue = Integer.MAX_VALUE - 1;
            mMinValue = 0;
            mValue = 0;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tvValue = (TextView) findViewById(R.id.tv_picker_value);
        btnDec = (ImageButton) findViewById(R.id.btn_dec);
        btnInc = (ImageButton) findViewById(R.id.btn_inc);
        tvValue.setText(String.valueOf(mValue));
        btnInc.setOnClickListener(v -> inc());
        btnDec.setOnClickListener(v -> dec());
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int mMaxValue) {
        this.mMaxValue = mMaxValue;
        if (mValue > mMaxValue) {
            setValue(mMaxValue);
        }
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setMinValue(int mMinValue) {
        this.mMinValue = mMinValue;
        if (mValue < mMinValue) {
            setValue(mMinValue);
        }
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        if (value < mMinValue) {
            this.mValue = mMinValue;
        } else if (value > mMaxValue) {
            this.mValue = mMaxValue;
        } else {
            this.mValue = value;
        }
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener.onValueChange(mValue);
        }
        tvValue.setText(String.valueOf(mValue));
        invalidate();
        requestLayout();
    }

    //Инкремент текущего значения
    public void inc() {
        setValue(mValue + 1);
    }

    //Декремент текущего значения
    private void dec() {
        setValue(mValue - 1);
    }


    public interface OnValueChangeListener {
        void onValueChange(int newVal);
    }

}
