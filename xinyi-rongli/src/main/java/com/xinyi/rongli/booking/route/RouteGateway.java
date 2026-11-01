package com.xinyi.rongli.booking.route;

public interface RouteGateway {
    record Point(double lng,double lat) { }
    record Route(long distanceMeter,long durationSecond,String provider,String policy) { }
    Route driving(Point from,Point to);
}
