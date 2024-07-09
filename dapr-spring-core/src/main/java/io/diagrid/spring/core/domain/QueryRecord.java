package io.diagrid.spring.core.domain;

public record QueryRecord(String key, byte[] value, 
                    boolean isBoolean, String insertDate, 
                    String updateDate, String expireDate){}
