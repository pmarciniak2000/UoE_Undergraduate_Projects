package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BikeType {
    public static Map<BikeType,BigDecimal> replacementValues = new HashMap<BikeType,BigDecimal>();

    private BigDecimal depreciationRate;
    private String typeName;
    
    public BikeType(BigDecimal depreciationRate, String typeName) {
        this.depreciationRate = depreciationRate;
        this.typeName = typeName;
    }
    
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setDepreciationRate(BigDecimal depreciationRate) {
        this.depreciationRate = depreciationRate;
    }

    public BigDecimal getDepreciationRate() {
        return depreciationRate;
    }
    
    public BigDecimal getReplacementValue() {
        return replacementValues.get(typeName);
    }
}