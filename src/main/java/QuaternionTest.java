import com.mojang.math.Quaternion;

import java.util.Random;

//Class to test quaternion operation, needs to be removed asap
public class QuaternionTest {
	public static void main(String[] args) {
		
		//Alright, so if I have quaternions "q1" and "q2"
		//q1*q2 => q2.mul(q1);
		//q2^ * q1^ = (q1*q2)^
		//..
		//..
		Random r = new Random();
		for (int i = 0; i < 200; i++) {
			
			int a = r.nextInt(100), b = r.nextInt(100), c = r.nextInt(100);
			Quaternion q = Quaternion.ONE.copy();
			q.mul(new Quaternion(0, a, 0, true));
			q.mul(new Quaternion(b, 0, 0, true));
			q.mul(new Quaternion(0, 0, c, true));
			
			Quaternion p = Quaternion.fromYXZ((float) Math.toRadians(a), (float) Math.toRadians(b), (float) Math.toRadians(c));
			if (!q.equals(p)) {
				System.out.println(q + "; " + p);
			}
		}
		
		Quaternion q1 = Quaternion.ONE.copy();
		q1.mul(new Quaternion(0, 10, 0, true));
		q1.mul(new Quaternion(20, 0, 0, true));
		q1.mul(new Quaternion(0, 0, 30, true));
		
		Quaternion q2 = Quaternion.ONE.copy();
		q2.mul(new Quaternion(0, 0, -30, true));
		q2.mul(new Quaternion(-20, 0, 0, true));
		q2.mul(new Quaternion(0, -10, 0, true));
		
		System.out.println(q1 + "; " + q2);
	}
}
