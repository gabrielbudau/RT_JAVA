package ray.tracer;

/**
 *
 * @author Gabriel
 */
public class Ray {
    Vec origin, direction;
    
    Ray(final Vec o, final Vec d){
        this.origin = o;
        this.direction = d;
    }
}
