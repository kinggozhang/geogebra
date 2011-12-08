/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

package geogebra.common.kernel.geos;

import geogebra.common.kernel.Path;
import geogebra.common.kernel.roots.RealRootFunction;
//import geogebra.kernel.geos.GeoVec2D;


public interface ParametricCurve extends Traceable, Path {
	double getMinParameter(); 
	double getMaxParameter();	
	
	RealRootFunction getRealRootFunctionX();
	RealRootFunction getRealRootFunctionY();
			
	void evaluateCurve(double t, double [] out);
	GeoVec2D evaluateCurve(double t);	

	double evaluateCurvature(double t);	
	boolean isFunctionInX();
    
	GeoElement toGeoElement();
}
