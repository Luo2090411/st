package uk.ac.ed.inf;



import uk.ac.ed.inf.ilp.data.LngLat;

import java.util.Objects;

class point {

    double row, col;   // Cell position
    double angle,f, g, h;    // A* algorithm value parameters
    point parent;    // Parent record: come from
    LngLat position; // a esaier way to get lnglat

    public point(double row, double col) {
        this.row = row;
        this.col = col;
        this.position =new LngLat(row,col);



        this.parent = null;
        this.angle = 0;

        f = 0;
        g = 0;
        h = 0;
    }
    public LngLat getLngLat(){
        return this.position;
    }
    public double getRow(){return this.row;}
    public double getCol(){return this.col;}

    public double getAngle(){return this.angle;}
    public double getF(){return this.f;}

    public double getg(){return this.g;}
    public double getH(){return this.h;}
    public void setParent(point A){this.parent = A;}

    @Override
    public int hashCode(){
        return Objects.hash(row, col);
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }

        if(obj == null || getClass() != obj.getClass()){
            return false;
        }

        point other = (point)obj;
        return other.row == row && other.col == col;
    }

}
