package ray.tracer;

import java.util.*;
import java.io.*;

public final class Test {
    double delta=Math.sqrt(Math.ulp(1.0)), infinity=Float.POSITIVE_INFINITY;

    class Vec {
	public double x, y, z;
	public Vec(double x2, double y2, double z2) { x=x2; y=y2; z=z2; }
    }

    Vec add(Vec a, Vec b) { return new Vec(a.x+b.x, a.y+b.y, a.z+b.z); }
    Vec sub(Vec a, Vec b) { return new Vec(a.x-b.x, a.y-b.y, a.z-b.z); }
    Vec scale(double s, Vec a) { return new Vec(s*a.x, s*a.y, s*a.z); }
    double dot(Vec a, Vec b) { return a.x*b.x + a.y*b.y + a.z*b.z; }
    double length(Vec a) { return Math.sqrt(dot(a, a)); }
    Vec unitise(Vec a) { return scale(1 / length(a), a); }

    class Ray {
	public Vec orig, dir;

	public Ray(Vec o, Vec d) { orig=o; dir=d; }
    }

    class Hit {
	public double lambda;
	public Vec normal;

 	public Hit(double l, Vec n) { lambda=l; normal=n; }
    }

    abstract class Scene {
	abstract public void intersect(Hit i, Vec dir);
	abstract public boolean sintersect(Ray ray);
	abstract public Sphere bound(Sphere b);
    }

    class Sphere extends Scene {
	public Vec center;
	public double radius;

	public Sphere(Vec c, double r) { center=c; radius=r; }

	public double ray_sphere(Vec dir) {
	    double b = center.x*dir.x + center.y*dir.y + center.z*dir.z;
	    double disc = b*b - (center.x*center.x + center.y*center.y + center.z*center.z) + radius * radius;
	    if (disc < 0) return infinity;
	    double d = Math.sqrt(disc), t2 = b+d ;
	    if (t2 < 0) return infinity;
	    double t1 = b-d;
	    return (t1 > 0 ? t1 : t2);
	}

	public void intersect(Hit i, Vec dir) {
	    double l = ray_sphere(dir);
	    if (l >= i.lambda) return;
	    i.normal.x = l*dir.x - center.x;
	    i.normal.y = l*dir.y - center.y;
	    i.normal.z = l*dir.z - center.z;
	    double len = Math.sqrt(i.normal.x*i.normal.x +
				   i.normal.y*i.normal.y +
				   i.normal.z*i.normal.z);
	    i.normal.x /= len;
	    i.normal.y /= len;
	    i.normal.z /= len;
	    i.lambda = l;
	}

	public boolean sintersect(Ray ray) {
	    double
		vx = center.x - ray.orig.x,
		vy = center.y - ray.orig.y,
		vz = center.z - ray.orig.z;
	    double b = vx*ray.dir.x + vy*ray.dir.y + vz*ray.dir.z;
	    double disc = b*b - (vx*vx + vy*vy + vz*vz) + radius * radius;
	    return disc >= 0 && b + Math.sqrt(disc) >= 0;
	}

	public Sphere bound(Sphere b) {
	    double s = length(sub(b.center, center)) + radius;
	    return new Sphere(b.center, (b.radius > s ? b.radius : s));
	}
    }

    class Group extends Scene {
	public Sphere bound;
	public Scene[] objs;

	public Group(Sphere b, Scene[] o) {
	    bound = b;
	    objs = o;
	}

	public void intersect(Hit i, Vec dir) {
	    double l = bound.ray_sphere(dir);
	    if (l >= i.lambda) return;
		
	    for(Scene scene : objs )
	    	scene.intersect(i, dir);
	}

	public boolean sintersect(Ray ray) {
	    if (!bound.sintersect(ray)) return false;
	    for(Scene scene : objs )
		if (scene.sintersect(ray)) return true;
	    return false;
	}

	public Sphere bound(Sphere b) {
	    for(Scene scene : objs )
	    	b = scene.bound(b);
	    return b;
	}
    }

    double ray_trace(Vec neg_light, Vec dir, Scene scene) {
	Hit i = new Hit(infinity, new Vec(0, 0, 0));
	scene.intersect(i, dir);
	if (i.lambda == infinity) return 0;
	double g = dot(i.normal, neg_light);
	if (g <= 0) return 0.;
	Vec o = add(scale(i.lambda, dir), scale(delta, i.normal));
	return (scene.sintersect(new Ray(o, neg_light)) ? 0 : g);
    }

    Scene create(int level, Vec c, double r) {
	Sphere sphere = new Sphere(c, r);
	if (level == 1) return sphere;
	double x = 3*r/Math.sqrt(12);
	Scene[] objs = new Scene[1 + 2 * 2];
	int index = 0 ;
	objs[index++] = sphere;
	Sphere b = new Sphere(add(c, new Vec(0, r, 0)), 2*r);
	for (int dz=-1; dz<=1; dz+=2)
	    for (int dx=-1; dx<=1; dx+=2) {
		Vec c2 = new Vec(c.x+dx*x, c.y+x, c.z+dz*x);
		Scene scene = create(level-1, c2, r/2);
		objs[index++] = scene;
		b = scene.bound(b);
	    }
	return new Group(b, objs);
    }

    void run(int n, int level, int ss) throws java.io.IOException {
	Scene scene = create(level, new Vec(0, -1, 4), 1);
	BufferedOutputStream out =
	    new BufferedOutputStream(new FileOutputStream("image.pgm"));
	out.write(("P5\n"+n+" "+n+"\n255\n").getBytes());
	for (int y=n-1; y>=0; --y)
	    for (int x=0; x<n; ++x) {
		double g=0;
		for (int dx=0; dx<ss; ++dx)
		    for (int dy=0; dy<ss; ++dy) {
			Vec d = new Vec(x+dx*1./ss-n/2., y+dy*1./ss-n/2., n);
               
			g += ray_trace(unitise(new Vec(1, 3, -2)),
				       unitise(d), scene);
		    }
                out.write((int)(.5+255.*g/(ss*ss)));
	    }
	out.close();
    }

    public static void main(String[] args) throws java.io.IOException {
	    (new Test()).run(512, 3, 4);
    }
}