package ray.tracer;

import java.util.ArrayList;

/**
 *
 * @author Gabriel
 */
public class RayTracer {

    static final double PI = 3.14159265359;
    ArrayList<Sphere> Spheres;

    public RayTracer() {
        Spheres = new ArrayList<>();

        Spheres.add(new Sphere(1e5, new Vec(1e5 + 1, 40.8, 81.6), new Vec(), new Vec(.75, .25, .25), Refl_t.DIFF));//Left
        Spheres.add(new Sphere(1e5, new Vec(-1e5 + 99, 40.8, 81.6), new Vec(), new Vec(.25, .25, .75), Refl_t.DIFF));//Right
        Spheres.add(new Sphere(1e5, new Vec(50, 40.8, 1e5), new Vec(), new Vec(.75, .75, .75), Refl_t.DIFF));//Back
        Spheres.add(new Sphere(1e5, new Vec(50, 40.8, -1e5 + 170), new Vec(), new Vec(), Refl_t.DIFF));//Front
        Spheres.add(new Sphere(1e5, new Vec(50, 1e5, 81.6), new Vec(), new Vec(.75, .75, .75), Refl_t.DIFF));//Bottom
        Spheres.add(new Sphere(1e5, new Vec(50, -1e5 + 81.6, 81.6), new Vec(), new Vec(.75, .75, .75), Refl_t.DIFF));//Top
        Spheres.add(new Sphere(16.5, new Vec(27, 16.5, 47), new Vec(), new Vec(1, 1, 1).multC(.999), Refl_t.SPEC));//Mirror
        Spheres.add(new Sphere(16.5, new Vec(73, 16.5, 78), new Vec(), new Vec(1, 1, 1).multC(.999), Refl_t.REFR));//Glass
        Spheres.add(new Sphere(600, new Vec(50, 681.6 - .27, 81.6), new Vec(12, 12, 12), new Vec(), Refl_t.DIFF)); //Light
    }

    double erand48(short xsubi) {
        return Math.random() / Double.MAX_VALUE;
    }

    double clamp(final double x) {
        if (x < 0.0) {
            return 0.0;
        } else if (x > 1.0) {
            return 1.0;
        } else {
            return x;
        }
    }

    int toInt(final double x) {
        return (int) (Math.pow(clamp(x), 1 / 2.2) * 255 + 0.5);
    }

    boolean intersect(final Ray r, double t, int id) {
        int n = Spheres.size();
        double d;
        double inf = 1e20;
        t = 1e20;

        for (int i = n; (i--) != 0;) {
            if (((d = Spheres.get(i).intersect(r)) == 0.0) && (d < t)) {
                t = d;
                id = i;
            }
        }
        return t < inf;
    }

    Vec radiance(final Ray r, int depth, short Xi) {
        double t = 0.0;// distanta la intersectie
        int id = 0;// id-ul obiectului intersectat
        if (!intersect(r, t, id)) {//daca nu intersecteaza intoarce negru
            return new Vec();
        }

        final Sphere obj = Spheres.get(id);// Obiectul lovit
        Vec x = r.origin.add(r.direction.multC(t));
        Vec n = x.diff(obj.position).norm();
        Vec nl = (n.dot(r.direction) < 0.0) ? n : n.multC(-1);
        Vec f = obj.color;
        double p = f.x > f.y && f.x > f.z && f.x > f.z ? f.x : f.y > f.z ? f.y : f.z;// reflectia maxima
        if (++depth > 5) {
            if (erand48(Xi) < p) {
                f = f.multC(1 / p);
            }
        } else {
            return obj.emission;
        }

        if (obj.refl == Refl_t.DIFF) {
            double r1 = 2 * PI * erand48(Xi);
            double r2 = erand48(Xi);
            double r2s = Math.sqrt(r2);
            Vec w = nl;
            Vec u = ((Math.abs(w.x) > 0.1) ? new Vec(0.0, 1.0, 0.0) : (new Vec(1.0, 0.0, 0.0))).mod(w).norm();
            Vec v = w.mod(u);
            Vec d = ((u.multC(Math.cos(r1)).multC(r2s)).add(v.multC(Math.sin(r1)).multC(r2s)).add(w.multC(Math.sqrt(1 - r2)))).norm();
            return obj.emission.add(f.mult(radiance(new Ray(x, d), depth, Xi)));
        } else if (obj.refl == Refl_t.SPEC) {
            return obj.emission.add(f.mult(radiance(new Ray(x, r.direction.diff(n.multC(2 * n.dot(r.direction)))), depth, Xi)));
        }

        //Sticla
        Ray reflRay = new Ray(x, r.direction.diff(n.multC(2 * n.dot(r.direction))));
        boolean into = n.dot(nl) > 0.0;// Raza din exterior merge in interior?
        double nc = 1.0;
        double nt = 1.5;
        double nnt = into ? nc / nt : nt / nc;
        double ddn = r.direction.dot(nl);
        double cos2t;

        if ((cos2t = 1 - nnt * nnt * (1 - ddn * ddn)) < 0.0) {//Reflectie interioara
            return obj.emission.add(f.mult(radiance(reflRay, depth, Xi)));
        }

        Vec tdir = (r.direction.multC(nnt).diff(n.multC((into ? 1 : -1) * (ddn * nnt + Math.sqrt(cos2t))))).norm();
        double a = nt - nc;
        double b = nt + nc;
        double R0 = a*a/(b*b);
        double c = 1 - (into ? -ddn:tdir.dot(n));
        double Re = R0 + (1 - R0) * c * c * c * c * c;
        double Tr = 1 - Re;
        double P = 0.25 + 0.5 * Re;
        double RP = Re / P;
        double TP = Tr / (1 - P);
        
        //Russian Roullete
        return obj.emission.add(f.mult(depth > 2 ? (erand48(Xi) < P ? 
                radiance(reflRay,depth,Xi).multC(RP): radiance(new Ray(x,tdir),depth,Xi).multC(TP)) :
                radiance(reflRay,depth,Xi).multC(Re).add(radiance(new Ray(x,tdir),depth,Xi).multC(Tr))
                ));
    }
    
   Vec[] render(){
        int w = 512;
        int h = 384;
        int samps = 1;
        Ray cam = new Ray(new Vec(50, 52, 295.6), new Vec(0,-0.042612,-1).norm());
        Vec cx = new Vec(w * 0.5135 / h, 0.0, 0.0);
        Vec cy = ((cx.mod(cam.direction)).norm()).multC(0.5135);
        Vec r = new Vec();
        Vec[] c = new Vec[w * h];
        for(int i = 0; i < w * h; i++)
            c[i] = new Vec();
        
        
        for(int y = 0; y < h; y ++){//Iteram pixeli pe linii
            System.out.printf("\r Rendering (%d spp) %5.2f%%", samps * 4, 100.*y/(h-1));
            for(short x = 0, Xi = (short) (y*y*y); x < w; x ++){//iteram coloane
                for(int sy = 0, i = (h - y - 1) * w + x; sy < 2; sy ++){//2x2 linii subpixel
                    for(int sx = 0; sx < 2; sx ++, r = new Vec()){//2x2 coloane subpixel
                        for(int s = 0; s < samps; s ++){
                            double r1 = 2 * erand48(Xi), dx = r1 < 1? Math.sqrt(r1) - 1 : 1 - Math.sqrt(2 - r1);
                            double r2 = 2 * erand48(Xi), dy = r2 < 1? Math.sqrt(r2) - 1 : 1 - Math.sqrt(2 - r2);
                            Vec d = cx.multC((( sx + 0.5 + dx ) / 2 + x) / w - 0.5).add(cy.multC(((sy + 0.5 + dy) / 2 + y) / h - 0.5)).add(cam.direction);
                            r = r.add(radiance(new Ray(cam.origin.add(d.multC(140)), d.norm()),0, Xi).multC(1./samps));
                        }
                        Vec temp = c[i].add(new Vec(clamp(r.x), clamp(r.y), clamp(r.z)).multC(0.25));
                        System.out.println(temp.x + " " + temp.y + " " + temp.z);
                        c[i] = temp;
                    }
                }
            }
        }
        
        return c;
    }
    

    public static void main(String[] args) {
        RayTracer rt = new RayTracer();
        Vec[] a = rt.render();
        System.out.println("Finnished...");
    }

}
