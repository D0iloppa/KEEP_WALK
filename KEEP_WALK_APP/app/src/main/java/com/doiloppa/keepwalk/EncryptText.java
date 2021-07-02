package com.doiloppa.keepwalk;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptText {
    public String encryptSHA256(String str){
        String sha = "";

        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteDate[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i=0;i<byteDate.length;i++)
                sb.append(Integer.toString(byteDate[i]&0xff,16).substring(1));
            sha = sb.toString();


        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            sha = null;
        }
        return sha;
    }

}
