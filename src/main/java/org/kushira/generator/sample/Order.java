package org.kushira.generator.sample;

import org.kushira.generator.annotation.JSONToString;

import java.math.BigDecimal;

@JSONToString
public class Order {

    private Boolean invoiceSeparate;

    private Integer totalQuantity;

    private int totalSplits;

    private String id;

    private Long deliveryDate;

    private BigDecimal totalPrice;

    private boolean isOffDay;

    private User user;

    public void setInvoiceSeparate(Boolean invoiceSeparate) {
        this.invoiceSeparate = invoiceSeparate;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public void setTotalSplits(int totalSplits) {
        this.totalSplits = totalSplits;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDeliveryDate(Long deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setOffDay(boolean offDay) {
        isOffDay = offDay;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override()
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"invoiceSeparate\":").append(invoiceSeparate).append(",");
        builder.append("\"totalQuantity\":").append(totalQuantity).append(",");
        builder.append("\"totalSplits\":").append(totalSplits).append(",");
        builder.append("\"id\":").append("\"").append(id).append("\"").append(",");
        builder.append("\"deliveryDate\":").append(deliveryDate).append(",");
        builder.append("\"totalPrice\":").append(totalPrice).append(",");
        builder.append("\"isOffDay\":").append(isOffDay).append(",");
        builder.append("\"user\":").append(user);
        builder.append("{");
        return builder.toString();
    }
}
