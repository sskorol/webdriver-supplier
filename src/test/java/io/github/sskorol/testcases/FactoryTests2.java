package io.github.sskorol.testcases;

import org.testng.annotations.Test;

import static io.github.sskorol.listeners.BaseListener.getDriverMetaData;

public class FactoryTests2 {

    @Test
    public void test5() {
        getDriverMetaData()._1.get("https://google.com/ncr");
    }

    @Test
    public void test6() {
        getDriverMetaData()._1.get("https://google.com/ncr");
    }

    @Test
    public void test7()  {
        getDriverMetaData()._1.get("https://google.com/ncr");
    }

    @Test
    public void test8() {
        getDriverMetaData()._1.get("https://google.com/ncr");
    }
}
