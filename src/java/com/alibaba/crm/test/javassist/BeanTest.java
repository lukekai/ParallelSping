package com.alibaba.crm.test.javassist;

public class BeanTest
{
    private Bean m_bean;
    
    private BeanTest() {
        m_bean = new Bean("originalA", "originalB");
    }
    
    private void print() {
        System.out.println("Bean values are " +
            m_bean.getA() + " and " + m_bean.getB());
    }
    
    private void changeValues(String lead) {
        m_bean.setA(lead + "A");
        m_bean.setB(lead + "B");
    }
    
    public static void main(String[] args) {
        BeanTest inst = new BeanTest();
        inst.print();
        inst.changeValues("new");
        inst.print();
    }
}
