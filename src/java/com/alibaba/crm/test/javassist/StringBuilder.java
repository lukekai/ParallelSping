package com.alibaba.crm.test.javassist;

public class StringBuilder
{
    public String buildString() {
        String result = "";
        for (int i = 0; i < 2000; i++) {
            result += (char)(i%26 + 'a');
        }
        return result;
    }
    public void test(){
        StringBuilder inst = new StringBuilder();
        {
            String result = inst.buildString();
            System.out.println("Constructed string of length " +
                result.length());
        }
    }
    
    public static void main(String[] argv) {
        StringBuilder inst = new StringBuilder();
        for (int i = 0; i < argv.length; i++) {
            String result = inst.buildString();
            System.out.println("Constructed string of length " +
                result.length());
        }
    }
}
