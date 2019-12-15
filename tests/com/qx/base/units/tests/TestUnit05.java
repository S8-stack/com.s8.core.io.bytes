package com.qx.base.units.tests;

import com.qx.level0.utilities.units.SI_Unit;
import com.qx.level0.utilities.units.SI_Unit.WrongUnitFormat;

public class TestUnit05 {

	public static void main(String[] args) throws WrongUnitFormat {
		
		// input = "W.m-2.K-2.A-4";
		SI_Unit unit = new SI_Unit("kJ.kg-1.K-2");
		
		System.out.println(unit.convertBack(100.0));
		
		
	}

}
