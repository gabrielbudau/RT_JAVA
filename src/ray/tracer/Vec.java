package ray.tracer;

/**
 *
 * @author Gabriel
 */
public class Vec {
    double x,y,z;
    
    Vec (){
        this.x = this.y = this.z = 0.0;
    }
    
    Vec(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vec add(final Vec v){
        return new Vec(
                this.x + v.x,
                this.y + v.y,
                this.z + v.z
        );
    }
    
    public Vec diff(final Vec v){
        return new Vec(
                this.x - v.x,
                this.y - v.y,
                this.z - v.z
        );
    }
    
    public Vec multC(final double v){
        return new Vec(
                this.x * v,
                this.y * v,
                this.z * v
        );
    }
    
    public Vec mult(final Vec v){
        return new Vec(
                this.x * v.x,
                this.y * v.y,
                this.z * v.z
        );
    }
    
    public Vec norm(){
        Vec temp = this;
        temp.multC(1/Math.sqrt(x * x + y * y + z * z));
        this.x = temp.x;
        this.y = temp.y;
        this.z = temp.z;
        return temp;
    }
    
    public double dot(final Vec v){
        return this.x*v.x + this.y * v.y + this.z * v.z;
    }
    
    public Vec mod(final Vec v){
        return new Vec(
                this.y * v.z - this.z * v.y,
                this.z * v.x - this.x * v.z,
                this.x * v.y - this.y * v.x
        );
    }
}
