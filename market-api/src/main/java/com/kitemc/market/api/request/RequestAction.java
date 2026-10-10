package com.kitemc.market.api.request;

/** The only SDK-requestable trade actions. No wallet minting, administrator or inventory actions. */
public enum RequestAction { CREATE, BUY, SUPPLY, BID, CANCEL }
