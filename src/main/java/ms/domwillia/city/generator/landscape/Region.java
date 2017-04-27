package ms.domwillia.city.generator.landscape;

import ms.domwillia.city.generator.RegionType;

import java.awt.*;
import java.awt.geom.Point2D;

class Region
{
	Point2D.Double centre;
	Polygon area;
	RegionType type;
	double distanceFromCentre;
}
