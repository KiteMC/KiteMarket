package com.kitemc.market.api.model;

/** Safe categories; unknown internal categories are represented by OTHER. */
public enum HistoryKind {
  CREATE, BUY, SUPPLY, BID, CANCEL, SALE_INCOME, SUPPLY_RECEIVED, BID_RELEASED,
  AUCTION_WON, ORDER_FINISHED, DEPOSIT, WITHDRAW, ITEM_DEPOSIT, CLAIM,
  ADMIN_CANCEL, ADMIN_RESOLVE, OTHER
}
