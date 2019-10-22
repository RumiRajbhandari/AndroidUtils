package com.evolve.rosiautils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;


public class DateSelectionView extends AppCompatEditText {
    private Date selectedDate;
    private Activity activity;

    public DateSelectionView(Context context) {
        super(context);
        init(null, context);
    }

    public DateSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);

    }

    public DateSelectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, context);
    }

    public void init(AttributeSet attributeSet, Context context) {
        setClickable(false);
        setFocusable(false);
        // setTextColor(getResources().getColor(R.color.theme_grey));
        setCompoundDrawablePadding((int) getResources().getDimension(R.dimen.padding_small));
        setCompoundDrawablesWithIntrinsicBounds(null,
                null, ContextCompat.getDrawable(context, R.drawable.ic_menu_today_grey_compat), null);
        this.setOnClickListener(view -> showDatePicker());
    }

    private void showDatePicker() {
        hideSoftKeyboard(this);
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        if (selectedDate != null)
            cal.setTime(selectedDate);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            selectedDate = newDate.getTime();
            setViewBasedOnSelectedDate();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();


    }

    private void setViewBasedOnSelectedDate() {
        if (selectedDate != null)
            DateSelectionView.this.setText(getFormattedStringFromDate(selectedDate));
    }

    private String getFormattedStringFromDate(Date date) {
        if (date == null) {
            return "";
        }

        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
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
