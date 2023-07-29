package QKART_TESTNG;
        import org.testng.ITestListener;
        import org.testng.ITestResult;

public class ListenerClass extends QKART_Tests implements ITestListener {
    public void onTestStart(ITestResult result){
        System.out.println("Test Started" + result.getName());
        QKART_Tests.takeScreenshot(driver, "Test Case Start", result.getName());
    }

    public void onTestSuccess(ITestResult result){
        System.out.println("Test Success" + result.getName());
        QKART_Tests.takeScreenshot(driver, "Test Case Success", result.getName());
    }

    public void onTestFailure(ITestResult result){
        System.out.println("Test Failed" + result.getName() + "Taking Screenshot");
        QKART_Tests.takeScreenshot(driver, "Test Case Failure", result.getName());
    }

}
