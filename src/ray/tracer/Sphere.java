package ray.tracer;

/**
 *
 * @author Gabriel
 */
public class Sphere {
    double radius;
    Vec position, emission, color;
    Refl_t refl;//tipul reflectiei
    
    Sphere(final double r, final Vec p, final Vec e, final Vec c, final Refl_t re){
        this.radius = r;
        this.position = p;
        this.emission = e;
        this.color = c;
        this.refl = re;
    }
    
    /**
     * Rezolva ecuatia: t^2*d.d + 2*t*(o-p).d + (o-p).(o-p)-R^2 = 0
     * @param r raza pentru care se calculeaza intersectia
     * @return distanta sau 0 daca nu este intersectie
     */
    double intersect(final Ray r){
        Vec op = this.position.diff(r.origin);
        double t;
        double eps = 1e-4;
        double b = op.dot(r.direction);
        double det = b * b - op.dot(op) + this.radius * this.radius;
        if(det < 0.0){
            return 0;
        }else{
            det = Math.sqrt(det);
        }
        
        if((t = b - det) > eps){
            return t;
        }else{
            if((t = b + det) > eps){
                return t;
            }else{
                return 0;
            }
        }
    }
}

enum Refl_t{
    DIFF, SPEC, REFR
}
