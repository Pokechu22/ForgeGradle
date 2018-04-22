public class Client {
	public String clientString;

	public void foo(int n) {
		int local = 2 * n;
		System.out.println(clientString + local);
	}
}