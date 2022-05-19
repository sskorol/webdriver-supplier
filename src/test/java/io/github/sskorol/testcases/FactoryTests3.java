package io.github.sskorol.testcases;

import org.testng.annotations.Test;

import static io.github.sskorol.listeners.BaseListener.getDriverMetaData;

public class FactoryTests3 {

    @Test
    public void test9() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test10() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test11() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test12() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }
}
