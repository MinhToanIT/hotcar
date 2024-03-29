package com.app.hotgo.utility;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.DatePicker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;



public class MyDatePickerDialog extends DatePickerDialog {
    public MyDatePickerDialog(Context context, OnDateSetListener callBack,
                               int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
        if (Build.VERSION.SDK_INT == 24) {
            try {
                final Field field = this.findField(
                        DatePickerDialog.class,
                        DatePicker.class,
                        "mDatePicker"
                );

                final DatePicker datePicker = (DatePicker) field.get(this);
                final Class<?> delegateClass = Class.forName(
                        "android.widget.DatePicker$DatePickerDelegate"
                );
                final Field delegateField = this.findField(
                        DatePicker.class,
                        delegateClass,
                        "mDelegate"
                );

                final Object delegate = delegateField.get(datePicker);
                final Class<?> spinnerDelegateClass = Class.forName(
                        "android.widget.DatePickerSpinnerDelegate"
                );

                if (delegate.getClass() != spinnerDelegateClass) {
                    delegateField.set(datePicker, null);
                    datePicker.removeAllViews();

                    final Constructor spinnerDelegateConstructor =
                            spinnerDelegateClass.getDeclaredConstructor(
                                    DatePicker.class,
                                    Context.class,
                                    AttributeSet.class,
                                    int.class,
                                    int.class
                            );
                    spinnerDelegateConstructor.setAccessible(true);

                    final Object spinnerDelegate = spinnerDelegateConstructor.newInstance(
                            datePicker,
                            context,
                            null,
                            android.R.attr.datePickerStyle,
                            0
                    );
                    delegateField.set(datePicker, spinnerDelegate);

                    datePicker.init(year, monthOfYear, dayOfMonth, this);
                    datePicker.setCalendarViewShown(false);
                    datePicker.setSpinnersShown(true);
                }
            } catch (Exception e) { /* Do nothing */ }
        }
    }

    private Field findField(Class objectClass, Class fieldClass, String expectedName) {
        try {
            final Field field = objectClass.getDeclaredField(expectedName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) { /* Ignore */ }

        for (final Field field : objectClass.getDeclaredFields()) {
            if (field.getType() == fieldClass) {
                field.setAccessible(true);
                return field;
            }
        }

        return null;
    }
}