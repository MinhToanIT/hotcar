package com.app.hotgo.modelmanager;

import com.app.hotgo.config.LinkApi;
import com.app.hotgo.object.CarType;
import com.app.hotgo.object.CityObj;
import com.app.hotgo.object.CurrentOrder;
import com.app.hotgo.object.DriverOnlineObj;
import com.app.hotgo.object.ItemTransactionHistory;
import com.app.hotgo.object.ItemTripHistory;
import com.app.hotgo.object.LocationDriverObj;
import com.app.hotgo.object.ProductObj;
import com.app.hotgo.object.RequestObj;
import com.app.hotgo.object.SettingObj;
import com.app.hotgo.object.ShopObj;
import com.app.hotgo.object.StateObj;
import com.app.hotgo.object.Transfer;
import com.app.hotgo.object.User;
import com.app.hotgo.object.UserFacebook;
import com.app.hotgo.object.UserOnlineObj;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseJsonUtil {

    private static final String PAYPAY_RESONDE = "response";
    private static final String PAYPAL_RESPONDE_TYPE = "response_type";

    // Get
    public static boolean isSuccess(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.getString(LinkApi.KEY_JSON_STATUS)
                    .equalsIgnoreCase(LinkApi.JSON_STATUS_SUCCESS)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

        }
        return false;
    }

    // Check trip status
    public static String pareWaitDriverConfirm(String json) {
        String action = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray(LinkApi.KEY_JSON_DATA);
            JSONObject itemJson;
            itemJson = jsonArray.getJSONObject(0);
            return itemJson.getString("isWattingConfirm");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return action;
    }

    public static boolean isSuccessData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.getString(LinkApi.KEY_JSON_DATA)
                    .equalsIgnoreCase(LinkApi.JSON_DATA_ERROR)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

        }
        return false;
    }

    // Get json message
    public static String getMessage(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            return jsonobj.getString(LinkApi.KEY_MESSAGE);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getEstimateFare(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            return jsonobj.getString("estimate_fare");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getIsActiveAccount(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            return jsonobj.getString("isActive");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getCountDriver(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            return jsonobj.getString("count");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getIsActiveAsDriver(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            return jsonobj.getString("is_active");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getIsActiveAsShop(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            return jsonobj.getString("is_active");
        } catch (Exception e) {
            return "";
        }
    }

    // Count My Request
    public static int countMyRequest(String json) {
        int count = 0;
        try {
            JSONObject jsonobj = new JSONObject(json);
            return (jsonobj.getJSONArray(LinkApi.KEY_JSON_DATA))
                    .length();
        } catch (Exception e) {
            return count;
        }
    }

    // Get Token from login
    public static String getTokenFromLogin(String json) {
        try {
            JSONObject object = new JSONObject(json);
            return object.getJSONObject(LinkApi.KEY_JSON_DATA)
                    .getString("token");
        } catch (Exception e) {

            return "";
        }

    }

    // Get Id from login
    public static String getIdFromLogin(String json) {
        try {
            JSONObject object = new JSONObject(json);
            return object.getJSONObject(LinkApi.KEY_JSON_DATA)
                    .getString("user_id");
        } catch (Exception e) {

            return "";
        }
    }

    // Get Id from login
    public static boolean isDriverFromLogin(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.getJSONObject(LinkApi.KEY_JSON_DATA)
                    .getString("isDriver").equals("1")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

        }
        return false;
    }

    // Get Driver Is Active from login
    public static boolean driverIsActive(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.getJSONObject(LinkApi.KEY_JSON_DATA)
                    .getString("driverActive").equals("1")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

        }
        return false;
    }

    // Get user information
    public static UserFacebook parseUser(String json) {
        UserFacebook user = new UserFacebook();
        try {
            JSONObject jsonobj = new JSONObject(json)
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            user.setId(jsonobj.getString("gcm_id"));
            user.setFull_name(jsonobj.getString("name"));
            user.setLinkProfile(jsonobj.getString("image"));
            user.setEmail(jsonobj.getString("email"));
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return user;
    }

    public static UserFacebook parseLogin(String json) {
        UserFacebook user = new UserFacebook();
        try {
            JSONObject jsonobj = new JSONObject(json)
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            user.setId(jsonobj.getString("gcm_id"));
            user.setEmail(jsonobj.getString("email"));
            user.setIme(jsonobj.getString("ime"));
            user.setType(jsonobj.getString("type"));
            user.setLat(jsonobj.getString("lat"));
            user.setLng(jsonobj.getString("long"));
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return user;
    }

    // Get max page
    public static int getMaxPage(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            return jsonobj.getInt("numpage");
        } catch (Exception e) {
            return 0;
        }
    }

    public static ArrayList<CarType> parseListCarTypes(String json) {
        ArrayList<CarType> arr = new ArrayList<CarType>();
        CarType stateObj;
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray("list_job");
            for (int i = 0; i < jsonArray.length(); i++) {
                stateObj = new CarType();
                JSONObject itemJson = jsonArray.getJSONObject(i);
                stateObj.setLink(itemJson.getString("link"));
                stateObj.setId(itemJson.getString("link"));
                stateObj.setName(itemJson.getString("name"));
                stateObj.setImageType(itemJson.getString("image"));
                stateObj.setStart_fare(itemJson.getString("startFare"));
                stateObj.setFee_per_minute(itemJson.getString("feePerMinute"));
                stateObj.setFee_per_kilometer(itemJson.getString("feePerKilometer"));
                stateObj.setGotoA(itemJson.getString("pickUpAtA"));
                stateObj.setWorkAtB(itemJson.getString("workAtB"));
                stateObj.setImageMarker(itemJson.getString("imgMarker"));
                stateObj.setTaskRate(itemJson.getString("taskRate"));
                stateObj.setTaskDefaultTime(itemJson.getString("taskDefaultTime"));
                stateObj.setImageTypeActive(itemJson.getString("imgActive"));
                stateObj.setImageSelected(itemJson.getString("imgSelected"));

                arr.add(stateObj);
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }
        return arr;
    }

    // get data trip history
    public static ArrayList<ItemTripHistory> parseTripHistory(String json) {
        ArrayList<ItemTripHistory> arr = new ArrayList<ItemTripHistory>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray(LinkApi.KEY_JSON_DATA);
            JSONObject itemJson;
            ItemTripHistory itemHistory;

            for (int i = 0; i < jsonArray.length(); i++) {
                itemHistory = new ItemTripHistory();
                itemJson = jsonArray.getJSONObject(i);
                itemHistory.setTripId(itemJson.getInt("id"));
                itemHistory.setDriverId(itemJson.getInt("driverId"));
                itemHistory.setPassengerId(itemJson.getInt("passengerId"));
                itemHistory.setStartTime(itemJson.getString("startTime"));
                itemHistory.setEndTime(itemJson.getString("endTime"));
                itemHistory.setStartLocaton(itemJson.getString("startLocation"));
                itemHistory.setEndLocation(itemJson.getString("endLocation"));
                itemHistory.setDistance(itemJson.getString("distance"));
                itemHistory.setActualFare(itemJson.getString("actualFare"));
                itemHistory.setActualReceive(itemJson.getString("actualReceive"));
                itemHistory.setTotalTime(itemJson.getString("totalTime"));
                itemHistory.setLink(itemJson.getString("link"));
                itemHistory.setStartTimeWorking(itemJson.getString("startTimeWorking"));
                itemHistory.setEndTimeWorking(itemJson.getString("endTimeWorking"));
                itemHistory.setPaymentMethod(itemJson.getString("paymentMethod"));
                itemHistory.setIsRate(itemJson.getString("isRate"));
                itemHistory.setRateProduct(itemJson.getString("productRate"));

                JSONObject product = itemJson.getJSONObject("product");
                itemHistory.setShopName(product.getString("shopName"));
                itemHistory.setShopAddress(product.getString("address"));
                itemHistory.setCategory(product.getString("categoryName"));
                itemHistory.setProductName(product.getString("productName"));
                itemHistory.setShippingPrice(product.getString("shipFee"));
                itemHistory.setPrice(product.getString("price"));
                itemHistory.setTotalPrice(product.getString("totalPrice"));
                itemHistory.setQuantity(product.getString("quantity"));

                JSONObject driver = itemJson.getJSONObject("driver");
                itemHistory.setShipper(driver.getString("fullName"));

                JSONObject passenger = itemJson.getJSONObject("passenger");
                itemHistory.setBuyer(passenger.getString("fullName"));


                arr.add(itemHistory);
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }
        return arr;
    }

    public static String parseDistanceFromMap(String json) {
        String str = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray("routes");
            JSONObject itemJson = jsonArray.getJSONObject(0);
            JSONArray jsonArrayLeg = itemJson.getJSONArray("legs");
            JSONObject object = jsonArrayLeg.getJSONObject(0);
            JSONObject object1 = object.getJSONObject("distance");
            JSONObject object2 = object.getJSONObject("duration");
            str = object1.getString("text");


        } catch (JSONException e) {

            e.printStackTrace();
        }
        return str;
    }

    public static String parseTimeFromMap(String json) {
        String str = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray("routes");
            JSONObject itemJson = jsonArray.getJSONObject(0);
            JSONArray jsonArrayLeg = itemJson.getJSONArray("legs");
            JSONObject object = jsonArrayLeg.getJSONObject(0);
            JSONObject object2 = object.getJSONObject("duration");
            str = object2.getString("text");


        } catch (JSONException e) {

            e.printStackTrace();
        }
        return str;
    }

    public static ArrayList<StateObj> parseListStates(String json) {
        ArrayList<StateObj> arr = new ArrayList<StateObj>();
        StateObj stateObj;
        ArrayList<CityObj> listCity;
        CityObj cityObj;
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray(LinkApi.KEY_JSON_DATA);

            for (int i = 0; i < jsonArray.length(); i++) {
                stateObj = new StateObj();
                JSONObject itemJson = jsonArray.getJSONObject(i);
                stateObj.setStateId(itemJson.getString("stateId"));
                stateObj.setStateName(itemJson.getString("stateName"));
                JSONArray arrayStates = itemJson.getJSONArray("stateCities");
                listCity = new ArrayList<>();
                if (arrayStates != null && arrayStates.length() > 0) {
                    for (int j = 0; j < arrayStates.length(); j++) {
                        cityObj = new CityObj();
                        JSONObject objectCity = arrayStates.getJSONObject(j);
                        cityObj.setCityId(objectCity.getString("cityId"));
                        cityObj.setCityName(objectCity.getString("cityName"));
                        listCity.add(cityObj);
                    }
                    stateObj.setStateCities(listCity);
                }
                arr.add(stateObj);
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }
        return arr;
    }

    // get data transaction history
    public static ArrayList<ItemTransactionHistory> parseTransactionHistory(
            String json) {
        ArrayList<ItemTransactionHistory> arrTransaction = new ArrayList<ItemTransactionHistory>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray(LinkApi.KEY_JSON_DATA);
            JSONObject itemJson;
            ItemTransactionHistory itemHistory;

            for (int i = 0; i < jsonArray.length(); i++) {
                itemHistory = new ItemTransactionHistory();
                itemJson = jsonArray.getJSONObject(i);
                itemHistory.setTransactionId(itemJson.getString("id"));
                itemHistory.setDateTimeTransaction(itemJson
                        .getString("dateCreated"));
                itemHistory.setTypeTransaction(itemJson.getString("type"));
                itemHistory.setPointTransaction(itemJson.getString("amount"));
                itemHistory.setNoteTransaction(itemJson.getString("action"));

                String tripId = itemJson.getString("tripId");

                itemHistory.setTripId(tripId);

                arrTransaction.add(itemHistory);
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }
        return arrTransaction;
    }

    // get data profile
    public static User parseInfoProfile(String json) {
        User itemUser = new User();
        try {
            JSONObject jsonObject = new JSONObject(json)
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            itemUser.setIdUser(jsonObject.getString("id"));
            itemUser.setFullName(jsonObject.getString("fullName"));
            itemUser.setLinkImage(jsonObject.getString("image"));
            itemUser.setBalance(jsonObject.getDouble("balance"));
            itemUser.setEmail(jsonObject.getString("email"));
            itemUser.setAddress(jsonObject.getString("address"));
            itemUser.setPhone(jsonObject.getString("phone"));
            itemUser.setGender(jsonObject.getString("gender"));
            itemUser.setDob(jsonObject.getString("dob"));
            itemUser.setDescription(jsonObject.getString("description"));
            itemUser.setIsOnline(jsonObject.getString("isOnline"));
            itemUser.setIsActive(jsonObject.getString("isActive"));
            itemUser.setPassengerRate(jsonObject.getString("passengerRate"));
            itemUser.setStateId(jsonObject.getString("stateId"));
            itemUser.setStateName(jsonObject.getString("stateName"));
            itemUser.setCityId(jsonObject.getString("cityId"));
            itemUser.setCityName(jsonObject.getString("cityName"));
            itemUser.setTypeAccount(jsonObject.getString("typeAccount"));
            itemUser.setTypeTasker(jsonObject.getString("typeTasker"));
            if (!jsonObject.isNull("account")) {
                itemUser.setAccount(
                        jsonObject.getString("account"));
            }
//            if(!jsonObject.isNull("driver")){
            try {
                JSONObject objDriver = jsonObject.getJSONObject("driver");
                itemUser.getDriverObj()
                        .setStatus(objDriver.getString("status"));
                itemUser.getDriverObj().setIsOnline(
                        objDriver.getString("isOnline"));
                itemUser.getDriverObj().setIsActive(
                        objDriver.getString("isActive"));
                itemUser.getDriverObj().setDriverRate(
                        objDriver.getString("driverRate"));
                itemUser.getDriverObj().setUpdatePending(
                        objDriver.getString("updatePending"));
                itemUser.getDriverObj().setBankAccount(
                        objDriver.getString("bankAccount"));
            } catch (Exception e) {
                // TODO: handle exception
            }
            try {
                JSONObject objCar = jsonObject.getJSONObject("car");
                itemUser.getCarObj().setVehiclePlate(objCar.getString("vehiclePlate"));
                itemUser.getCarObj().setVehicleType(objCar.getString("vehicleType"));
                itemUser.getCarObj().setImageone(objCar.getString("image1"));
                itemUser.getCarObj().setImagetwo(objCar.getString("image2"));
            } catch (Exception e) {
                // TODO: handle exception
            }
//            }

//            if(!jsonObject.isNull("shop")){
            try {
                JSONObject objShop = jsonObject.getJSONObject("shop");
                itemUser.getShopObj()
                        .setStatus(objShop.getString("status"));
                itemUser.getShopObj().setIsActive(
                        objShop.getString("isActive"));
                itemUser.getShopObj().setUpdatePending(
                        objShop.getString("updatePending"));
                itemUser.getShopObj().setBankAccount(
                        objShop.getString("bankAccount"));
                itemUser.getShopObj().setImageExtra(objShop.getString("imageExtra"));
                itemUser.getShopObj().setImageExtra2(objShop.getString("imageExtra2"));
                itemUser.getShopObj().setLatitude(objShop.getString("lat"));
                itemUser.getShopObj().setLongitude(objShop.getString("long"));
                itemUser.getShopObj().setIsOnline(objShop.getString("isOnline"));
                itemUser.getShopObj().setRate(objShop.getString("driverRate"));
                itemUser.getShopObj().setRateCount(objShop.getString("driverRateCount"));
                itemUser.getShopObj().setCarPlate(objShop.getString("carPlate"));
                itemUser.getShopObj().setCarType(objShop.getString("carType"));
            } catch (Exception e) {
                // TODO: handle exception
            }


        } catch (JSONException e) {

            e.printStackTrace();
        }
        return itemUser;
    }

    // get data transfer
    public static Transfer parseInfoTransfer(String json) {
        Transfer itemTransfer = new Transfer();
        try {
            JSONObject jsonObject = new JSONObject(json)
                    .getJSONObject(LinkApi.KEY_JSON_DATA);

            itemTransfer.setReceiverName(jsonObject.getString("fullName"));
            itemTransfer.setReceiverProfile(jsonObject.getString("image"));
            itemTransfer.setReceiverEmail(jsonObject.getString("email"));
            itemTransfer.setReceiverGender(jsonObject.getString("gender"));

        } catch (JSONException e) {

            e.printStackTrace();
        }
        return itemTransfer;
    }

    // get data trip history
    public static ArrayList<RequestObj> parseMyRequest(String json) {
        ArrayList<RequestObj> arr = new ArrayList<RequestObj>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray(LinkApi.KEY_JSON_DATA);
            JSONObject itemJson, passengerObj;
            RequestObj itemRequest;

            for (int i = 0; i < jsonArray.length(); i++) {
                itemRequest = new RequestObj();
                itemJson = jsonArray.getJSONObject(i);
                itemRequest.setId(itemJson.getString("id"));
                itemRequest.setPassengerId(itemJson.getString("passengerId"));
                itemRequest.setDriverId(itemJson.getString("driverId"));
                itemRequest.setRequestTime(itemJson.getString("requestTime"));
                itemRequest.setLink(itemJson.getString("link"));
                itemRequest.setStartLat(itemJson.getString("startLat"));
                itemRequest.setStartLong(itemJson.getString("startLong"));
                itemRequest.setStartLat(itemJson.getString("startLat"));
                itemRequest.setStartLocation(itemJson
                        .getString("startLocation"));
                itemRequest.setEndLat(itemJson.getString("endLat"));
                itemRequest.setEndLong(itemJson.getString("endLong"));
                itemRequest.setEndLocation(itemJson.getString("endLocation"));
                itemRequest.setPassengerRate(itemJson
                        .getString("passengerRate"));
                itemRequest.setEstimate_fare(itemJson.getString("estimate_fare"));
                itemRequest.setDistance(itemJson.getString("distance"));

                passengerObj = itemJson.getJSONObject("passenger");
                itemRequest.setPassengerImage(passengerObj.getString("image"));
                itemRequest.setPassengerName(passengerObj.getString("fullName"));
                itemRequest.setPassengerphone(passengerObj.getString("phone"));
                itemRequest.setRateCount(passengerObj.getString("rateCount"));

                JSONObject jsonProduct = itemJson.getJSONObject("product");
                itemRequest.setShopName(jsonProduct.getString("shopName"));
                itemRequest.setProductName(jsonProduct.getString("productName"));
                itemRequest.setQuantity(jsonProduct.getString("quantity"));
                itemRequest.setPrice(jsonProduct.getString("price"));
                itemRequest.setSize(jsonProduct.getString("productSize"));
                itemRequest.setShippingPrice(jsonProduct.getString("shipFee"));
                itemRequest.setCategoryName(jsonProduct.getString("categoryName"));

                arr.add(itemRequest);
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }
        return arr;
    }

    public static CurrentOrder parseCurrentOrder(String json) {
        CurrentOrder currentOrder = new CurrentOrder();
        try {
            JSONObject jsonobj = new JSONObject(json)
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            currentOrder.setId(jsonobj.getString("id"));
            currentOrder.setPassengerId(jsonobj.getString("passengerId"));
            currentOrder.setLink(jsonobj.getString("link"));
            currentOrder.setStartTime(jsonobj.getString("startTime"));
            currentOrder.setStartLat(jsonobj.getString("startLat"));
            currentOrder.setStartLong(jsonobj.getString("startLong"));
            currentOrder.setEndLat(jsonobj.getString("endLat"));
            currentOrder.setEndLong(jsonobj.getString("endLong"));
            currentOrder.setDateCreated(jsonobj.getString("dateCreated"));
            currentOrder.setDriverId(jsonobj.getString("driverId"));
            currentOrder.setStartLocation(jsonobj.getString("startLocation"));
            currentOrder.setEndLocation(jsonobj.getString("endLocation"));
            currentOrder.setStatus(jsonobj.getString("status"));
            currentOrder.setEndTime(jsonobj.getString("endTime"));
            currentOrder.setDistance(jsonobj.getString("distance"));
            currentOrder.setEstimateFare(jsonobj.getString("estimateFare"));
            currentOrder.setActualFare(jsonobj.getString("actualFare"));
            currentOrder.setStartTimeWorking(jsonobj.getString("startTimeWorking"));
            currentOrder.setEndTimeWorking(jsonobj.getString("endTimeWorking"));
            currentOrder.setDriverRateTrip(jsonobj.getString("driverRate"));
            currentOrder.setPassengerRateTrip(jsonobj.getString("passengerRate"));
            if (jsonobj.has("totalPrice"))
                currentOrder.setTotalPrice(jsonobj.getString("totalPrice"));
            if (!jsonobj.isNull("totalTime")) {
                currentOrder.setTotalTime(jsonobj.getString("totalTime"));
            }
            if (!jsonobj.isNull("pickUpAtA")) {
                currentOrder.setPickUpAtA(jsonobj.getString("pickUpAtA"));
            }
            if (!jsonobj.isNull("workAtB")) {
                currentOrder.setWorkAtB(jsonobj.getString("workAtB"));
            }


            JSONObject driver = jsonobj.getJSONObject("driver");
            currentOrder.setDriverName(driver.getString("driverName"));
            currentOrder.setImageDriver(driver.getString("imageDriver"));
            currentOrder.setCarPlate(driver.getString("carPlate"));
            currentOrder.setCarImage(driver.getString("carImage"));
            currentOrder.setDriver_phone(driver.getString("phone"));
            currentOrder.setDriver_rate(driver.getString("rate"));
            currentOrder.setDriverRate(driver.getString("rate"));
            currentOrder.setIdentity(driver.getString("identity"));
            currentOrder.setCarPlate(driver.getString("carPlate"));
            if (driver.has("rateCount"))
                currentOrder.setDriverRateCount(driver.getString("rateCount"));

            JSONObject passenger = jsonobj.getJSONObject("passenger");
            currentOrder.setPassengerName(passenger.getString("passengerName"));
            currentOrder.setImagePassenger(passenger
                    .getString("imagePassenger"));
            currentOrder.setPassenger_rate(passenger.getString("rate"));
            currentOrder.setPassengerRate(passenger.getString("rate"));
            currentOrder.setPassenger_phone(passenger.getString("phone"));
            if (passenger.has("rateCount"))
                currentOrder.setPassengerRateCount(passenger.getString("rateCount"));

            JSONObject product = jsonobj.getJSONObject("product");
            currentOrder.setShopName(product.getString("shopName"));
            currentOrder.setPhone(product.getString("phone"));
            currentOrder.setProductName(product.getString("productName"));
            currentOrder.setProductSize(product.getString("productSize"));
            currentOrder.setQuantity(product.getString("quantity"));
            currentOrder.setPrice(product.getString("price"));
            currentOrder.setShipFee(product.getString("shipFee"));
            currentOrder.setProductImage(product.getString("image"));
            currentOrder.setShopImage(product.getString("shopImage"));
            currentOrder.setMarkerImage(product.getString("marker"));
            currentOrder.setProductDesciption(product.getString("description"));
            currentOrder.setCategory(product.getString("categoryName"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return currentOrder;
    }

    // public static CurrentOrder parseCurrentOrderFromDriverConfirm(String
    // json) {
    // CurrentOrder currentOrder = new CurrentOrder();
    // try {
    // JSONObject jsonobj = new JSONObject(json);
    // currentOrder.setId(jsonobj.getString("tripId"));
    // currentOrder.setDriverName(jsonobj.getString("driverName"));
    // currentOrder.setRate(jsonobj.getString("rate"));
    // currentOrder.setDriverImage(jsonobj.getString("imageDriver"));
    // currentOrder.setDriverPhone(jsonobj.getString("phone"));
    // currentOrder.setCarPlate(jsonobj.getString("carPlate"));
    // currentOrder.setCarImage(jsonobj.getString("carImage"));
    // currentOrder.setStartLocation(jsonobj.getString("startLocation"));
    // currentOrder.setEndLocation(jsonobj.getString("endLocation"));
    // } catch (JSONException e) {
    //
    // e.printStackTrace();
    // }
    // return currentOrder;
    // }

    // Get Actual Fare
    public static String getActualFare(String json) {
        try {
            JSONObject object = new JSONObject(json);
            return object.getString("actualFare");
        } catch (Exception e) {

            return "";
        }
    }

    // Get Actual Fare
    public static String getMissingFare(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            return jsonobj.getString("data");
        } catch (Exception e) {

            return "";
        }
    }

    // Get Driver Is Active from login
    public static boolean paymentIsPaypal(JSONObject json) {
        try {
            if (json.getString(PAYPAL_RESPONDE_TYPE).equals("payment")) {
                return true;
            }

        } catch (Exception e) {

        }
        return false;
    }

    public static String getTransactionFromPaypal(JSONObject json) {
        try {
            JSONObject proof_of_payment = json
                    .getJSONObject(PAYPAY_RESONDE);

            return proof_of_payment.getString("state")
                    + proof_of_payment.getString("id");
        } catch (Exception e) {

        }
        return "";
    }

    public static String getTransactionFromCart(JSONObject json) {
        try {
            JSONObject proof_of_payment = json
                    .getJSONObject("proof_of_payment");
            JSONObject rest_api = proof_of_payment.getJSONObject("rest_api");
            return rest_api.getString("state")
                    + rest_api.getString("payment_id");
        } catch (Exception e) {

        }
        return "";
    }

    // GET DISTANCE
    public static String getDistance(String json) {
        try {
            JSONObject object = new JSONObject(json);
            return object.getString("data");
        } catch (Exception e) {
            return "";
        }
    }

    public static String parseOrderIdFromDriverConfirm(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            return jsonobj.getString("tripId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Check trip status
    public static String parseTripStatus(String json) {
        String action = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray(LinkApi.KEY_JSON_DATA);
            JSONObject itemJson;
            itemJson = jsonArray.getJSONObject(0);
            return itemJson.getString("status");
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return action;
    }

    public static String parseTripPassengerRate(String json) {
        String action = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray(LinkApi.KEY_JSON_DATA);
            JSONObject itemJson;
            itemJson = jsonArray.getJSONObject(0);
            return itemJson.getString("passengerRate");
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return action;
    }

    public static String parseTripDriverRate(String json) {
        String action = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray(LinkApi.KEY_JSON_DATA);
            JSONObject itemJson;
            itemJson = jsonArray.getJSONObject(0);
            return itemJson.getString("driverRate");
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return action;
    }

    // Check trip status
    public static String parseTripId(String json) {
        String action = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray(LinkApi.KEY_JSON_DATA);
            JSONObject itemJson;
            itemJson = jsonArray.getJSONObject(0);
            return itemJson.getString("id");
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return action;
    }

    public static String getShareDriverBonus(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            JSONObject data = jsonobj
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            return data.getString("driver_share_bonus");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getTimeSendRequestAgain(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            JSONObject data = jsonobj
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            return data.getString("time_to_send_request_again");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getMaxTimeSendRequest(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            JSONObject data = jsonobj
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            return data.getString("max_time_send_request");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getSharePassengerBonus(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            JSONObject data = jsonobj
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            return data.getString("passenger_share_bonus");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getPhoneAdmin(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            JSONObject data = jsonobj
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            return data.getString("admin_phone_number");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getMinRedeem(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            JSONObject data = jsonobj
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            return data.getString("min_redeem_amount");
        } catch (Exception e) {
            return "";
        }
    }

    public static SettingObj getGeneralSettings(String json) {
        SettingObj settingObj = new SettingObj();
        try {
            JSONObject jsonobj = new JSONObject(json);
            JSONObject data = jsonobj
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            settingObj.setTime_to_send_request_again(data.getString("time_to_send_request_again"));
            settingObj.setMax_time_send_request(data.getString("max_time_send_request"));
            settingObj.setEstimate_fare_speed(data.getString("estimate_fare_speed"));
            settingObj.setPpm_of_link_i(data.getString("ppm_of_link_i"));
            settingObj.setPpm_of_link_ii(data.getString("ppm_of_link_ii"));
            settingObj.setPpm_of_link_iii(data.getString("ppm_of_link_iii"));
            settingObj.setPpk_of_link_i(data.getString("ppk_of_link_i"));
            settingObj.setPpk_of_link_ii(data.getString("ppk_of_link_ii"));
            settingObj.setPpk_of_link_iii(data.getString("ppk_of_link_iii"));
            settingObj.setSf_of_link_i(data.getString("sf_of_link_i"));
            settingObj.setSf_of_link_ii(data.getString("sf_of_link_ii"));
            settingObj.setSf_of_link_iii(data.getString("sf_of_link_iii"));
            return settingObj;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMinTranfer(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            JSONObject data = jsonobj
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            return data.getString("min_transfer_amount");
        } catch (Exception e) {
            return "";
        }
    }

    public static double getDriverEarn(String json) {
        try {
            JSONObject jsonobj = new JSONObject(json);
            JSONObject data = jsonobj
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            return data.getDouble("driver_earn");
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static boolean isDriverFromSplash(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.getJSONObject(LinkApi.KEY_JSON_DATA)
                    .getJSONObject("driver").getString("isActive") != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

        }
        return false;
    }

    public static boolean driverIsActiveFromSplash(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.getJSONObject(LinkApi.KEY_JSON_DATA)
                    .getJSONObject("driver").getString("isActive").equals("1")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

        }
        return false;
    }

    public static boolean isShopFromSplash(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.getJSONObject(LinkApi.KEY_JSON_DATA)
                    .getJSONObject("shop").getString("isActive") != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

        }
        return false;
    }

    public static boolean shopIsActiveFromSplash(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.getJSONObject(LinkApi.KEY_JSON_DATA)
                    .getJSONObject("shop").getString("isActive").equals("1")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

        }
        return false;
    }

    // Get Id drivers number
    public static String getDriverCount(String json) {
        try {
            JSONObject object = new JSONObject(json);
            return object.getString(LinkApi.KEY_JSON_COUNT) + "";
        } catch (Exception e) {
            return "0";
        }
    }

    public static LocationDriverObj parseLocationDriver(String json) {
        LocationDriverObj user = new LocationDriverObj();
        try {
            JSONObject jsonobj = new JSONObject(json)
                    .getJSONObject(LinkApi.KEY_JSON_DATA);
            user.setDriverId(jsonobj.getString("driverId"));
            user.setLatitude(jsonobj.getString("driverLat"));
            user.setLongitude(jsonobj.getString("driverLon"));
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return user;
    }

    public static UserOnlineObj parseDriver(String json) {
        UserOnlineObj userOnlineObj = new UserOnlineObj();
        ArrayList<DriverOnlineObj> listDrivers = new ArrayList<>();
        DriverOnlineObj user;

        try {
            JSONObject jsonobj = new JSONObject(json);
            userOnlineObj.setCount(jsonobj.getString(LinkApi.KEY_JSON_COUNT));

            JSONArray array = jsonobj.getJSONArray(LinkApi.KEY_JSON_DATA);
            for (int i = 0; i < array.length(); i++) {
                user = new DriverOnlineObj();
                JSONObject object = array.getJSONObject(i);
                user.setFullName(object.getString("fullName"));
                user.setDriverId(object.getString("driverId"));
                user.setImage(object.getString("image"));
                user.setGender(object.getString("gender"));
                user.setDescription(object.getString("description"));
                user.setPhone(object.getString("phone"));
                user.setLat(object.getString("lat"));
                user.setLongitude(object.getString("long"));
                user.setRate(object.getString("rate"));
                user.setRateCount(object.getString("rateCount"));
                listDrivers.add(user);
            }
            userOnlineObj.setDriverOnlineObj(listDrivers);
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return userOnlineObj;
    }

    public static ArrayList<ProductObj> parseListProduct(
            String json) {
        ArrayList<ProductObj> arrProduct = new ArrayList<ProductObj>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray(LinkApi.KEY_JSON_DATA);
            JSONObject itemJson;
            ProductObj itemProduct;

            for (int i = 0; i < jsonArray.length(); i++) {
                itemProduct = new ProductObj();
                itemJson = jsonArray.getJSONObject(i);
                itemProduct.setId(itemJson.getString("id"));
                itemProduct.setTitle(itemJson.getString("title"));
                itemProduct.setPrice(itemJson.getString("price"));
                itemProduct.setImage(itemJson.getString("image"));
                itemProduct.setCategoryId(itemJson.getString("categoryId"));
                itemProduct.setDescription(itemJson.getString("description"));
                itemProduct.setStatus(itemJson.getString("status"));
                arrProduct.add(itemProduct);
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }
        return arrProduct;
    }

    public static ArrayList<ProductObj> parseListProductByCategory(
            String json) {
        ArrayList<ProductObj> arrProduct = new ArrayList<ProductObj>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject
                    .getJSONArray(LinkApi.KEY_JSON_DATA);
            JSONObject itemJson;
            ProductObj itemProduct;
            for (int i = 0; i < jsonArray.length(); i++) {
                itemProduct = new ProductObj();
                itemJson = jsonArray.getJSONObject(i);
                itemProduct.setId(itemJson.getString("id"));
                itemProduct.setTitle(itemJson.getString("title"));
                itemProduct.setPrice(itemJson.getString("price"));
                itemProduct.setImage(itemJson.getString("image"));
                itemProduct.setCategoryId(itemJson.getString("categoryId"));
                itemProduct.setDescription(itemJson.getString("description"));
                itemProduct.setStatus(itemJson.getString("status"));


                itemProduct.setShopName(itemJson.getString("shopName"));
                itemProduct.setPhone(itemJson.getString("phone"));
                itemProduct.setAddress(itemJson.getString("address"));
                itemProduct.setRate(itemJson.getString("rate"));
                itemProduct.setRateCount(itemJson.getString("rateCount"));
                itemProduct.setShipFee(itemJson.getString("shipFee"));
                itemProduct.setTotalPrice(itemJson.getString("totalPrice"));
                itemProduct.setDistance(itemJson.getString("distance"));

                arrProduct.add(itemProduct);
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }
        return arrProduct;
    }

    public static ArrayList<ShopObj> parseShopByCategory(String json) {
        ArrayList<ShopObj> listShops = new ArrayList<>();
        ShopObj shopObj;

        try {
            JSONObject jsonobj = new JSONObject(json);

            JSONArray array = jsonobj.getJSONArray(LinkApi.KEY_JSON_DATA);
            for (int i = 0; i < array.length(); i++) {
                shopObj = new ShopObj();
                JSONObject object = array.getJSONObject(i);
                shopObj.setId(object.getString("id"));
                shopObj.setName(object.getString("shopName"));
                shopObj.setLatitude(object.getString("shopLat"));
                shopObj.setLongitude(object.getString("shopLong"));
                shopObj.setImgSelected(object.getString("imgSelected"));
                listShops.add(shopObj);
            }
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return listShops;
    }
}
