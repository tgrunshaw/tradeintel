/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.col.priceowl.web;

public enum Accuracy {

    LOW(1), MEDIUM(0.4), HIGH(0.15);

    private final double upperCoefficientOfVar;

    private Accuracy(double upperCOV) {
        this.upperCoefficientOfVar = upperCOV;
    }

    public double getUpperCoefficientOfVar() {
        return upperCoefficientOfVar;
    }

    public static Accuracy fromCoefficientOfVar(double cov) {
        if (cov <= 0.15) {
            return HIGH;
        } else if (cov <= 0.4) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }
    
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
