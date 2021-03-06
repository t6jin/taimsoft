package com.taim.dto;

import com.taim.dto.basedtos.BaseModelDTO;
import javafx.beans.property.*;

public class ProductDTO extends BaseModelDTO{
    private StringProperty sku;
    private DoubleProperty length;
    private DoubleProperty width;
    private DoubleProperty height;
    private StringProperty displayName;
    private StringProperty picUrl;
    private StringProperty texture;
    private DoubleProperty totalNum;
    private DoubleProperty virtualTotalNum;
    private DoubleProperty unitPrice;
    private DoubleProperty stockUnitPrice;
    private BooleanProperty checked;
    private IntegerProperty piecePerBox;

    public ProductDTO(){
        unitPrice = new SimpleDoubleProperty();
        stockUnitPrice = new SimpleDoubleProperty();
        sku = new SimpleStringProperty();
        length = new SimpleDoubleProperty();
        width = new SimpleDoubleProperty();
        height = new SimpleDoubleProperty();
        displayName = new SimpleStringProperty();
        picUrl = new SimpleStringProperty();
        texture = new SimpleStringProperty();
        totalNum = new SimpleDoubleProperty();
        virtualTotalNum = new SimpleDoubleProperty();
        checked = new SimpleBooleanProperty();
        piecePerBox = new SimpleIntegerProperty();
    }

    public String getSku() {
        return sku.get();
    }

    public StringProperty skuProperty() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku.set(sku);
    }

    public double getLength() {
        return length.get();
    }

    public DoubleProperty lengthProperty() {
        return length;
    }

    public void setLength(double length) {
        this.length.set(length);
    }

    public double getWidth() {
        return width.get();
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double width) {
        this.width.set(width);
    }

    public double getHeight() {
        return height.get();
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public void setHeight(double height) {
        this.height.set(height);
    }

    public String getDisplayName() {
        return displayName.get();
    }

    public StringProperty displayNameProperty() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName.set(displayName);
    }

    public String getPicUrl() {
        return picUrl.get();
    }

    public StringProperty picUrlProperty() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl.set(picUrl);
    }

    public String getTexture() {
        return texture.get();
    }

    public StringProperty textureProperty() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture.set(texture);
    }

    public double getTotalNum() {
        return totalNum.get();
    }

    public DoubleProperty totalNumProperty() {
        return totalNum;
    }

    public void setTotalNum(double totalNum) {
        this.totalNum.set(totalNum);
    }

    public double getUnitPrice() {
        return unitPrice.get();
    }

    public DoubleProperty unitPriceProperty() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice.set(unitPrice);
    }

    public boolean isChecked() {
        return checked.get();
    }

    public BooleanProperty checkedProperty() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked.set(checked);
    }

    public double getVirtualTotalNum() {
        return virtualTotalNum.get();
    }

    public int getPiecePerBox() {
        return piecePerBox.get();
    }

    public IntegerProperty piecePerBoxProperty() {
        return piecePerBox;
    }

    public void setPiecePerBox(int piecePerBox) {
        this.piecePerBox.set(piecePerBox);
    }

    public DoubleProperty virtualTotalNumProperty() {
        return virtualTotalNum;
    }

    public void setVirtualTotalNum(double virtualTotalNum) {
        this.virtualTotalNum.set(virtualTotalNum);
    }

    public double getStockUnitPrice() {
        return stockUnitPrice.get();
    }

    public DoubleProperty stockUnitPriceProperty() {
        return stockUnitPrice;
    }

    public void setStockUnitPrice(double stockUnitPrice) {
        this.stockUnitPrice.set(stockUnitPrice);
    }
}
