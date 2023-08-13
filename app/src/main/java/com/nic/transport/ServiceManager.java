package com.nic.transport;

public class ServiceManager {

 // public static String BASE_URL = "http://10.132.36.244:80/Apitesting/ApkWebService.asmx/";

 public static String BASE_URL = "https://cgtransport.gov.in/apitesting/ApkWebService.asmx/";


 public static String RegisterURL = BASE_URL+"RegisterNewUser";
 public static String GetOTP = BASE_URL+"CheckRegisteredUser";
 public static String DashBoardOffenseCount = BASE_URL+"ServiceCount";
 public static String GetRegistrationDetails = BASE_URL+"GetRegistrationDetails";
 public static String ListServiceDetails = BASE_URL+"ListServiceDetails";
 public static String ListServiceAvailable = BASE_URL+"ListServiceAvailable";
 public static String UserLogin = BASE_URL+"UserLogin";
 public static String GetOTPText = BASE_URL+"GetOTPText";
 public static String SaveImageData = BASE_URL+"SaveImageData";
 public static String SaveServiceDetails = BASE_URL+"SaveServiceDetails";

}
