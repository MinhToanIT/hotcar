package com.app.hotgo.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.app.hotgo.BaseFragment;
import com.app.hotgo.R;
import com.app.hotgo.adapters.TypeCarAdapter;
import com.app.hotgo.config.Constant;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.CarType;
import com.app.hotgo.object.User;
import com.app.hotgo.utility.FileDialog;
import com.app.hotgo.utility.PermissionUtil;
import com.app.hotgo.widget.CircleImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RegisterFragment extends BaseFragment implements OnClickListener {

    private ImageView imgPhotoOne, imgPhotoTwo;
    private Bitmap imageOne, imageTwo;
    private File document;

    private EditText editName, editEmail, editCarPlate, editModel, editYear,
            editBrand, txtDocument, txtAccount, txtUpdateStatus, txtPhone, txtIdentity;
    private TextView btnSave;
    private TextView lbl_CarPlace, lbl_Brand,
            lbl_Year, lbl_Email,
            lbl_Model, txtStatusPaypalOption;
    private CircleImageView imgProfile;
    private Spinner txtTypeCar;

    private User user;
    private AQuery aq;

    /* FOR DATE TIME */
    private DatePickerDialog datePickerDialog, yearPickerDialog;
    private SimpleDateFormat yearFormatter;
    private Calendar timeDob, timeYear;
    Calendar newCalendar = Calendar.getInstance();
    String typeCar = "1";
    private ArrayList<CarType> listCarTypes = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstancfeState) {
        View view = inflater.inflate(R.layout.fragment_register, container,
                false);
        getDataFromGlobal();
        initUI(view);
        initMenuButton(view);
        setHeaderTitle(getResources()
                .getString(R.string.lbl_register_as_driver));

        init(view);
        initControl();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getData();
        }
    }

    ;

    private void getDataFromGlobal() {
        user = GlobalValue.getInstance().user;
    }

    public void changeLanguage() {

        lbl_CarPlace.setText(R.string.lbl_car_plate);
        lbl_Model.setText(R.string.lbl_model_car);
        lbl_Brand.setText(R.string.lbl_brand_car);
        lbl_Year.setText(R.string.lbl_year_manufacturer);
        lbl_Email.setText(R.string.lbl_email);
    }

    private void init(View view) {

        lbl_Email = (TextView) view.findViewById(R.id.lbl_Email);
        txtIdentity = (EditText) view.findViewById(R.id.txtIdentity);

        editName = (EditText) view.findViewById(R.id.txtUpdateNameDrive);
        editCarPlate = (EditText) view.findViewById(R.id.txtUpdateCarPlate);
        editModel = (EditText) view.findViewById(R.id.txtUpdateModelCar);
        editBrand = (EditText) view.findViewById(R.id.txtUpdateBankCar);
        editYear = (EditText) view.findViewById(R.id.txtUpdateYear);
        editEmail = (EditText) view.findViewById(R.id.txtUpdateEmail);
        txtUpdateStatus = (EditText) view.findViewById(R.id.txtUpdateStatus);
        txtDocument = (EditText) view.findViewById(R.id.txtDocument);
        txtAccount = (EditText) view.findViewById(R.id.txtAccount);
        txtStatusPaypalOption = (TextView) view.findViewById(R.id.txtStatusPaypalOption);
        txtPhone = (EditText) view.findViewById(R.id.txtUpdatePhone);
        imgProfile = (CircleImageView) view.findViewById(R.id.imgProfile);
        imgPhotoOne = (ImageView) view.findViewById(R.id.imgPhotoOne);
        imgPhotoTwo = (ImageView) view.findViewById(R.id.imgPhotoTwo);
        txtTypeCar = (Spinner) view.findViewById(R.id.txtTypeCar);
        btnSave = (TextView) view.findViewById(R.id.btnSave);
    }

    private void initControl() {
        editName.setEnabled(false);
        editEmail.setEnabled(false);
        txtPhone.setEnabled(false);
        txtDocument.setOnClickListener(this);
        aq = new AQuery(self);
        imgPhotoOne.setOnClickListener(this);
        imgPhotoTwo.setOnClickListener(this);
        editYear.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        /* CREATE DATE FORMAT */
        yearFormatter = new SimpleDateFormat("yyyy", Locale.US);

        /* CREATE DAILOG DOB */
        timeDob = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getActivity(),
                new OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        timeDob.set(year, monthOfYear, dayOfMonth);
                    }
                }, newCalendar.get(Calendar.YEAR),
                newCalendar.get(Calendar.MONTH),
                newCalendar.get(Calendar.DAY_OF_MONTH));

        txtTypeCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                typeCar = listCarTypes.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void showYearDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.yeardialog, null, false);

        TextView set = (TextView) v.findViewById(R.id.button1);
        TextView cancel = (TextView) v.findViewById(R.id.button2);
        final NumberPicker nopicker = (NumberPicker) v.findViewById(R.id.numberPicker1);
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setView(v)
                .create();


        nopicker.setMaxValue(year + 200);
        nopicker.setMinValue(year - 200);
        nopicker.setWrapSelectorWheel(false);
        nopicker.setValue(year);
        nopicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        set.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editYear.setText(String.valueOf(nopicker.getValue()));
                alertDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();


    }

    // get data info user
    public void getData() {
        ModelManager.showInfoProfile(
                PreferencesManager.getInstance(mainActivity).getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            GlobalValue.getInstance().setUser(
                                    ParseJsonUtil.parseInfoProfile(json));
                            user = ParseJsonUtil.parseInfoProfile(json);

                            editName.setText(user.getFullName());
                            editEmail.setText(user.getEmail());
                            txtPhone.setText(user.getPhone());
                            initDataTypeCar();
                            user.getIsOnline();
                            aq.id(imgProfile).image(user.getLinkImage());
                            if (user.getDriverObj().getIsActive() == null
                                    || user.getDriverObj()
                                    .getIsActive().equals("0")) {
                                txtStatusPaypalOption.setText(getString(R.string.lblPayoutPaypal));
                            } else {
                                txtStatusPaypalOption.setText(getString(R.string.lblPayoutPaypalBinding));
                            }
                        } else {
                            showToast(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToast(getResources().getString(
                                R.string.message_have_some_error));

                    }
                });
    }

    public void initDataTypeCar() {
        for (int i = 0; i < GlobalValue.getInstance().getListCarTypes().size(); i++) {
            if (GlobalValue.getInstance().getListCarTypes().get(i).getId() != null && !GlobalValue.getInstance().getListCarTypes().get(i).getId().equals("")) {
                listCarTypes.add(GlobalValue.getInstance().getListCarTypes().get(i));
            }
        }
        TypeCarAdapter adapter = new TypeCarAdapter(getActivity(), listCarTypes);
        txtTypeCar.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgPhotoOne:
                mainActivity.choiseImageOne();
                break;
            case R.id.imgPhotoTwo:
                mainActivity.choiseImageTwo();
                break;
            /*
             * case R.id.txtUpdateDob: datePickerDialog.show(); break;
             */
            case R.id.txtUpdateYear:
                showYearDialog();
//                yearPickerDialog.show();
                break;
            case R.id.txtDocument:
                choiseFile();
                break;
            case R.id.btnSave:
                register();
                break;
            default:
                break;
        }
    }

    public void setImageOne(Uri selectedImage, Bitmap image) {
        imageOne = image;
        imgPhotoOne.setImageURI(selectedImage);
    }

    public void setImageOne(Bitmap imageBitmap) {
        imageOne = imageBitmap;
        imgPhotoOne.setImageBitmap(imageBitmap);
    }

    public void setImageTwo(Uri selectedImage, Bitmap image) {
        imageTwo = image;
        imgPhotoTwo.setImageURI(selectedImage);
    }

    public void setImageTwo(Bitmap imageBitmap) {
        imageTwo = imageBitmap;
        imgPhotoTwo.setImageBitmap(imageBitmap);
    }

    private void choiseFile() {
        if (PermissionUtil.checkReadWriteStoragePermission(self)) {
            File mPath = new File(Environment.getExternalStorageDirectory()
                    + "//DIR//");
            FileDialog fileDialog = new FileDialog(mainActivity, mPath);
            fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    Log.d("trangpv", "size file: " + file.length());
                    if (file.length() > (2 * 1024 * 1024)) {
                        Toast.makeText(self, getString(R.string.notify_file_upload_size), Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        document = file;
                        txtDocument.setText(file.getName());
                    }
                }
            });
            fileDialog.showDialog();
        }
    }

    public void register() {
        if (validate()) {
            user = new User();
            user.setDob(editEmail.getText().toString());
            user.setAccount(txtAccount.getText().toString());
            user.getCarObj().setCarPlate(editCarPlate.getText().toString());
            user.getCarObj().setBrand(editBrand.getText().toString());
            user.getCarObj().setModel(editModel.getText().toString());
            user.getCarObj().setYear(editYear.getText().toString());
            user.getCarObj().setStatus(txtUpdateStatus.getText().toString());
            user.getCarObj().setTypeCar(typeCar);
            user.getDriverObj().setIdentity(txtIdentity.getText().toString());
            ModelManager.driverRegister(mainActivity.preferencesManager.getToken(), user.getCarObj()
                            .getCarPlate(), user.getDriverObj().getIdentity(), user.getCarObj().getBrand(), user
                            .getCarObj().getModel(), user.getCarObj().getYear(),
                    user.getCarObj().getStatus(), user.getAccount(), user.getCarObj().getTypeCar(),
                    imageOne, imageTwo, document, self, true,
                    new ModelManagerListener() {

                        @Override
                        public void onError() {
                            mainActivity.showToastMessage(getResources().getString(
                                    R.string.message_have_some_error));
                        }

                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                showToast(getResources().getString(
                                        R.string.message_register_success));
                                preferencesManager.setIsDriver(true);
                                if (ParseJsonUtil.getIsActiveAsDriver(json).equals(Constant.KEY_IS_ACTIVE)) {
                                    preferencesManager.setDriverIsActive();
                                } else {
                                    preferencesManager.setDriverIsUnActive();
                                }
                                getDataProfile();
                            } else {
                                mainActivity.showToastMessage(ParseJsonUtil.getMessage(json));
                            }
                        }
                    });
        }
    }

    public void getDataProfile() {
        ModelManager.showInfoProfile(
                PreferencesManager.getInstance(mainActivity).getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            GlobalValue.getInstance().setUser(
                                    ParseJsonUtil.parseInfoProfile(json));
                            mainActivity.setMenuByUserType();
                            mainActivity.showFragment(mainActivity.PASSENGER_PAGE1);
                        } else {
                            showToast(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToast(getResources().getString(
                                R.string.message_have_some_error));
                    }
                });
    }

    /* VALIDATE */
    public boolean validate() {
        if (editCarPlate.getText().toString().isEmpty()) {
            editCarPlate.requestFocus();
            showToast(getString(R.string.message_carPlate));
            return false;
        }
        if (imageOne == null || imageTwo == null) {
            showToast(getString(R.string.message_moreImage));
            return false;
        }
        if (imgProfile == null) {
            showToast(getString(R.string.lblValidateImage));
            return false;
        }
        return true;
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
