package QKART_TESTNG;

import QKART_TESTNG.pages.Checkout;
import QKART_TESTNG.pages.Home;
import QKART_TESTNG.pages.Login;
import QKART_TESTNG.pages.Register;
import QKART_TESTNG.pages.SearchResult;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

// @Listeners(ListenerClass.class)
public class QKART_Tests {

    static RemoteWebDriver driver;
    public static String lastGeneratedUserName;

    @BeforeSuite(alwaysRun = true)
    public static void createDriver() throws MalformedURLException {
        // Launch Browser using Zalenium
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName(BrowserType.CHROME);
        driver = new RemoteWebDriver(new URL("http://localhost:8082/wd/hub"), capabilities);
        System.out.println("createDriver()");
    }

    /*
     *  Verify a new user can successfully register
     */
    @Test(priority = 1, groups = {"Sanity_test"},
            description = "Verify registration happens correctly")
    @Parameters({"TC01_Username", "TC01_Password"})
    public void TestCase01(@Optional("testUser") String Username,
    @Optional("abc@123") String Password) throws InterruptedException {
        Boolean status;

        // Visit the Registration page and register a new user
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser(Username, Password, true);
        Assert.assertTrue(status, "Failed to register new user");

        // Save the last generated username
        lastGeneratedUserName = registration.lastGeneratedUsername;

        // Visit the login page and login with the previuosly registered user
        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, Password);
        Assert.assertTrue(status, "Failed to login with registered user");

        // Visit the home page and log out the logged in user
        Home home = new Home(driver);
        status = home.PerformLogout();

    }

    /*
     * Verify that an existing user is not allowed to re-register on QKart
     */
    @Test(priority = 2, groups = {"Sanity_test"},
            description = "Verify re-registering an already registered user fails")
    @Parameters({"TC02_Username", "TC02_Password"})
    public void TestCase02(@Optional("testUser") String Username,
            @Optional("abc@123") String Password) throws InterruptedException {
        Boolean status;

        // Visit the Registration page and register a new user
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser(Username, Password, true);
        Assert.assertTrue(status, "Verify user Registration : Failed");

        // Save the last generated username
        lastGeneratedUserName = registration.lastGeneratedUsername;

        // Visit the Registration page and try to register using the previously
        // registered user's credentials
        registration.navigateToRegisterPage();
        status = registration.registerUser(lastGeneratedUserName, Password, false);
        Assert.assertTrue(!status, "End TestCase Test Case 2: Verify user Registration : Failed");
    }

    /*
     * Verify the functinality of the search text box
     */
    @Test(priority = 3, groups = {"Sanity_test"},
            description = "Verify the functionality of search text box")
    @Parameters("TC3_ProductNameToSearchFor")
    public void TestCase03(@Optional("YONEX") String product) throws InterruptedException {
        boolean status;

        // Visit the home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();

        // Search for the "yonex" product
        status = homePage.searchForProduct(product);
        Assert.assertTrue(status, "search for given product");

        // Fetch the search results
        List<WebElement> searchResults = homePage.getSearchResults();

        // Verify the search results are available
        for (WebElement webElement : searchResults) {
            // Create a SearchResult object from the parent element
            SearchResult resultelement = new SearchResult(webElement);

            // Verify that all results contain the searched text
            String elementText = resultelement.getTitleofResult();
            Assert.assertTrue(elementText.toUpperCase().contains("YONEX"),
                    "TestCase 3:Test Case Failure. Test Results contains un-expected values: "
                            + elementText);
        }

        // Search for product
        status = homePage.searchForProduct("Gesundheit");
        Assert.assertFalse(status,
                "TestCase 3, Test Case Failure. Invalid keyword returned results");

        // Verify no search results are found
        searchResults = homePage.getSearchResults();
        Assert.assertTrue(homePage.isNoResultFound(),
                "TestCase 3: Test Case Fail. Expected: no results , actual: Results were available");
    }

    // /*
    // * Verify the presence of size chart and check if the size chart content is as expected
    // */
    @Test(priority = 4, groups = {"Regression_Test"},
            description = "Verify the existence of size chart for certain items and validate contents of size chart")
    @Parameters("TC4_ProductNameToSearchFor")
    public void TestCase04(@Optional("Roadster") String product) throws InterruptedException {
        // Visit home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();
        homePage.searchForProduct(product);
        List<WebElement> searchResults = homePage.getSearchResults();

        // Create expected values
        List<String> expectedTableHeaders =
                Arrays.asList("Size", "UK/INDIA", "EU", "HEEL TO TOE");
        List<List<String>> expectedTableBody =
                Arrays.asList(Arrays.asList("6", "6", "40", "9.8"),
                        Arrays.asList("7", "7", "41", "10.2"),
                        Arrays.asList("8", "8", "42", "10.6"),
                        Arrays.asList("9", "9", "43", "11"),
                        Arrays.asList("10", "10", "44", "11.5"),
                        Arrays.asList("11", "11", "45", "12.2"),
                        Arrays.asList("12", "12", "46", "12.6"));

        // Verify size chart presence and content matching for each search result
        for (WebElement webElement : searchResults) {
            SearchResult result = new SearchResult(webElement);

            SoftAssert sa = new SoftAssert();
            sa.assertTrue(result.verifySizeChartExists(),
                    "Test case failed Size chart link doesn't exist");
            sa.assertTrue(result.verifyExistenceofSizeDropdown(driver),
                    "Test case failed: size dropdown is not present ");
            sa.assertTrue(result.openSizechart(),
                    "Test case failed Size chart link doesn't exist");
            sa.assertTrue(result.validateSizeChartContents(expectedTableHeaders,
                            expectedTableBody, driver),
                    "Test Case Failed: while validating contents of Size Chart Link");
            sa.assertTrue(result.closeSizeChart(driver),
                    "End Test Case: Validated Size Chart Details");
            sa.assertAll();
        }
    }
    /*
     * Verify the complete flow of checking out and placing order for products is working
     * correctly
     */
    @Test(priority = 5, groups = {"Sanity_test"}, description = "Verify that a new user can add multiple products in to the cart and Checkout")
    @Parameters({"TC5_ProductNameToSearchFor", "TC5_ProductNameToSearchFor2", "TC5_AddressDetails"})
    public void TestCase05(@Optional("YONEX ") String product, @Optional("Tan") String product2,
                    @Optional("Addr line 1 addr Line 2 addr line 3") String address)
        throws InterruptedException {
    Boolean status;

    // Go to the Register page
    Register registration = new Register(driver);
    registration.navigateToRegisterPage();

    // Register a new user
    status = registration.registerUser("testUser", "abc@123", true);
    Assert.assertTrue(status, "Test Case Failure. Happy Flow Test Failed");

    // Save the username of the newly registered user
    lastGeneratedUserName = registration.lastGeneratedUsername;

    // Go to the login page
    Login login = new Login(driver);
    login.navigateToLoginPage();

    // Login with the newly registered user's credentials
    status = login.PerformLogin(lastGeneratedUserName, "abc@123");
    Assert.assertTrue(status,
            "User Perform Login Failed --> Test Case 5: Happy Flow Test Failed");

    // Go to the home page
    Home homePage = new Home(driver);
    homePage.navigateToHome();

    // Find required products by searching and add them to the user's cart
    status = homePage.searchForProduct(product);
    homePage.addProductToCart("YONEX Smash Badminton Racquet");
    status = homePage.searchForProduct(product2);
    homePage.addProductToCart("Tan Leatherette Weekender Duffle");

    // Click on the checkout button
    homePage.clickCheckout();

    // Add a new address on the Checkout page and select it
    Checkout checkoutPage = new Checkout(driver);
    checkoutPage.addNewAddress(address);
    checkoutPage.selectAddress(address);

    // Place the order
    checkoutPage.placeOrder();

    WebDriverWait wait = new WebDriverWait(driver, 30);
    wait.until(ExpectedConditions
            .urlToBe("https://crio-qkart-frontend-qa.vercel.app/thanks"));

    // Check if placing order redirected to the Thansk page
    status = driver.getCurrentUrl().endsWith("/thanks");

    // Go to the home page
    homePage.navigateToHome();

    // Log out the user
    homePage.PerformLogout();

    }

    // /*
    // * Verify the quantity of items in cart can be updated
    // */
    @Test(priority = 6, groups = {"Regression_Test"},
            description = "Verify that the contents of the cart can be edited")
    @Parameters({"TC6_ProductNameToSearch1", "TC6_ProductNameToSearch2"})
    public void TestCase06(@Optional("Xtend") String product, @Optional("Yarine") String product2)
            throws InterruptedException {
        Boolean status;

        Home homePage = new Home(driver);
        Register registration = new Register(driver);
        Login login = new Login(driver);

        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        Assert.assertTrue(status,
                "User Perform Register Failed --> Test Case 6:  Verify that cart can be edited");

        lastGeneratedUserName = registration.lastGeneratedUsername;

        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        Assert.assertTrue(status,
                "User Perform Login Failed --> Test Case 6:  Verify that cart can be edited:");

        homePage.navigateToHome();
        status = homePage.searchForProduct(product);
        homePage.addProductToCart("Xtend Smart Watch");

        status = homePage.searchForProduct(product2);
        homePage.addProductToCart("Yarine Floor Lamp");

        // update watch quantity to 2
        homePage.changeProductQuantityinCart("Xtend Smart Watch", 2);

        // update table lamp quantity to 0
        homePage.changeProductQuantityinCart("Yarine Floor Lamp", 0);

        // update watch quantity again to 1
        homePage.changeProductQuantityinCart("Xtend Smart Watch", 1);

        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress("Addr line 1 addr Line 2 addr line 3");
        checkoutPage.selectAddress("Addr line 1 addr Line 2 addr line 3");

        checkoutPage.placeOrder();

        try {
            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(
                    ExpectedConditions.urlToBe("https://crio-qkart-frontend-qa.vercel.app/thanks"));
        } catch (TimeoutException e) {
            System.out.println("Error while placing order in: " + e.getMessage());
            status = false;
        }

        status = driver.getCurrentUrl().endsWith("/thanks");
        Assert.assertTrue(status, "The URL of the final page caontains /thanks");
        Assert.assertTrue(status, "Test case 6: Order Places successfully");
        System.out.println(status);

        homePage.navigateToHome();
        homePage.PerformLogout();
    }


    /*
     * Verify that the cart contents are persisted after logout
     */
//     @Test(priority = 7, groups = {"Regression_Test"},
//             description = "Verify that the contents made to the cart are saved against the user's login details")
//     @Parameters({"TC7_ProductsToAddToCart", "TC7_ProductsToAddToCart2"})
//     public void TestCase07(@Optional("Stylecon") String product, @Optional("Xtend") String product2)
//             throws InterruptedException {
//         Boolean status = false;
//         List<String> expectedResult =
//                 Arrays.asList("Stylecon 9 Seater RHS Sofa Set", "Xtend Smart Watch");

//         Register registration = new Register(driver);
//         Login login = new Login(driver);
//         Home homePage = new Home(driver);

//         registration.navigateToRegisterPage();
//         status = registration.registerUser("testUser", "abc@123", true);
//         Assert.assertTrue(status,
//                 "Userperform login Failed --> Test Case 7: Verify that cart contents are persisted after logout ");

//         lastGeneratedUserName = registration.lastGeneratedUsername;

//         login.navigateToLoginPage();
//         status = login.PerformLogin(lastGeneratedUserName, "abc@123");
//         Assert.assertTrue(status,
//                 "Userperform login Failed --> Test Case 7: Verify that cart contents are persisted after logout ");

//         homePage.navigateToHome();
//         status = homePage.searchForProduct(product);
//         homePage.addProductToCart("Stylecon 9 Seater RHS Sofa Set");

//         status = homePage.searchForProduct(product2);
//         homePage.addProductToCart("Xtend Smart Watch");

//         homePage.PerformLogout();

//         login.navigateToLoginPage();
//         status = login.PerformLogin(lastGeneratedUserName, "abc@123");

//         status = homePage.verifyCartContents(expectedResult);
//         Assert.assertTrue(status,
//                 "Test case 7: Verify that cart contents are persisted after logout ");

//         homePage.PerformLogout();
//     }

    @Test(priority = 7, groups = {"Sanity_test"},
            description = "Verify that insufficient balance error is thrown when the wallet balance is not enough")
    @Parameters({"TC7_ProductsToAddToCart", "TC7_ProductsQuantity"})
    public void TestCase07(@Optional("Stylecon") String product, @Optional("60") String quantity)
            throws InterruptedException {
        Boolean status;

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        Assert.assertTrue(status,
                "Test case 7: Verify that insufficient balance error is thrown when the wallet balance is not enough");

        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        Assert.assertTrue(status,
                "User Perform Login Failed --> Verify that insufficient balance error is thrown when the wallet balance is not enough");

        Home homePage = new Home(driver);
        homePage.navigateToHome();
        status = homePage.searchForProduct(product);
        homePage.addProductToCart("Stylecon 9 Seater RHS Sofa Set ");

        homePage.changeProductQuantityinCart("Stylecon 9 Seater RHS Sofa Set ",
                Integer.parseInt(quantity));

        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress("Addr line 1 addr Line 2 addr line 3");
        checkoutPage.selectAddress("Addr line 1 addr Line 2 addr line 3");

        checkoutPage.placeOrder();
        Thread.sleep(3000);

        status = checkoutPage.verifyInsufficientBalanceMessage();
        Assert.assertTrue(status,
                "Testcase07: The error message You do not have enough balance in your wallet for this purchase is displayed succesfully");
        System.out.println(status);
    }

    @Test(priority = 8, groups = {"Regression_Test"},
            description = "Verify that a product added to a cart is available when a new tab is added")
    public void TestCase08() throws InterruptedException {
        Boolean status = false;

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        Assert.assertTrue(status,
                "Test case 8 --> Test Case Failure. Verify that product added to cart is available when a new tab is opened");

        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        Assert.assertTrue(status, "Step Failure: User Perform Login Failed");

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        status = homePage.searchForProduct("YONEX");
        homePage.addProductToCart("YONEX Smash Badminton Racquet");

        String currentURL = driver.getCurrentUrl();

        driver.findElement(By.linkText("Privacy policy")).click();
        Set<String> handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);

        driver.get(currentURL);
        Thread.sleep(2000);

        List<String> expectedResult = Arrays.asList("YONEX Smash Badminton Racquet");
        status = homePage.verifyCartContents(expectedResult);

        driver.close();

        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);

        Assert.assertTrue(status,
                "Test Case 8: Verify that product added to cart is available when a new tab is opened");
    }

    @Test(priority = 9, groups = {"Regression_Test"},
            description = "Verify that privacy policy and about us links are working fine")
    public void TestCase09() throws InterruptedException {
        Boolean status = false;

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        Assert.assertTrue(status,
                "TestCase 9: Test Case Failure -->Verify that the Privacy Policy, About Us are displayed correctly ");

        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        Assert.assertTrue(status,
                "User perform login Failed --> Test Case 9:    Verify that the Privacy Policy, About Us are displayed correctly");

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        String basePageURL = driver.getCurrentUrl();

        driver.findElement(By.linkText("Privacy policy")).click();
        status = driver.getCurrentUrl().equals(basePageURL);
        Assert.assertTrue(status,
                "Test Case09 Failed: Verifying parent page url didn't change on privacy policy link click failed ");

        Set<String> handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);
        WebElement PrivacyPolicyHeading =
                driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/h2"));
        status = PrivacyPolicyHeading.getText().equals("Privacy Policy");
        Assert.assertTrue(status,
                "Test Case09 Failed: Verifying new tab opened has Privacy Policy page heading failed");

        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);
        driver.findElement(By.linkText("Terms of Service")).click();

        handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[2]);
        WebElement TOSHeading =
                driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/h2"));
        status = TOSHeading.getText().equals("Terms of Service");
        Assert.assertTrue(status,
                "Test Case09 Failed: Verifying new tab opened has Terms Of Service page heading failed");

        driver.close();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]).close();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);
    }

    @Test(priority = 10, groups = {"Regression_Test"},
            description = "Verify that the contact us dialog works fine")
    @Parameters({"TestCase10_name", "TestCase10_email", "TestCase11_query"})
    public void TestCase10(@Optional("crio user") String Name,
                           @Optional("criouser@gmail.com") String Email,
                           @Optional("Testing the contact us page") String Query)
            throws InterruptedException {

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        driver.findElement(By.xpath("//*[text()='Contact us']")).click();

        WebElement name = driver.findElement(By.xpath("//input[@placeholder='Name']"));
        name.sendKeys(Name);
        WebElement email = driver.findElement(By.xpath("//input[@placeholder='Email']"));
        email.sendKeys(Email);
        WebElement message =
                driver.findElement(By.xpath("//input[@placeholder='Message']"));
        message.sendKeys(Query);

        WebElement contactUs = driver.findElement(By.xpath(
                "/html/body/div[2]/div[3]/div/section/div/div/div/form/div/div/div[4]/div/button"));

        contactUs.click();

        WebDriverWait wait = new WebDriverWait(driver, 30);
        Assert.assertTrue(wait.until(ExpectedConditions.invisibilityOf(contactUs)));

    }

    @Test(priority = 11, groups = {"Sanity_test"},
            description = "Ensure that the Advertisement Links on the QKART page are clickable")
    @Parameters({"TC11_ProductNameToSearchFor", "TC11_address"})
    public void TestCase11(@Optional("YONEX Smash Badminton Racquet") String product,
                           @Optional("Addr line 1  addr Line 2  addr line 3") String address)
            throws InterruptedException {
        Boolean status = false;

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        Assert.assertTrue(status,
                "Test Case 11 Failed: Ensure that the links on the QKART advertisement are clickable");

        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        Assert.assertTrue(status,
                "User Perform Login Failed --> Test Case 11: Ensure that the links on the QKART advertisement are clickable ");

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        status = homePage.searchForProduct(product);
        homePage.addProductToCart("YONEX Smash Badminton Racquet");
        homePage.changeProductQuantityinCart("YONEX Smash Badminton Racquet", 1);
        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress(address);
        checkoutPage.selectAddress("Addr line 1  addr Line 2  addr line 3");
        checkoutPage.placeOrder();
        Thread.sleep(3000);

        String currentURL = driver.getCurrentUrl();

        List<WebElement> Advertisements = driver.findElements(By.xpath("//iframe"));

        status = Advertisements.size() == 3;
        Assert.assertTrue(status, "Verify that 3 Advertisements are available");

        WebElement Advertisement1 = driver.findElement(
                By.xpath("//*[@id=\"root\"]/div/div[2]/div/iframe[1]"));
        driver.switchTo().frame(Advertisement1);

        status = driver.findElement(By.xpath("//button[text()='Buy Now']")).getText().equalsIgnoreCase("Buy Now");
        Assert.assertTrue(status, "Successfully Switching over to advertisment frame 1");

        driver.findElement(By.xpath("//button[text()='Buy Now']")).click();
        status = !driver.getCurrentUrl().equals(currentURL);
        Assert.assertTrue(status, "Clicking the buy now button on the advertisement1 page");

        driver.navigate().back();

        driver.switchTo().parentFrame();
        status = driver.getCurrentUrl().equals(currentURL);
        Assert.assertTrue(status, "Successfully switching over to the parent frame");

        driver.get(currentURL);
        Thread.sleep(3000);

        WebElement Advertisement2 = driver.findElement(
                By.xpath("//*[@id=\"root\"]/div/div[2]/div/iframe[2]"));
        driver.switchTo().frame(Advertisement2);

        status = driver.findElement(By.xpath("//button[text()='Buy Now']")).getText().equalsIgnoreCase("Buy Now");
        Assert.assertTrue(status, "Successfully Switching over to advertisment frame 2");

        driver.findElement(By.xpath("//button[text()='Buy Now']")).click();
        status = !driver.getCurrentUrl().equals(currentURL);
        Assert.assertTrue(status, "Successfully Clicking the buy now button on the advertisement2 page");

        driver.navigate().back();

        driver.switchTo().parentFrame();
        status = driver.getCurrentUrl().equals(currentURL);
        Assert.assertTrue(status, "Successfully switching over to the parent frame");

    }




    @AfterSuite
    public static void quitDriver() {
        System.out.println("quit()");
        driver.quit();
    }

    public static void logStatus(String type, String message, String status) {

        System.out.println(String.format("%s |  %s  |  %s | %s",
                String.valueOf(java.time.LocalDateTime.now()), type, message, status));
    }

    public static void takeScreenshot(WebDriver driver, String screenshotType, String description) {
        try {
            File theDir = new File("/screenshots");
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            String timestamp = String.valueOf(java.time.LocalDateTime.now());
            String fileName = String.format("screenshot_%s_%s_%s.png", timestamp, screenshotType,
                    description);
            TakesScreenshot scrShot = ((TakesScreenshot) driver);
            File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
            File DestFile = new File("screenshots/" + fileName);
            FileUtils.copyFile(SrcFile, DestFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


