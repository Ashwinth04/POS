package com.increff.service;

import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderData;

public class FoBuilder {

    public static String build(OrderData order) {
        StringBuilder fo = new StringBuilder();

        fo.append("""
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="A4"
                page-height="29.7cm" page-width="21cm" margin="2cm">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>

          <fo:page-sequence master-reference="A4">
            <fo:flow flow-name="xsl-region-body">

              <fo:block font-size="18pt" font-weight="bold">Invoice</fo:block>
        """);

        fo.append("""
              <fo:block>Order ID: %s</fo:block>
              <fo:block>Status: %s</fo:block>
              <fo:block space-after="10pt"/>
        """.formatted(order.getId(), order.getOrderStatus()));

        fo.append("""
              <fo:table table-layout="fixed" width="100%" border="1pt solid black">
                <fo:table-header>
                  <fo:table-row>
                    <fo:table-cell><fo:block font-weight="bold">Barcode</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-weight="bold">Qty</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-weight="bold">Price</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block font-weight="bold">Total</fo:block></fo:table-cell>
                  </fo:table-row>
                </fo:table-header>
                <fo:table-body>
        """);

        double grandTotal = 0;

        for (OrderItem item : order.getOrderItems()) {
            double total = item.getOrderedQuantity() * item.getSellingPrice();
            grandTotal += total;

            fo.append("""
                  <fo:table-row>
                    <fo:table-cell><fo:block>%s</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>%d</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>%.2f</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>%.2f</fo:block></fo:table-cell>
                  </fo:table-row>
            """.formatted(
                    item.getBarcode(),
                    item.getOrderedQuantity(),
                    item.getSellingPrice(),
                    total
            ));
        }

        fo.append("""
                </fo:table-body>
              </fo:table>
        """);

        fo.append("""
              <fo:block space-before="10pt" font-weight="bold">
                Grand Total: %.2f
              </fo:block>
        """.formatted(grandTotal));

        fo.append("""
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """);

        return fo.toString();
    }
}
