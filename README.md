# WebDriver Supplier 

[![Build Status](https://travis-ci.org/sskorol/webdriver-supplier.svg?branch=master)](https://travis-ci.org/sskorol/webdriver-supplier)
[![codebeat badge](https://codebeat.co/badges/a0385906-b9a9-4287-b07f-70584e1e0702)](https://codebeat.co/projects/github-com-sskorol-webdriver-supplier-master)
[![codecov](https://codecov.io/gh/sskorol/webdriver-supplier/branch/master/graph/badge.svg)](https://codecov.io/gh/sskorol/webdriver-supplier)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.sskorol/webdriver-supplier/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.github.sskorol/webdriver-supplier)
[![GitHub license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://goo.gl/9GLmMZ)
[![Twitter](https://img.shields.io/twitter/url/https/github.com/sskorol/webdriver-supplier.svg?style=social)](https://twitter.com/intent/tweet?text=Check%20new%20WebDriver%20Supplier%20library:&url=https://github.com/sskorol/webdriver-supplier)

This repository contains a web browser management tool.

## Motivation

You know that WebDriver API helps to control browsers' behavior. However, Selenium is a pretty low level library, 
which can't protect users against common mistakes like NPE or race condition. 

Browser management is not a trivial task, especially when tests must be scaled across different VMs or containers.

Ideally, you should carefully think about architecture design to build a reliable module, 
which will automatically handle browsers' setup / cleanup activities, provide thread safety and reduce configuration overhead.

This library is intended to simplify and encapsulate all these processes from QA engineers, 
so that they could concentrate on more high level tasks.

## Installation

### Java 11 - Gradle

Add the following configuration into **build.gradle**:

```groovy
repositories {
    mavenCentral()
}

sourceCompatibility = JavaVersion.VERSION_11
targetCompatibility = JavaVersion.VERSION_11
    
dependencies {
    compile(
            'org.testng:testng:7.5',
            'io.github.sskorol:webdriver-supplier:0.9.3'
    )
}
    
test {
    useTestNG() {
        listeners << 'io.github.sskorol.listeners.BeforeMethodListener'
    }
}
```

### Java 17 - Gradle

```groovy
repositories {
    mavenCentral()
}

sourceCompatibility = JavaVersion.VERSION_17
targetCompatibility = JavaVersion.VERSION_17
    
dependencies {
    compile(
            'org.testng:testng:7.6.0',
            'io.github.sskorol:webdriver-supplier:1.1.3'
    )
}
    
test {
    useTestNG() {
        listeners << 'io.github.sskorol.listeners.BeforeMethodListener'
    }
}
```

### Java 11 - Maven

Add the following configuration into **pom.xml**:

```xml
<dependencies>
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <version>7.5</version>
    </dependency>
    <dependency>
        <groupId>io.github.sskorol</groupId>
        <artifactId>webdriver-supplier</artifactId>
        <version>0.9.3</version>
    </dependency>
</dependencies>
    
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.10.1</version>
            <configuration>
                <source>11</source>
                <target>11</target>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.0.0-M6</version>
            <configuration>
                <properties>
                    <property>
                        <name>listener</name>
                        <value>io.github.sskorol.listeners.BeforeMethodListener</value>
                    </property>
                </properties>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Java 17 - Maven

```xml
<dependencies>
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <version>7.6.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.sskorol</groupId>
        <artifactId>webdriver-supplier</artifactId>
        <version>1.1.3</version>
    </dependency>
</dependencies>
    
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.10.1</version>
            <configuration>
                <source>17</source>
                <target>17</target>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.0.0-M6</version>
            <configuration>
                <properties>
                    <property>
                        <name>listener</name>
                        <value>io.github.sskorol.listeners.BeforeMethodListener</value>
                    </property>
                </properties>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Dependencies

### TestNG

**WebDriver Supplier** is driven by TestNG framework behind the scene. So basically you'll need to add a special 
**BeforeMethodListener** into build's configuration file, as described in installation section.

This listener will automatically raise a new web browser instance before a new test has started, 
and close it when test has finished.

### Selenium

As web browsers are managed by WebDriver API, Selenium 4 is required for proper work. Note that library currently supports 
only the following list of browsers (in both local and remote mode):

 - Chrome
 - Firefox
 - Internet Explorer
 - Edge

### WebDriver Manager

To reduce an overhead with WebDrivers' binaries downloading and configuration, 
[webdrivermanager](https://github.com/bonigarcia/webdrivermanager) library was used. 
Note that it's required only for local tests execution.

## Browser configuration

**WebDriver Supplier** uses [SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) mechanism to provide 
browsers' implementations to the library core.

Basically, you'll need to create a new class, which implements a special **Browser** interface.

```java
public class Chrome implements Browser {
        
    public Name name() {
        return Name.Chrome;
    }
}
```

By default, only `name()` method should be overridden. You can pick one from the following enum:

```java
enum Name {
    Chrome("chrome"),
    Firefox("firefox"),
    InternetExplorer("ie"),
    Edge("edge"),
    Remote("remote");
    //...
}
```

Note that `Remote` constant is for internal usage only. If you want to run your tests remotely, you should override 
`isRemote()` method:

```java
public boolean isRemote() {
    return true;
}
```

If your hub's address differs from *localhost*, override the following:

```java
public String url() {
    return "http://ip:port/wd/hub";
}
```

Finally, you can provide your own set of capabilities or options:

```java
public Capabilities configuration(final XmlConfig config) {
    final DesiredCapabilities caps = new DesiredCapabilities();
    //...
    return caps;
}

public Capabilities configuration(final XmlConfig config) {
    final ChromeOptions options = new ChromeOptions();
    //...
    return merge(config, options);
}
```

```XmlConfig``` is a wrapper for TestNG suite. It gives an access to common browser configuration defined as parameters: 
**browserName**, **version** and **platform**. Besides that, you can also retrieve current test name. 

Note that you have to provide at least browser name on any of the following levels:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd" >
<suite name="Suite name">
	<!-- Suite level parameters -->
	<parameter name="browserName" value="chrome"/>
	<parameter name="browserVersion" value="1.1.0"/>
	<parameter name="platformName" value="MAC"/>
		
	<test name="Test block name">
		<!-- Test level parameters -->	
		<parameter name="browserName" value="firefox"/>
		<parameter name="browserVersion" value="1.1.1"/>
		<parameter name="platformName" value="LINUX"/>
		
		<classes>
			<class name="path.to.your.TestClass">
				<methods>
					<!-- Class level parameters -->
					<parameter name="browserName" value="ie"/>
					<parameter name="browserVersion" value="1.1.2"/>
					<parameter name="platformName" value="WINDOWS"/>
					
					<include name="methodName">
						<!-- Method level parameters -->
						<parameter name="browserName" value="edge"/>
						<parameter name="browserVersion" value="1.1.3"/>
						<parameter name="platformName" value="WINDOWS"/>
					</include>
				</methods>
			</class>
		</classes>
	</test>
</suite>
```

When all the levels are covered, only the lowest one is used. Configuration lookup goes in the following order: 
methods, classes, tests, suites. If there was no **browserName** parameter found, corresponding test(-s) will be skipped.

Independently of browser's parameters location in xml, new instances will always be raised in **before method** 
configuration, and closed when test execution is finished. Assuming deep scaling support, it's not reasonable to open 
browsers in before class, test group or suite configuration.
 
Note that in case of custom capabilities' usage, it's recommended to merge them with defaults:

```java
public Capabilities configuration(final XmlConfig config) {
    final DesiredCapabilities caps = new DesiredCapabilities();
    //...
    return merge(config, caps);
}
```

`defaultConfiguration` method automatically handles browser-specific xml parameters, and creates corresponding capabilities. 
So you don't need to set browserName / version / platform explicitly in code.

When you finished with configuration staff, newly created browsers' implementation classes must be linked with a `Browser` SPI. 
According to official Oracle documentation, you should create **META-INF/services** folder within **resources** root, 
and put an SPI-named file **io.github.sskorol.core.Browser** there with a list of implementation classes:

```text
your.full.path.to.Chrome
your.full.path.to.Firefox
your.full.path.to.Edge
your.full.path.to.Chrome
your.full.path.to.IE
```  

Note that it's not required to add all the implementations you have. But only specified items will be tracked by the 
library internals.

## Factory

**WebDriver Supplier** has a preceded factory. So you don't need to care of browsers' initialization staff. 
New instances will be automatically created depending on specified `Browser.Name` and `isRemote` flag in SPI 
implementation classes.

But if you still want to manage WebDrivers manually, you can provide your own factory implementation. 
This is handled by SPI mechanism as well. You just need to implement `WebDriverProvider` interface and put its reference 
into **META-INF/services/io.github.sskorol.core.WebDriverProvider**. The same way it was described for `Browser` SPI.

By default you have to override the following methods:

```java
String label();
    
WebDriver createDriver(Browser browser, XmlConfig config);
```

With `label` you can define a unique factory name, so that it could be identified by library internals. 

`createDriver` allows you getting actual info retrieved from `Browser` SPI implementation classes and TestNG xml.

```java
public String label() {
    return "customFactory";
}
    
public WebDriver createDriver(final Browser browser, final XmlConfig config) {
    return Match(browser.name()).of(
            Case($(Browser.Name.Firefox), () -> new FirefoxDriver()),
            Case($(Browser.Name.Chrome), () -> new ChromeDriver())
    );
}
```

## WebDriver access

Newly raised `WebDriver` instance can be retrieved via `getDriverMetaData` method call. Just add the following import 
to get full access to `Tuple2<WebDriver, WebDriverWait>`:

```java
import static io.github.sskorol.listeners.BaseListener.getDriverMetaData;
    
public abstract class BasePage {
        
    private final WebDriver driver;
    private final WebDriverWait wait;
        
    public BasePage() {
        final WebDriverContainer wdMeta = getDriverMetaData();
        this.driver = wdMeta.getWebDriver();
        this.wait = wdMeta.getWebDriverWait();
    }
}
```

By default `WebDriverWait` is configured to wait for 10 sec until throwing a timeout exception. But you can override this 
option via **wd.wait.timeout** system property. It could be set either on configuration level in build.gradle / pom.xml, 
or by putting **webdriver.properties** with the same record into classpath.

## Chrome DevTools Protocol

Since 1.0.0 version `webdriver-supplier` introduces initial CDP support. Basically, your browser class should
implement `CDP` interface and override the following method to enable this feature:

```java
public String cdpWebSocketUrl(final String sessionId) {
    return format("ws://localhost:4444/devtools/%s/page", sessionId);
}
```

The above example uses Selenoid to connect to the open websocket within browser's container.

Next, you can access `ChromeDevToolsService` from within `WebDriverContainer`:

```java
final ChromeDevToolsService cdp = getDriverMetaData().getDevToolsService();
```

## Custom parameters

If you want to access custom parameters from `testng.xml` (e.g. as a feature toggling technique), you can use the following:

```xml
<classes>
    <class name="path.to.your.TestClass">
        <parameter name="feature1" value="value1"/>
    </class>
</classes>
```

Then you can access `XmlConfig` object with all scoped parameters from within WS container instance:
```java
final WebDriverContainer wdMeta = getDriverMetaData();
final XmlConfig config = wdMeta.getConfig();
Optional.ofNullable(config.getValue("feature1"))
    .ifPresent(System.out::println);
```

## SessionId access

Sometimes it might be useful to retrieve a current session id from `RemoteWebDriver` instance. For example,
[Selenoid](http://aerokube.com/selenoid/latest/#_video_recording) uses session id as a default name for video recording.

**webdriver-supplier** automatically injects `sessionId` as a custom TestNG attribute. It allows getting 
corresponding access in e.g. `afterInvocation` event listener.

```java
@Slf4j
public class SessionListener implements IInvokedMethodListener {
    //...
            
    @Override
    public void afterInvocation(final IInvokedMethod method, final ITestResult testResult) {
        if (method.isTestMethod()) {
            log.info("Session ID = {}", testResult.getAttribute("sessionId"));
        }
    }
}
```

## Taking screenshots

Screenshots could be enabled either via boolean **wd.take.screenshot** system property or by putting **webdriver.properties** 
with the same record into classpath.

For getting access to screenshots' content, it's required to implement the following SPI:

```java
public class Screenshooter implements ScreenshotConsumer {
        
    @Override
    public void handle(final byte[] screenshot, final ITestResult testResult) {
        if (nonNull(screenshot) && testResult.getStatus() == FAILURE) {
            // attach screenshot
        }
    }
}

```

Similar to `Browser` SPI, it should be added into **META-INF/services/io.github.sskorol.core.ScreenshotConsumer**.

## WebDriver properties

**webdriver.properties** file, put into classpath, allows defining or overriding the following settings:

```text
wd.wait.timeout = 10 (in sec)
wd.screen.resolution = max (accepts WxH format, e.g. 1280x768)
wd.take.screenshot = false
```   

## Full example

To establish connection with [Selenoid](http://aerokube.com/selenoid/latest) hub and Firefox node containers 
raised on localhost, you can use the following configuration:

##### build.gradle

```groovy
repositories {
    mavenCentral()
}

sourceCompatibility = JavaVersion.VERSION_17
targetCompatibility = JavaVersion.VERSION_17
    
dependencies {
    compile('org.testng:testng:7.6.0',
            'io.github.sskorol:webdriver-supplier:1.1.1'
    )
}
    
test {
    useTestNG() {
        listeners << 'io.github.sskorol.listeners.BeforeMethodListener'
        suites 'src/test/resources/smoke-suite.xml'
    }
}
```

##### Chrome.java

```java
public class Chrome implements Browser, CDP {
    
    public Name name() {
        return Name.Chrome;
    }
    
    public boolean isRemote() {
        return true;
    }
    
    public Capabilities configuration(final XmlConfig config) {
        final ChromeOptions options = new ChromeOptions();
        options.setCapability("enableVNC", true);
        options.setCapability("enableVideo", true);
        options.setCapability("name", config.getTestName());
        options.setCapability("screenResolution", "1280x1024x24");
        return merge(config, options);
    }

    public String cdpWebSocketUrl(final String sessionId) {
        return format("ws://localhost:4444/devtools/%s/page", sessionId);
    }
}
```

##### io.github.sskorol.core.Browser

```text
full.path.to.Chrome
```

##### smoke-suite.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd" >
<suite name="Smoke suite">
	<test name="Chrome group">
		<parameter name="browserName" value="Chrome"/>
		<parameter name="platformName" value="LINUX"/>
		<classes>
			<class name="path.to.your.testcases.SmokeTests"/>
		</classes>
	</test>
</suite>
```

##### BasePage.java

```java
import static io.github.sskorol.listeners.BaseListener.getDriverMetaData;
    
public abstract class BasePage {
        
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final ChromeDevToolsService cdp;
        
    public BasePage() {
        final WebDriverContainer wdMeta = getDriverMetaData();
        this.driver = wdMeta.getWebDriver();
        this.wait = wdMeta.getWebDriverWait();
        this.cdp = wdMeta.getDevToolsService();
    }
    
    protected void navigateTo(final String url) {
        driver.get(url);
    }    
       
    protected void click(final By locator) {
        waitFor(locator, ExpectedConditions::elementToBeClickable).click();
    }
    
    protected void type(final By locator, final CharSequence text) {
        waitFor(locator, ExpectedConditions::visibilityOfElementLocated).sendKeys(text);
    }
        
    private WebElement waitFor(final By locator, final Function<By, ExpectedCondition<WebElement>> condition) {
        return wait.until(condition.apply(locator));
    }    
}
```

That's everything you need for a quick start. Enjoy it!

## TBD

- [ ] A separate GitHub project with usage examples.
