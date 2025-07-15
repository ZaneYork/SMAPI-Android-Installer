package com.abdurazaaqmohammed.AntiSplit.main;

public class MismatchedSplitsException extends Exception {
    public MismatchedSplitsException(String cancelled) {
        super(cancelled);
    }
}
