package model;

/**
 * DTO para vendas agregadas por produto.
 */
public class ProductSales {
    private final String productName;
    private final int    totalQuantity;

    public ProductSales(String productName, int totalQuantity) {
        this.productName   = productName;
        this.totalQuantity = totalQuantity;
    }

    public String getProductName() {
        return productName;
    }
    public int getTotalQuantity() {
        return totalQuantity;
    }
}
