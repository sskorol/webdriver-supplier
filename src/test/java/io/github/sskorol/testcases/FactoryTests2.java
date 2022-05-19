package io.github.sskorol.testcases;

import org.testng.annotations.Test;

import static io.github.sskorol.listeners.BaseListener.getDriverMetaData;

public class FactoryTests2 {

    @Test
    public void test5() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test6() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test7()  {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }

    @Test
    public void test8() {
        getDriverMetaData().getWebDriver().get("https://google.com/ncr");
    }
}
