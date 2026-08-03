package edu.suibe.evidence.service;

public record ChainReceipt(
    String txId,
    long blockHeight,
    String responsePayload,
    String provider,
    String mode,
    String status) {}
