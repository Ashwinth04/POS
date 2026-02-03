package com.increff.pos.service;

import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderData;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FoBuilder {

    public static String build(OrderData order) {
        StringBuilder fo = new StringBuilder();

        // Fix for Instant formatting: Provide a ZoneId (System Default or Asia/Kolkata)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a")
                .withZone(ZoneId.systemDefault());

        String formattedTime = formatter.format(order.getOrderTime());

        fo.append("""
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="A4"
            page-height="29.7cm" page-width="21cm" margin="1.5cm">
          <fo:region-body margin-top="1cm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>

      <fo:page-sequence master-reference="A4">
        <fo:flow flow-name="xsl-region-body">
          
          <fo:table table-layout="fixed" width="100%%" space-after="25pt">
            <fo:table-column column-width="50%%"/>
            <fo:table-column column-width="50%%"/>
            <fo:table-body>
              <fo:table-row>
                <fo:table-cell>
                  <fo:block font-size="26pt" font-weight="bold" color="#1a2a3a" font-family="Helvetica">
                    INVOICE
                  </fo:block>
                  <fo:block font-size="9pt" color="#7f8c8d" space-before="2pt">
                    Thank you for your purchase.
                  </fo:block>
                </fo:table-cell>
                <fo:table-cell text-align="right">
                  <fo:block font-size="9pt" font-weight="bold" color="#95a5a6">ORDER ID</fo:block>
                  <fo:block font-size="11pt" font-weight="bold" space-after="8pt">#%s</fo:block>
                  <fo:block font-size="9pt" font-weight="bold" color="#95a5a6">DATE</fo:block>
                  <fo:block font-size="11pt">%s</fo:block>
                </fo:table-cell>
              </fo:table-row>
            </fo:table-body>
          </fo:table>

          <fo:table table-layout="fixed" width="100%%" border-collapse="collapse">
            <fo:table-column column-width="40%%"/>
            <fo:table-column column-width="20%%"/>
            <fo:table-column column-width="20%%"/>
            <fo:table-column column-width="20%%"/>

            <fo:table-header>
              <fo:table-row background-color="#1a2a3a">
                <fo:table-cell padding="10pt" border="1pt solid #1a2a3a">
                  <fo:block font-weight="bold" font-size="10pt" color="white">DESCRIPTION / BARCODE</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="10pt" border="1pt solid #1a2a3a" text-align="right">
                  <fo:block font-weight="bold" font-size="10pt" color="white">QTY</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="10pt" border="1pt solid #1a2a3a" text-align="right">
                  <fo:block font-weight="bold" font-size="10pt" color="white">PRICE</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="10pt" border="1pt solid #1a2a3a" text-align="right">
                  <fo:block font-weight="bold" font-size="10pt" color="white">TOTAL</fo:block>
                </fo:table-cell>
              </fo:table-row>
            </fo:table-header>

            <fo:table-body>
    """.formatted(order.getOrderId(), formattedTime));

        double grandTotal = 0;

        for (OrderItem item : order.getOrderItems()) {
            double total = item.getOrderedQuantity() * item.getSellingPrice();
            grandTotal += total;

            fo.append("""
              <fo:table-row border-bottom="1pt solid #eeeeee">
                <fo:table-cell padding="12pt 10pt">
                  <fo:block font-size="10pt" color="#2c3e50">%s</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="12pt 10pt" text-align="right">
                  <fo:block font-size="10pt" color="#2c3e50">%d</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="12pt 10pt" text-align="right">
                  <fo:block font-size="10pt" color="#2c3e50">&#8377;%.2f</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="12pt 10pt" text-align="right">
                  <fo:block font-size="10pt" font-weight="bold" color="#2c3e50">&#8377;%.2f</fo:block>
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

          <fo:table table-layout="fixed" width="100%%" space-before="10pt">
            <fo:table-column column-width="70%%"/>
            <fo:table-column column-width="30%%"/>
            <fo:table-body>
              <fo:table-row>
                <fo:table-cell padding="12pt" text-align="right">
                  <fo:block font-size="12pt" font-weight="bold" color="#1a2a3a">GRAND TOTAL</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="12pt" text-align="right" background-color="#f4f6f7">
                  <fo:block font-size="14pt" font-weight="bold" color="#1a2a3a">&#8377;%.2f</fo:block>
                </fo:table-cell>
              </fo:table-row>
            </fo:table-body>
          </fo:table>

          <fo:block font-size="8pt" color="#bdc3c7" text-align="center" space-before="60pt">
            Computer Generated Invoice - No Signature Required
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
    """.formatted(grandTotal));

        return fo.toString();
    }
}