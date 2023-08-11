package QKART_TESTNG;

import java.io.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;


public class DP  {
    XSSFWorkbook workbook =null;
    XSSFSheet sheet = null;
    XSSFRow row= null;
    XSSFCell cell = null;
    Object obj[][]=null;
    int lastRow= 0;
    int lastColumn=0;

    @DataProvider (name = "data-provider")
    public Object[][] dpMethod(Method m) throws IOException{
        String methodName= m.getName();
       switch(methodName){
        case "TestCase01":
        setupExcel();
       return readDataFromExcel(methodName);
        case "TestCase02":
        setupExcel();
       return readDataFromExcel(methodName);
        case "TestCase03":
        setupExcel();
        return readDataFromExcel(methodName);
        case "TestCase04":
        setupExcel();
        return readDataFromExcel(methodName);
        case "TestCase05":
        setupExcel();
        return readDataFromExcel(methodName);
        case "TestCase06":
        setupExcel();
        return readDataFromExcel(methodName);
        case "TestCase07":
        setupExcel();
        return readDataFromExcel(methodName);
        case "TestCase08" :
        setupExcel();
        return readDataFromExcel(methodName);
        case "TestCase11" :
        setupExcel();
        return readDataFromExcel(methodName);
        case "TestCase12" :
        setupExcel();
        return readDataFromExcel(methodName);
       }
        return obj;
    }


    public Object[][] readDataFromExcel(String sheetName){
        sheet=workbook.getSheet(sheetName);
        lastRow=sheet.getLastRowNum();
        lastColumn = sheet.getRow(0).getLastCellNum();
        obj = new Object[lastRow][lastColumn-1]; 
       for (int i=0 ; i<=obj.length-1;i++){
            row=sheet.getRow(i+1);
           for (int j =0 ; j<=obj[0].length-1;j++){
                cell=row.getCell(j+1);
               obj[i][j]=cell.getStringCellValue();
           }
       }
       return obj;
    }

    public  void setupExcel(){
        File file = new File(System.getProperty("user.dir") + "/src/test/resources/Dataset.xlsx");
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
             workbook = new XSSFWorkbook(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    
}
}