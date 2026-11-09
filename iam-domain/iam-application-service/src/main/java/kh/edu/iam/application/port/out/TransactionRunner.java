package kh.edu.iam.application.port.out;

import java.util.function.Supplier;

public interface TransactionRunner {

    <T> T required(Supplier<T> operation);
}