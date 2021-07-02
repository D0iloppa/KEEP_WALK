package com.doiloppa.keepwalk;

public class GetDist {
    double myKG = 60.0;
    int c_MET = 4;


    double getDist(int kcal) {
        double dist = 0.0;
        double min = kcal * 1000 / (c_MET * myKG * 3.5 * 5);
        dist = (6.0 / 60) * min;

        return dist;
    }
}
