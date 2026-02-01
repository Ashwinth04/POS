package com.increff.pos.service;

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

          <!-- Title -->
          <fo:block font-size="20pt" font-weight="bold" text-align="center" space-after="10pt">
            INVOICE
          </fo:block>

          <fo:block border-bottom="1pt solid black" space-after="10pt"/>

          <!-- Order info -->
          <fo:block font-size="10pt" space-after="2pt">Order ID: %s</fo:block>
          <fo:block font-size="10pt" space-after="10pt">Status: %s</fo:block>
    """.formatted(order.getOrderId(), order.getOrderStatus()));

        fo.append("""
          <!-- Table -->
          <fo:table table-layout="fixed" width="100%" border-collapse="collapse">
            <fo:table-column column-width="40%"/>
            <fo:table-column column-width="20%"/>
            <fo:table-column column-width="20%"/>
            <fo:table-column column-width="20%"/>

            <fo:table-header>
              <fo:table-row border-bottom="1pt solid black">
                <fo:table-cell padding="6pt">
                  <fo:block font-weight="bold">Barcode</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="6pt">
                  <fo:block font-weight="bold" text-align="right">Qty</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="6pt">
                  <fo:block font-weight="bold" text-align="right">Price</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="6pt">
                  <fo:block font-weight="bold" text-align="right">Total</fo:block>
                </fo:table-cell>
              </fo:table-row>
            </fo:table-header>

            <fo:table-body>
    """);

        double grandTotal = 0;

        for (OrderItem item : order.getOrderItems()) {
            double total = item.getOrderedQuantity() * item.getSellingPrice();
            grandTotal += total;

            fo.append("""
              <fo:table-row border-bottom="0.5pt solid black">
                <fo:table-cell padding="6pt">
                  <fo:block>%s</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="6pt">
                  <fo:block text-align="right">%d</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="6pt">
                  <fo:block text-align="right">%.2f</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="6pt">
                  <fo:block text-align="right">%.2f</fo:block>
                </fo:table-cell>
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

          <!-- Total section -->
          <fo:block border-top="1pt solid black" space-before="10pt" padding-top="6pt">
            <fo:block font-size="12pt" font-weight="bold" text-align="right">
              Grand Total: %.2f
            </fo:block>
          </fo:block>

        </fo:flow>
      </fo:page-sequence>
    </fo:root>
    """.formatted(grandTotal));

        return fo.toString();
    }

}
