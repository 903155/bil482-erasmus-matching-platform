package com.sangreyoro.matching.testkit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a test method. {@code id} is the traceable test id (UC2-Txx), {@code covers} the SRS items it verifies. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
    String id();

    String covers();
}
