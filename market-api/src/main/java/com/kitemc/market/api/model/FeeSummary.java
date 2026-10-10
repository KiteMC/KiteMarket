package com.kitemc.market.api.model;

import java.util.Objects;

/** Publication-time fee schedule. Fixed amounts use minor units; rates use basis points. */
public final class FeeSummary {
  private final long listingFixed, sellerFixed, buyerFixed;
  private final int listingBasisPoints, sellerBasisPoints, buyerBasisPoints;
  private final String ruleId;
  public FeeSummary(long listingFixed, int listingBasisPoints, long sellerFixed, int sellerBasisPoints,
      long buyerFixed, int buyerBasisPoints, String ruleId) {
    if (listingFixed < 0 || sellerFixed < 0 || buyerFixed < 0
        || listingBasisPoints < 0 || listingBasisPoints >= 10000
        || sellerBasisPoints < 0 || sellerBasisPoints >= 10000
        || buyerBasisPoints < 0 || buyerBasisPoints >= 10000)
      throw new IllegalArgumentException("INVALID_FEE_SUMMARY");
    this.listingFixed = listingFixed;
    this.listingBasisPoints = listingBasisPoints;
    this.sellerFixed = sellerFixed;
    this.sellerBasisPoints = sellerBasisPoints;
    this.buyerFixed = buyerFixed;
    this.buyerBasisPoints = buyerBasisPoints;
    this.ruleId = Objects.requireNonNull(ruleId);
  }
  public long getListingFixed() { return listingFixed; }
  public int getListingBasisPoints() { return listingBasisPoints; }
  public long getSellerFixed() { return sellerFixed; }
  public int getSellerBasisPoints() { return sellerBasisPoints; }
  public long getBuyerFixed() { return buyerFixed; }
  public int getBuyerBasisPoints() { return buyerBasisPoints; }
  public String getRuleId() { return ruleId; }
}
