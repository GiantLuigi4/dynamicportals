import com.mojang.math.Quaternion;

//Class to test quaternion operation, needs to be removed asap
public class QuaternionTest {
	public static void main(String[] args) {

		//Alright, so if I have quaternions "q1" and "q2"
		//q1*q2 => q2.mul(q1);
		//q2^ * q1^ = (q1*q2)^
		//..
		//..

		Quaternion q1 = new Quaternion(2, 3, 4, 1);
		Quaternion q2 = new Quaternion(5, 3, 6, 2);
		Quaternion p = new Quaternion(1, 1, 1, 0);

		Quaternion q1c = q1.copy(); q1c.conj();
		Quaternion q2c = q2.copy(); q2c.conj();

		System.out.println(q1c + "; " + q2c);

		Quaternion q1cTimesQ2c = q2c.copy(); q1cTimesQ2c.mul(q1c);
		Quaternion q2cTimesQ1c = q1c.copy(); q2cTimesQ1c.mul(q2c);

		Quaternion q1TimesQ2Conj = q2.copy(); q1TimesQ2Conj.mul(q1); q1TimesQ2Conj.conj();
		System.out.println(q1cTimesQ2c + "; " + q1TimesQ2Conj);
		System.out.println(q2cTimesQ1c + "; " + "");
	}
}
