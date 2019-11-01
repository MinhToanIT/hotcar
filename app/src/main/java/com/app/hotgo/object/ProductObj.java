package com.app.hotgo.object;

import android.os.Parcel;
import android.os.Parcelable;

public class ProductObj implements Parcelable {
    private String id;
    private String title;
    private String price;
    private String image;
    private String categoryId;
    private String description;
    private String status;
    private String size;

    private String shopName;
    private String phone;
    private String address;
    private String rate, rateCount;
    private String shipFee;
    private String totalPrice;
    private String distance;

    public ProductObj() {
    }

    protected ProductObj(Parcel in) {
        id = in.readString();
        title = in.readString();
        price = in.readString();
        image = in.readString();
        categoryId = in.readString();
        description = in.readString();
        status = in.readString();
        size = in.readString();
        shopName = in.readString();
        phone = in.readString();
        address = in.readString();
        rate = in.readString();
        rateCount = in.readString();
        shipFee = in.readString();
        totalPrice = in.readString();
        distance = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(price);
        dest.writeString(image);
        dest.writeString(categoryId);
        dest.writeString(description);
        dest.writeString(status);
        dest.writeString(size);
        dest.writeString(shopName);
        dest.writeString(phone);
        dest.writeString(address);
        dest.writeString(rate);
        dest.writeString(rateCount);
        dest.writeString(shipFee);
        dest.writeString(totalPrice);
        dest.writeString(distance);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProductObj> CREATOR = new Creator<ProductObj>() {
        @Override
        public ProductObj createFromParcel(Parcel in) {
            return new ProductObj(in);
        }

        @Override
        public ProductObj[] newArray(int size) {
            return new ProductObj[size];
        }
    };

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getRateCount() {
        return rateCount;
    }

    public void setRateCount(String rateCount) {
        this.rateCount = rateCount;
    }

    public String getShipFee() {
        return shipFee;
    }

    public void setShipFee(String shipFee) {
        this.shipFee = shipFee;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
