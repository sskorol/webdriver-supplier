package io.github.sskorol.testcases;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static io.github.sskorol.listeners.BaseListener.getDriverMetaData;

public class FactoryTests1 {

    @BeforeMethod
    public void setUp() {
    }

    @Test
    public void test1() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test2() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test3() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test4() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }
}
