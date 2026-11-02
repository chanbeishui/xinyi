package com.xinyi.rongli.order.dto;
public record OrderCreate(String quoteId,String termsVersion,String privacyVersion,boolean acceptedTerms,String remark) { }
