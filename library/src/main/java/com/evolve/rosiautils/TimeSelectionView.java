package com.evolve.rosiautils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;


public class TimeSelectionView extends AppCompatEditText {
    private Date selectedDate;
    private Activity activity;
    private Drawable drawable;

    public TimeSelectionView(Context context) {
        super(context);
        init(null, context);
    }

    public TimeSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        obtainStyledAttributes(context, attrs, 0);
        init(attrs, context);
    }

    public TimeSelectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainStyledAttributes(context, attrs, defStyleAttr);
        init(attrs, context);
    }

    public void init(AttributeSet attributeSet, Context context) {
        setClickable(false);
        setFocusable(false);
        setCompoundDrawablePadding((int) getResources().getDimension(R.dimen.padding_small));
        setCompoundDrawablesWithIntrinsicBounds(null,
                null, drawable, null);
        this.setOnClickListener(v -> showDatePicker());
    }

    private void obtainStyledAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EdittextAttribute, defStyleAttr, 0);
            try {
                // Resources$NotFoundException if vector image
                int drawableResId = typedArray.getResourceId(R.styleable.EdittextAttribute_customDrawable, -1);
                drawable = AppCompatResources.getDrawable(context, drawableResId);

            } catch (Exception e) {
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_timer_grey_compat);
            } finally {
                typedArray.recycle();
            }
        }
    }

    private void showDatePicker() {
        hideSoftKeyboard(this);
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        if (selectedDate != null)
            cal.setTime(selectedDate);


        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), AlertDialog.THEME_HOLO_LIGHT, (view, hourOfDay, minute) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            newDate.set(Calendar.MINUTE, minute);
            selectedDate = newDate.getTime();
            setViewBasedOnSelectedDate();
        }, cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), false);

        timePickerDialog.show();

    }

    private void setViewBasedOnSelectedDate() {
        if (selectedDate != null)
            TimeSelectionView.this.setText(getFormattedStringFromDate(selectedDate));
    }

    private String getFormattedStringFromDate(Date date) {
        if (date == null) {
            return "";
        }

        DateFormat format = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        return format.format(date);
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;

        setViewBasedOnSelectedDate();
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private String convertDateToString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return simpleDateFormat.format(date);
    }

    public void hideSoftKeyboard(EditText view) {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }
    }

}
